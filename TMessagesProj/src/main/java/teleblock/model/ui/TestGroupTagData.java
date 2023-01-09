package teleblock.model.ui;

/**
 * Time:2022/8/29
 * Author:Perry
 * Description：测试群标签数据
 */
public class TestGroupTagData {

    private String tagName;
    private boolean isCheck;

    public TestGroupTagData(String tagName, boolean isCheck) {
        this.tagName = tagName;
        this.isCheck = isCheck;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
