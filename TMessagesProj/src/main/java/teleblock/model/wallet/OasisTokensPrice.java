package teleblock.model.wallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2022/10/19
 * 描述：
 */
public class OasisTokensPrice {

    private int code;
    private Map<String, Double> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Map<String, Double> getData() {
        return data == null ? new HashMap<>() : data;
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
    }
}