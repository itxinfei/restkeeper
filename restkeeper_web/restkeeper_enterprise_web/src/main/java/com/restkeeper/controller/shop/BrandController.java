package com.restkeeper.controller.shop;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.service.IBrandService;
import com.restkeeper.vo.shop.AddTBrandVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 品牌管理。端点路径对齐 provider 契约 {@link IBrandService}。
 */
@Slf4j
@Tag(name = "品牌管理")
@RestController
@RequestMapping("/brand")
public class BrandController {

    private final IBrandService brandService;

    public BrandController(IBrandService brandService) {
        this.brandService = brandService;
    }

    @Operation(summary = "分页查询所有品牌信息")
    @GetMapping("/page/{pageNo}/{pageSize}")
    public PageResult<Brand> pageList(@PathVariable("pageNo") int pageNo,
                                      @PathVariable("pageSize") int pageSize) {
        return brandService.queryPage(pageNo, pageSize);
    }

    @Operation(summary = "新增品牌")
    @PostMapping
    public boolean add(@RequestBody AddTBrandVO addTBrandVO) {
        Brand brand = new Brand();
        BeanUtils.copyProperties(addTBrandVO, brand);
        return brandService.save(brand);
    }

    @Operation(summary = "品牌列表（下拉选择使用）")
    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        return brandService.getBrandList();
    }
}
