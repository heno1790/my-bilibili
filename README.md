

<h2 id="Oh6EL">注册与登录</h2>
+ 相关接口：
    1. 新建用户
    2. 用户登录：验证用户名和密码来返回token，后面的行为都是用这个token来解析用户id
    3. 查询用户是否存在
    4. 更新用户信息

<h3 id="jVv32">加密</h3>
<h4 id="WvbC3">RSA和md5加密算法</h4>
+ RSA加密
    - 非对称加密，有公钥和私钥之分，公钥用于数据加密，私钥用于数据解密。加密结果可逆
    - 公钥一般提供给外部进行使用，私钥需要放置在服务器端保证安全性。
    - 特点：加密安全性很高，但是加密速度较慢
    - 项目中公钥和私钥都是`RSAUtil`类中的常量，加密在前端实现，后端只需要解密。
+ RSA用在密码的**传递**过程，MD5用在密码的**存储**过程
+ md5加密
    - 单向加密，无法解密，只是通过把原始密码加密后的密码和用输入的密码加密后的密码比对，一致则认为密码正确



**注册 @PostMapping("/user")**

1. 判断该手机号是否为空
2. 查数据库，看该手机号是否已注册
3. 在前端使用`公钥`对用户原始密码加密`password`，这个是前端完成的，在postman测试时直接传入
4. 后端用`私钥`对`password`解密直接得到`rawPassword`，这个才是用户输入的原始密码

```java
rawPassword = RSAUtil.decrypt(password);
```

5. 根据当前时间获取盐值，利用md5+盐值加密得到密码，将**新密码**和**盐值**存入数据库  
rawPassword + salt --> md5Password

```java
String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
```



**登录 @PostMapping("/user-tokens")**

1. 根据手机号查数据库的user；
2. 判断输入的密码和数据库的密码是否匹配：  
新来的密码同样经过公钥加密和私钥解密，和数据库的盐值计算加密后的密码，看和数据库中存的密码是否匹配
3. 返回JWT令牌（token）
+ 这样如果数据库泄露，只泄露了盐值和加密后的密码，无法反推出原始密码是多少。md5 加密后**只能正向**算出加密后的密码

<h4 id="i2iZp">JWT</h4>
项目中用到的jwt也是用<font style="color:#DF2A3F;">RSA</font>加密算法加密（RSA256，也可选用其它如ES256等），加密对象是userId。

token是在请求头（Header）中携带。

```java
import com.auth0.jwt.algorithms.Algorithm;
public static String generateToken(Long userId) throws Exception {
    //指定jwt令牌的加密算法，调用RSA
    Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.SECOND, 7200);  // 设置令牌过期时间为7200s
    //生成令牌
    return JWT.create().withKeyId(String.valueOf(userId))  //用户id
    .withIssuer(ISSUER)  //签发者
    .withExpiresAt(calendar.getTime())  //过期时间
    .sign(algorithm);  //加密
}
```

JWT 是一种无状态的身份验证机制，服务器不需要存储会话信息（如 Session），而是通过签名校验 Token 的合法性。

+ 组成结构：
    - Header（头部）：描述 Token 类型（typ: "JWT"）和签名算法（如 HS256, RSA256）
    - Payload（载荷）：存放实际数据（如签发者、用户ID、角色、过期时间等）
    - Signature（签名）：将 Header 和 Payload 用 Base64Url 编码后拼接，再通过指定算法（如 HS256）和密钥生成签名，确保 Token 未被篡改。
+ token的优势
    1. token 不储存在服务器，不会造成服务器压力
    2. token 可以存储在非 cookie 中，安全性高
    3. token 值存储在 Local Storage 中，因此支持跨域访问
    4. 分布式系统下扩展性强
+ 项目中使用`UserSupport.getCurrentUserId()`方法直接从token解析得到userId（实际上**token过期也能解析出用户id**），解析出id后先判断是否小于0，校验其合法性。
+ session
    - 服务器端写入 session 数据，向客户端浏览器返回 sessionid，浏览器将 sessionid保存在 cookie 中，当用户再次访问服务器时，会携带 sessionid，服务器会拿着 sessionid从数据库获取 session 数据，然后进行用户信息查询，查询到，就会将查询到的用户信息返回，从而实现状态保持。
    - 缺点：
        * 随着用户的增多，服务端压力增大；
        * cookie 被攻击者拦截，容易受到跨站请求伪造攻击
        * 扩展性不强：session保存在服务器内存中，多个服务器不共享
+ 关键特点：
    - 无状态 ：服务器无需存储 Session，适合分布式系统
    - 自包含：Payload 可以直接包含用户信息
    - 防篡改： Signature 基于密钥生成，任何修改都会导致签名失效
    - 过期控制 ：灵活设置有效期

<h4 id="o5QiI">双token</h4>
+ 单token缺陷：
    - 用户退出登录时 token 仍处在有效期内，则仍然可以使用该token 去访问系统资源。
    - 过期时间短，需要频繁重新登录。
+ refresh token：过期时间长；access token：过期时间短。
+ refresh token：仅在登录时和AT过期时传输，频率较低，泄露风险小；access token：频繁在客户端与服务端之间传输，泄露风险大。
+ 两者都存放在客户端中

<h4 id="X96hh">原始方案</h4>
1. 登录  
客户端提交用户名密码 → 服务端验证通过后，生成Access Token和Refresh Token，**都返回客户端**
2. 访问  
在请求头携带Access Token访问API
3. token刷新  
客户端检测到AccessToken过期 → 自动用Refresh Token请求新Access Token。（刷新Access Token并重试请求的过程主要由前端完成，后端的核心职责是提供一个验证Refresh Token并颁发新Access Token的接口）  
服务端验证RefreshToken
4. 退出登录  
删除RefreshToken，但AccessToken还能短期内访问

<h4 id="JNC2u">改进方案</h4>
1. 登录  
客户端提交用户名密码 → 服务端验证通过后，生成Access Token和Refresh Token，RT存入数据库，**仅AT返回客户端**。
2. 访问  
在请求头携带Access Token访问API
3. token刷新  
服务端验证AccessToken过期 → AccessToken中解析出id（过期也能解析），再用这个id去数据库中查对应的RefreshToken，验证RT，再返回新的AT
4. 退出登录  
删除RefreshToken，但AccessToken还能短期内访问



+ 时效性本质上由rt决定，那为什么不直接把单个token过期时间设置长一点？
    - Token在传输中可能被截，经常刷新的过期时间短的at安全性更高。

<h2 id="PL97U">权限控制 RBAC（Role-Based Access Control，基于角色的访问控制）</h2>
<h3 id="xBOWS">数据库表设计</h3>
user_role表：关联每个用户的 userId 和 roleId  
auth_role表：只是用来存放 roleId 对应的 name 和 code （感觉关联性不大）  
roleId 又通过 两个关联表 分别关联 两种控制表，所以总共有三张关联表：

![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396329-309b0320-de28-43be-9ec0-06f1d22dc40d.png)

1. userId 和 roleId
2. roleId 和 elementOperationId  **控制页面元素**（前端的按键，如“视频投稿按钮”）
3. roleId 和 menuId **控制页面访问**（接口如“购买邀请码”）

<h3 id="VAuY3">查表过程</h3>
@GetMapping("/user-authorities")  
根据userId查user_role表，查出的结果封装为UserRole类。关联表 t_user_role 作为主表，外连接查询。

```sql
SELECT ur.*, ar.NAME roleName, ar.CODE roleCode 
FROM t_user_role ur LEFT JOIN t_auth_role ar ON ur.roleId = ar.id 
WHERE ur.userId = #{userId}
```



根据 UserRole 的 roleId，联表查出的结果封装为 AuthRoleElementOperation 类。

![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396412-86492b84-39a8-4125-9ac8-745cafb45285.png)

```sql
SELECT areo.*, aeo.elementName, aeo.elementCode, aeo.operationType
FROM t_auth_role_element_operation areo 
LEFT JOIN t_auth_element_operation aeo 
ON areo.elementOperationId = aeo.id
WHERE areo.roleId 
IN 
<foreach collection="roleIdSet" item="roleId" index="index" open="(" close=")" separator=",">
     #{roleId}
</foreach>
```



同理查出AuthRoleMenu类。

![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396472-103095d8-be19-48eb-b0c5-e3ec65177675.png)

![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396535-f6cde591-73cb-4f86-abba-84f7a9e46ccc.png)

最后将这两个类的事例封装到UserAuthorities类的实例中。

<h3 id="WmHGW">Spring AOP（Aspect-Oriented Programming，面向切面编程）</h3>
<font style="color:rgb(44, 62, 80);">AOP能够将那些与业务无关，</font>**<font style="color:rgb(48, 79, 254);">却为业务模块所共同调用的逻辑或责任（例如事务处理、日志管理、权限控制等）封装起来</font>**<font style="color:rgb(44, 62, 80);">，便于</font>**<font style="color:rgb(48, 79, 254);">减少系统的重复代码</font>**<font style="color:rgb(44, 62, 80);">，</font>**<font style="color:rgb(48, 79, 254);">降低模块间的耦合度</font>**<font style="color:rgb(44, 62, 80);">，并</font>**<font style="color:rgb(48, 79, 254);">有利于未来的可拓展性和可维护性</font>**<font style="color:rgb(44, 62, 80);">。</font>

+ Aspecct
    - 封装横切逻辑的类，包含**切入点**和**通知**。
    - ApiLimitedRoleAspect 类（被 @Aspect 注解标记），封装了“接口角色权限校验”的横切逻辑。
+ Pointcut
    - 定义“哪些方法/类”需要被拦截（即横切逻辑的作用范围）。
    - @Pointcut("@annotation(com.tt.domain.annotation.ApiLimitedRole)")：拦截所有被`@ApiLimitedRole`注解标记的方法。
+ Advice
    - @Before：在目标方法执行前触发
    - @Before("check() && @annotation(apiLimitedRole)")：前置通知，在目标方法执行前校验权限。
    - @After：在目标方法执行后触发（无论是否抛出异常）
    - @AfterReturning：在目标方法成功执行并返回结果后触发（仅正常返回时）
+ JoinPoint
    - 程序执行过程中可被 AOP 拦截的“具体时机”（如方法调用、异常抛出等）。	
    - 装了目标方法的上下文（如方法参数、签名等），此处用于获取目标方法的执行信息。





1. 在dao层**自定义一个注解**`ApiLimitedRole`，通过第三步中切点的指定，使用了该注解的方法要走AOP。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Component
public @interface ApiLimitedRole {
    String[] limitedRoleCodeList() default {};
}
```

2. 在api层**定义切面类**：使用@Component 和@Aspect 配置 `ApiLimitedRoleAspect` 类，在这个类中配置 AOP。
3. 在`ApiLimitedRoleAspect` 类中定义切点：@Pointcut 定义切点为 check()方法，来告诉 spring 在什么时候进行依赖注入。这里指定了，表示当执行到我们自定义的这个注解的时候就执行。

实际的注解是由自定义的接口ApiLimitedRole调用。

被 @Pointcut 注解标记的方法（如你的 check()）本质是 “切入点签名”，**仅用于承载切入点表达式，不需要实现任何业务逻辑。因此方法体为空是正常的，甚至推荐这样做（避免误解）**。

4. 在切面类中@Before：方法执行前触发（例如权限校验）
5. 在方法内部：查询数据库得到当前用户所对应的权限列表，然后和自己设置的权限列表相比对，如果二者有交集则**抛出异常**不让访问。【如：LV0 的用户不能投稿，则将 LV0 写入禁止的权限列表，查询当前用户等级是否为 LV0】
6. 通过上述配置，只需要在需要权限验证的方法上面加上一个`@ApiLimitedRole`，并指定不能访问该接口的等级，即可通过 AOP 实现权限控制。

```java
@Order(1)
@Component
@Aspect
public class ApiLimitedRoleAspect {
    @Autowired
    private UserSupport userSupport;
    @Autowired
    private UserRoleService userRoleService;

    //定义切点：所有被@ApiLimitedRole注解标记的方法都会被拦截。
    // 标准切入点方法：仅定义切入点表达式，方法体为空
    @Pointcut("@annotation(com.tt.domain.annotation.ApiLimitedRole)")
    public void check() {}

    //前置通知：在目标方法执行前触发，并通过@annotation(apiLimitedRole)获取方法上的注解实例。
    @Before("check() && @annotation(apiLimitedRole)")
    //JoinPoint是Spring AOP中封装目标方法上下文信息的对象，通过它可以获取方法签名、参数、目标对象等元数据
    //这个方法可以随便命名。在 Spring AOP 中，通知方法（Advice Method）的名称没有固定要求，
    //核心是通过注解（如 @Before）标识其为“前置通知”
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId); //实际上根据id只能查出一个UserRole
        //注解中定义的,需要限制接口访问的角色代码列表
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        //用set去重 两个set的元素（Lv0）对应数据库中auth_role表的code字段
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        Set<String> userRoleSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        //取交集，如果用户的roleCode和限制表的roleCode有重合，则代表这个用户被限制了
        userRoleSet.retainAll(limitedRoleCodeSet);
        if (!userRoleSet.isEmpty()) {
            throw new ConditionException("用户所属角色无权访问该接口!");
        }
    }
}
```

具体切入点：在动态发布的api加入注解。

```java
@ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_CODE_LV0})
@DataLimited
@PostMapping("/user-moments")
public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
}
```



<h2 id="GoQur">关注</h2>
+ 相关接口：
    1. 添加关注
    2. 关注列表
    3. 粉丝列表
    4. 分页查询用户信息
+ 相关表：
    1. UserFollowing 关注关系表

![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396597-ab706458-4ad3-4242-9f25-ff6382f5430a.png)

    2. FollowingGroup 关注分组表![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396685-b78c86cd-2d85-467d-a318-b986d3be5bc4.png)

<h3 id="TYcgN">添加关注</h3>
在 addUserFollowings 方法前，加上`@Transactional` 开启事务。因为在这个方法同时实现了添加和更新操作，在每次添加前都进行一次删除，如果关注过某个相同用户，则**先删除掉原来的数据，再重新添加**。这样，就不用单独写 update 方法了。事务保证添加和删除操作同时成功或失败。

<h3 id="YLnq6">查询关注列表</h3>
1. 根据当前用户的 userId 查出 UserFollowing，userFollowingList
2. 遍历 userFollowingList 得到关注的用户的 userID，即followingIdSet
3. 根据 followingIdSet 查出每个关注用户的 userInfo，即userInfoList
4. 对比 userFollowingList 的 userID 和 userInfoList 的 userID，如果相等，更新UserFollowing中的userInfo参数 （冗余）
5. 展示关注的列表：
    - 按分组展示:
        1. 根据用户id查出所有分组`List<FollowingGroup> followingGroupList`
        2. 外层循环`followingGroupList`，内层循环遍历`userFollowingList`逐个对比`groupId`，相等则将对应的`userInfo`查出，set 到每一个`followingGroup`中。
        3. 把每一个`followingGroup`构成的加入 result。
    - 所有关注一并展示：
        1. new 一个`FollowingGroup allGroup`，这个分组并不存在数据库中，故没有id、type属性
        2. set userId、name（“全部关注“”） 和前面第三步查出的userInfoList
        3. 把这个`allGroup`加入 result。
+ 可以对UserFollowing表的 user_id + group_id **联合索引**完成查询，得到某个分组列表

<h3 id="s7oMN">查询粉丝列表</h3>
1. 查询 UserFollowing 表，确定 followingId 是本用户的关注关系列表`List<UserFollowing> fanUserFollowingList`，再由此得到粉丝id列表fanIdList
2. 根据粉丝id列表fanIdList查到UserInfo，一次性得到粉丝的信息列表fanInfoList
3. 查询 UserFollowing 表，得到本用户的关注关系列表`List<UserFollowing> myUserFollowingList`
4. 遍历 fanInfoList 和 fanUserFollowingList，把每个粉丝信息 set 到 每个粉丝的详细信息中。（UserFollowing 类中有个冗余变量 UserInfo，表示当前用户的信息）
5. 逐个对比 myUserFollowingList 的关注的用户的id和fanInfoList的粉丝的id，如果相等代表互粉（UserInfo 类中也有个冗余变量 followed，表是否互粉，表中没有这个字段，类中有）
+ （补充）直接查询互粉关系可以使用**内联查询**（两个t_user_following 自连接)：

```sql
-- 查询用户X的互粉好友（即X关注的人里，同时也关注X的用户）
SELECT
  a.followed_user_id AS mutual_friend_id
FROM
  t_user_following a
  JOIN t_user_following b ON a.followingId = b.userId -- a关注的人（b的user_id）
  AND a.userId = b.followingId -- a的user_id（X）是b关注的人
WHERE
  a.userId = X;  -- X是当前用户ID
```



<h3 id="HR3Ai">分页查询用户，已关注的做标记，支持模糊查询</h3>
+ 通过`@RequestParam`直接接受请求头的参数`no`（表示起始索引）`size`（表示每页数据条数），`nick`则是可选参数。

```java
@GetMapping("/user-infos")  //有RequestParam注解的输入参数必须传参
public JsonResponse<PageResult<UserInfo>> pageListUserInfos
(@RequestParam Integer no, @RequestParam Integer size, String nick) {... }
```

+ 还写了个泛型类`PageResult<T>`用来保存分页查询的结果，包含两个变量：`total`表示数据条数，`List<T> list`表示具体的数据列表。
+ 参数封装利用JsonObject类，params对象最后直接传到xml文件中，sql语句可以直接解析出参数

```java
//JsonObject是fastJson包内的一个类,和Map差不多用法,但比Map更智能和方便
JSONObject params = new JSONObject();
params.put("no", no);
params.put("size", size);
params.put("nick", nick);
params.put("userId", userId);
PageResult<UserInfo> result = userService.pageListUserInfos(params);
```



1. 将参数put进param实例中
2. 根据参数进行分页查询 t_user_info 表，查出结果`List<UserInfo>`，和数量`total`。将`List<UserInfo>`和`total` set 到`PageResult<UserInfo>`中
3. 再取出`List<UserInfo>`，标记followed字段
    1. 根据userId查出本用户的关注关系列表`List<UserFollowing>`
    2. 逐个对比两个表的id，有重合的就把UserInfo的followed字段设为true
4. 最终返回`PageResult<UserInfo> result`

<h2 id="LtiIb">动态</h2>
<h3 id="wQPUw">RocketMQ + Redis</h3>
userMoment表结构：

![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396739-87638e4c-10e4-4bd0-bb01-030b81f27f85.png)

+ 发布动态 @PostMapping("/user-moments")  
调用 MQ producer，将新动态放进去，然后让 MQ **异步**存储到 redis 中。这样可以减少用户等待时间。
    1. 先插入动态到数据库中。
    2. 将userMoment对象**转化成json**，调用**同步发送**方法推送到消息队列中。
+ 在RocketMQConfig.java中mq的操作逻辑  
在 RocketMQConfig 类中初始化生产者 DefaultMQProducer 和消费者 DefaultMQPushConsumer 实例，并为其指定对应的 group，以及通过 setNamesrvAddr(nameServerAddr) 为其指定 MQ 的 IP 地址。  
生产者bean

```java
  @Bean("momentsProducer")  //自定义该bean的名称
  public DefaultMQProducer momentsProducer() throws MQClientException {
      //新建一个生产者bean,为其指定生产者组的名称
      DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
      //连接nameServer
      producer.setNamesrvAddr(nameServerAddress);
      producer.start();
      //生产者bean创建完成,返回给IOC容器管理
      return producer;
  }
```

消费者bean

```java
  @Bean("momentConsumer")
  public DefaultMQPushConsumer momentsConsumer() throws MQClientException {
      //创建一个消费者并分配给默认的消费者组(和生产者组名称一样）
      DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
      //连接nameServer
      consumer.setNamesrvAddr(nameServerAddress);
      //订阅一个默认主题
      consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS, "*");
      //分配一个监听器,定义接收到消息后的处理逻辑(消息被push直接推动,这是只能被动消费的消费者)
      consumer.registerMessageListener(new MessageListenerConcurrently() {
          @Override
          public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
              ...(内部逻辑)
              //返回消息消费成功的状态码
              return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
          }
      });
      consumer.start();
      return consumer;
  }
```

监听器对消息的具体处理逻辑：

    1. 每次只处理一条消息，若消息为空，直接return消费成功。
    2. 否则将消息从**json转换为UserMoment实体类**。
    3. 从userMoment中获取到这个发布者的userId，根据它查出其所有粉丝。
    4. 遍历每个粉丝，得到粉丝的userId，在redis中对应的key则为"subscribed-" + userId。
    5. 根据 key 在 Redis 中查出 String subscribedListStr ，对应着这个id的用户的订阅的所有动态的列表。
    6. String subscribedListStr 不为空则将其转换为 List<UserMoment> subscribedList，为空则new一个list。
    7. 在列表中插入这个userMoment动态，再把它转换为String，更新Redis的值。



+ 获取动态 @GetMapping("user-subscribed-moments")  
直接去Redis中查询"subscribed-"+userId的键，对应的值是该用户关注的所有用户发布的动态列表

```java
public class VideoComment {
    private Long id;
    private Long videoId;
    private Long userId;//创建本条评论的用户的id
    private String comment;
    private Long replyUserId;//本评论回复所回复的用户的id,只有二级评论该字段才不为空
    private Long rootId;//根结点评论id,当评论为一级评论时,该字段为空
    private Date createTime;
    private Date updateTime;
    /*存放二级评论的列表,若本评论本身就为二级评论或本一级评论暂无人回复,则此字段为空*/
    private List<VideoComment> childList;
    private UserInfo userInfo;
    private UserInfo replyUserInfo;
}
```

+ VideoComment类中包含变量`List<VideoComment> childList`，用于表示子评论。
+ 这里会存在递归，评论里有评论，所有只设计了两层。如果设计多层，每一层都要查表，要查出子评论，还要把userInfo等冗余信息set进去。
+ 如果要多层，rootId这个字段就表示上一层评论的id。

<h2 id="ohlB2">文件</h2>
+ FastDFS文件服务器搭建、相关工具类开发
+ 视频上传、视频处理、视频获取、视频在线播放、视频下载
+ 弹幕系统、数据统计、社交属性(点赞、投币、收藏、评论)

<h3 id="tVBvC">FastDFS</h3>
+ 什么是FastDFS：开源的轻量级分布式文件系统，用于解决大数据量存储和负载均衡等问题。
+ 优点：支持HTTP协议传输文件(结合Nginx);对文件内容做Hash处理，节约磁盘空间;支持负载均衡、整体性能较佳
+ 适用系统类型：中小型系统
+ OSS：阿里的，性能更高的文件系统
+ FastDFS的两个角色：跟踪服务器(Tracker)、存储服务器（Storage）
    - 跟踪服务器：主要做**调度**工作，起到负载均衡的作用。它是客户端和存储服务器交互的枢纽
    - 存储服务器：主要提供服务，存储服务器是以组(Group)为单位，每个组内可以有多台存储服务器，数据互为备份。文件及属性(Meta Data)都保存在该服务器上![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396795-caeb599e-affc-4b79-9223-fdd15235ffc3.png)

<h3 id="Tja31">nginx</h3>
反向代理，负载均衡

+ 普通的正向代理：服务端不知道客户端、客户端知道代理端![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396870-1d485521-aebb-4045-bbf5-f6063911a8fc.png)
+ nginx 反向代理：服务端知道客户端、客户端不知道代理端![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529396943-f9c38f4c-477b-4711-9448-a81c94d1d75d.png)
+ Nginx 结合 FastDFS 实现文件资源 HTTP 访问，相当于屏蔽了具体的 tracker 服务器内网 ip。
    - 在有多个 tracker 的时候，当外面来请求的时候，**直接请求 nginx 的地址**，
    - 再由 nginx 进行请求转发，转发给合适的内网服务器（tracker）。
    - 再由 tracker 找到存储服务器
    - 按照下面的流程图，是转发给 gateway 网关，相当于给 gateway 做负载均衡。![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529397009-1a59e7a0-d97c-4259-892d-3143b5122a4d.png)
+ `service.util.FastDFSUtil`

<h3 id="Eganb">文件分片（实际上是前端完成的任务）</h3>
`void convertFileToSlices(MultipartFile multipartFile)`

+ 根据`SLICE_SIZE`参数（每个分片的大小），把大文件分成多个小片。（最后一个分片大小不固定）
+ 分片的文件存储自己设置的本机路径中

<h3 id="J1Bra">断点续传</h3>
`String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo)`

+ file：当前文件的某个分片
+ fileMd5：文件内容进行md5加密后形成的唯一标识符字符串，用于标识文件，会用于redis的key
+ sliceNo：当前上传的分片的序号
+ totalSliceNo：整个文件总共包含的分片数
+ 返回值：文件在FastDFS中存储的路径，如果没完成最后一片的上传，路径是空字符串

Redis存储的键所有都是由**键名拼接md5密码**，用于区分不同文件的分片。同一个文件的不同文件的MD5密码都是一样的。

+ pathKey：在fastDFS的**存储路径**
+ uploadedSizeKey：已上传完成的部分的**文件的大小**
+ uploadedNoKey：已完成上传的**分片的最大序号**

上传过程：

1. 查redis的uploadedSizeKey，如果为空，则设置为初始值0；
2. 如果 sliceNo==1 即第一个分片，调用uploadAppenderFile函数（此函数用于上传第一片），得到path，写入redis，将 uploadedNoKey 键设为1
3. 如果不是第一个分片，则调用modifyAppenderFile函数（此函数用于追加上传），将uploadedNoKey键+1；
    - 注：利用了 FastDFS 的 **"追加写入"** 特性来实现分片上传，其核心是没有物理合并文件的过程，而是通过顺序追加的方式直接在 FastDFS 上形成完整的文件。
4. 更新已上传的大小：uploadedSizeKey += file.getSize() ；
5. 最后判断uploadedNoKey是否等于totalSliceNo，若相等表明所有分片上传完成，删除Redis中对应的三个key，返回文件所在FastDFS的路径path。
+ **补充应对一些异常情况**:
    1. 可以增加校验，理论上`uploadedNoKey * 每个分片的大小 = uploadedSizeKey`.
        * 例如，`SLICE_SIZE`设置为5mb，经过三次上传后，Redis的实际存储的uploadedSizeKey = 15728640 = 1024 * 1024 * 5 * 3
    2. 避免重复上传/跳跃上传：追加上传前，**校验分片的序号**。例如已经上传到第二片，Redis中存储的uploadedNoKey就应该是2，那么就限制只能追加上传第3片。
    3. 对于Redis宕机导致的问题，例如文件上传成功，但Redis更新失败，可能没有什么好的解决办法

<h2 id="Kda4i">视频</h2>
<h3 id="UcO7b">在线看视频</h3>
下面是直接在浏览器中访问FastDFS中的文件的响应信息：

![](https://cdn.nlark.com/yuque/0/2025/jpg/60106566/1756529397083-86def248-994f-45da-a7e9-bb1d39c70e17.jpg)

`void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String path)`

1. 前端发请求给后端，require请求头中包含Content-Range信息。
2. 后端解析请求头和响应头，将其封装好**转发给DFS服务器**。
    1. 获取文件信息

```java
FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
long totalFileSize = fileInfo.getFileSize();
```

    2. 构建资源URL

```java
String url = fdfsStorageAddrPrefix + path;
```

    3. 复制请求头：将客户端原始请求的所有请求头（如 User-Agent、Range 等）复制到 headers Map中，以便转发给FastDFS服务器。
    4. 解析Range请求头（分片的关键参数，实际上通过拖动进度条，浏览器会<font style="color:#DF2A3F;">自动调整range的范围</font>）  
处理客户端的分片请求（Range 头），未提供时默认请求整个文件。  
Range 格式示例：bytes=0-499（请求0到499字节）。
    5. 计算分片范围  
begin：从数组索引1获取起始字节位置（如 range[1] = "0"）。  
end：从数组索引2获取结束字节位置（如 range[2] = "499"），未提供时设为文件末尾。  
len：计算分片实际字节长度。
    6. 设置HTTP响应头
    7. 转发请求到FastDFS服务器

```java
//转发完整的请求给DFS服务器，并提前为DFS做好一些响应头的设置
HttpUtil.get(url, headers, response);
```



<h3 id="DCw9x">视频点赞评论</h3>
<h4 id="moBFT">查询点赞数量：</h4>
1. 传入(Long videoId, Long userId)
2. Long count = videoDao.getVideoLikes(videoId); 查询数量
3. VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
4. 无论是否登录都可以查询，如果未登录，userId为null则第二条语句查出为空
5. 额外增加一个标记当前用户是否点赞了这条视频

<h4 id="tMNNC">分页查询评论：</h4>
VideoComment表结构：  
rootid 表示评论等级。如果为null，则该条评论是一级评论。

![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529397153-efae2c6d-57ff-4113-98f1-a245b16d9def.png)

+ 和前面的分页查询用户一样，通过`@RequestParam`直接接受请求头的参数`no`（表示起始索引）`size`（表示每页数据条数），`videoId`（视频id）。
+ 查询结果同样用 PageResult 类封装起来。
+ 参数通过`Map<String, Object> params`来封装。
1. 先由start、limit、videoId分页查询数据库得到一级评论列表`List<VideoComment> parentCommentList`
2. 再得到一级评论的评论id集合：`Set<Long> parentCommentIdSet`
3. 再由 parentCommentIdSet 查询数据库得到二级评论列表：`List<VideoComment> childCommentList`
4. 再由两个评论列表分别查询评论对应的用户id，把两组 userId 合并成一个总的用户id set
5. 根据这个idset查询数据库的UserInfo得到总的userinfo（冗余）
6. 遍历二级评论把一级评论对应的二级评论的表赋值给一级评论的对应元素中
7. VideoComment类中有一个List<VideoComment> childList元素用于存储对应的子评论

<h2 id="YZUlC">弹幕</h2>
<h3 id="ps1w1">WebSocket</h3>
WebSocket 是一种基于 TCP 连接的 **全双工** 通信协议，即客户端和服务器可以同时发送和接收数据。（http是**半双工**：同一时间里，客户端和服务器只能有一方主动发数据，这就是所谓的半双工。）  
WebSocket 协议本质上是 **应用层** 的协议，用于弥补 HTTP 协议在持久通信能力上的不足。客户端和服务器仅需一次握手，两者之间就直接可以创建持久性的连接，并进行双向数据传输。

<h3 id="FgS86">弹幕系统架构</h3>
![](https://cdn.nlark.com/yuque/0/2025/png/60106566/1756529397216-112b38fe-9164-4031-bc82-73bc40be2287.png)

+ 通过service.config.WebSocketConfig来引入WebSocket

```java
@Component
@ServerEndpoint("/imserver/{token}") // 对应弹幕服务的请求地址
public class WebSocketService {
    //本类的相关日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //AtomicInger是维持了原子性操作的Integer封装类,使用它的原因是在高并发场景下需要保持在线人数这个属性的线程安全性
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    //这个Map也是为了线程安全而被制造出来的,每个客户端都有一个WebSocketService bean对象(多例模式),通过这个Map来查找
    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();

    //每个客户端与服务端之间建立通信后都有一个会话,也就是这个session属性
    private Session session;

    //每个WebSocketService bean的唯一标识字段
    private String sessionId;

    private Long userId;

    private static ApplicationContext APPLICATION_CONTEXT;
    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }
}
```

+ AtomicInteger ONLINE_COUNT：在线人数
+ ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP
+ ApplicationContext APPLICATION_CONTEXT：用来解决多例模式下无法直接注入Spring Bean的问题  
将Spring上下文保存到WebSocketService的静态变量中

弹幕功能及在线人数统计  
@PathParam("token")对应之前请求中的 @PathVariable Integer id  
用户登录进来之后，首先走@OnOpen，向WEBSOCKET_MAP 中添加数据，key 是sessionid，value 就是当前用户对应的 websoketservice。同时，将在线人数加一。  
//redis中有的话就先查 redis中，没有的话就查 mysql，然后从再拷贝到 redis中

