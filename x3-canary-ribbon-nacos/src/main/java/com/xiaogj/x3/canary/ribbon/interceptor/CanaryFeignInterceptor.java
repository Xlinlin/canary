package com.xiaogj.x3.canary.ribbon.interceptor;

import com.xiaogj.x3.canary.common.context.CanaryConstants;
import com.xiaogj.x3.canary.common.context.CanaryContext;
import com.xiaogj.x3.canary.common.context.CanaryFilterContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * feign拦截，将header信息保存到上下文中传递参数
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/8 09:46
 */
@Slf4j
public class CanaryFeignInterceptor implements RequestInterceptor {

    /**
     * Called for every request. Add data using methods on the supplied {@link RequestTemplate}.
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        CanaryContext ribbonFilterContext = CanaryFilterContextHolder.getCurrentContext();
        String version = ribbonFilterContext.get(CanaryConstants.HEADER_VERSION);
        if (StringUtils.isNotEmpty(version)) {
            log.info("2.CanaryFeignInterceptor设置灰度版本：{}到请求头中", version);
            template.header(CanaryConstants.HEADER_VERSION, version);
        }
    }
}
