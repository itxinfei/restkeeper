package com.restkeeper.controller.store;

import com.restkeeper.store.entity.Remark;
import com.restkeeper.store.service.IRemarkService;
import com.restkeeper.vo.store.RemarkVO;
import com.restkeeper.vo.store.SettingsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 门店备注设置。端点路径对齐 provider 契约 {@link IRemarkService}。
 */
@Slf4j
@Tag(name = "门店备注管理")
@RestController
@RequestMapping("/remark")
public class SettingsController {

    private final IRemarkService remarkService;

    public SettingsController(IRemarkService remarkService) {
        this.remarkService = remarkService;
    }

    @Operation(summary = "获取门店设置信息")
    @GetMapping("/list")
    public SettingsVO getSysSettings() {
        List<Remark> remarks = remarkService.getRemarks();

        List<RemarkVO> remarkVOS = new ArrayList<>();
        remarks.forEach(d -> {
            RemarkVO remarkVO = new RemarkVO();
            remarkVO.setRemarkName(d.getRemarkName());

            String remarkValue = d.getRemarkValue();
            if (remarkValue != null) {
                String remarkValueSubstring = remarkValue.substring(remarkValue.indexOf("[") + 1, remarkValue.indexOf("]"));
                if (StringUtils.hasText(remarkValueSubstring)) {
                    String[] remarkArray = remarkValueSubstring.split(",");
                    remarkVO.setRemarkValue(Arrays.asList(remarkArray));
                }
            }
            remarkVOS.add(remarkVO);
        });

        SettingsVO settingsVO = new SettingsVO();
        settingsVO.setRemarks(remarkVOS);
        return settingsVO;
    }

    @Operation(summary = "修改门店备注")
    @PutMapping("/update")
    public boolean update(@RequestBody SettingsVO settingsVO) {
        List<Remark> remarks = new ArrayList<>();
        List<RemarkVO> remarkVOList = settingsVO.getRemarks();

        remarkVOList.forEach(remarkVO -> {
            Remark remark = new Remark();
            remark.setRemarkName(remarkVO.getRemarkName());
            remark.setRemarkValue(remarkVO.getRemarkValue().toString());
            remarks.add(remark);
        });

        return remarkService.updateRemarks(remarks);
    }
}
