package com.tt.service;

import com.tt.domain.auth.*;
import com.tt.domain.constant.AuthRoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName: UserAuthService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/4/9 10:30
 */
@Service
public class UserAuthService {
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private AuthRoleService authRoleService;
    @Autowired
    private AuthRoleElementOperationService authRoleElementOperationService;

    public UserAuthorities getUserAuthorities(Long userId) {
        //根据用户id获得用户角色关联表（相当于获取了用户的角色id，也即获取了用户的角色）
        //都是通过查关联的中间表的形式来查到相应的权限或角色的
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //从关联表中获取所有的角色id，即所有的角色
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());

        //查与角色们关联的另外两张关联表
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationByRoleIds(roleIdSet);
        List<AuthRoleMenu> authRoleMenuList = authRoleService.getRoleMenusByroleIds(roleIdSet);

        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        return userAuthorities;
    }

    //用于新建用户时添加默认的角色信息
    public void addUserDefaultRole(long userid) {
        UserRole userRole = new UserRole();
        AuthRole authRole = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_CODE_LV0);
        userRole.setUserId(userid);
        userRole.setRoleId(authRole.getId());
        userRoleService.addUserRole(userRole);
    }
}
