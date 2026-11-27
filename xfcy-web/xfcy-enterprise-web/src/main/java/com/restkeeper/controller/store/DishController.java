package com.restkeeper.controller.store;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.DishWithFlavors;
import com.restkeeper.store.entity.Dish;
import com.restkeeper.store.entity.DishFlavor;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.vo.store.DishFlavorVO;
import com.restkeeper.vo.store.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 菜品管理。端点路径对齐 provider 契约 {@link IDishService}。
 */
@Slf4j
@Tag(name = "菜品管理")
@RestController
@RequestMapping("/dish")
public class DishController {

    private final IDishService dishService;

    public DishController(IDishService dishService) {
        this.dishService = dishService;
    }

    @Operation(summary = "添加菜品")
    @PostMapping
    public boolean add(@RequestBody DishVO dishVO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishVO, dish);
        return dishService.save(buildRequest(dish, dishVO.getDishFlavors()));
    }

    @Operation(summary = "修改菜品")
    @PutMapping
    public boolean update(@RequestBody DishVO dishVO) {
        if (dishVO.getId() == null || dishVO.getId().isEmpty()) {
            throw new BussinessException("缺少菜品ID");
        }
        Dish dish = dishService.getById(dishVO.getId());
        if (dish == null) {
            throw new BussinessException("菜品不存在");
        }
        BeanUtils.copyProperties(dishVO, dish);
        return dishService.update(buildRequest(dish, dishVO.getDishFlavors()));
    }

    @Operation(summary = "根据id查询菜品相关信息")
    @GetMapping("/{id}")
    public DishVO getDish(@PathVariable("id") String id) {
        Dish dish = dishService.getById(id);
        if (dish == null) {
            throw new BussinessException("菜品不存在");
        }

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        List<DishFlavorVO> dishFlavorVOList = new ArrayList<>();
        List<DishFlavor> dishFlavorList = dish.getFlavorList();
        if (dishFlavorList != null) {
            for (DishFlavor dishFlavor : dishFlavorList) {
                DishFlavorVO dishFlavorVO = new DishFlavorVO();
                dishFlavorVO.setFlavor(dishFlavor.getFlavorName());

                String flavorValue = dishFlavor.getFlavorValue();
                if (flavorValue != null) {
                    String subFlavorValue = flavorValue.substring(flavorValue.indexOf("[") + 1, flavorValue.indexOf("]"));
                    if (StringUtils.hasText(subFlavorValue)) {
                        String[] flavorArray = subFlavorValue.split(",");
                        dishFlavorVO.setFlavorData(Arrays.asList(flavorArray));
                    }
                }
                dishFlavorVOList.add(dishFlavorVO);
            }
        }
        dishVO.setDishFlavors(dishFlavorVOList);
        return dishVO;
    }

    @Operation(summary = "管理端菜品分页")
    @GetMapping("/manage/page/{pageNum}/{pageSize}")
    public PageResult<Dish> queryManagePage(@PathVariable("pageNum") int pageNum,
                                            @PathVariable("pageSize") int pageSize,
                                            @RequestParam(value = "name", required = false) String name,
                                            @RequestParam(value = "categoryId", required = false) String categoryId,
                                            @RequestParam(value = "status", required = false) Integer status) {
        return dishService.queryManagePage(pageNum, pageSize, name, categoryId, status);
    }

    @Operation(summary = "查询可用的菜品列表")
    @GetMapping("/enable/list/{categoryId}")
    public List<Map<String, Object>> findEnableDishList(@PathVariable("categoryId") String categoryId,
                                                        @RequestParam(value = "name", required = false) String name) {
        return dishService.findEnableDishListInfo(categoryId, name);
    }

    @Operation(summary = "停售菜品")
    @PutMapping("/forbidden")
    public boolean forbidden(@RequestBody List<String> ids) {
        return dishService.forbiddenDishes(ids);
    }

    @Operation(summary = "起售菜品")
    @PutMapping("/enable")
    public boolean enable(@RequestBody List<String> ids) {
        return dishService.enableDishes(ids);
    }

    @Operation(summary = "删除菜品")
    @DeleteMapping
    public boolean delete(@RequestBody List<String> ids) {
        return dishService.deleteDishes(ids);
    }

    private DishWithFlavors buildRequest(Dish dish, List<DishFlavorVO> dishFlavorsVO) {
        List<DishFlavor> dishFlavorList = new ArrayList<>();
        if (dishFlavorsVO != null) {
            for (DishFlavorVO dishFlavorVO : dishFlavorsVO) {
                DishFlavor dishFlavor = new DishFlavor();
                dishFlavor.setFlavorName(dishFlavorVO.getFlavor());
                dishFlavor.setFlavorValue(dishFlavorVO.getFlavorData().toString());
                dishFlavorList.add(dishFlavor);
            }
        }

        DishWithFlavors request = new DishWithFlavors();
        request.setDish(dish);
        request.setFlavorList(dishFlavorList);
        return request;
    }
}
