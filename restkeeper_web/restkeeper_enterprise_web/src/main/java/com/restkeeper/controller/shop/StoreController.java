package com.restkeeper.controller.shop;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.service.IStoreService;
import com.restkeeper.utils.Result;
import com.restkeeper.vo.shop.AddStoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店信息。端点路径对齐 provider 契约 {@link IStoreService}。
 */
@Slf4j
@Tag(name = "门店信息")
@RestController
@RequestMapping("/store")
public class StoreController {

    private final IStoreService storeService;

    public StoreController(IStoreService storeService) {
        this.storeService = storeService;
    }

    @Operation(summary = "分页查询所有门店")
    @GetMapping("/page/{pageNo}/{pageSize}")
    public PageResult<Store> findListByPage(@PathVariable("pageNo") int pageNo,
                                            @PathVariable("pageSize") int pageSize,
                                            @RequestParam(value = "name", required = false) String name) {
        return storeService.queryPageByName(pageNo, pageSize, name);
    }

    @Operation(summary = "新增门店")
    @PostMapping
    public boolean add(@RequestBody AddStoreVO storeVO) {
        Store store = new Store();
        BeanUtils.copyProperties(storeVO, store);
        return storeService.save(store);
    }

    @Operation(summary = "按主键查询门店")
    @GetMapping("/{id}")
    public Store getById(@PathVariable("id") String id) {
        return storeService.getById(id);
    }

    @Operation(summary = "按主键更新门店")
    @PutMapping
    public boolean updateById(@RequestBody Store store) {
        return storeService.updateById(store);
    }

    @Operation(summary = "门店停用")
    @PutMapping("/disable/{id}")
    public boolean disable(@PathVariable("id") String id) {
        return storeService.disable(id);
    }

    @Operation(summary = "门店启用")
    @PutMapping("/enable/{id}")
    public boolean enable(@PathVariable("id") String id) {
        return storeService.enable(id);
    }

    @Operation(summary = "获取门店省份信息")
    @GetMapping("/province/list")
    public List<String> listProvince() {
        return storeService.getAllProvince();
    }

    @Operation(summary = "根据省份获取门店列表")
    @GetMapping("/province/{province}")
    public List<StoreDTO> getStoreByProvince(@PathVariable("province") String province) {
        return storeService.getStoreByProvince(province);
    }

    @Operation(summary = "获取当前商户管理的门店信息")
    @GetMapping("/manager/list")
    public List<StoreDTO> getStoreListByManagerId() {
        return storeService.getStoreListByManagerId();
    }

    @Operation(summary = "门店切换")
    @PutMapping("/switch/{storeId}")
    public Result switchStore(@PathVariable("storeId") String storeId) {
        return storeService.switchStore(storeId);
    }
}
