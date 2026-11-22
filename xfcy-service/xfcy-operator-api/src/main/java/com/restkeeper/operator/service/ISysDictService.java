package com.restkeeper.operator.service;

import com.restkeeper.operator.entity.SysDictionary;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@Tag(name = "系统字典服务")
@HttpExchange("/sysDict")
public interface ISysDictService {

    //按分类查询字典项
    @GetExchange("/list/{category}")
    List<SysDictionary> getDictionaryList(@PathVariable("category") String category);
}
