package com.restkeeper.store.controller;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.StaffLoginDTO;
import com.restkeeper.store.entity.Staff;
import com.restkeeper.store.service.IStaffService;
import com.restkeeper.store.service.StaffServiceImpl;
import com.restkeeper.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店员工 HTTP 端点：契约映射由 {@link IStaffService} 上的 @HttpExchange 提供
 */
@RestController
public class StaffController implements IStaffService {

    @Autowired
    private StaffServiceImpl staffService;

    @Override
    public PageResult<Staff> queryPage(int pageNum, int pageSize, String staffName, String phone) {
        return staffService.queryPage(pageNum, pageSize, staffName, phone);
    }

    @Override
    public boolean save(Staff staff) {
        return staffService.save(staff);
    }

    @Override
    public boolean updateById(Staff staff) {
        return staffService.updateById(staff);
    }

    @Override
    public boolean pause(String id) {
        return staffService.pause(id);
    }

    @Override
    public String addStaff(Staff staff) {
        return staffService.addStaff(staff);
    }

    @Override
    public Result login(StaffLoginDTO dto) {
        return staffService.login(dto.getShopId(), dto.getLoginName(), dto.getPassword());
    }
}
