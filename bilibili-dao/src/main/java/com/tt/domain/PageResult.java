package com.tt.domain;

import java.util.List;

/**
 * ClassName: PageResult
 * Package: com.tt.domain
 * Description:
 *
 * @Create 2025/3/22 20:11
 */
public class PageResult<T> {
    private Integer total;//总的数据条数

    private List<T> list;

    public PageResult(Integer total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
