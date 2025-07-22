package com.tt.api;

import com.tt.api.support.UserSupport;
import com.tt.domain.JsonResponse;
import com.tt.domain.auth.UserAuthorities;
import com.tt.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: UserAuthApi
 * Package: com.tt.api
 * Description:
 *
 * @Create 2025/4/9 10:20
 */
@RestController
public class UserAuthApi {
    @Autowired
    private UserSupport userSupport;
    @Autowired
    private UserAuthService userAuthService;

    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthorities> getUserAuthorities() {
        Long userId = userSupport.getCurrentUserId();
        UserAuthorities result = userAuthService.getUserAuthorities(userId);
        return new JsonResponse<>(result);
    }
}
