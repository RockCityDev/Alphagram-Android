package teleblock.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import teleblock.file.KKFileMessage;
import teleblock.util.SystemUtil;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDataManager;
import teleblock.widget.CircleProgressBar;

/**
 * 首页视频列表4，备注下以免忘记
 */
public class VideoRvAdapter2 extends BaseDelegateMultiAdapter<KKFileMessage, BaseViewHolder> implements LoadMoreModule {
    public final int VIDEO4_ITEM_STYLE1 = 1;
    public final int VIDEO4_ITEM_STYLE2 = 2;

    Context context;

    public VideoRvAdapter2(Context context) {
        this.context = context;
        initDelegate();
    }

    private void initDelegate() {
        // 第一步，设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<KKFileMessage>() {
            @Override
            public int getItemType(@NotNull List<? extends KKFileMessage> data, int position) {
                return position % 2 == 0 ? VIDEO4_ITEM_STYLE1 : VIDEO4_ITEM_STYLE2;
            }
        });
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()
                .addItemType(VIDEO4_ITEM_STYLE1, R.layout.view_video4_item_style1)
                .addItemType(VIDEO4_ITEM_STYLE2, R.layout.view_video4_item_style2);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, KKFileMessage entity) {
        switch (baseViewHolder.getItemViewType()) {
            case VIDEO4_ITEM_STYLE1:
            case VIDEO4_ITEM_STYLE2:
                showItem(baseViewHolder, entity);
                break;
        }
    }

    public KKFileMessage getMessageByFileName(String fileName) {
        for (KKFileMessage kkVideoMessage : getData()) {
            if (!kkVideoMessage.isDateMessage() && fileName.equals(kkVideoMessage.getDownloadFileName())) {
                return kkVideoMessage;
            }
        }
        return null;
    }

    public void notifyItemStatusChanged(String fileName) {
        int position = -1;
        List<KKFileMessage> list = getData();
        for (int i = 0; i < list.size(); i++) {
            KKFileMessage kkVideoMessage = list.get(i);
            if (!kkVideoMessage.isDateMessage() && fileName.equals(kkVideoMessage.getDownloadFileName())) {
                position = i;
                break;
            }
        }
        if (position > -1) {
            notifyItemChanged(position);
        }
    }

    public List<KKFileMessage> getStatusMessage(KKFileDownloadStatus.Status status) {
        if (status == null) return getData();
        List<KKFileMessage> result = new ArrayList<>();
        for (KKFileMessage kkVideoMessage : getData()) {
            if (!kkVideoMessage.isDateMessage() && kkVideoMessage.getDownloadStatus() != null && status == kkVideoMessage.getDownloadStatus().getStatus()) {
                result.add(kkVideoMessage);
            }
        }
        return result;
    }

    private void showItem(BaseViewHolder baseViewHolder, KKFileMessage entity) {
        CircleProgressBar circle_progress = baseViewHolder.findView(R.id.circle_progress);
        ImageView iv_video_status = baseViewHolder.findView(R.id.iv_video_status);
        FrameLayout icon_frame = baseViewHolder.findView(R.id.icon_frame);
        FrameLayout image_frame = baseViewHolder.findView(R.id.image_container);

        //封面
        BackupImageView ivThumb = new BackupImageView(context);
        if (entity.getDocument() != null && entity.getDocument().thumbs != null) {
            TLRPC.PhotoSize bigthumb = FileLoader.getClosestPhotoSizeWithSize(entity.getDocument().thumbs, 320);
            TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(entity.getDocument().thumbs, 40);
            if (thumb == bigthumb) {
                bigthumb = null;
            }
            ivThumb.setRoundRadius(AndroidUtilities.dp(4), AndroidUtilities.dp(4), 0, 0);
            ivThumb.getImageReceiver().setNeedsQualityThumb(bigthumb == null);
            ivThumb.getImageReceiver().setShouldGenerateQualityThumb(bigthumb == null);
            ivThumb.setImage(ImageLocation.getForDocument(bigthumb, entity.getDocument()), "256_142", ImageLocation.getForDocument(thumb, entity.getDocument()), "256_142_b", null, 0, 1, entity.getMessageObject());
            image_frame.removeAllViews();
            image_frame.addView(ivThumb);
        }

        //视频大小
        baseViewHolder.setText(R.id.tv_video_size, SystemUtil.getSizeFormat(entity.getSize()));

        //视频长度
        baseViewHolder.setText(R.id.tv_video_time, SystemUtil.timeTransfer(entity.getMediaDuration()));

        //视频查看数
        baseViewHolder.setText(R.id.tv_views, entity.getViewNumber() + "");

        //发布时间
        long time = entity.getMessageDate();
        if (time == 0) {
            baseViewHolder.setGone(R.id.tv_time, true);
        } else {
            baseViewHolder.setGone(R.id.tv_time, false);
            String timeStr;
            if (TimeUtils.isToday(time * 1000)) {
                SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat_am", R.string.dateformat_am), Locale.ENGLISH);
                timeStr = formatter.format(time * 1000);
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
                timeStr = formatter.format(time * 1000);
            }
            baseViewHolder.setText(R.id.tv_time, timeStr);
        }


        //视频状态
        circle_progress.setVisibility(View.GONE);
        iv_video_status.setVisibility(View.GONE);

        if (entity.getDownloadStatus() != null) {
            if (entity.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {//已完成
                iv_video_status.setVisibility(View.VISIBLE);
                iv_video_status.setImageResource(R.drawable.ic_video4_status_play);
                circle_progress.stopAnim();
            } else if (entity.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.NOT_START) {//未开始
                iv_video_status.setVisibility(View.VISIBLE);
                iv_video_status.setImageResource(R.drawable.ic_video4_status_download);//下载
                circle_progress.stopAnim();
            } else if (entity.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.DOWNLOADING) {//下载中
                int pro = (int) (entity.getDownloadStatus().getDownloadedSize() * 1.00 / entity.getDownloadStatus().getTotalSize() * 100);
                if (pro <= 0) pro = 1;
                circle_progress.startShow();
                circle_progress.setProgress(pro);
                KKFileDownloadStatus downloadStatus = entity.getDownloadStatus();
                iv_video_status.setVisibility(View.VISIBLE);
                iv_video_status.setImageResource(R.drawable.ic_video4_status_stop);
                baseViewHolder.setText(R.id.tv_video_size, SystemUtil.getSizeFormat(downloadStatus.getDownloadedSize()) + "/" + SystemUtil.getSizeFormat(downloadStatus.getTotalSize()));
            } else if (entity.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.PAUSE) {//暂停中
                iv_video_status.setVisibility(View.VISIBLE);
                iv_video_status.setImageResource(R.drawable.ic_video4_status_download);//下载
                circle_progress.stopAnim();
            } else if (entity.getDownloadStatus().getStatus() == KKFileDownloadStatus.Status.FAILED) {//已失败
                iv_video_status.setVisibility(View.VISIBLE);
                iv_video_status.setImageResource(R.drawable.ic_video4_status_download);//下载
                circle_progress.stopAnim();
            }
        }

        //描述
        if (TextUtils.isEmpty(entity.getMessage())) {
            baseViewHolder.setGone(R.id.tv_video_text, true);
        } else {
            baseViewHolder.setGone(R.id.tv_video_text, false);
            baseViewHolder.setText(R.id.tv_video_text, entity.getMessage());
        }

        //群名
        baseViewHolder.setText(R.id.tv_group_name, entity.getFromName());

        //头像
        TLRPC.Chat chat = KKVideoDataManager.getInstance().getChat(entity.getDialogId());
        if (chat != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarDrawable.setInfo(chat);
            avatarImageView.setRoundRadius(AndroidUtilities.dp(22));
            avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), "22_22", avatarDrawable, chat);
            icon_frame.removeAllViews();
            icon_frame.addView(avatarImageView);
        }
    }
}
