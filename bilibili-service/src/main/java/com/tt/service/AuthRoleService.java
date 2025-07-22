package com.tt.service;

import com.tt.dao.AuthRoleDao;
import com.tt.domain.auth.AuthRole;
import com.tt.domain.auth.AuthRoleElementOperation;
import com.tt.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * ClassName: AuthRoleService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/4/9 10:32
 */
@Service
public class AuthRoleService {
    @Autowired
    private AuthRoleElementOperationService authRoleElementOperationService;
    @Autowired
    private AuthRoleMenuService authRoleMenuService;
    @Autowired
    private AuthRoleDao authRoleDao;

    public List<AuthRoleElementOperation> getRoleElementOperationByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationByRoleIds(roleIdSet);
    }

    public List<AuthRoleMenu> getRoleMenusByroleIds(Set<Long> roleIdSet) {
        return authRoleMenuService.getRoleMenusByRoleIds(roleIdSet);
    }

    public AuthRole getRoleByCode(String code) {
        return authRoleDao.getRoleByCode(code);
    }
}
