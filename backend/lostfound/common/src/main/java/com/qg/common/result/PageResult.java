package com.qg.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T>  {

    private List<T> list;

    private Long total;

    private Integer pageNum;

    private Integer pageSize;

    // 计算总页数
    public Integer getTotalPages() {
        if (total == null || pageSize == null || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }
}
