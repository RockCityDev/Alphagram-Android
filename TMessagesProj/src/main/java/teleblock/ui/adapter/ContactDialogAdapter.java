package teleblock.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.UserCell;

import teleblock.model.ContactDialogEntity;

//public class ContactDialogAdapter extends BaseMultiItemQuickAdapter<ContactDialogEntity, BaseViewHolder> {
//
//    public ContactDialogAdapter() {
//        super();
//        addItemType(ContactDialogEntity.TYPE_CONTACT, R.layout.item_dialog_list);
//        addItemType(ContactDialogEntity.TYPE_DIALOG, R.layout.item_dialog_list);
//    }
//
//
//    @Override
//    protected void convert(@NonNull BaseViewHolder helper, ContactDialogEntity entity) {
//        switch (helper.getItemViewType()) {
//            case ContactDialogEntity.TYPE_CONTACT:
//                ContactViewHolder contactViewHolder = (ContactViewHolder) helper;
//                UserCell userCell = contactViewHolder.userCell;
//                userCell.setAvatarPadding(3);
//                TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(entity.getContact().user_id);
//                userCell.setData(user, null, null, 0);
//                break;
//            case ContactDialogEntity.TYPE_DIALOG:
//                DialogViewHolder dialogViewHolder = (DialogViewHolder) helper;
//                DialogCell dialogCell = dialogViewHolder.dialogCell;
//                dialogCell.setDialog(entity.getDialog(), 0, 0, false);
//                break;
//        }
//    }
//
//    @Override
//    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
//        switch (viewType) {
//            case ContactDialogEntity.TYPE_CONTACT:
//                return new ContactViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_list, parent, false));
//            case ContactDialogEntity.TYPE_DIALOG:
//                return new DialogViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_list, parent, false));
//            default:
//                return super.onCreateDefViewHolder(parent, viewType);
//        }
//    }
//
//    public class ContactViewHolder extends BaseViewHolder {
//
//        private View vLine;
//        private UserCell userCell;
//
//        public ContactViewHolder(View view) {
//            super(view);
//            vLine = view.findViewById(R.id.v_line);
//            vLine.setBackgroundColor(Theme.getColor(Theme.key_divider));
//
//            userCell = new UserCell(getContext(), 58, 1, false, false, null);
//            FrameLayout frameLayout = (FrameLayout) view;
//            frameLayout.addView(userCell, 0);
//
//        }
//    }
//
//    public class DialogViewHolder extends BaseViewHolder {
//
//        private View vLine;
//        private DialogCell dialogCell;
//
//        public DialogViewHolder(View view) {
//            super(view);
//            vLine = view.findViewById(R.id.v_line);
//            vLine.setBackgroundColor(Theme.getColor(Theme.key_divider));
//
//            dialogCell = new DialogCell(null, getContext(), true, false, UserConfig.selectedAccount, null);
//            FrameLayout frameLayout = (FrameLayout) view;
//            frameLayout.addView(dialogCell, 0);
//
//        }
//    }
//}
