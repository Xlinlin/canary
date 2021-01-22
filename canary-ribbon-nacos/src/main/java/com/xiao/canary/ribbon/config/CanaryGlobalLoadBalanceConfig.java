package com.xiao.canary.ribbon.config;

import com.netflix.loadbalancer.IRule;
import com.xiao.canary.ribbon.nacos.CanaryNacosRibbonRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName: CanaryGlobalLoadBalanceConfig
 * @Description: 全局路由规则配置
 * @author: xiaolinlin
 * @date: 2020/9/18 11:57
 **/
@Configuration
@ConditionalOnProperty(name = "xiaogj.x3.canary.enabled", havingValue = "true")
@ComponentScan(basePackages = "com.xiaogj.x3.canary.ribbon")
@Slf4j
public class CanaryGlobalLoadBalanceConfig {

    @Bean
    public IRule canaryRule() {
        log.info("初始化灰度路由规则..CanaryNacosRibbonRule");
        return new CanaryNacosRibbonRule();
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(){
        log.warn("未找到RestTemplate..初始化默认的RestTemplate");
        return new RestTemplate();
    }
}
