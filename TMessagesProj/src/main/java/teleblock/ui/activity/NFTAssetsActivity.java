package teleblock.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.JsonUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.body.JsonBody;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnDownloadListener;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.http.model.HttpMethod;
import com.hjq.http.request.HttpRequest;
import com.luck.picture.lib.config.Crop;
import com.luck.picture.lib.utils.DateUtils;
import com.ruffian.library.widget.RTextView;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.yalantis.ucrop.UCrop;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityNftAssetsBinding;
import org.telegram.messenger.databinding.ViewNftEmptyBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.web3j.utils.Numeric;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.wallet.JsonRpc;
import teleblock.model.wallet.NFTAssets;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTInfoList;
import teleblock.model.wallet.OasisToken;
import teleblock.model.wallet.TTMetadata;
import teleblock.model.wallet.TTNft;
import teleblock.network.api.NftInfoApi;
import teleblock.network.api.blockchain.ethereum.EthNftsApi;
import teleblock.network.api.blockchain.oasis.OasisTokensApi;
import teleblock.network.api.blockchain.polygon.PolygonAssentsApi;
import teleblock.network.api.blockchain.thundercore.TTNftsApi;
import teleblock.network.api.blockchain.thundercore.TTSingleNftApi;
import teleblock.ui.adapter.NFTAssetsAdapter;
import teleblock.ui.dialog.ChainTypeSelectorDialog;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.StringUtil;
import teleblock.util.SystemUtil;
import teleblock.widget.GlideHelper;
import teleblock.widget.divider.CustomItemDecoration;
import timber.log.Timber;

public class NFTAssetsActivity extends BaseFragment implements OnItemClickListener, OnRefreshListener, OnLoadMoreListener {

    private ActivityNftAssetsBinding binding;
    private NFTAssetsAdapter nftListAdapter;
    private long currentTimeMillis;
    private String address;
    private String order_direction = "desc";
    private String limit = "10";
    private String cursor;
    private NFTInfo nftInfo;
    private WalletNetworkConfigEntity.WalletNetworkConfigChainType currentChainType;

    public NFTAssetsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        if (getArguments() != null) {
            currentTimeMillis = arguments.getLong("currentTimeMillis");
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EasyHttp.cancel(getClass().getSimpleName());
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("nft_assets_title", R.string.nft_assets_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = ActivityNftAssetsBinding.inflate(LayoutInflater.from(context));
        addRightView();
        initView();
        initStyle();
        return fragmentView = binding.getRoot();
    }

    private void addRightView() {
        int additionalTop = AndroidUtilities.getStatusBarHeight(getParentActivity());
        float marginTop = additionalTop + (ActionBar.getCurrentActionBarHeight() - SizeUtils.dp2px(30)) / 2f;
        RTextView rightTv = (RTextView) LayoutInflater.from(getParentActivity()).inflate(R.layout.layout_switch_chain, null);
        Drawable rightDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_ab_new);
        rightDrawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY));
        rightTv.getHelper().setIconNormalRight(rightDrawable);
        actionBar.addView(rightTv, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 30, Gravity.RIGHT, 0f, SizeUtils.px2dp(marginTop), 12f, 0f));

        rightTv.setOnClickListener(view ->
                new ChainTypeSelectorDialog(getContext(), data -> {
                    MMKVUtil.currentChainConfig(data);
                    updateChainType(rightTv);
                }).setCurrentChainType(MMKVUtil.currentChainConfig()).show()
        );
        updateChainType(rightTv);
    }

    private void updateChainType(RTextView rightTv) {
        currentChainType = MMKVUtil.currentChainConfig();
        if (currentChainType == null) return;
        rightTv.setText(currentChainType.getName());
        GlideHelper.getDrawableGlide(getContext(), currentChainType.getIcon(), drawable -> rightTv.getHelper().setIconNormalLeft(drawable));
        initData();
    }

    private void initView() {
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getParentActivity(), 2));
        binding.recyclerView.addItemDecoration(new CustomItemDecoration(2, 20f, 20f, true));
        nftListAdapter = new NFTAssetsAdapter();
        nftListAdapter.setEmptyView(createEmptyView());
        nftListAdapter.getEmptyLayout().setVisibility(View.GONE);
        nftListAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        nftListAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(nftListAdapter);
    }

    private void initStyle() {//临时先不适配主题了
        actionBar.setBackgroundColor(Color.parseColor("#ffffff"));
        actionBar.setTitleColor(Color.parseColor("#000000"));
        actionBar.getBackButton().setColorFilter(Color.BLACK);
        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(), true), 200);
    }

    private View createEmptyView() {
        ViewNftEmptyBinding binding = ViewNftEmptyBinding.inflate(LayoutInflater.from(getParentActivity()));
        binding.tvEmptyNft.setText(LocaleController.getString("nft_assets_empty_text", R.string.nft_assets_empty_text));
        return binding.getRoot();
    }

    private void initData() {
        address = MMKVUtil.connectedWalletAddress();
        binding.refreshLayout.autoRefresh();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        cursor = null;
        loadData();
    }

    @Override
    public void onLoadMore() {
        loadData();
    }

    private void loadData() {
        String symbol = currentChainType.getMain_currency_name();
        HttpRequest httpRequest = null;
        if ("ETH".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.get(new ApplicationLifecycle())
                    .api(new EthNftsApi()
                            .setOwner(address)
                            .setOrder_direction(order_direction)
                            .setLimit(limit)
                            .setCursor(cursor)
                            .setInclude_orders(true));
        } else if ("MATIC".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.get(new ApplicationLifecycle())
                    .api(new PolygonAssentsApi()
                            .setOwner_address(address)
                            .setOrder_direction(order_direction)
                            .setLimit(limit)
                            .setCursor(cursor)
                            .setInclude_orders(true));
        } else if ("TT".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.post(new ApplicationLifecycle())
                    .api(new TTNftsApi())
                    .body(new JsonBody(TTNftsApi.createJson(address)));
        } else if ("ROSE".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.get(new ApplicationLifecycle())
                    .api(new OasisTokensApi()
                            .setAddress(address));
        }
        if (httpRequest == null) {
            nftListAdapter.setList(null);
            binding.refreshLayout.finishRefresh();
            nftListAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
            return;
        }
        httpRequest.request(new OnHttpListener<String>() {
            @Override
            public void onSucceed(String result) {
                if ("TT".equalsIgnoreCase(symbol)) {
                    JsonRpc jsonRpc = JsonUtil.parseJsonToBean(result, JsonRpc.class);
                    if (jsonRpc != null) {
                        List<TTNft> ttNftList = JsonUtil.parseJsonToList(jsonRpc.getResult(), TTNft.class);
                        nftListAdapter.setList(TTNft.parse(ttNftList));
                        nftListAdapter.getLoadMoreModule().loadMoreEnd();
                        // 请求NFT详情数据
                        CollectionUtils.forAllDo(nftListAdapter.getData(), new CollectionUtils.Closure<NFTInfo>() {
                            @Override
                            public void execute(int index, NFTInfo item) {
                                getTTSingleNft(index, item);
                            }
                        });
                    }
                } else if ("ROSE".equalsIgnoreCase(symbol)) {
                    List<OasisToken> tokenList = JsonUtil.parseJsonToList(JsonUtils.getString(result, "result"), OasisToken.class);
                    List<NFTInfo> nftInfoList = new ArrayList<>();
                    for (OasisToken oasisToken : tokenList) {
                        if ("ERC-20".equals(oasisToken.getType())) continue;
                        nftInfoList.add(OasisToken.parse(oasisToken));
                    }
                    nftListAdapter.setList(nftInfoList);
                    nftListAdapter.getLoadMoreModule().loadMoreEnd();
                } else {
                    NFTInfoList nftInfoList = NFTAssets.parse(result);
                    if (TextUtils.isEmpty(cursor)) {
                        nftListAdapter.setList(nftInfoList.assets);
                    } else {
                        nftListAdapter.addData(nftInfoList.assets);
                    }
                    if (TextUtils.isEmpty(nftInfoList.next)) {
                        nftListAdapter.getLoadMoreModule().loadMoreEnd();
                    } else {
                        nftListAdapter.getLoadMoreModule().loadMoreComplete();
                        cursor = nftInfoList.next;
                    }
                }
                binding.refreshLayout.finishRefresh();
                if (nftListAdapter.getData().isEmpty()) {
                    nftListAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFail(Exception e) {
                nftListAdapter.getLoadMoreModule().loadMoreFail();
                binding.refreshLayout.finishRefresh();
                nftListAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
            }
        });
    }

    private void getTTSingleNft(int index, NFTInfo nftInfo) {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new TTSingleNftApi())
                .body(new JsonBody(TTSingleNftApi.createJson(nftInfo)))
                .request(new OnHttpListener<String>() {
                    @Override
                    public void onSucceed(String result) {
                        JsonRpc jsonRpc = JsonUtil.parseJsonToBean(result, JsonRpc.class);
                        if (jsonRpc != null) {
                            String json = ConvertUtils.bytes2String(Numeric.hexStringToByteArray(jsonRpc.getResult()));
                            Timber.i("getTTSingleNft-->" + json);
                            String url = null;
                            if (RegexUtils.isMatch("(.*)ipfs://(.*?)metadata.json(.*)", json)) {
                                url = RegexUtils.getMatches("ipfs://(.*?)metadata.json", json).get(0);
                                url = url.replace("ipfs://", "https://ipfs.io/ipfs/");
                            } else if (RegexUtils.isMatch("(.*)https://(.*?)metadata.json(.*)", json)) {
                                url = RegexUtils.getMatches("https://(.*?)metadata.json", json).get(0);
                            }
                            Timber.i("parseLink-->" + url);
                            if (TextUtils.isEmpty(url)) return;
                            OkHttpUtils.get().url(url).build().readTimeOut(20000).execute(new StringCallback() {
                                @Override
                                public void onResponse(String response, int id) {
                                    TTMetadata ttMetadata = JsonUtil.parseJsonToBean(response, TTMetadata.class);
                                    if (ttMetadata != null) {
                                        nftInfo.asset_name = ttMetadata.getName();
                                        nftInfo.thumb_url = ttMetadata.getImage().replace("ipfs://", "https://ipfs.io/ipfs/");
                                        nftInfo.original_url = nftInfo.thumb_url;
                                        nftListAdapter.setData(index, nftInfo);
                                    }
                                }

                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    Timber.e("getTTSingleNft-->" + e.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        NFTInfo nftInfo = nftListAdapter.getItem(position);
        if (TextUtils.isEmpty(nftInfo.original_url)) {
            return;
        }
        Timber.i("original_url-->" + nftInfo.original_url);
        String path = PathUtils.getExternalAppFilesPath() + "/nft_images/" + nftInfo.contract_address + "-" + nftInfo.token_id;
        if (!FileUtils.isFileExists(path)) {
            AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.setCanCancel(false);
            FileUtils.createOrExistsFile(path);
            if (nftInfo.original_url.endsWith("svg")) { // svg图片重新生成
                handleSvgPhoto(nftInfo, progressDialog, path);
            } else {
                downloadPhoto(nftInfo, progressDialog, path);
            }
        } else {
            openPhotoForSelect(nftInfo, new File(path));
        }
    }

    private void downloadPhoto(NFTInfo nftInfo, AlertDialog progressDialog, String path) {
        EasyHttp.download(new ApplicationLifecycle())
                .method(HttpMethod.GET)
                .file(new File(path))
                .url(nftInfo.original_url)
                .tag(getClass().getSimpleName())
                .listener(new OnDownloadListener() {
                    @Override
                    public void onStart(File file) {
                        progressDialog.show();
                    }

                    @Override
                    public void onProgress(File file, int progress) {
                        Timber.i("onProgress-->" + progress);
                    }

                    @Override
                    public void onComplete(File file) {
                        openPhotoForSelect(nftInfo, file);
                    }

                    @Override
                    public void onError(File file, Exception e) {
                        Timber.e("onError-->" + e);
                        FileUtils.delete(file);
                    }

                    @Override
                    public void onEnd(File file) {
                        progressDialog.dismiss();
                    }
                }).start();
    }

    private void handleSvgPhoto(NFTInfo nftInfo, AlertDialog progressDialog, String path) {
        progressDialog.show();
        Glide.with(getParentActivity()).load(nftInfo.original_url).addListener(new RequestListener<Drawable>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                progressDialog.dismiss();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<String>() {
                    @Override
                    public String doInBackground() throws Throwable {
                        Bitmap bitmap = ImageUtils.view2Bitmap(binding.ivNftAvatar);
                        FileIOUtils.writeFileFromBytesByStream(path, ConvertUtils.bitmap2Bytes(bitmap));
                        return null;
                    }

                    @Override
                    public void onSuccess(String result) {
                        progressDialog.dismiss();
                        openPhotoForSelect(nftInfo, new File(path));
                    }
                });
                return false;
            }
        }).into(binding.ivNftAvatar);

    }

    private void openPhotoForSelect(NFTInfo nftInfo, File file) {
        this.nftInfo = nftInfo;
        Uri inputUri = Uri.fromFile(file);
        String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(PathUtils.getExternalAppCachePath(), fileName));
        UCrop uCrop = UCrop.of(inputUri, destinationUri);
        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);
        options.setCircleDimmedLayer(true);
        options.withAspectRatio(1, 1);
        options.isDarkStatusBarBlack(true);
        uCrop.withOptions(options);
        uCrop.start(getParentActivity());
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {
                Uri output = Crop.getOutput(data);
                if (output == null) return;
                NftInfoApi nftInfoApi = new NftInfoApi()
                        .setNft_path(output.getPath())
                        .setNft_contract(nftInfo.contract_address)
                        .setNft_contract_image(nftInfo.original_url)
                        .setNft_token_id(nftInfo.token_id)
                        .setNft_name(nftInfo.asset_name)
                        .setNft_chain_id(currentChainType.getId())
                        .setNft_price(nftInfo.getEthPrice())
                        .setNft_token_standard(nftInfo.token_standard);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPLOAD_USER_PROFILE, currentTimeMillis + "", nftInfoApi));
                finishFragment();
            }
        }
    }
}
