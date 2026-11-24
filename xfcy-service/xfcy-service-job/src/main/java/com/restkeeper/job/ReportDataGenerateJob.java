package com.restkeeper.job;

import com.restkeeper.service.OrderHistoryService;
import com.restkeeper.service.ReportDishService;
import com.restkeeper.service.ReportPayService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ReportDataGenerateJob {

    @Autowired
    private ReportPayService reportPayService;

    @Autowired
    private ReportDishService reportDishService;

    @Autowired
    private OrderHistoryService orderHistoryService;

    @XxlJob("generateReportHandler")//必须的，注解参数需要和在xxl-job-admin里创建的jobHandler的名称一致
    public void jobHandler(String param){
        try {
            //统计历史的营收概况
            reportPayService.generateData();
            log.info("{} **************历史汇总成功**************", LocalDateTime.now());

            //统计历史菜品销量
            reportDishService.generateData();
            log.info("{} **************历史菜品销量汇总成功**************", LocalDateTime.now());

            //订单数据迁移
            orderHistoryService.exportToHistory();
            log.info("{} **************job执行成功**************", LocalDateTime.now());

            XxlJobHelper.handleSuccess();
        } catch (Exception e) {
            log.error("报表汇总任务执行失败", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }
}
