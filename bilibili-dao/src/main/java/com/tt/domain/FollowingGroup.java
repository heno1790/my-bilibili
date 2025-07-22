package com.tt.domain;

import java.util.Date;
import java.util.List;

/**
 * ClassName: FollwingGroup
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/3/19 21:52
 */
public class FollowingGroup {  // 对应不同用户的不同关注分组
    private Long id;  //主键
    private Long userId;
    private String name;  // 关注分组的名称
    private String type;  // 关注分组的类型：可以取0,1,2,3，0对应name特别关注，1悄悄关注，2默认，3自定义
    private Date createTime;
    private Date updateTime;
    private List<UserInfo> followingUserInfoList;  //关注者的详细信息的列表


    public List<UserInfo> getFollowingUserInfoList() {
        return followingUserInfoList;
    }

    public void setFollowingUserInfoList(List<UserInfo> followingUserInfoList) {
        this.followingUserInfoList = followingUserInfoList;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


}
