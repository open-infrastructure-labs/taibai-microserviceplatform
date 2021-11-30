
package com.taibai.common.data.tenant;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

/**
 * @author Taibai
 * @date 2018-12-26
 *       <p>
 *       租户维护处理器
 */
@Slf4j
public class MyTenantHandler implements com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler {
    @Autowired
    private TenantConfigProperties properties;

    /**
     * 获取租户值
     *
     * @return 租户值
     */
    @Override
    public Expression getTenantId(boolean select) {
        Integer tenantId = TenantContextHolder.getTenantId();
        log.debug("当前租户为 >> {}", tenantId);
        return new LongValue(tenantId);
    }

    /**
     * 获取租户字段名
     *
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return properties.getColumn();
    }

    /**
     * 根据表名判断是否进行过滤
     *
     * @param tableName 表名
     * @return 是否进行过滤
     */
    @Override
    public boolean doTableFilter(String tableName) {
        return !properties.getTables().contains(tableName);
    }
}
