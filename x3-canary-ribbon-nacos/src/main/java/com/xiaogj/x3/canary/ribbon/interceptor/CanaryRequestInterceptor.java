package com.xiaogj.x3.canary.ribbon.interceptor;

import com.xiaogj.x3.canary.common.context.CanaryConstants;
import com.xiaogj.x3.canary.common.context.CanaryFilterContextHolder;
import com.xiaogj.x3.canary.common.context.TenantContextHolder;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 请求头拦截，仅针对webmvc的
 *
 * <p>
 * 实现WebMvcConfigurer.addInterceptors
 * <p>
 * registry.addInterceptor(getInterceptor()).addPathPatterns("/**");
 * </p>
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/8 08:55
 */
@Slf4j
public class CanaryRequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        // 获取头部header
        Enumeration<String> headers = request.getHeaders(CanaryConstants.HEADER_VERSION);
        if (headers.hasMoreElements()) {
            String version = headers.nextElement();
            log.debug("1. CanaryRequestInterceptor(webmvc)触发灰度拦截，当前版本：{}", version);
            CanaryFilterContextHolder.getCurrentContext().add(CanaryConstants.HEADER_VERSION, version);
        }
        Enumeration<String> tenants = request.getHeaders(CanaryConstants.TENANT_KEY);
        if (tenants.hasMoreElements()) {
            String tenant = tenants.nextElement();
            log.debug("1. CanaryRequestInterceptor(webmvc)触发灰度拦截，当前租户信息：{}", tenant);
            TenantContextHolder.setTenant(tenant);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
        throws Exception {
        CanaryFilterContextHolder.clearCurrentContext();
        TenantContextHolder.clearCurrentContext();
    }
}
