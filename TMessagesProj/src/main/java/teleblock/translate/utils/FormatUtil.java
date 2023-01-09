package teleblock.translate.utils;

import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by LSD on 2021/12/22.
 * Desc
 */
public class FormatUtil {
    public static String paramsFormat(Map<String, String> params) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), "utf-8"));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (Exception e) {
        }
        return "";
    }
}
