package com.xiao.canary.gateway.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import com.xiao.canary.gateway.balancer.CanaryLoadBalancer;
import com.xiao.canary.gateway.tenant.TenantService;
import com.xiao.canary.common.context.CanaryConstants;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * gateway全局fitler <br>
 * <p>
 * 1. 参考：{@code org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter}
 * <p>
 * 2.注意ordered的实现，返回的值应该需要跟ReactiveLoadBalancerClientFilter.getOrder一样或者在前面
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/5 15:16
 */
public class CanaryLoadBalancerFilter implements GlobalFilter, Ordered {

    private static final Log log = LogFactory.getLog(CanaryLoadBalancerFilter.class);

    private static final int LOAD_BALANCER_CLIENT_FILTER_ORDER = 10149;

    private final LoadBalancerClientFactory clientFactory;
    private final LoadBalancerProperties balancerProperties;
    private final TenantService tenantService;
    private final Environment environment;

    /**
     * 构造器
     *
     * @param clientFactory :
     * @param properties :
     * @param environment :
     * @param tenantService :
     * @return
     * @author xiaolinlin
     * @date 14:09 2020/9/22
     **/
    public CanaryLoadBalancerFilter(LoadBalancerClientFactory clientFactory, LoadBalancerProperties properties,
        Environment environment,
        TenantService tenantService) {
        this.clientFactory = clientFactory;
        this.balancerProperties = properties;
        this.tenantService = tenantService;
        this.environment = environment;
    }

    /**
     * Process the Web request and (optionally) delegate to the next {@code WebFilter} through the given {@link
     * GatewayFilterChain}.
     *
     * @param exchange the current server exchange
     * @param chain provides a way to delegate to the next filter
     * @return {@code Mono<Void>} to indicate when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 处理消息头信息
        setHeaderInfo(exchange);

        URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);

        // 默认都走负载
        // if (url == null
        //     || (!"lb".equals(url.getScheme()) && !"lb".equals(schemePrefix))) {
        //     // 继续走下一个filter，并附带版本号
        //     return chain.filter(exchange);
        // }

        // preserve the original url
        addOriginalRequestUrl(exchange, url);

        if (log.isTraceEnabled()) {
            log.trace(CanaryLoadBalancerFilter.class.getSimpleName()
                + " url before: " + url);
        }

        return choose(exchange).doOnNext(response -> {

            if (!response.hasServer()) {
                throw NotFoundException.create(balancerProperties.isUse404(),
                    "Unable to find instance for " + url.getHost());
            }

            URI uri = exchange.getRequest().getURI();

            // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the default,
            // if the loadbalancer doesn't provide one.
            String overrideScheme = null;
            if (schemePrefix != null) {
                overrideScheme = url.getScheme();
            }

            DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(
                response.getServer(), overrideScheme);

            URI requestUrl = reconstructURI(serviceInstance, uri);

            if (log.isTraceEnabled()) {
                log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);
            }
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
        }).then(chain.filter(exchange));
    }


    /**
     * 设置消息头信息
     *
     * @param exchange :
     * @author xiaolinlin
     * @date 15:34 2020/9/21
     **/
    private void setHeaderInfo(ServerWebExchange exchange) {

        String version = "";
        // 获取租户
        String tenant = exchange.getRequest().getHeaders().getFirst(CanaryConstants.TENANT_KEY);
        if (StringUtils.isNotBlank(tenant) && null != tenantService) {
            // 通过租户信息获取版本号
            version = tenantService.getVersion(tenant);
        }

        // 如果版本号为空，到header 头去找找
        if (StringUtils.isBlank(version)) {
            version = exchange.getRequest().getHeaders().getFirst(CanaryConstants.HEADER_VERSION);
        }
        exchange.getRequest().mutate().header(CanaryConstants.HEADER_VERSION, version);
        exchange.getRequest().mutate().header(CanaryConstants.TENANT_KEY, tenant);

    }

    protected URI reconstructURI(ServiceInstance serviceInstance, URI original) {
        return LoadBalancerUriTools.reconstructURI(serviceInstance, original);
    }

    private Mono<Response<ServiceInstance>> choose(ServerWebExchange exchange) {
        URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        CanaryLoadBalancer loadBalancer = new CanaryLoadBalancer(
            clientFactory.getLazyProvider(uri.getHost(), ServiceInstanceListSupplier.class), uri.getHost());
        if (loadBalancer == null) {
            throw new NotFoundException("No loadbalancer available for " + uri.getHost());
        } else {
            return loadBalancer.choose(this.createRequest(exchange));
        }
    }

    private Request createRequest(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        Request<HttpHeaders> request = new DefaultRequest<>(headers);
        return request;
    }

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat analogous to Servlet {@code load-on-startup}
     * values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        // 请注意这个ORDER 一定和内置的LoadBalancerFilter顺序保持一直
        return LOAD_BALANCER_CLIENT_FILTER_ORDER;
    }
}
