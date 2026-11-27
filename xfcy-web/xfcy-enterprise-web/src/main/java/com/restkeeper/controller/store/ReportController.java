package com.restkeeper.controller.store;

import com.restkeeper.dto.CurrentAmountCollectDTO;
import com.restkeeper.dto.CurrentHourCollectDTO;
import com.restkeeper.dto.DayAmountCollectDTO;
import com.restkeeper.dto.PayTypeCollectDTO;
import com.restkeeper.dto.PrivilegeDTO;
import com.restkeeper.entity.OrderDetailAllView;
import com.restkeeper.entity.ReportDishEntity;
import com.restkeeper.service.IOrderDetailService;
import com.restkeeper.service.IOrderService;
import com.restkeeper.service.IReportDishService;
import com.restkeeper.service.IReportPayService;
import com.restkeeper.vo.store.AmountCollectVO;
import com.restkeeper.vo.store.BarChartCollectVO;
import com.restkeeper.vo.store.PieVo;
import com.restkeeper.vo.store.PrivilegeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 经营报表。端点路径对齐 order 域 provider 契约；日期统一走 query 参数（LocalDate，可选，默认当日）。
 */
@Tag(name = "报表")
@RestController
public class ReportController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM月dd日");

    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;
    private final IReportPayService reportPayService;
    private final IReportDishService reportDishService;

    public ReportController(IOrderService orderService,
                            IOrderDetailService orderDetailService,
                            IReportPayService reportPayService,
                            IReportDishService reportDishService) {
        this.orderService = orderService;
        this.orderDetailService = orderDetailService;
        this.reportPayService = reportPayService;
        this.reportDishService = reportDishService;
    }

    @Operation(summary = "获取当日销量数据")
    @GetMapping("/order/report/currentAmount")
    public AmountCollectVO getAmountCollect(@RequestParam(value = "start", required = false) LocalDate start,
                                            @RequestParam(value = "end", required = false) LocalDate end) {
        LocalDate[] range = todayRange(start, end);
        AmountCollectVO vo = new AmountCollectVO();
        CurrentAmountCollectDTO dto = orderService.getCurrentAmount(range[0], range[1]);
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }

    @Operation(summary = "获取当天24小时销量数据汇总")
    @GetMapping("/order/report/hourCollect")
    public BarChartCollectVO getHourCollect(@RequestParam(value = "type") Integer type,
                                            @RequestParam(value = "start", required = false) LocalDate start,
                                            @RequestParam(value = "end", required = false) LocalDate end) {
        LocalDate[] range = todayRange(start, end);
        List<CurrentHourCollectDTO> dtos = orderService.getCurrentHourCollect(range[0], range[1], type);
        BarChartCollectVO vo = new BarChartCollectVO();
        dtos.forEach(d -> {
            vo.getXAxis().add(d.getCurrentDateHour() + "");
            vo.getSeries().add(d.getTotalAmount());
        });
        return vo;
    }

    @Operation(summary = "获取菜品分类销售金额排行")
    @GetMapping("/order/detail/report/categoryAmount")
    public List<PieVo> getCategoryAmountCollect(@RequestParam(value = "start", required = false) LocalDate start,
                                               @RequestParam(value = "end", required = false) LocalDate end) {
        LocalDate[] range = todayRange(start, end);
        List<OrderDetailAllView> list = orderDetailService.getCurrentCategoryAmountCollect(range[0], range[1]);
        int total = list.stream().mapToInt(OrderDetailAllView::getDishAmount).sum();
        return list.stream().map(d -> {
            PieVo pieVo = new PieVo();
            pieVo.setValue(d.getDishAmount());
            pieVo.setName(d.getDishCategoryName());
            pieVo.setPercent(percent(d.getDishAmount(), total));
            return pieVo;
        }).collect(Collectors.toList());
    }

    @Operation(summary = "获取菜品分类销售数量排行")
    @GetMapping("/order/detail/report/categoryCount")
    public List<PieVo> getCategoryCountCollect(@RequestParam(value = "start", required = false) LocalDate start,
                                              @RequestParam(value = "end", required = false) LocalDate end) {
        LocalDate[] range = todayRange(start, end);
        List<OrderDetailAllView> list = orderDetailService.getCurrentCategoryCountCollect(range[0], range[1]);
        int total = list.stream().mapToInt(OrderDetailAllView::getTotalCount).sum();
        return list.stream().map(d -> {
            PieVo pieVo = new PieVo();
            pieVo.setValue(d.getTotalCount());
            pieVo.setName(d.getDishCategoryName());
            pieVo.setPercent(percent(d.getTotalCount(), total));
            return pieVo;
        }).collect(Collectors.toList());
    }

    @Operation(summary = "获取当日菜品销售排行")
    @GetMapping("/order/detail/report/dishRank")
    public BarChartCollectVO getCurrentDishRank(@RequestParam(value = "start", required = false) LocalDate start,
                                                @RequestParam(value = "end", required = false) LocalDate end) {
        LocalDate[] range = todayRange(start, end);
        BarChartCollectVO result = new BarChartCollectVO();
        orderDetailService.getCurrentDishRank(range[0], range[1]).forEach(d -> {
            result.getXAxis().add(d.getDishName());
            result.getSeries().add(d.getTotalCount());
        });
        return result;
    }

    @Operation(summary = "获取各种支付类型数据汇总")
    @GetMapping("/order/report/payType")
    public List<PieVo> getPayTypeCollect(@RequestParam(value = "start", required = false) LocalDate start,
                                        @RequestParam(value = "end", required = false) LocalDate end) {
        LocalDate[] range = todayRange(start, end);
        List<PayTypeCollectDTO> list = orderService.getPayTypeCollect(range[0], range[1]);
        int total = list.stream().mapToInt(d -> d.getTotalCount() == null ? 0 : d.getTotalCount()).sum();
        return list.stream().map(d -> {
            PieVo pieVo = new PieVo();
            pieVo.setName(d.getPayName());
            pieVo.setValue(d.getTotalCount());
            pieVo.setPercent(percent(d.getTotalCount() == null ? 0 : d.getTotalCount(), total));
            return pieVo;
        }).collect(Collectors.toList());
    }

    @Operation(summary = "获取各种优惠类型数据汇总")
    @GetMapping("/order/report/privilege")
    public PrivilegeVO getPrivilegeCollect(@RequestParam(value = "start", required = false) LocalDate start,
                                           @RequestParam(value = "end", required = false) LocalDate end) {
        LocalDate[] range = todayRange(start, end);
        PrivilegeDTO privilegeDTO = orderService.getPrivilegeCollect(range[0], range[1]);

        double total = privilegeDTO.getPresentAmount() + privilegeDTO.getFreeAmount() + privilegeDTO.getSmallAmount();
        List<PieVo> pieVOList = new ArrayList<>();

        PieVo present = new PieVo();
        present.setName("赠菜");
        present.setValue(privilegeDTO.getPresentAmount());
        present.setPercent(percent(privilegeDTO.getPresentAmount(), total));
        pieVOList.add(present);

        PieVo small = new PieVo();
        small.setName("抹零");
        small.setValue(privilegeDTO.getSmallAmount());
        small.setPercent(percent(privilegeDTO.getSmallAmount(), total));
        pieVOList.add(small);

        PieVo free = new PieVo();
        free.setName("免单");
        free.setValue(privilegeDTO.getFreeAmount());
        free.setPercent(percent(privilegeDTO.getFreeAmount(), total));
        pieVOList.add(free);

        PrivilegeVO privilegeVO = new PrivilegeVO();
        privilegeVO.setDataList(pieVOList);
        privilegeVO.setTotal(privilegeDTO.getPresentAmount() + privilegeDTO.getFreeAmount() + privilegeDTO.getSmallAmount());
        return privilegeVO;
    }

    @Operation(summary = "获取一定日期之内的销售趋势")
    @GetMapping("/report/pay/dayAmount")
    public BarChartCollectVO getDayAmountCollect(@RequestParam("type") int type,
                                                 @RequestParam("start") String start,
                                                 @RequestParam("end") String end) {
        LocalDate startDate = LocalDate.parse(start, DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(end, DATE_FORMATTER);

        List<DayAmountCollectDTO> results = reportPayService.getDayAmountCollect(startDate, endDate);
        BarChartCollectVO vo = new BarChartCollectVO();
        if (type == 1) {
            vo.setXAxis(results.stream().map(r -> r.getDate().format(MONTH_DAY_FORMATTER)).collect(Collectors.toList()));
            vo.setSeries(results.stream().map(DayAmountCollectDTO::getTotalAmount).collect(Collectors.toList()));
        }
        if (type == 2) {
            vo.setXAxis(results.stream().map(r -> r.getDate().getDayOfMonth() + "").collect(Collectors.toList()));
            vo.setSeries(results.stream().map(DayAmountCollectDTO::getTotalCount).collect(Collectors.toList()));
        }
        return vo;
    }

    @Operation(summary = "获取时间范围之内的各种支付类型数据汇总")
    @GetMapping("/report/pay/payType")
    public List<PieVo> getDatePayTypeCollect(@RequestParam("start") String start,
                                             @RequestParam("end") String end) {
        LocalDate startDate = LocalDate.parse(start, DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(end, DATE_FORMATTER);
        List<PayTypeCollectDTO> list = reportPayService.getPayTypeCollect(startDate, endDate);
        int total = list.stream().mapToInt(d -> d.getTotalCount() == null ? 0 : d.getTotalCount()).sum();
        return list.stream().map(d -> {
            PieVo pieVo = new PieVo();
            pieVo.setName(d.getPayName());
            pieVo.setValue(d.getTotalCount());
            pieVo.setPercent(percent(d.getTotalCount() == null ? 0 : d.getTotalCount(), total));
            return pieVo;
        }).collect(Collectors.toList());
    }

    @Operation(summary = "获取时间范围之内的优惠指标汇总数据")
    @GetMapping("/report/pay/privilege")
    public PrivilegeVO getPrivilegeByDate(@RequestParam("start") String start,
                                          @RequestParam("end") String end) {
        LocalDate startDate = LocalDate.parse(start, DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(end, DATE_FORMATTER);
        PrivilegeDTO dto = reportPayService.getPrivilegeCollectByDate(startDate, endDate);

        double total = dto.getPresentAmount() + dto.getFreeAmount() + dto.getSmallAmount();
        List<PieVo> pieVoList = new ArrayList<>();

        PieVo present = new PieVo();
        present.setValue(dto.getPresentAmount());
        present.setName("赠菜");
        present.setPercent(percent(dto.getPresentAmount(), total));
        pieVoList.add(present);

        PieVo free = new PieVo();
        free.setName("免单");
        free.setValue(dto.getFreeAmount());
        free.setPercent(percent(dto.getFreeAmount(), total));
        pieVoList.add(free);

        PieVo small = new PieVo();
        small.setName("抹零");
        small.setValue(dto.getSmallAmount());
        small.setPercent(percent(dto.getSmallAmount(), total));
        pieVoList.add(small);

        PrivilegeVO vo = new PrivilegeVO();
        vo.setDataList(pieVoList);
        vo.setTotal(dto.getPresentAmount() + dto.getFreeAmount() + dto.getSmallAmount());
        return vo;
    }

    @Operation(summary = "获取时间范围之内的菜品类别销售汇总")
    @GetMapping("/report/dish/categoryAmount")
    public List<PieVo> getDateCategoryCollect(@RequestParam("type") int type,
                                              @RequestParam("start") String start,
                                              @RequestParam("end") String end) {
        LocalDate startDate = LocalDate.parse(start, DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(end, DATE_FORMATTER);

        List<ReportDishEntity> results = reportDishService.getCategoryAmountCollect(startDate, endDate);
        if (type == 1) {
            int total = results.stream().mapToInt(d -> d.getDishMoney() == null ? 0 : d.getDishMoney()).sum();
            return results.stream().map(d -> {
                PieVo pieVo = new PieVo();
                pieVo.setValue(d.getDishMoney());
                pieVo.setName(d.getCategory());
                pieVo.setPercent(percent(d.getDishMoney() == null ? 0 : d.getDishMoney(), total));
                return pieVo;
            }).collect(Collectors.toList());
        }
        if (type == 2) {
            int total = results.stream().mapToInt(d -> d.getDishNumber() == null ? 0 : d.getDishNumber()).sum();
            return results.stream().map(d -> {
                PieVo pieVo = new PieVo();
                pieVo.setValue(d.getDishNumber());
                pieVo.setName(d.getCategory());
                pieVo.setPercent(percent(d.getDishNumber() == null ? 0 : d.getDishNumber(), total));
                return pieVo;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Operation(summary = "获取时间范围之内的菜品销售排行")
    @GetMapping("/report/dish/dishRank")
    public BarChartCollectVO getDishRank(@RequestParam("start") String start,
                                         @RequestParam("end") String end) {
        LocalDate startDate = LocalDate.parse(start, DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(end, DATE_FORMATTER);

        BarChartCollectVO result = new BarChartCollectVO();
        reportDishService.getDishRank(startDate, endDate).forEach(d -> {
            result.getXAxis().add(d.getDishName());
            result.getSeries().add(d.getDishNumber());
        });
        return result;
    }

    private LocalDate[] todayRange(LocalDate start, LocalDate end) {
        LocalDate begin = start != null ? start : LocalDate.now();
        LocalDate finish = end != null ? end : begin.plusDays(1);
        return new LocalDate[]{begin, finish};
    }

    private double percent(double amount, double total) {
        if (amount == 0 || total == 0) {
            return 0.0;
        }
        return amount / total * 100;
    }
}
