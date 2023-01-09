package teleblock.ui.view;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewCreategroupFuncationBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Components.LayoutHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.network.api.CreateGroupApi;
import teleblock.ui.activity.WalletBindAct;
import teleblock.ui.dialog.ChainTypeSelectorDialog;
import teleblock.ui.dialog.CoinTypeSelectorDialog;
import teleblock.ui.dialog.LoadingDialog;
import teleblock.ui.dialog.TokenTypeSelectorDialog;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/10/25
 * Author:Perry
 * Description：群组创建view
 */
public class CreateGroupView extends LinearLayout {

    private ViewCreategroupFuncationBinding mBinding;
    private BaseFragment baseFragment;
    private LoadingDialog mLoadingDialog;

    //单选条件
    private RadioCell unlimitedRadio;
    private RadioCell conditionRadio;
    private RadioCell paylimitRadio;
    private final static int PAYLIMIT = 2, CONDITION = 1, UNLIMIT = 0;
    private ArrayList<RadioCell> radioCells = new ArrayList();
    //默认选中无条件
    private int selectType = UNLIMIT;

    //钱包配置数据
    private WalletNetworkConfigEntity webConfigData;

    //链选择对话框
    private ChainTypeSelectorDialog mChainTypeSelectorDialog;
    //币种选择对话框
    private CoinTypeSelectorDialog mCoinTypeSelectorDialog;
    //token选择对话框
    private TokenTypeSelectorDialog mTokenTypeSelectorDialog;

    //请求类
    private CreateGroupApi mCreateGroupApi;
    /******请求参数-start*****/
    private String walletAddress = "";//钱包地址
    private String walletName = "";//钱包名称
    /******end*****/

    public CreateGroupView(BaseFragment baseFragment, LoadingDialog mLoadingDialog) {
        super(baseFragment.getParentActivity());
        this.baseFragment = baseFragment;
        this.mLoadingDialog = mLoadingDialog;

        mBinding = ViewCreategroupFuncationBinding.inflate(LayoutInflater.from(baseFragment.getParentActivity()), this, true);
        mBinding.getRoot().setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundGray));

        //初始化请求类
        mCreateGroupApi = new CreateGroupApi(1);
        mCreateGroupApi.setJoin_type(1);//默认第一个

        setThemeColor();

        initView();

        initClick();
    }

    /**
     * 设置主题适配
     */
    private void setThemeColor() {
        /*适配主题颜色-背景色*/
        mBinding.tvGroupDescribe.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));
        mBinding.etAddGroupDescribe.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));
        mBinding.tvGroupTypeTitle.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));
        mBinding.llJoinType.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));
        mBinding.llJoinContent.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));

        if (Theme.getCurrentTheme().isDark()) { //夜间模式
            mBinding.tvWalletAddress.getHelper().setBackgroundColorNormal(Color.parseColor("#3A485A"));
        } else {
            mBinding.tvWalletAddress.getHelper().setBackgroundColorNormal(Color.parseColor("#F0F5FF"));
        }

        mBinding.etInputCoinnumMin.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));
        mBinding.etInputPaynum.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));
        mBinding.etInputTokenAddress.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));

        //适配主题颜色-文字颜色
        mBinding.tvGroupDescribe.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        mBinding.etAddGroupDescribe.setHintTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteHintText));
        mBinding.etAddGroupDescribe.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));

        mBinding.tvGroupTypeTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        mBinding.tvJoingroupTips.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteGrayText4));

        mBinding.tvJoinTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        mBinding.tvAddressTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.tvWalletAddress.getHelper().setTextColorNormal(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.tvUnbindWallet.getHelper().setTextColorNormal((baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader)));
        mBinding.tvSelectChainTypeTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.tvSelectChainType.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        mBinding.tvSelectTokenTypeTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.tvSelectTokenType.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));

        mBinding.tvJoinContentTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        mBinding.tvSelectCoinTypeTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.tvSelectCoinType.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        mBinding.etInputCoinnumMin.setHintTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteHintText));
        mBinding.etInputCoinnumMin.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.etInputPaynum.setHintTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteHintText));
        mBinding.etInputPaynum.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.rtvSelectCoinType.getHelper().setTextColorNormal(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.tvTokenTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        mBinding.etInputTokenAddress.setHintTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteHintText));
        mBinding.etInputTokenAddress.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));

        //图标颜色
        Drawable leftIconDrawable = ResourceUtils.getDrawable(R.drawable.icon_connect_group3_white);
        leftIconDrawable.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), PorterDuff.Mode.MULTIPLY));
        Drawable rightIconDrawable = ResourceUtils.getDrawable(R.drawable.icon_next_group3_white);
        rightIconDrawable.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), PorterDuff.Mode.MULTIPLY));
        mBinding.tvUnbindWallet.getHelper().setIconNormalLeft(leftIconDrawable).setIconNormalRight(rightIconDrawable);
    }

    private void initClick() {
        //无条件
        unlimitedRadio.setOnClickListener(v -> {
            setCheckedEnableLimitCell(UNLIMIT, () -> {
                mCreateGroupApi.setJoin_type(1);
                unLimitedUI();
            });
        });

        //条件加群
        conditionRadio.setOnClickListener(v -> {
            setCheckedEnableLimitCell(CONDITION, () -> {
                mCreateGroupApi.setJoin_type(2);
                conditionUI();
            });
        });

        //支付入群
        paylimitRadio.setOnClickListener(v -> {
            setCheckedEnableLimitCell(PAYLIMIT, () -> {
                mCreateGroupApi.setJoin_type(3);
                payLimitUI();
            });
        });

        //选择链
        mBinding.rlSelectChainType.setOnClickListener(v -> {
            if (mChainTypeSelectorDialog != null) {
                mChainTypeSelectorDialog.show();
            }
        });

        //币种选择
        mBinding.rlSelectorCoinType.setOnClickListener(v -> {
            if (mCoinTypeSelectorDialog != null) {
                mCoinTypeSelectorDialog.show();
            }
        });
        mBinding.rtvSelectCoinType.setOnClickListener(v -> {
            if (mCoinTypeSelectorDialog != null) {
                mCoinTypeSelectorDialog.show();
            }
        });

        //token选择
        mBinding.rlSelectTokenType.setOnClickListener(v -> {
            if (mTokenTypeSelectorDialog != null) {
                mTokenTypeSelectorDialog.show();
            }
        });

        //去绑定钱包页面
        mBinding.tvUnbindWallet.setOnClickListener(v -> baseFragment.presentFragment(new WalletBindAct()));
    }


    private void initView() {
        EventBus.getDefault().register(this);

        //简介
        mBinding.tvGroupDescribe.setText(LocaleController.getString("create_group_describe", R.string.create_group_describe));
        mBinding.etAddGroupDescribe.setHint(LocaleController.getString("create_group_add_describe", R.string.create_group_add_describe));

        //私人群组类型
        mBinding.tvGroupTypeTitle.setText(LocaleController.getString("tv_group_type_title", R.string.create_group_type));
        unlimitedRadio = new RadioCell(getContext());
        unlimitedRadio.setText(LocaleController.getString("create_group_join_group", R.string.create_group_join_group), true, true);
        unlimitedRadio.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));

        conditionRadio = new RadioCell(getContext());
        conditionRadio.setText(LocaleController.getString("create_group_conditionjoin_group", R.string.create_group_conditionjoin_group), false, true);
        conditionRadio.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));

        paylimitRadio = new RadioCell(getContext());
        paylimitRadio.setText(LocaleController.getString("create_group_paytojoin_group", R.string.create_group_paytojoin_group), false, false);
        paylimitRadio.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));

        mBinding.llRadio.addView(unlimitedRadio, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        mBinding.llRadio.addView(conditionRadio, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        mBinding.llRadio.addView(paylimitRadio, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        //添加到数组
        radioCells.add(unlimitedRadio);
        radioCells.add(conditionRadio);
        radioCells.add(paylimitRadio);

        //条件入群-付费入群
        mBinding.tvAddressTitle.setText(LocaleController.getString("create_group_address_title", R.string.create_group_address_title));
        mBinding.tvUnbindWallet.setText(LocaleController.getString("create_group_link_wallet", R.string.create_group_link_wallet));
        mBinding.tvSelectChainTypeTitle.setText(LocaleController.getString("create_group_chain_type", R.string.create_group_chain_type));
        mBinding.tvSelectTokenTypeTitle.setText(LocaleController.getString("create_group_token_type", R.string.create_group_token_type));
        mBinding.tvSelectCoinTypeTitle.setText(LocaleController.getString("create_group_coin_type", R.string.create_group_coin_type));
        mBinding.etInputCoinnumMin.setHint(LocaleController.getString("create_group_input_tokennum_min", R.string.create_group_input_tokennum_min));
        mBinding.etInputPaynum.setHint(LocaleController.getString("create_group_input_paynum", R.string.create_group_input_paynum));
        mBinding.tvTokenTitle.setText(LocaleController.getString("create_group_coin_type", R.string.create_group_coin_type));
        mBinding.etInputTokenAddress.setHint(LocaleController.getString("create_group_input_tokenaddress", R.string.create_group_input_tokenaddress));

        if (mChainTypeSelectorDialog == null) {
            //初始化选择链对话框
            mChainTypeSelectorDialog = new ChainTypeSelectorDialog(baseFragment.getParentActivity(), this::setSelectChainDataUI);
        }

        if (mCoinTypeSelectorDialog == null) {
            //初始化选择币种对话框
            mCoinTypeSelectorDialog = new CoinTypeSelectorDialog(baseFragment.getParentActivity(), this::setSelectCoinDataUI);
        }

        if (mTokenTypeSelectorDialog == null) {
            //初始化选择token对话框
            mTokenTypeSelectorDialog = new TokenTypeSelectorDialog(baseFragment.getParentActivity(), this::setSelectTokenDataUI);
        }

        //默认是第一个按钮选中
        unLimitedUI();
    }

    /**
     * 无条件ui
     */
    private void unLimitedUI() {
        mBinding.tvJoingroupTips.setText(LocaleController.getString("create_group_tips_unlimit", R.string.create_group_tips_unlimit));
        mBinding.tvJoingroupTips.setPadding(SizeUtils.dp2px(20), SizeUtils.dp2px(8), SizeUtils.dp2px(20), 0);
        mBinding.llJoinType.setVisibility(GONE);
        mBinding.llJoinContent.setVisibility(GONE);
    }

    /**
     * 条件加群
     */
    private void conditionUI() {
        mBinding.tvJoingroupTips.setText(LocaleController.getString("create_group_tips_conditions", R.string.create_group_tips_conditions));
        mBinding.tvJoingroupTips.setPadding(SizeUtils.dp2px(20), SizeUtils.dp2px(8), SizeUtils.dp2px(20), SizeUtils.dp2px(18));
        mBinding.tvJoinTitle.setText(LocaleController.getString("create_group_conditionjoin_group", R.string.create_group_conditionjoin_group));
        mBinding.tvJoinContentTitle.setText(LocaleController.getString("create_group_conditions_described", R.string.create_group_conditions_described));

        mBinding.llJoinType.setVisibility(VISIBLE);
        mBinding.llJoinContent.setVisibility(VISIBLE);
        mBinding.rlWalletAddress.setVisibility(GONE);
        mBinding.lineWalletAddress.setVisibility(GONE);
        mBinding.rlSelectChainType.setVisibility(VISIBLE);
        mBinding.rlSelectTokenType.setVisibility(VISIBLE);

        defultValueSet();
    }

    /**
     * 支付入群
     */
    private void payLimitUI() {
        mBinding.tvJoingroupTips.setText(LocaleController.getString("create_group_tips_pay", R.string.create_group_tips_pay));
        mBinding.tvJoingroupTips.setPadding(SizeUtils.dp2px(20), SizeUtils.dp2px(8), SizeUtils.dp2px(20), SizeUtils.dp2px(18));
        mBinding.tvJoinTitle.setText(LocaleController.getString("create_group_paytojoin_group", R.string.create_group_paytojoin_group));
        mBinding.tvJoinContentTitle.setText(LocaleController.getString("create_group_pay_described", R.string.create_group_pay_described));

        mBinding.llJoinType.setVisibility(VISIBLE);
        mBinding.rlWalletAddress.setVisibility(VISIBLE);

        setWalletAddress();
    }

    /**
     * 用户有没有绑定过钱包地址，判断显示
     */
    private void setWalletAddress() {
        //获取钱包名称和地址
        walletName = MMKVUtil.connectedWalletPkg();
        walletAddress = MMKVUtil.connectedWalletAddress();

        if (TextUtils.isEmpty(walletAddress)) {
            //没有链接钱包
            mBinding.tvUnbindWallet.setVisibility(VISIBLE);
            mBinding.tvWalletAddress.setVisibility(GONE);
            mBinding.lineWalletAddress.setVisibility(GONE);
            mBinding.rlSelectChainType.setVisibility(GONE);
            mBinding.rlSelectTokenType.setVisibility(GONE);
            mBinding.llJoinContent.setVisibility(GONE);
        } else {
            //有链接钱包
            mBinding.tvUnbindWallet.setVisibility(GONE);
            mBinding.tvWalletAddress.setVisibility(VISIBLE);
            mBinding.lineWalletAddress.setVisibility(VISIBLE);
            mBinding.rlSelectChainType.setVisibility(VISIBLE);
            mBinding.rlSelectTokenType.setVisibility(VISIBLE);
            mBinding.llJoinContent.setVisibility(VISIBLE);

            //显示钱包图标 和 地址
            mBinding.tvWalletAddress.getHelper().setIconNormalLeft(ContextCompat.getDrawable(baseFragment.getParentActivity(), BlockchainConfig.getWalletIconByPkg(walletName)));
            mBinding.tvWalletAddress.setText(WalletUtil.formatAddress(walletAddress));

            defultValueSet();

            for (WalletNetworkConfigEntity.WalletNetworkConfigEntityItem walletData : webConfigData.getWalletType()) {
                if (BlockchainConfig.getWalletTypeByPkg(walletName).equals(walletData.getName())) {
                    //钱包id
                    mCreateGroupApi.setWallet_id(walletData.getId());
                }
            }
            //收款账户
            mCreateGroupApi.setReceipt_account(walletAddress);
            //钱包名称
            mCreateGroupApi.setWallet_name(walletName);
        }
    }

    /**
     * 链 token 币种默认选择
     */
    private List<WalletNetworkConfigEntity.WalletNetworkConfigEntityItem> conditionTokenData = new ArrayList<>();
    private List<WalletNetworkConfigEntity.WalletNetworkConfigEntityItem> payLimitTokenData = new ArrayList<>();
    private void defultValueSet() {
        conditionTokenData.clear();
        payLimitTokenData.clear();

        conditionTokenData.addAll(webConfigData.getTokenType());

        for (WalletNetworkConfigEntity.WalletNetworkConfigEntityItem tokenData : webConfigData.getTokenType()) {
            if (tokenData.getName().equals("ERC20")) {
                payLimitTokenData.add(tokenData);
            }
        }

        //设置token集合数据
        if (mCreateGroupApi.getJoin_type() == 2) {
            mTokenTypeSelectorDialog.setList(conditionTokenData);
        } else {
            mTokenTypeSelectorDialog.setList(payLimitTokenData);
        }

        //默认选择第一条链数据
        setSelectChainDataUI(webConfigData.getChainType().get(0));
        //默认选择第一个token数据
        setSelectTokenDataUI(webConfigData.getTokenType().get(0));
    }

    /**
     * 设置链数据
     * @param selectChainData
     */
    private void setSelectChainDataUI(WalletNetworkConfigEntity.WalletNetworkConfigChainType selectChainData) {
        //设置选中数据给对话框
        mChainTypeSelectorDialog.setCurrentChainType(selectChainData);

        //设置当前链下面的币种列表数据给dialog
        mCoinTypeSelectorDialog.setList(selectChainData.getCurrency());

        //设置选择的链名称
        mBinding.tvSelectChainType.setText(selectChainData.getName());

        //默认选择第一个币种数据
        setSelectCoinDataUI(selectChainData.getCurrency().get(0));

        //set到请求类
        mCreateGroupApi.setChain_id(selectChainData.getId());
        mCreateGroupApi.setChain_name(selectChainData.getName());
    }

    /**
     * 设置token数据
     * @param selectTokenData
     */
    private void setSelectTokenDataUI(WalletNetworkConfigEntity.WalletNetworkConfigEntityItem selectTokenData) {
        //设置选中的token数据
        mTokenTypeSelectorDialog.setCurrentTokenData(selectTokenData);

        //设置选择的token名称
        mBinding.tvSelectTokenType.setText(selectTokenData.getName());

        //set到请求类
        mCreateGroupApi.setToken_id(selectTokenData.getId());
        mCreateGroupApi.setToken_name(selectTokenData.getName());

        if (selectTokenData.getName().equals("ERC20")) { //币种协议布局
            if (mCreateGroupApi.getJoin_type() == 2) { //条件入群
                mBinding.rlSelectorCoinType.setVisibility(VISIBLE);
                mBinding.etInputCoinnumMin.setVisibility(VISIBLE);
                mBinding.rlInputPaynum.setVisibility(GONE);
            } else { //付费入群
                mBinding.rlSelectorCoinType.setVisibility(GONE);
                mBinding.etInputCoinnumMin.setVisibility(GONE);
                mBinding.rlInputPaynum.setVisibility(VISIBLE);
            }
            mBinding.llTokenAddress.setVisibility(GONE);
        } else {//nft协议布局
            mBinding.rlSelectorCoinType.setVisibility(GONE);
            mBinding.etInputCoinnumMin.setVisibility(GONE);
            mBinding.rlInputPaynum.setVisibility(GONE);
            mBinding.llTokenAddress.setVisibility(VISIBLE);
        }
    }

    /**
     * 设置币种数据
     * @param selectCoinData
     */
    private void setSelectCoinDataUI(WalletNetworkConfigEntity.WalletNetworkConfigEntityItem selectCoinData) {
        //设置选中的币种数据
        mCoinTypeSelectorDialog.setCurrentCoinData(selectCoinData);

        //设置选择的coin名称 和 图标
        mBinding.tvSelectCoinType.setText(selectCoinData.getName());
        mBinding.rtvSelectCoinType.setText(selectCoinData.getName());

        //币种图标
        GlideHelper.getDrawableGlide(baseFragment.getParentActivity(), selectCoinData.getIcon(), drawable -> {
            mBinding.rtvSelectCoinType.getHelper().setIconNormalLeft(drawable);
        });

        //set到请求类
        mCreateGroupApi.setCurrency_id(selectCoinData.getId());
        mCreateGroupApi.setCurrency_name(selectCoinData.getName());
    }

    /**
     * 获取webconfig数据
     * @param runnable
     */
    private void obtionWebConfigData(Runnable runnable) {
        //清空请求类所有数据
        mCreateGroupApi = new CreateGroupApi(1);
        mBinding.etInputCoinnumMin.setText("");
        mBinding.etInputPaynum.setText("");
        mBinding.etInputTokenAddress.setText("");

        if (webConfigData == null) {
            mLoadingDialog.show();
            WalletUtil.requestWalletNetworkConfigData(() -> {
                webConfigData = MMKVUtil.getWalletNetworkConfigEntity();
                mLoadingDialog.dismiss();
                runnable.run();
            });
        } else {
            runnable.run();
        }
    }

    /**
     * 设置选中
     * @param selectType
     */
    private void setCheckedEnableLimitCell(int selectType, Runnable runnable) {
        obtionWebConfigData(() -> {
            if (this.selectType == selectType) {
                return;
            }
            this.selectType = selectType;

            for (int i = 0; i < radioCells.size(); i++) {
                radioCells.get(i).setChecked(selectType == i, true);
            }
            runnable.run();
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.WALLET_CONNECT_APPROVED:
            case EventBusTags.WALLET_CONNECT_CLOSED:
                setWalletAddress();
                break;
        }
    }


    /**
     * 获取创建群请求api
     * @return
     */
    public CreateGroupApi getmCreateGroupApi() {
        //群描述
        mCreateGroupApi.setDescription(mBinding.etAddGroupDescribe.getText().toString());

        if (mCreateGroupApi.getJoin_type() == 1) {
            return mCreateGroupApi;
        }

        if (mCreateGroupApi.getJoin_type() == 3) {
            if (StringUtils.isEmpty(mCreateGroupApi.getWallet_name())) {
                ToastUtils.showShort(LocaleController.getString("toast_tips_qx_bindwallet", R.string.toast_tips_qx_bindwallet));
                return null;
            }
        }

        if (mCreateGroupApi.getToken_name().equals("ERC20")) { //代币协议
            //清除token地址数据
            mCreateGroupApi.setToken_address("");
            if (mCreateGroupApi.getJoin_type() == 2) {
                String minNum = mBinding.etInputCoinnumMin.getText().toString();
                if (StringUtils.isEmpty(minNum)) {
                    ToastUtils.showShort(LocaleController.getString("toast_tips_qsr_min_tokennum", R.string.toast_tips_qsr_min_tokennum));
                    return null;
                } else {
                    if (WalletUtil.decimalCompareTo(new BigDecimal(minNum), new BigDecimal(0))) {
                        mCreateGroupApi.setAmount(minNum);//最小输入金额
                    } else {
                        ToastUtils.showShort(LocaleController.getString("toast_tips_qx_min_token_must", R.string.toast_tips_qx_min_token_must));
                        return null;
                    }
                }
            } else {
                String payNum = mBinding.etInputPaynum.getText().toString();
                if (StringUtils.isEmpty(payNum)) {
                    ToastUtils.showShort(LocaleController.getString("toast_tips_qsr_paynum", R.string.toast_tips_qsr_paynum));
                    return null;
                } else {
                    mCreateGroupApi.setAmount(payNum);//需要支付的数量
                }
            }
        } else {
            //清除代币相关数据
            mCreateGroupApi.setAmount("");
            mCreateGroupApi.setCurrency_name("");
            mCreateGroupApi.setCurrency_id(0L);
            String tokenAddress = mBinding.etInputTokenAddress.getText().toString();
            if (StringUtils.isEmpty(tokenAddress)) {
                ToastUtils.showShort(LocaleController.getString("toast_tips_please_input_tokenaddress", R.string.toast_tips_please_input_tokenaddress));
                return null;
            } else {
                mCreateGroupApi.setToken_address(tokenAddress);//合约地址
            }
        }

        return mCreateGroupApi;
    }
}
