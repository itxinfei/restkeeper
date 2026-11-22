package com.restkeeper.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.operator.entity.SysDictionary;
import com.restkeeper.operator.mapper.SysDictMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("sysDictionaryService")
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDictionary> {

    //按分类查询字典项
    public List<SysDictionary> getDictionaryList(String category) {
        QueryWrapper<SysDictionary> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysDictionary::getCategory, category);
        return this.list(queryWrapper);
    }
}
