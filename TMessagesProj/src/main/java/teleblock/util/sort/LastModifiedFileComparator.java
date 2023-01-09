package teleblock.util.sort;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class LastModifiedFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 7372168004395734046L;
    public static final Comparator<File> LASTMODIFIED_COMPARATOR = new LastModifiedFileComparator();
    public static final Comparator<File> LASTMODIFIED_REVERSE = new ReverseComparator(LASTMODIFIED_COMPARATOR);

    @Override
    public int compare(final File file1, final File file2) {
        if (file1 == null && file2 == null) {
            return 0;
        }
        if (file1 == null) {
            return -1;
        }
        if (file2 == null) {
            return 1;
        }
        final long result = file1.lastModified() - file2.lastModified();
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
