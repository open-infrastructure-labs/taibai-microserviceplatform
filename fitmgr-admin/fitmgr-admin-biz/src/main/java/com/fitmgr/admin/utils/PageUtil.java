package com.fitmgr.admin.utils;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageUtil<T> implements Serializable {

    private List<T> data;
    private int pageSize;

    /**
     * @param data     原始数据
     * @param pageSize 每页条数
     */
    public PageUtil(List<T> data, int pageSize) {
        this.data = data;
        this.pageSize = pageSize;
    }

    /**
     * 获取某页数据，从第1页开始
     *
     * @param pageNum 第几页
     * @return 分页数据
     */
    public List<T> page(int pageNum) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(pageNum * pageSize, data.size());
        if (from > to) {
            from = to;
        }
        return data.subList(from, to);
    }

    /**
     * 获取总页数
     */
    public int getPageCount() {
        if (pageSize == 0) {
            return 0;
        }
        return data.size() % pageSize == 0 ? (data.size() / pageSize) : (data.size() / pageSize + 1);
    }

}
