package com.xiaogj.canary.demo.gateway.tenant;

import com.xiaogj.canary.gateway.tenant.TenantService;
import org.springframework.stereotype.Service;

/**
 * @ClassName: TenantServiceImpl
 * @Description: 租户实现类
 * @author: xiaolinlin
 * @date: 2020/9/21 10:39
 **/
@Service
public class TenantServiceImpl implements TenantService {

    /**
     * 获取租户当前的版本号
     *
     * @param tenant
     * @return
     */
    @Override
    public String getVersion(String tenant) {
        if ("w1".equals(tenant)) {
            return "v1";
        }
        if ("x3".equals(tenant)) {
            return "v3";
        }
        return "";
    }
}
