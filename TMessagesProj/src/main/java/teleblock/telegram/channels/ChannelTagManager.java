package teleblock.telegram.channels;

import org.telegram.messenger.UserConfig;

import java.util.ArrayList;
import java.util.List;

import teleblock.database.KKVideoMessageDB;
import teleblock.model.ChannelTagEntity;
import teleblock.model.ChannelWithTagEntity;


public class ChannelTagManager {

    private static ChannelTagManager instance;

    private ChannelTagManager() {
    }

    public static ChannelTagManager getInstance() {
        if (instance == null) {
            synchronized (ChannelTagManager.class) {
                if (instance == null) {
                    instance = new ChannelTagManager();
                }
            }
        }
        return instance;
    }

    public boolean deleteTag(int tag_id) {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteTagById(tag_id);
    }

    public boolean deleteTagWithNoChannel() {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteTagWhenNoChannel();
    }

    public boolean keepTagToDB(String tag) {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).createChannelTag(tag);
    }

    public List<ChannelTagEntity> getTagList() {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryChannelTagList();
    }

    public ChannelTagEntity getChannelTagByName(String name) {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryChannelTagByName(name);
    }

    public boolean keepChannelWithTag(long channel_id, int tag_id, String tag_name) {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).createChannelWithTag(Math.abs(channel_id), tag_id, tag_name);
    }

    public List<Long> getChannelIdsByTag(int tag_id) {
        List<Long> ids = new ArrayList<>();
        List<ChannelWithTagEntity> entities = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryChannelByTagId(tag_id);
        for (ChannelWithTagEntity tagEntity : entities) {
            ids.add(tagEntity.channelId);
        }
        return ids;
    }

    public List<ChannelWithTagEntity> getTagsByChannelId(long channel_id) {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).queryTagByChannelId(Math.abs(channel_id));
    }

    public boolean deleteChannelWithTag(long channel_id, int tag_id) {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteChannelWithTag(Math.abs(channel_id), tag_id);
    }

    public boolean deleteChannelWithTagByTagId(int tag_id) {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteChannelWithTagByTagId(tag_id);
    }

    public boolean deleteChannelWithTagByChannelId(long channel_id) {
        return KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteChannelWithTagByChannelId(Math.abs(channel_id));
    }

    public void updateChannelTagLevel(int tagId, int level) {
        KKVideoMessageDB.getInstance(UserConfig.selectedAccount).updateChannelLevelById(tagId, level);
    }
}
