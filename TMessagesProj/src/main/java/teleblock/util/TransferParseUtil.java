package teleblock.util;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import teleblock.config.AppConfig;

/**
 * Time:2022/9/9
 * Author:Perry
 * Description：转账解析工具类
 */
public class TransferParseUtil {
    private static final String PARSE = "$Teleblock$";

    /**
     * 规则拼接
     * 开头固定：$Teleblock$ ,分割
     * 发送人tgid
     * 接受人tgid
     * 币种单位
     * 交易成功hash值
     * 发送人的钱包地址
     * 接收人的钱包地址
     * 转账金额
     * gass费用，不需要可为0
     * 转账金额+gas金额的总金额
     * 总金额的价值 美元
     * 选择的链id
     * 通过ase加密 key：1tgvd8gAES89KEY2
     */
    public static String setParseStr(
            long fromUserId,
            long toUserId,
            String coinType,
            String hash,
            String fromWalletAddress,
            String toWalletAddress,
            BigDecimal moneyNum,
            BigDecimal gassFee,
            BigDecimal totalMoney,
            BigDecimal totalMoneyDoller,
            long chainId
    ) {
        StringBuffer sb = new StringBuffer(PARSE);
        sb.append(",").append(fromUserId)
                .append(",").append(toUserId)
                .append(",").append(coinType)
                .append(",").append(hash)
                .append(",").append(fromWalletAddress)
                .append(",").append(toWalletAddress)
                .append(",").append(moneyNum.toPlainString())
                .append(",").append(gassFee.toPlainString())
                .append(",").append(totalMoney.toPlainString())
                .append(",").append(totalMoneyDoller.toPlainString())
                .append(",").append(chainId)
        ;

        //加密
        byte[] encryptByte = EncryptUtils.encryptAES(ConvertUtils.string2Bytes(sb.toString()), AppConfig.ENCRYCONFIG.key, "AES/CBC/PKCS5Padding", AppConfig.ENCRYCONFIG.iv);
        return ConvertUtils.bytes2HexString(encryptByte);
    }

    /**
     * 解析
     * @param parse
     * @return
     */
    public static List<String> parse(String parse) {
        if (!isNumeric(parse)) {
            return new ArrayList<>();
        }

        //解密
        byte[] decryptByte = EncryptUtils.decryptHexStringAES(parse, AppConfig.ENCRYCONFIG.key, "AES/CBC/PKCS5Padding", AppConfig.ENCRYCONFIG.iv);
        if (decryptByte == null) return new ArrayList<>();

        String decryptString = ConvertUtils.bytes2String(decryptByte);//解密后的字符串
        List<String> parseLiset = Arrays.asList(decryptString.split(","));
        return new ArrayList<>(parseLiset);
    }

    /***
     * 是不是转账消息
     * @param parse
     * @return
     */
    public static boolean isTransferMsg(long messageFromUserId, String parse) {
        List<String> parseValus = parse(parse);
        return !parseValus.isEmpty() && parseValus.get(0).equals(PARSE) && String.valueOf(messageFromUserId).equals(parseValus.get(1));
    }

    /**
     * 判断是不是hexstring
     * @param value
     * @return
     */
    public static boolean isNumeric(String value) {
        String regex = "-?[0-9a-fA-F]+";
        return value.matches(regex);
    }
}