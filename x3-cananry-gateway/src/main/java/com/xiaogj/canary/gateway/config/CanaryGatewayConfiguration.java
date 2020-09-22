package com.xiaogj.canary.gateway.config;

import com.xiaogj.canary.gateway.filter.CanaryLoadBalancerFilter;
import com.xiaogj.canary.gateway.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: CanaryGatewayConfig
 * @Description: 初始化
 * @author: xiaolinlin
 * @date: 2020/9/21 9:23
 **/
@Configuration
@Slf4j
public class CanaryGatewayConfiguration {

    /**
     * @param loadBalancerClientFactory :
     * @param loadBalancerProperties :
     * @param tenantService :
     * @return com.xiaogj.canary.gateway.filter.CanaryLoadBalancerFilter
     * @author xiaolinlin
     * @date 9:27 2020/9/21
     **/
    @Bean
    @ConditionalOnBean(TenantService.class)
    public CanaryLoadBalancerFilter canaryLoadBalancerFilter(LoadBalancerClientFactory loadBalancerClientFactory,
        LoadBalancerProperties loadBalancerProperties, TenantService tenantService) {
        log.info("=====================初始化灰度过滤器");
        return new CanaryLoadBalancerFilter(loadBalancerClientFactory, loadBalancerProperties, tenantService);
    }
}
