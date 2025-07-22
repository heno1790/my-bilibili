package com.tt.api;

import com.tt.api.support.UserSupport;
import com.tt.domain.JsonResponse;
import com.tt.domain.UserMoment;
import com.tt.domain.annotation.ApiLimitedRole;
import com.tt.domain.annotation.DataLimited;
import com.tt.domain.constant.AuthRoleConstant;
import com.tt.service.UserMomentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: UserMomentsApi
 * Package: com.tt.api
 * Description:
 *
 * @Create 2025/3/24 11:50
 */
@RestController
public class UserMomentsApi {
    @Autowired
    private UserMomentsService userMomentsService;
    @Autowired
    private UserSupport userSupport;

    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_CODE_LV0})
    @DataLimited
    @PostMapping("/user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return new JsonResponse<>("用户动态发布成功");
    }

    //获取用户订阅的up主动动态信息
    @GetMapping("user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments() {
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> list = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(list);
    }

}
