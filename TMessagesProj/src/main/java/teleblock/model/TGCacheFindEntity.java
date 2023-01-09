package teleblock.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LSD on 2021/9/29.
 * Desc
 */
public class TGCacheFindEntity implements Serializable {
    public long totalSize = 0;
    public List<TGCacheEntity> list = new ArrayList<>();
}
