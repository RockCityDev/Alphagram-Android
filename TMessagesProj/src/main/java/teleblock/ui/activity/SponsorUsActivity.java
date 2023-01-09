package teleblock.ui.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivitySponsorusBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import teleblock.blockchain.WCSessionManager;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.ui.SelectorSponsorPriceData;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.ui.adapter.SelectorSponsorPriceAdp;
import teleblock.ui.dialog.TransferDialog;
import teleblock.util.MMKVUtil;
import teleblock.util.SpacesItemDecoration;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/9/21
 * Author:Perry
 * Description：打赏我们
 */
public class SponsorUsActivity extends BaseFragment {

    private ActivitySponsorusBinding binding;

    //选择的捐献金额数据
    private List<SelectorSponsorPriceData> selectorSponsorPriceDataList = new ArrayList<>();
    private SelectorSponsorPriceAdp mSelectorSponsorPriceAdp;

    //币种数据
    private long chainId;
    private WalletNetworkConfigEntity.WalletNetworkConfigEntityItem ethCoinData;

    //钱包余额
    private BigDecimal walletBalanceBigDecimal;
    //币种单价
    private BigDecimal coinPrice;

    //打赏金额
    private BigDecimal sponsorUsPrice;

    //转账对话框
    private TransferDialog mTransferDialog;

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);//不显示actionbar
        binding = ActivitySponsorusBinding.inflate(LayoutInflater.from(context));
        fragmentView = binding.getRoot();

        initData();
        return fragmentView;
    }

    private void initData() {
        mSelectorSponsorPriceAdp = new SelectorSponsorPriceAdp();

        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.001", "\uD83C\uDF6D", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.002", "\uD83C\uDF66", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.003", "\uD83C\uDF54", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.005", "\uD83E\uDD70", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.01", "\uD83E\uDD29", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.02", "\uD83D\uDCB8", false));

        //所有的数据
        List<WalletNetworkConfigEntity.WalletNetworkConfigChainType> mWalletNetworkConfigChainTypeList = MMKVUtil.getWalletNetworkConfigEntity().getChainType();
        for (WalletNetworkConfigEntity.WalletNetworkConfigChainType chainData : mWalletNetworkConfigChainTypeList) {
            if (1 == chainData.getId()) {
                chainId = chainData.getId();
                for (WalletNetworkConfigEntity.WalletNetworkConfigEntityItem coinData : chainData.getCurrency()) {
                    if (coinData.isIs_main_currency()) {//获取eth主币
                        ethCoinData = coinData;
                    }
                }
            }
        }

        if (ethCoinData == null) {
            return;
        }

        getETHWalletInfoData();
        initView();
    }

    private void initView() {
        binding.ivBack.setOnClickListener(view -> finishFragment());
        binding.tvTips.setText(LocaleController.getString("sponsorus_tips", R.string.sponsorus_tips));
        binding.tvCoinType.setText(ethCoinData.getName());
        GlideHelper.getDrawableGlide(getContext(), ethCoinData.getIcon(), drawable -> binding.tvCoinType.getHelper().setIconNormalLeft(drawable));
        binding.tvCustomquantity.setText(LocaleController.getString("sponsorus_customquantity", R.string.sponsorus_customquantity));
        binding.tvSponsorus.setText(LocaleController.getString("sponsorus_sure", R.string.sponsorus_sure));
        binding.tvLoading.setText(LocaleController.getString("Loading", R.string.Loading));

        binding.rvPrice.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.rvPrice.addItemDecoration(new SpacesItemDecoration(3, 2, false));
        binding.rvPrice.setAdapter(mSelectorSponsorPriceAdp);
        mSelectorSponsorPriceAdp.setList(selectorSponsorPriceDataList);

        //适配器点击事件
        mSelectorSponsorPriceAdp.setOnItemClickListener((adapter, view, position) -> {
            mSelectorSponsorPriceAdp.selectorOpera(position);
            sponsorUsPrice = new BigDecimal(mSelectorSponsorPriceAdp.getData().get(position).getPrice());//打赏金额
        });

        //确定打赏
        binding.tvSponsorus.setOnClickListener(view -> {
            if (MMKVUtil.connectedWalletAddress().isEmpty()) {//没有绑定钱包就跳转去绑定
                presentFragment(new WalletBindAct());
                finishFragment();
                return;
            }

            if (sponsorUsPrice == null) {
                ToastUtils.showShort(LocaleController.getString("toast_tips_qxzdsje", R.string.toast_tips_qxzdsje));
                return;
            }

            WalletUtil.walletPayVerify(chainId, () -> sendTransaction());
        });

        //自定义币种和数量
        binding.tvCustomquantity.setOnClickListener(view -> {
            if (MMKVUtil.connectedWalletAddress().isEmpty()) {//没有绑定钱包就跳转去绑定
                presentFragment(new WalletBindAct());
                finishFragment();
                return;
            }

            if (mTransferDialog == null) {
                mTransferDialog = new TransferDialog(
                        getParentActivity(),
                        getUserConfig().getCurrentUser(),
                        null,
                        MMKVUtil.getSystemMsg().wallet_address,
                        -1,
                        true, null
                );
            }

            mTransferDialog.show();
        });
    }

    /**
     * 发起转账
     */
    private void sendTransaction() {
        WCSessionManager.getInstance().sendTransaction(MMKVUtil.getSystemMsg().wallet_address, sponsorUsPrice.toPlainString(), "", new WCSessionManager.Callback<String>() {
            @Override
            public void onSuccess(String data) {
                ToastUtils.showLong(LocaleController.getString("toast_tips_dscg", R.string.toast_tips_dscg));
            }

            @Override
            public void onError(String msg) {
                ToastUtils.showLong(msg);
            }
        });
    }

    /**
     * 请求钱包和链相关数据
     */
    private void getETHWalletInfoData() {
        new Thread(() -> {
            CountDownLatch countDownLatch = new CountDownLatch(2);
            if (TextUtils.isEmpty(MMKVUtil.connectedWalletAddress())) {
                countDownLatch.countDown();
            } else {
                //钱包数据请求
                WalletUtil.requestWalletBalanceData(MMKVUtil.connectedWalletAddress(), "ETH", new WalletUtil.RequestStateListener() {
                    @Override
                    public void requestEnd() {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void requestError(String msg) {

                    }

                    @Override
                    public void requestSuccessful(String data) {
                        walletBalanceBigDecimal = new BigDecimal(WalletUtil.parseToken(data, 18));
                    }
                });
            }

            //币种单价请求
            WalletUtil.requestMainCoinPrice(ethCoinData.getCoin_id(), new WalletUtil.RequestCoinPriceListener() {
                @Override
                public void requestEnd() {
                    countDownLatch.countDown();
                }

                @Override
                public void requestError(String msg) {}

                @Override
                public void requestSuccessful(CurrencyPriceEntity resultData) {
                    coinPrice = new BigDecimal(String.valueOf(resultData.getUsd()));
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            AndroidUtilities.runOnUIThread(() -> {
                binding.tvSponsorus.setVisibility(View.VISIBLE);
                binding.llLoading.setVisibility(View.GONE);

                if (coinPrice == null) {
                    return;
                }

                if (!MMKVUtil.connectedWalletAddress().isEmpty() && walletBalanceBigDecimal != null) {
                    //钱包账户余额
                    String walletBalance = String.format(
                            LocaleController.getString("sponsorus_walletbalance", R.string.sponsorus_walletbalance)
                            , walletBalanceBigDecimal.toPlainString() + ethCoinData.getName()
                    ) + WalletUtil.toCoinPriceUSD(walletBalanceBigDecimal, coinPrice, 6);
                    binding.tvWalletAccount.setText(walletBalance);
                }

                //获取到单价之后，传递给适配器，重新计算
                mSelectorSponsorPriceAdp.setPrice(coinPrice);
                mSelectorSponsorPriceAdp.notifyDataSetChanged();
            });
        }).start();
    }

}