package teleblock.network;

/**
 * Time:2022/6/20
 * Author:Perry
 * Description：http网络请求基类
 */
public class BaseBean<T> {

    /**
     * 返回码
     */
    private int code;
    /**
     * 状态
     */
    private String status;
    /**
     * 错误提示
     */
    private String message;
    /**
     * 数据
     */
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 是否请求成功
     */
    public boolean isRequestSucceed() {
        return (code == 200 || code == 422) && status.equals("success");
    }

    /**
     * 是否 Token 失效
     */
    public boolean isTokenFailure() {
        return code == 401;
    }
}
