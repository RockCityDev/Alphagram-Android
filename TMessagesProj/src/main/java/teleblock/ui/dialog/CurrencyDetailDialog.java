package teleblock.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.bumptech.glide.Glide;
import com.coingecko.domain.Coins.CoinFullData;
import com.coingecko.domain.Coins.CoinList;
import com.flyco.tablayout.listener.OnTabSelectListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogCurrencyDetailBinding;

import java.util.List;
import java.util.Map;

import teleblock.manager.CoinGeckoManager;
import teleblock.util.MMKVUtil;
import teleblock.util.StringUtil;


/**
 * 货币详情
 */
public class CurrencyDetailDialog extends BaseBottomSheetDialog implements View.OnClickListener {

    private DialogCurrencyDetailBinding binding;
    private int completeNum; // 并发接口完成数
    private String id; // 货币id

    public CurrencyDetailDialog(@NonNull Context context, CharSequence charSequence) {
        super(context);
        List<CoinList> coinList = MMKVUtil.getCoinList();
        CoinList coinData = CollectionUtils.find(coinList, new CollectionUtils.Predicate<CoinList>() {
            @Override
            public boolean evaluate(CoinList item) {
                return item.getSymbol().equalsIgnoreCase(charSequence.toString());
            }
        });
        if (coinData != null) {
            id = coinData.getId();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogCurrencyDetailBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
        binding.tvDataSource.setText(LocaleController.getString("currency_data_source", R.string.currency_data_source));

        binding.ivClose.setOnClickListener(this);
    }

    private void initData() {
        binding.tabLayout.setTabData(new String[]{LocaleController.getString("currency_tab_price", R.string.currency_tab_price),
                LocaleController.getString("currency_tab_general", R.string.currency_tab_general), LocaleController.getString("currency_tab_exchange", R.string.currency_tab_exchange)});
        binding.tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                showTabView(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        getPriceData();
        getCoinData();
        binding.currencyPriceView.getMarketChartData(id, 1);
    }


    private void getPriceData() {
        CoinGeckoManager.getInstance().getPrice(id, true, new CoinGeckoManager.Callback<Map<String, Map<String, Double>>>() {
            @Override
            public void onSuccess(Map<String, Map<String, Double>> data) {
                Map<String, Double> map = data.get(id);
                binding.tvCoinPrice.setText("$" + StringUtil.formatNumber(map.get("usd"), 8));
                String change = StringUtil.formatNumber(map.get("usd_24h_change"), 2);
                binding.tvCoinChange.setText(change + "%");
                if (change.startsWith("-")) {
                    binding.ivCoinChange.setImageResource(R.mipmap.coin_change_decrease);
                    binding.tvCoinChange.setTextColor(Color.parseColor("#FF2929"));
                } else {
                    binding.ivCoinChange.setImageResource(R.mipmap.coin_change_increase);
                    binding.tvCoinChange.setTextColor(Color.parseColor("#38D103"));
                }
                binding.currencyGeneralView.setPriceData(map);

                completeNum += 1;
                completeLoad();
            }
        });
    }

    private void getCoinData() {
        CoinGeckoManager.getInstance().getCoinById(id, new CoinGeckoManager.Callback<CoinFullData>() {
            @Override
            public void onSuccess(CoinFullData data) {
                Glide.with(getContext()).load(data.getImage().getLarge()).circleCrop().into(binding.ivCoinIcon);
                SpanUtils.with(binding.tvCoinName)
                        .append(data.getName())
                        .append("(" + data.getSymbol() + ")").setForegroundColor(Color.parseColor("#868686"))
                        .create();
                binding.tvCoinRank.setText("#" + data.getMarketCapRank());
                binding.currencyPriceView.setCoinData(data);
                binding.currencyGeneralView.setCoinData(data);

                completeNum += 1;
                completeLoad();
            }
        });
    }

    private void showTabView(int position) {
        binding.currencyPriceView.setVisibility(View.INVISIBLE);
        binding.currencyGeneralView.setVisibility(View.INVISIBLE);
        binding.currencyExchangeView.setVisibility(View.INVISIBLE);
        if (position == 0) binding.currencyPriceView.setVisibility(View.VISIBLE);
        else if (position == 1) binding.currencyGeneralView.setVisibility(View.VISIBLE);
        else if (position == 2) binding.currencyExchangeView.setVisibility(View.VISIBLE);
    }

    private void completeLoad() {
        // 所有接口请求完成
        if (completeNum == 2) {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.contentLayout.setVisibility(View.VISIBLE);
            showTabView(0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.ivClose)) {
            dismiss();
        }
    }
}