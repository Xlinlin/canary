package com.xiaogj.x3.canary.ribbon.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.ExtendBalancer;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerStats;
import com.xiaogj.x3.canary.common.context.CanaryConstants;
import com.xiaogj.x3.canary.common.context.CanaryFilterContextHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

/**
 * <@link https://blog.51cto.com/zero01/2424180></@link> 支持Nacos权重配置的负载均衡策略
 * <p>
 * 配置文件使用：
 * <pre>
 * {@code
 * user-center:
 *   ribbon:
 *     NFLoadBalancerRuleClassName: com.xiao.springcloud.loadbalancer.ribbon.NacosWeightedRule
 *     }
 * </pre>​
 * 基于版本号&权重
 * <pre>
 *  {@code
 *  spring:
 *   cloud:
 *     nacos:
 *       discovery:
 *         # 指定nacos server的地址
 *         server-addr: 127.0.0.1:8848
 *         # 配置元数据
 *         metadata:
 *           # 当前实例版本
 *           version: v1
 *  }
 * </pre>
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/7 19:30
 */
@Slf4j
public class CanaryNacosRibbonRule extends AbstractLoadBalancerRule {

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    /**
     * 负载状态
     */
    private LoadBalancerStats loadBalancerStats;

    /**
     * nacos同集群内优先选择
     */
    @Value("${ribbon.nacos.priority.cluster.same:false}")
    private boolean nacosClusterSamePriority;

    /**
     * 基于nacos版本号做区分
     */
    @Value("${ribbon.nacos.priority.version:true}")
    private boolean nacosVersionPriority;

    /**
     * Concrete implementation should implement this method so that the configuration set via {@link IClientConfig} (which in
     * turn were set via Archaius properties) will be taken into consideration
     *
     * @param clientConfig
     */
    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {

    }

    @Override
    public Server choose(Object key) {

        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
        if (null == loadBalancerStats) {
            loadBalancerStats = loadBalancer.getLoadBalancerStats();
        }

        // 需要请求的微服务名称
        String serviceId = loadBalancer.getName();
        // 获取服务发现的相关API
        NamingService namingService = discoveryProperties.namingServiceInstance();

        // 基于nacos 版本号做区分请求
        if (nacosVersionPriority) {
            return getVersionServer(serviceId, namingService);
        } else {
            return defaultChoose(serviceId, namingService);
        }
    }

    /**
     * 默认选择
     *
     * @param serviceId
     * @param namingService
     * @return
     */
    private Server defaultChoose(String serviceId, NamingService namingService) {
        try {
            Instance instance = null;
            if (nacosClusterSamePriority) {
                instance = nacosClusterWeight(serviceId, namingService);
            } else {
                // 普通基于权重轮询
                // 调用该方法时nacos client会自动通过基于权重的负载均衡算法选取一个实例，由底层nacos client提供实现
                instance = namingService.selectOneHealthyInstance(serviceId);
            }
            log.debug("选择的实例是：instance = {}", instance);

            return new NacosServer(instance);
        } catch (NacosException e) {
            return null;
        }
    }

    private Server getVersionServer(String serviceId, NamingService namingService) {
        // 获取该微服务的所有健康实例
        List<Instance> instances = getInstances(serviceId, namingService);
        // 如果没有权重信息需要从元数据获取权重值
        // instances.forEach(
        //     instance -> instance.setWeight(Integer.parseInt(instance.getMetadata().get(MetadataConstants.SERVER_WEIGHT))));
        List<Instance> metadataMatchInstances = instances;

        String currentVersion = CanaryFilterContextHolder.getCurrentContext().get(CanaryConstants.HEADER_VERSION);
        log.debug("从请求上下文中获取到的版本version：{}", currentVersion);
        // 版本号不为空则表示带版本请求
        if (StringUtils.isNotEmpty(currentVersion)) {
            // 过滤与版本元数据相匹配的实例，以实现版本控制
            metadataMatchInstances = filter(instances,
                i -> Objects.equals(currentVersion, i.getMetadata().get(CanaryConstants.HEADER_VERSION)));
            if (CollectionUtils.isEmpty(metadataMatchInstances)) {
                log.warn("未找到元数据匹配的目标实例！请检查配置。targetVersion = {}, instance = {}",
                    currentVersion, instances);
                return null;
            }
        }

        List<Instance> clusterMetadataMatchInstances = metadataMatchInstances;
        // 获取配置文件中所配置的集群名称
        String clusterName = discoveryProperties.getClusterName();
        // 如果配置了集群名称，需筛选同集群下元数据匹配的实例
        if (StringUtils.isNotBlank(clusterName)) {
            // 过滤出相同集群下的所有实例
            clusterMetadataMatchInstances = filter(metadataMatchInstances,
                i -> Objects.equals(clusterName, i.getClusterName()));

            if (CollectionUtils.isEmpty(clusterMetadataMatchInstances)) {
                clusterMetadataMatchInstances = metadataMatchInstances;
                log.warn("发生跨集群调用。clusterName = {}, targetVersion = {}, clusterMetadataMatchInstances = {}", clusterName,
                    currentVersion, clusterMetadataMatchInstances);
            }
        }

        NacosServer nacosServer = bestAvailable(clusterMetadataMatchInstances);
        if (null == nacosServer) {
            // 基于随机权重的负载均衡算法，从实例列表中选取一个实例
            Instance instance = ExtendBalancer.getHostByRandomWeight2(clusterMetadataMatchInstances);
            nacosServer = new NacosServer(instance);
        }
        return nacosServer;
    }

    /**
     * 先过滤出故障服务器，再选择一个当前并发请求数最小的服务
     * <p>
     * 参考：com.netflix.loadbalancer.BestAvailableRule
     *
     * @param clusterMetadataMatchInstances :
     * @return com.alibaba.cloud.nacos.ribbon.NacosServer
     * @author xiaolinlin
     * @date 11:48 2020/9/18
     **/
    private NacosServer bestAvailable(List<Instance> clusterMetadataMatchInstances) {
        log.debug("基于先过滤出故障服务器，再选择一个当前并发请求数最小的服务");
        List<NacosServer> nacosServersList = new ArrayList<>(clusterMetadataMatchInstances.size());
        clusterMetadataMatchInstances.forEach(instance -> new NacosServer(instance));
        int minimalConcurrentConnections = Integer.MAX_VALUE;
        long currentTime = System.currentTimeMillis();
        for (NacosServer server : nacosServersList) {
            ServerStats serverStats = loadBalancerStats.getSingleServerStat(server);
            if (!serverStats.isCircuitBreakerTripped(currentTime)) {
                int concurrentConnections = serverStats.getActiveRequestsCount(currentTime);
                if (concurrentConnections < minimalConcurrentConnections) {
                    minimalConcurrentConnections = concurrentConnections;
                    return server;
                }
            }
        }
        return null;
    }

    /**
     * 通过过滤规则过滤实例列表
     */
    private List<Instance> filter(List<Instance> instances, Predicate<Instance> predicate) {
        return instances.stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    private List<Instance> getInstances(String serviceId, NamingService namingService) {
        try {
            // 获取该微服务的所有健康实例
            return namingService.selectInstances(serviceId, true);
        } catch (NacosException e) {
            log.error("发生异常", e);
            return Collections.emptyList();
        }
    }


    /**
     * 同一集权内优先选择实例
     *
     * @param serviceId :
     * @param namingService :
     * @return com.alibaba.nacos.api.naming.pojo.Instance
     * @author xiaolinlin  2020/5/7 - 19:52
     **/
    private Instance nacosClusterWeight(String serviceId, NamingService namingService) throws NacosException {
        // 获取该微服务的所有健康实例
        List<Instance> instances = namingService.selectInstances(serviceId, true);

        // 如果没有权重信息需要从元数据获取权重值
        // instances.forEach(instance -> instance.setWeight(Integer.parseInt(instance.getMetadata().get("weight"))));

        // 获取配置文件中所配置的集群名称
        String clusterName = discoveryProperties.getClusterName();
        // 相同集群下没有实例则需要使用其他集群下的实例
        List<Instance> instancesToBeChosen = new ArrayList<>();
        if (StringUtils.isNotEmpty(clusterName)) {
            // 过滤出相同集群下的所有实例
            List<Instance> sameClusterInstances = instances.stream()
                .filter(i -> Objects.equals(i.getClusterName(), clusterName))
                .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(sameClusterInstances)) {
                instancesToBeChosen = sameClusterInstances;

            }
        }
        if (CollectionUtils.isEmpty(instancesToBeChosen)) {
            instancesToBeChosen = instances;
            log.warn("发生跨集群调用，name = {}, clusterName = {}, instances = {}",
                serviceId, clusterName, instances);
        }

        // 基于随机权重的负载均衡算法，从实例列表中选取一个实例
        Instance instance = ExtendBalancer.getHostByRandomWeight2(instancesToBeChosen);
        log.debug("选择的实例是：port = {}, instance = {}", instance.getPort(), instance);
        return instance;
    }

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        super.setLoadBalancer(lb);
        if (lb instanceof AbstractLoadBalancer) {
            loadBalancerStats = ((AbstractLoadBalancer) lb).getLoadBalancerStats();
        }
    }
}
