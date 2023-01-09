/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;

import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.R;
import org.telegram.messenger.SecureDocument;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;


import teleblock.config.AppConfig;
import teleblock.database.KKVideoMessageDB;
import teleblock.model.wallet.WalletInfo;
import teleblock.widget.NftHexagonView;
import timber.log.Timber;

public class BackupImageView extends View {

    // 自定义START
    public boolean drawNftView;
    public NftHexagonView nftHexagonView;
    //链的图片 id
    public boolean ifShowChainIcon = true;//是否显示链图标
    public int chainId = -1;
    // 自定义END

    protected ImageReceiver imageReceiver;
    protected int width = -1;
    protected int height = -1;
    public AnimatedEmojiDrawable animatedEmojiDrawable;
    private AvatarDrawable avatarDrawable;
    boolean attached;

    public BackupImageView(Context context) {
        super(context);
        imageReceiver = new ImageReceiver(this);
    }

    public void setOrientation(int angle, boolean center) {
        imageReceiver.setOrientation(angle, center);
    }

    public void setImage(SecureDocument secureDocument, String filter) {
        setImage(ImageLocation.getForSecureDocument(secureDocument), filter, null, null, null, null, null, 0, null);
    }

    public void setImage(ImageLocation imageLocation, String imageFilter, String ext, Drawable thumb, Object parentObject) {
        setImage(imageLocation, imageFilter, null, null, thumb, null, ext, 0, parentObject);
    }

    public void setImage(ImageLocation imageLocation, String imageFilter, Drawable thumb, Object parentObject) {
        setImage(imageLocation, imageFilter, null, null, thumb, null, null, 0, parentObject);
    }

    public void setImage(ImageLocation mediaLocation, String mediaFilter, ImageLocation imageLocation, String imageFilter, Drawable thumb, Object parentObject) {
        imageReceiver.setImage(mediaLocation, mediaFilter, imageLocation, imageFilter, null, null, thumb, 0, null, parentObject, 1);

        setNftData(parentObject, NftHexagonView.FORCE_DISPLAY); // 个人主页强制显示所有样式
    }

    public void setImage(ImageLocation imageLocation, String imageFilter, Bitmap thumb, Object parentObject) {
        setImage(imageLocation, imageFilter, null, null, null, thumb, null, 0, parentObject);
    }

    public void setImage(ImageLocation imageLocation, String imageFilter, Drawable thumb, int size, Object parentObject) {
        setImage(imageLocation, imageFilter, null, null, thumb, null, null, size, parentObject);
    }

    public void setImage(ImageLocation imageLocation, String imageFilter, Bitmap thumbBitmap, int size, int cacheType, Object parentObject) {
        Drawable thumb = null;
        if (thumbBitmap != null) {
            thumb = new BitmapDrawable(null, thumbBitmap);
        }
        imageReceiver.setImage(imageLocation, imageFilter, null, null, thumb, size, null, parentObject, cacheType);
    }

    public void setForUserOrChat(TLObject object, AvatarDrawable avatarDrawable) {
        imageReceiver.setForUserOrChat(object, avatarDrawable);

        setNftData(object, NftHexagonView.DEFULT);
    }

    public void setNftData(Object object, int model) {
        drawNftView = false;
        chainId = -1;
        if (object instanceof TLRPC.User) {
            TLRPC.User user = (TLRPC.User) object;
            if (user.photo != null) {
                WalletInfo walletInfo = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).getUserNftData(user.id);
                if (walletInfo != null) {
                    drawNftView = walletInfo.getNft_photo_id() == user.photo.photo_id;
                    chainId = walletInfo.chain_id;

                    if (drawNftView) {
                        if (nftHexagonView == null) {
                            nftHexagonView = new NftHexagonView(getContext());
                            nftHexagonView.setModel(model);
                            nftHexagonView.changeSize(getWidth(), getHeight());
                        }
                        nftHexagonView.setNftData(walletInfo.nft_name, walletInfo.nft_token_id);
                    }
                }
            }
        }
    }

    public void setForUserOrChat(TLObject object, AvatarDrawable avatarDrawable, Object parent) {
        imageReceiver.setForUserOrChat(object, avatarDrawable, parent);
    }

    public void setImageMedia(ImageLocation mediaLocation, String mediaFilter, ImageLocation imageLocation, String imageFilter, Bitmap thumbBitmap, int size, int cacheType, Object parentObject) {
        Drawable thumb = null;
        if (thumbBitmap != null) {
            thumb = new BitmapDrawable(null, thumbBitmap);
        }
        imageReceiver.setImage(mediaLocation, mediaFilter, imageLocation, imageFilter, null, null, thumb, size, null, parentObject, cacheType);
    }

    public void setImage(ImageLocation imageLocation, String imageFilter, ImageLocation thumbLocation, String thumbFilter, int size, Object parentObject) {
        setImage(imageLocation, imageFilter, thumbLocation, thumbFilter, null, null, null, size, parentObject);
    }

    public void setImage(String path, String filter, Drawable thumb) {
        setImage(ImageLocation.getForPath(path), filter, null, null, thumb, null, null, 0, null);
    }

    public void setImage(String path, String filter, String thumbPath, String thumbFilter) {
        setImage(ImageLocation.getForPath(path), filter, ImageLocation.getForPath(thumbPath), thumbFilter, null, null, null, 0, null);
    }

    public void setImage(ImageLocation imageLocation, String imageFilter, ImageLocation thumbLocation, String thumbFilter, Drawable thumb, Bitmap thumbBitmap, String ext, int size, Object parentObject) {
        if (thumbBitmap != null) {
            thumb = new BitmapDrawable(null, thumbBitmap);
        }
        imageReceiver.setImage(imageLocation, imageFilter, thumbLocation, thumbFilter, thumb, size, ext, parentObject, 0);
    }

    public void setImage(ImageLocation imageLocation, String imageFilter, ImageLocation thumbLocation, String thumbFilter, String ext, long size, int cacheType, Object parentObject) {
        imageReceiver.setImage(imageLocation, imageFilter, thumbLocation, thumbFilter, null, size, ext, parentObject, cacheType);
    }

    public void setImageMedia(ImageLocation mediaLocation, String mediaFilter, ImageLocation imageLocation, String imageFilter, ImageLocation thumbLocation, String thumbFilter, String ext, int size, int cacheType, Object parentObject) {
        imageReceiver.setImage(mediaLocation, mediaFilter, imageLocation, imageFilter, thumbLocation, thumbFilter, null, size, ext, parentObject, cacheType);
    }

    public void setImageBitmap(Bitmap bitmap) {
        imageReceiver.setImageBitmap(bitmap);
    }

    public void setImageResource(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        imageReceiver.setImageBitmap(drawable);
        invalidate();
    }

    public void setImageResource(int resId, int color) {
        Drawable drawable = getResources().getDrawable(resId);
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        }
        imageReceiver.setImageBitmap(drawable);
        invalidate();
    }

    public void setImageDrawable(Drawable drawable) {
        imageReceiver.setImageBitmap(drawable);
    }

    public void setLayerNum(int value) {
        imageReceiver.setLayerNum(value);
    }

    public void setRoundRadius(int value) {
        imageReceiver.setRoundRadius(value);
        invalidate();
    }

    public void setRoundRadius(int tl, int tr, int bl, int br) {
        imageReceiver.setRoundRadius(tl, tr, bl ,br);
        invalidate();
    }

    public int[] getRoundRadius() {
        return imageReceiver.getRoundRadius();
    }

    public void setAspectFit(boolean value) {
        imageReceiver.setAspectFit(value);
    }

    public ImageReceiver getImageReceiver() {
        return imageReceiver;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        invalidate();
    }

    public AvatarDrawable getAvatarDrawable() {
        if (avatarDrawable == null) {
            avatarDrawable = new AvatarDrawable();
        }
        return avatarDrawable;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
        imageReceiver.onDetachedFromWindow();
        if (animatedEmojiDrawable != null) {
            animatedEmojiDrawable.removeView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        imageReceiver.onAttachedToWindow();
        if (animatedEmojiDrawable != null) {
            animatedEmojiDrawable.addView(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ImageReceiver imageReceiver = animatedEmojiDrawable != null ? animatedEmojiDrawable.getImageReceiver() : this.imageReceiver;
        if (imageReceiver == null) {
            return;
        }
        if (width != -1 && height != -1) {
            imageReceiver.setImageCoords((getWidth() - width) / 2, (getHeight() - height) / 2, width, height);
        } else {
            imageReceiver.setImageCoords(0, 0, getWidth(), getHeight());
        }

        if (drawNftView) { // 绘制六边形
            nftHexagonView.changeSize(getWidth(), getHeight());
            Bitmap bitmap = imageReceiver.getBitmap();
            if (bitmap == null || bitmap.getWidth() == 0) {
                imageReceiver.draw(canvas);
                return;
            }

            nftHexagonView.setBitmap(bitmap);
            nftHexagonView.draw(canvas);
        } else {
            imageReceiver.draw(canvas);
        }

        //绘制bitmap
        canvasBitmap(canvas);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        imageReceiver.setColorFilter(colorFilter);
    }

    public void setAnimatedEmojiDrawable(AnimatedEmojiDrawable animatedEmojiDrawable) {
        if (this.animatedEmojiDrawable == animatedEmojiDrawable) {
            return;
        }
        if (attached && this.animatedEmojiDrawable != null) {
            this.animatedEmojiDrawable.removeView(this);
        }
        this.animatedEmojiDrawable = animatedEmojiDrawable;
        if (attached && animatedEmojiDrawable != null) {
            animatedEmojiDrawable.addView(this);
        }
    }

    ValueAnimator roundRadiusAnimator;

    public void animateToRoundRadius(int animateToRad) {
        if (getRoundRadius()[0] != animateToRad) {
            if (roundRadiusAnimator != null) {
                roundRadiusAnimator.cancel();
            }
            roundRadiusAnimator = ValueAnimator.ofInt(getRoundRadius()[0], animateToRad);
            roundRadiusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setRoundRadius((Integer) animation.getAnimatedValue());
                }
            });
            roundRadiusAnimator.setDuration(200);
            roundRadiusAnimator.start();
        }
    }

    /**
     * 绘制bitmap
     * @param canvas
     */
    public void canvasBitmap(Canvas canvas) {
        if (getWidth() == 0) {
            return;
        }
        Bitmap bitmap;

        if (chainId == -1) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transparent_space_icon);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("user_chain_logo_" + chainId, "drawable", getContext().getPackageName()));
        }

        //如果设置不显示图标，或者bitmap为null，或者数据库里面么有这个人的信息
        if (!ifShowChainIcon || bitmap == null || !drawNftView) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transparent_space_icon);
        }

        //bitmap缩放
        int bitmapWidth = bitmap.getWidth();
        float scaleWidth = getWidth() * AppConfig.ViewConfig.COIN_ICON_SCALING / bitmapWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        Timber.i("chainId-->" + chainId + "---" + bitmapWidth + "---" + scaleWidth + "---" + getWidth());
        Bitmap newChainImgBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapWidth, matrix, true);

        //绘制bitmap
        float marginLeft = getWidth() - newChainImgBitmap.getWidth();
        float marginTop = newChainImgBitmap.getWidth() * AppConfig.ViewConfig.COIN_ICON_MAGIN_TOP;
        if (drawNftView) {
            marginLeft = marginLeft - newChainImgBitmap.getWidth() * AppConfig.ViewConfig.COIN_ICON_MAGIN_LEFT;
        } else {
            marginLeft = marginLeft - newChainImgBitmap.getWidth() * AppConfig.ViewConfig.COIN_ICON_MAGIN_LEFT / 2;
        }
        canvas.drawBitmap(newChainImgBitmap, marginLeft, marginTop, null);
        newChainImgBitmap.recycle();
        bitmap.recycle();
    }
}
