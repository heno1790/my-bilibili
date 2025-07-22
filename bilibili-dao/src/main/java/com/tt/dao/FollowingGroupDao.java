package com.tt.dao;

import com.tt.domain.FollowingGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: FollowingGroupDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/3/19 21:53
 */
@Mapper
public interface FollowingGroupDao {
    FollowingGroup getByType(@Param("userId") Long userId, @Param("type") String type);

    FollowingGroup getById(Long id);

    List<FollowingGroup> getByUserId(Long userId);

    Integer addUserFollowingGroup(FollowingGroup followingGroup);

    List<FollowingGroup> getUserFollowingGroups(Long userId);
}
