package com.tt.service;

import com.tt.dao.FollowingGroupDao;
import com.tt.domain.FollowingGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: FollowingGroupService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/3/19 21:56
 */
@Service
public class FollowingGroupService {
    @Autowired
    private FollowingGroupDao followingGroupDao;

    //根据关注分组获得查询对应的关注分组的信息
    public FollowingGroup getByType(Long userId, String type){
        return followingGroupDao.getByType(userId, type);
    }

    //根据分组id查询对应的关注分组的信息
    public FollowingGroup getById(Long id){
        return followingGroupDao.getById(id);
    }

    public List<FollowingGroup> getByUserId(Long userId) {
        return followingGroupDao.getByUserId(userId);
    }

    public void addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroupDao.addUserFollowingGroup(followingGroup);
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupDao.getUserFollowingGroups(userId);
    }
}
