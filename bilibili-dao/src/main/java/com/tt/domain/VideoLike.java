package com.tt.domain;

import java.util.Date;

/**
 * ClassName: VideoLike
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/6/13 11:43
 */
public class VideoLike {
    private Long id;
    private Long userId;
    private Long videoId;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
