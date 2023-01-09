package teleblock.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.CollectionUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.DialogGroupConditionJoinBinding;
import org.telegram.messenger.databinding.ItemGroupJoinTagBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.List;

import okhttp3.Call;
import teleblock.model.OrderResultEntity;
import teleblock.model.PrivateGroupEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.GroupAccreditApi;
import teleblock.ui.activity.WalletBindAct;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2022/7/15
 * 描述：条件入群
 */
public class GroupValidateJoinDialog extends BaseBottomSheetDialog implements View.OnClickListener {

    private DialogGroupConditionJoinBinding binding;
    private PrivateGroupEntity privateGroup;
    private BaseFragment fragment;

    public GroupValidateJoinDialog(BaseFragment fragment, PrivateGroupEntity privateGroup) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        this.privateGroup = privateGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGroupConditionJoinBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
        binding.tvTagTitle.setText(LocaleController.getString("group_validate_join_tag_title", R.string.group_validate_join_tag_title));
        binding.tvConditionJoinTitle.setText(LocaleController.getString("group_validate_join_condition_join_title", R.string.group_validate_join_condition_join_title));
        binding.tvJoinSuccess.setText(LocaleController.getString("group_validate_join_join_success", R.string.group_validate_join_join_success));
        binding.tvValidating.setText(LocaleController.getString("group_validate_join_validating", R.string.group_validate_join_validating));
        binding.tvValidateJoin.setText(LocaleController.getString("group_validate_join_validate_join", R.string.group_validate_join_validate_join));
        binding.tvValidateCancel.setText(LocaleController.getString("group_validate_join_validate_cancel", R.string.group_validate_join_validate_cancel));

        binding.tvCloseDialog.setOnClickListener(this);
        binding.tvValidateJoin.setOnClickListener(this);
        binding.tvValidateCancel.setOnClickListener(this);
    }

    private void initData() {
        GlideHelper.displayImage(getContext(), binding.ivGroupAvatar, privateGroup.getAvatar());
        binding.tvGroupName.setText(privateGroup.getTitle());
        binding.tvGroupDesc.setText(privateGroup.getDescription());
        List<String> tagList = privateGroup.getTags();
        if (CollectionUtils.isEmpty(tagList)) {
            binding.tvTagTitle.setVisibility(View.GONE);
            binding.flTag.setVisibility(View.GONE);
        } else {
            for (String tag : tagList) {
                View view = createTagView(tag);
                binding.flTag.addView(view);
            }
        }

        if (privateGroup.getToken_name().equals("ERC20")) {
            String format = LocaleController.getString("group_validate_join_condition_join", R.string.group_validate_join_condition_join);
            binding.tvConditionJoin.setText(String.format(format, privateGroup.getAmount()));
        } else {
            binding.tvConditionJoin.setText("Hold  " + WalletUtil.formatAddress(privateGroup.getToken_address()) + " NFT");
        }
    }

    private View createTagView(String tag) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_group_join_tag, null);
        ItemGroupJoinTagBinding binding = ItemGroupJoinTagBinding.bind(view);
        binding.tvTag.setText(tag);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_close_dialog:
                dismiss();
                break;
            case R.id.tv_validate_join:
                validateJoin();
                break;
            case R.id.tv_validate_cancel:
                dismiss();
                break;
        }
    }

    private void validateJoin() {
        if (MMKVUtil.connectedWalletAddress().isEmpty()) {
            fragment.presentFragment(new WalletBindAct());
            dismiss();
            return;
        }
        binding.llValidating.setVisibility(View.VISIBLE);
        binding.tvValidateCancel.setVisibility(View.VISIBLE);
        binding.tvValidateJoin.setVisibility(View.GONE);
        EasyHttp.post(new ApplicationLifecycle())
                .api(new GroupAccreditApi()
                        .setGroup_id(privateGroup.getId())
                        .setPayment_account(MMKVUtil.connectedWalletAddress())
                ).request(new OnHttpListener<BaseBean<OrderResultEntity>>() {

                    @Override
                    public void onEnd(Call call) {
                        binding.llValidating.setVisibility(View.GONE);
                        binding.tvValidateCancel.setVisibility(View.GONE);
                        binding.tvValidateJoin.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSucceed(BaseBean<OrderResultEntity> result) {
                        if (result.getCode() == 422) {
                            binding.tvValidateJoin.setText(LocaleController.getString("group_validate_join_validate_not_satisfied", R.string.group_validate_join_validate_not_satisfied));
                        } else if (result.getData().ship != null) {
                            binding.tvValidateJoin.setVisibility(View.GONE);
                            binding.tvJoinSuccess.setVisibility(View.VISIBLE);
                            Browser.openUrl(fragment.getParentActivity(), (result.getData().ship.url));
                            dismiss();
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        binding.tvValidateJoin.setText(LocaleController.getString("group_validate_join_validate_fail", R.string.group_validate_join_validate_fail));
                    }
                });
    }
}