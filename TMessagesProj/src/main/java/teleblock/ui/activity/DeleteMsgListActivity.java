package teleblock.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityDeleteMsgListBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.DeletedMessageManager;
import teleblock.ui.adapter.DeleteMsgListRvAdapter;

/**
 * Created by LSD on 2022/1/12.
 * Desc
 */
public class DeleteMsgListActivity extends BaseFragment {
    ActivityDeleteMsgListBinding binding;
    private DeleteMsgListRvAdapter deleteMsgListRvAdapter;
    private long dialogId;

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    public DeleteMsgListActivity(long dialogId) {
        this.dialogId = dialogId;
    }

    @Override
    public void onResume() {
        super.onResume();
        initStyle();
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_delete_msg_list_title", R.string.ac_delete_msg_list_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = ActivityDeleteMsgListBinding.inflate(LayoutInflater.from(context));
        initView();
        loadData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvDeleteBtn.setText(LocaleController.getString("delete_msg_list_text", R.string.delete_msg_list_text));
        binding.tvDeleteBtn.setOnClickListener(v -> deleteAllData());

        binding.msgListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.msgListRv.setAdapter(deleteMsgListRvAdapter = new DeleteMsgListRvAdapter(getContext()));
        binding.msgListRv.setItemAnimator(null);//取消item动画
    }

    private void initStyle() {//临时先不适配主题了
        actionBar.setBackgroundColor(Color.parseColor("#ffffff"));
        actionBar.setTitleColor(Color.parseColor("#000000"));
        actionBar.getBackButton().setColorFilter(Color.BLACK);
        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(),true),200);
    }

    private void deleteAllData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(LocaleController.getString("delete_msg_dialog_title", R.string.delete_msg_dialog_title));
        builder.setMessage(LocaleController.getString("delete_msg_dialog_content", R.string.delete_msg_dialog_content));
        builder.setPositiveButton(LocaleController.getString("delete_msg_dialog_confirm", R.string.delete_msg_dialog_confirm), (dialogInterface, i) -> {
            AlertDialog progressDialog = new AlertDialog(getContext(), 3);
            progressDialog.show();
            DeletedMessageManager.getInstance().deleteDeletedMessage(deleteMsgListRvAdapter.getData(), () -> {
                AndroidUtilities.runOnUIThread(() -> {
                    progressDialog.dismiss();
                    finishFragment();
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.DEL_DB_DELETE_MSG));
                });
            });
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        AlertDialog dialog = builder.create();
        showDialog(dialog);
        TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
        }
    }

    private void loadData() {
        final AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.show();
        DeletedMessageManager.getInstance().loadDeleteMessageList(dialogId, list -> {
            progressDialog.dismiss();
            deleteMsgListRvAdapter.setList(list);
        });
    }
}
