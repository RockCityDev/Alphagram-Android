package teleblock.ui.view;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.config.IRequestApi;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.LayoutBaseRefreshBinding;
import org.telegram.messenger.databinding.ViewTokenEmptyBinding;

import java.math.BigDecimal;
import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.EthTransactions;
import teleblock.model.wallet.TTTransactions;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.api.blockchain.ethereum.EthTransactionsApi;
import teleblock.network.api.blockchain.oasis.OasisTransactionsApi;
import teleblock.network.api.blockchain.polygon.PolygonTransactionsApi;
import teleblock.network.api.blockchain.thundercore.TTTransactionsApi;
import teleblock.ui.activity.WalletHomeAct;
import teleblock.ui.adapter.TransactionInfoAdapter;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;

public class WalletTransactionView extends FrameLayout implements OnRefreshListener, OnLoadMoreListener, OnItemClickListener {

    private LayoutBaseRefreshBinding binding;
    private WalletHomeAct walletHomeAct;
    private TransactionInfoAdapter transactionInfoAdapter;
    private int page;
    private String currentPrice;

    public WalletTransactionView(WalletHomeAct walletHomeAct) {
        super(walletHomeAct.getParentActivity());
        this.walletHomeAct = walletHomeAct;
        initView();
        initData();
    }

    private void initView() {
        binding = LayoutBaseRefreshBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        binding.recyclerView.addItemDecoration(new CustomItemDecoration(RecyclerView.VERTICAL, Color.parseColor("#E6E6E6"), 1f));
        transactionInfoAdapter = new TransactionInfoAdapter(walletHomeAct.address);
        transactionInfoAdapter.setEmptyView(createEmptyView());
        transactionInfoAdapter.getEmptyLayout().setVisibility(View.GONE);
        transactionInfoAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        transactionInfoAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(transactionInfoAdapter);
    }

    private void initData() {
        binding.refreshLayout.autoRefresh();
    }

    private View createEmptyView() {
        ViewTokenEmptyBinding binding = ViewTokenEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmptyToken.setText(LocaleController.getString("wallet_home_transaction_empty_text", R.string.wallet_home_transaction_empty_text));
        return binding.getRoot();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        page = 1;
        loadData();
    }

    @Override
    public void onLoadMore() {
        page++;
        loadData();
    }

    private void loadData() {
        WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = MMKVUtil.currentChainConfig();
        if (chainType == null) return;
        String symbol = chainType.getMain_currency_name();
        IRequestApi api = null;
        if ("ETH".equalsIgnoreCase(symbol)) {
            api = new EthTransactionsApi()
                    .setAddress(walletHomeAct.address)
                    .setPage(page);
        } else if ("MATIC".equalsIgnoreCase(symbol)) {
            api = new PolygonTransactionsApi()
                    .setAddress(walletHomeAct.address)
                    .setPage(page);
        } else if ("TT".equalsIgnoreCase(symbol)) {
            api = new TTTransactionsApi()
                    .setAddress(walletHomeAct.address)
                    .setPage(page);
        } else if ("ROSE".equalsIgnoreCase(symbol)) {
            api = new OasisTransactionsApi()
                    .setAddress(walletHomeAct.address)
                    .setPage(page);
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(api)
                .request(new OnHttpListener<String>() {

                    @Override
                    public void onSucceed(String result) {
                        List<TransactionInfo> list = null;
                        if ("TT".equalsIgnoreCase(symbol)) {
                            list = TTTransactions.parse(result);
                        } else {
                            list = EthTransactions.parse(result);
                        }
                        if (page == 1) {
                            transactionInfoAdapter.setList(list);
                        } else {
                            transactionInfoAdapter.addData(list);
                        }
                        if (CollectionUtils.size(list) >= 10) {
                            transactionInfoAdapter.getLoadMoreModule().loadMoreComplete();
                        } else {
                            transactionInfoAdapter.getLoadMoreModule().loadMoreEnd();
                        }
                        binding.refreshLayout.finishRefresh();
                        if (transactionInfoAdapter.getData().isEmpty()) {
                            transactionInfoAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                        } else if (TextUtils.isEmpty(currentPrice)) { //币种单价请求
                            requestCoinPrice(chainType);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        binding.refreshLayout.finishRefresh();
                        transactionInfoAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                    }
                });
    }

    private void requestCoinPrice(WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType) {
        WalletNetworkConfigEntity.WalletNetworkConfigEntityItem mainCurrencyData = BlockchainConfig.getMainCurrency(chainType);
        if (mainCurrencyData == null) {
            return;
        }
        WalletUtil.requestMainCoinPrice(mainCurrencyData.getCoin_id(), new WalletUtil.RequestCoinPriceListener() {
            @Override
            public void requestEnd() {
            }

            @Override
            public void requestError(String msg) {
            }

            @Override
            public void requestSuccessful(CurrencyPriceEntity resultData) {
                currentPrice = String.valueOf(resultData.getUsd());
                transactionInfoAdapter.updateData(currentPrice);
            }
        });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
    }

}