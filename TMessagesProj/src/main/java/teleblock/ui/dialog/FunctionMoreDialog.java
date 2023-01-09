package teleblock.ui.dialog;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ruffian.library.widget.RTextView;
import com.yalantis.ucrop.util.DensityUtil;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ListView.RecyclerListViewWithOverlayDraw;
import org.telegram.ui.Components.RecyclerListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import teleblock.manager.StickerManager;
import teleblock.model.GifStickerEntity;
import teleblock.model.MoreFuncEntity;
import teleblock.ui.adapter.MoreFuncRvAdapter;
import teleblock.ui.adapter.GifHAdapter;


/**
 * Created by LSD on 2021/9/28.
 * Desc
 */
public class FunctionMoreDialog extends BaseBottomSheetDialog {
    private FunctionMoreListener listener;
    private Context context;

    private TextView tv_storage_size;
    private TextView tv_storage_unit;
    private TextView tvStorageScan;
    private RTextView tvStorageClean;
    private TextView tvContactUs;
    private TextView tvOfficialGroup;

    RecyclerView function_rv;
    FrameLayout stickerFrame;


    MoreFuncRvAdapter moreFuncRvAdapter;
    GifHAdapter gifHAdapter;
    AnimationDrawable animVideo;

    public interface FunctionMoreListener {
        void onItemClick(MoreFuncEntity entity);
    }

    public FunctionMoreDialog(@NonNull Context context, FunctionMoreListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_function_more);

        initView();
        loadData();
        loadSize();
        loadGifData();
    }

    private void initView() {
        tv_storage_size = findViewById(R.id.tv_storage_size);
        tv_storage_unit = findViewById(R.id.tv_storage_unit);
        tvStorageScan = findViewById(R.id.tv_storage_scan);
        tvStorageClean = findViewById(R.id.tv_storage_clean);
        tvContactUs = findViewById(R.id.tv_contact_us);
        tvOfficialGroup = findViewById(R.id.tv_official_group);
        RTextView tvCollect = findViewById(R.id.tv_collect);
        LinearLayout dialog_main = findViewById(R.id.dialog_main);
        dialog_main.setOnClickListener(view -> {
        });

        tvStorageScan.setText(LocaleController.getString("dg_storage_scan_tips", R.string.dg_storage_scan_tips));
        tvStorageClean.setText(LocaleController.getString("dg_storage_scan_clean", R.string.dg_storage_scan_clean));
        tvContactUs.setText(LocaleController.getString("view_home_contact_us", R.string.view_home_contact_us));
        tvOfficialGroup.setText(LocaleController.getString("view_home_official_group", R.string.view_home_official_group));
        tvCollect.setText(LocaleController.getString("sticker_dialog_collect", R.string.sticker_dialog_collect));
        tvCollect.getHelper().setTextColorNormal(Theme.getColor(Theme.key_actionBarDefault));

        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_arrow_right_bluev2);
        drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarDefault), PorterDuff.Mode.SRC_IN));
        tvCollect.getHelper().setIconNormalRight(drawable);
        tvCollect.setOnClickListener(view -> {
            dismiss();
            MoreFuncEntity itemEntity = new MoreFuncEntity();
            itemEntity.id = -4;
            listener.onItemClick(itemEntity);
        });

        //intStyle
        tvStorageClean.getHelper().setBackgroundColorNormal(Theme.getColor(Theme.key_chats_actionBackground));

        tvStorageClean.setOnClickListener(view -> {
            dismiss();
            MoreFuncEntity itemEntity = new MoreFuncEntity();//清理缓存
            itemEntity.id = -1;
            listener.onItemClick(itemEntity);
        });
        function_rv = findViewById(R.id.function_rv);
        findViewById(R.id.tv_contact_us).setOnClickListener(view -> {
            dismiss();
            MoreFuncEntity itemEntity = new MoreFuncEntity();//联系我们
            itemEntity.id = -2;
            listener.onItemClick(itemEntity);
        });
        findViewById(R.id.tv_official_group).setOnClickListener(view -> {
            dismiss();
            MoreFuncEntity itemEntity = new MoreFuncEntity();//官方群組
            itemEntity.id = -3;
            listener.onItemClick(itemEntity);
        });
        findViewById(R.id.close_frame).setOnClickListener(view -> dismiss());

        function_rv.setLayoutManager(new GridLayoutManager(context, 3));
        function_rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            int spacing = DensityUtil.dip2px(context, 20);

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });
        function_rv.setAdapter(moreFuncRvAdapter = new MoreFuncRvAdapter(context));
        moreFuncRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            dismiss();
            MoreFuncEntity itemEntity = moreFuncRvAdapter.getItem(position);
            listener.onItemClick(itemEntity);
        });

        initStickerRv();
    }

    private void initStickerRv() {
        stickerFrame = findViewById(R.id.sticker_frame);
        //stickerRv
        RecyclerListView stickerRv = new RecyclerListViewWithOverlayDraw(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                return super.onInterceptTouchEvent(event);
            }
        };
        stickerRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        stickerRv.setGlowColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
        stickerRv.setClipToPadding(false);
        stickerRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            int spacing = ConvertUtils.dp2px(4);

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = spacing;
                outRect.bottom = spacing;
                outRect.top = spacing;
            }
        });
        stickerFrame.addView(stickerRv);
        stickerRv.setAdapter(gifHAdapter = new GifHAdapter());
    }

    private void loadData() {
        List<MoreFuncEntity> list = new ArrayList<>();
        MoreFuncEntity item;

        item = new MoreFuncEntity();
        item.id = 7;
        item.text = LocaleController.getString("dg_storage_more_fun_qrcode", R.string.dg_storage_more_fun_qrcode);
        item.iconRes = R.drawable.ic_func_qrcode;
        item.smallIcon = true;
        list.add(item);

        item = new MoreFuncEntity();
        item.id = 8;
        item.text = LocaleController.getString("dg_storage_more_fun_transfer", R.string.dg_storage_more_fun_transfer);
        item.iconRes = R.drawable.icon_message_transfer_good_tools;
        item.smallIcon = true;
        list.add(item);

        item = new MoreFuncEntity();
        item.id = 2;
        item.text = LocaleController.getString("dg_storage_more_fun_message", R.string.dg_storage_more_fun_message);
        item.iconRes = R.drawable.ic_func_message;
        item.smallIcon = true;
        list.add(item);

        item = new MoreFuncEntity();
        item.id = 6;
        item.text = LocaleController.getString("dg_storage_more_fun_delete_msg", R.string.dg_storage_more_fun_delete_msg);
        item.iconRes = R.drawable.ic_func_delete_msg;
        item.smallIcon = true;
        list.add(item);

        item = new MoreFuncEntity();
        item.id = 0;
        item.text = LocaleController.getString("dg_storage_more_fun_theme", R.string.dg_storage_more_fun_theme);
        item.iconRes = R.drawable.ic_func_theme;
        item.smallIcon = false;
        list.add(item);

        item = new MoreFuncEntity();
        item.id = 1;
        item.text = LocaleController.getString("dg_storage_more_fun_wallpaper", R.string.dg_storage_more_fun_wallpaper);
        item.iconRes = R.drawable.ic_video_wallpaper_anim;
        item.smallIcon = false;
        list.add(item);

        moreFuncRvAdapter.setList(list);
    }

    private void loadGifData() {
        StickerManager.getInstance().loadStickerGifMessageList(1, 20, GifStickerEntity.TYPE_GIF, list -> {
            if (list != null && list.size() > 0) {
                stickerFrame.setVisibility(View.VISIBLE);
                gifHAdapter.setList(list);
            }
        });
    }

    private void loadSize() {
        Utilities.globalQueue.postRunnable(() -> {
            long cacheSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), 0);
            long photoSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_IMAGE), 0);
            long videoSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_VIDEO), 0);
            long documentsSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 1);
            long musicSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_DOCUMENT), 2);
            long stickersSize = getDirectorySize(new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache"), 0);
            long audioSize = getDirectorySize(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_AUDIO), 0);
            long totalSize = cacheSize + videoSize + audioSize + photoSize + documentsSize + musicSize + stickersSize;
            AndroidUtilities.runOnUIThread(() -> {
                if (totalSize > 1024 * 1024 * 5) {
                    String size = AndroidUtilities.formatFileSize(totalSize, false);
                    String[] sizeArr = size.split(" ");
                    if (sizeArr.length > 0) {
                        tv_storage_size.setText(sizeArr[0]);
                    }
                    if (sizeArr.length > 1) {
                        tv_storage_unit.setText(sizeArr[1]);
                    }
                }
                tvStorageScan.setText(LocaleController.getString("dg_storage_total", R.string.dg_storage_total));
            });
        });
    }

    public static String formatFileSize(long size, boolean removeZero) {
        if (size < 1024 * 1024 * 5) {
            return "--  ";
        } else if (size < 1024 * 1024 * 1024) {
            float value = size / 1024.0f / 1024.0f;
            if (removeZero && (value - (int) value) * 10 == 0) {
                return String.format("%d MB", (int) value);
            } else {
                return String.format("%.1f MB", value);
            }
        } else {
            float value = size / 1024.0f / 1024.0f / 1024.0f;
            if (removeZero && (value - (int) value) * 10 == 0) {
                return String.format("%d GB", (int) value);
            } else {
                return String.format("%.1f GB", value);
            }
        }
    }

    @Override
    public void show() {
        super.show();
        resetPeekHeight();
        function_rv.postDelayed(() -> startVideoAmin(), 200);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (animVideo != null) animVideo.stop();
    }

    private void startVideoAmin() {
        RecyclerView.ViewHolder viewHolder = function_rv.findViewHolderForAdapterPosition(5);
        if (viewHolder != null) {
            BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
            ImageView imageView = baseViewHolder.findView(R.id.iv_func);
            animVideo = (AnimationDrawable) imageView.getBackground();
            if (animVideo != null) animVideo.start();
        }
    }

    private long getDirectorySize(File dir, int documentsMusicType) {
        if (dir == null) {
            return 0;
        }
        long size = 0;
        if (dir.isDirectory()) {
            size = Utilities.getDirSize(dir.getAbsolutePath(), documentsMusicType, false);
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }
}
