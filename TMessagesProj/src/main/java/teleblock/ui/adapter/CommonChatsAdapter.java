package teleblock.ui.adapter;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;

import java.util.ArrayList;

public class CommonChatsAdapter extends BaseQuickAdapter<TLRPC.Chat, CommonChatsAdapter.MyViewHolder> {

    public CommonChatsAdapter() {
        super(R.layout.item_common_chat);
    }


    @Override
    protected void convert(@NonNull MyViewHolder helper, TLRPC.Chat chat) {
        ProfileSearchCell cell = helper.cell;
        cell.setData(chat, null, null, null, false, false);
    }


    public class MyViewHolder extends BaseViewHolder {

        private View vLine;
        private ProfileSearchCell cell;

        public MyViewHolder(View view) {
            super(view);
            vLine = view.findViewById(R.id.v_line);
            vLine.setBackgroundColor(Theme.getColor(Theme.key_divider));

            cell = new ProfileSearchCell(getContext());
            FrameLayout frameLayout = (FrameLayout) view;
            frameLayout.addView(cell, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
        }
    }
}
