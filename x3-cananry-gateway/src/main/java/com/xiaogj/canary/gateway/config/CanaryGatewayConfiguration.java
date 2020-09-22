package com.xiaogj.canary.gateway.config;

import com.xiaogj.canary.gateway.filter.CanaryLoadBalancerFilter;
import com.xiaogj.canary.gateway.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @ClassName: CanaryGatewayConfig
 * @Description: 初始化
 * @author: xiaolinlin
 * @date: 2020/9/21 9:23
 **/
@Configuration
@ConditionalOnProperty(name = "xiaogj.x3.canary.enabled", havingValue = "true")
@Slf4j
public class CanaryGatewayConfiguration {

    /**
     * 初始化灰度过滤器
     *
     * @param loadBalancerClientFactory :
     * @param loadBalancerProperties :
     * @param tenantService : 租户服务
     * @return com.xiaogj.canary.gateway.filter.CanaryLoadBalancerFilter
     * @author xiaolinlin
     * @date 9:27 2020/9/21
     **/
    @Bean
    @ConditionalOnBean(TenantService.class)
    public CanaryLoadBalancerFilter canaryLoadBalancerFilter(LoadBalancerClientFactory loadBalancerClientFactory,
        LoadBalancerProperties loadBalancerProperties, TenantService tenantService, Environment environment) {
        log.info("初始化灰度过滤器CanaryLoadBalancerFilter");
        return new CanaryLoadBalancerFilter(loadBalancerClientFactory, loadBalancerProperties, environment, tenantService);
    }
}
