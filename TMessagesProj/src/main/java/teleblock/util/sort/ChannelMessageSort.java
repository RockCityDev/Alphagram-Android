package teleblock.util.sort;

import java.util.Comparator;

import teleblock.telegram.channels.ChannelMessage;

/**
 * Created by LSD on 2021/3/26.
 * Desc
 */
public class ChannelMessageSort implements Comparator<ChannelMessage> {
    @Override
    public int compare(ChannelMessage t1, ChannelMessage t2) {
        final long result = t2.messageObject.messageOwner.date - t1.messageObject.messageOwner.date;
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        } else {
            return 0;
        }
    }

}
