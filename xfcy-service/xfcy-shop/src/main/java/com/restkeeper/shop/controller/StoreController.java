package com.restkeeper.shop.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.service.IStoreService;
import com.restkeeper.shop.service.StoreServiceImpl;
import com.restkeeper.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店 HTTP 端点：契约映射由 {@link IStoreService} 上的 @HttpExchange 提供
 */
@RestController
public class StoreController implements IStoreService {

    @Autowired
    private StoreServiceImpl storeService;

    @Override
    public PageResult<Store> queryPageByName(int pageNo, int pageSize, String name) {
        return storeService.queryPageByName(pageNo, pageSize, name);
    }

    @Override
    public boolean save(Store store) {
        return storeService.save(store);
    }

    @Override
    public Store getById(String id) {
        return storeService.getById(id);
    }

    @Override
    public boolean updateById(Store store) {
        return storeService.updateById(store);
    }

    @Override
    public boolean disable(String id) {
        return storeService.disable(id);
    }

    @Override
    public boolean enable(String id) {
        return storeService.enable(id);
    }

    @Override
    public List<String> getAllProvince() {
        return storeService.getAllProvince();
    }

    @Override
    public List<StoreDTO> getStoreByProvince(String province) {
        return storeService.getStoreByProvince(province);
    }

    @Override
    public List<StoreDTO> getStoreListByManagerId() {
        return storeService.getStoreListByManagerId();
    }

    @Override
    public Result switchStore(String storeId) {
        return storeService.switchStore(storeId);
    }
}
