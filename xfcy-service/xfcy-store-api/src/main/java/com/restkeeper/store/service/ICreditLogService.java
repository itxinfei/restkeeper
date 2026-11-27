package com.restkeeper.store.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.CreditLogs;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "挂账流水服务")
@HttpExchange("/credit/log")
public interface ICreditLogService {

    //挂账流水分页查询
    @GetExchange("/page/{pageNum}/{pageSize}/{creditId}")
    PageResult<CreditLogs> queryPage(@PathVariable("pageNum") int pageNum,
                                     @PathVariable("pageSize") int pageSize,
                                     @PathVariable("creditId") String creditId);

    //按挂账 id 与时间区间查询流水
    @GetExchange("/list")
    List<CreditLogs> list(@RequestParam("creditId") String creditId,
                          @RequestParam(value = "start", required = false)
                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                          @RequestParam(value = "end", required = false)
                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end);

    //保存挂账流水（挂账结账流程内部调用）
    @PostExchange
    boolean save(@RequestBody CreditLogs creditLogs);

    //按订单查询挂账流水（反结账撤销挂账用）
    @GetExchange("/list/byOrder")
    List<CreditLogs> listByOrderId(@RequestParam("orderId") String orderId);

    //按订单删除挂账流水（反结账撤销挂账用，逻辑删）
    @DeleteExchange("/deleteByOrder")
    boolean deleteByOrderId(@RequestParam("orderId") String orderId);
}
