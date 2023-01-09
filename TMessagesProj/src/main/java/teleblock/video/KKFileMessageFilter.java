package teleblock.video;

import org.telegram.messenger.UserConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import teleblock.database.KKVideoMessageDB;
import teleblock.file.KKFileMessage;


public class KKFileMessageFilter<T extends KKFileMessage> {


    public void hideVideoMessage(KKFileMessage fileMessageMessage) {
        KKVideoMessageDB.getInstance(UserConfig.selectedAccount).addHideMessageId(fileMessageMessage.getId(), fileMessageMessage.getMessageObject().messageOwner.date);
    }

    public List<T> filterMessages(List<T> fileMessages) {
        List<T> result = new ArrayList<>();
        if (fileMessages == null || fileMessages.size() == 0) return result;
        int startDate = fileMessages.get(0).getMessageObject().messageOwner.date;
        int endDate = fileMessages.get(fileMessages.size() - 1).getMessageObject().messageOwner.date;
        List<Integer> ids = new ArrayList<>();
        for (T videoMessage : fileMessages) {
            ids.add(videoMessage.getId());
        }
        Set<Integer> hideIds = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).loadHideMessageIds(ids, Math.min(startDate, endDate), Math.max(startDate, endDate));
        if (hideIds == null || hideIds.size() == 0) return fileMessages;
        for (T videoMessage : fileMessages) {
            if (!hideIds.contains(videoMessage.getId())) {
                result.add(videoMessage);
            }
        }
        return result;
    }
}
