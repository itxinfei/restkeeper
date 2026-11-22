package com.restkeeper.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * 订单口味明细服务契约（当前无远程端点，保留契约占位；provider 本地 ServiceImpl 继续使用）
 */
@Tag(name = "订单口味明细服务")
@HttpExchange("/order/detailMeal")
public interface IOrderDetailMealService {
}
