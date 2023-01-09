package teleblock.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;

import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.DeletedMessageManager;
import teleblock.model.DeleteMessageEntity;
import teleblock.ui.activity.DeleteMsgListActivity;
import teleblock.ui.adapter.DeleteMsgGroupRvAdapter;


/**
 * Created by LSD on 2022/3/2.
 * Desc
 */
public class DeleteMessageFragment extends BaseFragment {
    public static org.telegram.ui.ActionBar.BaseFragment actBase;
    int type;//0：单聊，1：群聊
    private RecyclerView delete_msg_rv;
    private DeleteMsgGroupRvAdapter deleteMsgGroupRvAdapter;

    public static DeleteMessageFragment instance(org.telegram.ui.ActionBar.BaseFragment actBaseFragment, int type) {
        actBase = actBaseFragment;
        DeleteMessageFragment fragment = new DeleteMessageFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_delete_message, null);
    }

    @Override
    protected void onViewCreated() {
        type = getArguments().getInt("type");
        initView();
        loadData();
    }

    private void initView() {
        delete_msg_rv = rootView.findViewById(R.id.delete_msg_rv);
        delete_msg_rv.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        delete_msg_rv.setAdapter(deleteMsgGroupRvAdapter = new DeleteMsgGroupRvAdapter(mActivity));
        delete_msg_rv.setItemAnimator(null);//取消item动画
        delete_msg_rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 1;
            }
        });
        deleteMsgGroupRvAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                DeleteMessageEntity entity = deleteMsgGroupRvAdapter.getItem(position);
                DeleteMsgListActivity deleteMsgListActivity = new DeleteMsgListActivity(entity.getDialogId());
                actBase.presentFragment(deleteMsgListActivity);
            }
        });
    }

    public void loadData() {
        final AlertDialog progressDialog = new AlertDialog(mActivity, 3);
        progressDialog.show();
        DeletedMessageManager.getInstance().loadDeleteMessageList(true, type, new DeletedMessageManager.DeletedMessageLoadListener() {
            @Override
            public void onLoad(List<DeleteMessageEntity> list) {
                progressDialog.dismiss();
                deleteMsgGroupRvAdapter.setList(list);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.DEL_DB_DELETE_MSG:
                loadData();
                break;
        }
    }
}
