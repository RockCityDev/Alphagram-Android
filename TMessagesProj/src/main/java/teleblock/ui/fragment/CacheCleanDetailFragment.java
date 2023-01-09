package teleblock.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SizeUtils;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.FragmentTgcleanDetailBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.MiddleData;
import teleblock.model.TGCacheEntity;
import teleblock.model.TGCacheFindEntity;
import teleblock.ui.activity.VideoPlayActivity;
import teleblock.ui.adapter.TGCleanGridRvAdapter;
import teleblock.ui.adapter.TGCleanListRvAdapter;
import teleblock.ui.adapter.TGCleanRvAdapter;
import teleblock.util.SystemUtil;
import teleblock.util.ShareUtil;
import teleblock.util.TGLog;


/**
 * Created by LSD on 2021/5/10.
 * Desc
 */
public class CacheCleanDetailFragment extends BaseFragment {

    private FragmentTgcleanDetailBinding binding;
    private int type;
    private TGCacheFindEntity data;

    private TGCleanRvAdapter tgCleanRvAdapter;
    private List<String> selectList = new ArrayList<>();

    public static CacheCleanDetailFragment instance(TGCacheFindEntity data, int type) {
        CacheCleanDetailFragment fragment = new CacheCleanDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", data);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        binding = FragmentTgcleanDetailBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void onViewCreated() {
        Bundle extra = getArguments();
        data = (TGCacheFindEntity) extra.getSerializable("data");
        if (data == null) data = new TGCacheFindEntity();
        type = extra.getInt("type");
        initView();
    }

    private void initView() {
        String totalFormat = LocaleController.getString("ac_tgclean_total_show_format", R.string.ac_tgclean_total_show_format);
        String totalText = String.format(totalFormat, data.list.size(), SystemUtil.getSizeFormat(data.totalSize));
        binding.tvTotal.setText(totalText);
        
        if (data.list.size() == 0) {
            binding.nullLayout.setVisibility(View.VISIBLE);
        }
        binding.tvEmpty.setText(LocaleController.getString("ac_tgclean_null_file", R.string.ac_tgclean_null_file));

        binding.llEditViews.setOnClickListener(v -> {//切换视图
            if (binding.llShowViews.getVisibility() == View.VISIBLE) {
                binding.llShowViews.setVisibility(View.GONE);
            } else {
                binding.llShowViews.setVisibility(View.VISIBLE);
            }
        });

        binding.llViewsGrid.setOnClickListener(v -> {//网格视图
            binding.llShowViews.setVisibility(View.GONE);
            binding.ivEditViews.setImageResource(R.drawable.ic_views_grid);
            binding.tgfileRv.setLayoutManager(new GridLayoutManager(mActivity, 3));
            binding.tgfileRv.setPadding(SizeUtils.dp2px(10), 0, 0, 0);
            binding.tgfileRv.setAdapter(tgCleanRvAdapter = new TGCleanGridRvAdapter(data.list, type));
            setItemClick();
        });
        binding.tvTgcleanViewsGrid.setText(LocaleController.getString("ac_tgclean_views_grid", R.string.ac_tgclean_views_grid));

        binding.llViewsList.setOnClickListener(v -> {//列表视图
            binding.tgfileRv.setPadding(0, 0, 0, 0);
            binding.llShowViews.setVisibility(View.GONE);
            binding.ivEditViews.setImageResource(R.drawable.ic_views_list);
            binding.tgfileRv.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
            binding.tgfileRv.setAdapter(tgCleanRvAdapter = new TGCleanListRvAdapter(data.list, type));
            setItemClick();
        });
        binding.tvTgcleanViewsList.setText(LocaleController.getString("ac_tgclean_views_list", R.string.ac_tgclean_views_list));

        binding.tvSaveToGallery.setText(LocaleController.getString("ac_download_text_save_gallery", R.string.ac_download_text_save_gallery));
        binding.tvSaveToGallery.setOnClickListener(v -> {
            if (selectList == null || selectList.size() == 0) return;
            List<String> saveList = new ArrayList<>(selectList);
            String picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Picture/Telegram";
            File folder = new File(picturePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            new Thread(() -> {
                for (String path : saveList) {
                    String name = SystemUtil.getFileName(path);
                    File newFile = SystemUtil.simpleCopyFile(mActivity, path, picturePath + "/" + name);
                    AndroidUtilities.runOnUIThread(() -> SystemUtil.notifyFileScan(mActivity, newFile));
                }
                AndroidUtilities.runOnUIThread(() -> {
                    Toast.makeText(mActivity, LocaleController.getString("ac_downed_save_to_gallery_ok", R.string.ac_downed_save_to_gallery_ok), Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.CLEAR_CACHE_OK, false));
                });
            }).start();
        });

        binding.tvDoDelete.setText(LocaleController.getString("ac_download_text_delete", R.string.ac_download_text_delete));
        binding.tvDoDelete.setOnClickListener(v -> {
            if (selectList.size() > 0) {
                for (String path : selectList) {
                    SystemUtil.deleteFile(path);
                    String fileName = SystemUtil.getFileName(path);
                    tgCleanRvAdapter.deleteItem(path);
                }
                selectList.clear();
                Toast.makeText(mActivity, LocaleController.getString("ac_downed_delete_ok", R.string.ac_downed_delete_ok), Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new MessageEvent(EventBusTags.CLEAR_CACHE_OK, true));

                List<TGCacheEntity> list = tgCleanRvAdapter.getData();
                if (list.size() == 0) {
                    binding.nullLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.nullLayout.setVisibility(View.GONE);
                }
            }
        });

        binding.tvDoShare.setText(LocaleController.getString("ac_download_text_share", R.string.ac_download_text_share));
        binding.tvDoShare.setOnClickListener(v -> {
            if (selectList.size() > 0) {
                ShareUtil.shareVideo2(mActivity, selectList.get(0));
            }
        });

        binding.tvCheckall.setOnClickListener(v -> {
            if (selectList.size() == tgCleanRvAdapter.getItemCount()) {
                checkAllOrNo(false);
            } else {
                checkAllOrNo(true);
            }
        });

        binding.tvCancel.setOnClickListener(v -> {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.CLEAR_CACHE_OK, false));
            checkAllOrNo(false);
        });

        binding.tgfileRv.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        binding.tgfileRv.setAdapter(tgCleanRvAdapter = new TGCleanListRvAdapter(data.list, type));
        setItemClick();
    }

    private void setItemClick() {
        tgCleanRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            TGCacheEntity itemEntity = tgCleanRvAdapter.getItem(position);
            if (tgCleanRvAdapter.isDeleteModel()) {//删除模式
                itemEntity.checked = !itemEntity.checked;
                if (itemEntity.checked) {
                    selectList.add(itemEntity.path);
                } else {
                    selectList.remove(itemEntity.path);
                }
                tgCleanRvAdapter.notifyItemChanged(position);
                String format = LocaleController.getString("ac_downed_text_check_num", R.string.ac_downed_text_check_num);
                String showText = String.format(format, selectList.size());
                binding.tvSelectNum.setText(showText);
                if (selectList.size() == tgCleanRvAdapter.getItemCount()) {
                    binding.tvCheckall.setText(LocaleController.getString("ac_downed_text_checkno", R.string.ac_downed_text_checkno));
                } else {
                    binding.tvCheckall.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
                }
            } else {//普通模式点击
                if (type == 2) {//视频
                    List<String> dataList = new ArrayList<>();
                    List<TGCacheEntity> list = tgCleanRvAdapter.getData();
                    for (int i = 0; i < list.size(); i++) {
                        TGCacheEntity temp = list.get(i);
                        dataList.add(temp.path);
                    }
                    if (dataList.size() == 0) return;
                    MiddleData.getInstance().playList = dataList;
                    startActivity(new Intent(mActivity, VideoPlayActivity.class).putExtra("position", position));
                } else {//其他
                    try {
                        AndroidUtilities.openForView(new File(itemEntity.path), mActivity);
                    } catch (Exception e) {
                        TGLog.erro(e.getMessage());
                    }
                }
            }
        });
    }

    public void checkAllOrNo(boolean checkAll) {
        List<TGCacheEntity> list = tgCleanRvAdapter.getData();
        selectList.clear();
        for (TGCacheEntity entity : list) {
            if (checkAll) {
                selectList.add(entity.path);
            }
            entity.checked = checkAll;
        }
        tgCleanRvAdapter.notifyDataSetChanged();
        String format = LocaleController.getString("ac_downed_text_check_num", R.string.ac_downed_text_check_num);
        String showText = String.format(format, selectList.size());
        binding.tvSelectNum.setText(showText);

        if (checkAll) {
            binding.tvCheckall.setText(LocaleController.getString("ac_downed_text_checkno", R.string.ac_downed_text_checkno));
        } else {
            binding.tvCheckall.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
        }
    }

    public void notifyEditChange(boolean isEdit) {
        tgCleanRvAdapter.deleteModel(isEdit);
        if (isEdit) {
            binding.llShowsLayout.setVisibility(View.GONE);
            binding.llStateLayout.setVisibility(View.VISIBLE);
            binding.bottomLayout.setVisibility(View.VISIBLE);
        } else {
            binding.llShowsLayout.setVisibility(View.VISIBLE);
            binding.llStateLayout.setVisibility(View.GONE);
            binding.bottomLayout.setVisibility(View.GONE);
        }
    }
}