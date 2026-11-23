package com.restkeeper.controller.store;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.Staff;
import com.restkeeper.store.service.IStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(summary = "员工分页")
    @GetMapping("/page/{pageNum}/{pageSize}")
    public PageResult<Staff> page(@PathVariable("pageNum") int pageNum,
                                  @PathVariable("pageSize") int pageSize,
                                  @RequestParam(value = "staffName", required = false) String staffName,
                                  @RequestParam(value = "phone", required = false) String phone) {
        PageResult<Staff> result = staffService.queryPage(pageNum, pageSize, staffName, phone);
        // 密码哈希不对外暴露（编辑时前端留空即不改密码）
        if (result.getRecords() != null) {
            result.getRecords().forEach(staff -> staff.setPassword(null));
        }
        return result;
    }

    @Operation(summary = "新增员工")
    @PostMapping("/add")
    public boolean add(@RequestBody Staff staff) {
        return staffService.addStaff(staff);
    }

    @Operation(summary = "更新员工")
    @PutMapping
    public boolean update(@RequestBody Staff staff) {
        return staffService.updateById(staff);
    }

    @Operation(summary = "停用员工")
    @PutMapping("/pause/{id}")
    public boolean pause(@PathVariable("id") String id) {
        return staffService.pause(id);
    }
}
