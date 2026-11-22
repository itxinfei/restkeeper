package com.restkeeper.dto;

import com.restkeeper.entity.OrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 挂账结账请求体：订单主体 + 挂账信息（跨 order/store 两库，Seata 全局事务）
 */
@Data
@Schema(description = "挂账结账提交体")
public class CreditPayRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单主体")
    private OrderEntity order;

    @Schema(description = "挂账信息")
    private CreditDTO credit;
}
