package com.xiao.canary.common.context;

/**
 * 元数据常量
 *
 * @author xiaolinlin
 * @version 1.0, 2020/5/8 09:03
 */
public interface CanaryConstants {

    /**
     * 头部灰度版本号key，统一入口由网关+机构/租户中心配置
     */
    String HEADER_VERSION = "x3_version";

    /**
     * 服务版本号KEY
     */
    String SERVICE_VERSION = "version";

    /**
     * 权重key
     */
    String SERVER_WEIGHT = "weight";

    /**
     * 租户 header头
     */
    String TENANT_KEY = "tenant";
}
