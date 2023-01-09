package teleblock.util.sort;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;

/**
 * Time:2022/7/19
 * Author:Perry
 * Description：消息按照点赞人数降序排列
 */
public class MsgReactionCountSort implements Comparable<MsgReactionCountSort> {

    private boolean chosen;
    private ReactionsLayoutInBubble.VisibleReaction visibleReaction;
    private int count;

    public MsgReactionCountSort(boolean chosen, TLRPC.Reaction reaction, int count) {
        this.chosen = chosen;
        this.visibleReaction = ReactionsLayoutInBubble.VisibleReaction.fromTLReaction(reaction);
        this.count = count;
    }

    public boolean isChosen() {
        return chosen;
    }

    public ReactionsLayoutInBubble.VisibleReaction getVisibleReaction() {
        return visibleReaction;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(MsgReactionCountSort m) {
        return Integer.compare(this.count, m.count);
    }


}
