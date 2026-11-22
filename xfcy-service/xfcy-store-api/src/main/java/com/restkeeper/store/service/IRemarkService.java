package com.restkeeper.store.service;

import com.restkeeper.store.entity.Remark;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@Tag(name = "备注设置服务")
@HttpExchange("/remark")
public interface IRemarkService {

    //查询备注列表
    @GetExchange("/list")
    List<Remark> getRemarks();

    //修改备注信息
    @PutExchange("/update")
    boolean updateRemarks(@RequestBody List<Remark> remarks);
}
