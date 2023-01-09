package teleblock.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ArticleViewer;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.PhotoViewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import teleblock.model.DeleteMessageEntity;
import teleblock.telegram.channels.ExtendMessageType;
import teleblock.util.SystemUtil;
import teleblock.video.KKVideoDataManager;
import teleblock.widget.CircleProgressBar;
import teleblock.widget.ExpandableTextView;

public class DeleteMsgListRvAdapter extends BaseDelegateMultiAdapter<DeleteMessageEntity, BaseViewHolder> implements LoadMoreModule {
    public static final int TYPE_LIST[] = {0, 1, 3, 8, 9, 10, 13};

    public static final int ITEM_TYPE_UNKNOWN = -1;//未知
    public static final int ITEM_TYPE_TEXT = 0;//文本
    public static final int ITEM_TYPE_IMAGE = 1;//图片
    public static final int ITEM_TYPE_VIDEO = 3;//视频
    public static final int ITEM_TYPE_IMAGE8 = 8;//GIF
    public static final int ITEM_TYPE_IMAGE13 = 13;//贴纸
    public static final int ITEM_TYPE_IMAGE100 = 100;//Photo

    public static final int ITEM_TYPE_FILE = 9;//文件
    public static final int ITEM_TYPE_TIPS = 10;//提示消息
    public static final int ITEM_TYPE_FORWARD = ExtendMessageType.FORWARD;//转发消息

    Context context;

    public DeleteMsgListRvAdapter(Context context) {
        this.context = context;
        initDelegate();
    }

    private void initDelegate() {
        // 第一步，设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<DeleteMessageEntity>() {
            @Override
            public int getItemType(@NotNull List<? extends DeleteMessageEntity> data, int position) {
                DeleteMessageEntity message = data.get(position);
                if (message.getMessageType() == ITEM_TYPE_TEXT) {
                    if (message.messageObject.messageOwner.media != null && message.messageObject.messageOwner.media.webpage != null) {
                        return ITEM_TYPE_FORWARD;
                    } else {
                        return ITEM_TYPE_TEXT;
                    }
                } else if (message.getMessageType() == ITEM_TYPE_IMAGE || message.getMessageType() == ITEM_TYPE_IMAGE8 || message.getMessageType() == ITEM_TYPE_IMAGE13 || message.getMessageType() == ITEM_TYPE_IMAGE100) {
                    return ITEM_TYPE_IMAGE;
                } else if (message.getMessageType() == ITEM_TYPE_VIDEO) {
                    return ITEM_TYPE_VIDEO;
                } else if (message.getMessageType() == ITEM_TYPE_FILE) {
                    return ITEM_TYPE_FILE;
                } else if (message.getMessageType() == ITEM_TYPE_TIPS) {
                    return ITEM_TYPE_TIPS;
                }
                return ITEM_TYPE_UNKNOWN;
            }
        });
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()
                .addItemType(ITEM_TYPE_UNKNOWN, R.layout.view_channel_item_unknown)
                .addItemType(ITEM_TYPE_TEXT, R.layout.view_channel_item_text)
                .addItemType(ITEM_TYPE_IMAGE, R.layout.view_channel_item_image)
                .addItemType(ITEM_TYPE_VIDEO, R.layout.view_channel_item_video)
                .addItemType(ITEM_TYPE_FILE, R.layout.view_channel_item_file)
                .addItemType(ITEM_TYPE_TIPS, R.layout.view_channel_item_tips)
                .addItemType(ITEM_TYPE_FORWARD, R.layout.view_channel_item_forward);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
        switch (baseViewHolder.getItemViewType()) {
            case ITEM_TYPE_TEXT:
                showTextItem(baseViewHolder, entity);
                break;
            case ITEM_TYPE_IMAGE:
                showImageItem(baseViewHolder, entity);
                break;
            case ITEM_TYPE_VIDEO:
                showVideoItem(baseViewHolder, entity);
                break;
            case ITEM_TYPE_FILE:
                showFileItem(baseViewHolder, entity);
                break;
            case ITEM_TYPE_FORWARD:
                showForwardtem(baseViewHolder, entity);
                break;
            case ITEM_TYPE_TIPS:
                showTipsItem(baseViewHolder, entity);
                break;
            default:
                showUnknown(baseViewHolder, entity);
                break;
        }
    }

    public void showTipsItem(BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
    }

    //NULL
    public void showUnknown(BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
        TextView tv_type = baseViewHolder.findView(R.id.tv_type);
        tv_type.setText("未适配类型：" + entity.getMessageType() + "\n" + entity.getMessageText());
        tv_type.setVisibility(View.GONE);
    }

    //文字样式
    private void showTextItem(BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
        FrameLayout avatar_frame = baseViewHolder.findView(R.id.avatar_frame);
        TextView tv_channel = baseViewHolder.findView(R.id.tv_channel);
        TextView tv_time = baseViewHolder.findView(R.id.tv_time);
        ExpandableTextView expandableTextView = baseViewHolder.findView(R.id.expandableTextView);
        TextView tv_channel_views = baseViewHolder.findView(R.id.tv_channel_views);
        baseViewHolder.setGone(R.id.function_layout,true);
        baseViewHolder.setGone(R.id.iv_channel_more,true);

        //channel头像
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.getDialogId());
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(chat);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, chat);
            }
            avatar_frame.removeAllViews();
            avatar_frame.addView(avatarImageView);
        } else {
            TLRPC.User user = entity.messageObject.messageOwner.from_id.user_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getUser(entity.messageObject.messageOwner.from_id.user_id) : null;
            if (user != null) {
                AvatarDrawable avatarDrawable = new AvatarDrawable();
                BackupImageView avatarImageView = new BackupImageView(context);
                avatarDrawable.setInfo(user);
                if (avatarImageView != null) {
                    avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                    avatarImageView.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, user);
                }
                avatar_frame.removeAllViews();
                avatar_frame.addView(avatarImageView);
            }
        }

        //发送者
        if (!TextUtils.isEmpty(entity.getFromName())) {
            tv_channel.setVisibility(View.VISIBLE);
            tv_channel.setText(entity.getFromName());
            TextPaint tp = tv_channel.getPaint();
            //tp.setFakeBoldText(true);
        } else {
            tv_channel.setVisibility(View.GONE);
        }

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        long time = entity.getMessageObject().messageOwner.date;
        String formatDate = formatter.format(time * 1000);
        tv_time.setText(formatDate);

        //文字内容
        String messageText = entity.getMessageText();
        if (TextUtils.isEmpty(messageText)) {
            expandableTextView.setVisibility(View.GONE);
        } else {
            expandableTextView.setVisibility(View.VISIBLE);
            //处理文字中特殊数据
            if (entity.getMessageObject().messageOwner.entities != null && entity.getMessageObject().messageOwner.entities.size() > 0) {
                SpannableString spannableString = new SpannableString(messageText);
                for (TLRPC.MessageEntity tlEntity : entity.getMessageObject().messageOwner.entities) {
                    if (tlEntity.offset > messageText.length() || tlEntity.offset + tlEntity.length > messageText.length()) {
                        continue;
                    }
                    if (!TextUtils.isEmpty(tlEntity.url)) {
                        spannableString.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View arg0) {
                                //AlertsCreator.showOpenUrlAlert(DialogsActivity.dialogsActivity, tlEntity.url, false, true);
                                Browser.openUrl(context, tlEntity.url, true, true);
                            }
                        }, tlEntity.offset, tlEntity.offset + tlEntity.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#4E9AD4")), tlEntity.offset, tlEntity.offset + tlEntity.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);//颜色
                    } else {
                        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), tlEntity.offset, tlEntity.offset + tlEntity.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                expandableTextView.setText(spannableString);
                expandableTextView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                expandableTextView.setText(messageText);
            }
        }

        //查看人数
        tv_channel_views.setText(entity.getViewNumber() + "");

        //检查是否是2分钟内的消息
        View view = baseViewHolder.findView(R.id.more_message_layout);
        checkJoinTimeMessage(entity, view);
    }

    //图片样式
    private void showImageItem(BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
        FrameLayout avatar_frame = baseViewHolder.findView(R.id.avatar_frame);
        TextView tv_channel = baseViewHolder.findView(R.id.tv_channel);
        TextView tv_time = baseViewHolder.findView(R.id.tv_time);
        ExpandableTextView expandableTextView = baseViewHolder.findView(R.id.expandableTextView);
        TextView tv_expand = baseViewHolder.findView(R.id.tv_expand);
        TextView tv_channel_views = baseViewHolder.findView(R.id.tv_channel_views);
        FrameLayout media_frame = baseViewHolder.findView(R.id.media_frame);
        baseViewHolder.setGone(R.id.function_layout,true);
        baseViewHolder.setGone(R.id.iv_channel_more,true);

        //channel头像
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.getDialogId());
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(chat);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, chat);
            }
            avatar_frame.removeAllViews();
            avatar_frame.addView(avatarImageView);
        } else {
            TLRPC.User user = entity.messageObject.messageOwner.from_id.user_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getUser(entity.messageObject.messageOwner.from_id.user_id) : null;
            if (user != null) {
                AvatarDrawable avatarDrawable = new AvatarDrawable();
                BackupImageView avatarImageView = new BackupImageView(context);
                avatarDrawable.setInfo(user);
                if (avatarImageView != null) {
                    avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                    avatarImageView.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, user);
                }
                avatar_frame.removeAllViews();
                avatar_frame.addView(avatarImageView);
            }
        }

        //发送者
        if (!TextUtils.isEmpty(entity.getFromName())) {
            tv_channel.setVisibility(View.VISIBLE);
            tv_channel.setText(entity.getFromName());
            TextPaint tp = tv_channel.getPaint();
            //tp.setFakeBoldText(true);
        } else {
            tv_channel.setVisibility(View.GONE);
        }

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        long time = entity.getMessageObject().messageOwner.date;
        String formatDate = formatter.format(time * 1000);
        tv_time.setText(formatDate);

        //描述
        String messageText = entity.getMessage();
        if (TextUtils.isEmpty(messageText)) {
            expandableTextView.setVisibility(View.GONE);
        } else {
            expandableTextView.setVisibility(View.VISIBLE);
            expandableTextView.setText(messageText);
        }

        //图片显示
        int sw = ScreenUtils.getScreenWidth();
        media_frame.removeAllViews();
        media_frame.addView(getImageItemView(entity, sw, 0, true));

        //查看人数
        tv_channel_views.setText(entity.getViewNumber() + "");

        //检查是否是2分钟内的消息
        View view = baseViewHolder.findView(R.id.more_message_layout);
        checkJoinTimeMessage(entity, view);
    }

    //视频样式
    private void showVideoItem(BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
        FrameLayout avatar_frame = baseViewHolder.findView(R.id.avatar_frame);
        TextView tv_channel = baseViewHolder.findView(R.id.tv_channel);
        TextView tv_time = baseViewHolder.findView(R.id.tv_time);
        ExpandableTextView expandableTextView = baseViewHolder.findView(R.id.expandableTextView);
        TextView tv_expand = baseViewHolder.findView(R.id.tv_expand);
        TextView tv_channel_views = baseViewHolder.findView(R.id.tv_channel_views);
        FrameLayout media_frame = baseViewHolder.findView(R.id.media_frame);
        baseViewHolder.setGone(R.id.function_layout,true);
        baseViewHolder.setGone(R.id.iv_channel_more,true);

        //channel头像
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.getDialogId());
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(chat);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, chat);
            }
            avatar_frame.removeAllViews();
            avatar_frame.addView(avatarImageView);
        } else {
            TLRPC.User user = entity.messageObject.messageOwner.from_id.user_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getUser(entity.messageObject.messageOwner.from_id.user_id) : null;
            if (user != null) {
                AvatarDrawable avatarDrawable = new AvatarDrawable();
                BackupImageView avatarImageView = new BackupImageView(context);
                avatarDrawable.setInfo(user);
                if (avatarImageView != null) {
                    avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                    avatarImageView.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, user);
                }
                avatar_frame.removeAllViews();
                avatar_frame.addView(avatarImageView);
            }
        }

        //发送者
        if (!TextUtils.isEmpty(entity.getFromName())) {
            tv_channel.setVisibility(View.VISIBLE);
            tv_channel.setText(entity.getFromName());
            TextPaint tp = tv_channel.getPaint();
            //tp.setFakeBoldText(true);
        } else {
            tv_channel.setVisibility(View.GONE);
        }

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        long time = entity.getMessageObject().messageOwner.date;
        String formatDate = formatter.format(time * 1000);
        tv_time.setText(formatDate);

        //描述
        String messageText = entity.getMessage();
        if (TextUtils.isEmpty(messageText)) {
            expandableTextView.setVisibility(View.GONE);
        } else {
            expandableTextView.setVisibility(View.VISIBLE);
            expandableTextView.setText(messageText);
        }

        //视频区域大小
        int sw = ScreenUtils.getScreenWidth();
        media_frame.removeAllViews();
        media_frame.addView(getVideoItemView(entity, sw, 0, true), LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));


        //查看人数
        tv_channel_views.setText(entity.getViewNumber() + "");

        //检查是否是2分钟内的消息
        View view = baseViewHolder.findView(R.id.more_message_layout);
        checkJoinTimeMessage(entity, view);
    }

    //文件样式
    private void showFileItem(BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
        FrameLayout avatar_frame = baseViewHolder.findView(R.id.avatar_frame);
        TextView tv_channel = baseViewHolder.findView(R.id.tv_channel);
        TextView tv_time = baseViewHolder.findView(R.id.tv_time);
        ExpandableTextView expandableTextView = baseViewHolder.findView(R.id.expandableTextView);
        TextView tv_expand = baseViewHolder.findView(R.id.tv_expand);
        TextView tv_channel_views = baseViewHolder.findView(R.id.tv_channel_views);
        LinearLayout file_frame = baseViewHolder.findView(R.id.file_frame);
        baseViewHolder.setGone(R.id.function_layout,true);
        baseViewHolder.setGone(R.id.iv_channel_more,true);

        //channel头像
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.getDialogId());
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(chat);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, chat);
            }
            avatar_frame.removeAllViews();
            avatar_frame.addView(avatarImageView);
        } else {
            TLRPC.User user = entity.messageObject.messageOwner.from_id.user_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getUser(entity.messageObject.messageOwner.from_id.user_id) : null;
            if (user != null) {
                AvatarDrawable avatarDrawable = new AvatarDrawable();
                BackupImageView avatarImageView = new BackupImageView(context);
                avatarDrawable.setInfo(user);
                if (avatarImageView != null) {
                    avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                    avatarImageView.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, user);
                }
                avatar_frame.removeAllViews();
                avatar_frame.addView(avatarImageView);
            }
        }

        //发送者
        if (!TextUtils.isEmpty(entity.getFromName())) {
            tv_channel.setVisibility(View.VISIBLE);
            tv_channel.setText(entity.getFromName());
            TextPaint tp = tv_channel.getPaint();
        } else {
            tv_channel.setVisibility(View.GONE);
        }

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        long time = entity.getMessageObject().messageOwner.date;
        String formatDate = formatter.format(time * 1000);
        tv_time.setText(formatDate);

        //描述
        String messageText = entity.getMessage();
        if (TextUtils.isEmpty(messageText)) {
            expandableTextView.setVisibility(View.GONE);
        } else {
            expandableTextView.setVisibility(View.VISIBLE);
            expandableTextView.setText(messageText);
        }

        //文件列表
        setFileItemView(entity, file_frame);

        //查看人数
        tv_channel_views.setText(entity.getViewNumber() + "");

        //检查是否是2分钟内的消息
        View view = baseViewHolder.findView(R.id.more_message_layout);
        checkJoinTimeMessage(entity, view);
    }

    //转发
    private void showForwardtem(BaseViewHolder baseViewHolder, DeleteMessageEntity entity) {
        FrameLayout avatar_frame = baseViewHolder.findView(R.id.avatar_frame);
        TextView tv_channel = baseViewHolder.findView(R.id.tv_channel);
        TextView tv_time = baseViewHolder.findView(R.id.tv_time);
        ExpandableTextView expandableTextView = baseViewHolder.findView(R.id.expandableTextView);
        TextView tv_expand = baseViewHolder.findView(R.id.tv_expand);
        TextView tv_channel_views = baseViewHolder.findView(R.id.tv_channel_views);
        LinearLayout forward_frame = baseViewHolder.findView(R.id.forward_frame);
        FrameLayout forward_image = baseViewHolder.findView(R.id.forward_image);
        LinearLayout forward_text_layout = baseViewHolder.findView(R.id.forward_text_layout);
        TextView tv_forward_title = baseViewHolder.findView(R.id.tv_forward_title);
        TextView tv_forward_content = baseViewHolder.findView(R.id.tv_forward_content);
        baseViewHolder.setGone(R.id.function_layout,true);
        baseViewHolder.setGone(R.id.iv_channel_more,true);

        //channel头像
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.getDialogId());
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(chat);
            if (avatarImageView != null) {
                avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, chat);
            }
            avatar_frame.removeAllViews();
            avatar_frame.addView(avatarImageView);
        } else {
            TLRPC.User user = entity.messageObject.messageOwner.from_id.user_id != 0 ? MessagesController.getInstance(UserConfig.selectedAccount).getUser(entity.messageObject.messageOwner.from_id.user_id) : null;
            if (user != null) {
                AvatarDrawable avatarDrawable = new AvatarDrawable();
                BackupImageView avatarImageView = new BackupImageView(context);
                avatarDrawable.setInfo(user);
                if (avatarImageView != null) {
                    avatarImageView.setRoundRadius(AndroidUtilities.dp(44));
                    avatarImageView.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "44_44", avatarDrawable, user);
                }
                avatar_frame.removeAllViews();
                avatar_frame.addView(avatarImageView);
            }
        }

        //发送者
        if (!TextUtils.isEmpty(entity.getFromName())) {
            tv_channel.setVisibility(View.VISIBLE);
            tv_channel.setText(entity.getFromName());
            TextPaint tp = tv_channel.getPaint();
        } else {
            tv_channel.setVisibility(View.GONE);
        }

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        long time = entity.getMessageObject().messageOwner.date;
        String formatDate = formatter.format(time * 1000);
        tv_time.setText(formatDate);

        //描述
        String messageText = entity.getMessageText();
        if (TextUtils.isEmpty(messageText)) {
            expandableTextView.setVisibility(View.GONE);
        } else {
            expandableTextView.setVisibility(View.VISIBLE);
            //处理文字中特殊数据
            if (entity.getMessageObject().messageOwner.entities != null && entity.getMessageObject().messageOwner.entities.size() > 0) {
                SpannableString spannableString = new SpannableString(messageText);
                for (TLRPC.MessageEntity tlEntity : entity.getMessageObject().messageOwner.entities) {
                    int end = Math.min(tlEntity.offset + tlEntity.length, messageText.length());
                    if (!TextUtils.isEmpty(tlEntity.url)) {
                        spannableString.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View arg0) {
                                Browser.openUrl(context, tlEntity.url, true, true);
                            }
                        }, tlEntity.offset, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#4E9AD4")), tlEntity.offset, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);//颜色
                    } else if (spannableString.length() > end && end > tlEntity.offset) {
                        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), tlEntity.offset, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                expandableTextView.setText(spannableString);
                expandableTextView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                expandableTextView.setText(messageText);
            }
        }

        //查看人数
        tv_channel_views.setText(entity.getViewNumber() + "");

        forward_frame.setVisibility(View.GONE);
        if (entity.getMessageObject().messageOwner.media != null && entity.getMessageObject().messageOwner.media.webpage != null) {
            forward_frame.setVisibility(View.VISIBLE);
            forward_frame.setOnClickListener(view -> {
                if (entity.getMessageObject().messageOwner.media.webpage.cached_page != null) {
                    ArticleViewer.getInstance().setParentActivity((Activity) context, null);
                    ArticleViewer.getInstance().open(entity.getMessageObject());
                } else {
                    String url = entity.getMessageObject().messageOwner.media.webpage.url;
                    if (!TextUtils.isEmpty(url)) {
                        Browser.openUrl((Activity) context, url, true, true);
                    }
                }
            });
            //转发的图片
            forward_image.setVisibility(View.GONE);
            if (entity.getMessageObject().messageOwner.media.webpage.photo != null) {
                forward_image.setVisibility(View.VISIBLE);
                MessageObject object = entity.getMessageObject();
                TLRPC.Photo photo = entity.getMessageObject().messageOwner.media.webpage.photo;
                //图片显示
                ArrayList<TLRPC.PhotoSize> photoSizeArr = photo.sizes;
                if (photoSizeArr != null && photoSizeArr.size() > 0) {
                    TLRPC.PhotoSize currentPhotoObject = photoSizeArr.get(photoSizeArr.size() - 1);//最大的一个
                    TLRPC.PhotoSize currentPhotoObjectThumb = photoSizeArr.get(0);

                    // object.photoThumbsObject
                    TLObject photoParentObject = photo;
                    int w = photoSizeArr.get(photoSizeArr.size() - 1).w;
                    int h = photoSizeArr.get(photoSizeArr.size() - 1).h;
                    int viewW = ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(32);
                    int viewH = 178;
                    if (w != 0 && h != 0) {
                        viewH = h * viewW / w;
                        int maxH = ConvertUtils.dp2px(178);
                        if (viewH >= maxH) {//限制最大高度
                            viewH = maxH;
                        }
                    }
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) forward_image.getLayoutParams();
                    params.width = viewW;
                    params.height = viewH;
                    forward_image.setLayoutParams(params);

                    BackupImageView bImageView = new BackupImageView(context);
                    bImageView.setRoundRadius(AndroidUtilities.dp(4), AndroidUtilities.dp(4), 0, 0);
                    bImageView.getImageReceiver().setNeedsQualityThumb(currentPhotoObject == null);
                    bImageView.getImageReceiver().setShouldGenerateQualityThumb(currentPhotoObject == null);
                    ImageReceiver photoImage = bImageView.getImageReceiver();
                    boolean photoExist = true;
                    String fileName = FileLoader.getAttachFileName(currentPhotoObject);
                    if (object.mediaExists) {
                        //DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
                    } else {
                        photoExist = false;
                    }
                    if (photoExist || !object.loadingCancelled && DownloadController.getInstance(UserConfig.selectedAccount).canDownloadMedia(object) || FileLoader.getInstance(UserConfig.selectedAccount).isLoadingFile(fileName)) {
                        photoImage.setImage(ImageLocation.getForObject(currentPhotoObject, photoParentObject), null, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), null, currentPhotoObject.size, null, object, object.shouldEncryptPhotoOrVideo() ? 2 : 0);
                    } else {
                        if (currentPhotoObjectThumb != null) {
                            photoImage.setImage(null, null, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), null, 0, null, object, object.shouldEncryptPhotoOrVideo() ? 2 : 0);
                        } else {
                            photoImage.setImageBitmap((Drawable) null);
                        }
                    }
                    forward_image.addView(bImageView);
                }
            }

            forward_text_layout.setVisibility(View.GONE);
            //转发标题
            String forwardTitle = entity.getMessageObject().messageOwner.media.webpage.title;
            if (!TextUtils.isEmpty(forwardTitle)) {
                forward_text_layout.setVisibility(View.VISIBLE);
                tv_forward_title.setText(forwardTitle);
                TextPaint tp = tv_forward_title.getPaint();
                tp.setFakeBoldText(true);
            }

            //转发内容
            String forwardContent = entity.getMessageObject().messageOwner.media.webpage.description;
            if (!TextUtils.isEmpty(forwardContent)) {
                forward_text_layout.setVisibility(View.VISIBLE);
                tv_forward_content.setText(forwardContent);
            }

            //检查是否是2分钟内的消息
            View view = baseViewHolder.findView(R.id.more_message_layout);
            checkJoinTimeMessage(entity, view);
        }
    }

    //文件item
    private void setFileItemView(DeleteMessageEntity entity, View fileItem) {
        ImageView iv_centerimg = fileItem.findViewById(R.id.iv_centerimg);
        ProgressBar loading_view = fileItem.findViewById(R.id.loading_view);
        CircleProgressBar circle_progress = fileItem.findViewById(R.id.circle_progress);
        TextView tv_file_name = fileItem.findViewById(R.id.tv_file_name);
        TextView tv_file_size = fileItem.findViewById(R.id.tv_file_size);
        tv_file_name.setText(entity.getFileName());
        tv_file_size.setText(SystemUtil.getSizeFormat(entity.getSize()));


        //状态
        iv_centerimg.setVisibility(View.VISIBLE);
        loading_view.setVisibility(View.GONE);
        circle_progress.setVisibility(View.GONE);
    }

    private PhotoViewer.PhotoViewerProvider photoViewerProvider = new PhotoViewer.EmptyPhotoViewerProvider() {
        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview) {
            return null;
        }
    };

    //视频Item
    private View getVideoItemView(DeleteMessageEntity entity) {
        return getVideoItemView(entity, 0, 0, false);
    }

    private View getVideoItemView(DeleteMessageEntity entity, int viewW, int viewH, boolean single) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_channel_item_video_child, null);
        view.setOnClickListener(tView -> {
            PhotoViewer.getInstance().setParentActivity((Activity) context);
            PhotoViewer.getInstance().openPhoto(entity.messageObject, null, entity.getDialogId(), 0,0, photoViewerProvider);
        });
        FrameLayout image_layout = view.findViewById(R.id.image_layout);
        FrameLayout blurry_image_layout = view.findViewById(R.id.blurry_image_layout);
        TextView tv_size = view.findViewById(R.id.tv_size);
        TextView tv_length = view.findViewById(R.id.tv_length);

        //视频区域大小
        TLRPC.Document document = entity.getMessageObject().getDocument();
        if (document != null) {
            ArrayList<TLRPC.DocumentAttribute> attributes = document.attributes;
            if (attributes != null && attributes.size() > 0) {
                int w = attributes.get(0).w;
                int h = attributes.get(0).h;
                if (w != 0 && h != 0) {
                    if (viewW == 0) {
                        viewW = w * viewH / h;
                    } else if (viewH == 0) {
                        viewH = h * viewW / w;
                        if (h >= w) {//竖屏视频处理下
                            int maxH = ConvertUtils.dp2px(360);
                            if (viewH >= maxH) {
                                viewH = maxH;
                                viewW = w * viewH / h;
                            }
                        }
                    }
                }
            }
            if (viewH == 0) viewH = 320;
            if (viewW == 0) viewW = 320;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            if (single) {//单个视频动态设置宽高，多个视频因为外部已经有限制尺寸了所以就 MATCH_PARENT
                params.width = viewW;
                params.height = viewH;
            }
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            image_layout.setLayoutParams(params);

            //视频封面
            BackupImageView ivThumb = new BackupImageView(context);
            if (document.thumbs != null) {
                TLRPC.PhotoSize bigthumb = FileLoader.getClosestPhotoSizeWithSize(entity.getMessageObject().getDocument().thumbs, 320);
                TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(entity.getMessageObject().getDocument().thumbs, 40);
                if (thumb == bigthumb) {
                    bigthumb = null;
                }
                //ivThumb.setRoundRadius(AndroidUtilities.dp(6), AndroidUtilities.dp(8), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
                ivThumb.getImageReceiver().setNeedsQualityThumb(bigthumb == null);
                ivThumb.getImageReceiver().setShouldGenerateQualityThumb(bigthumb == null);
                ivThumb.setImage(ImageLocation.getForDocument(bigthumb, entity.getMessageObject().getDocument()), null, ImageLocation.getForDocument(thumb, entity.getMessageObject().getDocument()), null, null, 0, 1, entity.getMessageObject());
                if (image_layout.getChildCount() == 0) {
                    image_layout.addView(ivThumb);
                }
            }
        }
        //大小
        tv_size.setText(SystemUtil.getSizeFormat(entity.getSize()));
        //长度
        tv_length.setText(SystemUtil.timeTransfer(entity.getMediaDuration()));
        return view;
    }

    //图片Item
    private View getImageItemView(DeleteMessageEntity entity) {
        return getImageItemView(entity, 0, 0, false);
    }

    private View getImageItemView(DeleteMessageEntity entity, int viewW, int viewH, boolean sigle) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_channel_item_image_child, null);
        view.setOnClickListener(tView -> {
            PhotoViewer.getInstance().setParentActivity((Activity) context);
            PhotoViewer.getInstance().openPhoto(entity.messageObject, null, entity.getDialogId(), 0,0, photoViewerProvider);
        });
        FrameLayout image_layout = view.findViewById(R.id.image_layout);
        MessageObject object = entity.getMessageObject();

        //图片显示
        ArrayList<TLRPC.PhotoSize> photoSizeArr = object.photoThumbs;
        if (photoSizeArr != null && photoSizeArr.size() > 0) {
            TLRPC.PhotoSize currentPhotoObject = photoSizeArr.get(photoSizeArr.size() - 1);//最大的一个
            TLRPC.PhotoSize currentPhotoObjectThumb = photoSizeArr.get(0);
            TLObject photoParentObject = object.photoThumbsObject;
            int w = photoSizeArr.get(photoSizeArr.size() - 1).w;
            int h = photoSizeArr.get(photoSizeArr.size() - 1).h;
            if (w != 0 && h != 0) {
                if (viewW == 0) {
                    viewW = w * viewH / h;
                } else if (viewH == 0) {
                    viewH = h * viewW / w;
                    if (h >= w) {//竖屏图片处理下
                        int maxH = ConvertUtils.dp2px(360);
                        if (viewH >= maxH) {
                            viewH = maxH;
                            viewW = w * viewH / h;
                        }
                    }
                }
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            if (sigle) {
                params.width = viewW;
                params.height = viewH;
                if (viewH > viewW) {
                    //params.setMargins(ConvertUtils.dp2px(16), 0, 0, 0);
                }
                params.addRule(RelativeLayout.CENTER_IN_PARENT);//不居左了
            } else {//多图-用原始尺寸
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
            }
            image_layout.setLayoutParams(params);


            BackupImageView bImageView = new BackupImageView(context);
            bImageView.setBackgroundColor(Color.parseColor("#30999999"));
            bImageView.getImageReceiver().setNeedsQualityThumb(currentPhotoObject == null);
            bImageView.getImageReceiver().setShouldGenerateQualityThumb(currentPhotoObject == null);
            ImageReceiver photoImage = bImageView.getImageReceiver();
            boolean photoExist = true;
            String fileName = FileLoader.getAttachFileName(currentPhotoObject);
            if (object.mediaExists) {
                //DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
            } else {
                photoExist = false;
            }
            if (photoExist || !object.loadingCancelled && DownloadController.getInstance(UserConfig.selectedAccount).canDownloadMedia(object) || FileLoader.getInstance(UserConfig.selectedAccount).isLoadingFile(fileName)) {
                photoImage.setImage(ImageLocation.getForObject(currentPhotoObject, photoParentObject), null, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), null, currentPhotoObject.size, null, object, object.shouldEncryptPhotoOrVideo() ? 2 : 0);
            } else {
                if (currentPhotoObjectThumb != null) {
                    photoImage.setImage(null, null, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), null, 0, null, object, object.shouldEncryptPhotoOrVideo() ? 2 : 0);
                } else {
                    photoImage.setImageBitmap((Drawable) null);
                }
            }
            image_layout.addView(bImageView);
        }
        return view;
    }

    //2分钟内内消息
    private void checkJoinTimeMessage(DeleteMessageEntity channelMessage, View view) {
        view.setVisibility(View.GONE);
        return;
    }
}
