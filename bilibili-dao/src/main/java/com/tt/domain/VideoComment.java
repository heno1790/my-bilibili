package com.tt.domain;

import java.util.Date;
import java.util.List;

/**
 * ClassName: VideoComment
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/6/15 15:46
 */
public class VideoComment {
    private Long id;
    private Long videoId;
    private Long userId;//创建本条评论的用户的id
    private String comment;
    private Long replyUserId;//本评论回复所回复的用户的id,只有二级评论该字段才不为空
    private Long rootId;//根结点评论id,当评论为一级评论时,该字段为空
    private Date createTime;
    private Date updateTime;
    /*存放二级评论的列表,若本评论本身就为二级评论或本一级评论暂无人回复,则此字段为空*/
    private List<VideoComment> childList;
    private UserInfo userInfo;
    private UserInfo replyUserInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(Long replyUserId) {
        this.replyUserId = replyUserId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
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

    public List<VideoComment> getChildList() {
        return childList;
    }

    public void setChildList(List<VideoComment> childList) {
        this.childList = childList;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getReplyUserInfo() {
        return replyUserInfo;
    }

    public void setReplyUserInfo(UserInfo replyUserInfo) {
        this.replyUserInfo = replyUserInfo;
    }
}
