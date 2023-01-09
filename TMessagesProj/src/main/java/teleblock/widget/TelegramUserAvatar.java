package teleblock.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.StringUtils;
import com.ruffian.library.widget.RFrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import teleblock.config.AppConfig;
import teleblock.database.KKVideoMessageDB;
import teleblock.model.wallet.WalletInfo;

/**
 * Time:2022/10/17
 * Author:Perry
 * Description：telegram用户头像
 */
public class TelegramUserAvatar extends RFrameLayout {

    //默认模式，根据userinfo来获取显示头像
    public static final int DEFAUTL = 0;
    //显示打赏我们的头像
    public static final int SPONSOR_US = 1;
    //地址转账下的头像
    public static final int ADDRESS_TRANSFER = 2;

    //模式
    private int model;

    //用户信息
    private TLRPC.User userInfo;

    //是否显示链图标
    private boolean ifShowChainIcon = true;
    //链的图片 id
    private int chainId = -1;

    //圆形边框宽度 边框颜色
    private int borderWidth = 0;
    private int borderColor = 0;

    public TelegramUserAvatar setUserInfo(TLRPC.User userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public TelegramUserAvatar setModel(int model) {
        this.model = model;
        return this;
    }

    public TelegramUserAvatar setBorder(int borderWidth, @ColorRes int borderColor) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        return this;
    }

    public void setIfShowChainIcon(boolean ifShowChainIcon) {
        this.ifShowChainIcon = ifShowChainIcon;
    }

    public TelegramUserAvatar(@NonNull Context context) {
        super(context);
    }

    public TelegramUserAvatar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TelegramUserAvatar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void loadView() {
        chainId = -1;

        switch (model) {
            case SPONSOR_US://打赏我们的头像
                getHelper().setBackgroundColorNormal(Color.parseColor("#00000000")).setCornerRadius(0);
                removeAllViews();
                ImageView iv = new ImageView(getContext());
                iv.setImageResource(R.drawable.sponsour_logo);
                addView(iv, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
                break;

            case ADDRESS_TRANSFER:
                removeAllViews();
                float margin = AndroidUtilities.dp(5) * 0.68f;

                //钱包图标
                ImageView walletIcon = new ImageView(getContext());
                walletIcon.setImageResource(R.drawable.line_wallet);

                //圆形背景
                getHelper().setBackgroundColorNormal(Color.parseColor("#02ABFF")).setCornerRadius(999);
                //add到主view
                addView(walletIcon,
                        LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER, margin, margin, margin, margin)
                );
                break;

            default:
                getHelper().setBackgroundColorNormal(Color.parseColor("#00000000")).setCornerRadius(0);
                removeAllViews();
                defualtModel();
                break;
        }
    }

    /**
     * 模式模式
     */
    private BackupImageView avatarImageView;
    private void defualtModel() {
        if (userInfo != null) {
            long tgId = userInfo.id;//获取用户ID
            int value = getLayoutParams().height;//加载头像的父布局高度
            //tg原生头像
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setInfo(userInfo);
            avatarImageView = new BackupImageView(getContext());
            avatarImageView.setRoundRadius(AndroidUtilities.dp(value / 2f));
            avatarImageView.setForUserOrChat(userInfo, avatarDrawable);

            //先看看本地库里面有没有这个人的NFT数据
            WalletInfo dataBaseWalletInfo = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).getUserNftData(tgId);
            if (dataBaseWalletInfo.getTg_user_id() == 0L && StringUtils.isEmpty(dataBaseWalletInfo.nft_contract_image) && borderWidth > 0) {
                getHelper().setBorderColorNormal(borderColor)
                        .setBorderWidthNormal(borderWidth)
                        .setCornerRadius(value);
                setPadding(borderWidth, borderWidth, borderWidth, borderWidth);
            } else {
                //获取链ID
                chainId = dataBaseWalletInfo.chain_id;
                avatarImageView.invalidate();
            }

            addView(avatarImageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            showChainIcon();
        }
    }


    /**
     * 显示链图标
     */
    private void showChainIcon() {
        if (chainId != -1 && ifShowChainIcon) {
            //生成链icon图标
            ImageView imageView = new ImageView(getContext());
            float scaleWidth = SizeUtils.px2dp(getWidth() * AppConfig.ViewConfig.COIN_ICON_SCALING);
            float marginTop = scaleWidth * AppConfig.ViewConfig.COIN_ICON_MAGIN_TOP;
            float marginRight = scaleWidth * AppConfig.ViewConfig.COIN_ICON_MAGIN_LEFT;

            //获取链图标
            int chainImgResourse = getResources().getIdentifier("user_chain_logo_" + chainId, "drawable", getContext().getPackageName());
            imageView.setImageResource(chainImgResourse);
            //角标位置
            addView(imageView, LayoutHelper.createFrame((int) scaleWidth, scaleWidth, Gravity.RIGHT, 0f, marginTop, marginRight, 0f));
        }
    }
}
