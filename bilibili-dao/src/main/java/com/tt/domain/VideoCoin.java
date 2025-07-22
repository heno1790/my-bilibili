package com.tt.domain;

import java.util.Date;

/**
 * ClassName: VideoCoin
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/6/13 17:06
 */
public class VideoCoin {
    private Long id;
    private Long userId;//视频作者id
    private Long videoId;//视频id
    private Integer amount;//本用户对当前这个视频已经投过的硬币数量，也可以代表想要投币的数量
    private Date createTime;
    private Date updateTime;

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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
