package com.tt.api;

import com.tt.api.support.UserSupport;
import com.tt.domain.FollowingGroup;
import com.tt.domain.JsonResponse;
import com.tt.domain.UserFollowing;
import com.tt.service.UserFollowingService;
import com.tt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: UserFollowingApi
 * Package: com.tt.api
 * Description:
 *
 * @Create 2025/3/21 22:09
 */
@RestController
public class UserFollowingApi {
    @Autowired
    private UserFollowingService userFollowingService;

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserService userService;

    //增加关注
    @PostMapping("/user-followings")
    public JsonResponse<String> addUserFollowing(@RequestBody UserFollowing userFollowing) {
        Long UserId = userSupport.getCurrentUserId();
        //从辅助bean获取当前用户id
        userFollowing.setUserId(UserId);
        userFollowingService.addUserFollowing(userFollowing);
        return JsonResponse.success();
    }

    //查询用户关注列表，以分组形式返回
    @GetMapping("/user-followings")
    public JsonResponse<List<FollowingGroup>> getUserFollowings(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> result = userFollowingService.getUserFollowings(userId);
        return new JsonResponse<>(result);
    }

    //查询粉丝列表,以userMapping形式返回
    @GetMapping("/user-fans")
    public JsonResponse<List<UserFollowing>> getUserFans() {
        Long userId = userSupport.getCurrentUserId();
        List<UserFollowing> result = userFollowingService.getUserFans(userId);
        return new JsonResponse<>(result);
    }

    //新建用户关注分组,并把新建的分组的id返回给前端
    @PostMapping("/user-following-groups")
    public JsonResponse<Long>addUserFollowingGroups(@RequestBody FollowingGroup followingGroup) {
        Long userId = userSupport.getCurrentUserId();
        followingGroup.setUserId(userId);
        Long groupId=userFollowingService.addUserFollowingGroups(followingGroup);
        return new JsonResponse<>(groupId);
    }

    //查询用户关注分组,只返回分组本身，而不用返回分组内部的up的详细详细信息
    @GetMapping("/user-following-groups")
    public JsonResponse<List<FollowingGroup>> getUserFollowingGroups() {
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> list = userFollowingService.getUserFollowingGroups(userId);
        return new JsonResponse<>(list);
    }

}
