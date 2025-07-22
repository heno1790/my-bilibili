package com.tt.service;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.tt.dao.UserDao;
import com.tt.domain.*;
import com.tt.domain.constant.UserConstant;
import com.tt.domain.exception.ConditionException;
import com.tt.service.util.MD5Util;
import com.tt.service.util.RSAUtil;
import com.tt.service.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ClassName: UserService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/3/17 21:18
 */
@Service
public class UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserAuthService userAuthService;

    public void addUser(User user) {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            //自定义抛出异常的提示信息,主动将异常抛到后端的顶层全局异常处理器,处理器封装异常到JsonResponse后将其返回给前端
            throw new ConditionException("手机号不能为空");
        }
        if (this.getUserByPhone(phone) != null) {
            throw new ConditionException("这个手机号码已经被注册过了");
        }
        //验证通过,开始注册业务
        /*配合时间戳生成随机盐值,盐值将用来对解密后的rawpassword进行MD5算法单向加密
        加密后才可将密码数据保存进数据库,这样即使黑客黑了数据库,获得的也只是用户加密后的密码，且即使知道盐值
        也无法反向解密出正确密码,将来验证密码正确否只需要再加密一次然后和数据库比对即可
        */
        Date now = new Date();
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword();  //这里的password是前端经过RSA加密的password
        String rawPassword;
        /*前端传来的用户信息中的密码项是经过RSA算法的公钥加密的,公钥就是后端服务器的加密工具类发出的,这个工具类自然也有私钥对加密后的密码进行解密
        前端传RSA加密的密码密文，后端解密后从数据库读取id用户对应的盐值,然后用盐为密码明文加密的md5密文,最后与数据库密文匹配，匹配成功则密码正确成功登录*/
        try {
            rawPassword = RSAUtil.decrypt(password);  // 这个方法会自动抛出异常
        } catch (Exception e) {
            throw new ConditionException("密码解密失败,原因是RSA公钥私钥不匹配或者是加密数据传输过程中信息丢失或遭到篡改");
        }
        //走到这里说明注册用户所需的表单信息成功传到后端了,补充用户数据,给密码加密然后存入数据库
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        user.setSalt(salt);
        user.setPassword(md5Password);  // 这里存入数据库的密码：原始密码-->前端RSA加密-->RSA解密-->加盐值MD5加密
        user.setCreateTime(now);
        userDao.addUser(user);

        //补充用户信息表信息,将用户信息表也插入数据库
        UserInfo userInfo = new UserInfo();
        //关联两张表的属性,直接用主键id来将其关联
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setGender(UserConstant.GENDER_UNKNOW);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);

        //补充用户的默认关注分组，对应数据库中的following_group表
        FollowingGroup followingGroup = new FollowingGroup();
        followingGroup.setUserId(user.getId());
        followingGroup.setName(UserConstant.USER_FOLLOWING_GROUP_NAME_DEFAULT);
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
        followingGroup.setCreateTime(now);
        userDao.addFollowingGroup(followingGroup);

        //添加默认的用户角色
        userAuthService.addUserDefaultRole(user.getId());
    }

    public User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception {
        //要优化的话,判断手机号和邮箱号哪个不为空,只有一个会不为空,然后走不同的分支
        //回字的两种写法而已,先不补充了
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        //判断要登录的用户是否存在
        if (dbUser == null) {
            throw new ConditionException("用户不存在");
        }

        //判断密码是否正确
        String password = user.getPassword();
        String rawPassword;
        //用工具类中的RSA算法私钥解析前端发来的密码密文
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("RSA私钥解析失败,密码传输过程中遭到篡改或者数据丢失,或者是后台服务器的公钥出错了,具体原因自行排查");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("登陆失败,密码错误");
        }
        //返回JWT登录鉴权令牌,令牌有效时间为在工具中自行设置
        return TokenUtil.generateToken(dbUser.getId());
    }

    public User getUserInfo(Long userId) {
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoById(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    public void updateUsers(User user, Long userId) {
        String password = user.getPassword();
        user.setId(userId);
        String rawPassword;
        //用服务器RSA公钥解密前端传来的加密密码,有传密码才需要这些操作
        if (password != null) {
            try {
                rawPassword = RSAUtil.decrypt(password);
            } catch (Exception e) {
                throw new ConditionException("密码解密失败,原因是RSA公钥私钥不匹配或者是加密数据传输过程中信息丢失或遭到篡改");
            }
            //走到这里说明注册用户所需的表单信息成功传到后端了,补充用户数据,给密码加密然后存入数据库
            String salt = userDao.getSaltByUserId(userId);
            String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
            //设置写进数据库的密码,md5算法只能单向加密,且无法反向解密,增强密码的安全性
            user.setPassword(md5Password);
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        //elasticSearchService.addUserInfo(userInfo);
        userDao.updateUserInfos(userInfo);
    }

    public User getUserById(Long userId) {
        return userDao.getUserById(userId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.getUserInfoByUserIds(userIdList);
    }

    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        Integer no = params.getInteger("no");
        Integer size = params.getInteger("size");
        params.put("start", (no - 1) * size);  //当前页的第一条数据的索引
        params.put("limit", size);  //当前页最多能有多少条数据
        Integer total = userDao.pageCountUserInfos(params);  //计算数据库中符合查询要求的数据条数
        List<UserInfo> list = new ArrayList<UserInfo>();
        //有数据才用查,当前页的list可能为空,但只要进了if,数据条数total就不为0了,
        // 前端会设置好，如果只有5页,就无法跳转到第10页,所以total>0,则list一定不为空,这点无需操心
        if (total > 0) {
            list = userDao.pageListUserInfos(params);
        }
        return new PageResult<>(total, list);
    }

    public Map<String, Object> loginForDts(User user) throws Exception {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        //判断要登录的用户是否存在
        if (dbUser == null) {
            throw new ConditionException("用户不存在");
        }
        String password = user.getPassword();
        String rawPassword;
        //用工具类中的RSA算法私钥解析前端发来的密码密文
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("RSA私钥解析失败,密码传输过程中遭到篡改或者数据丢失,或者是后台服务器的公钥出错了,具体原因自行排查");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("登陆失败,密码错误");
        }
        Long userId = dbUser.getId();
        String accessToken = TokenUtil.generateToken(userId);
        String refreshToken = TokenUtil.generateRefreshToken(userId);
        //将refreshToken存储到数据库,方便后续查询刷新等操作
        //先删除后添加,防止数据库中存在两份一模一样的refreshToken
        userDao.deleteRefreshToken(userId);
        userDao.addRefreshToken(refreshToken, userId, new Date());

        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        return map;
    }

    public void logout(Long userId) {
        userDao.deleteRefreshToken(userId);
    }

    public String refreshAccessToken(String refreshToken) throws Exception {
        //查询refreshToken详情
        RefreshTokenDetail refreshTokenDetail = userDao.getRefreshTokenDetail(refreshToken);
        if (refreshTokenDetail == null) { //用户登出了,refreshToken被删除了
            throw new ConditionException("accessToken已过期且refresh令牌不存在!");
        }
        //数据库中refreshToken令牌存在,解析看是否过期和合法,过期了则需要从数据库内删除之
        Long userId = TokenUtil.verifyrefreshToken(refreshToken);
        if (userId == null || userId <= 0) {
            userDao.deleteRefreshTokenByTokenId(refreshTokenDetail.getId());
            throw new ConditionException("refreshToken令牌已过期并从数据库中删除,请重新登录!");
        }
        String accessToken = TokenUtil.generateToken(userId);
        return accessToken;
    }
}
