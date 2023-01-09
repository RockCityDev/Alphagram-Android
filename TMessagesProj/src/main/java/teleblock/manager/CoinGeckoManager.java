package teleblock.manager;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.coingecko.constant.Currency;
import com.coingecko.domain.Coins.CoinFullData;
import com.coingecko.domain.Coins.CoinList;
import com.coingecko.domain.Coins.MarketChart;
import com.coingecko.impl.CoinGeckoApiClientImpl;
import com.google.android.exoplayer2.util.Log;

import java.util.List;
import java.util.Map;

import teleblock.util.TGLog;
import timber.log.Timber;

/**
 * 创建日期：2022/6/9
 * 描述：https://www.coingecko.com/zh/api/documentation
 */
public class CoinGeckoManager {

    private static CoinGeckoManager instance;
    private CoinGeckoApiClientImpl client;

    public CoinGeckoManager() {
        client = new CoinGeckoApiClientImpl();
    }

    public static CoinGeckoManager getInstance() {
        if (instance == null) {
            synchronized (CoinGeckoManager.class) {
                if (instance == null) {
                    instance = new CoinGeckoManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取所有货币列表
     */
    public void getCoinList(Callback<List<CoinList>> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<List<CoinList>>() {
            @Override
            public List<CoinList> doInBackground() throws Throwable {
                return client.getCoinList();
            }

            @Override
            public void onSuccess(List<CoinList> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t);
            }
        });
    }

    /**
     * 获取货币的价格（查询多个则以逗号分隔）
     */
    public void getPrice(String ids, boolean includeOther, Callback<Map<String, Map<String, Double>>> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<Map<String, Map<String, Double>>>() {
            @Override
            public Map<String, Map<String, Double>> doInBackground() throws Throwable {
                return client.getPrice(ids, Currency.USD, includeOther, includeOther, includeOther, includeOther);
            }

            @Override
            public void onSuccess(Map<String, Map<String, Double>> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t);
            }
        });
    }

    /**
     * 获取货币的当前数据（名称、价格、市场……包括交易所代码）
     */
    public void getCoinById(String id, Callback<CoinFullData> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<CoinFullData>() {
            @Override
            public CoinFullData doInBackground() throws Throwable {
                return client.getCoinById(id);
            }

            @Override
            public void onSuccess(CoinFullData result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t);
            }
        });
    }

    /**
     * 获取历史市场数据，包括价格、市值和 24 小时交易量（粒度自动）
     * <p>
     * 数据粒度是自动的（无法调整）
     * 从当前时间开始 1 天 = 5 分钟间隔数据
     * 从当前时间开始 1 - 90 天 = 每小时数据
     * 从当前时间起 90 天以上 = 每日数据 (00:00 UTC)
     */
    public void getCoinMarketChartById(String id, Integer day, Callback<MarketChart> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<MarketChart>() {
            @Override
            public MarketChart doInBackground() throws Throwable {
                return client.getCoinMarketChartById(id, Currency.USD, day);
            }

            @Override
            public void onSuccess(MarketChart result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t);
            }
        });
    }


    public abstract static class Callback<T> {

        public abstract void onSuccess(T data);

        public void onError(Throwable t) {
            Timber.e(t);
        }
    }
}