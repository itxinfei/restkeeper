package com.restkeeper.order.config;

import org.apache.seata.core.context.RootContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Seata 全局事务 XID 出站透传。
 * <p>order provider 的 {@code @GlobalTransactional} 方法开启全局事务后，XID 存放在
 * {@link RootContext}（ThreadLocal）。跨服务 HTTP 调用（如挂账写 store 库）必须带上
 * {@code TX_XID}，下游分支事务才会挂到同一个全局事务上；缺失时跨库写无法回滚，
 * 会出现「订单已改状态、挂账额度没撤销」的双标。
 * <p>未开启全局事务时 XID 为 null，不写头，不影响普通调用。
 */
public class SeataXidClientInterceptor implements ClientHttpRequestInterceptor {

    /**
     * Seata 规定的 XID 请求头名
     */
    public static final String TX_XID = "TX_XID";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String xid = RootContext.getXID();
        if (xid != null && !xid.isEmpty()) {
            request.getHeaders().set(TX_XID, xid);
        }
        return execution.execute(request, body);
    }
}
