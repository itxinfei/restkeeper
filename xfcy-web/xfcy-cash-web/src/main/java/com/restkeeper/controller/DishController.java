package com.restkeeper.controller;

import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import com.restkeeper.http.PageResult;
import com.restkeeper.service.IDishSearchService;
import com.restkeeper.store.service.ISellCalculationService;
import com.restkeeper.vo.DishPanelVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品搜索（阶段 4：Dubbo @Reference → HTTP Interface 契约代理）。
 */
@Tag(name = "菜品搜索")
@RestController
public class DishController {

    private final IDishSearchService dishSearchService;
    private final ISellCalculationService sellCalculationService;

    public DishController(IDishSearchService dishSearchService,
                          ISellCalculationService sellCalculationService) {
        this.dishSearchService = dishSearchService;
        this.sellCalculationService = sellCalculationService;
    }

    @Operation(summary = "根据商品编码搜索菜品")
    @GetMapping("/search/dish/code/{code}/page/{pageNum}/{pageSize}")
    public PageResult<DishPanelVO> queryByCode(@PathVariable("code") String code,
                                               @PathVariable("pageNum") int pageNum,
                                               @PathVariable("pageSize") int pageSize) {
        SearchResult<DishEs> searchResult = dishSearchService.searchDishByCode(code, pageNum, pageSize);

        List<DishPanelVO> dishVOList = new ArrayList<>();
        searchResult.getRecords().forEach(es -> {
            DishPanelVO dishPanelVO = new DishPanelVO();
            dishPanelVO.setDishId(es.getId());
            dishPanelVO.setDishName(es.getDishName());
            dishPanelVO.setPrice(es.getPrice());
            dishPanelVO.setImage(es.getImage());
            dishPanelVO.setType(es.getType());
            dishPanelVO.setRemainder(sellCalculationService.getRemainderCount(es.getId()));
            dishVOList.add(dishPanelVO);
        });

        long pages = searchResult.getTotal() % pageSize == 0
                ? searchResult.getTotal() / pageSize
                : searchResult.getTotal() / pageSize + 1;

        PageResult<DishPanelVO> result = new PageResult<>();
        result.setRecords(dishVOList);
        result.setTotal(searchResult.getTotal());
        result.setPage(pageNum);
        result.setSize(pageSize);
        result.setPages(pages);
        return result;
    }
}
