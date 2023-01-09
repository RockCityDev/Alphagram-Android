package teleblock.blockchain;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.walletconnect.BridgeServer;
import org.walletconnect.Session;
import org.walletconnect.impls.FileWCSessionStore;
import org.walletconnect.impls.MoshiPayloadAdapter;
import org.walletconnect.impls.OkHttpTransport;
import org.walletconnect.impls.WCSession;
import org.walletconnect.impls.WCSessionStore;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.LoginDataResult;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.wallet.ChainInfo;
import teleblock.model.wallet.WalletInfo;
import teleblock.network.BaseBean;
import teleblock.network.api.BindWalletApi;
import teleblock.ui.activity.WalletBindAct;
import teleblock.ui.dialog.WalletBindingDialog;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletUtil;
import timber.log.Timber;

/**
 * 创建日期：2022/8/4
 * 描述：
 */
public class WCSessionManager implements Session.Callback {

    private static WCSessionManager instance;
    public Session.Config config;
    private OkHttpClient client;
    private Moshi moshi;
    private BridgeServer bridge;
    private WCSessionStore storage;
    public WCSession session;
    public String pkg;
    private WalletBindingDialog progressDialog;
    private boolean hasAccount;

    public static WCSessionManager getInstance() {
        if (instance == null) {
            synchronized (WCSessionManager.class) {
                if (instance == null) {
                    instance = new WCSessionManager();
                }
            }
        }
        return instance;
    }

    public WCSessionManager() {
    }

    public void init() {
        client = new OkHttpClient.Builder().build();
        moshi = new Moshi.Builder().addLast(new KotlinJsonAdapterFactory()).build();
        bridge = new BridgeServer(moshi);
        bridge.start();
        File file = new File(PathUtils.getInternalAppFilesPath(), "session_store.json");
        FileUtils.createOrExistsFile(file);
        storage = new FileWCSessionStore(file, moshi);
        if (MMKVUtil.sessionConfig() != null) {
            createSession(MMKVUtil.sessionConfig());
            if (session.approvedAccounts() != null) {
                session.update(session.approvedAccounts(), 0);
            }
        } else {
            MMKVUtil.connectedWalletAddress("");
            MMKVUtil.connectedWalletPkg("");
            EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CONNECT_CLOSED));
        }
    }

    private void createSession(Session.Config config) {
        session = new WCSession(
                config,
                new MoshiPayloadAdapter(moshi),
                storage,
                new OkHttpTransport.Builder(client, moshi),
                new Session.PeerMeta(
                        "https://alphagram.app/",
                        AppUtils.getAppName(),
                        "",
                        new ArrayList<>()
                ),
                null
        );
        session.clearCallbacks();
        session.addCallback(this);
    }

    public void resetSession() {
        progressDialog = new WalletBindingDialog(ActivityUtils.getTopActivity(), pkg);
        progressDialog.show();
        if (session != null) {
            session.clearCallbacks();
        }
        byte[] bytes = new byte[32];
        new Random().nextBytes(bytes);
        String key = Numeric.toHexStringNoPrefix(bytes);
        config = new Session.Config(
                UUID.randomUUID().toString(),
//                "wss://bridge.walletconnect.org",
                "wss://bridge.aktionariat.com:8887",
                key,
                "wc",
                1
        );
        MMKVUtil.sessionConfig(config);
        createSession(config);
        session.offer();
    }

    @Override
    public void onStatus(@NonNull Session.Status status) {
        Timber.i("onStatus-->" + status);
        AndroidUtilities.runOnUIThread(() -> {
            if (progressDialog != null) progressDialog.dismiss();
        });
        if (Session.Status.Approved.INSTANCE.equals(status)) {
            AppUtils.launchApp(AppUtils.getAppPackageName());
            //获取钱包地址
            String address = session.approvedAccounts().get(0);
            //获取钱包
            String pkg = BlockchainConfig.getPkgByFullName(session.peerMeta().getName());
            //请求服务器绑定钱包地址
            EasyHttp.post(new ApplicationLifecycle())
                    .api(new BindWalletApi()
                            .setWallet_type(BlockchainConfig.getWalletTypeByPkg(pkg))
                            .setWallet_address(address)
                            .setChain_id(session.chainId()))
                    .request(new OnHttpListener<BaseBean<LoginDataResult.UserEntity>>() {
                        @Override
                        public void onSucceed(BaseBean<LoginDataResult.UserEntity> result) {
                            //LoginManager.saveUserInfo(result.getData());
                            //存储钱包地址
                            MMKVUtil.connectedWalletAddress(address);
                            //存储钱包名称
                            MMKVUtil.connectedWalletPkg(pkg);
                            MMKVUtil.setNftphotoIfShow(1);
                            //请求自己的nft数据，存到库里面
                            TelegramUtil.getUserNftData(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser().id, new TelegramUtil.UserNftDataListener() {
                                @Override
                                public void nftDataRequestSuccessful(List<WalletInfo> walletInfoList) {
                                    //发送通知更改头像
                                    EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_AVATAR_STATE));
                                }

                                @Override
                                public void nftDataRequestError(String errorMsg) {

                                }
                            });
                        }

                        @Override
                        public void onFail(Exception e) {
                        }

                        @Override
                        public void onEnd(Call call) {
                            EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CONNECT_APPROVED));
                        }
                    });
        } else if (Session.Status.Closed.INSTANCE.equals(status)) {
        } else if (Session.Status.Connected.INSTANCE.equals(status)) {
            if (TextUtils.isEmpty(pkg)) return;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(config.toWCUri()));
            intent.setPackage(pkg);
            ActivityUtils.startActivity(intent);
            pkg = null;
        } else if (Session.Status.Disconnected.INSTANCE.equals(status)) {
            if (hasAccount) { // 已链接过才断开
                disConnect();
            }
        }
    }

    @Override
    public void onMethodCall(@NonNull Session.MethodCall call) {
        Timber.i("onMethodCall-->" + call);
        if (call instanceof Session.MethodCall.SessionUpdate) {
            Session.MethodCall.SessionUpdate sessionUpdate = (Session.MethodCall.SessionUpdate) call;
            hasAccount = CollectionUtils.isNotEmpty(sessionUpdate.getParams().getAccounts());
        }
    }

    /**
     * 断开链接
     */
    private void disConnect() {
        AppUtils.launchApp(AppUtils.getAppPackageName());
        ToastUtils.showLong(LocaleController.getString("wallet_failure_tips", R.string.wallet_failure_tips));
        MMKVUtil.connectedWalletAddress("");
        MMKVUtil.connectedWalletPkg("");
        EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CONNECT_CLOSED));
    }

    /**
     * 返回客户端持有的地址列表
     */
    public void getAccounts(Callback<List<String>> callback) {
        if (session == null) {
            disConnect();
            return;
        }
        session.performMethodCall(
                new Session.MethodCall.Custom(System.currentTimeMillis(), "eth_accounts", null),
                response -> {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.getError() == null) {
                                if (response.getResult() instanceof List) {
                                    String json = JsonUtil.parseObjToJson(response.getResult());
                                    callback.onSuccess(JsonUtil.parseJsonToList(json, String.class));
                                } else {
                                    callback.onError("Unknown response");
                                }
                            } else {
                                callback.onError(response.getError().getMessage());
                            }
                        }
                    });
                    return null;
                });
        WalletUtil.goToWallet();
    }

    /**
     * 返回当前配置的链ID
     */
    public void getChainId(boolean gotoWallet, Callback<String> callback) {
        session.performMethodCall(
                new Session.MethodCall.Custom(System.currentTimeMillis(), "eth_chainId", null),
                response -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (response.getError() == null) {
                            if (response.getResult() instanceof String) {
                                callback.onSuccess((String) response.getResult());
                            } else {
                                callback.onError("Unknown response");
                            }
                        } else {
                            callback.onError(response.getError().getMessage());
                        }
                    });
                    return null;
                });
        if (gotoWallet) {
            WalletUtil.goToWallet();
        }
    }

    /**
     * 切换网络
     */
    public void switchNetwork(String chainId, Callback<String> callback) {
        List<ChainInfo> params = new ArrayList<>();
        WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = BlockchainConfig.getChainType(Integer.parseInt(chainId));
        if (chainType == null) return;
        params.add(new ChainInfo(chainType.getId() + "", chainType.getName(), chainType.getMain_currency_name(), chainType.getRpc_url()));
        session.performMethodCall(
                new Session.MethodCall.Custom(System.currentTimeMillis(), "ETH".equals(chainType.getMain_currency_name()) ? "wallet_switchEthereumChain" : "wallet_addEthereumChain", params),
                new Function1<Session.MethodCall.Response, Unit>() {
                    @Override
                    public Unit invoke(Session.MethodCall.Response response) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                AppUtils.launchApp(AppUtils.getAppPackageName());
                                if (response.getError() == null) {
                                    if (response.getResult() instanceof String) {
                                        callback.onSuccess((String) response.getResult());
                                    } else {
                                        callback.onError("Unknown response");
                                    }
                                } else {
                                    callback.onError(response.getError().getMessage());
                                }
                            }
                        });
                        return null;
                    }
                });
        WalletUtil.goToWallet();
    }

    /**
     * 发起主币交易
     */
    public void sendTransaction(String to, String amount, String data, Callback<String> callback) {
        sendTransaction(to, null, null, amount, 18, data, callback);
    }

    /**
     * 发起交易（代币需要传gasPrice、gasLimit）
     */
    public void sendTransaction(String to, String gasPrice, String gasLimit, String amount, int decimal, String data, Callback<String> callback) {
        if (!TextUtils.isEmpty(gasPrice)) {
            gasPrice = Numeric.toHexStringWithPrefix(Convert.toWei(gasPrice, Convert.Unit.GWEI).toBigInteger());
        }

        if (!TextUtils.isEmpty(gasLimit)) {
            gasLimit = Numeric.toHexStringWithPrefix(new BigDecimal(gasLimit).toBigInteger());
        }

        String from = MMKVUtil.connectedWalletAddress();

        String value = Numeric.toHexStringWithPrefix(new BigDecimal(WalletUtil.toWei(amount, decimal)).toBigInteger());
        if (!TextUtils.isEmpty(data)) value = "";
        session.performMethodCall(
                new Session.MethodCall.SendTransaction(System.currentTimeMillis(), from, to, null, gasPrice, gasLimit, value, data),
                response -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        AppUtils.launchApp(AppUtils.getAppPackageName());
                        if (response.getError() == null) {
                            if (response.getResult() instanceof String) {
                                callback.onSuccess((String) response.getResult());
                            } else {
                                callback.onError("Unknown response");
                            }
                        } else {
                            callback.onError(response.getError().getMessage());
                        }
                    });
                    return null;
                });
        WalletUtil.goToWallet();
    }

    public abstract static class Callback<T> {

        public abstract void onSuccess(T data);

        public void onError(String msg) {
            ToastUtils.showLong(msg);
            Timber.e(msg);
        }
    }
}