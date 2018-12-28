package ni.network.airplay.raop;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract informations from RTSP Header
 * 
 * @author bencall
 */
//
public class RTSPPacket {
    private String req;
    private String directory;
    private String rtspVersion;
    private String content;
    private final Vector<String> headers;
    private final Vector<String> headerContent;
    private final String rawPacket;

    public RTSPPacket(final String packet) {
        // Init arrays
        headers = new Vector<String>();
        headerContent = new Vector<String>();
        rawPacket = packet;

        // If packet completed
        // First line
        Pattern p = Pattern.compile("^(\\w+)\\W(.+)\\WRTSP/(.+)\r\n");
        Matcher m = p.matcher(packet);
        if (m.find()) {
            req = m.group(1);
            directory = m.group(2);
            rtspVersion = m.group(3);
        }

        // Header fields
        p = Pattern.compile("^([\\w-]+):\\W(.+)\r\n", Pattern.MULTILINE);
        m = p.matcher(packet);
        while (m.find()) {
            headers.add(m.group(1));
            headerContent.add(m.group(2));
        }

        // Content if present or null if not
        p = Pattern.compile("\r\n\r\n(.+)", Pattern.DOTALL);
        m = p.matcher(packet);
        if (m.find()) {
            content = m.group(1).trim();
            if (content.length() == 0) {
                content = null;
            }
        }
    }

    public String getRawPacket() {
        return rawPacket;
    }

    public String getContent() {
        return content;
    }

    public String getReq() {
        return req;
    }

    public String getVersion() {
        return rtspVersion;
    }

    public String getDirectory() {
        return directory;
    }

    public int getCode() {
        return 200;
    }

    public String valueOfHeader(final String headerName) {
        final int i = headers.indexOf(headerName);
        if (i == -1) {
            return null;
        }
        return headerContent.elementAt(i);
    }

    @Override
    public String toString() {
        final String s = " < " + rawPacket.replaceAll("\r\n", "\r\n < ");
        return s.length() > 1024 ? s.substring(0, 1023) + " ..." : s;
    }
}
