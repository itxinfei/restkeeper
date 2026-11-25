package com.restkeeper.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HTTP 请求头文本编解码工具。
 * <p>HTTP 头值按 ISO-8859-1 传输，直接放中文等非 ASCII 字符会变成 "???"。
 * 约定：非 ASCII 文本统一使用 {@code B64:} 前缀 + Base64(UTF-8)；
 * 不带前缀的值视为普通 ASCII 文本原样返回，兼容历史调用方。
 */
public final class HeaderTextCodec {

    private static final String PREFIX = "B64:";

    private HeaderTextCodec() {
    }

    /**
     * 编码：含非 ASCII 字符时加 {@code B64:} 前缀并做 Base64，纯 ASCII 原样返回
     */
    public static String encode(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        boolean ascii = true;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > 127) {
                ascii = false;
                break;
            }
        }
        if (ascii) {
            return value;
        }
        return PREFIX + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码：带 {@code B64:} 前缀时还原 UTF-8 文本，否则原样返回
     */
    public static String decode(String value) {
        if (value == null || !value.startsWith(PREFIX)) {
            return value;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // 前缀存在但内容非法：按原值返回，避免影响主流程
            return value;
        }
    }
}
