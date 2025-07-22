package com.tt.dao;

import com.tt.domain.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: VideoDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/6/12 11:16
 */
@Mapper
public interface VideoDao {
    //发布一个视频时,把这个视频的相关信息也给插入到数据库里
    Integer addVideos(Video video);

    //给视频添加标签
    Integer batchAddVideoTags(List<VideoTag> videoTagList);

    Integer pageCountVideos(Map<String, Object> params);

    List<Video> pageListVideos(Map<String, Object> params);

    Video getVideoById(Long videoId);

    VideoLike getVideoLikeByVideoIdAndUserId(Long videoId, Long userId);

    void addVideoLike(Long videoId, Long userId, Date createTime);

    void deleteVideoLike(VideoLike videoLike);

    Long getVideoLikes(Long videoId);

    VideoCoin getVideoCoinByUserIdAndVideoId(Long userId, Long videoId);

    void updateVideoCoins(VideoCoin videoCoin);

    void postVideoCoins(VideoCoin videoCoin);

    Long getVideoCoinsAmount(Long videoId);

    void addVideoComment(VideoComment videoComment);

    Integer countVideoComments(Long videoId);

    List<VideoComment> pageListVideoComments(Map<String, Object> params);

    List<VideoComment> getAllChildCommentByCommentIds(Set<Long> parentCommentIdsSet);
}
