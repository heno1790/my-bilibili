package com.tt.service.util;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.CountDownLatch2;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: RocketMQUtil
 * Package: com.tt.service.util
 * Description:
 *
 * @Create 2025/3/24 11:39
 */
public class RocketMQUtil {
    //同步发送
    public static void syncSendMsg(DefaultMQProducer producer, Message msg) throws Exception {
        SendResult sendResult = producer.send(msg);
        System.out.println(sendResult);
    }

    //异步发送且有回调函数
    public static void asyncSendMsg(DefaultMQProducer producer, Message msg) throws Exception {
        int messageCount = 2;  //异步发送的消息的条数
        CountDownLatch2 countDownLatch = new CountDownLatch2(messageCount);
        for(int i = 0; i < messageCount; i++) {
            producer.send(msg, new SendCallback() { //从这里开始fork所有子线程会等到countdownlatch计数到0了才能完全执行并返回
                @Override
                public void onSuccess(SendResult sendResult) {
                    //同步器，当两个线程同时到达这里以后,才会放行
                    countDownLatch.countDown();  // 计数器减1
                    System.out.println(sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    //倒计时器开始转动
                    countDownLatch.countDown();
                    System.out.println("发送异步消息时发生了异常!" + e);
                    e.printStackTrace();
                }
            });
        }
        //计数器等待5s,若还没计数到0则直接返回
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}
