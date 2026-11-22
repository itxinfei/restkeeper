package com.restkeeper.order.config;

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
 * 订单侧双租户配置：业务表 SQL 自动追加 shop_id 与 store_id 条件。
 * 租户来源为 HTTP 请求头经 TenantContextHeaderFilter 还原后的 TenantContext。
 * Seata 2.5 starter 自动代理数据源，不再手写 DataSourceProxy 与 SqlSessionFactory。
 */
@Configuration
public class MybatisPlusTenantConfig {

    private static final String TENANT_COLUMN_SHOP_ID = "shop_id";
    private static final String TENANT_COLUMN_STORE_ID = "store_id";

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //多租户拦截器按顺序追加条件，分页拦截器必须放最后
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(shopTenantHandler()));
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(storeTenantHandler()));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    private TenantLineHandler shopTenantHandler() {
        return new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String shopId = TenantContext.getShopId();
                if (shopId == null || shopId.isEmpty() || "null".equals(shopId)) {
                    throw new IllegalStateException("租户上下文缺失：shopId");
                }
                return new StringValue(shopId);
            }

            @Override
            public String getTenantIdColumn() {
                return TENANT_COLUMN_SHOP_ID;
            }
        };
    }

    private TenantLineHandler storeTenantHandler() {
        return new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String storeId = TenantContext.getStoreId();
                if (storeId == null || storeId.isEmpty() || "null".equals(storeId)) {
                    throw new IllegalStateException("租户上下文缺失：storeId");
                }
                return new StringValue(storeId);
            }

            @Override
            public String getTenantIdColumn() {
                return TENANT_COLUMN_STORE_ID;
            }
        };
    }
}
