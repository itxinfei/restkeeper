package com.restkeeper.shop.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.service.IStoreManagerService;
import com.restkeeper.shop.service.StoreManagerServiceImpl;
import com.restkeeper.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店管理员 HTTP 端点：契约映射由 {@link IStoreManagerService} 上的 @HttpExchange 提供
 */
@RestController
public class StoreManagerController implements IStoreManagerService {

    @Autowired
    private StoreManagerServiceImpl storeManagerService;

    @Override
    public PageResult<StoreManager> queryPageByCriteria(int pageNo, int pageSize, String criteria) {
        return storeManagerService.queryPageByCriteria(pageNo, pageSize, criteria);
    }

    @Override
    public boolean addStoreManager(String name, String phone, List<String> storeIds) {
        return storeManagerService.addStoreManager(name, phone, storeIds);
    }

    @Override
    public boolean updateStoreManager(String storeManagerId, String name, String phone, List<String> storeIds) {
        return storeManagerService.updateStoreManager(storeManagerId, name, phone, storeIds);
    }

    @Override
    public boolean deleteStoreManager(String storeManagerId) {
        return storeManagerService.deleteStoreManager(storeManagerId);
    }

    @Override
    public boolean pauseStoreManager(String storeManagerId) {
        return storeManagerService.pauseStoreManager(storeManagerId);
    }

    @Override
    public Result login(String shopId, String phone, String loginPass) {
        return storeManagerService.login(shopId, phone, loginPass);
    }
}
