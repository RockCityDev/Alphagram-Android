package teleblock.event.data;

/**
 * Created by LSD on 2021/5/17.
 * Desc
 */
public class CollectChangeEvent {
    public boolean collect;
    public int messageId;

    public CollectChangeEvent(int messageId, boolean collect) {
        this.messageId = messageId;
        this.collect = collect;
    }
}
