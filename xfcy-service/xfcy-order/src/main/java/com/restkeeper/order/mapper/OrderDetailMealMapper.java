package com.restkeeper.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.entity.OrderDetailMealEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderDetailMealMapper extends BaseMapper<OrderDetailMealEntity> {

    /**
     * 按订单物理删除套餐拆分明细：结账是重建快照，反结账后再次结账若只做逻辑删除，
     * job 侧历史统计实体不带 @TableLogic 会把已删行再算一遍，报表双倍计数。
     */
    @Delete("DELETE FROM t_order_detail_meal WHERE order_id = #{orderId}")
    int physicalDeleteByOrderId(@Param("orderId") String orderId);
}
