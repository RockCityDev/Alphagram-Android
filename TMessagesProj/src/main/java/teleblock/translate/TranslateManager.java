package teleblock.translate;

import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.LanguageUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.BaseController;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.EditTextCaption;

import java.util.ArrayList;

import teleblock.chat.TGChatManager;
import teleblock.translate.callback.TranslateCallBack;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;

public class TranslateManager extends BaseController {

    private static TranslateManager instance;

    public TranslateManager() {
        super(UserConfig.selectedAccount);
    }

    public static TranslateManager getInstance() {
        if (instance == null) {
            synchronized (TGChatManager.class) {
                if (instance == null) {
                    instance = new TranslateManager();
                }
            }
        }
        return instance;
    }

    public void translateMessage(Context context, MessageObject messageObject, long dialog_id) {
        if (messageObject.messageOwner.translating) {
            return;
        }
        messageObject.messageOwner.translatedMessage = messageObject.messageOwner.message + "\n\n--------\n\n" + LocaleController.getString("translate_dialog_loading", R.string.translate_dialog_loading);
        messageObject.messageOwner.translating = true;
        messageObject.messageOwner.translated = false;
        resetMessageContent(dialog_id, messageObject);
        LanguageDetector.detectLanguage(messageObject.messageOwner.message, new LanguageDetector.StringCallback() {
            @Override
            public void run(String str) {
                fetchTranslation(str, context, messageObject, dialog_id);
            }
        }, new LanguageDetector.ExceptionCallback() {
            @Override
            public void run(Exception e) {
                fetchTranslation("auto", context, messageObject, dialog_id);
            }
        });
    }

    private void fetchTranslation(String from, Context context, MessageObject messageObject, long dialog_id) {
        EventUtil.track(context, EventUtil.Even.聊天翻译, null);
        String to = MMKVUtil.getTranslateCode();
        // 首次使用默认选中本地语言
        if (TextUtils.isEmpty(to)) {
            to = LanguageUtils.getAppContextLanguage().getLanguage();
        }
        if ("und".equals(from)) from = "auto";
        TranslatorFactory.getTranslator(context).run(from, to, messageObject.messageOwner.message, new TranslateCallBack() {
            @Override
            public void onSuccess(String data) {
                if (TextUtils.isEmpty(data)) {
                    ToastUtils.showLong(LocaleController.getString("translate_dialog_error", R.string.translate_dialog_error));
                    return;
                }
                messageObject.messageOwner.translatedMessage = messageObject.messageOwner.message + "\n\n--------\n\n" + data;
                messageObject.messageOwner.translating = false;
                messageObject.messageOwner.translated = true;
                resetMessageContent(dialog_id, messageObject);
            }

            @Override
            public void onFail() {
                ToastUtils.showLong(LocaleController.getString("translate_dialog_error", R.string.translate_dialog_error));
                messageObject.messageOwner.translatedMessage = "";
                messageObject.messageOwner.translating = false;
                messageObject.messageOwner.translated = false;
                resetMessageContent(dialog_id, messageObject);
            }
        });
    }

    public void resetMessageContent(long dialog_id, MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;
        MessageObject obj = new MessageObject(currentAccount, message, true, true);
        ArrayList<MessageObject> arrayList = new ArrayList<>();
        arrayList.add(obj);
        getNotificationCenter().postNotificationName(NotificationCenter.replaceMessagesObjects, dialog_id, arrayList, false);
    }

    public void translateComment(Context context, EditTextCaption messageEditText) {
        AlertDialog progressDialog = new AlertDialog(context, 3);
        progressDialog.show();
        int start = messageEditText.getSelectionStart();
        int end = messageEditText.getSelectionEnd();
        CharSequence text = messageEditText.getText();
        if (start != end) {
            text = text.subSequence(start, end);
        }
        TranslatorFactory.getTranslator(context).run("auto", "en", text.toString(), new TranslateCallBack() {
            @Override
            public void onSuccess(String data) {
                progressDialog.dismiss();
                if (start == end) {
                    messageEditText.setText(data);
                } else {
                    messageEditText.getText().replace(start, end, data);
                }
            }

            @Override
            public void onFail() {
                progressDialog.dismiss();
            }
        });
    }
}
