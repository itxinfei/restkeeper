package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.store.entity.TableArea;
import com.restkeeper.store.mapper.TableAreaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 桌台区域本地服务
 */
@Service("tableAreaService")
public class TableAreaServiceImpl extends ServiceImpl<TableAreaMapper, TableArea> {

    /**
     * 新增区域（名称防重）
     */
    @Override
    @Transactional
    public boolean save(TableArea tableArea) {
        checkNameExist(tableArea.getAreaName());
        return super.save(tableArea);
    }

    /**
     * 区域下拉列表（按排序号倒序）
     */
    public List<Map<String, Object>> listTableArea() {
        QueryWrapper<TableArea> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .select(TableArea::getAreaId, TableArea::getAreaName)
                .orderByDesc(TableArea::getIndexNumber);
        return this.listMaps(queryWrapper);
    }

    /**
     * 区域名称防重
     */
    private void checkNameExist(String areaName) {
        QueryWrapper<TableArea> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(TableArea::getAreaId).eq(TableArea::getAreaName, areaName);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BussinessException("该区域已存在");
        }
    }
}
