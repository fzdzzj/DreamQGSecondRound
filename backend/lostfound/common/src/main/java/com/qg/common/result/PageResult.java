package com.qg.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 数据列表
     */
    private List<T> list;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     * 每页记录数
     */
    private Integer pageSize;

}
