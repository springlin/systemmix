package com.myun.spring.dlna.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * 
 * @ClassName: MediaFormatUtils
 * @Description: 媒体格式处理
 * @author 欧阳占柱
 * @date 2012-6-18 下午03:31:19
 * 
 */
public class MediaFormatUtils {
    public static final String TAG = "MediaFormatUtils";
    /**
     * 图片类型后缀
     */
    public static final String JPG = "jpg";
    public static final String JPEG = "jpeg";
    public static final String GIF = "gif";
    public static final String PNG = "png";
    public static final String BMP = "bmp";
    /**
     * 音乐类型后缀
     */
    public static final String MP3 = "mp3";
    public static final String WMA = "wma";
    public static final String AMR = "amr";
    public static final String M4A = "m4a";
    public static final String OGG = "ogg";
    public static final String WAV = "wav";
    public static final String AWB = "awb";
    public static final String AAC = "aac";
    public static final String MP2 = "mp2";
    public static final String RA = "ra";
    public static final String FLAC = "flac";
    public static final String APE = "ape";
    /**
     * 视频类型后缀
     */
    public static final String QT = "qt";
    public static final String FLV = "flv";
    public static final String RM = "rm";
    public static final String RMVB = "rmvb";
    public static final String THREEGP = "3gp";
    public static final String WMV = "wmv";
    public static final String AVI = "avi";
    public static final String MP4 = "mp4";
    public static final String M4V = "m4v";
    public static final String THREEGPP = "3gpp";
    public static final String THREEGTWO = "3g2";
    public static final String THREEGPPTWO = "3gpp2";
    public static final String WM = "wm";
    public static final String RV = "rv";
    public static final String MKV = "mkv";
    public static final String VOB = "vob";
    public static final String TS = "ts";
    public static final String TP = "tp";

    /**
     * 直播格式
     */
    public static final String M3U8 = "m3u8";

    public static Map<String, String> videoTypes = new HashMap<String, String>();
    public static Map<String, String> imageTypes = new HashMap<String, String>();
    public static Map<String, String> audioTypes = new HashMap<String, String>();
    public static Map<String, String> liveTypes = new HashMap<String, String>();

    static {
        videoTypes.put(MP4, "video/mp4");
        videoTypes.put(M4V, "video/mp4");
        videoTypes.put(THREEGP, "video/3gpp");
        videoTypes.put(THREEGPP, "video/3gpp");
        videoTypes.put(THREEGTWO, "video/3gpp2");
        videoTypes.put(THREEGPPTWO, "video/3gpp2");

        videoTypes.put(WMV, "video/x-ms-wmv");
        videoTypes.put(WM, "video/x-ms-wmv");
        videoTypes.put(RM, "video/x-pn-realvideo");
        videoTypes.put(RV, "video/x-pn-realvideo");
        videoTypes.put(RMVB, "video/x-pn-realvideo");
        videoTypes.put(AVI, "video/x-msvideo");
        videoTypes.put(FLV, "video/x-flv");

        videoTypes.put(MKV, "video/mkv");
        videoTypes.put(VOB, "video/vob");
        videoTypes.put(TS, "video/ts");
        videoTypes.put(TP, "video/tp");

        imageTypes.put(JPEG, "image/jpeg");
        imageTypes.put(JPG, "image/jpeg");
        imageTypes.put(GIF, "image/gif");
        imageTypes.put(PNG, "image/png");
        imageTypes.put(BMP, "image/x-ms-bmp");

        audioTypes.put(MP3, "audio/mp3");
        audioTypes.put(WMA, "audio/wma");
        audioTypes.put(AMR, "audio/amr");
        audioTypes.put(M4A, "audio/m4a");
        audioTypes.put(OGG, "audio/ogg");
        audioTypes.put(WAV, "audio/wav");
        audioTypes.put(AWB, "audio/awb");
        audioTypes.put(AAC, "audio/aac");
        audioTypes.put(MP2, "audio/mp2");
        audioTypes.put(RA, "audio/ra");
        audioTypes.put(FLAC, "audio/flac");
        audioTypes.put(APE, "audio/ape");

        liveTypes.put(M3U8, M3U8);
    }

    /**
     * 图片类型
     */
    public final static int PICTURE = 0x1;
    /**
     * 音频类型
     */
    public final static int AUDIO = 0x2;
    /**
     * 视频类型
     */
    public final static int VIDIO = 0x3;
    /**
     * 直播类型
     */
    public final static int LIVE = 0x4;
    /**
     * 未知类型
     */
    public final static int UNKOWN = 0x5;

    public static boolean isVideoByPostfix(String fileName) {
        String postfix = fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length()).toLowerCase();
        return videoTypes.containsKey(postfix);
    }

    public static boolean isImageByPostfix(String fileName) {
        String postfix = fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length());
        return imageTypes.containsKey(postfix.toLowerCase());
    }

    public static boolean isAudioByPostfix(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length());
        return audioTypes.containsKey(suffix.toLowerCase());
    }

    public static boolean isLiveByPostfix(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length());
        return liveTypes.containsKey(suffix.toLowerCase());
    }

    public static int getMediaFormat(String fileName) {
        if (isVideoByPostfix(fileName))
            return VIDIO;
        else if (isAudioByPostfix(fileName))
            return AUDIO;
        else if (isImageByPostfix(fileName))
            return PICTURE;
        else if (isLiveByPostfix(fileName))
            return LIVE;
        else
            return UNKOWN;
    }

    public static int getMediaFormatByContentType(String url) {
        URLConnection cn = null;
        try {
            cn = new URL(url).openConnection();
            cn.setConnectTimeout(5000);
            cn.setReadTimeout(5000);
            cn.connect();
            String contentType = cn.getContentType();
            Log.d(TAG, "getMediaFormatByContentType contentType:" + contentType);
            if (contentType != null && !"".equals(contentType)) {
                if (contentType.startsWith("audio/")) {
                    return AUDIO;
                } else if (contentType.startsWith("image/")) {
                    return PICTURE;
                } else if (contentType.startsWith("video/")) {
                    return VIDIO;
                }
            }
            if (cn != null)
                cn = null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return UNKOWN;
        } catch (IOException e) {
            e.printStackTrace();
            return UNKOWN;
        } catch (Exception e) {
            e.printStackTrace();
            return UNKOWN;
        }
        return UNKOWN;
    }
}
