package teleblock.util;

import com.blankj.utilcode.util.CollectionUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.checkerframework.checker.units.qual.A;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import teleblock.model.wallet.WalletInfo;
import teleblock.network.BaseBean;
import teleblock.network.api.TgUseridApi;

/***
 * 获取TG绑定钱包联系人
 */
public class ContactInfoUtil {
    private int subSize = 50;

    public interface LoadCallBack {
        void onLoad(List<WalletInfo> data);
    }

    private List<WalletInfo> hasWalletFriends = new ArrayList<>();
    private static ContactInfoUtil instance;
    CountDownLatch countDownLatch = null;
    private boolean loading;

    public static ContactInfoUtil getInstance() {
        if (instance == null) {
            synchronized (ContactInfoUtil.class) {
                if (instance == null) {
                    instance = new ContactInfoUtil();
                }
            }
        }
        return instance;
    }

    public void load(LoadCallBack callBack) {
        hasWalletFriends.clear();

        if (loading) return;
        List<Long> userIdList = new ArrayList<>();
        for (TLRPC.TL_contact contact : ContactsController.getInstance(UserConfig.selectedAccount).contacts) {
            userIdList.add(contact.user_id);
        }
        if (userIdList.size() == 0) return;
        loading = true;
        int groupCount = (userIdList.size() / subSize) + 1;
        countDownLatch = new CountDownLatch(groupCount);
        new Thread(() -> {
            for (int i = 0; i < groupCount; i++) {
                int start = i * subSize;
                int end = (i + 1) * subSize;
                if (end > userIdList.size()) end = userIdList.size();
                List<Long> subList = userIdList.subList(start, end);
                loadHttpData(subList);
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AndroidUtilities.runOnUIThread(() -> {
                loading = false;
                if (callBack != null) callBack.onLoad(hasWalletFriends);
            });
        }).start();
    }

    private void loadHttpData(List<Long> userIdList) {
        TelegramUtil.getUserNftData(userIdList, new TelegramUtil.UserNftDataListener() {
            @Override
            public void nftDataRequestSuccessful(List<WalletInfo> walletInfoList) {
                countDownLatch.countDown();
                if (!CollectionUtils.isEmpty(walletInfoList)) {
                    hasWalletFriends.addAll(walletInfoList);
                }
            }

            @Override
            public void nftDataRequestError(String errorMsg) {
                countDownLatch.countDown();
            }
        });
    }
}
