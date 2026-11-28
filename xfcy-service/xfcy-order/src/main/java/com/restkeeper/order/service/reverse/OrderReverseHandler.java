package com.restkeeper.order.service.reverse;

/**
 * 反结账的按支付方式分派处理器：每种支付方式的订单被反结时，必须提供对应的撤销动作
 * （如挂账要回退主体欠款并作废流水），避免"订单回到未付而外部数据留存"的双标状态。
 * 实现类注册为 Spring Bean 即可自动参与分派。
 */
public interface OrderReverseHandler {

    /**
     * 是否处理该支付方式
     */
    boolean support(Integer payType);

    /**
     * 执行撤销动作（在全局事务内调用）
     */
    void reverse(String orderId);
}
