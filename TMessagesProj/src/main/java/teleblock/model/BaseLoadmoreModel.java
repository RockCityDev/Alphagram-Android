package teleblock.model;

import java.util.List;

/**
 * Time:2022/7/15
 * Author:Perry
 * Description：更多请求数据
 */
public class BaseLoadmoreModel<T> {

    //总数
    private int total;
    //当前请求到了多少条数据
    private int to;

    //数据
    private List<T> data;

    public List<T> getData() {
        return data;
    }

    public int getTotal() {
        return total;
    }

    public int getTo() {
        return to;
    }

    /**是否有剩余数据没有请求完**/
    public boolean whetherRemaining() {
        return total > to;
    }

}
