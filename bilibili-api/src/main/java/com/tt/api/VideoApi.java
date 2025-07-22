package com.tt.api;

import com.tt.api.support.UserSupport;
import com.tt.domain.*;
import com.tt.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * ClassName: VideoApi
 * Package: com.tt.api
 * Description:
 *
 * @Create 2025/6/12 10:54
 */
@RestController
public class VideoApi {
    @Autowired
    private VideoService videoService;
    @Autowired
    private UserSupport userSupport;

    //视频投稿，实际上上传视频和投稿是先后连续进行的,这个接口调用时已经上传过了的
    @PostMapping("/videos")
    public JsonResponse<String> addVideos(@RequestBody Video video) {
        Long userId = userSupport.getCurrentUserId();
        video.setUserId(userId);
        videoService.addVideos(video);
        //elasticSearchService.addVideo(video);
        return JsonResponse.success();
    }

    //分页查询加载视频流,输入每页大小和第几页,以及要展示的分区,然后去数据库查
    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>> pageListVideos(Integer size, Integer no, String area) {
        PageResult<Video> result = videoService.pageListVideos(size, no, area);
        return new JsonResponse<>(result);
    }

    //在线浏览观看视频接口
    /*文件分片下载，后台直接返回存储服务器文件的完整http请求路径的话，不安全，会被白嫖，客户端只能知道相对路径，由后台来包装并帮助客户端
    发起文件下载请求并写入输出流返回给前端*/
    @GetMapping("video-slices")
    public void viewVideosOnlineBySlices(HttpServletRequest request, //请求
                                         HttpServletResponse response,  //响应
                                         String url) throws Exception {//客户端至多只能知道资源在服务器上的相对路径，完整的请求由后台发起
        videoService.viewVideoOnlineBySlices(request, response, url);
    }

    /*点赞视频*/
    @PostMapping("/video-likes")
    public JsonResponse<String> addVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoLike(videoId, userId);
        return JsonResponse.success("点赞成功，感谢支持！");
    }

    /*取消点赞*/
    @DeleteMapping("/video-likes")
    public JsonResponse<String> deleteVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId, userId);
        return JsonResponse.success("成功取消点赞");
    }

    /*查询点赞数量,游客模式下也可查询*/
    @GetMapping("/video-likes")
    public JsonResponse<Map<String, Object>> getVideoLikes(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception e) {
        }//若令牌解析异常,说明为游客模式，那就不解析令牌了,走空执行
        Map<String, Object> result = videoService.getVideoLikes(videoId, userId);
        return new JsonResponse<>(result);
    }

    /*视频投币*/
    @PostMapping("/video-coins")
    public JsonResponse<String> addVideoCoins(@RequestBody VideoCoin videoCoin) {
        //不catch异常了,因为只有登录了的用户才有资格投币
        Long userId = userSupport.getCurrentUserId();
        videoCoin.setUserId(userId);
        System.out.println("视频id是：" + videoCoin.getVideoId());
        videoService.addVideoCoins(videoCoin);
        return JsonResponse.success();
    }

    /*查询视频硬币数量,游客和登录用户区别对待*/
    @GetMapping("/video-coins")
    public JsonResponse<Map<String, Object>> getVideoCoins(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception e) {
        }
        Map<String, Object> result = videoService.getVideoCoins(videoId, userId);
        return new JsonResponse<>(result);
    }

    /*添加视频评论*/
    @PostMapping("/video-comments")
    public JsonResponse<String> addVideoComment(@RequestBody VideoComment videoComment) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoComment(videoComment, userId);
        return JsonResponse.success();
    }

    /*分页查询视频评论*/
    @GetMapping("/video-comments")
    public JsonResponse<PageResult<VideoComment>> pageListVideoComments(@RequestParam Integer no,
                                                                        @RequestParam Integer size,
                                                                        @RequestParam Long videoId) {
        PageResult<VideoComment> pageList = videoService.pageListVideoComments(no, size, videoId);
        return new JsonResponse<>(pageList);
    }

    /*用户点击视频进入视频界面后,需返回给前端视频详情,以便前端加载视频界面*/
    @GetMapping("/video-details")
    public JsonResponse<Map<String, Object>> getVideoDetails(@RequestParam Long videoId) {
        Map<String, Object> result = videoService.getVideoDetails(videoId);
        return new JsonResponse<>(result);
    }
}
