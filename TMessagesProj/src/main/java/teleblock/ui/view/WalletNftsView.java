package teleblock.ui.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.JsonUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.RegexUtils;
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
import com.luck.picture.lib.utils.DateUtils;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.yalantis.ucrop.UCrop;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewNftEmptyBinding;
import org.telegram.messenger.databinding.ViewWalletHomeBinding;
import org.telegram.ui.ActionBar.AlertDialog;
import org.web3j.utils.Numeric;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.JsonRpc;
import teleblock.model.wallet.NFTAssets;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTInfoList;
import teleblock.model.wallet.OasisToken;
import teleblock.model.wallet.TTMetadata;
import teleblock.model.wallet.TTNft;
import teleblock.model.wallet.TokenBalance;
import teleblock.network.api.blockchain.ethereum.EthNftsApi;
import teleblock.network.api.blockchain.oasis.OasisTokensApi;
import teleblock.network.api.blockchain.polygon.PolygonAssentsApi;
import teleblock.network.api.blockchain.thundercore.TTNftsApi;
import teleblock.network.api.blockchain.thundercore.TTSingleNftApi;
import teleblock.ui.activity.WalletHomeAct;
import teleblock.ui.adapter.NFTAssetsAdapter;
import teleblock.ui.dialog.CommonTipsDialog;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.divider.CustomItemDecoration;
import timber.log.Timber;

public class WalletNftsView extends FrameLayout implements OnRefreshListener, OnLoadMoreListener, OnItemClickListener {

    private ViewWalletHomeBinding binding;
    private final WalletHomeAct walletHomeAct;
    private NFTAssetsAdapter nftListAdapter;
    private String order_direction = "desc";
    private String limit = "10";
    private String cursor;

    public WalletNftsView(WalletHomeAct walletHomeAct) {
        super(walletHomeAct.getParentActivity());
        this.walletHomeAct = walletHomeAct;
        initView();
        initData();
    }

    private void initView() {
        binding = ViewWalletHomeBinding.inflate(LayoutInflater.from(getContext()), this, true);

    }

    private void initData() {
        binding.refreshLayout.setEnableLoadMore(false);
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerView.addItemDecoration(new CustomItemDecoration(2, 20f, 20f, true));
        nftListAdapter = new NFTAssetsAdapter();
        nftListAdapter.setEmptyView(createEmptyView());
        nftListAdapter.getEmptyLayout().setVisibility(View.GONE);
        nftListAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        nftListAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(nftListAdapter);
        binding.refreshLayout.autoRefresh();
    }

    private View createEmptyView() {
        ViewNftEmptyBinding binding = ViewNftEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmptyNft.setText(LocaleController.getString("nft_assets_empty_text", R.string.nft_assets_empty_text));
        return binding.getRoot();
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
        WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = MMKVUtil.currentChainConfig();
        if (chainType == null) return;
        String symbol = chainType.getMain_currency_name();
        HttpRequest httpRequest = null;
        if ("ETH".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.get(new ApplicationLifecycle())
                    .api(new EthNftsApi()
                            .setOwner(walletHomeAct.address)
                            .setOrder_direction(order_direction)
                            .setLimit(limit)
                            .setCursor(cursor)
                            .setInclude_orders(true));
        } else if ("MATIC".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.get(new ApplicationLifecycle())
                    .api(new PolygonAssentsApi()
                            .setOwner_address(walletHomeAct.address)
                            .setOrder_direction(order_direction)
                            .setLimit(limit)
                            .setCursor(cursor)
                            .setInclude_orders(true));
        } else if ("TT".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.post(new ApplicationLifecycle())
                    .api(new TTNftsApi())
                    .body(new JsonBody(TTNftsApi.createJson(walletHomeAct.address)));
        } else if ("ROSE".equalsIgnoreCase(symbol)) {
            httpRequest = EasyHttp.get(new ApplicationLifecycle())
                    .api(new OasisTokensApi()
                            .setAddress(walletHomeAct.address));
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
        if (!walletHomeAct.userSelf) return; // 只能选自己的NFT
        NFTInfo nftInfo = nftListAdapter.getItem(position);
        if (TextUtils.isEmpty(nftInfo.original_url)) {
            return;
        }
        new CommonTipsDialog(walletHomeAct.getParentActivity(), "使用此 NFT 作为头像显示？") {
            @Override
            public void onRightClick() {
                String path = PathUtils.getExternalAppFilesPath() + "/nft_images" + nftInfo.original_url.substring(nftInfo.original_url.lastIndexOf("/"));
                boolean svg = FileUtils.getFileExtension(path).equals("svg");
                if (svg) path = path.substring(0, path.length() - 4) + ".jpg";
                if (!FileUtils.isFileExists(path)) {
                    AlertDialog progressDialog = new AlertDialog(walletHomeAct.getParentActivity(), 3);
                    progressDialog.setCanCancel(false);
                    FileUtils.createOrExistsFile(path);
                    if (svg) { // svg图片重新生成
                        handleSvgPhoto(nftInfo, progressDialog, path);
                    } else {
                        downloadPhoto(nftInfo, progressDialog, path);
                    }
                } else {
                    openPhotoForSelect(nftInfo, new File(path));
                }
            }
        }
                .setLeftTextAndColor("取消", Color.parseColor("#868686"))
                .setRightTextAndColor("确认", Color.parseColor("#03BDFF"))
                .show();
    }

    private void downloadPhoto(NFTInfo nftInfo, AlertDialog progressDialog, String path) {
        EasyHttp.download(new ApplicationLifecycle())
                .method(HttpMethod.GET)
                .file(new File(path))
                .url(nftInfo.original_url)
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
        Glide.with(walletHomeAct.getParentActivity()).load(nftInfo.original_url).addListener(new RequestListener<Drawable>() {

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
        walletHomeAct.nftInfo = nftInfo;
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
        uCrop.start(walletHomeAct.getParentActivity());
    }
}