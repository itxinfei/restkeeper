package com.restkeeper.store.controller;

import com.restkeeper.store.entity.Remark;
import com.restkeeper.store.service.IRemarkService;
import com.restkeeper.store.service.RemarkServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店备注 HTTP 端点：契约映射由 {@link IRemarkService} 上的 @HttpExchange 提供
 */
@RestController
public class RemarkController implements IRemarkService {

    @Autowired
    private RemarkServiceImpl remarkService;

    @Override
    public List<Remark> getRemarks() {
        return remarkService.getRemarks();
    }

    @Override
    public boolean updateRemarks(List<Remark> remarks) {
        return remarkService.updateRemarks(remarks);
    }
}
