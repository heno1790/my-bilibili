package com.tt.domain.auth;

import java.util.Date;

/**
 * ClassName: AuthMenu
 * Description:
 *页面元素表
 * @Author 林伟朝
 * @Create 2024/10/14 22:09
 */
public class AuthMenu {
    private Long id;
    private String name;
    private String code;
    private Date createTime;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
