package com.restkeeper.config;

import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * C 端独立 StpLogic（阶段 6）：token 会话键带 "microapp" 前缀，与 B 端（运营/企业/店长/员工）隔离。
 * <p>网关对 /microApp/** 放行，鉴权由本 BFF 内部用该 StpLogic 校验。
 */
@Configuration
public class MicroappStpLogicConfig {

    @Bean
    public StpLogic microappStpLogic() {
        return new StpLogic("microapp");
    }
}
