package com.restkeeper.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restkeeper.exception.BussinessException;
import com.restkeeper.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 统一识别 provider 的 Result 错误信封。
 * <p>provider 出错时返回的是 HTTP 200 + {@code {"status":500,"desc":"..."}}，
 * 声明式 HTTP 代理会把它当业务数据反序列化：目标是对象时字段全为默认值（前端看到“共 0 条”），
 * 目标是 boolean/String 时直接抛 {@code Error while extracting response}，真实原因丢失。
 * <p>本拦截器在解码前读出响应体，确认是 Result 信封且 status 不为 200 才抛 {@link BussinessException}，
 * 由 BFF 的 GlobalExceptionHandler 转成可读提示；其余响应原样放行。
 * <p>注意：不能只靠 {@code status} 判定——业务实体也常有 status 字段
 * （如 {@code Dish.status}=1 表示起售），只看 status 会把正常数据误判成错误。
 */
@Slf4j
public class ResultStatusInterceptor implements ClientHttpRequestInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        byte[] payload = StreamUtils.copyToByteArray(response.getBody());
        if (payload.length == 0) {
            return response;
        }
        checkStatus(payload);
        return new ReplayedResponse(response, payload);
    }

    /**
     * 已读出响应体的可重复读取包装，供后续消息转换器正常解码。
     */
    private static final class ReplayedResponse implements ClientHttpResponse {

        private final ClientHttpResponse delegate;
        private final InputStream body;

        private ReplayedResponse(ClientHttpResponse delegate, byte[] payload) {
            this.delegate = delegate;
            this.body = new ByteArrayInputStream(payload);
        }

        @Override
        public InputStream getBody() {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }

    private void checkStatus(byte[] payload) {
        JsonNode node;
        try {
            node = OBJECT_MAPPER.readTree(payload);
        } catch (Exception e) {
            log.debug("响应体不是 JSON，跳过 Result 状态检查");
            return;
        }
        if (!isResultEnvelope(node)) {
            return;
        }
        int status = node.get("status").asInt();
        if (status != ResultCode.success) {
            throw new BussinessException(node.path("desc").asText("系统异常，请稍后重试"));
        }
    }

    /**
     * 是否为 provider 的 Result 信封：必须有 status，且带 token 或同时带 desc + data。
     * provider 侧 Result 四个字段（status/desc/data/token）恒定序列化（含 null），
     * 业务实体不会同时具备这些字段。
     */
    private boolean isResultEnvelope(JsonNode node) {
        if (node == null || !node.isObject() || !node.hasNonNull("status")) {
            return false;
        }
        return node.has("token") || (node.has("desc") && node.has("data"));
    }
}
