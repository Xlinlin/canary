package com.xiao.canary.ribbon.interceptor;

import com.xiao.canary.common.context.CanaryConstants;
import com.xiao.canary.common.context.CanaryContext;
import com.xiao.canary.common.context.CanaryFilterContextHolder;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

/**
 * Request template 拦截
 * <p>
 * restTemplate.setInterceptors(ris);
 * </p>
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/8 09:51
 */
@Slf4j
public class CanaryRestInterceptor implements ClientHttpRequestInterceptor {

    /**
     * Intercept the given request, and return a response. The given {@link ClientHttpRequestExecution} allows the interceptor
     * to pass on the request and response to the next entity in the chain.
     * <p>A typical implementation of this method would follow the following pattern:
     * <ol>
     * <li>Examine the {@linkplain HttpRequest request} and body</li>
     * <li>Optionally {@linkplain HttpRequestWrapper
     * wrap} the request to filter HTTP attributes.</li>
     * <li>Optionally modify the body of the request.</li>
     * <li><strong>Either</strong>
     * <ul>
     * <li>execute the request using
     * {@link ClientHttpRequestExecution#execute(HttpRequest, byte[])},</li>
     * <strong>or</strong>
     * <li>do not execute the request to block the execution altogether.</li>
     * </ul>
     * <li>Optionally wrap the response to filter HTTP attributes.</li>
     * </ol>
     *
     * @param request the request, containing method, URI, and headers
     * @param body the body of the request
     * @param execution the request execution
     * @return the response
     * @throws IOException in case of I/O errors
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
        CanaryContext currentContext = CanaryFilterContextHolder.getCurrentContext();
        String version = currentContext.getAttributes().get(CanaryConstants.HEADER_VERSION);
        if (StringUtils.isNotBlank(version)) {
            log.debug("3.CanaryRestInterceptor拦截，设置版本号：{}到RestTemplate中", version);
            requestWrapper.getHeaders().add(CanaryConstants.HEADER_VERSION, version);
        }
        String tenant = currentContext.getAttributes().get(CanaryConstants.TENANT_KEY);
        if (StringUtils.isNotBlank(tenant)) {
            log.debug("3.CanaryRestInterceptor拦截，设置租户：{}到RestTemplate中", tenant);
            requestWrapper.getHeaders().add(CanaryConstants.TENANT_KEY, tenant);
        }
        return execution.execute(requestWrapper, body);
    }
}
