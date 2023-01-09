package teleblock.model;

import java.util.List;


/**
 * Created by LSD on 2021/9/7.
 * Desc
 */
public class MiddleData {
    static MiddleData instance;

    public VideoSlipEntity videoSlipEntity;
    public ThemeEntity.ItemEntity themeInfo = null;
    public List<String> playList = null;

    public static MiddleData getInstance() {
        if (instance == null) {
            synchronized (MiddleData.class) {
                if (instance == null) {
                    instance = new MiddleData();
                }
            }
        }
        return instance;
    }
}
