package com.tt.service.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ClassName: JsonHttpMessageConverterConfig
 * Package: com.tt.config
 * Description:
 *
 * @Create 2025/3/17 17:10
 */
@Configuration  //标识配置相关的类，内含component注解
public class JsonHttpMessageConverterConfig {
    //public static void main(String[] args){
    //    List<Object> list = new ArrayList<>();
    //    Object o = new Object();
    //    list.add(o);
    //    list.add(o);
    //    System.out.println(list.size());
    //    // 把list转换为JSON字符串打印，不关闭循环引用，因为list中有两个相同元素，打印的第二个元素是地址信息
    //    System.out.println(JSONObject.toJSONString(list));
    //    // 关闭循环引用检测，会打印出两个一样的结果
    //    System.out.println(JSONObject.toJSONString(list, SerializerFeature.DisableCircularReferenceDetect));
    //}

    @Bean
    @Primary
    public HttpMessageConverters fastJsonHttpMessageConverters(){
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConfig.setSerializerFeatures(  //json数据序列化相关配置
                SerializerFeature.PrettyFormat,  //格式化输出
                SerializerFeature.WriteNullStringAsEmpty,  //对null值转换为空字符串
                SerializerFeature.WriteNullListAsEmpty,  //对null值list转换为空字符串
                SerializerFeature.WriteMapNullValue,  //对map值list转换为空字符串
                SerializerFeature.MapSortField,  //对Map对象先排序再转为json
                SerializerFeature.DisableCircularReferenceDetect  // 禁用循环引用
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //如果使用feign进行微服务间的接口调用，则需要加上该配置
        //fastConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpMessageConverters(fastConverter);
    }
}
