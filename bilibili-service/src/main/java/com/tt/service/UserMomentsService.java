package com.tt.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tt.dao.UserMomentsDao;
import com.tt.domain.UserMoment;
import com.tt.domain.constant.UserMomentsConstant;
import com.tt.service.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * ClassName: UserMomentsService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/3/24 11:51
 */
@Service
public class UserMomentsService {
    @Autowired
    private UserMomentsDao userMomentsDao;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void addUserMoments(UserMoment userMoment) throws Exception {
        //先插入一条动态到数据库中
        userMoment.setCreateTime(new Date());
        userMomentsDao.addUserMoments(userMoment);
        //除了将动态发布到数据库外,还要将该消息发到rocketMQ,让消费者将其推送给粉丝们
        //生产者发送消息给MQ,告知订阅的消费者,某条动态被发布到数据库,可以去查阅了
        //生产者和消费者是配置类里的bean,这类bean的引入方式之一是通过上下文对象的方式引入
        //通过applicationContext得到MQconfig中自己创建的生产者
        DefaultMQProducer producer = (DefaultMQProducer)applicationContext.getBean("momentsProducer");
        //userMoment 对象转化成 json，推送到消息队列中
        Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
        RocketMQUtil.syncSendMsg(producer, msg);
    }

    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        //在redis里根据key来查找用户订阅的所有动态
        String key = "subscribed-"+userId;
        String subscribedList = redisTemplate.opsForValue().get(key);
        return JSONArray.parseArray(subscribedList, UserMoment.class);
    }
}
