package com.tt.dao;

import com.tt.domain.auth.AuthRoleElementOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * ClassName: AuthRoleElementOperationDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/4/9 16:01
 */
@Mapper
public interface AuthRoleElementOperationDao {
    List<AuthRoleElementOperation> getRoleElementOperationByRoleIds(@Param("roleIdSet") Set<Long> roleIdSet);
}
