package com.xiaogj.x3.canary.ribbon.config;

import com.xiaogj.x3.canary.ribbon.interceptor.CanaryRestInterceptor;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 将interceptor注入进去
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/8 10:19
 */
@Component
@ConditionalOnBean(RestTemplate.class)
@Slf4j
public class CanaryRestTemplateConfigurer implements ApplicationContextInitializer {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Initialize the given application context.
     *
     * @param applicationContext the application to configure
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("初始化灰度RestTemplate拦截..CanaryRestTemplateConfigurer");
        restTemplate.setInterceptors(Collections.singletonList(new CanaryRestInterceptor()));
    }
}
