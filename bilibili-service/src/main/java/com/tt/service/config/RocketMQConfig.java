package com.tt.service.config;

import com.alibaba.fastjson.JSONObject;
import com.tt.domain.UserFollowing;
import com.tt.domain.UserMoment;
import com.tt.domain.constant.DanmuConstant;
import com.tt.domain.constant.UserMomentsConstant;
import com.tt.service.UserFollowingService;
import com.tt.service.webSocket.WebSocketService;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: RocketMQConfig
 * Package: com.tt.service.config
 * Description:
 *
 * @Create 2025/3/23 16:11
 */
@Configuration  //标识配置文件
public class RocketMQConfig {
    //该注解将配置文件的该属性注入到该字段属性中
    @Value("${rocketmq.namesrv.address}")
    private String nameServerAddress;

    //操作redis的官方工具类,本身就是bean
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserFollowingService userFollowingService;


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
                //消费生产者发送的动态发布消息,消费方式是:接收broker的消息并推送给订阅这个Up的用户
                MessageExt msg = msgs.get(0);  //每次只有一条数据，故msgs只有一个元素
                if (msg == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String bodyStr = new String(msg.getBody());
                //得到userMoment实体类
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);
                //获取该Moments的up主的粉丝群体,定向push到redis中等待用户查找
                Long userId = userMoment.getUserId();  //发布动态的用户的id
                List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                for (UserFollowing fan : fanList) {
                    String key = "subscribed-" + fan.getUserId();  //对应redis中的键
                    //先查查redis库中的原有的字典字段
                    String subscribedListStr = redisTemplate.opsForValue().get(key);
                    List<UserMoment> subscribedList;  //redis中用户键对应的值(某个id的用户的订阅的所有消息的列表）
                    if (StringUtil.isNullOrEmpty(subscribedListStr)) { //为空则创建一个新的空列表
                        subscribedList = new ArrayList<>();
                    } else { // 不为空则把列表转换成List<UserMoment>
                        subscribedList = JSONObject.parseArray(subscribedListStr, UserMoment.class);
                    }
                    subscribedList.add(userMoment);  //添加新纪录到列表中
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subscribedList)); //更新redis的值
                }

                //返回消息消费成功的状态码
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws MQClientException {
        //新建一个生产者bean,为其指定默认的生产者组
        DefaultMQProducer producer = new DefaultMQProducer(DanmuConstant.GROUP_DANMUS);
        //连接nameServer
        producer.setNamesrvAddr(nameServerAddress);
        producer.start();
        //生产者bean创建完成,返回给IOC容器管理
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws Exception {
        //创建一个消费者并分配给默认的消费者组
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(DanmuConstant.GROUP_DANMUS);
        //连接nameServer
        consumer.setNamesrvAddr(nameServerAddress);
        //订阅一个默认主题
        consumer.subscribe(DanmuConstant.TOPIC_DANMUS, "*");
        //注册回调实现类(监听器类)来处理从broker中拉取来的,需要消费的信息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //获取消息体
                MessageExt msg = msgs.get(0);
                byte[] msgByte = msg.getBody();
                String bodyStr = new String(msgByte);
                //将String型消息体解析封装回一种类Map类
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");
                //获取当前消息对应的会话对象
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                //核心消费代码
                if (webSocketService.getSession().isOpen()) {
                    try {//这是webSocket双工通信的体现
                        webSocketService.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //标记该消息为已成功消费(本例之消费亦即已将新弹幕推送给当前消息所对应的用户会话)
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }
}
