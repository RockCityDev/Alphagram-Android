package teleblock.ui.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.SizeUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teleblock.file.KKFileMessage;
import teleblock.file.KKFileMessageCollectManager;
import teleblock.model.CollectFileEntity;
import teleblock.model.VideoSlipEntity;
import teleblock.ui.activity.VideoSlipPageActivity;
import teleblock.ui.adapter.MyCollectRvAdapter;
import teleblock.util.EventUtil;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDownloadListener;


/**
 * Created by LSD on 2021/3/20.
 * Desc
 */
public class MyCollectFragment extends BaseFragment implements KKVideoDownloadListener {
    private RecyclerView collectRv;
    private SmartRefreshLayout refreshLayout;
    private LinearLayout nullLayout;
    private LinearLayout bottomLayout;
    private TextView tvCheckAll;
    private TextView tvDelete;
    public ImageView ivEdit;
    private TextView tvEmpty;

    private MyCollectRvAdapter videoCollectRvAdapter;
    private List<KKFileMessage> deleteList = new ArrayList<>();
    public boolean edit = false;

    public static MyCollectFragment instance() {
        MyCollectFragment fragment = new MyCollectFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_mycollect, null);
    }

    @Override
    protected void onViewCreated() {
        KKFileMessageCollectManager.getInstance().addDownloadFilesListener(this);    //添加下载完成监听

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initView() {
        bottomLayout = rootView.findViewById(R.id.bottom_layout);
        tvCheckAll = rootView.findViewById(R.id.tv_checkall);
        tvDelete = rootView.findViewById(R.id.tv_dodelete);
        nullLayout = rootView.findViewById(R.id.null_layout);
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        collectRv = rootView.findViewById(R.id.collect_rv);
        rootView.findViewById(R.id.bottom_layout);

        tvEmpty.setText(LocaleController.getString("ac_downing_null_tips", R.string.ac_downing_null_tips));
        tvCheckAll.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
        tvDelete.setText(LocaleController.getString("ac_downed_text_delete", R.string.ac_downed_text_delete));

        (ivEdit = rootView.findViewById(R.id.iv_edit)).setOnClickListener(view -> {
            edit = !edit;
            if (edit) {
                ivEdit.setImageResource(R.drawable.ic_mix_close);
            } else {
                ivEdit.setImageResource(R.drawable.ic_mix_delete);
            }
            notifyCollectDelete(edit);
        });

        tvCheckAll.setOnClickListener(view -> {
            List<CollectFileEntity> list = videoCollectRvAdapter.getData();

            boolean lastCheckAll = false;
            if (deleteList.size() == list.size()) {
                lastCheckAll = true;
            }
            boolean checkAll = !lastCheckAll;
            deleteList.clear();
            for (CollectFileEntity entity : list) {
                if (checkAll) {
                    deleteList.add(entity.message);
                }
                entity.deleteSelect = checkAll;
            }
            if (checkAll) {
                tvCheckAll.setText(LocaleController.getString("ac_downed_text_checkno", R.string.ac_downed_text_checkno));
            } else {
                tvCheckAll.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
            }
            String fixText = deleteList.size() > 0 ? "(" + deleteList.size() + ")" : "";
            tvDelete.setText(LocaleController.getString("ac_downed_text_delete", R.string.ac_downed_text_delete) + fixText);
            videoCollectRvAdapter.notifyDataSetChanged();
        });
        tvDelete.setOnClickListener(view -> {
            if (deleteList.size() > 0) {
                for (KKFileMessage message : deleteList) {
                    KKFileMessageCollectManager.getInstance().removeCollect(message);
                    videoCollectRvAdapter.deleteItem(message.getId());
                }
                deleteList.clear();
                Toast.makeText(mActivity, LocaleController.getString("ac_downed_delete_ok", R.string.ac_downed_delete_ok), Toast.LENGTH_LONG).show();
                tvDelete.setText(LocaleController.getString("ac_downed_text_delete", R.string.ac_downed_text_delete));

                List<CollectFileEntity> list = videoCollectRvAdapter.getData();
                if (list.size() == 0) {
                    nullLayout.setVisibility(View.VISIBLE);
                } else {
                    nullLayout.setVisibility(View.GONE);
                }
            }
        });

        //下拉刷新
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            loadData();
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        collectRv.setLayoutManager(layoutManager);
        collectRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            int spacing = SizeUtils.dp2px(5f);
            int halfSpacing = spacing >> 1;

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = halfSpacing;
                outRect.right = halfSpacing;
                outRect.bottom = halfSpacing;
                outRect.left = halfSpacing;
            }
        });
        collectRv.setAdapter(videoCollectRvAdapter = new MyCollectRvAdapter(mActivity));
        videoCollectRvAdapter.getLoadMoreModule().setEnableLoadMore(false);
        videoCollectRvAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            ivEdit.performClick();
            return true;
        });
        videoCollectRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            CollectFileEntity itemEntity = videoCollectRvAdapter.getItem(position);
            if (videoCollectRvAdapter.isDeleteModel()) {//删除模式
                itemEntity.deleteSelect = !itemEntity.deleteSelect;
                if (itemEntity.deleteSelect) {
                    deleteList.add(itemEntity.message);
                } else {
                    deleteList.remove(itemEntity.message);
                }
                String fixText = deleteList.size() > 0 ? "(" + deleteList.size() + ")" : "";
                tvDelete.setText(LocaleController.getString("ac_downed_text_delete", R.string.ac_downed_text_delete) + fixText);
                videoCollectRvAdapter.notifyItemChanged(position);
            } else {//普通模式点击
                VideoSlipEntity videoSlipEntity = new VideoSlipEntity();
                videoSlipEntity.from = "collectPage";
                videoSlipEntity.title = "我的收藏";
                videoSlipEntity.position = position;
                videoSlipEntity.title = LocaleController.getString("commontools_my_collection", R.string.commontools_my_collection);
                mActivity.startActivity(new Intent(mActivity, VideoSlipPageActivity.class).putExtra("entity", videoSlipEntity));
            }
        });
    }

    private void loadData() {
        KKFileMessageCollectManager.getInstance().loadCollectMessageList(videoMessages -> AndroidUtilities.runOnUIThread(() -> {
            refreshLayout.finishRefresh();
            if (videoMessages.size() == 0) {
                collectRv.setVisibility(View.GONE);
                videoCollectRvAdapter.setList(null);
                nullLayout.setVisibility(View.VISIBLE);
            } else {
                collectRv.setVisibility(View.VISIBLE);
                nullLayout.setVisibility(View.GONE);

                List<CollectFileEntity> list = new ArrayList<>();
                for (KKFileMessage message : videoMessages) {
                    CollectFileEntity entity = new CollectFileEntity();
                    entity.message = message;
                    list.add(entity);
                }
                videoCollectRvAdapter.setList(list);
            }
        }));
    }

    private void notifyCollectDelete(boolean delete) {
        videoCollectRvAdapter.deleteModel(delete);
        if (delete) {
            bottomLayout.setVisibility(View.VISIBLE);
        } else {
            bottomLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
    }
}
