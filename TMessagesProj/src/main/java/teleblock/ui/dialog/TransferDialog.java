package teleblock.ui.dialog;

import static teleblock.widget.TelegramUserAvatar.DEFAUTL;
import static teleblock.widget.TelegramUserAvatar.SPONSOR_US;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.DrawableUtils;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.DialogTransferBinding;
import org.telegram.tgnet.TLRPC;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.blockchain.WCSessionManager;
import teleblock.blockchain.Web3TransactionUtils;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.ui.MyCoinListData;
import teleblock.model.ui.SelectorGasFeeData;
import teleblock.model.wallet.GasFeeEntity;
import teleblock.network.api.AddTransferRecordApi;
import teleblock.util.MMKVUtil;
import teleblock.util.TGLog;
import teleblock.util.TransferParseUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;
import teleblock.widget.PriceTextWatcher;
import teleblock.widget.TelegramUserAvatar;

/**
 * Time:2022/9/8
 * Author:Perry
 * Description：转账Dialog
 */
public class TransferDialog extends Dialog {
    private DialogTransferBinding binding;

    //步骤1
    private int setp = 1;
    //打赏我们的逻辑
    private boolean sponsorUs = false;
    //外部传入的链ID
    private int selectorChainId = -1;

    //钱包配置数据
    private List<WalletNetworkConfigEntity.WalletNetworkConfigChainType> mWalletNetworkConfigChainTypeList;

    private TransferDialogListener mTransferDialogListener;

    private TLRPC.User ownUserInfor;//自己的user信息
    private TLRPC.User userInfor;//对方的user信息

    private String ownWalletAddress;//自己的钱包地址
    private String toAddress;//对方的钱包地址

    //是否在请求中
    private boolean loading = false;

    //币种图标
    private Drawable coinDrawable;

    //钱包余额 单位：coinType
    private BigDecimal walletBalanceBigDecimal;

    //币种单价 单位：美元
    private BigDecimal coinPrice;
    //主币单价 单位：美元
    private BigDecimal mainCoinPrice;

    //转账金额 单位：coinType
    private BigDecimal amount;

    //总额 单位：coinType
    private BigDecimal totalMoney;
    //总额 单位：美元
    private BigDecimal totalMoneyDoller;

    //gas金额 单位：coinType
    private String gasPrice;
    private BigDecimal gasCoinNum;

    //选择链对话框
    private ChainTypeSelectorDialog mChainTypeSelectorDialog;
    //选中的链数据
    private WalletNetworkConfigEntity.WalletNetworkConfigChainType currentChainType;

    //链下面的币种数据对话框
    private TransferSelectorCointypeDialog mTransferSelectorCointypeDialog;
    //币种列表数据
    private List<MyCoinListData> mMyCoinListDataList = new ArrayList<>();
    //选中的币种数据
    private MyCoinListData selectorCoinData;

    //什么是gas费用
    private GasTipsDialog mGasTipsDialog;
    //选择gas费用对话框
    private SelectorGasFeeDialog mSelectorGasFeeDialog;
    //gas费用列表
    private List<SelectorGasFeeData> gasFeeList = new ArrayList<>();
    private String gasLimit;

    //转账时候的data
    private String transferData;

    public TransferDialog(
            @NonNull Context context,
            TLRPC.User ownUserInfor,
            TLRPC.User user,
            String toAddress,
            int selectorChainId,
            boolean sponsorUs,
            TransferDialogListener mTransferDialogListener
    ) {
        super(context, R.style.dialog2);
        //获取链数据
        mWalletNetworkConfigChainTypeList = MMKVUtil.getWalletNetworkConfigEntity().getChainType();
        this.ownUserInfor = ownUserInfor;
        this.userInfor = user;
        this.ownWalletAddress = MMKVUtil.connectedWalletAddress();
        this.toAddress = toAddress;
        this.selectorChainId = selectorChainId;
        this.sponsorUs = sponsorUs;
        this.mTransferDialogListener = mTransferDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(getWindow());
        BarUtils.setStatusBarLightMode(getWindow(), true);
        binding = DialogTransferBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.tvStepTwoBack.setText(LocaleController.getString("dg_transfer_step_tow_title", R.string.dg_transfer_step_tow_title));
        binding.mainContainer.setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);
        if (selectorChainId == -1) {
            currentChainType = mWalletNetworkConfigChainTypeList.get(0);
        } else {
            //根据id来匹配选择的链
            for (WalletNetworkConfigEntity.WalletNetworkConfigChainType chainData : mWalletNetworkConfigChainTypeList) {
                if (chainData.getId() == selectorChainId) {
                    currentChainType = chainData;
                }
            }
        }

        //设置链数据
        setChainTypeUi(currentChainType);

        //选择链类型
        binding.tvChaintype.setOnClickListener(view -> {
            if (mChainTypeSelectorDialog == null) {
                mChainTypeSelectorDialog = new ChainTypeSelectorDialog(getContext(), data -> {
                    currentChainType = data;
                    setChainTypeUi(data);
                });
            }
            mChainTypeSelectorDialog.setCurrentChainType(currentChainType);

            if (!loading) {
                //显示选择链对话框
                mChainTypeSelectorDialog.show();
            }
        });

        //关闭弹窗
        binding.tvCloseDialog.setOnClickListener(view -> dismiss());
        //显示对方头像
        if (userInfor != null) {
            binding.flAvatar.setUserInfo(userInfor)
                    .setModel(sponsorUs ? SPONSOR_US : DEFAUTL)
                    .loadView();
        } else {
            binding.flAvatar.setModel(TelegramUserAvatar.ADDRESS_TRANSFER).loadView();
        }

        //像谁转账
        String nickName;
        if (sponsorUs) {
            nickName = LocaleController.getString("sponsorus_tips", R.string.sponsorus_tips);
        } else if (userInfor != null) {
            nickName = String.format(LocaleController.getString("chat_transfer_towhotransfer", R.string.chat_transfer_towhotransfer), UserObject.getUserName(userInfor));
        } else {
            nickName = String.format(LocaleController.getString("chat_transfer_towhotransfer", R.string.chat_transfer_towhotransfer), WalletUtil.formatAddress(toAddress));
        }

        binding.tvNickname.setText(nickName);
        binding.tvNickname2.setText(nickName);

        showSetpView();
    }

    /**
     * 根据当前进度显示不同页面
     */
    private void showSetpView() {
        if (setp == 1) {
            setSetpOne();
        } else {
            setSetpTwo();
        }
    }

    /**
     * 设置步骤1样式和逻辑
     */
    private void setSetpOne() {
        binding.tvTips.setText(LocaleController.getString("chat_transfer_tips", R.string.chat_transfer_tips));
        binding.tvChaintype.setVisibility(View.VISIBLE);
        binding.tvStepTwoBack.setVisibility(View.GONE);
        binding.llStepOne.setVisibility(View.VISIBLE);
        binding.clSetpTwo.setVisibility(View.GONE);

        //输入的金额转化的美元，默认值
        binding.tvInputPrice.setText(LocaleController.getString("chat_transfer_input_price_tips", R.string.chat_transfer_input_price_tips));
        binding.tvInputPrice.setTextColor(Color.parseColor("#56565c"));

        //选择币种 弹出对话框
        binding.tvCoinType.setOnClickListener(view -> {
            if (mTransferSelectorCointypeDialog == null) {
                mTransferSelectorCointypeDialog = new TransferSelectorCointypeDialog(getContext(), coinData -> {
                    selectorCoinData = coinData;

                    selectorCoinInfo(coinData);
                });
            }

            mTransferSelectorCointypeDialog.setData(mMyCoinListDataList);
            mTransferSelectorCointypeDialog.show();
        });

        //键盘点击事件监听
        binding.etInputNum.setOnEditorActionListener((textView, i, keyEvent) -> {
            AndroidUtilities.hideKeyboard(getWindow().getDecorView());
            if (binding.llNextstep.isClickable()) {
                binding.llNextstep.performClick();
            }
            return false;
        });

        //输入限制判断
        binding.etInputNum.addTextChangedListener(new PriceTextWatcher(binding.etInputNum) {
            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);
                if (walletBalanceBigDecimal == null) {
                    return;
                }
                String transFerMoneyStr = binding.etInputNum.getText().toString().trim();
                try {
                    amount = new BigDecimal(transFerMoneyStr);
                } catch (Exception e) {
                    amount = new BigDecimal(String.valueOf(0f));
                }

                if (WalletUtil.decimalCompareTo(amount, new BigDecimal(String.valueOf(0f)))) {
                    if (WalletUtil.decimalCompareTo(amount, walletBalanceBigDecimal)) {
                        changeNextBtnStatus(false);
                        binding.tvInputPrice.setText(LocaleController.getString("chat_transfer_input_price_tips1", R.string.chat_transfer_input_price_tips1));
                        binding.tvInputPrice.setTextColor(Color.parseColor("#FF4550"));
                    } else {
                        changeNextBtnStatus(true);
                        binding.tvInputPrice.setText(WalletUtil.toCoinPriceUSD(amount, coinPrice, 6));
                        binding.tvInputPrice.setTextColor(Color.parseColor("#56565c"));
                    }
                } else {
                    binding.tvInputPrice.setText(LocaleController.getString("chat_transfer_input_price_tips", R.string.chat_transfer_input_price_tips));
                    binding.tvInputPrice.setTextColor(Color.parseColor("#56565c"));
                    changeNextBtnStatus(false);
                }
            }
        });

        //下一步按钮点击事件
        binding.llNextstep.setOnClickListener(view -> {
            nextClick();
        });

        changeNextBtnStatus(false);
    }

    /**
     * 下一步按钮点击事件
     */
    private void nextClick() {
        //显示加载中布局
        changeNextBtnUiStyle(true);

        //主币精度
        int decimal = 18;
        WalletNetworkConfigEntity.WalletNetworkConfigEntityItem mainCoinData = null;//当前链主币
        for (WalletNetworkConfigEntity.WalletNetworkConfigEntityItem coinItemData : currentChainType.getCurrency()) {
            if (coinItemData.isIs_main_currency()) {
                mainCoinData = coinItemData;
            }
        }
        if (mainCoinData != null) {
            decimal = mainCoinData.getDecimal();
        }

        //代币转账时候的data
        transferData = Web3TransactionUtils.encodeTransferData(toAddress, new BigDecimal(WalletUtil.toWei(amount.toPlainString(), selectorCoinData.getDecimal())).toBigInteger());
        WalletUtil.requestChainGasfee(
                currentChainType.getId(),
                decimal,
                toAddress,
                transferData,
                new WalletUtil.RequestGasFeeListener() {
                    @Override
                    public void requestEnd() {
                        changeNextBtnUiStyle(false);
                    }

                    @Override
                    public void requestError(String msg) {
                        TGLog.erro("请求gas费失败：" + msg);
                    }

                    @Override
                    public void requestSuccessful(GasFeeEntity resultData, BigInteger limit) {
                        //默认获取 中间的gas金额
                        gasFeeList.clear();
                        gasLimit = limit.toString();

                        setGasmoneyUi(resultData.getResult().getProposeGasPrice());
                        //设置gas费用列表数据
                        if (!TextUtils.isEmpty(resultData.getResult().getSafeGasPrice())) {
                            gasFeeList.add(new SelectorGasFeeData("Cheaper \uD83D\uDC22", resultData.getResult().getSafeGasPrice(), false));//安全
                        }
                        if (!TextUtils.isEmpty(resultData.getResult().getProposeGasPrice())) {
                            gasFeeList.add(new SelectorGasFeeData("Suggested", resultData.getResult().getProposeGasPrice(), true));//默认
                        }
                        if (!TextUtils.isEmpty(resultData.getResult().getFastGasPrice())) {
                            gasFeeList.add(new SelectorGasFeeData("Faster \uD83D\uDE80", resultData.getResult().getFastGasPrice(), false));//快速
                        }

                        setp++;
                        showSetpView();

                    }
                });
    }

    /**
     * 设置步骤2的样式和逻辑
     */
    private void setSetpTwo() {
        binding.tvChaintype.setVisibility(View.INVISIBLE);
        binding.tvStepTwoBack.setVisibility(View.VISIBLE);
        binding.llStepOne.setVisibility(View.GONE);
        binding.clSetpTwo.setVisibility(View.VISIBLE);//显示步骤2view

        binding.tvBtnTransferConfirm.setText(LocaleController.getString("chat_transfer_confirm", R.string.chat_transfer_confirm));
        binding.tvBtnBack.setText(LocaleController.getString("chat_transfer_back", R.string.chat_transfer_back));

        binding.tvGas.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
        binding.tvGas.getPaint().setAntiAlias(true);

        //返回上一步
        binding.tvStepTwoBack.setOnClickListener(view -> {
            setp--;
            binding.tvStepTwoBack.setVisibility(View.GONE);
            binding.tvChaintype.setVisibility(View.VISIBLE);
            binding.llStepOne.setVisibility(View.VISIBLE);
            binding.clSetpTwo.setVisibility(View.GONE);//隐藏步骤2view
        });

        //gas说明
        binding.tvGasTitle.setOnClickListener(view -> {
            if (mGasTipsDialog == null) {
                mGasTipsDialog = new GasTipsDialog(getContext());
            }
            mGasTipsDialog.show();
        });

        //选择gas费用
        binding.tvGas.setOnClickListener(view -> {
            if (gasFeeList.size() < 2) {
                return;//只有中间值的情况
            }
            if (mSelectorGasFeeDialog == null) {
                //设置gas费用
                mSelectorGasFeeDialog = new SelectorGasFeeDialog(getContext(), this::setGasmoneyUi);
            }

            mSelectorGasFeeDialog.setGasData(gasFeeList, coinPrice, gasLimit, selectorCoinData.getSymbol(), coinPrice, mainCoinPrice);
            mSelectorGasFeeDialog.show();
        });

        //确认转账
        binding.tvBtnTransferConfirm.setOnClickListener(view -> {
            if (BlockchainConfig.PKG_TOKEN_POCKET.equals(MMKVUtil.connectedWalletPkg())) {
                sendTransaction();
            } else {
                //支付环境验证
                WalletUtil.walletPayVerify(currentChainType.getId(), () -> sendTransaction());
            }
        });

        //设置自己的钱包地址
        binding.tvFromAccount.setText(WalletUtil.formatAddress(ownWalletAddress));
        //自己的头像
        binding.flFromAvatar.setUserInfo(ownUserInfor).loadView();

        //设置对方的钱包地址
        binding.tvToAccount.setText(WalletUtil.formatAddress(toAddress));
        //显示对方头像
        if (userInfor != null) {
            binding.flToAvatar.setUserInfo(userInfor).setModel(sponsorUs ? SPONSOR_US : DEFAUTL).loadView();
            binding.flAvatar2.setUserInfo(userInfor).setModel(sponsorUs ? SPONSOR_US : DEFAUTL).loadView();
        } else {
            binding.flToAvatar.setModel(TelegramUserAvatar.ADDRESS_TRANSFER).loadView();
            binding.flAvatar2.setModel(TelegramUserAvatar.ADDRESS_TRANSFER).loadView();
        }

        //钱包余额
        String balanceStr = String.format(LocaleController.getString("chat_transfer_balance", R.string.chat_transfer_balance),
                priceCoinType(walletBalanceBigDecimal, selectorCoinData));
        binding.tvFromBalance.setText(balanceStr);

        //转账金额
        binding.tvTransferfee.setText(priceCoinType(amount, selectorCoinData));
        //转账金额 美元
        binding.tvTransferfeeDollar.setText(WalletUtil.toCoinPriceUSD(amount, coinPrice, 6));
    }

    /**
     * 发起转账
     */
    private void sendTransaction() {
        WalletUtil.sendTransaction(currentChainType.getId(), toAddress, gasPrice, gasLimit, amount.toPlainString(),
                selectorCoinData.getContractAddress(), selectorCoinData.getDecimal(), selectorCoinData.getSymbol(), new WCSessionManager.Callback<String>() {
                    @Override
                    public void onSuccess(String data) {
                        dismiss();
                        if (sponsorUs) {
                            ToastUtils.showLong(LocaleController.getString("toast_tips_dscg", R.string.toast_tips_dscg));
                        } else {
                            transferSuccessful(data);
                        }
                    }
                });
    }

    /**
     * 转账成功 上报结果
     *
     * @param hash
     */
    private void transferSuccessful(String hash) {
        long coinId = 0L;
        for (WalletNetworkConfigEntity.WalletNetworkConfigEntityItem coinItemData : currentChainType.getCurrency()) {
            if (coinItemData.getName().equals(selectorCoinData.getSymbol())) {
                coinId = coinItemData.getId();
            }
        }

        //上报转账结果
        AddTransferRecordApi addTransferRecordApi = new AddTransferRecordApi();
        addTransferRecordApi.setPayment_tg_user_id(String.valueOf(ownUserInfor.id))
                .setPayment_account(ownWalletAddress)
                .setReceipt_tg_user_id(String.valueOf(userInfor == null ? 0 : userInfor.id))
                .setReceipt_account(toAddress)
                .setChain_id(currentChainType.getId())
                .setChain_name(currentChainType.getName())
                .setCurrency_id(coinId)
                .setCurrency_name(selectorCoinData.getSymbol())
                .setAmount(amount.toPlainString())
                .setTx_hash(hash);
        EasyHttp.post(new ApplicationLifecycle()).api(addTransferRecordApi).request(null);

        if (mTransferDialogListener != null) {
            String parserStr = TransferParseUtil.setParseStr(
                    ownUserInfor.id,
                    userInfor == null ? 0 : userInfor.id,
                    selectorCoinData.getSymbol(),
                    hash,
                    ownWalletAddress,
                    toAddress,
                    amount,
                    gasCoinNum,
                    totalMoney,
                    totalMoneyDoller,
                    currentChainType.getId()
            );
            mTransferDialogListener.transferSuccessful(parserStr);
        }
    }

    /**
     * 修改下一步按钮状态，是否可以点击
     *
     * @param isClickable
     */
    private void changeNextBtnStatus(boolean isClickable) {
        if (isClickable) {
            binding.llNextstep.setClickable(true);
            binding.llNextstep.setAlpha(1f);
        } else {
            binding.llNextstep.setClickable(false);
            binding.llNextstep.setAlpha(0.5f);
        }
    }

    /**
     * 修改下一步按钮的样式
     *
     * @param ifLoading
     */
    private void changeNextBtnUiStyle(boolean ifLoading) {
        this.loading = ifLoading;
        if (ifLoading) {
            binding.llNextstep.setAlpha(0.5f);
            binding.pbLoading.setVisibility(View.VISIBLE);
            binding.tvLoading.setText(LocaleController.getString("Loading", R.string.Loading));
        } else {
            binding.pbLoading.setVisibility(View.GONE);
            binding.tvLoading.setText(LocaleController.getString("chat_transfer_nextstep", R.string.chat_transfer_nextstep));
        }
    }

    /**
     * 设置gas费用 并计算总额
     *
     * @param gasPrice 单位Gwei
     */
    private void setGasmoneyUi(String gasPrice) {
        this.gasPrice = gasPrice;
        //计算gas个数
        gasCoinNum = WalletUtil.gasFree(gasPrice, gasLimit, mainCoinPrice, coinPrice);
        //默认gas费用
        binding.tvGas.setText(priceCoinType(gasCoinNum, selectorCoinData));
        //gas费用美元
        binding.tvGasDollar.setText(WalletUtil.toCoinPriceUSD(gasCoinNum, coinPrice, 6));

        setTotalMoneyUi();
    }

    /**
     * 设置总额
     */
    private void setTotalMoneyUi() {
        if (!StringUtils.isEmpty(gasPrice)) {//gas费不为空，则总额加上gas费用
            totalMoney = amount.add(gasCoinNum);
        } else {
            totalMoney = amount;
        }

        //总额
        binding.tvTotal.setText(WalletUtil.bigDecimalScale(totalMoney, 10).toPlainString());
        binding.tvTotal.getHelper().setIconNormalLeft(coinDrawable);

        //总额美元
        totalMoneyDoller = totalMoney.multiply(coinPrice);
        binding.tvTotalDoller.setText("$" + WalletUtil.bigDecimalScale(totalMoneyDoller, 10).toPlainString());
    }

    /**
     * bigdecimal转String 增加币种单位
     *
     * @param bigDecimal
     */
    private String priceCoinType(BigDecimal bigDecimal, MyCoinListData selectorCoinData) {
        return WalletUtil.bigDecimalScale(bigDecimal, 10).toPlainString() + " " + selectorCoinData.getSymbol();
    }

    /**
     * 设置链的ui数据 并赋值
     *
     * @param data
     */
    private void setChainTypeUi(WalletNetworkConfigEntity.WalletNetworkConfigChainType data) {
        requestWalletInforData();

        //链的名称
        binding.tvChaintype.setText(data.getName());
        //获取显示链图标
        GlideHelper.getDrawableGlide(getContext(), data.getIcon(), drawable -> binding.tvChaintype.getHelper().setIconNormalLeft(drawable));
    }


    /**
     * 请求链和钱包下面的数据
     */
    private void requestWalletInforData() {
        changeNextBtnUiStyle(true);

        //清空一些数据
        binding.etInputNum.setText("");
        walletBalanceBigDecimal = null;
        coinPrice = null;
        mainCoinPrice = null;
        gasPrice = "";

        //每次切换链的时候都要获取这个链下面的主币账户信息，以及钱包下所有的代币信息
        WalletUtil.requestWalletCoinBalance(ownWalletAddress, currentChainType, mMyCoinListDataList, () -> {
            loading = false;
            if (!mMyCoinListDataList.isEmpty()) {
                //获取主币价格
                for (MyCoinListData coinListData : mMyCoinListDataList) {
                    if (coinListData.isIs_main_currency()) {
                        mainCoinPrice = coinListData.getPrice();
                    }
                }

                //隐藏加载状态
                changeNextBtnUiStyle(false);

                //默认选中第一条的币种数据
                selectorCoinData = mMyCoinListDataList.get(0);

                //设置选中的币种数据
                selectorCoinInfo(selectorCoinData);
            }
        });
    }

    /**
     * 计算选择的币种信息数据并设置显示图标icon和名称
     *
     * @param mMyCoinListData
     */
    private void selectorCoinInfo(MyCoinListData mMyCoinListData) {
        walletBalanceBigDecimal = mMyCoinListData.getBalance();
        coinPrice = mMyCoinListData.getPrice();

        //当前币种账户余额
        String walletBalance = String.format(
                LocaleController.getString("chat_transfer_wallet_balance", R.string.chat_transfer_wallet_balance)
                , priceCoinType(walletBalanceBigDecimal, mMyCoinListData)
        ) + WalletUtil.toCoinPriceUSD(walletBalanceBigDecimal, coinPrice, 6);
        binding.tvWalletBalance.setText(walletBalance);

        //币种 文字
        binding.tvCoinType.setText(mMyCoinListData.getSymbol());
        //币种 图标
        if (!TextUtils.isEmpty(mMyCoinListData.getIcon())) {
            GlideHelper.getDrawableGlide(getContext(), mMyCoinListData.getIcon(), drawable -> {
                coinDrawable = drawable;
                binding.tvCoinType.getHelper().setIconNormalLeft(drawable);
            });
        } else {
            coinDrawable = ResourceUtils.getDrawable(mMyCoinListData.getIconRes());
            binding.tvCoinType.getHelper().setIconNormalLeft(coinDrawable);
        }
    }

    public interface TransferDialogListener {
        void transferSuccessful(String parseStr);
    }
}
