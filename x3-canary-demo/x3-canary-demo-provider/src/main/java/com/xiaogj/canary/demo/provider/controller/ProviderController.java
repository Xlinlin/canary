package com.xiaogj.canary.demo.provider.controller;

import com.xiaogj.x3.canary.common.context.CanaryConstants;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: ProviderController
 * @Description:
 * @author: xiaolinlin
 * @date: 2020/9/21 14:26
 **/
@RestController
@RequestMapping("/provider")
public class ProviderController {

    @RequestMapping("/test")
    public String provider(HttpServletRequest request) {
        String version = request.getHeader(CanaryConstants.HEADER_VERSION);
        String tenant = request.getHeader(CanaryConstants.TENANT_KEY);
        return "provider: " + tenant + "-" + version;
    }

}
