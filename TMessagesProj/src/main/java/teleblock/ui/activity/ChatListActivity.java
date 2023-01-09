package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.LayoutBaseRecyclerBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.List;

import teleblock.manager.DialogManager;
import teleblock.model.ChatInfoEntity;
import teleblock.ui.adapter.ChatListAdapter;

/**
 * 创建日期：2022/6/22
 * 描述：
 */
public class ChatListActivity extends BaseFragment implements OnItemClickListener, DialogManager.ChatInfoLoadListener {

    private LayoutBaseRecyclerBinding binding;
    private ChatListAdapter chatListAdapter;

    @Override
    public boolean onFragmentCreate() {
        DialogManager.getInstance(currentAccount).addChatInfoLoadListener(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        DialogManager.getInstance(currentAccount).removeChatInfoLoadListener(this);
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE,true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("所有群组");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = LayoutBaseRecyclerBinding.inflate(LayoutInflater.from(context));
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.addView(binding.getRoot(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        TextView textView = new TextView(context);
        textView.setText("取消请求");
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.BLUE);
        frameLayout.addView(textView, LayoutHelper.createFrame(100, 40, Gravity.BOTTOM | Gravity.END,0,0,0,50));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showLong("接口请求已全部取消");
                DialogManager.getInstance(getCurrentAccount()).cancelRequest();
            }
        });
        fragmentView = frameLayout;
        initData();
        return fragmentView;
    }

    private void initData() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        chatListAdapter = new ChatListAdapter();
        chatListAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(chatListAdapter);

        DialogManager.getInstance(getCurrentAccount()).loadChatDialogs(new DialogManager.Callback<List<ChatInfoEntity>>() {
            @Override
            public void onSuccess(List<ChatInfoEntity> data) {
                chatListAdapter.setList(data);
                int num = Math.min(chatListAdapter.getData().size(), DialogManager.getInstance(getCurrentAccount()).finishLoadChatDialogNum);
                actionBar.setTitle("所有群组" + "(" + data.size() + "/" + num + ")");
            }
        });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
    }


    @Override
    public void updateChatLoad(ChatInfoEntity chatInfo) {
        AndroidUtilities.runOnUIThread(() -> {
            int position = -1;
            List<ChatInfoEntity> chatInfoList = new ArrayList<>(chatListAdapter.getData());
            for (int i = 0; i < chatInfoList.size(); i++) {
                if (chatInfo.chatId == chatInfoList.get(i).chatId) {
                    position = i;
                    break;
                }
            }
            if (position > -1) {
                chatListAdapter.notifyItemChanged(position);
            } else {
                chatListAdapter.addData(chatInfo);
            }
            if (!TextUtils.equals("正在获取", chatInfo.loadMember)) {
//                if (!TextUtils.equals("正在获取", chatInfo.loadAdmin)) {
                    DialogManager.getInstance(getCurrentAccount()).finishLoadChatDialogNum++;
//                }
            }
            int num = Math.min(chatListAdapter.getData().size(), DialogManager.getInstance(getCurrentAccount()).finishLoadChatDialogNum);
            actionBar.setTitle("所有群组" + "(" + chatListAdapter.getData().size() + "/" + num + ")");
        });
    }
}