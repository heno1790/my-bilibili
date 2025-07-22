package com.tt.dao;

import com.tt.domain.auth.AuthRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName: AuthRoleDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/4/11 18:48
 */
@Mapper
public interface AuthRoleDao {
    AuthRole getRoleByCode(String code);
}
