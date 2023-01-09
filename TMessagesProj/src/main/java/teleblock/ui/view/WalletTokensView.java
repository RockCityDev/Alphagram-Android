package teleblock.ui.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.JsonUtils;
import com.blankj.utilcode.util.MapUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.body.JsonBody;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.http.request.HttpRequest;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.LayoutBaseRefreshBinding;
import org.telegram.messenger.databinding.ViewTokenEmptyBinding;
import org.web3j.protocol.core.Response;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.wallet.Balances;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.JsonRpc;
import teleblock.model.wallet.OasisToken;
import teleblock.model.wallet.OasisTokensPrice;
import teleblock.model.wallet.TTToken;
import teleblock.model.wallet.TTTokensPrice;
import teleblock.model.wallet.TokenBalance;
import teleblock.network.api.blockchain.TokenBalancesApi;
import teleblock.network.api.blockchain.oasis.OasisBalanceApi;
import teleblock.network.api.blockchain.oasis.OasisTokensApi;
import teleblock.network.api.blockchain.thundercore.TTTokensApi;
import teleblock.ui.activity.WalletHomeAct;
import teleblock.ui.adapter.TokenBalanceAdapter;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.StringUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.divider.CustomItemDecoration;

public class WalletTokensView extends FrameLayout implements OnRefreshListener, OnItemClickListener {

    private LayoutBaseRefreshBinding binding;
    private final WalletHomeAct walletHomeAct;
    private TokenBalanceAdapter tokenBalanceAdapter;
    private Map<String, Double> ttTokensPrice, oasisTokensPrice;
    private List<TokenBalance> tokenBalances;

    public WalletTokensView(WalletHomeAct walletHomeAct) {
        super(walletHomeAct.getParentActivity());
        this.walletHomeAct = walletHomeAct;
        initView();
        initData();
    }

    private void initView() {
        binding = LayoutBaseRefreshBinding.inflate(LayoutInflater.from(getContext()), this, true);

    }

    private void initData() {
        binding.refreshLayout.setEnableLoadMore(false);
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addItemDecoration(new CustomItemDecoration(RecyclerView.VERTICAL, Color.parseColor("#E6E6E6"), 1f));
        tokenBalanceAdapter = new TokenBalanceAdapter();
        tokenBalanceAdapter.setEmptyView(createEmptyView());
        tokenBalanceAdapter.getEmptyLayout().setVisibility(View.GONE);
        tokenBalanceAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(tokenBalanceAdapter);
        binding.refreshLayout.autoRefresh();
    }

    private View createEmptyView() {
        ViewTokenEmptyBinding binding = ViewTokenEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmptyToken.setText(LocaleController.getString("wallet_home_token_empty_text", R.string.wallet_home_token_empty_text));
        return binding.getRoot();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        loadData();
    }

    private void loadData() {
        tokenBalances = new ArrayList<>();
        walletHomeAct.binding.tvTotalAmount.setText("US$" + StringUtil.formatPrice(0, true));
        WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = MMKVUtil.currentChainConfig();
        if (chainType == null) return;
        String symbol = chainType.getMain_currency_name();
        HttpRequest httpRequest = null;
        if ("TT".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.post(new ApplicationLifecycle())
                    .api(new TTTokensApi())
                    .body(new JsonBody(TTTokensApi.createJson(walletHomeAct.address)));
            // 请求币种单价
            ttTokensPrice = new HashMap<>();
            WalletUtil.requestMainCoinPrice("thunder-token", new WalletUtil.RequestCoinPriceListener() {
                @Override
                public void requestEnd() {
                    getTTTokensPrice();
                }

                @Override
                public void requestError(String msg) {
                }

                @Override
                public void requestSuccessful(CurrencyPriceEntity resultData) {
                    ttTokensPrice.put("TT", resultData.getUsd());
                }
            });
        } else if ("ROSE".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.get(new ApplicationLifecycle())
                    .api(new OasisTokensApi()
                            .setAddress(walletHomeAct.address));
            // 请求币种单价
            oasisTokensPrice = new HashMap<>();
            WalletUtil.requestMainCoinPrice("oasis-network", new WalletUtil.RequestCoinPriceListener() {
                @Override
                public void requestEnd() {
                    getOasisTokensPrice();
                }

                @Override
                public void requestError(String msg) {
                }

                @Override
                public void requestSuccessful(CurrencyPriceEntity resultData) {
                    oasisTokensPrice.put("ROSE", resultData.getUsd());
                }
            });
            getOasisBalance();
        } else {
            httpRequest = EasyHttp.get(new ApplicationLifecycle())
                    .api(new TokenBalancesApi()
                            .setAddresses(walletHomeAct.address)
                            .setNetwork(symbol));
        }
        httpRequest.request(new OnHttpListener<String>() {
            @Override
            public void onSucceed(String result) {
                double totalAmount = 0;
                if ("TT".equalsIgnoreCase(symbol)) {
                    List<JsonRpc> jsonRpcList = JsonUtil.parseJsonToList(result, JsonRpc.class);
                    List<TTToken> tokenList = MMKVUtil.getTTTokens();
                    // 添加主币数据
                    TTToken ttToken = new TTToken();
                    ttToken.setImage("https://ttswap.space/static/media/tt.e15cb968.png");
                    ttToken.setSymbol("TT");
                    ttToken.setDecimals(18);
                    tokenList.add(0, ttToken);
                    List<TokenBalance> finalTokenBalances = tokenBalances;
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
                            if (ttTokensPrice != null && ttTokensPrice.get(item.getSymbol()) != null) {
                                item.setPrice(ttTokensPrice.get(item.getSymbol()));
                            }
                            finalTokenBalances.add(TokenBalance.parse(item));
                        }
                    });
                    // 过滤掉没余额的代币
                    CollectionUtils.filter(tokenBalances, new CollectionUtils.Predicate<TokenBalance>() {
                        @Override
                        public boolean evaluate(TokenBalance item) {
                            return Double.parseDouble(item.balance) > 0;
                        }
                    });
                    // 计算总资产
                    for (TokenBalance tokenBalance : tokenBalances) {
                        totalAmount += tokenBalance.balanceUSD;
                    }
                } else if ("ROSE".equalsIgnoreCase(symbol)) {
                    List<OasisToken> tokenList = JsonUtil.parseJsonToList(JsonUtils.getString(result, "result"), OasisToken.class);
                    for (OasisToken oasisToken : tokenList) {
                        if (!"ERC-20".equals(oasisToken.getType())) continue;
                        if (oasisTokensPrice != null && oasisTokensPrice.get(oasisToken.getSymbol()) != null) {
                            oasisToken.setPrice(oasisTokensPrice.get(oasisToken.getSymbol()));
                        }
                        String drawableName = "ic_os_" + oasisToken.getSymbol().toLowerCase();
                        if (ResourceUtils.getDrawableIdByName(drawableName) > 0) {
                            oasisToken.setImageRes(ResourceUtils.getDrawableIdByName(drawableName));
                        }
                        tokenBalances.add(TokenBalance.parse(oasisToken));
                    }
                    // 计算总资产
                    for (TokenBalance tokenBalance : tokenBalances) {
                        totalAmount += tokenBalance.balanceUSD;
                    }
                } else {
                    Balances balances = Balances.parse(result, walletHomeAct.address.toLowerCase());
                    if (balances != null) {
                        tokenBalances = TokenBalance.parse(balances);
                        totalAmount = balances.value;
                    }
                }
                tokenBalanceAdapter.setList(tokenBalances);
                walletHomeAct.binding.tvTotalAmount.setText("US$" + StringUtil.formatPrice(totalAmount, true));
                binding.refreshLayout.finishRefresh();
                if (tokenBalanceAdapter.getData().isEmpty()) {
                    tokenBalanceAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFail(Exception e) {
                binding.refreshLayout.finishRefresh();
                tokenBalanceAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
            }
        });
    }

    private void getTTTokensPrice() {
        OkHttpUtils.get().url("https://ttswap.space/api/tokens").build().execute(new StringCallback() {
            @Override
            public void onResponse(String response, int id) {
                TTTokensPrice result = JsonUtil.parseJsonToBean(response, TTTokensPrice.class);
                if (result != null) {
                    for (TTTokensPrice.DataBean.TokenListBean token : result.getData().getTokenList()) {
                        ttTokensPrice.put(token.getSymbol(), token.getPrice());
                    }
                    double totalAmount = 0;
                    for (TokenBalance tokenBalance : tokenBalances) {
                        if (ttTokensPrice.get(tokenBalance.symbol) != null) {
                            tokenBalance.price = ttTokensPrice.get(tokenBalance.symbol);
                            tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
                            totalAmount += tokenBalance.balanceUSD;
                        }
                    }
                    tokenBalanceAdapter.setList(tokenBalances);
                    walletHomeAct.binding.tvTotalAmount.setText("US$" + StringUtil.formatPrice(totalAmount, true));
                }
            }

            @Override
            public void onError(Call call, Exception e, int id) {

            }
        });
    }

    private void getOasisTokensPrice() {
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
                    double totalAmount = 0;
                    for (TokenBalance tokenBalance : tokenBalances) {
                        if (oasisTokensPrice.get(tokenBalance.symbol) != null) {
                            tokenBalance.price = oasisTokensPrice.get(tokenBalance.symbol);
                            tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
                            totalAmount += tokenBalance.balanceUSD;
                        }
                    }
                    tokenBalanceAdapter.setList(tokenBalances);
                    walletHomeAct.binding.tvTotalAmount.setText("US$" + StringUtil.formatPrice(totalAmount, true));
                }
            }

            @Override
            public void onError(Call call, Exception e, int id) {

            }
        });
    }

    private void getOasisBalance() {
        EasyHttp.get(new ApplicationLifecycle())
                .api(new OasisBalanceApi()
                        .setAddress(walletHomeAct.address))
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
                            tokenBalances.add(0, TokenBalance.parse(oasisToken));
                            // 计算总资产
                            double totalAmount = 0;
                            for (TokenBalance tokenBalance : tokenBalances) {
                                totalAmount += tokenBalance.balanceUSD;
                            }
                            tokenBalanceAdapter.setList(tokenBalances);
                            walletHomeAct.binding.tvTotalAmount.setText("US$" + StringUtil.formatPrice(totalAmount, true));
                        }
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
    }

}