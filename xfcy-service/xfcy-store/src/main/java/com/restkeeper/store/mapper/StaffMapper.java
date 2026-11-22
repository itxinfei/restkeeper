package com.restkeeper.store.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.store.entity.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StaffMapper extends BaseMapper<Staff> {

    /**
     * 员工登录：登录时没有租户上下文，SQL 已显式带 shop_id 条件，豁免双租户插件。
     * 手写 SQL 不经过逻辑删除/租户插件，必须显式过滤 is_deleted，并拒绝停用账号登录。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("select * from t_staff where shop_id=#{shopId} and staff_name=#{loginName} "
            + "and status=1 and is_deleted=0")
    Staff login(@Param("shopId") String shopId, @Param("loginName") String loginName);
}
