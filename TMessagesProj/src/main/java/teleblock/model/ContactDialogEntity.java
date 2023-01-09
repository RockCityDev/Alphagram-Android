package teleblock.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.telegram.tgnet.TLRPC;

/**
 * 创建日期：2022/6/27
 * 描述：
 */
public class ContactDialogEntity implements MultiItemEntity {

    public static final int TYPE_CONTACT = 1;
    public static final int TYPE_DIALOG = 2;

    private int itemType;
    private TLRPC.TL_contact contact;
    private TLRPC.Dialog dialog;

    public TLRPC.TL_contact getContact() {
        return contact;
    }

    public void setContact(TLRPC.TL_contact contact) {
        this.contact = contact;
    }

    public TLRPC.Dialog getDialog() {
        return dialog;
    }

    public void setDialog(TLRPC.Dialog dialog) {
        this.dialog = dialog;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}