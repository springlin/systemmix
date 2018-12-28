package ni.network.http;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ni.common.util.Log;

public class HTTPResponse {

    private final StringBuilder headerData = new StringBuilder();
    private String content = null;
    private byte[] bytebuf = null;
    public  int size=0;

    public HTTPResponse(final String header) {
        headerData.append(header + "\r\n");
    }

    public void appendHeaderDate() {
        headerData.append("Date: " + getServerTime() + "\r\n");
    }

    public void appendHeaderValue(final String key, final String value) {
        headerData.append(key + ": " + value + "\r\n");
    }

    public void appendHeaderContent(final String aContentType, final String aContent) {
        content = aContent;
        // Content header
        if (aContent != null) {
            if (aContentType != null) {
                headerData.append("Content-Type: " + aContentType + "\r\n");
            }
            headerData.append("Content-Length: " + aContent.length() + "\r\n");
        }
    }

    public void appendHeaderContent(final String aContentType, final byte[] bytebuf) {
        this.bytebuf = bytebuf;
        // Content header
        if (bytebuf != null) {
            if (aContentType != null) {
                headerData.append("Content-Type: " + aContentType + "\r\n");
            }
            headerData.append("Content-Length: " + bytebuf.length + "\r\n");
        }
    }    
    public String buildPacketString() {
        StringBuilder packet = new StringBuilder();

        // Header fields
        packet.append(headerData);

        // Content data
        packet.append("\r\n");
        if (content != null && content.length() > 0) {
            packet.append(content);
        }

        return packet.toString();
    }

    public byte[] buildPacketBytes() {
        try {
           
        	if(bytebuf==null){
        		byte[] buf=buildPacketString().getBytes("UTF-8");
        		size=buf.length;
        	    return buf;
        	}else{
        		ByteBuffer raw=ByteBuffer.allocate(4  * 1024);
        		raw.put(buildPacketString().getBytes("UTF-8"));
        		//Log.d("lcm","qvod raw "+raw.position());
        		raw.put(bytebuf);
        		//Log.d("lcm","qvod raw "+raw.position());
        		size=raw.position();
        		return raw.array();
        	}
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return " > " + buildPacketString().replaceAll("\r\n", "\r\n > ");
    }

    static String getServerTime() {
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }
}
