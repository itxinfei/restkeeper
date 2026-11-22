package com.restkeeper.store.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.store.entity.DishFlavor;
import com.restkeeper.store.mapper.DishFlavorMapper;
import org.springframework.stereotype.Service;

/**
 * 菜品口味本地服务：仅 provider 内部被菜品服务调用，无独立 HTTP 端点
 */
@Service("dishFlavorService")
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> {
}
