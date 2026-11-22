package com.restkeeper.shop.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.restkeeper.tenant.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商户（shop）侧多租户配置：所有业务表自动追加 shop_id 条件。
 * 租户来源从 Dubbo 时代的 RpcContext 附件改为 HTTP 请求头经
 * {@link com.restkeeper.http.TenantContextHeaderFilter} 还原后的 TenantContext。
 */
@Configuration
public class MybatisPlusTenantConfig {

    /**
     * 多租户标识字段
     */
    private static final String SYSTEM_TENANT_ID = "shop_id";

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //多租户 inner 拦截器必须排在分页之前
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {

            @Override
            public Expression getTenantId() {
                String shopId = TenantContext.getShopId();
                //TenantContext 取不到值时返回字符串 "null"
                if (shopId == null || shopId.isEmpty() || "null".equals(shopId)) {
                    throw new IllegalStateException("租户上下文缺失：shopId");
                }
                return new StringValue(shopId);
            }

            @Override
            public String getTenantIdColumn() {
                return SYSTEM_TENANT_ID;
            }

            @Override
            public boolean ignoreTable(String tableName) {
                //shop 库品牌/门店/门店管理员表全部带 shop_id，无忽略表
                return false;
            }
        }));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
