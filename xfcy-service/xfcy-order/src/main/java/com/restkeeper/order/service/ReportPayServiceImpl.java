package com.restkeeper.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.dto.DayAmountCollectDTO;
import com.restkeeper.dto.PayType;
import com.restkeeper.dto.PayTypeCollectDTO;
import com.restkeeper.dto.PrivilegeDTO;
import com.restkeeper.entity.ReportPay;
import com.restkeeper.order.mapper.ReportPayMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 收款报表本地服务
 */
@Service("reportPayService")
public class ReportPayServiceImpl extends ServiceImpl<ReportPayMapper, ReportPay> {

    //销售趋势（应收概况）：无数据日期补零
    public List<DayAmountCollectDTO> getDayAmountCollect(LocalDate start, LocalDate end) {

        QueryWrapper<ReportPay> wrapper = new QueryWrapper<>();
        wrapper.select("sum(pay_amount) as pay_amount","sum(pay_count) as pay_count","pay_date")
                .lambda().ge(ReportPay::getPayDate,start).lt(ReportPay::getPayDate,end)
                .groupBy(ReportPay::getPayDate)
                .orderByDesc(ReportPay::getPayDate);
        List<ReportPay> reportPayList = this.list(wrapper);


        List<DayAmountCollectDTO> fromQuery = reportPayList.stream().map(r -> {
            DayAmountCollectDTO dayAmountCollectDTO = new DayAmountCollectDTO();
            dayAmountCollectDTO.setDate(r.getPayDate());
            dayAmountCollectDTO.setTotalAmount(r.getPayAmount());
            dayAmountCollectDTO.setTotalCount(r.getPayCount());
            return dayAmountCollectDTO;
        }).collect(Collectors.toList());


        List<DayAmountCollectDTO> result = new ArrayList<>();
        result.addAll(fromQuery);

        //得到日期天数差
        long daysCount = end.toEpochDay()-start.toEpochDay();

        //区间内缺失的日期补零
        Stream.iterate(start,i->i.plusDays(1)).limit(daysCount)
                .map(d->{
                    DayAmountCollectDTO dayAmountCollectDTO = new DayAmountCollectDTO();
                    dayAmountCollectDTO.setDate(d);
                    dayAmountCollectDTO.setTotalCount(0);
                    dayAmountCollectDTO.setTotalAmount(0);
                    return dayAmountCollectDTO;
                })
                .forEach(d->{
                    //将原有数据插入到集合中
                    if (!fromQuery.stream().anyMatch(item->item.getDate().isEqual(d.getDate()))){
                        result.add(d);
                    }
                });

        //result排序
        result.sort((a,b)->{
            if (a.getDate().isBefore(b.getDate())){
                return -1;
            }
            if (a.getDate().isAfter(b.getDate())){
                return 1;
            }
            return 0;
        });


        return result;
    }

    //收款构成
    public List<PayTypeCollectDTO> getPayTypeCollect(LocalDate start, LocalDate end) {

        QueryWrapper<ReportPay> wrapper = new QueryWrapper<>();
        wrapper.select("sum(pay_amount) as total_amount","pay_type")
                .lambda().ge(ReportPay::getPayDate,start).lt(ReportPay::getPayDate,end)
                .isNotNull(ReportPay::getPayType)
                .groupBy(ReportPay::getPayType);
        List<ReportPay> reportPayList = this.list(wrapper);


        return reportPayList.stream().map(reportPay -> {
            PayTypeCollectDTO dto = new PayTypeCollectDTO();
            dto.setPayType(reportPay.getPayType());
            dto.setTotalCount(reportPay.getTotalAmount());
            dto.setPayName(PayType.getName(reportPay.getPayType()));
            return dto;
        }).collect(Collectors.toList());
    }

    //优惠统计
    public PrivilegeDTO getPrivilegeCollectByDate(LocalDate start, LocalDate end) {
        QueryWrapper<ReportPay> wrapper = new QueryWrapper<>();
        wrapper.select("present_amount","small_amount","free_amount")
                .lambda().ge(ReportPay::getPayDate,start).lt(ReportPay::getPayDate,end);
        List<ReportPay> reportPayList = this.list(wrapper);

        PrivilegeDTO dto = new PrivilegeDTO();

        int present = 0;
        int small = 0;
        int free = 0;
        for (ReportPay row : reportPayList){
            present += row.getPresentAmount() == null ? 0 : row.getPresentAmount();
            small += row.getSmallAmount() == null ? 0 : row.getSmallAmount();
            free += row.getFreeAmount() == null ? 0 : row.getFreeAmount();
        }
        dto.setPresentAmount(present);
        dto.setSmallAmount(small);
        dto.setFreeAmount(free);

        return dto;
    }
}
