package teleblock.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time:2022/6/20
 * Author:Perry
 * Description：字符串工具类
 */
public class StringUtil {

    /**
     * 判断字符串是不是json
     *
     * @param str
     * @return
     */
    public static boolean isJson(String str) {
        try {
            new Gson().fromJson(str, Object.class);
            return true;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    /**
     * 格式化数字
     *
     * @param number
     * @param fractionDigits
     * @return
     */
    public static String formatNumber(double number, int fractionDigits) {
        DecimalFormat decimalFormat = new DecimalFormat();
        //设置最大小数位数
        decimalFormat.setMaximumFractionDigits(fractionDigits);
        //设置分组大小，也就是显示逗号的位置
//        decimalFormat.setGroupingSize(3);
        //设置四舍五入的模式
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat.format(number);
    }

    /**
     * 格式化金额千分位
     *
     * @param price
     * @param halfUp
     * @return
     */
    public static String formatPrice(double price, boolean halfUp) {
        return num2thousand00(getBigDecimal2(price, halfUp));
    }

    /**
     * 字符串 千位符  保留两位小数点后两位
     *
     * @param num
     * @return
     */
    public static String num2thousand00(String num) {
        String numStr = "";
        if (TextUtils.isEmpty(num)) {
            return numStr;
        }
        NumberFormat nf = NumberFormat.getInstance();
        try {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            numStr = df.format(nf.parse(num));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return numStr;
    }

    /**
     * 保留两位小数 默认
     * true  4舍5入   false 正常保留
     */
    public static String getBigDecimal2(double num, boolean flag) {
        String allStringMoney = "";//最终取到的结果
        if (num == 0) {
            return "0.00";
        }
        if (flag) {
            DecimalFormat decimalFormat = new DecimalFormat(".00"); //取小数点后面两位
            //在方法体内加入下面的代码，便取到了四舍五入后的结果
            allStringMoney = decimalFormat.format(num);//四舍五入
        } else {
            allStringMoney = String.format("%.2f", num - 0.005);
        }
        return allStringMoney;
    }


    public static int String_length(String value) {
        if (TextUtils.isEmpty(value)) return 0;
        //去除空格、回车、换行符、制表符
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(value);
        value = m.replaceAll("");

        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }

    /**
     * 转换代币单位
     */
    public static String parseToken(String tokenNum, int tokenDecimal) {
        if (TextUtils.isEmpty(tokenNum)) tokenNum = "0";
        BigDecimal bigDecimal = new BigDecimal(tokenNum);
        BigDecimal result = bigDecimal.divide(new BigDecimal(Math.pow(10, tokenDecimal)));
        return result.toString();
    }

    /**
     * 判断是不是纯数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        return str.matches("[0-9]+");
    }
}
