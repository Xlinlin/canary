package com.xiaogj.x3.canary.ribbon.config;

import com.xiaogj.x3.canary.ribbon.interceptor.CanaryRequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * webmvc模式下的 webconfigurer配置
 * <p>
 * 必须存在于webmvc模式下
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/8 10:07
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@Slf4j
public class CanaryWebMvcConfigurer implements WebMvcConfigurer {

    @Bean
    public CanaryRequestInterceptor getInterceptor() {
        log.info("初始化灰度Webmvc拦截..CanaryWebMvcConfigurer");
        return new CanaryRequestInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("初始化灰度将Webmvc拦截注入到拦截器中..CanaryWebMvcConfigurer");
        registry.addInterceptor(getInterceptor()).addPathPatterns("/**");
    }
}
