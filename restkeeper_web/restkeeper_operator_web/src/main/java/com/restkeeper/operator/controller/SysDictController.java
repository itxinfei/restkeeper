package com.restkeeper.operator.controller;

import com.restkeeper.operator.entity.SysDictionary;
import com.restkeeper.operator.service.ISysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统字典 BFF 转发（旧 web 模块缺失，见 requirements.md 10.4-6）。
 */
@Tag(name = "系统字典")
@RestController
@RequestMapping("/sysDict")
public class SysDictController {

    private final ISysDictService sysDictService;

    public SysDictController(ISysDictService sysDictService) {
        this.sysDictService = sysDictService;
    }

    @Operation(summary = "按分类查询字典项")
    @GetMapping("/list/{category}")
    public List<SysDictionary> getDictionaryList(@PathVariable("category") String category) {
        return sysDictService.getDictionaryList(category);
    }
}
