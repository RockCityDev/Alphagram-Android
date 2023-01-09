package teleblock.telegram.channels;

/**
 * 查询文件消息结果回调接口
 */
public interface ChannelMessageLoadListener {
    /**
     * 列表加载出错
     */
    void onMessageLoadError();
}
