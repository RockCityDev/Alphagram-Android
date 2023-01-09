package teleblock.event.data;

/**
 * Created by LSD on 2021/9/9.
 * Desc
 */
public class ThemePreviewEvent {
    public String path;
    public String name;

    public ThemePreviewEvent(String path, String name) {
        this.path = path;
        this.name = name;
    }
}
