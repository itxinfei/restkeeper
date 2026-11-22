package com.restkeeper.oss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘存储实现（dev 环境）：写入 ${oss.local.dir}，经 /upload/** 静态映射访问。
 */
@Service
@Profile("dev")
public class LocalFileStorage implements FileStorage {

    @Value("${oss.local.dir:./upload}")
    private String localDir;

    @Override
    public String store(String fileName, InputStream input) throws IOException {
        Path target = Paths.get(localDir).resolve(fileName);
        Files.createDirectories(target.getParent());
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        return "/upload/" + fileName;
    }
}
