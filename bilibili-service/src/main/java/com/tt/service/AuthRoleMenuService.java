package com.tt.service;

import com.tt.dao.AuthRoleMenuDao;
import com.tt.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * ClassName: AuthRoleMenuService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/4/9 15:52
 */
@Service
public class AuthRoleMenuService {
    @Autowired
    private AuthRoleMenuDao authRoleMenuDao;

    public List<AuthRoleMenu> getRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuDao.getRoleMenusByRoleIds(roleIdSet);
    }
}
