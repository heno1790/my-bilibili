package com.tt.dao;

import com.tt.domain.UserCoin;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName: UserCoinDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/6/13 17:09
 */
@Mapper
public interface UserCoinDao {
    Integer getUserCoinsAmount(Long userId);

    void updateUserCoin(UserCoin userCoin);
}
