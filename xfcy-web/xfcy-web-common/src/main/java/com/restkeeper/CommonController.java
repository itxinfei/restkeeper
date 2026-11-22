package com.restkeeper;

import com.restkeeper.utils.PinyinUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/common")
@Tag(name = "通用接口")
public class CommonController {

    @GetMapping("/toHanyuPinyin/{chineseLanguage}")
    @Operation(summary = "中文转换汉语拼音")
    public Result toHanyuPinyin(@Parameter(description = "待转换的中文文本", required = true)
                                @PathVariable String chineseLanguage)
            throws BadHanyuPinyinOutputFormatCombination {
        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(PinyinUtil.toHanyuPinyin(chineseLanguage));
        return result;
    }
}
