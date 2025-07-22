package com.tt.api;

import com.alibaba.fastjson.JSONObject;
import com.tt.api.support.UserSupport;
import com.tt.domain.JsonResponse;
import com.tt.domain.PageResult;
import com.tt.domain.User;
import com.tt.domain.UserInfo;
import com.tt.service.UserFollowingService;
import com.tt.service.UserService;
import com.tt.service.util.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * ClassName: UserApi
 * Package: com.tt.com.tt.api
 * Description:
 *
 * @Create 2025/3/17 21:27
 */
@RestController
public class UserApi {
    @Autowired
    private UserService userService;

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserFollowingService userFollowingService;

    /*本接口路径是rsa-publickeys的缩写,前端若接收到一些用户的敏感信息，则会向后端服务器调用此接口请求一个RSA算法公钥,先对信息进行加密
    然后再传输给后端,后端通过后端服务器的私钥可以解密,这保证了信息传输过程中的安全不泄露*/
    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPublicKey() {
        String pk = RSAUtil.getPublicKeyStr();  //从后端服务器上的加密工具类调用公钥获取接口
        return new JsonResponse<>(pk);
    }

    //post请求注册用户的接口,post非幂等操作,多次操作会产生不同的影响,接口路径是资源存放的路径而不是资源本身
    @PostMapping("/user")
    public JsonResponse<String> addUser(@RequestBody User user) {//此注解将前端表单传来的json数据作为响应体封装为对象传给User
        userService.addUser(user);  //addUser方法会将可能的异常都处理了，执行完默认是成功添加
        return JsonResponse.success();
    }

    //用户登录接口，登录成功后返回JWT令牌（token）
    //login具体逻辑：根据手机号查数据库的user；判断输入的密码和数据库的密码是否匹配；返回JWT令牌（token）
    @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception {
        String token = userService.login(user);
        return JsonResponse.success(token);
    }

    //前端带着token请求头到后台来请求获取用户信息时，调用此接口
    //这个方法实际上根据前端的token查询到对应的userid，再根据id查user和userinfo，把userinfo更新到user中
    //为什么不直接获取userid？ 经过token获得更安全，token不易被篡改
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo() {
        Long userId = userSupport.getCurrentUserId();
        //走到这里不报异常，说明请求头token解析成功,用户存在且合法，令牌也没过期
        User user = userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }

    //更新user
    @PutMapping("/users")
    public JsonResponse<String> updateUsers(@RequestBody User user) throws Exception {
        //校验登录,不抛异常说明处于登录状态,且数据库中存在该用户
        Long userId = userSupport.getCurrentUserId();
        userService.updateUsers(user, userId);
        return JsonResponse.success();
    }

    //更新userinfo
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo) {
        /*关于为何要用通过解密请求头中的令牌来获取用户id,出于以下几点考虑
        一:对用户信息进行相关操作,必须是登录了的用户才能进行,令牌有登录鉴权作用,并且还有有效期
        能防止没登陆权限的人任意修改用户信息。
        二:用户id直接放在请求体中不安全容易被截取,请求头中的token比较难截取,就算被截取也很难解密得到令牌中的用户信息
        虽然黑客仍能使用截取到的token进行登录鉴权,但令牌有有效期,而且想直接解密令牌获得里面的用户信息很难。
        总之就是安全,一般不会把id直接从前端传到后端,都是从令牌中获取的用户id*/
        //令牌解析失败,相当于没登陆,直接抛出异常，接口内的操作根本无法执行
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success("用户信息更新成功！");
    }

    //用户分页查询接口,分页查询一些up以供关注，且支持根据昵称nick的模糊查询
    //传参告知要查看第几页，每页多大，是否模糊查询,是哪个用户在查,然后在服务层编写查询逻辑或直接调用专业api完成功能即可
    @GetMapping("/user-infos")  //有RequestParam注解的输入参数必须传参
    public JsonResponse<PageResult<UserInfo>> pageListUserInfos(@RequestParam Integer no, @RequestParam Integer size, String nick) {
        Long userId = userSupport.getCurrentUserId();
        //JsonObject是fastJson包内的一个类,和Map差不多用法,但比Map更智能和方便
        JSONObject params = new JSONObject();
        params.put("no", no);
        params.put("size", size);
        params.put("nick", nick);
        params.put("userId", userId);
        PageResult<UserInfo> result = userService.pageListUserInfos(params);
        //新增功能,检查查询出来的用户有没有本用户关注过的用户,有的话做标记
        if (result.getTotal() > 0) {
            //检查当前页的List中用户信息,若关注过,则修改UserInfo表中的followed字段
            List<UserInfo> checkedUserInfoList = userFollowingService.checkFollowingStatus(result.getList(), userId);
            result.setList(checkedUserInfoList);
        }
        return new JsonResponse<>(result);
    }

    //双token登录接口，登录成功后返回两个token,一个是accessToken,一个是refreshToken
    @PostMapping("/double-tokens")
    public JsonResponse<Map<String, Object>> loginForDts(@RequestBody User user) throws Exception {
        //返回两个token给前端,用map封装
        Map<String, Object> map = userService.loginForDts(user);
        return new JsonResponse<>(map);
    }

    //退出登录时,需删除数据库中对应的的refreshToken
    @DeleteMapping("/refresh-tokens")
    public JsonResponse<String> logout(HttpServletRequest request) {
        //在登录状态下,才有删除令牌之说,都不处于登录状态,何来登出之说,所以获取id必须写在开头
        Long userId = userSupport.getCurrentUserId();
        userService.logout(userId);
        return JsonResponse.success();
    }

    //accessToken过期时,前端收到令牌过期状态码后自动发送请求到该接口,
    // 去数据库查询refreshToken状态,向服务器请求发放一个新accessToken
    @PostMapping("/access-tokens")
    public JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception {
        String refreshToken = request.getHeader("refreshToken");
        //查询refreshToken状态,没过期则发布新的accessToken给前端
        String accessToken = userService.refreshAccessToken(refreshToken);
        return JsonResponse.success(accessToken);
    }
}
