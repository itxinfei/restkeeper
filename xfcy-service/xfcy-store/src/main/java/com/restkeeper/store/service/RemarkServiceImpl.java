package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.operator.entity.SysDictionary;
import com.restkeeper.operator.service.ISysDictService;
import com.restkeeper.store.entity.Remark;
import com.restkeeper.store.mapper.RemarkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 门店备注本地服务；字典兜底数据通过 HTTP Interface 调 operator 服务
 */
@Service("remarkService")
public class RemarkServiceImpl extends ServiceImpl<RemarkMapper, Remark> {

    @Autowired
    private ISysDictService sysDictService;

    /**
     * 查询门店备注，门店无自定义备注时回退到系统字典中的通用备注
     */
    public List<Remark> getRemarks() {
        List<Remark> remarks = this.list();
        if (remarks == null || remarks.isEmpty()) {
            remarks = new ArrayList<>();
            List<SysDictionary> dictionaryList = sysDictService.getDictionaryList(SystemCode.DICTIONARY_REMARK);
            for (SysDictionary dictionary : dictionaryList) {
                Remark remark = new Remark();
                remark.setRemarkName(dictionary.getDictName());
                remark.setRemarkValue(dictionary.getDictData());
                remarks.add(remark);
            }
        }
        return remarks;
    }

    /**
     * 全量覆盖门店备注（双租户插件自动限定当前门店）
     */
    @Transactional
    public boolean updateRemarks(List<Remark> remarks) {
        this.remove(new QueryWrapper<>());
        return this.saveBatch(remarks);
    }
}
