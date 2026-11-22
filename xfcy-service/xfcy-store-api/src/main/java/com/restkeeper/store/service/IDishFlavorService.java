package com.restkeeper.store.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * 菜品口味服务契约（当前无远程端点，保留契约占位；provider 本地 ServiceImpl 继续使用）
 */
@Tag(name = "菜品口味服务")
@HttpExchange("/dish/flavor")
public interface IDishFlavorService {
}
