package com.restkeeper.store.controller;

import com.restkeeper.store.entity.CreditRepayment;
import com.restkeeper.store.service.CreditRepaymentServiceImpl;
import com.restkeeper.store.service.ICreditRepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 挂账还款 HTTP 端点：契约映射由 {@link ICreditRepaymentService} 上的 @HttpExchange 提供
 */
@RestController
public class CreditRepaymentController implements ICreditRepaymentService {

    @Autowired
    private CreditRepaymentServiceImpl creditRepaymentService;

    @Override
    public boolean repayment(CreditRepayment creditRepayment) {
        return creditRepaymentService.repayment(creditRepayment);
    }
}
