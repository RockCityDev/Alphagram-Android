package teleblock.ui.adapter;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dylanc.viewbinding.brvah.BaseViewHolderUtilKt;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ItemNftAssetBinding;

import teleblock.model.wallet.NFTInfo;
import teleblock.widget.GlideHelper;
import timber.log.Timber;

public class NFTAssetsAdapter extends BaseQuickAdapter<NFTInfo, BaseViewHolder> implements LoadMoreModule {

    public NFTAssetsAdapter() {
        super(R.layout.item_nft_asset);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, NFTInfo item) {
        ItemNftAssetBinding binding = BaseViewHolderUtilKt.getBinding(baseViewHolder, ItemNftAssetBinding::bind);
        Timber.i("url-->" + item.thumb_url + "   " + item.original_url);
        Glide.with(getContext())
                .load(R.drawable.nft_loading)
                .transform(new CenterCrop(), new RoundedCorners(AndroidUtilities.dp(5)))
                .into(binding.ivLoading);
        Glide.with(getContext())
                .load("Oasis".equals(item.blockchain) ? R.drawable.nft_holder_oasis : item.thumb_url)
                .transform(new CenterCrop(), new RoundedCorners(AndroidUtilities.dp(5)))
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        binding.ivLoading.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(binding.ivNftAvatar);
        binding.tvAssetName.setText(item.asset_name);
        binding.tvNftName.setText(item.nft_name);
        if (TextUtils.isEmpty(item.price)) {
            binding.tvNftPrice.setVisibility(View.INVISIBLE);
        } else {
            binding.tvNftPrice.setVisibility(View.VISIBLE);
            binding.tvNftPrice.setText(item.getEthPrice());
        }
    }
}
