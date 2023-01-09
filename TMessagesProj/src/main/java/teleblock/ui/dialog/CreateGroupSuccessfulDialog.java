package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ResourceUtils;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogCreateGroupSuccessfulBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.QRCodeBottomSheet;

import java.io.File;

import teleblock.config.AppConfig;
import teleblock.network.api.CreateGroupApi;
import teleblock.util.GroupShareUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/10/27
 * Author:Perry
 * Description：创建群成功弹出的对话框
 */
public class CreateGroupSuccessfulDialog extends Dialog {

    private DialogCreateGroupSuccessfulBinding binding;
    private BaseFragment baseFragment;

    private CreateGroupApi createGroupApi;
    //群id，我们后台的id，不是tg的id
    private long groupId;

    //二维码弹窗
    private QRCodeBottomSheet qrCodeBottomSheet;

    public CreateGroupSuccessfulDialog(
            @NonNull BaseFragment baseFragment,
            CreateGroupApi createGroupApi,
            long groupId
    ) {
        super(baseFragment.getParentActivity(), R.style.dialog2);
        this.baseFragment = baseFragment;
        this.createGroupApi = createGroupApi;
        this.groupId = groupId;
        binding = DialogCreateGroupSuccessfulBinding.inflate(LayoutInflater.from(baseFragment.getParentActivity()));
        setContentView(binding.getRoot());

        setThemeColor();
        initView();
    }

    private void setThemeColor() {
        binding.vTop.getHelper().setBackgroundColorNormal(baseFragment.getThemedColor(Theme.key_featuredStickers_addButton));
        binding.vBottom.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));
        binding.tvName.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        binding.tvDescription.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteGrayText4));
        binding.line1.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundGray));
        binding.tvGroupTypeTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        binding.tvGroupType.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));

        binding.tvGroupInvitationTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        binding.tvGroupInvitationUrl.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));

        Drawable qrcodeDrawable = ResourceUtils.getDrawable(R.drawable.msg_qrcode);
        qrcodeDrawable.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), PorterDuff.Mode.MULTIPLY));
        binding.ivInvitationQrcode.setImageDrawable(qrcodeDrawable);

        Drawable copyDrawable = ResourceUtils.getDrawable(R.drawable.msg_copy_filled);
        copyDrawable.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.MULTIPLY));
        binding.rtvCopy.getHelper()
                .setIconNormalLeft(copyDrawable)
                .setTextColorNormal(baseFragment.getThemedColor(Theme.key_featuredStickers_buttonText))
                .setBackgroundColorNormal(baseFragment.getThemedColor(Theme.key_featuredStickers_addButton));

        Drawable shareDrawable = ResourceUtils.getDrawable(R.drawable.msg_share_filled);
        shareDrawable.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.MULTIPLY));
        binding.rtvShare.getHelper()
                .setIconNormalLeft(shareDrawable)
                .setTextColorNormal(baseFragment.getThemedColor(Theme.key_featuredStickers_buttonText))
                .setBackgroundColorNormal(baseFragment.getThemedColor(Theme.key_featuredStickers_addButton));
    }

    private void initView() {
        binding.rtvCopy.setText(LocaleController.getString("LinkActionCopy", R.string.LinkActionCopy));
        binding.rtvShare.setText(LocaleController.getString("LinkActionShare", R.string.LinkActionShare));

        binding.tvCloseDialog.setOnClickListener(v -> dismiss());
        //头像
        GlideHelper.displayImage(baseFragment.getContext(), binding.avatarGroup, createGroupApi.getAvatar());
        //群名
        binding.tvName.setText(createGroupApi.getTitle());
        //描述
        if (createGroupApi.getDescription().isEmpty()) {
            binding.tvDescription.getLayoutParams().height = 0;
        } else {
            binding.tvDescription.setText(createGroupApi.getDescription());
        }

        //入群方式
        if (createGroupApi.getJoin_type() == 3) {
            //付费入群
            binding.tvGroupTypeTitle.setText(LocaleController.getString("dialog_create_group_successful_addpaynum", R.string.dialog_create_group_successful_addpaynum));
        } else {
            binding.tvGroupTypeTitle.setText(LocaleController.getString("dialog_create_group_successful_addconding", R.string.dialog_create_group_successful_addconding));
        }

        if (createGroupApi.getJoin_type() == 1) {//无限制入群
            binding.tvGroupType.setText(LocaleController.getString("create_group_join_group", R.string.create_group_join_group));
        } else {
            if (createGroupApi.getToken_name().equals("ERC20")) {
                if (createGroupApi.getJoin_type() == 2) {
                    //条件入群
                    String format = LocaleController.getString("dialog_create_group_successful_condition_join", R.string.dialog_create_group_successful_condition_join);
                    binding.tvGroupType.setText(String.format(format, createGroupApi.getAmount() + " " + createGroupApi.getCurrency_name()));
                } else {
                    //付费入群
                    binding.tvGroupType.setText(createGroupApi.getAmount() + " " + createGroupApi.getCurrency_name());
                }
            } else {
                //nft地址
                binding.tvGroupType.setText(WalletUtil.formatAddress(createGroupApi.getToken_address()));
            }
        }

        //邀请连接标题
        binding.tvGroupInvitationTitle.setText(LocaleController.getString("group_details_invitation_title", R.string.group_details_invitation_title));
        //链接
        String shareUrl = MMKVUtil.getSystemMsg().h5_domain + "?id=" + groupId;
        binding.tvGroupInvitationUrl.setText(shareUrl.substring(0, 19) + "..." + shareUrl.substring(shareUrl.length() - 8));

        //显示二维码
        binding.ivInvitationQrcode.setOnClickListener(v -> {
            if (qrCodeBottomSheet == null) {
                qrCodeBottomSheet = new QRCodeBottomSheet(getContext(), shareUrl, LocaleController.getString("QRCodeLinkHelpGroup", R.string.QRCodeLinkHelpGroup)) {
                    @Override public void dismiss(){
                        super.dismiss();
                        qrCodeBottomSheet = null;
                    }
                };
            }
            qrCodeBottomSheet.show();
        });

        //复制
        binding.rtvCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("label", shareUrl);
            clipboard.setPrimaryClip(clip);
            BulletinFactory.createCopyLinkBulletin(binding.getRoot()).show();
        });

        //分享
        binding.rtvShare.setOnClickListener(v -> {
            //群名称
            String groupName = createGroupApi.getTitle();
            //邀请文案
            String inviter;
            if (groupName.length() > 15) {
                inviter = String.format(LocaleController.getString("group_share_content", R.string.group_share_content), groupName.substring(0, 15) + "...");
            } else {
                inviter = String.format(LocaleController.getString("group_share_content", R.string.group_share_content), groupName);
            }

            GroupShareUtil.getShareImage(
                    getContext(),
                    groupName,
                    createGroupApi.getDescription(),
                    inviter,
                    createGroupApi.getAvatar(),
                    (File file) -> {
                        //存到mmkv里面去
                        MMKVUtil.setGroupShareImgPath(file.getAbsolutePath());
                        MMKVUtil.setGroupShareInviterImgPath(shareUrl);

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, inviter + "\n" + shareUrl);
                        baseFragment.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteToGroupByLink", R.string.InviteToGroupByLink)), AppConfig.GROUP_SHARE_REQUEST);
                    });
        });
    }
}
