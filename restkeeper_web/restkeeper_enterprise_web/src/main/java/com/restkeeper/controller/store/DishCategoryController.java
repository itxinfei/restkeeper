package com.restkeeper.controller.store;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.DishCategory;
import com.restkeeper.store.service.IDishCategoryService;
import com.restkeeper.vo.store.AddDishCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 分类管理。端点路径对齐 provider 契约 {@link IDishCategoryService}。
 */
@Slf4j
@Tag(name = "分类管理")
@RestController
@RequestMapping("/dish/category")
public class DishCategoryController {

    private final IDishCategoryService dishCategoryService;

    public DishCategoryController(IDishCategoryService dishCategoryService) {
        this.dishCategoryService = dishCategoryService;
    }

    @Operation(summary = "添加分类")
    @PostMapping("/add")
    public boolean add(@RequestBody AddDishCategoryVO addDishCategoryVO) {
        return dishCategoryService.add(addDishCategoryVO.getCategoryName(), addDishCategoryVO.getType());
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public boolean update(@PathVariable("id") String id,
                          @RequestParam("name") String name) {
        return dishCategoryService.update(id, name);
    }

    @Operation(summary = "分页列表")
    @GetMapping("/page/{pageNum}/{pageSize}")
    public PageResult<DishCategory> findByPage(@PathVariable("pageNum") Integer pageNum,
                                               @PathVariable("pageSize") Integer pageSize) {
        return dishCategoryService.queryPage(pageNum, pageSize);
    }

    @Operation(summary = "按类型查询分类列表（1 菜品 2 套餐）")
    @GetMapping("/type/{type}")
    public List<Map<String, Object>> getByType(@PathVariable("type") Integer type) {
        return dishCategoryService.findCategoryList(type);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") String id) {
        return dishCategoryService.delete(id);
    }
}
