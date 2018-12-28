package ni.network.http;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ni.common.util.Log;
import ni.common.util.Strings;

import org.apache.commons.lang3.StringUtils;

public class HTTPPacket {
    private static final Pattern patternCompleted = Pattern.compile("(.*)\r\n\r\n");
    private static final Pattern patternRequest = Pattern.compile("^(\\w+)\\W(.+)\\WHTTP/(.+)\r\n");
    private static final Pattern patternStatus = Pattern.compile("^HTTP/(.+) (\\w+) (\\w+)\r\n");

    private String req;
    private String directory;
    private ArrayList<String> parameters;
    private String rtspVersion;
    private int packetHeaderEnd;
    private int contentLength;
    private final Vector<String> headers;
    private final Vector<String> headerContent;
    private final ByteBuffer rawPacket;

    public HTTPPacket() {
        headers = new Vector<String>();
        headerContent = new Vector<String>();
        rawPacket = ByteBuffer.allocate(4 * 1024 * 1024);
    }

    public void reset() {
        req = null;
        directory = null;
        parameters = null;
        rtspVersion = null;
        packetHeaderEnd = -1;
        contentLength = -1;
        headers.clear();
        headerContent.clear();
        rawPacket.rewind();
    }

    public String getPacketHeaderFromRawPacket() {
        String r;
        if (packetHeaderEnd == -1) {
            final String packet = new String(getRawPacket(), 0, getRawPacketSize());
            final Matcher matcher = patternCompleted.matcher(packet.toString());
            if (matcher.find()) {
                packetHeaderEnd = matcher.end();
                r = packet.substring(0, packetHeaderEnd);
            }
            else {
                return null;
            }
        }
        else {
            r = new String(getRawPacket(), 0, packetHeaderEnd);
        }
        return r;
    }

    public boolean parseHeader() {
        final String packet = getPacketHeaderFromRawPacket();
        if (packet == null)
            return false;

        // If packet completed
        // First line
        final Matcher m = patternRequest.matcher(packet);
        if (m.find()) {
            req = m.group(1);
            directory = m.group(2);
            rtspVersion = m.group(3);
            
        //    Log.d("lcm","qvod req:"+req+" directory:"+directory);
            directory=directory.replace(".xml", "xml");
            
            
            if (directory.contains("?")) {
            	parameters = new ArrayList<String>();
            	String[] splits = Strings.split(directory,"?");
            	directory = splits[0];
            	for (int i = 1; i < splits.length; ++i) {
            		String s = splits[i];
            		if (s.contains("&")) {
            			String[] s2 = Strings.split(s,"&");
            			for (String param : s2) {
            				parameters.add(param);
            			}
            		}
            		else {
            			parameters.add(s);
            		}
            	}
            }
        }
        else {
            // Check for a standard HTTP status header
            final Matcher matchStatus = patternStatus.matcher(packet);
            if (matchStatus.find()) {
                rtspVersion = matchStatus.group(1);
                req = matchStatus.group(2);
                directory = "";
            }
            else {
                // gni ?? what's that that we received ??
            }
        }

        // Header fields
        parseAsHeaderFields(packet);

        // Get the content length
        try {
            final int cl = Integer.valueOf(valueOfHeader("Content-Length"));
            if (cl > 0) {
                contentLength = cl;
            }
        }
        catch (final Throwable t) {

        }

        return true;
    }

    public void parseAsHeaderFields(final String packet) {
        final String normalizedPacket = StringUtils.replace(packet, "\r\n", "\n");
        final Pattern p = Pattern.compile("^([\\w-]+):\\W(.+)\n", Pattern.MULTILINE);
        final Matcher m = p.matcher(normalizedPacket);
        while (m.find()) {
            headers.add(m.group(1));
            headerContent.add(m.group(2));
        }
    }

    public void append(final byte[] buffer, final int len) {
        rawPacket.put(buffer, 0, len);
    }

    public byte[] getRawPacket() {
        return rawPacket.array();
    }

    public int getRawPacketSize() {
        return rawPacket.position();
    }

    public int getContentLength() {
        return contentLength;
    }

    public int getContentOffset() {
        return packetHeaderEnd;
    }

    public boolean hasFullContent() {
        if (packetHeaderEnd < 0 || contentLength < 0)
            return false;
        return getRawPacketSize() >= packetHeaderEnd + contentLength;
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

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public int getCode() {
        return 200;
    }

    public String valueOfHeader(final String headerName) {
        return valueOfHeader(headerName,null);
    }

    public String valueOfHeader(final String headerName, final String aDefault) {
        int i = headers.indexOf(headerName);
        if (i == -1) {
            i = headerName.indexOf(headerName.toLowerCase());
            if (i == -1)
                return aDefault;
        }
        return headerContent.elementAt(i);
    }

    @Override
    public String toString() {
        final String hdr = getPacketHeaderFromRawPacket();
        String s = " < " + hdr.replaceAll("\r\n", "\r\n < ");//
        s = s.length() > 1024 ? s.substring(0, 1023) + " ..." : s;
        return s;
    }
}
