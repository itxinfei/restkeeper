package com.restkeeper.advice;

import com.restkeeper.exception.BussinessException;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * provider 统一异常出口（K17）。
 * <p>provider 裸返回，异常原先落到 Spring 默认错误体（无统一 code/信封）；此处统一收敛为 {@link Result}
 * （status=500 + desc），与 BFF 层 BaseResponse 的错误语义对齐。由 provider 启动类
 * {@code scanBasePackages="com.restkeeper"} 扫描注册，对五个 provider + mq + job 生效。
 */
@Slf4j
@RestControllerAdvice
public class ProviderExceptionAdvice {

    /** 业务异常：消息直接回给调用方 */
    @ExceptionHandler(BussinessException.class)
    public Result handleBusiness(BussinessException e) {
        return error(e.getMessage());
    }

    /** 参数/状态校验失败（如账号类型错误、修改账户不存在） */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgument(IllegalArgumentException e) {
        return error(e.getMessage());
    }

    /** 兜底：记录日志，对外只给通用提示 */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("provider 未处理异常", e);
        return error("系统异常，请稍后重试");
    }

    private Result error(String desc) {
        Result result = new Result();
        result.setStatus(ResultCode.error);
        result.setDesc(desc);
        return result;
    }
}
