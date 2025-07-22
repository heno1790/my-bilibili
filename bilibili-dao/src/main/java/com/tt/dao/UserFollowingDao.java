package com.tt.dao;

import com.tt.domain.UserFollowing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: UserFollowingDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/3/19 21:53
 */
@Mapper
public interface UserFollowingDao {
    // @Param指定参数就不用在xml文件中指定parameterType
    Integer deleteUserFollowing(@Param("userId") Long userId,@Param("followingId") Long followingId);

    void addUserFollowing(UserFollowing userFollowing);

    List<UserFollowing> getUserFollowingByUserId(Long userId);

    List<UserFollowing> getUserFans(Long followingId);
}
