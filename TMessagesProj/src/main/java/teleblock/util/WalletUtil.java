package teleblock.util;


import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.JsonUtils;
import com.blankj.utilcode.util.MapUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.body.JsonBody;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.http.request.GetRequest;
import com.hjq.http.request.HttpRequest;
import com.hjq.http.request.PostRequest;
import com.tokenpocket.opensdk.base.TPListener;
import com.tokenpocket.opensdk.base.TPManager;
import com.tokenpocket.opensdk.simple.model.Authorize;
import com.tokenpocket.opensdk.simple.model.Blockchain;
import com.tokenpocket.opensdk.simple.model.Transfer;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.web3j.protocol.core.Response;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import teleblock.blockchain.BlockchainConfig;
import teleblock.blockchain.WCSessionManager;
import teleblock.blockchain.Web3TransactionUtils;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.LoginDataResult;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.ui.MyCoinListData;
import teleblock.model.wallet.Balances;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.GasFeeEntity;
import teleblock.model.wallet.JsonRpc;
import teleblock.model.wallet.OasisToken;
import teleblock.model.wallet.OasisTokensPrice;
import teleblock.model.wallet.TTToken;
import teleblock.model.wallet.TTTokensPrice;
import teleblock.model.wallet.TokenBalance;
import teleblock.network.BaseBean;
import teleblock.network.api.BindWalletApi;
import teleblock.network.api.CurrencyPriceApi;
import teleblock.network.api.RequestWalletNetworkConfig;
import teleblock.network.api.blockchain.TokenBalancesApi;
import teleblock.network.api.blockchain.ethereum.EthBalanceApi;
import teleblock.network.api.blockchain.ethereum.EthGasLimitApi;
import teleblock.network.api.blockchain.ethereum.EthGasfeeApi;
import teleblock.network.api.blockchain.oasis.OasisBalanceApi;
import teleblock.network.api.blockchain.oasis.OasisGasLimitApi;
import teleblock.network.api.blockchain.oasis.OasisGasPriceApi;
import teleblock.network.api.blockchain.oasis.OasisTokensApi;
import teleblock.network.api.blockchain.polygon.PolygonBalanceApi;
import teleblock.network.api.blockchain.polygon.PolygonGasLimitApi;
import teleblock.network.api.blockchain.polygon.PolygonGasfeeApi;
import teleblock.network.api.blockchain.thundercore.TTBalanceApi;
import teleblock.network.api.blockchain.thundercore.TTGasLimitApi;
import teleblock.network.api.blockchain.thundercore.TTGasPriceApi;
import teleblock.network.api.blockchain.thundercore.TTTokensApi;
import teleblock.ui.dialog.ErrorWalletAddressDialog;

/**
 * Time:2022/9/14
 * Author:Perry
 * Description：钱包工具类
 */
public class WalletUtil {

    private static Map<String, CurrencyPriceEntity> coinPriceMap = new HashMap<>(); //币单价Map

    public interface RequestStateListener {
        void requestEnd();

        void requestError(String msg);

        void requestSuccessful(String data);
    }

    public interface RequestGasFeeListener {
        void requestEnd();

        void requestError(String msg);

        void requestSuccessful(GasFeeEntity resultData, BigInteger limit);
    }

    public interface RequestCoinPriceListener {
        void requestEnd();

        void requestError(String msg);

        void requestSuccessful(CurrencyPriceEntity resultData);
    }

    /**
     * 链接钱包
     */
    public static void walletConnect(String pkg) {
        if (!AppUtils.isAppInstalled(pkg)) {
            ToastUtils.showLong(LocaleController.getString("wallet_home_tip_install", R.string.wallet_home_tip_install));
            return;
        }
        WalletNetworkConfigEntity walletNetworkConfig = MMKVUtil.getWalletNetworkConfigEntity();
        List<Blockchain> blockchains = new ArrayList<>();
        //blockchains.add(new Blockchain("ethereum", "1"));
        if (walletNetworkConfig != null) {
            List<WalletNetworkConfigEntity.WalletNetworkConfigChainType> chainTypes = walletNetworkConfig.getChainType();
            if (chainTypes != null) {
                for (WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType : chainTypes) {
                    blockchains.add(new Blockchain("ethereum", chainType.getId() + ""));
                }
            }
        }
        if (BlockchainConfig.PKG_TOKEN_POCKET.equals(pkg)) {
            Authorize authorize = new Authorize();
            authorize.setBlockchains(blockchains);
            authorize.setDappName(AppUtils.getAppName());
            authorize.setDappIcon("https://alphagram.app/");
            authorize.setActionId(String.valueOf(System.currentTimeMillis()));
            TPManager.getInstance().authorize(ApplicationLoader.applicationContext, authorize, new TPListener() {
                @Override
                public void onSuccess(String s) {
                    Log.d("authorize-->", s);
                    String address = JsonUtils.getString(s, "wallet");
                    if (!TextUtils.isEmpty(address)) {
                        walletBind(BlockchainConfig.PKG_TOKEN_POCKET, address, 1);
                    }
                }

                @Override
                public void onError(String s) {
                    ToastUtils.showLong(s);
                }

                @Override
                public void onCancel(String s) {
                }
            });
        } else {
            WCSessionManager.getInstance().pkg = pkg;
            WCSessionManager.getInstance().resetSession();
        }
    }

    /**
     * 跳转钱包
     */
    public static void goToWallet() {
        String pkg = MMKVUtil.connectedWalletPkg();
        if (!AppUtils.isAppInstalled(pkg)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("wc:"));
        intent.setPackage(pkg);
        ActivityUtils.startActivity(intent);
    }

    /**
     * 钱包绑定
     */
    public static void walletBind(String pkg, String address, long chainId) {
        AppUtils.launchApp(AppUtils.getAppPackageName());
        EasyHttp.post(new ApplicationLifecycle())
                .api(new BindWalletApi()
                        .setWallet_type(BlockchainConfig.getWalletTypeByPkg(pkg))
                        .setWallet_address(address)
                        .setChain_id(chainId))
                .request(new OnHttpListener<BaseBean<LoginDataResult.UserEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<LoginDataResult.UserEntity> result) {
//                            LoginManager.saveUserInfo(result.getData());
                        MMKVUtil.connectedWalletAddress(address);
                        MMKVUtil.connectedWalletPkg(pkg);
                        MMKVUtil.setNftphotoIfShow(1);
                        //请求自己的nft数据，存到库里面
                        TelegramUtil.getUserNftData(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser().id, null);
                        EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CONNECT_APPROVED));
                    }

                    @Override
                    public void onFail(Exception e) {
                    }
                });

    }

    /**
     * 钱包支付环境验证
     *
     * @param chainId
     * @param runnable
     */
    public static void walletPayVerify(long chainId, Runnable runnable) {
        WCSessionManager.getInstance().getAccounts(new WCSessionManager.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> data) {
                String address = CollectionUtils.find(data, item -> item.equals(MMKVUtil.connectedWalletAddress()));
                if (address == null) {
                    AppUtils.launchApp(AppUtils.getAppPackageName());
                    new ErrorWalletAddressDialog(ActivityUtils.getTopActivity(), ErrorWalletAddressDialog.TYPE_ERROR_BIND_ADDRESS) {
                        @Override
                        public void onConfirm() {
                            super.onConfirm();
                            WalletUtil.goToWallet();
                        }
                    }.show();
                    return;
                }

                WCSessionManager.getInstance().getChainId(true, new WCSessionManager.Callback<String>() {
                    @Override
                    public void onSuccess(String data) {
                        String chainIdStr = Numeric.toBigInt(data).toString();
                        if (!chainIdStr.equals(String.valueOf(chainId))) {
                            WCSessionManager.getInstance().switchNetwork(String.valueOf(chainId), new WCSessionManager.Callback<String>() {
                                @Override
                                public void onSuccess(String data) {
                                    runnable.run();
                                }
                            });
                        } else {
                            runnable.run();
                        }
                    }
                });
            }

            @Override
            public void onError(String msg) {
                AppUtils.launchApp(AppUtils.getAppPackageName());
                super.onError(msg);
            }
        });
    }

    /**
     * 发起交易
     */
    public static void sendTransaction(long chainId, String toAddress, String gasPrice, String gasLimit, String amount,
                                       String contractAddress, int decimal, String symbol, WCSessionManager.Callback<String> callback) {
        if (BlockchainConfig.PKG_TOKEN_POCKET.equals(MMKVUtil.connectedWalletPkg())) {
            Transfer transfer = new Transfer();
            List<Blockchain> blockchains = new ArrayList<>();
            blockchains.add(new Blockchain("ethereum", String.valueOf(chainId)));
            transfer.setBlockchains(blockchains);
            transfer.setDappName(AppUtils.getAppName());
            transfer.setDappIcon("https://alphagram.app/");
            transfer.setActionId(String.valueOf(System.currentTimeMillis()));
            transfer.setFrom(MMKVUtil.connectedWalletAddress());
            transfer.setTo(toAddress);
            transfer.setAmount(Double.parseDouble(amount));
            transfer.setContract(contractAddress);
            transfer.setSymbol(symbol);
            transfer.setDecimal(decimal);
            TPManager.getInstance().transfer(ApplicationLoader.applicationContext, transfer, new TPListener() {
                @Override
                public void onSuccess(String s) {
                    Log.d("transfer-->", s);
                    String hash = JsonUtils.getString(s, "txID");
                    callback.onSuccess(hash);
                }

                @Override
                public void onError(String s) {
                    ToastUtils.showLong(s);
                }

                @Override
                public void onCancel(String s) {
                }
            });
        } else {
            String to = toAddress;
            String data = "";
            if (!TextUtils.isEmpty(contractAddress)) {
                to = contractAddress;
                data = Web3TransactionUtils.encodeTransferData(
                        toAddress,
                        new BigDecimal(WalletUtil.toWei(amount, decimal)).toBigInteger()
                );
            }
            WCSessionManager.getInstance().sendTransaction(to, gasPrice, gasLimit, amount, decimal, data, new WCSessionManager.Callback<String>() {
                @Override
                public void onSuccess(String data) {
                    callback.onSuccess(data);
                }
            });
        }
    }

    /**
     * 请求这个链下面的主币信息，并请求这个钱包下面的所有代币信息
     *
     * @param walletAddress
     * @param data
     * @param mMyCoinListDataList
     */
    public static void requestWalletCoinBalance(
            String walletAddress,
            WalletNetworkConfigEntity.WalletNetworkConfigChainType data,
            List<MyCoinListData> mMyCoinListDataList,
            Runnable runnable
    ) {
        mMyCoinListDataList.clear();//先清除所有的老数据
        //获取主币
        WalletNetworkConfigEntity.WalletNetworkConfigEntityItem mainCurrencyData = null;
        //遍历当前币种下面所有的币种数据
        for (WalletNetworkConfigEntity.WalletNetworkConfigEntityItem selectorCoinData : data.getCurrency()) {
            if (selectorCoinData.isIs_main_currency()) { //获取主币
                mainCurrencyData = selectorCoinData;
            }
        }

        if (mainCurrencyData == null) { //判空
            runnable.run();
            return;
        }
        WalletNetworkConfigEntity.WalletNetworkConfigEntityItem finalMainCurrencyData = mainCurrencyData;

        //eth和polygon链用第三方的接口获取
        if ("ETH".equalsIgnoreCase(data.getMain_currency_name()) || "MATIC".equalsIgnoreCase(data.getMain_currency_name())) {
            EasyHttp.get(new ApplicationLifecycle()).api(new TokenBalancesApi().setAddresses(walletAddress).setNetwork(data.getMain_currency_name()))
                    .request(new OnHttpListener<String>() {
                        @Override
                        public void onSucceed(String result) {
                            Balances balances = Balances.parse(result, walletAddress.toLowerCase());
                            if (balances != null) {
                                if (!CollectionUtils.isEmpty(balances.products)) {
                                    if (!CollectionUtils.isEmpty(balances.products.get(0).assets)) {
                                        for (Balances.ProductsEntity.AssetsEntity assetsEntity : balances.products.get(0).assets) {
                                            addCoinToList(
                                                    assetsEntity.symbol,
                                                    assetsEntity.displayProps.images.get(0),
                                                    new BigDecimal(assetsEntity.balance),
                                                    new BigDecimal(String.valueOf(assetsEntity.price)),
                                                    assetsEntity.decimals,
                                                    assetsEntity.symbol.equalsIgnoreCase(data.getMain_currency_name()),
                                                    assetsEntity.address,
                                                    mMyCoinListDataList
                                            );
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFail(Exception e) {
                            ToastUtils.showShort(LocaleController.getString("http_response_error", R.string.http_response_error));
                            TGLog.erro("WalletUtil：" + data.getName() + "链币种列表数据返回失败：" + e.getMessage());
                        }

                        @Override
                        public void onEnd(Call call) {
                            if (mMyCoinListDataList.isEmpty()) {
                                addCoinToList(
                                        finalMainCurrencyData.getName(),
                                        finalMainCurrencyData.getIcon(),
                                        new BigDecimal("0"),
                                        new BigDecimal("0"),
                                        finalMainCurrencyData.getDecimal(),
                                        true, "",
                                        mMyCoinListDataList
                                );
                            }
                            runnable.run();
                        }
                    });
            return;
        } else if ("TT".equalsIgnoreCase(data.getMain_currency_name())) {
            getTTTokenList(walletAddress, mMyCoinListDataList, runnable);
            return;
        } else if ("ROSE".equalsIgnoreCase(data.getMain_currency_name())) {
            getOasisTokenList(walletAddress, mMyCoinListDataList, runnable);
            return;
        }
    }

    /**
     * 获取TT链代币列表
     */
    private static void getTTTokenList(String walletAddress, List<MyCoinListData> coinListDataList, Runnable runnable) {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Map<String, Double> ttTokensPrice = new HashMap<>();
        new Thread(() -> {
            // 请求主币和代币列表
            EasyHttp.post(new ApplicationLifecycle())
                    .api(new TTTokensApi())
                    .body(new JsonBody(TTTokensApi.createJson(walletAddress)))
                    .request(new OnHttpListener<String>() {
                        @Override
                        public void onSucceed(String result) {
                            List<JsonRpc> jsonRpcList = JsonUtil.parseJsonToList(result, JsonRpc.class);
                            List<TTToken> tokenList = MMKVUtil.getTTTokens();
                            // 添加主币数据
                            TTToken ttToken = new TTToken();
                            ttToken.setImage("https://ttswap.space/static/media/tt.e15cb968.png");
                            ttToken.setSymbol("TT");
                            ttToken.setDecimals(18);
                            tokenList.add(0, ttToken);
                            CollectionUtils.forAllDo(tokenList, new CollectionUtils.Closure<TTToken>() {
                                @Override
                                public void execute(int index, TTToken item) {
                                    String balance;
                                    try {
                                        balance = Numeric.toBigInt(jsonRpcList.get(index).getResult()).toString();
                                    } catch (Exception e) {
                                        balance = "0";
                                    }
                                    item.setBalance(balance);
                                    if (ttTokensPrice.get(item.getSymbol()) != null) {
                                        item.setPrice(ttTokensPrice.get(item.getSymbol()));
                                    }
                                    addCoinToList(
                                            item.getSymbol(),
                                            item.getImage(),
                                            new BigDecimal(WalletUtil.parseToken(item.getBalance(), item.getDecimals())),
                                            new BigDecimal(String.valueOf(item.getPrice())),
                                            item.getDecimals(),
                                            TextUtils.isEmpty(item.getContractAddress()),
                                            item.getContractAddress(),
                                            coinListDataList
                                    );
                                }
                            });
                            // 过滤掉没余额的代币
                            CollectionUtils.filter(coinListDataList, new CollectionUtils.Predicate<MyCoinListData>() {
                                @Override
                                public boolean evaluate(MyCoinListData item) {
                                    return item.getBalance().doubleValue() > 0;
                                }
                            });
                        }

                        @Override
                        public void onFail(Exception e) {

                        }

                        @Override
                        public void onEnd(Call call) {
                            countDownLatch.countDown();
                        }
                    });
            // 请求主币单价
            requestMainCoinPrice("thunder-token", new WalletUtil.RequestCoinPriceListener() {
                @Override
                public void requestEnd() {
                    // 请求代币单价
                    OkHttpUtils.get().url("https://ttswap.space/api/tokens").build().execute(new StringCallback() {
                        @Override
                        public void onResponse(String response, int id) {
                            TTTokensPrice result = JsonUtil.parseJsonToBean(response, TTTokensPrice.class);
                            if (result != null) {
                                for (TTTokensPrice.DataBean.TokenListBean token : result.getData().getTokenList()) {
                                    ttTokensPrice.put(token.getSymbol(), token.getPrice());
                                }
                                for (MyCoinListData coinListData : coinListDataList) {
                                    if (ttTokensPrice.get(coinListData.getSymbol()) != null) {
                                        coinListData.setPrice(new BigDecimal(ttTokensPrice.get(coinListData.getSymbol())));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onAfter(int id) {
                            countDownLatch.countDown();
                        }
                    });
                }

                @Override
                public void requestError(String msg) {
                }

                @Override
                public void requestSuccessful(CurrencyPriceEntity resultData) {
                    ttTokensPrice.put("TT", resultData.getUsd());
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        }).start();
    }

    /**
     * 获取Oasis链代币列表
     */
    private static void getOasisTokenList(String walletAddress, List<MyCoinListData> coinListDataList, Runnable runnable) {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        Map<String, Double> oasisTokensPrice = new HashMap<>();
        List<OasisToken> oasisTokenList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 请求代币列表
                EasyHttp.get(new ApplicationLifecycle())
                        .api(new OasisTokensApi()
                                .setAddress(walletAddress))
                        .request(new OnHttpListener<String>() {
                            @Override
                            public void onSucceed(String result) {
                                List<OasisToken> tokenList = JsonUtil.parseJsonToList(JsonUtils.getString(result, "result"), OasisToken.class);
                                CollectionUtils.filter(tokenList, new CollectionUtils.Predicate<OasisToken>() {
                                    @Override
                                    public boolean evaluate(OasisToken item) {
                                        return "ERC-20".equals(item.getType());
                                    }
                                });
                                oasisTokenList.addAll(tokenList);
                            }

                            @Override
                            public void onFail(Exception e) {

                            }

                            @Override
                            public void onEnd(Call call) {
                                countDownLatch.countDown();
                            }
                        });
                // 请求主币单价
                WalletUtil.requestMainCoinPrice("oasis-network", new WalletUtil.RequestCoinPriceListener() {
                    @Override
                    public void requestEnd() {
                        // 请求代币单价
                        OkHttpUtils.get().url("https://app.yuzu-swap.com/api/prices").build().execute(new StringCallback() {
                            @Override
                            public void onResponse(String response, int id) {
                                OasisTokensPrice result = JsonUtil.parseJsonToBean(response, OasisTokensPrice.class);
                                if (result != null) {
                                    MapUtils.forAllDo(result.getData(), new MapUtils.Closure<String, Double>() {
                                        @Override
                                        public void execute(String key, Double value) {
                                            oasisTokensPrice.put(key, value);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {

                            }

                            @Override
                            public void onAfter(int id) {
                                countDownLatch.countDown();
                            }
                        });
                    }

                    @Override
                    public void requestError(String msg) {
                    }

                    @Override
                    public void requestSuccessful(CurrencyPriceEntity resultData) {
                        oasisTokensPrice.put("ROSE", resultData.getUsd());
                    }
                });
                // 请求主币余额
                EasyHttp.get(new ApplicationLifecycle())
                        .api(new OasisBalanceApi()
                                .setAddress(walletAddress))
                        .request(new OnHttpListener<Response>() {
                            @Override
                            public void onSucceed(Response result) {
                                if (result.getResult() instanceof String) {
                                    // 添加主币数据
                                    OasisToken oasisToken = new OasisToken();
                                    oasisToken.setImageRes(R.drawable.ic_os_rose);
                                    oasisToken.setSymbol("ROSE");
                                    oasisToken.setDecimals(18);
                                    oasisToken.setBalance((String) result.getResult());
                                    oasisTokenList.add(0, oasisToken);
                                }
                            }

                            @Override
                            public void onFail(Exception e) {
                            }

                            @Override
                            public void onEnd(Call call) {
                                countDownLatch.countDown();
                            }
                        });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        for (OasisToken oasisToken : oasisTokenList) {
                            if (oasisToken.getImageRes() == 0) {
                                String drawableName = "ic_os_" + oasisToken.getSymbol().toLowerCase();
                                if (ResourceUtils.getDrawableIdByName(drawableName) > 0) {
                                    oasisToken.setImageRes(ResourceUtils.getDrawableIdByName(drawableName));
                                } else {
                                    oasisToken.setImageRes(R.drawable.token_holder);
                                }
                            }
                            if (oasisTokensPrice.get(oasisToken.getSymbol()) != null) {
                                oasisToken.setPrice(oasisTokensPrice.get(oasisToken.getSymbol()));
                            }
                            addCoinToList(
                                    oasisToken.getSymbol(),
                                    oasisToken.getImageRes(),
                                    new BigDecimal(WalletUtil.parseToken(oasisToken.getBalance(), oasisToken.getDecimals())),
                                    new BigDecimal(String.valueOf(oasisToken.getPrice())),
                                    oasisToken.getDecimals(),
                                    TextUtils.isEmpty(oasisToken.getContractAddress()),
                                    oasisToken.getContractAddress(),
                                    coinListDataList
                            );
                        }
                        runnable.run();
                    }
                });
            }
        }).start();
    }

    /**
     * 添加 币种数据到集合
     *
     * @param symbol
     * @param icon
     * @param balance
     * @param price
     * @param decimal
     * @param isMainCoin
     * @param mMyCoinListDataList
     */
    private static void addCoinToList(
            String symbol,
            Object icon,
            BigDecimal balance,
            BigDecimal price,
            int decimal,
            boolean isMainCoin,
            String address,
            List<MyCoinListData> mMyCoinListDataList
    ) {
        MyCoinListData mainCoinData = new MyCoinListData();
        mainCoinData.setSymbol(symbol);
        if (icon instanceof String) {
            mainCoinData.setIcon((String) icon);
        } else {
            mainCoinData.setIconRes((Integer) icon);
        }
        mainCoinData.setBalance(balance);
        mainCoinData.setPrice(price);
        mainCoinData.setDecimal(decimal);
        mainCoinData.setIs_main_currency(isMainCoin);
        if (!isMainCoin) {
            mainCoinData.setContractAddress(address);
        }
        mMyCoinListDataList.add(mainCoinData);
    }

    /**
     * 请求钱包余额数据
     *
     * @param walletAddress
     * @param coinType
     * @param listener
     */
    public static void requestWalletBalanceData(String walletAddress, String coinType, RequestStateListener listener) {
        String RequestTag = "requestWalletBalanceData";
        EasyHttp.cancel(RequestTag);

        //钱包数据请求
        PostRequest walletPostBalanceRequest = null;
        GetRequest walletGetBalanceRequest = null;

        switch (coinType) {
            case "ETH":
                walletPostBalanceRequest = EasyHttp.post(new ApplicationLifecycle()).api(new EthBalanceApi().setAddress(walletAddress));
                break;

            case "MATIC":
                walletPostBalanceRequest = EasyHttp.post(new ApplicationLifecycle()).api(new PolygonBalanceApi().setAddress(walletAddress));
                break;

            case "TT":
                walletGetBalanceRequest = EasyHttp.get(new ApplicationLifecycle()).api(new TTBalanceApi().setAddress(walletAddress));
                break;

            case "ROSE":
                walletGetBalanceRequest = EasyHttp.get(new ApplicationLifecycle()).api(new OasisBalanceApi().setAddress(walletAddress));
                break;
        }

        HttpRequest<? extends HttpRequest<?>> httpRequest = walletGetBalanceRequest != null ? walletGetBalanceRequest : walletPostBalanceRequest;
        httpRequest
                .tag(RequestTag)
                .request(new OnHttpListener<String>() {
                    @Override
                    public void onSucceed(String result) {
                        try {
                            String balance = "0";//账户余额
                            if ("TT".equals(coinType)) {
                                JSONArray jsonArray = new JSONArray(result);
                                if (jsonArray.length() > 0) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                                    balance = jsonObject.optString("balance");
                                }
                            } else {
                                JSONObject jsonObject = new JSONObject(result);
                                balance = jsonObject.optString("result");
                            }
                            listener.requestSuccessful(balance);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showShort(LocaleController.getString("http_response_error", R.string.http_response_error));
                        listener.requestError(e.getMessage());
                    }

                    @Override
                    public void onEnd(Call call) {
                        listener.requestEnd();
                    }
                });
    }

    /**
     * 请求主币单价行情
     */
    public static void requestMainCoinPrice(String coinId, RequestCoinPriceListener listener) {
        CurrencyPriceEntity coinPrice = coinPriceMap.get(coinId);
        if (coinPrice != null) {
            // 一分钟内不请求接口
            if (TimeUtils.getTimeSpanByNow(coinPrice.lastTime, TimeConstants.SEC) > -60) {
                listener.requestSuccessful(coinPrice);
                listener.requestEnd();
                return;
            }
        }

        EasyHttp.post(new ApplicationLifecycle())
                .api(new CurrencyPriceApi().setCoin_id(coinId))
                .request(new OnHttpListener<BaseBean<CurrencyPriceEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<CurrencyPriceEntity> result) {
                        if (result.getData() != null) {
                            CurrencyPriceEntity currencyPriceEntity = result.getData();
                            currencyPriceEntity.setLastTime(System.currentTimeMillis());
                            currencyPriceEntity.setCoinName(coinId);
                            coinPriceMap.put(coinId, currencyPriceEntity);
                            listener.requestSuccessful(currencyPriceEntity);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showShort(LocaleController.getString("http_response_error", R.string.http_response_error));
                        listener.requestError(e.getMessage());
                        listener.requestEnd();
                    }

                    @Override
                    public void onEnd(Call call) {
                        listener.requestEnd();
                    }
                });
    }

    /**
     * 请求gas费用 目前只有eth和polygon链的gas请求
     */
    public static void requestChainGasfee(long chainId, int decimal, String toAddress, String data, RequestGasFeeListener listener) {
        PostRequest gasFeeRequest = null;
        if (1 == chainId) {
            gasFeeRequest = EasyHttp.post(new ApplicationLifecycle()).api(new EthGasfeeApi());
        } else if (137 == chainId) {
            gasFeeRequest = EasyHttp.post(new ApplicationLifecycle()).api(new PolygonGasfeeApi());
        } else if (108 == chainId) {
            gasFeeRequest = EasyHttp.post(new ApplicationLifecycle()).api(new TTGasPriceApi()).body(new JsonBody(TTGasPriceApi.createJson()));
        } else if (42262 == chainId) {
            gasFeeRequest = EasyHttp.post(new ApplicationLifecycle()).api(new OasisGasPriceApi()).body(new JsonBody(OasisGasPriceApi.createJson()));
        }

        if (gasFeeRequest == null) {
            listener.requestEnd();
            return;
        }

        //gas费用请求
        gasFeeRequest.request(new OnHttpListener<String>() {
            @Override
            public void onSucceed(String result) {
                GasFeeEntity resultData = null;
                boolean goOn = false;
                if (1 == chainId || 137 == chainId) {
                    resultData = JsonUtil.parseJsonToBean(result, GasFeeEntity.class);
                    if (resultData.getStatus().equals("1")) {
                        goOn = true;
                    }
                } else {
                    try {
                        BigDecimal gasPrice = Convert.fromWei(Numeric.toBigInt(new JSONObject(result).optString("result")).toString(), Convert.Unit.GWEI);
                        resultData = new GasFeeEntity();
                        GasFeeEntity.GasFeeEntityResult gasFeeEntityResult = new GasFeeEntity.GasFeeEntityResult();
                        gasFeeEntityResult.setProposeGasPrice(gasPrice.toString());
                        resultData.setResult(gasFeeEntityResult);
                        goOn = true;
                    } catch (Exception e) {
                    }
                }
                GasFeeEntity fResultData = resultData;
                if (goOn) {
                    //获取gaslimit的值
                    PostRequest gasLimitRequest = null;
                    if (1 == chainId) {
                        gasLimitRequest = EasyHttp.post(new ApplicationLifecycle()).api(new EthGasLimitApi().setData(data).setTo(toAddress));
                    } else if (137 == chainId) {
                        gasLimitRequest = EasyHttp.post(new ApplicationLifecycle()).api(new PolygonGasLimitApi().setData(data).setTo(toAddress));
                    } else if (108 == chainId) {
                        gasLimitRequest = EasyHttp.post(new ApplicationLifecycle()).api(new TTGasLimitApi()).body(new JsonBody(TTGasLimitApi.createJson(toAddress, data)));
                    } else if (42262 == chainId) {
                        gasLimitRequest = EasyHttp.post(new ApplicationLifecycle()).api(new OasisGasLimitApi()).body(new JsonBody(OasisGasLimitApi.createJson(toAddress, data)));
                    }

                    if (gasLimitRequest == null) {
                        listener.requestEnd();
                        return;
                    }

                    gasLimitRequest.request(new OnHttpListener<String>() {
                        @Override
                        public void onSucceed(String result) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                listener.requestSuccessful(fResultData, Numeric.toBigInt(jsonObject.optString("result")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(Exception e) {
                            ToastUtils.showShort(LocaleController.getString("http_response_error", R.string.http_response_error));
                            listener.requestError(e.getMessage());
                        }

                        @Override
                        public void onEnd(Call call) {
                            listener.requestEnd();
                        }
                    });
                }
            }

            @Override
            public void onFail(Exception e) {
                ToastUtils.showShort(LocaleController.getString("http_response_error", R.string.http_response_error));
                listener.requestError(e.getMessage());
                listener.requestEnd();
            }
        });
    }

    /**
     * 请求钱包配置数据
     *
     * @param runnable
     */
    public static void requestWalletNetworkConfigData(Runnable runnable) {
        //取消当前请求
        EasyHttp.cancel("requestWalletNetworkConfigData");

        //说明本地有数据
        if (!MMKVUtil.getWalletNetworkConfigEntity().getWalletType().isEmpty()) {
            if (runnable != null) runnable.run();
            return;
        }

        EasyHttp.post(new ApplicationLifecycle())
                .tag("requestWalletNetworkConfigData")
                .api(new RequestWalletNetworkConfig())
                .request(new OnHttpListener<BaseBean<WalletNetworkConfigEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<WalletNetworkConfigEntity> result) {
                        MMKVUtil.setWalletNetworkConfigData(result.getData());
                        if (runnable != null) runnable.run();
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showShort(LocaleController.getString("http_response_error", R.string.http_response_error));
                    }
                });
    }

    /**
     * 保留几位小数点
     *
     * @param bd
     * @param scale
     * @return
     */
    public static BigDecimal bigDecimalScale(BigDecimal bd, int scale) {
        return bd.setScale(scale, RoundingMode.HALF_DOWN).stripTrailingZeros();
    }

    /**
     * bd1 是否大于 bd2
     *
     * @param bd1
     * @param bd2
     * @return
     */
    public static boolean decimalCompareTo(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2) > 0;
    }


    /**
     * 币种 转换成 美元
     *
     * @param balance 币种余额
     * @param price   币种单价
     * @param scale   为0就是保留所有小数
     * @return
     */
    public static String toCoinPriceUSD(BigDecimal balance, BigDecimal price, int scale) {
        if (balance == null) {
            return "";
        }

        if (price == null) {
            return "";
        }

        if (scale == 0) {
            return " $" + balance.multiply(price).stripTrailingZeros().toPlainString();
        }

        return " $" + bigDecimalScale(balance.multiply(price), scale).stripTrailingZeros().toPlainString();
    }

    public static double toCoinPriceUSD(String balance, String price) {
        if (TextUtils.isEmpty(balance) || TextUtils.isEmpty(price)) {
            return 0;
        }
        return bigDecimalScale(new BigDecimal(balance).multiply(new BigDecimal(price)), 2).doubleValue();
    }

    /**
     * gas费用计算
     *
     * @param gasPrice
     * @return
     */
    public static BigDecimal gasFree(String gasPrice, String limit, BigDecimal mainCoinPrice, BigDecimal coinPrice) {
        BigDecimal gasLimit = new BigDecimal(gasPrice).multiply(new BigDecimal(limit));
        String wei = Convert.toWei(gasLimit.toPlainString(), Convert.Unit.GWEI).toPlainString();//转成单位wei
        String mainCoinNum = WalletUtil.parseToken(wei, 18);//主币个数

        //币种个数
        BigDecimal coinNum = new BigDecimal(mainCoinNum).multiply(mainCoinPrice).divide(coinPrice, BigDecimal.ROUND_HALF_UP);
        return coinNum.stripTrailingZeros();
    }

    /**
     * 格式化钱包地址
     */
    public static String formatAddress(String address) {
        if (TextUtils.isEmpty(address)) return "";
        if (address.length() < 8) return address;
        return address.substring(0, 8) + "..." + address.substring(address.length() - 4);
    }

    /**
     * 转换代币单位
     */
    public static String parseToken(String tokenNum, int tokenDecimal) {
        if (TextUtils.isEmpty(tokenNum)) tokenNum = "0";
        BigDecimal bigDecimal = new BigDecimal(tokenNum);
        BigDecimal result = bigDecimal.divide(new BigDecimal(Math.pow(10, tokenDecimal)));
        return result.stripTrailingZeros().toPlainString();
    }

    /**
     * 转wei
     */
    public static String toWei(String tokenNum, int tokenDecimal) {
        if (TextUtils.isEmpty(tokenNum)) tokenNum = "0";
        BigDecimal bigDecimal = new BigDecimal(tokenNum);
        BigDecimal result = bigDecimal.multiply(new BigDecimal(10).pow(tokenDecimal));
        return result.stripTrailingZeros().toPlainString();
    }
}