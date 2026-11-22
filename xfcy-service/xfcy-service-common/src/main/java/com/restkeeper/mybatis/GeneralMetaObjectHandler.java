package com.restkeeper.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 通用字段自动填充
 */
@Component
public class GeneralMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 实体上不存在 lastUpdateTime 字段时 strictFill 自动跳过
        this.strictInsertFill(metaObject, "lastUpdateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "lastUpdateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
