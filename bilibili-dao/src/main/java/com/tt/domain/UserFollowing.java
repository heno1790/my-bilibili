package com.tt.domain;

import java.util.Date;

/**
 * ClassName: UserFollowing
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/3/19 21:51
 */
public class UserFollowing {
    private Long id;
    private Long userId;
    private Long followingId;  // 被关注的用户的id
    private Long groupId;  // 可以取0,1,2,3，对应following_group的name字段
    private Date createTime;
    private UserInfo userInfo; // 冗余字段

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

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

    public Long getFollowingId() {
        return followingId;
    }

    public void setFollowingId(Long followingId) {
        this.followingId = followingId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
