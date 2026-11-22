package com.restkeeper.response.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.restkeeper.exception.BussinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：消息可直接展示给前端
     */
    @ExceptionHandler(BussinessException.class)
    public Object businessException(BussinessException e) {
        return new ExceptionResponse(e.getMessage());
    }

    /**
     * Sa-Token 未登录 / 登录已过期
     */
    @ExceptionHandler(NotLoginException.class)
    public Object notLoginException(NotLoginException e) {
        log.warn("未登录访问: {}", e.getMessage());
        return new ExceptionResponse("未登录或登录已过期，请重新登录");
    }

    /**
     * Sa-Token 角色 / 权限不足
     */
    @ExceptionHandler({NotRoleException.class, NotPermissionException.class})
    public Object notAuthorizedException(RuntimeException e) {
        log.warn("无访问权限: {}", e.getMessage());
        return new ExceptionResponse("没有访问权限");
    }

    @ExceptionHandler(Exception.class)
    public Object exception(Exception e) {
        log.error("系统异常", e);
        return new ExceptionResponse(e.getMessage());
    }
}
