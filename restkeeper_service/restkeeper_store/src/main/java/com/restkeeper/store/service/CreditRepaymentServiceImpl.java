package com.restkeeper.store.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditRepayment;
import com.restkeeper.store.mapper.CreditRepaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 挂账还款本地服务
 */
@Service("creditRepaymentService")
public class CreditRepaymentServiceImpl extends ServiceImpl<CreditRepaymentMapper, CreditRepayment> {

    @Autowired
    private CreditServiceImpl creditService;

    /**
     * 还款：校验还款额不超额，落还款流水并回写挂账额度
     */
    @Transactional
    public boolean repayment(CreditRepayment creditRepayment) {
        Integer repaymentAmount = creditRepayment.getRepaymentAmount();
        Credit credit = creditService.getById(creditRepayment.getCreditId());

        if (credit.getCreditAmount() - repaymentAmount < 0) {
            throw new BussinessException("还款金额超过欠款金额");
        }

        this.save(creditRepayment);

        credit.setCreditAmount(credit.getCreditAmount() - repaymentAmount);
        credit.setTotalRepaymentAmount(credit.getTotalRepaymentAmount() + repaymentAmount);
        return creditService.saveOrUpdate(credit);
    }
}
