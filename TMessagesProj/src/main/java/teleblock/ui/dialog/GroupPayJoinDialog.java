package teleblock.ui.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.flyco.tablayout.listener.OnTabSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.DialogGroupPayJoinBinding;
import org.telegram.messenger.databinding.DialogUserProfileBinding;
import org.telegram.messenger.databinding.ItemGroupJoinTagBinding;
import org.telegram.messenger.databinding.ViewPrivateGroupTagItemBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;

import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.PrivateGroupEntity;
import teleblock.translate.model.BingEntity;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2022/7/15
 * 描述：付费入群
 */
public class GroupPayJoinDialog extends BaseBottomSheetDialog implements View.OnClickListener {

    private DialogGroupPayJoinBinding binding;
    private PrivateGroupEntity privateGroup;
    private BaseFragment fragment;

    public GroupPayJoinDialog(BaseFragment fragment, PrivateGroupEntity privateGroup) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        this.privateGroup = privateGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGroupPayJoinBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
        binding.tvTagTitle.setText(LocaleController.getString("group_pay_join_tag_title", R.string.group_pay_join_tag_title));
        binding.tvAmountTitle.setText(LocaleController.getString("group_pay_join_amount_title", R.string.group_pay_join_amount_title));
        binding.tvPayJoin.setText(LocaleController.getString("group_pay_join_pay_join", R.string.group_pay_join_pay_join));

        binding.tvCloseDialog.setOnClickListener(this);
        binding.tvPayJoin.setOnClickListener(this);
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
        binding.tvPayAmount.setText(privateGroup.getAmount() + " " + privateGroup.getCurrency_name());
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
            case R.id.tv_pay_join:
                new GroupPayConfirmDialog(fragment, privateGroup).show();
                dismiss();
                break;
        }
    }
}