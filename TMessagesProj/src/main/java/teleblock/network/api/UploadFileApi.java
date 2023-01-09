package teleblock.network.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestType;
import com.hjq.http.model.BodyType;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import teleblock.util.ShareUtil;

/**
 * Time:2022/9/1
 * Author:Perry
 * Description：文件上传
 */
public class UploadFileApi implements IRequestApi, IRequestType {

    private MultipartBody.Part part;
    private String folder;

    @NonNull
    @Override
    public String getApi() {
        return "/upload/file";
    }

    @NonNull
    @Override
    public BodyType getBodyType() {
        return BodyType.FORM;
    }

    public UploadFileApi setFolder(String folder) {
        this.folder = folder;
        return this;
    }

    public UploadFileApi setPart(File file) {
        this.part = MultipartBody.Part.createFormData(
                "file",
                file.getName(),
                RequestBody.create(file, MediaType.parse(ShareUtil.getMimeType(file.getPath())))
        );
        return this;
    }
}
