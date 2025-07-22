package com.tt.service;

import com.tt.dao.UserCoinDao;
import com.tt.domain.UserCoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * ClassName: UserCoinService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/6/13 17:08
 */
@Service
public class UserCoinService {
    @Autowired
    private UserCoinDao userCoinDao;

    public Integer getUserCoinsAmount(Long userId) {
        return userCoinDao.getUserCoinsAmount(userId);
    }

    public void updateUserCoin(Long userId, long coinsAmount) {
        UserCoin userCoin = new UserCoin();
        userCoin.setUserId(userId);
        userCoin.setAmount(coinsAmount);
        userCoin.setUpdateTime(new Date());
        userCoinDao.updateUserCoin(userCoin);
    }
}
