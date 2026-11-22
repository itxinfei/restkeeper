package com.restkeeper.order.controller;

import com.restkeeper.dto.DayAmountCollectDTO;
import com.restkeeper.dto.PayTypeCollectDTO;
import com.restkeeper.dto.PrivilegeDTO;
import com.restkeeper.order.service.ReportPayServiceImpl;
import com.restkeeper.service.IReportPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 收款报表 HTTP 端点：契约映射由 {@link IReportPayService} 上的 @HttpExchange 提供
 */
@RestController
public class ReportPayController implements IReportPayService {

    @Autowired
    private ReportPayServiceImpl reportPayService;

    @Override
    public List<DayAmountCollectDTO> getDayAmountCollect(LocalDate start, LocalDate end) {
        return reportPayService.getDayAmountCollect(start, end);
    }

    @Override
    public List<PayTypeCollectDTO> getPayTypeCollect(LocalDate start, LocalDate end) {
        return reportPayService.getPayTypeCollect(start, end);
    }

    @Override
    public PrivilegeDTO getPrivilegeCollectByDate(LocalDate start, LocalDate end) {
        return reportPayService.getPrivilegeCollectByDate(start, end);
    }
}
