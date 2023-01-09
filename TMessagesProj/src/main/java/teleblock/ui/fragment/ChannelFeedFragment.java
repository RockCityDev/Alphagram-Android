package teleblock.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.FragmentChannelFeedBinding;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.MediaActivity;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.ShareAlert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teleblock.file.KKFileMessageManager;
import teleblock.manager.ChatManager;
import teleblock.model.ChannelFeedEntity;
import teleblock.telegram.channels.ChannelMessage;
import teleblock.telegram.channels.ChannelMessageLoadListener;
import teleblock.telegram.channels.ChannelTagManager;
import teleblock.telegram.channels.ChannelTimeLine;
import teleblock.telegram.channels.GroupMediaMessage;
import teleblock.telegram.channels.JoinTimeMessage;
import teleblock.ui.activity.ChannelFeedActivity;
import teleblock.ui.adapter.ChannelFeedRvAdapter;
import teleblock.ui.dialog.ChannelMoreDialog;
import teleblock.ui.popup.ChannelLikePopup;
import teleblock.util.EventUtil;
import teleblock.util.TGLog;
import teleblock.util.sort.ChannelMessageSort;
import teleblock.video.KKFileDownloadStatus;
import teleblock.video.KKVideoDownloadListener;


/**
 * Time:2022/7/12
 * Author:Perry
 * Description：频道聚合消息子页面
 */
public class ChannelFeedFragment extends teleblock.ui.fragment.BaseFragment implements KKVideoDownloadListener,
        ChannelMessageLoadListener, ChannelLikePopup.ChannelLikePopupClickListener {
    private FragmentChannelFeedBinding binding;

    private BaseFragment mBaseFragment;

    private ChannelTimeLine channelTimeLine;
    private RecyclerView channelFeedRv;
    private SmartRefreshLayout refreshLayout;
    private LinearLayout nullLayout;
    private TextView tvChannelFeedEmpty;
    private TextView loadingText;

    private List<Long> chatIdList = new ArrayList<>();
    private int page = 1;
    private ChannelFeedRvAdapter channelFeedRvAdapter;
    private ChannelFeedEntity entity;
    private boolean refreshData;
    private boolean failToReloadData;
    private boolean successLoadData;
    private Handler mHandler = new Handler();

    private String[] loadingValues;
    private int loadingValueIndex = 0;
    private ChannelLikePopup popup;

    private int clickPosition = -1;

    Runnable loadingRunnable = () -> {
        if (loadingText.getVisibility() == View.VISIBLE) {
            if (loadingValueIndex < loadingValues.length - 1) {
                loadingValueIndex++;
            } else {
                loadingValueIndex = 0;
            }
            loadingText.setText(loadingValues[loadingValueIndex]);
            countDownLoading();
        }
    };

    private long lastDid;
    private long lastMid;

    //显示点赞弹窗的下标
    private int showPopupWindowPosition = -1;

    public ChannelFeedFragment() {}

    public void setmBaseFragment(BaseFragment mBaseFragment) {
        this.mBaseFragment = mBaseFragment;
    }

    public void setEntity(ChannelFeedEntity entity) {
        this.entity = entity;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        binding = FragmentChannelFeedBinding.inflate(LayoutInflater.from(getContext()));
        return binding.getRoot();
    }

    @Override
    protected void onViewCreated() {
        popup = new ChannelLikePopup(mBaseFragment, this);
        popup.setPopupGravity(Gravity.TOP);
        initView();
        channelTimeLine = new ChannelTimeLine();
        channelTimeLine.setDownloadFilesListener(this);//下载更新
        channelTimeLine.setChannelMessageLoadListener(this);

        if (entity.tagId == -1 && entity.chatId == -1) {//全部
            chatIdList.add((long) -1);
        } else if (entity.tagId == -1) {//子频道
            chatIdList.add(Math.abs(entity.chatId));
        } else {//聚合
            chatIdList = ChannelTagManager.getInstance().getChannelIdsByTag(entity.tagId);
        }
        if (chatIdList.size() == 0) {
            nullLayout.setVisibility(View.VISIBLE);
            channelFeedRv.setVisibility(View.GONE);
            return;
        }

        int currentConnectionState = ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState();
        if (currentConnectionState == ConnectionsManager.ConnectionStateWaitingForNetwork) {
            Toast.makeText(getContext(), LocaleController.getString("wait_for_network", R.string.wait_for_network), Toast.LENGTH_LONG).show();
            return;
        }

        refreshData();
    }

    /**
     * 刷新i
     */
    public void refreshData() {
        refreshLayout.autoRefresh(0);
    }

    private void initView() {
        binding.tvChannelFeedEmpty.setText(LocaleController.getString("ac_message_null_tips", R.string.ac_message_null_tips));
        nullLayout = binding.nullLayout;
        tvChannelFeedEmpty = binding.tvChannelFeedEmpty;
        channelFeedRv = binding.channelFeedRv;
        refreshLayout = binding.refreshLayout;
        loadingText = binding.tvLoadingText;
        tvChannelFeedEmpty.setText(LocaleController.getString("ac_message_null_tips", R.string.ac_message_null_tips));

        loadingValues = LocaleController.getString("array_channel_loading_tips", R.string.array_channel_loading_tips).split("\\|");
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshData = true;
            loadData();
        });

        if (channelFeedRv.getItemAnimator() != null) {
            ((SimpleItemAnimator) channelFeedRv.getItemAnimator()).setSupportsChangeAnimations(false);//去除item更新动画
        }
        channelFeedRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        channelFeedRvAdapter = new ChannelFeedRvAdapter(getContext());

        channelFeedRv.setAdapter(channelFeedRvAdapter);
        channelFeedRvAdapter.getLoadMoreModule().setEnableLoadMore(true);
        channelFeedRvAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
        channelFeedRvAdapter.getLoadMoreModule().setPreLoadNumber(4);//预加载
        channelFeedRvAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> loadMore());
        channelFeedRvAdapter.addChildClickViewIds(
                R.id.file_frame,
                R.id.v_click_baseinfor,
//                R.id.tv_channel_download,
//                R.id.tv_channel_collect,
                R.id.iv_channel_more,
                R.id.more_message_layout,
                R.id.tv_channel_share,
                R.id.tv_channel_comment,
                R.id.tv_channel_like
        );
        channelFeedRvAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (position < 0) return;//处理一些异常情况
            clickPosition = position;
            RecyclerView.ViewHolder viewHolder = channelFeedRv.findViewHolderForAdapterPosition(position);
            ChannelMessage message = channelFeedRvAdapter.getItem(position);
            switch (view.getId()) {
                case R.id.more_message_layout://展开更多消息
                    JoinTimeMessage joinTimeMessage = (JoinTimeMessage) message;
                    if (!joinTimeMessage.messageExpand) {
                        List<ChannelMessage> pinchMessages = joinTimeMessage.joinTimeMessages;
                        pinchMessages.remove(0);//第一个已经展示了
                        channelFeedRvAdapter.addData(position + 1, pinchMessages);
                        joinTimeMessage.messageExpand = true;
                        view.setVisibility(View.GONE);//不在折叠
                    }
                    break;
                case R.id.tv_channel_share:
                    EventUtil.track(getContext(), EventUtil.Even.频道转发click, new HashMap<>());
                    ArrayList<MessageObject> list = new ArrayList<>();
                    if (message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_GROUP_MESSAGE) {
                        GroupMediaMessage groupMediaMessageT = (GroupMediaMessage) message;
                        for (ChannelMessage gMessage : groupMediaMessageT.groupMediaMessages) {
                            if (gMessage.messageObject != null) {
                                list.add(gMessage.messageObject);
                            }
                        }
                    } else if (message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_JOIN_TIME) {
                        JoinTimeMessage joinTimeMessageT = (JoinTimeMessage) message;
                        for (ChannelMessage jMessage : joinTimeMessageT.joinTimeMessages) {
                            if (jMessage.messageObject != null) {
                                list.add(jMessage.messageObject);
                            }
                        }
                    } else {
                        list.add(message.messageObject);
                    }
                    showShareDialog(list);
                    break;
                case R.id.iv_channel_more:
                    boolean showMedia = false;
                    if (message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_IMAGE
                            || message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_IMAGE8
                            || message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_IMAGE13
                            || message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_IMAGE100
                            || message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_VIDEO
                            || message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_GROUP_MESSAGE) {
                        showMedia = true;
                    } else if (message.getMessageType() == ChannelFeedRvAdapter.ITEM_TYPE_TEXT) {
                        if (message.getMessageObject().messageOwner.media != null && message.getMessageObject().messageOwner.media.webpage != null) {
                            showMedia = true;
                        }
                    }
                    //点击更多
                    new ChannelMoreDialog(getContext(), message.getDialogId(), showMedia, flag -> {
                        if (flag.equals("inChat")) {
                            Bundle args = new Bundle();
                            args.putLong("chat_id", Math.abs(message.getDialogId()));
                            args.putInt("message_id", (int) message.getMessageId());
                            mBaseFragment.presentFragment(new ChatActivity(args));
                        } else if (flag.equals("leaveChannel")) {
                            long channelId = message.chat.id;
                            //取消关注
                            int currentAccount = UserConfig.selectedAccount;
                            MessagesController.getInstance(currentAccount).deleteParticipantFromChat((int) channelId, MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId()));
                            channelTimeLine.removeChannel(channelId);
                            Toast.makeText(getContext(), LocaleController.getString("dg_channel_block_ok", R.string.dg_channel_block_ok), Toast.LENGTH_LONG).show();
                        } else if (flag.equals("allMedia")) { // 源码入口已删除
                            Bundle args2 = new Bundle();
                            args2.putLong("dialog_id", message.getDialogId());
                            MediaActivity mediaActivity = new MediaActivity(args2, null);
                            mediaActivity.setChatInfo(mBaseFragment.getMessagesController().getChatFull(Math.abs((int) message.getDialogId())));
                            mBaseFragment.presentFragment(mediaActivity, false, true);
                        }
                    }).show();
                    break;
//                case R.id.tv_channel_collect:
//                    if (viewHolder != null) {
//                        BaseViewHolder baseViewHolder = (BaseViewHolder) viewHolder;
//                        channelFeedRvAdapter.updateCollectStatus(baseViewHolder, message);
//                    }
//                    break;
//                case R.id.tv_channel_download:
//                    KKFileMessageManager.getInstance().manualStartDownloadVideo(message.getMessageObject());//手动开始下载
//                    mBaseFragment.getParentActivity().startActivity(new Intent(getContext(), MyMixActivity.class).putExtra("currentItem", 1).putExtra("target", "downloading"));
//                    break;
                case R.id.v_click_baseinfor:
                    if (!("singleChannelView".equals(entity.from))) {
                        ChannelFeedEntity entity = new ChannelFeedEntity();
                        entity.from = "singleChannelView";
                        entity.title = "信息流单频道";
                        entity.tagId = -1;
                        entity.chatId = Math.abs(message.getDialogId());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("entity", entity);
                        mBaseFragment.presentFragment(new ChannelFeedActivity(bundle, mBaseFragment.getParentActivity()));
                    }
                    break;
                case R.id.file_frame:
                    KKFileDownloadStatus downloadStatus = message.downloadStatus;
                    if (downloadStatus != null) {
                        if (downloadStatus.getStatus() == KKFileDownloadStatus.Status.DOWNLOADED) {
                            try {
                                AndroidUtilities.openForView(message.getMessageObject(), mBaseFragment.getParentActivity(), null);
                            } catch (Exception e) {
                                TGLog.erro(e.getMessage());
                            }
                        } else if (downloadStatus.getStatus() == KKFileDownloadStatus.Status.DOWNLOADING) {
                            KKFileMessageManager.getInstance().pauseDownloadVideo(message.messageObject);
                        } else {
                            KKFileMessageManager.getInstance().startDownloadVideo(message.messageObject, "", 0);
                        }
                    }
                    break;

                case R.id.tv_channel_comment://跳转到留言页面
                    EventUtil.track(getContext(), EventUtil.Even.频道评论click, new HashMap<>());
                    dealComment(channelFeedRvAdapter.getData().get(position));
                    break;

                case R.id.tv_channel_like://点赞
                    EventUtil.track(getContext(), EventUtil.Even.频道点赞click, new HashMap<>());
                    showPopupWindowPosition = position;

                    //用户是否点赞过
                    boolean userIflike = false;
                    TLRPC.TL_availableReaction reaction = null;
                    if (channelFeedRvAdapter.getData().get(position).getMessageObject().messageOwner.reactions == null ||
                            channelFeedRvAdapter.getData().get(position).getMessageObject().messageOwner.reactions.results == null
                    ) {
                        userIflike = false;
                    } else {
                        for (TLRPC.ReactionCount data : channelFeedRvAdapter.getData().get(position).getMessageObject().messageOwner.reactions.results) {
                            if (data.chosen) {
                                userIflike = true;
                                reaction = mBaseFragment.getMediaDataController().getReactionsMap().get(ReactionsLayoutInBubble.VisibleReaction.fromTLReaction(data.reaction).emojicon);
                            }
                        }
                    }
                    if (userIflike) {//点赞过，取消点赞
                        sendReaction(ReactionsLayoutInBubble.VisibleReaction.fromEmojicon(reaction), false);
                    } else {
                        if (popup != null) {
                            popup.showLikePopupwindow(view, channelFeedRvAdapter.getData().get(position));
                        }
                    }
                    break;
            }
        });
    }

    /**
     * 跳转留言板或者是定位到当前消息
     *
     * @param channelMessage
     */
    private void dealComment(ChannelMessage channelMessage) {
        TLRPC.TL_messages_getDiscussionMessage req = new TLRPC.TL_messages_getDiscussionMessage();
        req.peer = MessagesController.getInputPeer(channelMessage.getChat());
        req.msg_id = (int) channelMessage.getMessageId();
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> {
            AndroidUtilities.runOnUIThread(() -> {
                if (response != null) {
                    if (response instanceof TLRPC.TL_messages_discussionMessage) {
                        TLRPC.TL_messages_discussionMessage res = (TLRPC.TL_messages_discussionMessage) response;
                        MessagesController.getInstance(UserConfig.selectedAccount).putUsers(res.users, false);
                        MessagesController.getInstance(UserConfig.selectedAccount).putChats(res.chats, false);
                        ArrayList<MessageObject> arrayList = new ArrayList<>();
                        for (int a = 0, N = res.messages.size(); a < N; a++) {
                            arrayList.add(new MessageObject(UserConfig.selectedAccount, res.messages.get(a), true, true));
                        }
                        if (!arrayList.isEmpty()) {
                            Bundle args = new Bundle();
                            args.putLong("chat_id", -arrayList.get(0).getDialogId());
                            args.putInt("message_id", Math.max(1, res.read_inbox_max_id));
                            ChatActivity chatActivity = new ChatActivity(args);
                            chatActivity.setThreadMessages(arrayList, channelMessage.getChat(), req.msg_id, res.read_inbox_max_id, res.read_outbox_max_id,null);
                            mBaseFragment.presentFragment(chatActivity);
                        }
                    }
                } else {
                    TGLog.erro("请求channe评论数据失败：" + error.text);
                    Bundle args = new Bundle();
                    args.putLong("chat_id", Math.abs(channelMessage.getDialogId()));
                    args.putInt("message_id", (int) channelMessage.getMessageId());
                    mBaseFragment.presentFragment(new ChatActivity(args));
                }
            });
        });
    }

    private void loadData() {
        showLoadingView();
        TGLog.debug("【" + entity.title + "】loadData ");
        page = 1;
        new Thread(() -> {
            List<ChannelMessage> messages = channelTimeLine.refresh(chatIdList);//testChannel：1201177205  //每日设计：1182228281
            onMessageLoad(messages);
        }).start();
    }

    private void loadMore() {
        page++;
        TGLog.debug("【" + entity.title + "】loadMore page =  " + page);
        new Thread(() -> {
            List<ChannelMessage> messages = channelTimeLine.loadMoreMessages();
            onMessageLoad(messages);
        }).start();
    }

    private void countDownLoading() {
        mHandler.postDelayed(loadingRunnable, 15000);
    }

    private void showLoadingView() {
        loadingText.setVisibility(View.VISIBLE);
        loadingValueIndex = 0;
        loadingText.setText(loadingValues[loadingValueIndex]);
        countDownLoading();
    }

    //处理相同消息- 不知道哪里来的重复
    private List<ChannelMessage> handleSameMessage(List<ChannelMessage> messageList) {
        if (messageList == null || messageList.size() == 0) return new ArrayList<>();

        List<ChannelMessage> newList = new ArrayList<>();
        if (page == 1) {
            lastDid = 0;
            lastMid = 0;
        }

        for (ChannelMessage channelMessage : messageList) {
            if (lastMid == 0 && lastDid == 0) {
                lastDid = channelMessage.getDialogId();
                lastMid = channelMessage.getMessageId();
                newList.add(channelMessage);
            } else {
                if (lastMid == channelMessage.getMessageId() && lastDid == channelMessage.getDialogId()) {
                    continue;
                } else {
                    lastDid = channelMessage.getDialogId();
                    lastMid = channelMessage.getMessageId();
                    newList.add(channelMessage);
                }
            }
        }
        return newList;
    }

    //处理组合并消息
    private List<ChannelMessage> handleGroupMessage(List<ChannelMessage> messageList) {
        List<ChannelMessage> newList = new ArrayList<>();
        if (messageList == null || messageList.size() == 0) return newList;

        //检查组消息
        Map<String, List<ChannelMessage>> groupMap = new HashMap<>();

        int joinTime = 0;
        long joinDialogId = 0;
        Map<String, List<ChannelMessage>> joinTimeMap = new HashMap<>();
        for (ChannelMessage channelMessage : messageList) {
            long grouped_id = channelMessage.getMessageGroupedId();
            if (grouped_id != 0) {//同一组发出来的消息
                if (groupMap.containsKey("" + grouped_id)) {
                    groupMap.get("" + grouped_id).add(channelMessage);
                } else {
                    List<ChannelMessage> groupOne = new ArrayList<>();
                    groupOne.add(channelMessage);
                    groupMap.put("" + grouped_id, groupOne);
                }
            } else {
                int itemTime = channelMessage.messageObject.messageOwner.date;
                long itemDialogId = channelMessage.messageObject.getDialogId();
                if (joinTime == 0 || Math.abs(joinTime - itemTime) > (2 * 60)) {
                    joinTime = itemTime;
                    joinDialogId = itemDialogId;
                    List<ChannelMessage> joinTimeOne = new ArrayList<>();
                    joinTimeOne.add(channelMessage);
                    joinTimeMap.put(joinTime + "" + joinDialogId, joinTimeOne);
                } else {
                    joinDialogId = itemDialogId;
                    if (joinTimeMap.containsKey(joinTime + "" + joinDialogId)) {
                        joinTimeMap.get(joinTime + "" + joinDialogId).add(channelMessage);
                    } else {
                        //两分钟内不同channel
                        List<ChannelMessage> joinTimeOne = new ArrayList<>();
                        joinTimeOne.add(channelMessage);
                        joinTimeMap.put(joinTime + "" + joinDialogId, joinTimeOne);
                    }
                }
            }
        }

        //添加组消息
        if (groupMap.entrySet().size() > 0) {
            for (Map.Entry<String, List<ChannelMessage>> entry : groupMap.entrySet()) {
                List<ChannelMessage> list = entry.getValue();
                for (ChannelMessage channelMessage : list) {
                    if (channelMessage.messageObject.caption == null) {
                        continue;
                    } else {
                        ChannelMessage groupFirst = channelMessage;
                        GroupMediaMessage group = new GroupMediaMessage(groupFirst.chat, groupFirst.messageObject, groupFirst.downloadStatus);
                        group.groupMediaMessages = list;
                        newList.add(group);
                        break;
                    }
                }
            }
        }

        //添加折叠消息
        if (joinTimeMap.entrySet().size() > 0) {
            for (Map.Entry<String, List<ChannelMessage>> entry : joinTimeMap.entrySet()) {
                List<ChannelMessage> list = entry.getValue();
                if (list.size() <= 4) {
                    for (ChannelMessage channelMessage : list) {//小于等于4个的不折叠
                        newList.add(channelMessage);
                    }
                } else {
                    for (ChannelMessage channelMessage : list) {
                        if (channelMessage.messageObject.caption == null) {
                            continue;
                        } else {
                            ChannelMessage joinTimeFirst = channelMessage;
                            JoinTimeMessage joinTimeMessage = new JoinTimeMessage(joinTimeFirst.chat, joinTimeFirst.messageObject, joinTimeFirst.downloadStatus);
                            joinTimeMessage.joinTimeMessages = list;
                            newList.add(joinTimeMessage);
                            break;
                        }
                    }
                }
            }
        }

        //排序
        if (newList.size() > 1) {
            Collections.sort(newList, new ChannelMessageSort());
        }
        return newList;
    }


    private void showShareDialog(ArrayList<MessageObject> arrayList) {
        ShareAlert dialog = new ShareAlert(getContext(), arrayList, null, true, null, false) {
            @Override
            public void dismissInternal() {
                super.dismissInternal();
            }
        };
        dialog.show();
    }

    private void onMessageLoad(List<ChannelMessage> messageList) {
        TGLog.debug("【" + entity.title + "】ChannelMessagesLoad==> page:" + page + ",size:" + messageList.size() + "");
        if (messageList.size() == 0 && page == 1) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", entity.title);
        }
        refreshData = false;
        refreshLayout.setEnableRefresh(true);
        if ("homeFeedView".equals(entity.from) && page == 1 && messageList.size() == 0 && failToReloadData) {
            return;
        }
        failToReloadData = false;

        List<ChannelMessage> sList = handleSameMessage(messageList);
        List<ChannelMessage> gList = handleGroupMessage(sList);
        AndroidUtilities.runOnUIThread(() -> {
            refreshLayout.finishRefresh();
            loadingText.setVisibility(View.GONE);
            channelFeedRvAdapter.getLoadMoreModule().loadMoreComplete();
            if (messageList.size() < 20) {
                channelFeedRvAdapter.getLoadMoreModule().loadMoreEnd(true);
            }
            if (page == 1) {
                if (messageList.size() == 0) {
                    nullLayout.setVisibility(View.VISIBLE);
                    channelFeedRv.setVisibility(View.GONE);
                } else {
                    successLoadData = true;
                    nullLayout.setVisibility(View.GONE);
                    channelFeedRv.setVisibility(View.VISIBLE);
                    if (gList.size() > 0) {
                        channelFeedRvAdapter.setList(gList);
                        channelFeedRv.scrollTo(0, 0);
                        mHandler.postDelayed(() -> channelFeedRv.scrollToPosition(0), 300);
                    }
                }
            } else {
                if (gList.size() == 0) return;
                channelFeedRvAdapter.addData(gList);
            }
        });
    }

    @Override
    public void updateVideoDownloadStatus(String fileName, KKFileDownloadStatus fileDownloadStatus) {
        AndroidUtilities.runOnUIThread(() -> {
            channelFeedRvAdapter.notifyItemStatusChanged(fileName);
        });
    }

    @Override
    public void onMessageLoadError() {
        if ("homeFeedView".equals(entity.from)) {
            if (successLoadData) return;
            if (failToReloadData) return;
            TGLog.debug("【" + entity.title + "】ChannelMessagesLoad==> failToReloadData");
            failToReloadData = true;
            AndroidUtilities.runOnUIThread(() -> {
                loadData();
            });
        }
    }

    public void refrashData() {
        if (refreshData) return;

        refreshData = true;
        refreshLayout.autoRefresh(0);
    }

    public void refrashDataByTag() {
        chatIdList = ChannelTagManager.getInstance().getChannelIdsByTag(entity.tagId);
        if (chatIdList.size() == 0) {
            nullLayout.setVisibility(View.VISIBLE);
            channelFeedRv.setVisibility(View.GONE);
            return;
        }
        refrashData();
    }

    @Override
    public void selectReaction(ReactionsLayoutInBubble.VisibleReaction visibleReaction, boolean bigEmoji) {
        sendReaction(visibleReaction, bigEmoji);
    }

    /**
     * 点赞请求
     */
    private void sendReaction(ReactionsLayoutInBubble.VisibleReaction visibleReaction, boolean bigEmoji) {
        MessageObject primaryMessage = channelFeedRvAdapter.getData().get(showPopupWindowPosition).getMessageObject();
        boolean added = primaryMessage.selectReaction(visibleReaction, bigEmoji, false);

        TLRPC.TL_messages_sendReaction req = new TLRPC.TL_messages_sendReaction();
        if (primaryMessage.messageOwner.isThreadMessage && primaryMessage.messageOwner.fwd_from != null) {
            req.peer = mBaseFragment.getMessagesController().getInputPeer(primaryMessage.getFromChatId());
            req.msg_id = primaryMessage.messageOwner.fwd_from.saved_from_msg_id;
        } else {
            req.peer = mBaseFragment.getMessagesController().getInputPeer(primaryMessage.getDialogId());
            req.msg_id = primaryMessage.getId();
        }

        if (added) {
            if (visibleReaction.documentId != 0) {
                TLRPC.TL_reactionCustomEmoji reactionCustomEmoji = new TLRPC.TL_reactionCustomEmoji();
                reactionCustomEmoji.document_id = visibleReaction.documentId;
                req.reaction.add(reactionCustomEmoji);
                req.flags |= 1;
            } else if (visibleReaction.emojicon != null) {
                TLRPC.TL_reactionEmoji defaultReaction = new TLRPC.TL_reactionEmoji();
                defaultReaction.emoticon = visibleReaction.emojicon;
                req.reaction.add(defaultReaction);
                req.flags |= 1;
            }
        }

        if (bigEmoji) {
            req.flags |= 2;
            req.big = true;
        }

        //请求
        mBaseFragment.getConnectionsManager().sendRequest(req, (response, error) -> {
            if (response != null) {
                mBaseFragment.getMessagesController().processUpdates((TLRPC.Updates) response, false);
                AndroidUtilities.runOnUIThread(() -> {
                    ChannelMessage message = channelFeedRvAdapter.getData().get(showPopupWindowPosition);
                    message.messageObject = primaryMessage;
                    channelFeedRvAdapter.setData(showPopupWindowPosition, message);
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (clickPosition != -1) {
            ChannelMessage data = channelFeedRvAdapter.getData().get(clickPosition);
            ChatManager.getInstance(UserConfig.selectedAccount).updateRepliesCount(
                    data.getChat(),
                    (int) data.getMessageId(),
                    replies -> {
                        ChannelMessage channelMessage = channelFeedRvAdapter.getData().get(clickPosition);
                        channelMessage.getMessageObject().messageOwner.replies = replies;
                        channelFeedRvAdapter.setData(clickPosition, channelMessage);
                    }
            );
        }
    }
}