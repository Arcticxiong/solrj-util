package cn.itcast.util;

import java.util.List;

public class BeanData<T> {

    //总记录数
    private Long total;

    //集合数据
    private List<T> list;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
