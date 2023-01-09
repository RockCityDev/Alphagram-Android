package teleblock.model.ui;

/**
 * Time:2022/7/22
 * Author:Perry
 * Description：缓存大小
 */
public class CacheSizeData {
    private Long size;
    private String name;
    private String color;
    private int type;
    private boolean ifClChecked;

    public CacheSizeData(Long size, String name, String color, int type, boolean ifClChecked) {
        this.size = size;
        this.name = name;
        this.color = color;
        this.type = type;
        this.ifClChecked = ifClChecked;
    }

    public int getType() {
        return type;
    }

    public void setIfClChecked(boolean ifClChecked) {
        this.ifClChecked = ifClChecked;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public boolean isIfClChecked() {
        return ifClChecked;
    }
}
