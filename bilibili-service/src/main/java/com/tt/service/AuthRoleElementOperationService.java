package com.tt.service;

import com.tt.dao.AuthRoleElementOperationDao;
import com.tt.domain.auth.AuthRoleElementOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * ClassName: AuthRoleElementOperationService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/4/9 13:17
 */
@Service
public class AuthRoleElementOperationService {
    @Autowired
    private AuthRoleElementOperationDao authRoleElementOperationDao;

    public List<AuthRoleElementOperation> getRoleElementOperationByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationDao.getRoleElementOperationByRoleIds(roleIdSet);
    }
}
