package teleblock.file;

import java.util.List;

/**
 * 查询文件消息结果回调接口
 */
public interface KKFileMessageLoadListener {

    /**
     * 加载完成后回调
     *
     * @param loadRequestId 请求数据时返回的loadRequestId，用于区分结果
     * @param fileMessages  返回的视频消息列表
     * @param dialogId      请求的dialogId
     */
    public void onMessagesLoad(int loadRequestId, long dialogId, List<KKFileMessage> fileMessages);

    /**
     * 视频列表加载出错
     *
     * @param loadRequestId 请求数据是返回的loadRequestId，用于区分结果
     * @param errorCode     错误码
     * @param msg           错误信息
     */
    public void onError(int loadRequestId, int errorCode, String msg);
}
