package com.qg.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
/**
 * 分页结果
 *
 * @param <T>
 */
@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private List<T> list;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    public PageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
