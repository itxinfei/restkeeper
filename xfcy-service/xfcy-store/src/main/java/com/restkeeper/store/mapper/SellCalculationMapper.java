package com.restkeeper.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.store.entity.SellCalculation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SellCalculationMapper extends BaseMapper<SellCalculation> {

    /**
     * 原子扣减剩余沽清数：单条 UPDATE 完成「判足 + 扣减」，避免读-改-写在并发下丢失扣减。
     * 显式带租户条件，不依赖多租户插件对注解 SQL 的注入行为。
     *
     * @return 影响行数，0 表示余量不足或记录不存在
     */
    @Update("UPDATE t_sell_calculation SET remainder = remainder - #{number}, last_update_time = NOW() " +
            "WHERE dish_id = #{dishId} AND shop_id = #{shopId} AND store_id = #{storeId} " +
            "AND remainder >= #{number}")
    int decreaseAtomic(@Param("dishId") String dishId,
                       @Param("number") int number,
                       @Param("shopId") String shopId,
                       @Param("storeId") String storeId);
}
