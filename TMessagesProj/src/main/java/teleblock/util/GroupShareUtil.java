package teleblock.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.PathUtils;

import org.telegram.messenger.databinding.ViewGroupShareBinding;

import java.io.File;
import java.io.IOException;

import teleblock.widget.GlideHelper;

public class GroupShareUtil {
    public interface AvatarLoader {
        void onLoad(File file);
    }

    public static void getShareImage(Context context, String title, String desc, String inviter, String photoUrl, AvatarLoader avatarLoader) {
        ViewGroupShareBinding shareBinding = ViewGroupShareBinding.inflate(LayoutInflater.from(context));
        GlideHelper.getDrawableGlide(context, photoUrl, drawable -> {
            Bitmap avatarBitmap = ConvertUtils.drawable2Bitmap(drawable);
            shareBinding.ivAvatar.setImageBitmap(avatarBitmap);
            shareBinding.tvGroupName.setText(title);
            shareBinding.tvGroupDesc.setText(desc);
            shareBinding.tvInvater.setText(inviter);
            Bitmap bitmap = ConvertUtils.view2Bitmap(shareBinding.getRoot());
            File directory = new File(PathUtils.getExternalAppCachePath() + "/share");
            if (!directory.exists() && !directory.isDirectory()) {
                directory.mkdirs();
            }
            File shareFile = new File(directory, "share.png");
            try {
                shareFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageUtils.save(bitmap, shareFile, Bitmap.CompressFormat.PNG);
            avatarLoader.onLoad(shareFile);
        });
    }
}
