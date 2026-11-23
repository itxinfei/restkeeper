package com.restkeeper.order.controller;

import com.restkeeper.dto.CurrentAmountCollectDTO;
import com.restkeeper.dto.CurrentHourCollectDTO;
import com.restkeeper.dto.CreditPayRequest;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.dto.PayTypeCollectDTO;
import com.restkeeper.dto.PrivilegeDTO;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.http.PageResult;
import com.restkeeper.order.service.OrderServiceImpl;
import com.restkeeper.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单 HTTP 端点：契约映射由 {@link IOrderService} 上的 @HttpExchange 提供
 */
@RestController
public class OrderController implements IOrderService {

    @Autowired
    private OrderServiceImpl orderService;

    @Override
    public String addOrder(OrderEntity orderEntity) {
        return orderService.addOrder(orderEntity);
    }

    @Override
    public boolean returnDish(DetailDTO detailDTO) {
        return orderService.returnDish(detailDTO);
    }

    @Override
    public boolean pay(OrderEntity orderEntity) {
        return orderService.pay(orderEntity);
    }

    @Override
    public boolean payByCredit(CreditPayRequest request) {
        return orderService.payByCredit(request);
    }

    @Override
    public boolean changeTable(String orderId, String targetTableId) {
        return orderService.changeTable(orderId, targetTableId);
    }

    @Override
    public CurrentAmountCollectDTO getCurrentAmount(LocalDate start, LocalDate end) {
        return orderService.getCurrentAmount(start, end);
    }

    @Override
    public List<CurrentHourCollectDTO> getCurrentHourCollect(LocalDate start, LocalDate end, Integer type) {
        return orderService.getCurrentHourCollect(start, end, type);
    }

    @Override
    public List<PayTypeCollectDTO> getPayTypeCollect(LocalDate start, LocalDate end) {
        return orderService.getPayTypeCollect(start, end);
    }

    @Override
    public PrivilegeDTO getPrivilegeCollect(LocalDate start, LocalDate end) {
        return orderService.getPrivilegeCollect(start, end);
    }

    @Override
    public PageResult<OrderEntity> queryPage(int pageNum, int pageSize, LocalDate start, LocalDate end,
                                             Integer payStatus, Integer payType, String orderNumber) {
        return orderService.queryPage(pageNum, pageSize, start, end, payStatus, payType, orderNumber);
    }

    @Override
    public OrderEntity getById(String id) {
        return orderService.getById(id);
    }

    @Override
    public OrderEntity getByOrderNumber(String orderNumber) {
        return orderService.getByOrderNumber(orderNumber);
    }
}
