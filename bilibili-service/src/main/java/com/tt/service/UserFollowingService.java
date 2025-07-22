package com.tt.service;

import com.tt.dao.UserFollowingDao;
import com.tt.domain.FollowingGroup;
import com.tt.domain.User;
import com.tt.domain.UserFollowing;
import com.tt.domain.UserInfo;
import com.tt.domain.constant.UserConstant;
import com.tt.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName: UserFollowingService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/3/19 21:55
 */
@Service
public class UserFollowingService {
    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;  //要用到另一个Service,再在service中调用其dao

    @Autowired
    private UserService userService;


    //用户点下关注按钮,前端通过requestBody传参,添加用户关注信息到数据库中的用户关注表
    @Transactional  //事务处理，确保删除和新增用户关注表的操作同时成功,不成功则回滚
    public void addUserFollowing(UserFollowing userFollowing) {
        Long userId = userFollowing.getUserId();
        Long groupId = userFollowing.getGroupId();
        //检查表单信息中是否包含分组Id信息(即用户有无将改up指定到特定的关注分组中去)
        if (groupId == null) {
            //如果关注分组id为空,即表单中没有传这个参数上来，就把这个up分到默认分组去
            //用户关注分组表内默认是有三个分组信息的,下面这行代码仅仅是为了获得默认分组的主键Id而已
            //这里注意得先在数据库中创建这个type=2的followingGroup，否则会报空指针异常
            FollowingGroup followingGroup = followingGroupService.getByType(userId, UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            //实际上getByType会查出多个不同用户的同一个type的表数据，这里应该再增加一个userId的约束
            //分组表的主键id即为用户关注表的GroupId，通过这个键来关联两张表
            userFollowing.setGroupId(followingGroup.getId());
        } else {
            //判断用户指定的关注分组id是否存在
            FollowingGroup follwingGroup = followingGroupService.getById(groupId);
            if (follwingGroup == null) {
                throw new ConditionException("用户指定的关注分组不存在!");
            }
        }
        //为up指定关注分组的操作已经完成，做别的逻辑判断
        User usr = userService.getUserById(userFollowing.getUserId());
        if (usr == null) {
            throw new ConditionException("关注的用户不存在!");
        }
        //分组指定完成,关注的用户也存在,开始操作数据库写表
        //这个up主可能以前关注过,得先删除以前的表记录,以防数据库中有互相冲突或重复的表记录
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), userFollowing.getFollowingId());
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
    }

    //获取用户的关注列表,将关注的up按关注分组进行分类
    //第一步:获取关注的up的UserFollowing记录
    //第二步:根据UserFollowing记录获取up主的id，进而查询up主们的详细信息
    //第三步:将up主们的详细信息列表写进对应的分组对象FollowingGroup的属性里
    //1. 根据当前用户的userId查出 UserFollowing，userFollowingList
    //2. 遍历userFollowingList得到关注的用户的userID，即followingIdSet
    //3. 根据followingIdSet查出每个关注用户的userInfo，即userInfoList
    //4. 对比 userFollowingList 的 userID 和 userInfoList 的 userID，如果相等，更新UserFollowing中的userInfo参数
    //5. 展示关注的列表：分为所有关注一并展示和按分组展示
    public List<FollowingGroup> getUserFollowings(Long userId) {
        //获取用户总的关注列表
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowingByUserId(userId);
        //从关注列表中提取所关注的up主的id的集合
        Set<Long> followingIdSet = userFollowingList.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        //根据up主的id集合获取up主们的详细信息集合
        List<UserInfo> userInfoList = new ArrayList<>();
        if (followingIdSet.size() > 0) {
            //一个一个去数据库查涉及多次io操作，这是大忌，所以这里专门重新写一个api一次性全部查出来
            //根据关注up主的集合查询到其对应的userinfo表的集合
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }
        //为关注记录userFollowing添加冗余字段,UserInfo,也即该条记录所对应的up主的详细信息
        for (UserFollowing userFollowing : userFollowingList) {
            for (UserInfo userInfo : userInfoList) {
                // 比较关注列表的up主的id和userinfo里的id，对应上的就把userinfo的信息更新到userFollowing中
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        //根据用户id，把用户的所有关注分组查出来(包括默认有的三个分组和自建的分组),也即查询本用户所拥有的分组(分组数量>=3)
        List<FollowingGroup> followingGroupList = followingGroupService.getByUserId(userId);

        // 先把所有关注用户的userInfoList给加入到allGroup这个分组中
        //allGroup这个分组并不存在数据库中，故没有id、type属性
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setUserId(userId);
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME); //设置名称为“全部关注”
        allGroup.setFollowingUserInfoList(userInfoList);
        //存放所有分组的列表
        List<FollowingGroup> result = new ArrayList<>();
        result.add(allGroup);

        // 再遍历不同的FollowingGroup分组，把对应的分组的infoList加入到对应分组中
        for (FollowingGroup group : followingGroupList) {
            List<UserInfo> infoList = new ArrayList<>();
            for (UserFollowing userFollowing : userFollowingList) {
                if (group.getId().equals(userFollowing.getGroupId())) {
                    infoList.add(userFollowing.getUserInfo());
                }
            }
            group.setFollowingUserInfoList(infoList);
            result.add(group);
        }
        return result;
    }

    //查询当前用户的粉丝列表,互粉的状态要特殊标识
    //第一步：查询UserFollowing表，确定粉丝列表fanList，再由此得到粉丝id列表fanIdList
    //第二步：根据粉丝id列表fanIdList查到userInfo，得到粉丝信息列表fanInfoList
    //第三步：查询本用户的关注，得到UserFollowing列表myUserFollowingList
    //      逐个对比myUserFollowingList的关注的用户的id和fanInfoList的粉丝的id，如果相等代表互粉
    public List<UserFollowing> getUserFans(Long userId) {
        //去数据库查UserFollowing表，本用户的粉丝
        List<UserFollowing> fanUserFollowingList = userFollowingDao.getUserFans(userId);
        //从粉本用户的数据记录中提纯出粉丝们的详细信息,先提取粉丝的Id集合
        Set<Long> fanIdList = fanUserFollowingList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());
        //根据粉丝Id查询粉丝详细信息
        List<UserInfo> fanInfoList = new ArrayList<>();
        if (!fanIdList.isEmpty()) {
            //一次性全查出来，减少数据库的多次io操作
            fanInfoList = userService.getUserInfoByUserIds(fanIdList);
        }
        //根据fanInfoList粉丝信息列表和userId,添加粉丝们的冗余信息字段followed,若为互粉则为true
        List<UserFollowing> myUserFollowingList = userFollowingDao.getUserFollowingByUserId(userId);
        for (UserInfo fanInfo : fanInfoList) {
            for (UserFollowing fanUserFollowing : fanUserFollowingList) {
                if (fanUserFollowing.getUserId().equals(fanInfo.getUserId())) {
                    fanInfo.setFollowed(false); //默认状态为非互粉
                    fanUserFollowing.setUserInfo(fanInfo);
                }
            }
            //判断本用户有没有关注这个粉丝
            for (UserFollowing myUserFollowing : myUserFollowingList) {
                if (fanInfo.getId() == myUserFollowing.getUserId())
                    fanInfo.setFollowed(true); //若为互粉的粉丝,则标识其状态
            }
        }
        return fanUserFollowingList; //返回粉丝列表，其冗余字段包含这些粉丝的详细信息
    }

    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        //用户自建的关注分组新类型,用户自定义分组
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addUserFollowingGroups(followingGroup);
        return followingGroup.getId();
    }


    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    //把userInfoList中被userId对应用户关注的userInfo的followed字段标记,返回标记后的userInfoList
    public List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId) {
        //查出本用户的关注列表
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowingByUserId(userId);
        //将其与当前查出的列表进行比对
        for (UserInfo userInfo : userInfoList) {
            userInfo.setFollowed(false);
            for (UserFollowing userFollowing : userFollowingList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userInfo.setFollowed(true);
                }
            }
        }
        return userInfoList;
    }
}
