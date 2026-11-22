package com.restkeeper.controller.store;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.SetMealWithDishes;
import com.restkeeper.store.entity.SetMeal;
import com.restkeeper.store.entity.SetMealDish;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.vo.store.SetMealDishVO;
import com.restkeeper.vo.store.SetMealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
import java.util.List;

/**
 * 套餐管理。端点路径对齐 provider 契约 {@link ISetMealService}。
 */
@Slf4j
@Tag(name = "套餐管理")
@RestController
@RequestMapping("/setMeal")
public class SetMealController {

    private final ISetMealService setMealService;

    public SetMealController(ISetMealService setMealService) {
        this.setMealService = setMealService;
    }

    @Operation(summary = "套餐分页查询")
    @GetMapping("/page/{pageNum}/{pageSize}")
    public PageResult<SetMeal> queryPage(@PathVariable("pageNum") Integer pageNum,
                                         @PathVariable("pageSize") Integer pageSize,
                                         @RequestParam(value = "name", required = false) String name) {
        return setMealService.queryPage(pageNum, pageSize, name);
    }

    @Operation(summary = "添加套餐")
    @PostMapping
    public boolean add(@RequestBody SetMealVO setMealVO) {
        SetMeal setMeal = new SetMeal();
        BeanUtils.copyProperties(setMealVO, setMeal);
        return setMealService.add(buildRequest(setMeal, setMealVO.getDishList()));
    }

    @Operation(summary = "根据id查询套餐信息")
    @GetMapping("/{id}")
    public SetMealVO getInfo(@PathVariable("id") String id) {
        SetMeal setMeal = setMealService.getById(id);
        if (setMeal == null) {
            throw new BussinessException("套餐不存在");
        }

        SetMealVO setMealVO = new SetMealVO();
        BeanUtils.copyProperties(setMeal, setMealVO);

        List<SetMealDishVO> setMealDishVOList = new ArrayList<>();
        List<SetMealDish> setMealDishList = setMeal.getDishList();
        if (setMealDishList != null) {
            for (SetMealDish setMealDish : setMealDishList) {
                SetMealDishVO setMealDishVO = new SetMealDishVO();
                setMealDishVO.setDishId(setMealDish.getDishId());
                setMealDishVO.setDishName(setMealDish.getDishName());
                setMealDishVO.setCopies(setMealDish.getDishCopies());
                setMealDishVOList.add(setMealDishVO);
            }
        }
        setMealVO.setDishList(setMealDishVOList);
        return setMealVO;
    }

    @Operation(summary = "修改套餐")
    @PutMapping
    public boolean update(@RequestBody SetMealVO setMealVO) {
        SetMeal setMeal = setMealService.getById(setMealVO.getId());
        if (setMeal == null) {
            throw new BussinessException("套餐不存在");
        }
        BeanUtils.copyProperties(setMealVO, setMeal);
        return setMealService.update(buildRequest(setMeal, setMealVO.getDishList()));
    }

    @Operation(summary = "套餐停售")
    @PutMapping("/forbidden")
    public boolean forbidden(@RequestBody List<String> ids) {
        return setMealService.forbiddenSetMeals(ids);
    }

    @Operation(summary = "套餐起售")
    @PutMapping("/enable")
    public boolean enable(@RequestBody List<String> ids) {
        return setMealService.enableSetMeals(ids);
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping
    public boolean delete(@RequestBody List<String> ids) {
        return setMealService.removeByIds(ids);
    }

    private SetMealWithDishes buildRequest(SetMeal setMeal, List<SetMealDishVO> dishList) {
        List<SetMealDish> setMealDishList = new ArrayList<>();
        if (dishList != null) {
            dishList.forEach(d -> {
                SetMealDish setMealDish = new SetMealDish();
                setMealDish.setIndex(0);
                setMealDish.setDishCopies(d.getCopies());
                setMealDish.setDishId(d.getDishId());
                setMealDish.setDishName(d.getDishName());
                setMealDishList.add(setMealDish);
            });
        }

        SetMealWithDishes request = new SetMealWithDishes();
        request.setSetMeal(setMeal);
        request.setDishList(setMealDishList);
        return request;
    }
}
