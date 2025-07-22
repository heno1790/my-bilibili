package com.tt.service.webSocket;

import com.alibaba.fastjson.JSONObject;
import com.tt.domain.Danmu;
import com.tt.domain.constant.DanmuConstant;
import com.tt.service.DanmuService;
import com.tt.service.util.RocketMQUtil;
import com.tt.service.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: WebSocketService
 * Package: com.tt.service.webSocket
 * Description:
 *
 * @Create 2025/6/16 17:22
 */
@Component
@ServerEndpoint("/imserver/{token}")
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

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }
//WebsocketService是多例模式,也就是会有多个WebSocketService对象。而SpringBoot的IOC容器是单例模式,
    //所以无法通过@Autowired注解注入RedisTemplate对象,因为RedisTemplate对象是单例模式的,
    //所以只能通过setApplicationContext()方法来注入。
    //前面获得MQProducer对象的方式也是通过ApplicationContext的getBean()方法来获得的。
    //但获取ApplicationContext的方式是通过Autowired注解来注入的,而不是通过setApplicationContext()方法来注入的。
    //@Autowired
    //private RedisTemplate<String, String> redisTemplate;

    private static ApplicationContext APPLICATION_CONTEXT;

    public WebSocketService() {
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }

    //当客户端和服务端成功建立一次WebSocket长连接对象后，会进入对应的WebSocektService bean对象并调用这个建立成功的方法
    @OnOpen
    public void openConnection(Session session, @PathParam("token") String token) {
        //RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) APPLICATION_CONTEXT.getBean("redisTemplate");

        //从请求路径的路径参数中获取令牌信息，解析出用户id(若令牌令牌存在且合法，也就是客户端处于登录状态，才解析的出来)
        try {
            this.userId = TokenUtil.verifyToken(token);
        } catch (Exception e) {
        }
        //为此连接的bean对象相应的字段值赋值
        sessionId = session.getId();
        this.session = session;
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            /*说明本用户之前已经与服务端建立过一次长连接了(而且还没下线)但不知何故,这里又建立了一次长连接，(
            比如同时开两个浏览器窗口看同一个视频(但是是同一个登录用户，只能算一个在线人数,这只是我的猜想）
            所以删掉Map里的旧对象换成新的，并且在线人数不变,会话维持两个还是一个有待验证，我觉得是两个。
             */
            //先把旧的删掉，再把新的放进去
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        } else {//新的会话连接，在线人数需要加一
            WEBSOCKET_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("用户:" + sessionId + "连接成功,当前在线人数为:" + ONLINE_COUNT.get());
        //还需告知前端连接成功了,没办法,websocket这个把消息给前端的方法会抛出受检异常（非RunTime Exception），只能try,catch一下
        try {
            this.sendMessage("0");
        } catch (IOException e) {
            logger.error("连接异常");
        }
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    //客户端或服务端请求关闭此连接时，进入此接口做善后工作
    @OnClose
    public void closeConnection() {
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info("用户:" + sessionId + "退出,当前在线人数为:" + ONLINE_COUNT.get());
    }

    //服务端接收到前端发来的字符串信息(这里是字幕信息)后进入的接口
    @OnMessage
    public void onMessage(String message) throws Exception {
        logger.info("用户信息:" + sessionId + ",报文:" + message);
        if (!StringUtil.isNullOrEmpty(message)) {
            try {
                //将弹幕信息群发给所有与后台服务端建立了webSocket连接的客户端群
                for (Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()) {
                    WebSocketService webSocketService = entry.getValue();
                    //直接发送弹幕消息
                    //if (webSocketService.session.isOpen()) {
                    //    webSocketService.sendMessage(message);
                    //}
                    //利用RocketMQ发送消息，导入之前自定义配置好的生产者bean
                    DefaultMQProducer danmusProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");
                    //将要托付给生产者发给MQ的消息封装起来
                    JSONObject jsonObject = new JSONObject();//和Map用法相同
                    jsonObject.put("message", message);//要群发的弹幕信息
                    jsonObject.put("sessionId", webSocketService.getSessionId());//当前连接的用户的会话id
                    Message msg = new Message(DanmuConstant.TOPIC_DANMUS, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    //消息封装完成,可以让生产者发出去了(异步发送,用rocketMQ解决高并发情况下可能出现的各种问题)
                    RocketMQUtil.asyncSendMsg(danmusProducer, msg);
                }
                //封装这条新弹幕，将其写进数据库和redis中
                if (this.userId != null) {
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    //System.out.println(danmu.getContent());
                    danmu.setUserId(userId);
                    danmu.setCreateTime(new Date());
                    /*本类是一个多例bean,在注入外部依赖bean时,必须以这种主动注入的形式注入,因为常规的Autowired注入的话只能注入一次
                    事实上,这里多个WebSocketService实例对象调用的是同一个danmuService Bean对象,前者是多例，后者还是单例*/
                    DanmuService danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
                    danmuService.asyncAddDanmu(danmu);//异步操作的优点是，不占用过长时间的主线程(开很多的分线程去写数据库，主线程则继续往下走)
                    /*关于将弹幕保存到数据库的过程的性能提升，也可以把保存到数据库这个操作扔到MQ中排队，这里就先不写了，和之前
                    的流程一样,优点是不用直接在数据库前面排队，这样可能会因排队超时等原因导致原本要写入数据库的数据丢失*/
                    danmuService.addDanmusToRedis(danmu);
                    /*redis写操作非常快,所支持并发量非常高，比数据库高的多，可以就用同步写的操作，不用异步优化也可以*/
                }
            } catch (Exception e) {
                logger.error("弹幕接收出现问题");
                e.printStackTrace();
            }
        }
    }

    //本接口定时向连接的客户端返回当前视频在线观看的人数
    @Scheduled(fixedRate = 5000)//5s钟触发一次的定时任务
    private void noticeOnlineCount() throws IOException {
        //遍历获取所有处于连接状态的客户端，向他们发消息即可
        for (Map.Entry<String, WebSocketService> entry : WebSocketService.WEBSOCKET_MAP.entrySet()) {
            WebSocketService webSocketService = entry.getValue();
            if (webSocketService.session.isOpen()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "当前在线人数为:" + ONLINE_COUNT.get());
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }

    @OnError
    public void oneError(Throwable throwable) {

    }


}
