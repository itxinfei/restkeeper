package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.mapper.CreditMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 挂账主体本地服务
 */
@Service("creditService")
public class CreditServiceImpl extends ServiceImpl<CreditMapper, Credit> {

    @Autowired
    private CreditCompanyUserServiceImpl creditCompanyUserService;

    /**
     * 新增挂账主体（企业挂账同时落关联用餐人）
     */
    @Transactional
    public boolean add(Credit credit, List<CreditCompanyUser> users) {
        this.save(credit);

        if (users != null && !users.isEmpty()) {
            checkUserNameDuplicate(users);
            users.forEach(user -> user.setCreditId(credit.getCreditId()));
            return creditCompanyUserService.saveBatch(users);
        }
        return true;
    }

    /**
     * 挂账分页：按挂账人名称或企业用餐人名称模糊，企业挂账回填用餐人列表
     */
    public PageResult<Credit> queryPage(int pageNum, int pageSize, String userName) {
        Page<Credit> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Credit> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(userName)) {
            //apply 使用 {0} 占位参数化，杜绝拼接注入；子查询同样被多租户插件追加 shop_id/store_id 条件
            queryWrapper.lambda().like(Credit::getUserName, userName)
                    .or()
                    .apply("credit_id in (select credit_id from t_credit_company_user where user_name like {0})",
                            "%" + userName + "%");
        }
        Page<Credit> resultPage = this.page(page, queryWrapper);

        resultPage.getRecords().forEach(credit -> {
            //挂账类型为 NULL 时不能与 int 常量直接比较，否则拆箱 NPE 整页 500
            if (Objects.equals(credit.getCreditType(), SystemCode.CREDIT_TYPE_COMPANY)) {
                QueryWrapper<CreditCompanyUser> userWrapper = new QueryWrapper<>();
                userWrapper.lambda().eq(CreditCompanyUser::getCreditId, credit.getCreditId());
                credit.setUsers(creditCompanyUserService.list(userWrapper));
            }
        });
        return PageResult.of(resultPage);
    }

    /**
     * 挂账详情（企业挂账含关联用餐人）
     */
    public Credit queryById(String id) {
        Credit credit = this.getById(id);
        if (credit == null) {
            throw new BussinessException("不存在该挂账信息");
        }
        fillCompanyUsers(credit);
        return credit;
    }

    /**
     * 修改挂账主体：企业挂账先删旧关联再重建
     */
    @Transactional
    public boolean updateInfo(Credit credit, List<CreditCompanyUser> users) {
        if (Objects.equals(credit.getCreditType(), SystemCode.CREDIT_TYPE_COMPANY)) {
            List<CreditCompanyUser> oldUsers = credit.getUsers();
            if (oldUsers != null && !oldUsers.isEmpty()) {
                List<String> idList = oldUsers.stream().map(CreditCompanyUser::getId).collect(Collectors.toList());
                creditCompanyUserService.removeByIds(idList);
            }
        }

        if (users != null && !users.isEmpty()) {
            checkUserNameDuplicate(users);
            users.forEach(user -> user.setCreditId(credit.getCreditId()));
            return creditCompanyUserService.saveBatch(users);
        }

        return this.saveOrUpdate(credit);
    }

    /**
     * 企业挂账回填关联用餐人列表
     */
    private void fillCompanyUsers(Credit credit) {
        if (!Objects.equals(credit.getCreditType(), SystemCode.CREDIT_TYPE_COMPANY)) {
            return;
        }
        QueryWrapper<CreditCompanyUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CreditCompanyUser::getCreditId, credit.getCreditId());
        credit.setUsers(creditCompanyUserService.list(queryWrapper));
    }

    /**
     * 用餐人名称重复校验
     */
    private void checkUserNameDuplicate(List<CreditCompanyUser> users) {
        List<String> userNameList = users.stream().map(CreditCompanyUser::getUserName).collect(Collectors.toList());
        long distinctCount = userNameList.stream().distinct().count();
        if (userNameList.size() != distinctCount) {
            throw new BussinessException("用户名重复");
        }
    }
}
