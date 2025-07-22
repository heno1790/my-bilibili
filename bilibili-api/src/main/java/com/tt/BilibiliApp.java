package com.tt;

import com.tt.service.webSocket.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ClassName: BilibiliApp
 * Package: com.tt
 * Description:
 *
 * @Create 2025/3/12 15:44
 */
@SpringBootApplication
@EnableTransactionManagement//开启项目的事务管理器
@EnableAsync//添加弹幕中用到的异步调用@Async注解
@EnableScheduling //定时发送在线人数用到@Scheduled注解
public class BilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(BilibiliApp.class, args);
        WebSocketService.setApplicationContext(app);
    }
}
