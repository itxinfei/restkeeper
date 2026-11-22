package com.restkeeper.oss;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件存储抽象：dev 走本地磁盘，其它环境走阿里云 OSS。
 * <p>替换已废弃的 spring-cloud-starter-alicloud-oss（见 requirements.md 10.4-8）。
 */
public interface FileStorage {

    /**
     * 存储文件并返回可访问 URL。
     *
     * @param fileName 目标文件名（含目录前缀）
     * @param input    文件内容
     * @return 可访问 URL（OSS 返回 https 绝对地址，本地返回 /upload/ 相对路径）
     */
    String store(String fileName, InputStream input) throws IOException;
}
