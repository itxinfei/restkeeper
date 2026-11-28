package com.restkeeper.order.service.reverse;

import com.restkeeper.constants.OrderPayType;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditLogs;
import com.restkeeper.store.service.ICreditLogService;
import com.restkeeper.store.service.ICreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 挂账支付的撤销：回退挂账主体剩余欠款并删除挂账流水。
 * 回退金额不超过主体当前欠款，避免历史已还款数据被反向撑大。
 */
@Slf4j
@Component
public class CreditReverseHandler implements OrderReverseHandler {

    @Autowired
    private ICreditLogService creditLogService;

    @Autowired
    private ICreditService creditService;

    @Override
    public boolean support(Integer payType) {
        return payType != null && payType == OrderPayType.CREDIT.getType();
    }

    @Override
    public void reverse(String orderId) {
        List<CreditLogs> creditLogsList = creditLogService.listByOrderId(orderId);
        if (creditLogsList == null || creditLogsList.isEmpty()) {
            return;
        }
        for (CreditLogs creditLogs : creditLogsList) {
            Integer amount = creditLogs.getCreditAmount();
            if (amount == null || amount <= 0) {
                continue;
            }
            Credit credit = creditService.getById(creditLogs.getCreditId());
            if (credit == null || credit.getCreditAmount() == null) {
                log.warn("挂账撤销跳过：挂账主体不存在或欠款为空, orderId={}, creditId={}", orderId, creditLogs.getCreditId());
                continue;
            }
            if (credit.getCreditAmount() >= amount) {
                credit.setCreditAmount(credit.getCreditAmount() - amount);
                creditService.saveOrUpdate(credit);
            } else {
                //已还过款的主体不会被反向撑大，留流水交人工核对
                log.warn("挂账撤销跳过：当前欠款不足回退额, orderId={}, creditId={}, creditAmount={}, amount={}",
                        orderId, creditLogs.getCreditId(), credit.getCreditAmount(), amount);
            }
        }
        creditLogService.deleteByOrderId(orderId);
    }
}
