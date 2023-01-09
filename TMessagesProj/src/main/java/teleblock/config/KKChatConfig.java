package teleblock.config;

/**
 * Created by LSD on 2021/7/2.
 * Desc
 */
public class KKChatConfig {

    public enum ChatType {
        NULL(""),
        CHANNEL("channel"),
        GROUP("group"),
        BOT("bot");

        private final String type;

        public String getType() {
            return type;
        }

        ChatType(String type) {
            this.type = type;
        }
    }

    public enum QueryOrder {
        HOT("follows"),
        NEW("update_time");

        private final String order;

        public String getOrder() {
            return order;
        }

        QueryOrder(String order) {
            this.order = order;
        }
    }
}
