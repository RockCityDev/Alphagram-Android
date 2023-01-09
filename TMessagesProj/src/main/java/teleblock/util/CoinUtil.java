package teleblock.util;

import com.coingecko.domain.Coins.CoinList;

import java.util.ArrayList;
import java.util.List;

import teleblock.config.AppConfig;

/**
 * Time:2022/6/30
 * Author:Perry
 * Description：货币相关工具类
 */
//public class CoinUtil {
//
//    /***
//     * 存储数据到本地
//     * @param keywords
//     * @param coinLists
//     */
//    public static void saveCoinData(List<String> keywords, List<CoinList> coinLists) {
//        MMKVUtil.saveValue(AppConfig.MkKey.COIN_KEYWORDS, JsonUtil.parseObjToJson(keywords));
//        MMKVUtil.saveValue(AppConfig.MkKey.COIN_LIST, JsonUtil.parseObjToJson(coinLists));
//    }
//
//    /**
//     * 获取存储到货币列表数据
//     * @return
//     */
//    public static List<CoinList> getCoinList() {
//        List<CoinList> data;
//        String localJson = MMKVUtil.getString(AppConfig.MkKey.COIN_LIST);
//        if (localJson.isEmpty()) {
//            data = new ArrayList<>();
//        } else {
//            data = JsonUtil.parseJsonToList(localJson, CoinList.class);
//        }
//        return data;
//    }
//
//    /**
//     * 获取货币关键词
//     * @return
//     */
//    public static List<String> getCoinKeywords() {
//        List<String> data;
//        String localJson = MMKVUtil.getString(AppConfig.MkKey.COIN_KEYWORDS);
//        if (localJson.isEmpty()) {
//            data = new ArrayList<>();
//        } else {
//            data = JsonUtil.parseJsonToList(localJson, String.class);
//        }
//
//        return data;
//    }
//}
