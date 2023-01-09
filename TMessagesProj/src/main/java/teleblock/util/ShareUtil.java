package teleblock.util;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.FileUtils;

import org.telegram.messenger.BuildConfig;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ShareUtil {
    public static boolean shareVideo(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return false;
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".file_provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        }
        intent.setType("video/*");
        Intent chooser = Intent.createChooser(intent, "Share：");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
        return true;
    }

    public static boolean shareVideo2(Context context, String filePath) {
        //Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, getContentUri(context, filePath));
        shareIntent.setType("video/*");
        context.startActivity(Intent.createChooser(shareIntent, "Share："));
        return true;
    }

    public static Uri getContentUri(Context context, String filePath) {
        try {
            Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=? ", new String[]{filePath}, null);
            Uri uri = null;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    Uri baseUri = Uri.parse("content://media/external/video/media");
                    uri = Uri.withAppendedPath(baseUri, "" + id);
                }
                cursor.close();
            }
            if (uri == null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, filePath);
                uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            }
            return uri;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 系统分享本地文件
     *
     * @param context
     * @param path
     */
    public static void shareLocalFile(Context context, String path, String pkg) {
        File shareFile = new File(path);
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//若SDK大于等于24  获取uri采用共享文件模式
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", shareFile);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(getMimeType(path));
        if (pkg.equals("com.tencent.mobileqq"))//QQ
            intent.setComponent(new ComponentName(pkg, "com.tencent.mobileqq.activity.JumpActivity"));
        else if (pkg.equals("com.tencent.mm"))//weixin
            intent.setComponent(new ComponentName(pkg, "com.tencent.mm.ui.tools.ShareImgUI"));
        else if (pkg.isEmpty())
            intent = Intent.createChooser(intent, "Share");
        else
            intent.setPackage(pkg);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "没有可用于分享的应用程序", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 系统分享多个文件
     *
     * @param context
     * @param pathList
     */
    public static void shareLocalFiles(Context context, List<String> pathList) {
        Intent intent = new Intent();
        ArrayList<Uri> uris = new ArrayList<Uri>();
        boolean isImage = true;
        for (String path : pathList) {
            isImage = getMimeType(path).startsWith("image");
            File shareFile = new File(path);
            if (isImage) {
                uris.add(getImageContentUri(context, shareFile));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//若SDK大于等于24  获取uri采用共享文件模式
                uris.add(FileProvider.getUriForFile(context, context.getPackageName() + ".provider", shareFile));
            } else {
                uris.add(Uri.fromFile(shareFile));
            }
        }
        if (uris.size() > 1) {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            shareLocalFile(context, pathList.get(0), "");
            return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(isImage ? "image/*" : "*/*");
        intent = Intent.createChooser(intent, "Share");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "没有可用于分享的应用程序", Toast.LENGTH_SHORT).show();
        }
    }


    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 获得文件对应的MIME类型
     */
    public static String getMimeType(String path) {
        String type = "*/*";
        try {
            String url = URLEncoder.encode(path, "UTF-8");
            //使用系统API，获取URL路径中文件的后缀名（扩展名）
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (TextUtils.isEmpty(extension)) {
                extension = FileUtils.getFileExtension(path);
            }
            if (!TextUtils.isEmpty(extension)) {
                //使用系统API，获取MimeTypeMap的单例实例，然后调用其内部方法获取文件后缀名（扩展名）所对应的MIME类型
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type == null ? "*/*" : type;
    }
}
