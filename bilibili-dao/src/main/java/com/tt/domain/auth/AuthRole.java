package com.tt.domain.auth;

import java.util.Date;

/**
 * ClassName: AuthRole
 * Description:
 *角色表
 * @Author 林伟朝
 * @Create 2024/10/14 22:00
 */
public class AuthRole {
    private Long id;
    private String name;
    private String code;//角色唯一编码
    private Date createTime;

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Date updateTime;
}
