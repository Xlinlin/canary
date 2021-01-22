package com.xiao.canary.gateway.tenant;

/**
 * @ClassName: TenantService
 * @Description: 租户服务
 * @author: xiaolinlin
 * @date: 2020/9/21 9:05
 **/
public interface TenantService {

    /**
     * 获取租户当前的版本号
     * @param tenant
     * @return
     */
    String getVersion(String tenant);
}
