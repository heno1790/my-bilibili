package com.tt.dao;

import com.tt.domain.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName: UserRoleDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/4/9 11:37
 */
@Mapper
public interface UserRoleDao {
    List<UserRole> getUserRoleByUserId(Long userId);

    void addUserRole(UserRole userRole);
}
