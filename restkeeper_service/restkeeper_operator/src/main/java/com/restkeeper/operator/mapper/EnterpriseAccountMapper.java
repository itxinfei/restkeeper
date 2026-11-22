package com.restkeeper.operator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.restkeeper.operator.entity.EnterpriseAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EnterpriseAccountMapper extends BaseMapper<EnterpriseAccount> {

    //帐号还原
    @Update("update t_enterprise_account set is_deleted=0 where enterprise_id=#{id} and is_deleted=1")
    boolean recovery(@Param("id") String id);

    //回收站分页：手写 SQL 绕过 @TableLogic 自动追加的 is_deleted=0
    @Select("<script>"
            + "select * from t_enterprise_account where is_deleted = 1"
            + "<if test='enterpriseName != null and enterpriseName != \"\"'>"
            + "  and enterprise_name like concat('%', #{enterpriseName}, '%')"
            + "</if>"
            + " order by application_time desc"
            + "</script>")
    IPage<EnterpriseAccount> queryRemovedPage(IPage<EnterpriseAccount> page,
                                              @Param("enterpriseName") String enterpriseName);
}
