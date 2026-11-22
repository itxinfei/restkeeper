package com.restkeeper.service;

import com.restkeeper.entity.ReverseOrder;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Tag(name = "反结账服务")
@HttpExchange("/order/reverse")
public interface IReverseOrderService {

    //整单反结
    @PostExchange
    boolean reverse(@RequestBody ReverseOrder reverseOrder);
}
