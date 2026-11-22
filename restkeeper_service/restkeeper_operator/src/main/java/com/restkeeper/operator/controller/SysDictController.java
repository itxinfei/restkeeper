package com.restkeeper.operator.controller;

import com.restkeeper.operator.entity.SysDictionary;
import com.restkeeper.operator.service.ISysDictService;
import com.restkeeper.operator.service.SysDictServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统字典 HTTP 端点：契约映射由 {@link ISysDictService} 上的 @HttpExchange 提供
 */
@RestController
public class SysDictController implements ISysDictService {

    @Autowired
    private SysDictServiceImpl sysDictService;

    @Override
    public List<SysDictionary> getDictionaryList(String category) {
        return sysDictService.getDictionaryList(category);
    }
}
