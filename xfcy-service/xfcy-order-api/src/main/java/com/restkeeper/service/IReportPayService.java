package com.restkeeper.service;

import com.restkeeper.dto.DayAmountCollectDTO;
import com.restkeeper.dto.PayTypeCollectDTO;
import com.restkeeper.dto.PrivilegeDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "收款报表服务")
@HttpExchange("/report/pay")
public interface IReportPayService {

    //获取一段日期内的销售趋势（应收概况）
    @GetExchange("/dayAmount")
    List<DayAmountCollectDTO> getDayAmountCollect(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    //获取一定日期内的收款构成
    @GetExchange("/payType")
    List<PayTypeCollectDTO> getPayTypeCollect(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);

    //获取一定日期内的优惠统计
    @GetExchange("/privilege")
    PrivilegeDTO getPrivilegeCollectByDate(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);
}
