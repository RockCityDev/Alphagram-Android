package teleblock.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coingecko.domain.Coins.CoinFullData;
import com.google.android.flexbox.FlexboxLayout;
import com.ruffian.library.widget.RTextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ViewCurrencyGeneralBinding;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import teleblock.util.StringUtil;


/**
 * 创建日期：2022/6/9
 * 描述：货币详情-概览
 */
public class CurrencyGeneralView extends FrameLayout {

    private ViewCurrencyGeneralBinding binding;

    public CurrencyGeneralView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        binding = ViewCurrencyGeneralBinding.inflate(LayoutInflater.from(getContext()), this, true);

        binding.tvRank.setText(LocaleController.getString("currency_general_rank", R.string.currency_general_rank));
        binding.tvWebsite.setText(LocaleController.getString("currency_general_website", R.string.currency_general_website));
        binding.tvAnnouncement.setText(LocaleController.getString("currency_general_announcement", R.string.currency_general_announcement));
        binding.tvExplorer.setText(LocaleController.getString("currency_general_explorer", R.string.currency_general_explorer));
        binding.tvSourceCode.setText(LocaleController.getString("currency_general_source_code", R.string.currency_general_source_code));
        binding.tvCommunity.setText(LocaleController.getString("currency_general_community", R.string.currency_general_community));
        binding.tvTags.setText(LocaleController.getString("currency_general_tags", R.string.currency_general_tags));
        binding.tvMarketCapTitle.setText(LocaleController.getString("currency_general_market_cap", R.string.currency_general_market_cap));
        binding.tv24hVolTitle.setText(LocaleController.getString("currency_general_24h_vol", R.string.currency_general_24h_vol));
        binding.tvCirculatingSupplyValue.setText(LocaleController.getString("currency_general_circulating_supply", R.string.currency_general_circulating_supply));
        binding.tvTotalSupplyTitle.setText(LocaleController.getString("currency_general_total_supply", R.string.currency_general_total_supply));
    }

    public void setPriceData(Map<String, Double> data) {
        binding.tvMarketCapValue.setText(StringUtil.formatPrice(data.get("usd_market_cap"), true));
        binding.tv24hVolValue.setText(StringUtil.formatPrice(data.get("usd_24h_vol"), true));
    }

    public void setCoinData(CoinFullData data) {
        if (data.getLinks() == null || data.getMarketData() == null) return;
        binding.tvCoinRank.setText("Rank #" + data.getMarketCapRank());
        addUrlView(binding.websiteLayout, data.getLinks().getHomepage());
        addUrlView(binding.announcementLayout, data.getLinks().getAnnouncementUrl());
        addUrlView(binding.explorerLayout, data.getLinks().getBlockchainSite());
        addUrlView(binding.sourceCodeLayout, data.getLinks().getReposUrl().getGithub());
        List<String> communityList = new ArrayList<>();
        if (!TextUtils.isEmpty(data.getLinks().getTwitterScreenName())) {
            communityList.add("https://twitter.com/" + data.getLinks().getTwitterScreenName());
        }
        if (!TextUtils.isEmpty(data.getLinks().getFacebookUsername())) {
            communityList.add("https://www.facebook.com/" + data.getLinks().getFacebookUsername());
        }
        if (!TextUtils.isEmpty(data.getLinks().getTelegramChannelIdentifier())) {
            communityList.add("https://t.me/" + data.getLinks().getTelegramChannelIdentifier());
        }
        communityList.add(data.getLinks().getSubredditUrl());
        communityList.addAll(data.getLinks().getChatUrl());
        addUrlView(binding.communityLayout, communityList);
        addTagView(binding.tagsLayout, data.getCategories());

        binding.tvCirculatingSupplyValue.setText(StringUtil.formatPrice(data.getMarketData().getCirculatingSupply(), true));
        binding.tvTotalSupplyValue.setText(StringUtil.formatPrice(data.getMarketData().getTotalSupply(), true));
    }

    private void addUrlView(FlexboxLayout flexboxLayout, List<String> list) {
        for (String url : list) {
            if (!TextUtils.isEmpty(url)) {
                TextView textView = new TextView(getContext());
                textView.setTextColor(Color.parseColor("#39AFEA"));
                textView.setTextSize(13);
                textView.setText(Uri.parse(url).getHost());
                LayoutParams layoutParams = new LayoutParams(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
                layoutParams.rightMargin = AndroidUtilities.dp(10);
                textView.setLayoutParams(layoutParams);
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Browser.openUrl(getContext(), url);
                    }
                });
                flexboxLayout.addView(textView);
            }
        }
    }

    private void addTagView(FlexboxLayout flexboxLayout, List<Object> list) {
        for (Object object : list) {
            if (object instanceof String) {
                String url = (String) object;
                RTextView textView = new RTextView(getContext());
                textView.getHelper()
                        .setBackgroundColorNormal(Color.parseColor("#EDEDED"))
                        .setCornerRadius(AndroidUtilities.dp(4));
                textView.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setTextSize(13);
                textView.setText(url);
                LayoutParams layoutParams = new LayoutParams(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
                layoutParams.rightMargin = AndroidUtilities.dp(10);
                layoutParams.bottomMargin = AndroidUtilities.dp(5);
                textView.setLayoutParams(layoutParams);
                flexboxLayout.addView(textView);
            }
        }
    }

}