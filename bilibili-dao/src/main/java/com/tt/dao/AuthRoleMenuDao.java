package com.tt.dao;

import com.tt.domain.auth.AuthRoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

/**
 * ClassName: AuthRoleMenuDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/4/9 17:06
 */
@Mapper
public interface AuthRoleMenuDao {
    List<AuthRoleMenu> getRoleMenusByRoleIds(Set<Long> roleIdSet);
}
