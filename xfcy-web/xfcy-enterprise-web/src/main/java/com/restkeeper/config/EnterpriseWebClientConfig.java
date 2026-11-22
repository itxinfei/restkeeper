package com.restkeeper.config;

import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.service.IOrderDetailService;
import com.restkeeper.service.IOrderService;
import com.restkeeper.service.IReportDishService;
import com.restkeeper.service.IReportPayService;
import com.restkeeper.shop.service.IBrandService;
import com.restkeeper.shop.service.IStoreManagerService;
import com.restkeeper.shop.service.IStoreService;
import com.restkeeper.store.service.ICreditLogService;
import com.restkeeper.store.service.ICreditRepaymentService;
import com.restkeeper.store.service.ICreditService;
import com.restkeeper.store.service.IDishCategoryService;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.IRemarkService;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.store.service.IStaffService;
import com.restkeeper.store.service.ITableAreaService;
import com.restkeeper.store.service.ITableService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * enterprise_web 的 provider 契约代理声明。
 * <p>服务名与 Nacos 注册名一致：operator/shop/store/order-dubbo-provider。
 */
@Configuration
public class EnterpriseWebClientConfig {

    private static final String OPERATOR = "operator-dubbo-provider";
    private static final String SHOP = "shop-dubbo-provider";
    private static final String STORE = "store-dubbo-provider";
    private static final String ORDER = "order-dubbo-provider";

    @Bean
    IEnterpriseAccountService enterpriseAccountService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, OPERATOR, IEnterpriseAccountService.class);
    }

    @Bean
    IBrandService brandService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, SHOP, IBrandService.class);
    }

    @Bean
    IStoreService storeService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, SHOP, IStoreService.class);
    }

    @Bean
    IStoreManagerService storeManagerService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, SHOP, IStoreManagerService.class);
    }

    @Bean
    ICreditService creditService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ICreditService.class);
    }

    @Bean
    ICreditLogService creditLogService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ICreditLogService.class);
    }

    @Bean
    ICreditRepaymentService creditRepaymentService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ICreditRepaymentService.class);
    }

    @Bean
    IDishCategoryService dishCategoryService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, IDishCategoryService.class);
    }

    @Bean
    IDishService dishService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, IDishService.class);
    }

    @Bean
    IRemarkService remarkService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, IRemarkService.class);
    }

    @Bean
    ISetMealService setMealService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ISetMealService.class);
    }

    @Bean
    IStaffService staffService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, IStaffService.class);
    }

    @Bean
    ITableAreaService tableAreaService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ITableAreaService.class);
    }

    @Bean
    ITableService tableService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ITableService.class);
    }

    @Bean
    IOrderService orderService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ORDER, IOrderService.class);
    }

    @Bean
    IOrderDetailService orderDetailService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ORDER, IOrderDetailService.class);
    }

    @Bean
    IReportDishService reportDishService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ORDER, IReportDishService.class);
    }

    @Bean
    IReportPayService reportPayService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ORDER, IReportPayService.class);
    }
}
