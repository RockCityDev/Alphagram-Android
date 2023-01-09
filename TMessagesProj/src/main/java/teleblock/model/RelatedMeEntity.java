package teleblock.model;

import com.chad.library.adapter.base.entity.JSectionEntity;

/**
 * 创建日期：2022/7/5
 * 描述：
 */
public class RelatedMeEntity extends JSectionEntity {

    private boolean isHeader;
    private Object object;
    private boolean canPin;

    public RelatedMeEntity(boolean isHeader, Object object) {
        this.isHeader = isHeader;
        this.object = object;
    }

    public RelatedMeEntity(boolean isHeader, Object object, boolean canPin) {
        this.isHeader = isHeader;
        this.object = object;
        this.canPin = canPin;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public boolean isHeader() {
        return isHeader;
    }

    public boolean isCanPin() {
        return canPin;
    }
}