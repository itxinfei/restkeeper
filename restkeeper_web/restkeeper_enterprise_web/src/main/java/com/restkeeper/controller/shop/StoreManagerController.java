package com.restkeeper.controller.shop;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.service.IStoreManagerService;
import com.restkeeper.vo.shop.StoreManagerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店管理员。端点路径对齐 provider 契约 {@link IStoreManagerService}。
 */
@RestController
@RequestMapping("/store/manager")
@io.swagger.v3.oas.annotations.tags.Tag(name = "门店管理员接口")
public class StoreManagerController {

    private final IStoreManagerService storeManagerService;

    public StoreManagerController(IStoreManagerService storeManagerService) {
        this.storeManagerService = storeManagerService;
    }

    @Operation(summary = "查询分页数据")
    @GetMapping("/page/{pageNo}/{pageSize}")
    public PageResult<StoreManager> findListByPage(@PathVariable("pageNo") int pageNo,
                                                   @PathVariable("pageSize") int pageSize,
                                                   @RequestParam(value = "criteria", required = false) String criteria) {
        return storeManagerService.queryPageByCriteria(pageNo, pageSize, criteria);
    }

    @Operation(summary = "添加店长")
    @PostMapping("/add")
    public boolean add(@RequestBody StoreManagerVO storeManagerVO) {
        return storeManagerService.addStoreManager(
                storeManagerVO.getName(),
                storeManagerVO.getPhone(),
                storeManagerVO.getStoreIds());
    }

    @Operation(summary = "更新数据")
    @PutMapping("/update")
    public boolean update(@RequestBody StoreManagerVO storeManagerVO) {
        return storeManagerService.updateStoreManager(storeManagerVO.getStoreManagerId(),
                storeManagerVO.getName(),
                storeManagerVO.getPhone(),
                storeManagerVO.getStoreIds());
    }

    @Operation(summary = "删除数据")
    @DeleteMapping("/{storeManagerId}")
    public boolean delete(@PathVariable("storeManagerId") String storeManagerId) {
        return storeManagerService.deleteStoreManager(storeManagerId);
    }

    @Operation(summary = "门店管理员停用")
    @PutMapping("/pause/{storeManagerId}")
    public boolean pause(@PathVariable("storeManagerId") String storeManagerId) {
        return storeManagerService.pauseStoreManager(storeManagerId);
    }
}
