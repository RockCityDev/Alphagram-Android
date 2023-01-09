
package teleblock.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.google.android.exoplayer2.util.Log;
import com.ruffian.library.widget.RFrameLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.StickerEmojiCell;
import org.telegram.ui.Components.EmojiView;
import org.telegram.ui.Components.ListView.RecyclerListViewWithOverlayDraw;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.ContentPreviewViewer;

import teleblock.event.BindEventBus;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.StickerManager;
import teleblock.model.GifStickerEntity;
import teleblock.ui.adapter.GifOnlyAdapter;
import teleblock.ui.adapter.StickerGifXAdapter;
import teleblock.ui.adapter.StickerOnlyAdapter;


/**
 * Created by LSD on 2022/3/2.
 * Desc
 */
@BindEventBus
public class StickerGifFragment extends BaseFragment {
    private int page = 1;
    private int size = 50;

    private int type;
    private RecyclerListView stickerRv;
    private FrameLayout sticker_container;
    private StickerGifXAdapter stickerGifXAdapter;
    private GifOnlyAdapter gifOnlyAdapter;
    private StickerOnlyAdapter stickerOnlyAdapter;

    public static StickerGifFragment instance(int type) {
        StickerGifFragment fragment = new StickerGifFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_sticker_gif, null);
    }

    @Override
    protected void onViewCreated() {
        type = getArguments().getInt("type");
        initView();
        loadData();
    }

    private ContentPreviewViewer.ContentPreviewViewerDelegate contentPreviewViewerDelegate = new ContentPreviewViewer.ContentPreviewViewerDelegate() {
        @Override
        public void sendSticker(TLRPC.Document sticker, String query, Object parent, boolean notify, int scheduleDate) {
        }

        @Override
        public boolean needSend() {
            return false;
        }

        @Override
        public boolean canSchedule() {
            return false;
        }

        @Override
        public boolean isInScheduleMode() {
            return true;
        }

        @Override
        public void openSet(TLRPC.InputStickerSet set, boolean clearsInputField) {
        }

        @Override
        public void sendGif(Object gif, Object parent, boolean notify, int scheduleDate) {
        }

        @Override
        public void gifAddedOrDeleted() {
        }

        @Override
        public long getDialogId() {
            return 0;
        }

        @Override
        public String getQuery(boolean isGif) {
            return null;
        }

        @Override
        public boolean needMenu() {
            return false;
        }
    };

    private void initView() {
        //预览配置
        ContentPreviewViewer.getInstance().setParentActivity(mActivity);
        ContentPreviewViewer.getInstance().setDelegate(contentPreviewViewerDelegate);

        sticker_container = rootView.findViewById(R.id.sticker_container);
        stickerRv = new RecyclerListViewWithOverlayDraw(mActivity) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                return super.onInterceptTouchEvent(event);
            }
        };
        stickerRv.setGlowColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
        stickerRv.setClipToPadding(false);
        stickerRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = ConvertUtils.dp2px(4);
                outRect.right = ConvertUtils.dp2px(4);
            }
        });
        stickerRv.setOnItemClickListener((view, position) -> {
            if (view instanceof StickerEmojiCell) {
                StickerEmojiCell stickerEmojiCell = (StickerEmojiCell) view;
                ContentPreviewViewer.getInstance().open(stickerEmojiCell.getSticker(), stickerEmojiCell.getStickerPath(), stickerEmojiCell.getEmoji(), null, null, 0, stickerEmojiCell.isRecent(), stickerEmojiCell.getParentObject(), null);
            } else if (view instanceof ContextLinkCell) {
                ContextLinkCell contextLinkCell = (ContextLinkCell) view;
                ContentPreviewViewer.getInstance().open(contextLinkCell.getDocument(), null, null, null, contextLinkCell.getBotInlineResult(), 1, false, contextLinkCell.getBotInlineResult() != null ? contextLinkCell.getInlineBot() : contextLinkCell.getParentObject(), null);
            } else if (view instanceof RFrameLayout) {
                FrameLayout frameLayout = view.findViewById(R.id.gif_frame);
                ContextLinkCell contextLinkCell = (ContextLinkCell) frameLayout.getChildAt(0);
                ContentPreviewViewer.getInstance().open(contextLinkCell.getDocument(), null, null, null, contextLinkCell.getBotInlineResult(), 1, false, contextLinkCell.getBotInlineResult() != null ? contextLinkCell.getInlineBot() : contextLinkCell.getParentObject(), null);
            }
        });
        stickerRv.setItemAnimator(null);//取消item动画
        if (type == GifStickerEntity.TYPE_GIF) {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            stickerRv.setLayoutManager(layoutManager);
            stickerRv.setAdapter(gifOnlyAdapter = new GifOnlyAdapter());
            gifOnlyAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
            gifOnlyAdapter.getLoadMoreModule().setPreLoadNumber(4);
            gifOnlyAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
                page = page + 1;
                loadData();
            });
        } else if (type == GifStickerEntity.TYPE_STICKER) {
            stickerRv.setLayoutManager(new GridLayoutManager(mActivity, 4));
            stickerRv.setAdapter(stickerOnlyAdapter = new StickerOnlyAdapter());
            stickerOnlyAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
            stickerOnlyAdapter.getLoadMoreModule().setPreLoadNumber(4);
            stickerOnlyAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
                page = page + 1;
                loadData();
            });
        } else {
            stickerRv.setLayoutManager(new GridLayoutManager(mActivity, 4));
            stickerRv.setAdapter(stickerGifXAdapter = new StickerGifXAdapter(mActivity));
        }
        sticker_container.addView(stickerRv);
    }

    public void loadData() {
        if (type == GifStickerEntity.TYPE_GIF) {
            StickerManager.getInstance().loadStickerGifMessageList(page, size, type, list -> {
                gifOnlyAdapter.getLoadMoreModule().loadMoreComplete();
                if (list == null || list.size() == 0) return;
                if (page > 1) {
                    gifOnlyAdapter.addData(list);
                } else {
                    gifOnlyAdapter.setList(list);
                }
                if (list.size() < size) {
                    gifOnlyAdapter.getLoadMoreModule().loadMoreEnd(true);
                }
            });
        } else if (type == GifStickerEntity.TYPE_STICKER) {
            StickerManager.getInstance().loadStickerGifMessageList(page, size, type, list -> {
                stickerOnlyAdapter.getLoadMoreModule().loadMoreComplete();
                if (list == null || list.size() == 0) return;
                if (page > 1) {
                    stickerOnlyAdapter.addData(list);
                } else {
                    stickerOnlyAdapter.setList(list);
                }
                if (list.size() < size) {
                    stickerOnlyAdapter.getLoadMoreModule().loadMoreEnd(true);
                }
            });
        } else if (type == 99) {
            StickerManager.getInstance().loadStickerCollectList(list -> {
                stickerGifXAdapter.setNewInstance(list);
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        if (event.getType().equals(EventBusTags.STICKER_GIF_COLLECT_CHANGE) && type == 99) {
            loadData();
        }
    }
}
