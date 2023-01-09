package teleblock.event.data;

/**
 * Created by LSD on 2021/3/20.
 * Desc
 */
public class DeleteVideoSelectEvent {
    public int num;
    public boolean selectAll;

    public DeleteVideoSelectEvent(int num, boolean selectAll) {
        this.num = num;
        this.selectAll = selectAll;
    }
}
