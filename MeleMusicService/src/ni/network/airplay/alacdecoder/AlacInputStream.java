package ni.network.airplay.alacdecoder;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: Denis Tulskiy Date: 4/7/11
 */
public class AlacInputStream extends DataInputStream {
    /**
     * Creates a DataInputStream that uses the specified underlying InputStream.
     * 
     * @param in the specified input stream
     */
    public AlacInputStream(final InputStream in) {
        super(in);
    }

    public void seek(final long pos) {
        if (in instanceof FileInputStream) {
            try {
                ((FileInputStream)in).getChannel().position(pos);
            }
            catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
