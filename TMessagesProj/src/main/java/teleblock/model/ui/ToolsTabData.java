package teleblock.model.ui;

import android.graphics.Color;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

/**
 * Time:2022/7/4
 * Author:Perry
 * Description：工具页面 tab数据
 */
public class ToolsTabData {

    private int id;
    private String name;
    private int icon;
    private int color;
    private int background;
    private int[] colors;

    public ToolsTabData(int id, String name, int icon, int[] colors) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.colors = colors;
    }

    public ToolsTabData(int id, String name, int icon, String color) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.color = Color.parseColor(color);
    }

    public ToolsTabData(int id, String name, int icon, int background) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.background = background;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public int getBackground() {
        return background;
    }

    public int[] getColors() {
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }
}
