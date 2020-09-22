package com.xiaogj.x3.canary.ribbon.config;

import com.xiaogj.x3.canary.ribbon.interceptor.CanaryFeignInterceptor;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * restTemplate 拦截添加
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/8 10:26
 */
@Configuration
@Slf4j
public class CanaryFeignInterceptorConfigurer {

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        log.info("初始化灰度Feign拦截..CanaryFeignInterceptorConfigurer");
        return new CanaryFeignInterceptor();
    }
}
