package com.restkeeper.controller;

import com.restkeeper.oss.FileStorage;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 图片上传通用接口（BFF 层能力，不在 provider 契约内）。
 * <p>阶段 4：存储实现由 {@link FileStorage} 提供（dev 本地磁盘 / 其它环境阿里云 OSS）。
 */
@Tag(name = "图片上传通用接口")
@RestController
public class FileUploadController {

    private final FileStorage fileStorage;

    public FileUploadController(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Operation(summary = "文件上传")
    @PostMapping("/fileUpload")
    public Result fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        Result result = new Result();
        String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
        try {
            String logoPath = fileStorage.store(fileName, multipartFile.getInputStream());
            result.setData(logoPath);
            result.setStatus(ResultCode.success);
            result.setDesc("上传成功");
            return result;
        } catch (IOException e) {
            result.setStatus(ResultCode.error);
            result.setDesc("上传失败");
            return result;
        }
    }

    @Operation(summary = "图片上传（带缩放）")
    @PostMapping("/imageUploadResize")
    public Result imageUploadResize(@RequestParam("fileName") MultipartFile multipartFile) {
        Result result = new Result();
        String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
        try {
            String imagePath = fileStorage.store(fileName, multipartFile.getInputStream())
                    + "?x-oss-process=image/resize,m_fill,h_100,w_200";
            result.setStatus(ResultCode.success);
            result.setDesc("图片上传成功");
            result.setData(imagePath);
            return result;
        } catch (IOException e) {
            result.setStatus(ResultCode.error);
            result.setDesc("图片上传失败");
            return result;
        }
    }
}
