package teleblock.model;

import org.telegram.tgnet.NativeByteBuffer;

/**
 * Created by LSD on 2022/1/12.
 * Desc
 */
public class DBDeleteMsgEntity {
    public long mid;
    public long uid;
    public int readState;
    public int sendState;
    public int date;
    public NativeByteBuffer data;
    public int out;
    public int ttl;
    public int media;
    public NativeByteBuffer replydata;
    public int imp;
    public int mention;
    public int forwards;
    public NativeByteBuffer repliesdata;
    public int thread_reply_id;
    public int is_channel;
    public int reply_to_message_id;
    public long delete_time;
}
