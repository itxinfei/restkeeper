package com.restkeeper.http;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 跨服务中立分页结果。
 * <p>
 * 替代 MyBatis-Plus 的 {@link IPage} 作为 HTTP 契约返回类型：
 * IPage 是接口，JSON 反序列化无具体实现类，无法直接跨进程传输。
 * provider 端由 {@link #of(IPage)} 转换，web 端再适配为 PageVO。
 */
@Data
@Schema(description = "跨服务中立分页结果")
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页数据")
    private List<T> records;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码（从 1 开始）")
    private long page;

    @Schema(description = "每页条数")
    private long size;

    @Schema(description = "总页数")
    private long pages;

    /**
     * 由 MyBatis-Plus 分页对象构造中立分页结果
     */
    public static <T> PageResult<T> of(IPage<T> iPage) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(iPage.getRecords());
        result.setTotal(iPage.getTotal());
        result.setPage(iPage.getCurrent());
        result.setSize(iPage.getSize());
        result.setPages(iPage.getPages());
        return result;
    }
}
