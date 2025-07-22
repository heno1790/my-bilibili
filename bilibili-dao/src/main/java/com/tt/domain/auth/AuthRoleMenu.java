package com.tt.domain.auth;

import java.util.Date;

/**
 * ClassName: AuthRoleMenu
 * Description:
 *角色与页面元素关联表
 * @Author 林伟朝
 * @Create 2024/10/14 22:10
 */
public class AuthRoleMenu {
    private Long id;
    private Long roleId;
    private Long menuId;
    private Date createTime;
    //冗余字段，用于联表查询,减少对数据库的查询次数,提高查询效率,后续用到时自行体会
    private AuthMenu authMenu;

    public AuthMenu getAuthMenu() {
        return authMenu;
    }

    public void setAuthMenu(AuthMenu authMenu) {
        this.authMenu = authMenu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
