package com.restkeeper.controller.store;

import com.alibaba.excel.EasyExcel;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.http.PageResult;
import com.restkeeper.store.dto.CreditWithCompanyUsers;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.entity.CreditLogs;
import com.restkeeper.store.entity.CreditRepayment;
import com.restkeeper.store.service.ICreditLogService;
import com.restkeeper.store.service.ICreditRepaymentService;
import com.restkeeper.store.service.ICreditService;
import com.restkeeper.vo.store.CreditLogExcelVO;
import com.restkeeper.vo.store.CreditRepaymentVO;
import com.restkeeper.vo.store.CreditVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 挂账管理。端点路径对齐 provider 契约 {@link ICreditService} / {@link ICreditLogService} /
 * {@link ICreditRepaymentService}；导出为 BFF 层能力（路径自定义）。
 */
@Slf4j
@Tag(name = "挂账管理")
@RestController
@RequestMapping("/credit")
public class CreditController {

    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ICreditService creditService;
    private final ICreditLogService creditLogService;
    private final ICreditRepaymentService creditRepaymentService;

    public CreditController(ICreditService creditService,
                            ICreditLogService creditLogService,
                            ICreditRepaymentService creditRepaymentService) {
        this.creditService = creditService;
        this.creditLogService = creditLogService;
        this.creditRepaymentService = creditRepaymentService;
    }

    @Operation(summary = "新增挂账")
    @PostMapping("/add")
    public boolean add(@RequestBody CreditVO creditVO) {
        Credit credit = new Credit();
        BeanUtils.copyProperties(creditVO, credit, "users");

        CreditWithCompanyUsers request = new CreditWithCompanyUsers();
        request.setCredit(credit);
        request.setUsers(toCompanyUsers(creditVO.getUsers()));
        return creditService.add(request);
    }

    @Operation(summary = "挂账管理列表")
    @GetMapping("/page/{pageNum}/{pageSize}")
    public PageResult<Credit> pageList(@PathVariable("pageNum") int pageNum,
                                       @PathVariable("pageSize") int pageSize,
                                       @RequestParam(value = "userName", defaultValue = "", required = false) String userName) {
        return creditService.queryPage(pageNum, pageSize, userName);
    }

    @Operation(summary = "根据id查询挂账信息")
    @GetMapping("/{id}")
    public CreditVO getCredit(@PathVariable("id") String id) {
        CreditVO creditVO = new CreditVO();
        Credit credit = creditService.queryById(id);
        BeanUtils.copyProperties(credit, creditVO);
        return creditVO;
    }

    @Operation(summary = "修改挂账")
    @PutMapping
    public boolean updateCredit(@RequestBody CreditVO creditVO) {
        Credit credit = new Credit();
        BeanUtils.copyProperties(creditVO, credit, "users");

        CreditWithCompanyUsers request = new CreditWithCompanyUsers();
        request.setCredit(credit);
        request.setUsers(toCompanyUsers(creditVO.getUsers()));
        return creditService.updateInfo(request);
    }

    @Operation(summary = "挂账订单明细列表")
    @GetMapping("/log/page/{pageNum}/{pageSize}/{creditId}")
    public PageResult<CreditLogs> getCreditLogPageList(@PathVariable("pageNum") int pageNum,
                                                       @PathVariable("pageSize") int pageSize,
                                                       @PathVariable("creditId") String creditId) {
        return creditLogService.queryPage(pageNum, pageSize, creditId);
    }

    @Operation(summary = "挂账流水导出")
    @GetMapping("/export/creditId/{creditId}/start/{start}/end/{end}")
    public void export(HttpServletResponse response,
                       @PathVariable("creditId") String creditId,
                       @PathVariable("start") String start,
                       @PathVariable("end") String end) throws Exception {

        LocalDateTime startTime = parseExportTime(start);
        LocalDateTime endTime = parseExportTime(end);

        if (endTime.isBefore(startTime)) {
            throw new BussinessException("结束时间不能比开始时间小");
        }

        List<CreditLogExcelVO> data = creditLogService.list(creditId, startTime, endTime)
                .stream()
                .map(c -> {
                    CreditLogExcelVO creditLogExcelVO = new CreditLogExcelVO();
                    creditLogExcelVO.setCreditAmount(c.getCreditAmount());
                    creditLogExcelVO.setDateTime(Date.from(c.getLastUpdateTime().atZone(ZoneId.systemDefault()).toInstant()));
                    creditLogExcelVO.setOrderAmount(c.getOrderAmount());
                    creditLogExcelVO.setRevenueAmount(c.getReceivedAmount());
                    creditLogExcelVO.setUserName(c.getUserName());
                    creditLogExcelVO.setOrderId(c.getOrderId());
                    if (c.getType() == SystemCode.CREDIT_TYPE_COMPANY) {
                        creditLogExcelVO.setCreditType("企业");
                    } else {
                        creditLogExcelVO.setCreditType("个人");
                    }
                    return creditLogExcelVO;
                }).collect(Collectors.toList());

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("挂账流水", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), CreditLogExcelVO.class).sheet("挂账流水").doWrite(data);
    }

    @Operation(summary = "还款")
    @PostMapping("/repayment")
    public boolean repayment(@RequestBody CreditRepaymentVO creditRepaymentVO) {
        CreditRepayment creditRepayment = new CreditRepayment();
        BeanUtils.copyProperties(creditRepaymentVO, creditRepayment);
        return creditRepaymentService.repayment(creditRepayment);
    }

    private List<CreditCompanyUser> toCompanyUsers(List<com.restkeeper.vo.store.CreditCompanyUserVO> users) {
        List<CreditCompanyUser> companyUsers = new ArrayList<>();
        if (users != null && !users.isEmpty()) {
            users.forEach(d -> {
                CreditCompanyUser creditCompanyUser = new CreditCompanyUser();
                BeanUtils.copyProperties(d, creditCompanyUser);
                companyUsers.add(creditCompanyUser);
            });
        }
        return companyUsers;
    }

    private LocalDateTime parseExportTime(String text) {
        try {
            return LocalDateTime.parse(text, EXPORT_TIME_FORMATTER);
        } catch (java.time.format.DateTimeParseException e) {
            // 兼容旧前端的 ISO_LOCAL_DATE_TIME（yyyy-MM-dd'T'HH:mm:ss）
            return LocalDateTime.parse(text);
        }
    }
}
