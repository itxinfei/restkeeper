package com.restkeeper.utils;

import java.security.SecureRandom;

/**
 * 随机数字码生成工具：初始密码、短信验证码等场景使用。
 * 基于 SecureRandom，避免引入 commons-lang3。
 */
public final class RandomCodeUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomCodeUtil() {
    }

    /**
     * 生成指定位数的随机数字串（首位允许为 0）
     *
     * @param length 位数
     * @return 随机数字串
     */
    public static String randomNumeric(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
