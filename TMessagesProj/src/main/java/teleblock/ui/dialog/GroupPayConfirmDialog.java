package teleblock.ui.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.DialogGroupPayConfirmBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.util.List;

import teleblock.blockchain.Web3TransactionUtils;
import teleblock.config.Constants;
import teleblock.manager.PayerGroupManager;
import teleblock.model.PrivateGroupEntity;
import teleblock.network.api.OrderPostApi;
import teleblock.ui.activity.WalletBindAct;
import teleblock.util.MMKVUtil;
import teleblock.blockchain.WCSessionManager;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2022/7/15
 * 描述：支付订单确认
 */
public class GroupPayConfirmDialog extends BaseBottomSheetDialog implements View.OnClickListener {

    private DialogGroupPayConfirmBinding binding;
    private PrivateGroupEntity privateGroup;
    private BaseFragment fragment;
    private int payStatus;
    private int seconds = 60;
    private ThreadUtils.Task task;

    public GroupPayConfirmDialog(BaseFragment fragment, PrivateGroupEntity privateGroup) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        this.privateGroup = privateGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGroupPayConfirmBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("group_pay_confirm_title", R.string.group_pay_confirm_title));
        binding.tvPermitTitle.setText(LocaleController.getString("group_pay_confirm_permit_title", R.string.group_pay_confirm_permit_title));
        binding.tvStatusTitle.setText(LocaleController.getString("group_pay_confirm_permit_title", R.string.group_pay_confirm_status_title));

        binding.ivCloseDialog.setOnClickListener(this);
        binding.tvPayStart.setOnClickListener(this);
        binding.tvPayCancel.setOnClickListener(this);
    }

    private void initData() {
        GlideHelper.displayImage(getContext(), binding.ivGroupAvatar, privateGroup.getAvatar());
        binding.tvGroupName.setText(privateGroup.getTitle());
        GlideHelper.getDrawableGlide(getContext(), privateGroup.getCurrency_icon(), drawable -> {
            binding.tvPayAmount.getHelper().setIconNormalLeft(drawable);
        });

        binding.tvPayAmount.setText(privateGroup.getAmount());
        String format = LocaleController.getString("group_pay_confirm_pay_member_count", R.string.group_pay_confirm_pay_member_count);
        binding.tvPayMemberCount.setText(String.format(format, privateGroup.getShip()));
        binding.tvPayTime.setText(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("MM/dd HH:mm")));
        binding.tvFromAccount.setText(WalletUtil.formatAddress(MMKVUtil.connectedWalletAddress()));
        binding.tvToAccount.setText(WalletUtil.formatAddress(privateGroup.getReceipt_account()));
        updatePayStatus(-1);
    }

    private void updatePayStatus(int payStatus) {
        this.payStatus = payStatus;
        if (payStatus == -1) {
            binding.tvPayStatus.setText(LocaleController.getString("group_pay_confirm_pay_off", R.string.group_pay_confirm_pay_off));
            binding.tvPayStart.setText(LocaleController.getString("group_pay_confirm_pay_confirm", R.string.group_pay_confirm_pay_confirm));
            binding.tvPaying.setText(LocaleController.getString("group_pay_confirm_paying", R.string.group_pay_confirm_paying));
            binding.tvPaySuccess.setText(LocaleController.getString("group_pay_confirm_pay_success", R.string.group_pay_confirm_pay_success));
            binding.tvPayCancel.setText(LocaleController.getString("group_pay_confirm_pay_cancel", R.string.group_pay_confirm_pay_cancel));
        } else if (payStatus == 0) {
            binding.tvPayStatus.setText(LocaleController.getString("group_pay_confirm_pay_off", R.string.group_pay_confirm_pay_off));
            binding.tvPayStatus.setTextColor(Color.parseColor("#FFD233"));
            binding.tvPayStart.setVisibility(View.GONE);
            binding.llPaying.setVisibility(View.VISIBLE);
        } else if (payStatus == 1) {
            binding.tvPayStatus.setText(LocaleController.getString("group_pay_confirm_pay_fail", R.string.group_pay_confirm_pay_fail));
            binding.tvPayStatus.setTextColor(Color.parseColor("#FF5F5F"));
            binding.tvPayStart.setVisibility(View.VISIBLE);
            binding.tvPayStart.setText(LocaleController.getString("group_pay_confirm_pay_continue", R.string.group_pay_confirm_pay_continue));
            binding.llPaying.setVisibility(View.GONE);
            binding.tvPayCancel.setText(LocaleController.getString("group_pay_confirm_pay_on", R.string.group_pay_confirm_pay_on));
        } else if (payStatus == 2) {
            binding.tvPayStatus.setText(LocaleController.getString("group_pay_confirm_pay_success", R.string.group_pay_confirm_pay_success));
            binding.tvPayStatus.setTextColor(Color.parseColor("#44D320"));
            binding.tvPaySuccess.setVisibility(View.VISIBLE);
            binding.llPaying.setVisibility(View.GONE);
            binding.tvPayCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close_dialog:
                dismiss();
                break;
            case R.id.tv_pay_start:
                if (MMKVUtil.connectedWalletAddress().isEmpty()) {
                    fragment.presentFragment(new WalletBindAct());
                    dismiss();
                    return;
                }
                WCSessionManager.getInstance().getAccounts(new WCSessionManager.Callback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> data) {
                        String address = CollectionUtils.find(data, item -> item.equals(MMKVUtil.connectedWalletAddress()));
                        if (address == null) {
                            AndroidUtilities.runOnUIThread(() -> {
                                AppUtils.launchApp(AppUtils.getAppPackageName());
                                new ErrorWalletAddressDialog(ActivityUtils.getTopActivity(), ErrorWalletAddressDialog.TYPE_ERROR_BIND_ADDRESS) {
                                    @Override
                                    public void onConfirm() {
                                        super.onConfirm();
                                        WalletUtil.goToWallet();
                                    }
                                }.show();
                            }, 200);
                            return;
                        }
                        WCSessionManager.getInstance().getChainId(true, new WCSessionManager.Callback<String>() {
                            @Override
                            public void onSuccess(String data) {
                                String chainId = Numeric.toBigInt(data).toString();
                                if (!chainId.equals(privateGroup.getChain_id() + "")) {
                                    WCSessionManager.getInstance().switchNetwork(privateGroup.getChain_id() + "", new WCSessionManager.Callback<String>() {
                                        @Override
                                        public void onSuccess(String data) {
                                            sendTransaction();
                                        }
                                    });
                                } else {
                                    sendTransaction();
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
                break;
            case R.id.tv_pay_cancel:
                if (payStatus == 1) {
                    Browser.openUrl(getContext(), Constants.getOfficialGroup(), true);
                }
                dismiss();
                break;
        }
    }

    private void sendTransaction() {
        updatePayStatus(0);
        String to = privateGroup.getReceipt_account();
        String data = "";
        if (!TextUtils.isEmpty(privateGroup.getContract_address())) {
            to = privateGroup.getContract_address();
            data = Web3TransactionUtils.encodeTransferData(
                    privateGroup.getReceipt_account(),
                    new BigDecimal(WalletUtil.toWei(privateGroup.getAmount(), privateGroup.getDecimal())).toBigInteger()
            );
        }
        WCSessionManager.getInstance().sendTransaction(to, privateGroup.getAmount(), data, new WCSessionManager.Callback<String>() {
            @Override
            public void onSuccess(String data) {
                EasyHttp.post(new ApplicationLifecycle())
                        .api(new OrderPostApi()
                                .setGroup_id(privateGroup.getId())
                                .setTx_hash(data)
                                .setPayment_account(MMKVUtil.connectedWalletAddress())
                        ).request(new OnHttpListener<String>() {
                            @Override
                            public void onSucceed(String result) {
                                PayerGroupManager.getInstance(fragment.getCurrentAccount()).addTxHash(data);
                            }

                            @Override
                            public void onFail(Exception e) {

                            }
                        });
                updatePayStatus(2);
                new UplinkVerificationDialog(getContext()).show();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                updatePayStatus(1);
            }
        });
    }

    @Override
    public void show() {
        super.show();
        resetPeekHeight();
    }

    @Override
    public void dismiss() {
        if (task != null) {
            task.cancel();
        }
        super.dismiss();
    }
}