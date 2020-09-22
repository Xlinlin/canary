package com.xiaogj.canary.demo.consumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @ClassName: ProviderFeign
 * @Description:
 * @author: xiaolinlin
 * @date: 2020/9/21 14:18
 **/
@FeignClient(name = "canary-provider",path = "/provider")
public interface ProviderFeign {

    @RequestMapping("/test")
    String provider();
}
