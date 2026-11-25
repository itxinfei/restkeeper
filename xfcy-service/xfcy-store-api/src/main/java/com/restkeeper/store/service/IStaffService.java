package com.restkeeper.store.service;

import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.StaffLoginDTO;
import com.restkeeper.store.entity.Staff;
import com.restkeeper.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@Tag(name = "员工服务")
@HttpExchange("/staff")
public interface IStaffService {

    //分页查询（按员工姓名、手机号模糊，双租户插件自动隔离门店）
    @GetExchange("/page/{pageNum}/{pageSize}")
    PageResult<Staff> queryPage(@PathVariable("pageNum") int pageNum,
                                @PathVariable("pageSize") int pageSize,
                                @RequestParam(value = "staffName", required = false) String staffName,
                                @RequestParam(value = "phone", required = false) String phone);

    //保存员工（管家端新增收银员入口）
    @PostExchange
    boolean save(@RequestBody Staff staff);

    //按主键更新员工（含停用后重新启用：改 status）
    @PutExchange
    boolean updateById(@RequestBody Staff staff);

    //停用员工（status 置 0）
    @PutExchange("/pause/{id}")
    boolean pause(@PathVariable("id") String id);

    //新增员工信息（返回实际生效的登录密码：未传密码时后端生成 8 位随机数字）
    @PostExchange("/add")
    String addStaff(@RequestBody Staff staff);

    //登录（body 传参：规避 Boot4/Tomcat11 query 中文按 ISO-8859-1 解码导致的 Character decoding failed）
    @PostExchange("/login")
    Result login(@RequestBody StaffLoginDTO dto);
}
