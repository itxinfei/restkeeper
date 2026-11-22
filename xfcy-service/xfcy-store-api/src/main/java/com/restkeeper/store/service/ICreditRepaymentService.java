package com.restkeeper.store.service;

import com.restkeeper.store.entity.CreditRepayment;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Tag(name = "挂账还款服务")
@HttpExchange("/credit/repayment")
public interface ICreditRepaymentService {

    //挂账还款
    @PostExchange
    boolean repayment(@RequestBody CreditRepayment creditRepayment);
}
