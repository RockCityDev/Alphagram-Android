package teleblock.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SearchTagEntity implements Serializable {

    public String name;
    public String word;

    public List<SearchTagEntity> list;
    @SerializedName("default")
    public List<String> defaults;

    public SearchTagEntity() {
    }

    public SearchTagEntity(String name, String word) {
        this.name = name;
        this.word = word;
    }
}