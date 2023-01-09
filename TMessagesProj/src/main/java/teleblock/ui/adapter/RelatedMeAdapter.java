package teleblock.ui.adapter;

import static com.chad.library.adapter.base.entity.SectionEntity.Companion.HEADER_TYPE;
import static com.chad.library.adapter.base.entity.SectionEntity.Companion.NORMAL_TYPE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.DialogsActivity;

import teleblock.model.RelatedMeEntity;


//public class RelatedMeAdapter extends BaseSectionQuickAdapter<RelatedMeEntity, BaseViewHolder> {
//
//    private DialogsActivity parentFragment;
//
//    public RelatedMeAdapter(ActionBarLayout parentLayout) {
//        super(R.layout.item_related_me_header, R.layout.item_related_me_content, null);
//        for (int a = 0, N = parentLayout.fragmentsStack.size(); a < N; a++) {
//            BaseFragment fragment = parentLayout.fragmentsStack.get(a);
//            if (fragment instanceof DialogsActivity) {
//                parentFragment = (DialogsActivity) fragment;
//                break;
//            }
//        }
//    }
//
//
//    @Override
//    protected void convertHeader(@NonNull BaseViewHolder helper, @NonNull RelatedMeEntity relatedMeEntity) {
//        HeaderViewHolder headerViewHolder = (HeaderViewHolder) helper;
//        headerViewHolder.tvTitle.setText((String) relatedMeEntity.getObject());
//    }
//
//    @Override
//    protected void convert(@NonNull BaseViewHolder helper, RelatedMeEntity relatedMeEntity) {
//        ContentViewHolder contentViewHolder = (ContentViewHolder) helper;
//        DialogCell cell = contentViewHolder.dialogCell;
//        cell.setDialog((TLRPC.Dialog) relatedMeEntity.getObject(), 0, 0, relatedMeEntity.isCanPin());
//    }
//
//    @Override
//    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
//        switch (viewType) {
//            case HEADER_TYPE:
//                return new HeaderViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_related_me_header, parent, false));
//            case NORMAL_TYPE:
//                return new ContentViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_related_me_content, parent, false));
//            default:
//                return super.onCreateDefViewHolder(parent, viewType);
//        }
//    }
//
//    private class HeaderViewHolder extends BaseViewHolder {
//
//        private TextView tvTitle;
//
//        public HeaderViewHolder(View view) {
//            super(view);
//            tvTitle = view.findViewById(R.id.tv_title);
//        }
//    }
//
//    private class ContentViewHolder extends BaseViewHolder {
//
//        private View vLine;
//        private DialogCell dialogCell;
//
//        public ContentViewHolder(View view) {
//            super(view);
//            vLine = view.findViewById(R.id.v_line);
//            vLine.setBackgroundColor(Theme.getColor(Theme.key_divider));
//
//            dialogCell = new DialogCell(parentFragment, getContext(), true, false, UserConfig.selectedAccount, null);
//            FrameLayout frameLayout = (FrameLayout) view;
//            frameLayout.addView(dialogCell, 0);
//        }
//    }
//}
