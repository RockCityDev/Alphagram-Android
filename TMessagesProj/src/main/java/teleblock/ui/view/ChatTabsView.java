package teleblock.ui.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.CollectionUtils;
import com.ruffian.library.widget.RFrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.wallet.WalletInfo;

/**
 * 聊天顶部菜单栏
 */
public class ChatTabsView extends FrameLayout {

    private Context context;
    private ChatActivity chatActivity;
    private ArrayList<Tab> tabs = new ArrayList<>();
    private ListAdapter adapter;
    private TabsViewDelegate delegate;
    private RFrameLayout chainContainer;

    public ChatTabsView(ChatActivity chatActivity) {
        super(chatActivity.getParentActivity());
        this.chatActivity = chatActivity;
        this.context = chatActivity.getParentActivity();
        initTabs();
        initView();
        updateTabs();
    }

    public void initTabs() {
        tabs.clear();
        if (chatActivity.getCurrentUser() == null || !chatActivity.getCurrentUser().self) {
            tabs.add(new Tab(0, R.drawable.chat_tab_mute_off));
        }
        //tabs.add(new Tab(1, R.drawable.chat_tab_watch_video));
        tabs.add(new Tab(2, R.drawable.chat_tab_media));
        if (chatActivity.searchItem != null) {
            tabs.add(new Tab(3, R.drawable.msg_search));
        }
        //tabs.add(new Tab(4, R.drawable.chat_tab_message_recycle));
        if (ChatObject.isChannel(chatActivity.getCurrentChat()) && !chatActivity.getCurrentChat().creator) {
            if (!ChatObject.isNotInChat(chatActivity.getCurrentChat())) {
                tabs.add(new Tab(5, R.drawable.msg_leave));
            }
        } else if (!ChatObject.isChannel(chatActivity.getCurrentChat())) {
            if (chatActivity.getCurrentChat() != null) {
                tabs.add(new Tab(5, R.drawable.msg_leave));
            } else {
                tabs.add(new Tab(5, R.drawable.msg_delete));
            }
        }
    }

    //链的图标
    public void addUserChainIcon(WalletInfo walletInfo) {
        if (!CollectionUtils.isEmpty(walletInfo.getChain_record())) {
            ArrayList<Tab> chainTabs = new ArrayList<>();
            for (WalletInfo.ChainInfoItem chainInfoItem : walletInfo.getChain_record()) {
                String resName = "user_chain_logo_" + chainInfoItem.chain_id;
                int resID = getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
                Tab chainTab = new Tab(chainInfoItem.chain_id, resID, true);
                chainTabs.add(chainTab);
            }
            if (chainTabs.size() > 0) {
                Tab chainDefault = new Tab(0, R.drawable.user_chain_logo_defalut, true);
                chainTabs.add(0, chainDefault);
                chainContainer.setVisibility(VISIBLE);
                RecyclerListView chainRv = new RecyclerListView(context);
                chainRv.setLayoutManager(new GridLayoutManager(context, chainTabs.size()));
                chainRv.addItemDecoration(new RecyclerView.ItemDecoration() {
                    int spacing = AndroidUtilities.dp(10);

                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.right = spacing;
                    }
                });
                ListAdapter chainAdapter = new ListAdapter(context);
                chainRv.setAdapter(chainAdapter);
                chainRv.setOnItemClickListener((view, position) -> {
                    if (delegate != null) {
                        delegate.onItemClick(chainTabs.get(position));
                    }
                });
                chainAdapter.setData(chainTabs);
                chainContainer.addView(chainRv, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            }
        }
    }

    private void initView() {
        LinearLayout layoutContainer = new LinearLayout(context);
        layoutContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(layoutContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        //chainTab
        chainContainer = new RFrameLayout(context);
        chainContainer.setVisibility(GONE);
        chainContainer.setOnClickListener(view -> {
        });
        chainContainer.setPadding(AndroidUtilities.dp(5), AndroidUtilities.dp(2), 0, AndroidUtilities.dp(2));
        chainContainer.getHelper().setCornerRadius(AndroidUtilities.dp(30));
        chainContainer.getHelper().setBackgroundColorNormal(Theme.getColor(Theme.key_login_progressOuter));
        chainContainer.getBackground().setAlpha(25);
        LinearLayout.LayoutParams backgroundParams = new LinearLayout.LayoutParams(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        backgroundParams.gravity = Gravity.CENTER;
        backgroundParams.leftMargin = AndroidUtilities.dp(20);
        layoutContainer.addView(chainContainer, backgroundParams);

        //commonTab
        RecyclerListView listView = new RecyclerListView(context);
        listView.setSelectorDrawableColor(Theme.getColor(Theme.key_listSelector));
        listView.setLayoutManager(new GridLayoutManager(context, tabs.size()));
        adapter = new ListAdapter(context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((view, position) -> {
            if (delegate != null) {
                delegate.onItemClick(tabs.get(position));
            }
        });
        layoutContainer.addView(listView, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));

        View divider = new View(context);
        divider.setBackgroundColor(Theme.getColor(Theme.key_divider));
        addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 0.5f, Gravity.BOTTOM));

        setBackgroundColor(Theme.getColor(Theme.key_chat_topPanelBackground));
    }

    public void updateTabs() {
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            if (!tab.isChainIcon && tab.id == 0) {
                tab.icon = chatActivity.getMessagesController().isDialogMuted(chatActivity.getDialogId(),chatActivity.getTopicId()) ?
                        R.drawable.chat_tab_mute_on : R.drawable.chat_tab_mute_off;
            }
        }
        adapter.setData(tabs);
    }

    public void setDelegate(TabsViewDelegate tabsViewDelegate) {
        delegate = tabsViewDelegate;
    }

    public interface TabsViewDelegate {
        void onItemClick(Tab tab);
    }

    public class Tab {
        public int id;
        public int icon;
        public boolean isChainIcon;

        public Tab(int id, int icon) {
            this.icon = icon;
            this.id = id;
        }

        public Tab(int id, int icon, boolean isChainIcon) {
            this.icon = icon;
            this.id = id;
            this.isChainIcon = isChainIcon;
        }
    }

    public class TabView extends FrameLayout {

        ImageView imageView;

        public TabView(Context context) {
            super(context);
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            addView(imageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        }

        public void setTab(Tab tab) {
            imageView.setImageResource(tab.icon);
            if (tab.isChainIcon) {
                imageView.setColorFilter(null);
            } else {
                imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_topPanelClose), PorterDuff.Mode.MULTIPLY));
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        List<Tab> tabs = new ArrayList<>();

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        public void setData(List<Tab> tabs) {
            this.tabs = tabs;
            this.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return tabs.size();
        }

        @Override
        public long getItemId(int position) {
            return tabs.get(position).id;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = new TabView(mContext);
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TabView tabView = (TabView) holder.itemView;
            tabView.setTab(tabs.get(position));
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }
    }
}