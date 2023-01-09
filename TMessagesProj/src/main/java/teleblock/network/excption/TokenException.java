package teleblock.network.excption;

import com.hjq.http.exception.HttpException;

/**
 * Time:2022/6/20
 * Author:Perry
 * Description：Token 失效异常
 */
public final class TokenException extends HttpException {

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}