package com.tt.dao;

import com.tt.domain.UserMoment;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName: UserMomentsDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/3/24 11:54
 */
@Mapper
public interface UserMomentsDao {

    void addUserMoments(UserMoment userMoment);
}
