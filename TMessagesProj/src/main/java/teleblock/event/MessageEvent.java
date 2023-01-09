package teleblock.event;

/**
 * EventBus 传递的参数
 */
public class MessageEvent {

    private String from;
    private String type;
    private Object data;

    public MessageEvent(String type) {
        this.type = type;
    }

    public MessageEvent(String type, String from) {
        this.type = type;
        this.from = from;
    }

    public MessageEvent(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public MessageEvent(String type, String from, Object data) {
        this.type = type;
        this.from = from;
        this.data = data;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
