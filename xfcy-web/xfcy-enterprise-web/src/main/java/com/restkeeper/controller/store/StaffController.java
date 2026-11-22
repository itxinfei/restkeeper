package com.restkeeper.controller.store;

import com.restkeeper.store.entity.Staff;
import com.restkeeper.store.service.IStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工管理。端点路径对齐 provider 契约 {@link IStaffService}。
 */
@Slf4j
@Tag(name = "员工管理")
@RestController
@RequestMapping("/staff")
public class StaffController {

    private final IStaffService staffService;

    public StaffController(IStaffService staffService) {
        this.staffService = staffService;
    }

    @Operation(summary = "新增员工")
    @PostMapping("/add")
    public boolean add(@RequestBody Staff staff) {
        return staffService.addStaff(staff);
    }
}
