package com.tt.domain.auth;

import java.util.Date;

/**
 * ClassName: UserRole
 * Description:
 *用户角色关联表
 * @Author 林伟朝
 * @Create 2024/10/14 21:59
 */
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
    private Date createTime;
    private String roleName;  //联表查出来的t_auth_role的name字段（等级0）
    private String roleCode;  //联表查出来的t_auth_role的code字段（Lv0）

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
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

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
