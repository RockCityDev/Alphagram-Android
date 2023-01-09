package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.google.android.flexbox.FlexboxLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.ChannelTagEntity;
import teleblock.model.ChannelWithTagEntity;
import teleblock.telegram.channels.ChannelTagManager;
import teleblock.ui.adapter.ChannelMoreTagRvAdapter;
import teleblock.util.EventUtil;

public class ChannelMoreDialog extends Dialog {

//    private TextView tvAddTag;
    private TextView tvAllMedia;
    private TextView tvInchat;
    private TextView tvPb;

    public interface ChannelOpCallback {
        void onSelect(String flag);
    }

    LinearLayout layoutAllMedia;
    Context context;
    ChannelOpCallback callback;
    boolean showMedia;

//    RecyclerView tagRv;
//    ChannelMoreTagRvAdapter tagRvAdapter;
    long channelId;

    public ChannelMoreDialog(@NonNull Context context, long channelId, boolean showMedia, ChannelOpCallback setCallback) {
        super(context, R.style.dialogBottomEnter);
        this.context = context;
        this.callback = setCallback;
        this.channelId = channelId;
        this.showMedia = showMedia;
        setTranslucentStatus();
        setContentView(R.layout.dialog_channel_more);

        initView();
        loadData();
    }

    private void setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {//4.4 全透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void initView() {
//        tvAddTag = findViewById(R.id.tv_add_tag);
        tvAllMedia = findViewById(R.id.tv_all_media);
        tvInchat = findViewById(R.id.tv_inchat);
        tvPb = findViewById(R.id.tv_pb);
//        tvAddTag.setText(LocaleController.getString("dg_channel_add_tag", R.string.dg_channel_add_tag));
        tvAllMedia.setText(LocaleController.getString("dg_channel_all_media", R.string.dg_channel_all_media));
        tvInchat.setText(LocaleController.getString("dg_channel_in_chat", R.string.dg_channel_in_chat));
        tvPb.setText(LocaleController.getString("dg_channel_block", R.string.dg_channel_block));


        findViewById(R.id.holder).setOnClickListener(view -> {
            dismiss();
        });
//        findViewById(R.id.tv_add_tag).setOnClickListener(view -> {
//            dismiss();
//            callback.onSelect("addTag");
//        });
        findViewById(R.id.tv_inchat).setOnClickListener(view -> {
            dismiss();
            callback.onSelect("inChat");
        });
        findViewById(R.id.tv_pb).setOnClickListener(view -> {
            dismiss();
            callback.onSelect("leaveChannel");
        });
        findViewById(R.id.tv_all_media).setOnClickListener(view -> {
            dismiss();
            callback.onSelect("allMedia");
        });
        layoutAllMedia = findViewById(R.id.layoutAllMedia);
        if (showMedia) {
            layoutAllMedia.setVisibility(View.VISIBLE);
        } else {
            layoutAllMedia.setVisibility(View.GONE);
        }

//        //TAG
//        tagRv = findViewById(R.id.tag_rv);
//        tagRv.setLayoutManager(new FlexboxLayoutManager(context));
//        tagRv.addItemDecoration(new RecyclerView.ItemDecoration() {
//            int spacing = SizeUtils.dp2px(8f);
//
//            @Override
//            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//                outRect.right = spacing;
//                outRect.bottom = spacing;
//            }
//        });
//        tagRvAdapter = new ChannelMoreTagRvAdapter(context);
//        tagRvAdapter.setOnItemClickListener((adapter, view, position) -> {
//            ChannelWithTagEntity entity = tagRvAdapter.getItem(position);
//            String secretStr = LocaleController.getString("channel_recommend_tag_secret", R.string.channel_recommend_tag_secret);
//            if (entity.tagId != 0) {
//                //删除标签
//                boolean res = ChannelTagManager.getInstance().deleteChannelWithTag(entity.channelId, entity.tagId);
//                if (res) {
//                    ChannelTagEntity tagEntity = new ChannelTagEntity();
//                    tagEntity.tagName = entity.tagName;
//                    tagEntity.tagId = entity.tagId;
//                    EventBus.getDefault().post(new MessageEvent(EventBusTags.CHANNEL_WITH_TAG_REFRASH, tagEntity));
//                }
//            } else {
//                if (tagRvAdapter.secretModel && !secretStr.equals(entity.tagName)) {
//                    //私密选中模式下，不添加标签
//                    return;
//                }
//                //添加标签
//                boolean res = ChannelTagManager.getInstance().keepTagToDB(entity.tagName);
//                if (!res) {
//                    //已经存在
//                } else {
//                    //更新头部栏
//                    EventBus.getDefault().post(new MessageEvent(EventBusTags.CHANNEL_TAG_REFRASH, false));
//                }
//                if (secretStr.equals(entity.tagName)) {
//                    //添加秘密标签，把其他的删掉
//                    ChannelTagManager.getInstance().deleteChannelWithTagByChannelId(channelId);
//                }
//                List<ChannelTagEntity> list = ChannelTagManager.getInstance().getTagList();
//                ChannelTagEntity tagEntity = null;
//                for (ChannelTagEntity temp : list) {
//                    if (entity.tagName.equals(temp.tagName)) {
//                        tagEntity = temp;
//                        break;
//                    }
//                }
//                if (tagEntity != null) {
//                    EventUtil.track(context,EventUtil.Even.频道页添加标签, new HashMap<>());
//                    ChannelTagManager.getInstance().keepChannelWithTag(channelId, tagEntity.tagId, tagEntity.tagName);
//                    EventBus.getDefault().post(new MessageEvent(EventBusTags.CHANNEL_WITH_TAG_REFRASH, tagEntity));
//                }
//            }
//            loadData();
//        });
//        tagRv.setAdapter(tagRvAdapter);
    }

    public void loadData() {
//        boolean secret = false;
//        String[] tagArr = LocaleController.getString("array_channel_recommend_tags", R.string.array_channel_recommend_tags).split("\\|");
//        List<ChannelWithTagEntity> list = ChannelTagManager.getInstance().getTagsByChannelId(channelId);
//
//        String secretStr = LocaleController.getString("channel_recommend_tag_secret", R.string.channel_recommend_tag_secret);
//        for (ChannelWithTagEntity withTagEntity : list) {
//            if (secretStr.equals(withTagEntity.tagName)) {
//                secret = true;
//                break;
//            }
//        }
//
//        List<ChannelWithTagEntity> newList = new ArrayList<>();
//        for (String tag : tagArr) {
//            ChannelWithTagEntity tmp = null;
//            for (ChannelWithTagEntity withTagEntity : list) {
//                if (tag.equals(withTagEntity.tagName)) {
//                    tmp = withTagEntity;
//                    break;
//                }
//            }
//            if (tmp != null) {
//                newList.add(tmp);
//            } else {
//                ChannelWithTagEntity withTagEntity = new ChannelWithTagEntity();
//                withTagEntity.tagName = tag;
//                newList.add(withTagEntity);
//            }
//        }
//        tagRvAdapter.setDataList(newList, secret);
    }

}
