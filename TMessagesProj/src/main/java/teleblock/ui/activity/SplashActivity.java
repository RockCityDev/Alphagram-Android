package teleblock.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.google.android.exoplayer2.util.Log;
import com.google.gson.JsonObject;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCParams;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivitySplashBinding;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.HashMap;

import teleblock.config.AppConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.SystemEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.SystemCheckApi;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.SystemUtil;
import teleblock.util.TelegramUtil;

/**
 * 创建日期：2022/6/21
 * 描述：
 */
public class SplashActivity extends BaseActivity {
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.setStatusBarColor(this, Color.parseColor("#334358"));
        // 将window的背景图设置为空
        getWindow().setBackgroundDrawable(null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivitySplashBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        AndroidUtilities.setNavigationBarColor(getWindow(), Color.parseColor("#060715"));

        if (!ApplicationLoader.applicationInited) { // 首次打开app
            getDeviceMsg();
            systemCheck();
            //每次打开app都要删除掉上次存储的钱包配置项数据
            MMKVUtil.removeValue(AppConfig.MkKey.WALLET_CONFIG_DATA);
        }

        if (MMKVUtil.firstLoad()) {
            new Thread(() -> applyDefaultTheme()).start();
            EventUtil.track(mActivity, EventUtil.Even.第一次打开, new HashMap<>());
            MMKVUtil.firstLoad(false);
        }

        new Handler().postDelayed(this::startMainAct, 1000);
    }

    private boolean test() {
        //Create client specifying JSON-RPC version 2.0
        new Thread(() -> {
            JSONRPCClient client = JSONRPCClient.create("https://mainnet-rpc.thundercore.com", JSONRPCParams.Versions.VERSION_2);
            client.setConnectionTimeout(2000);
            client.setSoTimeout(2000);
            try {
                String data = client.callString("eth_getBalance", "0x3fF28F18f5e99B6564157a5B58BC678c49Bc8B93","latest");
                Log.d("TTT",data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return true;
    }

    /**
     * 获取设备信息
     */
    private void getDeviceMsg() {
        if (MMKVUtil.getString(AppConfig.MkKey.DEVICE_ID).isEmpty()) {
            //存储设备信息到本地
            MMKVUtil.saveValue(AppConfig.MkKey.DEVICE_ID, SystemUtil.getUniquePsuedoID());
            MMKVUtil.saveValue(AppConfig.MkKey.COUNTRY_CODE, SystemUtil.getCountryZipCode(this));
            MMKVUtil.saveValue(AppConfig.MkKey.SIM_CODE, SystemUtil.getTelContry(this));
        }
    }

    /**
     * 登录完成之后才会跳转主页面
     */
    private void startMainAct() {
        startActivity(new Intent(this, LaunchActivity.class));
        finish();
    }

    /**
     * 请求系统配置信息
     */
    private void systemCheck() {
        EasyHttp.post(new ApplicationLifecycle()).api(new SystemCheckApi()
                        .setCountrycode(MMKVUtil.getString(AppConfig.MkKey.COUNTRY_CODE))
                        .setSimcode(MMKVUtil.getString(AppConfig.MkKey.SIM_CODE)))
                .request(new OnHttpListener<BaseBean<SystemEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<SystemEntity> result) {
                        MMKVUtil.setSystemMsg(result.getData());
                        //搜索机器人添加到缓存
                        TelegramUtil.getBotInfo(result.getData(), () -> {
                            EventBus.getDefault().postSticky(new MessageEvent(EventBusTags.OBTION_BOT_SUCCESSFUL));
                        });
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    private void applyDefaultTheme() {
        String assetPath = "theme/alpha.attheme";
        String themeFilePath = PathUtils.getExternalAppFilesPath() + "/" + assetPath;
        File themeFile = new File(themeFilePath);
        if (!themeFile.exists()) {
            ResourceUtils.copyFileFromAssets(assetPath, themeFilePath);
        }
        if (themeFile.exists()) {
            Theme.ThemeInfo themeInfo = Theme.applyThemeFile(themeFile, "alpha", null, true);
            Theme.saveCurrentTheme(themeInfo, false, false, false);
            SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE).edit();
            editor.putString("lastDayTheme", themeInfo.getKey());
            editor.commit();
        }
    }
}