package teleblock.network.excption;

import androidx.annotation.NonNull;

import com.hjq.http.exception.HttpException;

import teleblock.network.BaseBean;

/**
 * Time:2022/6/20
 * Author:Perry
 * Description： 返回结果异常
 */
public final class ResultException extends HttpException {

    private final BaseBean<?> mData;

    public ResultException(String message, BaseBean<?> data) {
        super(message);
        mData = data;
    }

    public ResultException(String message, Throwable cause, BaseBean<?> data) {
        super(message, cause);
        mData = data;
    }

    @NonNull
    public BaseBean<?> getHttpData() {
        return mData;
    }
}