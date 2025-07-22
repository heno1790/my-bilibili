package com.tt.service;

import com.tt.dao.UserRoleDao;
import com.tt.domain.auth.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * ClassName: UserRoleService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/4/9 10:32
 */
@Service
public class UserRoleService {
    @Autowired
    private UserRoleDao userRoleDao;

    public List<UserRole> getUserRoleByUserId(Long userId) {
        return userRoleDao.getUserRoleByUserId(userId);
    }

    public void addUserRole(UserRole userRole) {
        userRole.setCreateTime(new Date());
        userRoleDao.addUserRole(userRole);
    }
}
