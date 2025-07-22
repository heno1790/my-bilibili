package com.tt.dao;

import com.alibaba.fastjson.JSONObject;
import com.tt.domain.FollowingGroup;
import com.tt.domain.RefreshTokenDetail;
import com.tt.domain.User;
import com.tt.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: UserDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/3/17 21:30
 */
@Mapper
public interface UserDao {
    User getUserByPhone(String phone);

    Integer addUser(User user);

    void addUserInfo(UserInfo userInfo);

    User getUserById(Long id);

    UserInfo getUserInfoById(Long userId);

    String getSaltByUserId(Long userId);

    Integer updateUsers(User user); //写操作（update/insert/delete）：默认返回受影响行数（int/Integer），无需指定 resultType。

    Integer updateUserInfos(UserInfo userInfo);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);

    Integer pageCountUserInfos(Map<String, Object> params);

    List<UserInfo> pageListUserInfos(JSONObject params);

    void addFollowingGroup(FollowingGroup followingGroup);

    void deleteRefreshToken(Long userId);

    void addRefreshToken(@Param("refreshToken") String refreshToken, @Param("userId") Long userId, @Param("createTime") Date createTime);

    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);

    void deleteRefreshTokenByTokenId(Long id);
}
