package com.tt.service;

import com.tt.dao.VideoDao;
import com.tt.domain.*;
import com.tt.domain.exception.ConditionException;
import com.tt.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName: VideoService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/6/12 11:15
 */
@Service
public class VideoService {
    @Autowired
    private VideoDao videoDao;
    @Autowired
    private FastDFSUtil fastDFSUtil;
    //@Autowired
    //private UserCoinService userCoinService;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private UserCoinService userCoinService;

    //用户添加视频时,也会传视频标签,将其作为冗余字段参与VideoTag数据的制作
    @Transactional //进行了两次数据库操作,所以要加事务
    public void addVideos(Video video) {
        Date now = new Date();
        video.setCreateTime(now);
        videoDao.addVideos(video);
        Long videoId = video.getId();
        //补充视频类的冗余字段，将VideoTag关联表的信息插入数据库
        List<VideoTag> tagList = video.getVideoTagList();
        tagList.forEach(item -> {
            item.setVideoId(videoId);
            item.setCreateTime(now);
        });
        videoDao.batchAddVideoTags(tagList);
    }

    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        if (size == null || no == 0) {
            throw new ConditionException("参数异常!");
        }
        //需要知道从第几条数据开始查，要查多少条，知道这些后就能进数据库了
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        params.put("area", area);
        List<Video> list = new ArrayList<>();
        //先做第一次筛选,去数据库查询看看符合条件的数据有多少条，就是说看看所查的分区有没有数据
        Integer total = videoDao.pageCountVideos(params);
        if (total > 0) {
            //真正开始查页面数据
            list = videoDao.pageListVideos(params);
        }
        PageResult<Video> result = new PageResult<>(total, list);
        return result;
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
        fastDFSUtil.viewVideoOnlineBySlices(request, response, url);
    }

    public void addVideoLike(Long videoId, Long userId) {
        //首先，判断下这个视频存不存在
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("视频不存在");
        }
        //检查这个点赞操作是否已经做过
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        if (videoLike != null) {
            throw new ConditionException("你已点赞过该视频");
        }
        Date createTime = new Date();
        videoDao.addVideoLike(videoId, userId, createTime);
    }

    public void deleteVideoLike(Long videoId, Long userId) {
        //首先，判断下这个视频存不存在
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("视频不存在");
        }
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        if (videoLike == null) {
            throw new ConditionException("你未点赞过改视频");
        }
        videoDao.deleteVideoLike(videoLike);
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        Long count = videoDao.getVideoLikes(videoId);
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        Map<String, Object> result = new HashMap<>();
        // 如果videoLike为空，说明是游客模式
        if (videoLike == null) {
            result.put("like", false);
        } else result.put("like", true);
        result.put("count", count);
        return result;
    }

    public void addVideoCoins(VideoCoin videoCoin) {
        //查询要投币的视频是否存在...
        Long userId = videoCoin.getUserId();
        Long videoId = videoCoin.getVideoId();
        if (videoId == null || videoId == 0) {
            throw new ConditionException("视频id非法");
        }
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("视频不存在");
        }
        //判断用户的硬币数量是否足够
        Integer postAmount = videoCoin.getAmount(); //想要投的硬币数量
        Integer coinsAmount = userCoinService.getUserCoinsAmount(userId); //用户的硬币数量
        coinsAmount = coinsAmount == null ? 0 : coinsAmount;
        if (postAmount > coinsAmount) {
            throw new ConditionException("用户硬币数量不足,无法投币");
        }
        //查询之前是否给这个视频投过币
        VideoCoin dbVideoCoin = videoDao.getVideoCoinByUserIdAndVideoId(userId, videoId);
        if (dbVideoCoin != null) {
            videoCoin.setUpdateTime(new Date());
            videoCoin.setAmount(dbVideoCoin.getAmount() + postAmount);
            videoDao.updateVideoCoins(videoCoin);
        } else {
            videoCoin.setCreateTime(new Date());
            videoDao.postVideoCoins(videoCoin);
        }
        //更新用户硬币数量
        userCoinService.updateUserCoin(userId, (long) (coinsAmount - postAmount));
    }

    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Map<String, Object> map = new HashMap<>();
        Long count = videoDao.getVideoCoinsAmount(videoId);
        map.put("count", count);
        VideoCoin videoCollection = videoDao.getVideoCoinByUserIdAndVideoId(userId, videoId);
        Boolean postStatus = (videoCollection != null);
        map.put("postStatus", postStatus);
        return map;
    }

    public void addVideoComment(VideoComment videoComment, Long userId) {
        /*判断视频是否存在*/
        Long videoId = videoComment.getVideoId();
        if (videoId == null) {
            throw new ConditionException("参数非法");
        }
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("视频不存在");
        }
        videoComment.setUserId(userId);//设置当前评论的用户的id
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    public PageResult<VideoComment> pageListVideoComments(Integer no, Integer size, Long videoId) {
        /*判断视频是否存在*/
        if (videoId == null) {
            throw new ConditionException("参数非法");
        }
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("视频不存在");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        params.put("videoId", videoId);
        //查询一级评论的条数
        Integer total = videoDao.countVideoComments(videoId);
        List<VideoComment> parentCommentList = new ArrayList<>();
        if (total > 0) {
            parentCommentList = videoDao.pageListVideoComments(params);
            //由一级评论列表得到一级评论的用户id集合
            Set<Long> parentUserIdsSet = parentCommentList.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            //由一级评论的用户id集合得到一级评论的用户信息列表
            List<UserInfo> parentUserInfos = userService.getUserInfoByUserIds(parentUserIdsSet);
            //由一级评论列表得到一级评论的评论id集合
            Set<Long> parentCommentIdsSet = parentCommentList.stream().map(VideoComment::getId).collect(Collectors.toSet());
            //由一级评论的评论id集合得到二级评论列表
            List<VideoComment> childCommentList = videoDao.getAllChildCommentByCommentIds(parentCommentIdsSet);
            //由二级评论列表得到二级评论的用户id集合
            Set<Long> replyUserIdsSet = childCommentList.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            //由二级评论的用户id集合得到二级评论的用户信息列表
            List<UserInfo> replyUserInfos = userService.getUserInfoByUserIds(replyUserIdsSet);
            //冗余字段大封装，从外到里
            parentCommentList.forEach(parentComment -> {  //遍历一级评论列表，给每个一级评论对象填充用户信息和二级评论列表
                Long userId = parentComment.getUserId();
                Long id = parentComment.getId();
                List<VideoComment> childComments = new ArrayList<>();
                parentUserInfos.forEach(userInfo -> {
                    if (userId.equals(userInfo.getUserId())) {
                        parentComment.setUserInfo(userInfo);
                    }
                });
                childCommentList.forEach(childComment -> {  //
                    Long childUserId = childComment.getUserId();
                    Long replyUserId = childComment.getReplyUserId();
                    replyUserInfos.forEach(replyUserInfo -> {
                        if (replyUserId.equals(replyUserInfo.getUserId())) {
                            childComment.setReplyUserInfo(replyUserInfo);
                        }
                    });
                    parentUserInfos.forEach(userInfo -> {
                        if (childUserId.equals(userInfo.getUserId())) {
                            childComment.setUserInfo(userInfo);
                        }
                    });
                    Long rootId = childComment.getRootId();
                    if (rootId.equals(id)) {
                        childComments.add(childComment);
                    }
                });
                parentComment.setChildList(childComments);
            });
        }
        return new PageResult<VideoComment>(total, parentCommentList);
    }

    public Map<String, Object> getVideoDetails(Long videoId) {
        Video video = videoDao.getVideoById(videoId);
        Long userId = video.getUserId();
        User user = userService.getUserById(userId);
        UserInfo userInfo = user.getUserInfo();
        Map<String, Object> result = new HashMap<>();
        result.put("video", video);
        result.put("userInfo", userInfo);
        return result;
    }
}
