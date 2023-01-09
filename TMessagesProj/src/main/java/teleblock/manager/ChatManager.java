package teleblock.manager;

import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.FrameLayout;

import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

import java.util.ArrayList;

import teleblock.util.TGLog;

/**
 * Time:2022/7/15
 * Author:Perry
 * Description：chat管理
 */
public class ChatManager extends BaseController {

    public interface ChatManagerRunable {
        void chat(TLRPC.Chat chat);

        void chatFull(TLRPC.ChatFull chatFull);

        void chatInvalid();
    }

    public interface UpdateRepliesCountListener {
        void repliesCount(TLRPC.MessageReplies replies);
    }

    private static volatile ChatManager[] Instance = new ChatManager[UserConfig.MAX_ACCOUNT_COUNT];

    public ChatManager(int num) {
        super(num);
    }

    public static ChatManager getInstance(int num) {
        ChatManager localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (ChatManager.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new ChatManager(num);
                }
            }
        }
        return localInstance;
    }

    /**
     * 获取chat列表信息
     * @param chatId
     * @param chatLink
     * @param chatRunable
     */
    public void getChatMessage(long chatId, String chatLink, ChatManagerRunable chatRunable) {
        TLRPC.Chat chat = getMessagesController().getChat(Math.abs(chatId));
        if (chat != null) {
            chatRunable.chat(chat);
            getChatFull(chat, chatRunable);
            return;
        }

        chat = getMessagesStorage().getChat(Math.abs(chatId));
        if (chat != null) {
            chatRunable.chat(chat);
            getChatFull(chat, chatRunable);
            return;
        }

        TLRPC.TL_contacts_resolveUsername contacts_resolveUsername = new TLRPC.TL_contacts_resolveUsername();
        contacts_resolveUsername.username = chatLink;

        getConnectionsManager().sendRequest(contacts_resolveUsername, (response, error) -> {
            AndroidUtilities.runOnUIThread(() -> {
                if (error != null) {
                    TGLog.erro("loadChatData-->" + error.text);
                    chatRunable.chatInvalid();
                    return;
                }
                TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                TLRPC.Chat resultChat = res.chats.get(0);
                if (resultChat != null) {

                    getMessagesController().putUsers(res.users, false);
                    getMessagesController().putChats(res.chats, false);
                    getMessagesStorage().putUsersAndChats(res.users, res.chats, false, true);

                    chatRunable.chat(resultChat);
                    getChatFull(resultChat, chatRunable);
                }
            });
        });
    }

    /**
     * 获取chatfull信息
     * @param chat
     * @param chatRunable
     */
    public void getChatFull(TLRPC.Chat chat, ChatManagerRunable chatRunable) {
        TLRPC.ChatFull chatFull = getMessagesController().getChatFull(chat.id);
        if (chatFull != null) {
            chatRunable.chatFull(chatFull);
            return;
        }

        TLObject request;
        if (ChatObject.isChannel(chat)) {
            TLRPC.TL_channels_getFullChannel req = new TLRPC.TL_channels_getFullChannel();
            req.channel = MessagesController.getInputChannel(chat);
            request = req;
        } else {
            TLRPC.TL_messages_getFullChat req = new TLRPC.TL_messages_getFullChat();
            req.chat_id = chat.id;
            request = req;
        }

        getConnectionsManager().sendRequest(request, (response, error) -> {
            if (error == null) {
                AndroidUtilities.runOnUIThread(() -> {
                    TLRPC.TL_messages_chatFull res = (TLRPC.TL_messages_chatFull) response;

                    getMessagesStorage().putUsersAndChats(res.users, res.chats, true, true);
                    getMessagesStorage().updateChatInfo(res.full_chat, false);
                    getMessagesController().putChatFull(res.full_chat);

                    if (res.full_chat != null) {
                        chatRunable.chatFull(res.full_chat);
                    }
                });
            }
        });
    }

    /**
     * 显示圆形头像还是圆角头像
     * @param context
     * @param chat
     * @param flAvatar
     * @param isRound
     */
    public void addTgAvatarView(Context context, TLRPC.Chat chat, FrameLayout flAvatar, boolean isRound) {
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        BackupImageView avatarImageView = new BackupImageView(context);
        avatarDrawable.setInfo(chat);
        int value = flAvatar.getLayoutParams().height;
        avatarImageView.setRoundRadius(AndroidUtilities.dp(isRound ? (value / 2f) : 10));
        avatarImageView.setImage(ImageLocation.getForChat(chat, ImageLocation.TYPE_SMALL), value + "_" + value, avatarDrawable, chat);
        flAvatar.removeAllViews();
        flAvatar.addView(avatarImageView);
    }

    /**
     *
     * @param chat
     * @param messageId
     */
    public void updateRepliesCount(TLRPC.Chat chat, int messageId, UpdateRepliesCountListener listener) {
        TLObject req;
        TLRPC.TL_channels_getMessages request = new TLRPC.TL_channels_getMessages();
        request.channel = getMessagesController().getInputChannel(chat);
        request.id.add(messageId);
        req = request;
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (response != null) {
                AndroidUtilities.runOnUIThread(() -> {
                    TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                    getMessagesController().putUsers(res.users, false);
                    getMessagesController().putChats(res.chats, false);
                    getMessagesStorage().putUsersAndChats(res.users, res.chats, true, true);

                    if (!res.chats.isEmpty()) {
                        if (!res.messages.isEmpty()) {
                            if (res.messages.get(0).replies != null) {
                                int serverRepliesCount = res.messages.get(0).replies.replies;
                                listener.repliesCount(res.messages.get(0).replies);
                                getMessagesStorage().updateRepliesCountCover(
                                        res.chats.get(0).id,
                                        messageId,
                                        res.messages.get(0).replies.recent_repliers,
                                        res.messages.get(0).replies.max_id,
                                        serverRepliesCount
                                );
                            }
                        }
                    }
                });
            }
        });
    }
}
