package com.tt.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * ClassName: WebSocketConfig
 * Package: com.tt.service.config
 * Description:
 *
 * @Create 2025/6/16 17:20
 */
@Configuration
public class WebSocketConfig {
    //该配置类创建一个bean，这个bean是webSocket服务的服务端点导出者对象,负责服务端的webSocket服务的发现和获取
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
