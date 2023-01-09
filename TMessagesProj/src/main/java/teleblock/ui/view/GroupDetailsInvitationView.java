package teleblock.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ResourceUtils;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.GroupInvitationViewBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.QRCodeBottomSheet;

import java.io.File;

import teleblock.config.AppConfig;
import teleblock.model.PrivateGroupEntity;
import teleblock.util.GroupShareUtil;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/10/27
 * Author:Perry
 * Description：群组邀请连接view
 */
public class GroupDetailsInvitationView extends ConstraintLayout {
    private GroupInvitationViewBinding binding;
    private BaseFragment baseFragment;

    //群组详情
    private PrivateGroupEntity mGroupDetailsEntity;

    //二维码弹窗
    private QRCodeBottomSheet qrCodeBottomSheet;

    public GroupDetailsInvitationView(@NonNull BaseFragment baseFragment, PrivateGroupEntity mGroupDetailsEntity) {
        super(baseFragment.getParentActivity());
        this.baseFragment = baseFragment;
        this.mGroupDetailsEntity = mGroupDetailsEntity;

        binding = GroupInvitationViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setThemeColor();
        initView();
    }

    private void setThemeColor() {
        binding.getRoot().setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhite));
        binding.tvInvitationTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        binding.tvInvitationUrl.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlackText));

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

        binding.tvInvitationTips.setBackgroundColor(baseFragment.getThemedColor(Theme.key_windowBackgroundGray));
        binding.tvInvitationTips.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteGrayText4));
    }

    private void initView() {
        binding.tvInvitationTitle.setText(LocaleController.getString("group_details_invitation_title", R.string.group_details_invitation_title));
        binding.rtvCopy.setText(LocaleController.getString("LinkActionCopy", R.string.LinkActionCopy));
        binding.rtvShare.setText(LocaleController.getString("LinkActionShare", R.string.LinkActionShare));
        binding.tvInvitationTips.setText(LocaleController.getString("group_details_invitation_tips", R.string.group_details_invitation_tips));

        //链接
        String shareUrl = MMKVUtil.getSystemMsg().h5_domain + "?id=" + mGroupDetailsEntity.getId();
        binding.tvInvitationUrl.setText(shareUrl.substring(0, 19) + "..." + shareUrl.substring(shareUrl.length() - 8));

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
            BulletinFactory.createCopyLinkBulletin(baseFragment).show();
        });

        //分享
        binding.rtvShare.setOnClickListener(v -> {
            //群名称
            String groupName = mGroupDetailsEntity.getTitle();
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
                    mGroupDetailsEntity.getDescription(),
                    inviter,
                    mGroupDetailsEntity.getAvatar(),
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
