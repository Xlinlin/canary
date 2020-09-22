package com.xiaogj.canary.demo.consumer.controller;

import com.xiaogj.canary.demo.consumer.feign.ProviderFeign;
import com.xiaogj.x3.canary.common.context.CanaryConstants;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: ConsumerController
 * @Description:
 * @author: xiaolinlin
 * @date: 2020/9/21 13:36
 **/
@RestController
@RequestMapping("/consumer")
@Slf4j
public class ConsumerController {

    @Autowired
    private ProviderFeign providerFeign;

    @RequestMapping("/test")
    public String consumer(HttpServletRequest request) {
        String version = request.getHeader(CanaryConstants.HEADER_VERSION);
        String tenant = request.getHeader(CanaryConstants.TENANT_KEY);
        return "consumer: " + tenant + "-" + version;
    }

    @RequestMapping("/provider")
    public String provider(HttpServletRequest request) {
        String version = request.getHeader(CanaryConstants.HEADER_VERSION);
        String tenant = request.getHeader(CanaryConstants.TENANT_KEY);
        log.info("Consumer version: {}, tenant info: {}", version, tenant);
        return providerFeign.provider();
    }
}
