/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryPerformance;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hjq.http.EasyConfig;
import com.hjq.http.config.IRequestServer;
import com.hjq.http.ssl.HttpSslConfig;
import com.hjq.http.ssl.HttpSslFactory;
import com.tencent.mmkv.MMKV;

import org.telegram.messenger.voip.VideoCapturerDevice;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.ForegroundDetector;
import org.telegram.ui.LauncherIconController;

import java.io.File;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import okhttp3.OkHttpClient;
import teleblock.config.AppConfig;
import teleblock.network.RequestHandler;
import teleblock.network.RequestServer;
import teleblock.network.interceptor.ParamsEncryptionInterceptor;
import teleblock.widget.DebugLoggerTree;
import timber.log.Timber;

public class ApplicationLoader extends Application {

    private static ApplicationLoader applicationLoaderInstance;

    @SuppressLint("StaticFieldLeak")
    public static volatile Context applicationContext;
    public static volatile NetworkInfo currentNetworkInfo;
    public static volatile Handler applicationHandler;

    private static ConnectivityManager connectivityManager;
    public static volatile boolean applicationInited = false;
    private static volatile ConnectivityManager.NetworkCallback networkCallback;
    private static long lastNetworkCheckTypeTime;
    private static int lastKnownNetworkType = -1;

    public static long startTime;

    public static volatile boolean isScreenOn = false;
    public static volatile boolean mainInterfacePaused = true;
    public static volatile boolean mainInterfaceStopped = true;
    public static volatile boolean externalInterfacePaused = true;
    public static volatile boolean mainInterfacePausedStageQueue = true;
    public static boolean canDrawOverlays;
    public static volatile long mainInterfacePausedStageQueueTime;

    private static PushListenerController.IPushListenerServiceProvider pushProvider;
    private static IMapsProvider mapsProvider;
    private static ILocationServiceProvider locationServiceProvider;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static ILocationServiceProvider getLocationServiceProvider() {
        if (locationServiceProvider == null) {
            locationServiceProvider = applicationLoaderInstance.onCreateLocationServiceProvider();
            locationServiceProvider.init(applicationContext);
        }
        return locationServiceProvider;
    }

    protected ILocationServiceProvider onCreateLocationServiceProvider() {
        return new GoogleLocationProvider();
    }

    public static IMapsProvider getMapsProvider() {
        if (mapsProvider == null) {
            mapsProvider = applicationLoaderInstance.onCreateMapsProvider();
        }
        return mapsProvider;
    }

    protected IMapsProvider onCreateMapsProvider() {
        return new GoogleMapsProvider();
    }

    public static PushListenerController.IPushListenerServiceProvider getPushProvider() {
        if (pushProvider == null) {
            pushProvider = applicationLoaderInstance.onCreatePushProvider();
        }
        return pushProvider;
    }

    protected PushListenerController.IPushListenerServiceProvider onCreatePushProvider() {
        return PushListenerController.GooglePushListenerServiceProvider.INSTANCE;
    }

    public static String getApplicationId() {
        return applicationLoaderInstance.onGetApplicationId();
    }

    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    public static boolean isHuaweiStoreBuild() {
        return applicationLoaderInstance.isHuaweiBuild();
    }

    protected boolean isHuaweiBuild() {
        return false;
    }

    public static File getFilesDirFixed() {
        for (int a = 0; a < 10; a++) {
            File path = ApplicationLoader.applicationContext.getFilesDir();
            if (path != null) {
                return path;
            }
        }
        try {
            ApplicationInfo info = applicationContext.getApplicationInfo();
            File path = new File(info.dataDir, "files");
            path.mkdirs();
            return path;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return new File("/data/data/org.telegram.messenger/files");
    }

    public static void postInitApplication() {
        if (applicationInited || applicationContext == null) {
            return;
        }
        applicationInited = true;

        try {
            LocaleController.getInstance(); //TODO improve
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            connectivityManager = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        currentNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    } catch (Throwable ignore) {

                    }

                    boolean isSlow = isConnectionSlow();
                    for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                        ConnectionsManager.getInstance(a).checkConnection();
                        FileLoader.getInstance(a).onNetworkChanged(isSlow);
                    }
                }
            };
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            ApplicationLoader.applicationContext.registerReceiver(networkStateReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            final BroadcastReceiver mReceiver = new ScreenReceiver();
            applicationContext.registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PowerManager pm = (PowerManager) ApplicationLoader.applicationContext.getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isScreenOn();
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("screen state = " + isScreenOn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedConfig.loadConfig();
        SharedPrefsHelper.init(applicationContext);
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) { //TODO improve account
            UserConfig.getInstance(a).loadConfig();
            MessagesController.getInstance(a);
            if (a == 0) {
                SharedConfig.pushStringStatus = "__FIREBASE_GENERATING_SINCE_" + ConnectionsManager.getInstance(a).getCurrentTime() + "__";
            } else {
                ConnectionsManager.getInstance(a);
            }
            TLRPC.User user = UserConfig.getInstance(a).getCurrentUser();
            if (user != null) {
                MessagesController.getInstance(a).putUser(user, true);
                SendMessagesHelper.getInstance(a).checkUnsentMessages();
            }
        }

        ApplicationLoader app = (ApplicationLoader) ApplicationLoader.applicationContext;
        app.initPushServices();
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("app initied");
        }

        MediaController.getInstance();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) { //TODO improve account
            ContactsController.getInstance(a).checkAppAccount();
            DownloadController.getInstance(a);
        }
        ChatThemeController.init();
        BillingController.getInstance().startConnection();
    }

    public ApplicationLoader() {
        super();
    }

    @Override
    public void onCreate() {
        applicationLoaderInstance = this;
        try {
            applicationContext = getApplicationContext();
        } catch (Throwable ignore) {

        }

        super.onCreate();

        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("app start time = " + (startTime = SystemClock.elapsedRealtime()));
            FileLog.d("buildVersion = " + BuildVars.BUILD_VERSION);
        }
        if (applicationContext == null) {
            applicationContext = getApplicationContext();
        }

        NativeLoader.initNativeLibs(ApplicationLoader.applicationContext);
        ConnectionsManager.native_setJava(false);
        new ForegroundDetector(this) {
            @Override
            public void onActivityStarted(Activity activity) {
                boolean wasInBackground = isBackground();
                super.onActivityStarted(activity);
                if (wasInBackground) {
                    ensureCurrentNetworkGet(true);
                }
            }
        };
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("load libs time = " + (SystemClock.elapsedRealtime() - startTime));
        }

        applicationHandler = new Handler(applicationContext.getMainLooper());

        AndroidUtilities.runOnUIThread(ApplicationLoader::startPushService);

        LauncherIconController.tryFixLauncherIconIfNeeded();

        AppConfig.init();
        initSdk();
        Log.i("", "onCreate");
    }

    /**
     * 初始化一些三方SDK
     */
    private void initSdk() {
        // 网络请求框架初始化
        IRequestServer server = new RequestServer();
        //信任所有证书
        HttpSslConfig sslConfig = HttpSslFactory.generateSslConfig();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslConfig.getSslSocketFactory(), sslConfig.getTrustManager())
                .hostnameVerifier(HttpSslFactory.generateUnSafeHostnameVerifier())
                .build();
        EasyConfig.with(okHttpClient)
                // 是否打印日志
                .setLogEnabled(AppConfig.DEBUG)
                // 设置服务器配置
                .setServer(server)
                // 设置请求处理策略
                .setHandler(new RequestHandler(this))
                //添加参数拦截器：加密
                .setInterceptor(new ParamsEncryptionInterceptor())
                // 设置请求重试次数
                .setRetryCount(1)
                // 设置请求重试时间
                .setRetryTime(2000)
                .into();

        // 奔溃日志收集
        CaocConfig.Builder.create().apply();
        // 初始化日志打印
        if (AppConfig.DEBUG) {
            Timber.plant(new DebugLoggerTree());
        }
        // 初始化mmkv本地存储
        String root = applicationContext.getFilesDir().getAbsolutePath() + "/mmkv";
        MMKV.initialize(this, AppConfig.DEBUG ? root + "/debug" : root);

        // Flurry统计
        initFlurry();
    }

    private void initFlurry() {
        new FlurryAgent.Builder()
                .withDataSaleOptOut(false)
                // 捕获所有未捕获异常并将它们报告回 Flurry
                .withCaptureUncaughtExceptions(true)
                // 如果应在 applicationstate 处于非活动状态或后台时将此会话添加到总会话/DAU 中，则为 True。
                .withIncludeBackgroundSessionsInMetrics(true)
                // 设置内部 Flurry SDK 日志的日志级别
                .withLogLevel(Log.ERROR)
                .withPerformanceMetrics(FlurryPerformance.ALL)
                .build(this, BuildConfig.FLURRY_API_KEY);
    }

    public static void startPushService() {
        SharedPreferences preferences = MessagesController.getGlobalNotificationsSettings();
        boolean enabled;
        if (preferences.contains("pushService")) {
            enabled = preferences.getBoolean("pushService", true);
        } else {
            enabled = MessagesController.getMainSettings(UserConfig.selectedAccount).getBoolean("keepAliveService", true);
        }
        if (enabled) {
            try {
                applicationContext.startService(new Intent(applicationContext, NotificationsService.class));
            } catch (Throwable ignore) {

            }
        } else {
            applicationContext.stopService(new Intent(applicationContext, NotificationsService.class));

            PendingIntent pintent = PendingIntent.getService(applicationContext, 0, new Intent(applicationContext, NotificationsService.class), PendingIntent.FLAG_MUTABLE);
            AlarmManager alarm = (AlarmManager)applicationContext.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pintent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            LocaleController.getInstance().onDeviceConfigurationChange(newConfig);
            AndroidUtilities.checkDisplaySize(applicationContext, newConfig);
            VideoCapturerDevice.checkScreenCapturerSize();
            AndroidUtilities.resetTabletFlag();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPushServices() {
        AndroidUtilities.runOnUIThread(() -> {
            if (getPushProvider().hasServices()) {
                getPushProvider().onRequestPushToken();
            } else {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("No valid " + getPushProvider().getLogTitle() + " APK found.");
                }
                SharedConfig.pushStringStatus = "__NO_GOOGLE_PLAY_SERVICES__";
                PushListenerController.sendRegistrationToServer(getPushProvider().getPushType(), null);
            }
        }, 1000);
    }

    private boolean checkPlayServices() {
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            return resultCode == ConnectionResult.SUCCESS;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return true;
    }

    private static void ensureCurrentNetworkGet(boolean force) {
        if (force || currentNetworkInfo == null) {
            try {
                if (connectivityManager == null) {
                    connectivityManager = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                }
                currentNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (networkCallback == null) {
                        networkCallback = new ConnectivityManager.NetworkCallback() {
                            @Override
                            public void onAvailable(@NonNull Network network) {
                                lastKnownNetworkType = -1;
                            }

                            @Override
                            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                                lastKnownNetworkType = -1;
                            }
                        };
                        connectivityManager.registerDefaultNetworkCallback(networkCallback);
                    }
                }
            } catch (Throwable ignore) {

            }
        }
    }

    public static boolean isRoaming() {
        try {
            ensureCurrentNetworkGet(false);
            return currentNetworkInfo != null && currentNetworkInfo.isRoaming();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    public static boolean isConnectedOrConnectingToWiFi() {
        try {
            ensureCurrentNetworkGet(false);
            if (currentNetworkInfo != null && (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI || currentNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)) {
                NetworkInfo.State state = currentNetworkInfo.getState();
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING || state == NetworkInfo.State.SUSPENDED) {
                    return true;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    public static boolean isConnectedToWiFi() {
        try {
            ensureCurrentNetworkGet(false);
            if (currentNetworkInfo != null && (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI || currentNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) && currentNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    public static boolean isConnectionSlow() {
        try {
            ensureCurrentNetworkGet(false);
            if (currentNetworkInfo != null && currentNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (currentNetworkInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return true;
                }
            }
        } catch (Throwable ignore) {

        }
        return false;
    }

    public static int getAutodownloadNetworkType() {
        try {
            ensureCurrentNetworkGet(false);
            if (currentNetworkInfo == null) {
                return StatsController.TYPE_MOBILE;
            }
            if (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI || currentNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (lastKnownNetworkType == StatsController.TYPE_MOBILE || lastKnownNetworkType == StatsController.TYPE_WIFI) && System.currentTimeMillis() - lastNetworkCheckTypeTime < 5000) {
                    return lastKnownNetworkType;
                }
                if (connectivityManager.isActiveNetworkMetered()) {
                    lastKnownNetworkType = StatsController.TYPE_MOBILE;
                } else {
                    lastKnownNetworkType = StatsController.TYPE_WIFI;
                }
                lastNetworkCheckTypeTime = System.currentTimeMillis();
                return lastKnownNetworkType;
            }
            if (currentNetworkInfo.isRoaming()) {
                return StatsController.TYPE_ROAMING;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return StatsController.TYPE_MOBILE;
    }

    public static int getCurrentNetworkType() {
        if (isConnectedOrConnectingToWiFi()) {
            return StatsController.TYPE_WIFI;
        } else if (isRoaming()) {
            return StatsController.TYPE_ROAMING;
        } else {
            return StatsController.TYPE_MOBILE;
        }
    }

    public static boolean isNetworkOnlineFast() {
        try {
            ensureCurrentNetworkGet(false);
            if (currentNetworkInfo == null) {
                return true;
            }
            if (currentNetworkInfo.isConnectedOrConnecting() || currentNetworkInfo.isAvailable()) {
                return true;
            }

            NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
            return true;
        }
        return false;
    }

    public static boolean isNetworkOnlineRealtime() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                return true;
            }

            netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
            return true;
        }
        return false;
    }

    public static boolean isNetworkOnline() {
        boolean result = isNetworkOnlineRealtime();
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            boolean result2 = isNetworkOnlineFast();
            if (result != result2) {
                FileLog.d("network online mismatch");
            }
        }
        return result;
    }

    public static void startAppCenter(Activity context) {
        applicationLoaderInstance.startAppCenterInternal(context);
    }

    public static void checkForUpdates() {
        applicationLoaderInstance.checkForUpdatesInternal();
    }

    public static void appCenterLog(Throwable e) {
        applicationLoaderInstance.appCenterLogInternal(e);
    }

    protected void appCenterLogInternal(Throwable e) {

    }

    protected void checkForUpdatesInternal() {

    }

    protected void startAppCenterInternal(Activity context) {

    }

}