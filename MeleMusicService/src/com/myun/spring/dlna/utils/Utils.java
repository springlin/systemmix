package com.myun.spring.dlna.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;

import org.cybergarage.upnp.UPnP;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.myun.core.*;
import com.myun.net.protocol.PeerTools;

/**
 * 
 * 宸ュ叿绫�
 * 
 * @author 娆ч槼鍗犳煴
 * @date 2012-5-29 涓嬪崍02:14:51
 * 
 */
public class Utils {
    private static final String TAG = "Utils";
    public static final String SERVICE_DESCRIPTION = "/NscreenRenderer.xml";

//    public static String getNID(Context context) {
//        // String nid = PeerTools.getNID(context);
//        // Log.i(TAG, "setDeviceID.......nid=" + nid);
//        // String result = android.os.Build.MODEL;
//        // if (nid != null && !"".equals(nid)) {
//        // result = PeerMessage.NID_FLAG
//        // + nid.replaceAll(":", "").substring(6);
//        // }
//        String deviceID = getDeviceIDFullName(context);
//        Log.i(TAG, "getNID.......deviceID=" + deviceID);
//        String result = android.os.Build.MODEL;
//        if (deviceID != null && !"".equals(deviceID)) {
//            result = deviceID;
//        } else {
//            result = context.getString(R.string.device_name, result);
//        }
//
//        return result;
//    }

    
	public static String getDeviceIDFullName(Context context) {
		return PeerTools.getDeviceName(context);
	}
	
	
    public static String getSerialNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceid = getDefaultSerialNumber();
        if (deviceid != null && !"".equals(deviceid)) {
            deviceid = tm.getDeviceId();
        }
        Log.d(TAG, "getSerialNumber deviceid:" + deviceid);
        return deviceid;
    }

    private static String getDefaultSerialNumber() {
        StringBuffer TimeString = new StringBuffer();
        Calendar c = Calendar.getInstance();

        int yyyy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH) + 1;
        int dd = c.get(Calendar.DAY_OF_MONTH);
        int hh = c.get(Calendar.HOUR_OF_DAY);// 涓浗灞炰簬涓滃叓鍖猴紝鏃堕棿鍔�灏忔椂
        int MM = c.get(Calendar.MINUTE);
        int SS = c.get(Calendar.SECOND);
        TimeString.append(yyyy);
        TimeString.append(mm < 10 ? "0" + mm : "" + mm);
        TimeString.append(dd < 10 ? "0" + dd : "" + dd);
        TimeString.append(hh < 10 ? "0" + hh : "" + hh);
        TimeString.append(MM < 10 ? "0" + MM : "" + MM);
        TimeString.append(SS < 10 ? "0" + SS : "" + SS);

        return TimeString.toString();
    }

    public static String getUDN(Context context) {
        StringBuffer udn = new StringBuffer();
        udn.append("uuid:");
        udn.append(getDeviceIDFullName(context).substring(2, 6));
        udn.append(UPnP.createUUID().toUpperCase());
        udn.append("-");
        udn.append(getDeviceIDFullName(context).substring(2, 6));
        Log.d(TAG, "getUDN udn:" + udn.toString());
        return udn.toString();
    }

    public static void createServiceFile(Context context) {
        String filename = "NscreenDMR/NscreenRenderer.xml";

        copyAssetToAppDir(context, filename);

        filename = "NscreenDMR/service/AVTransport.xml";
        copyAssetToAppDir(context, filename);

        filename = "NscreenDMR/service/ConnectionManager.xml";
        copyAssetToAppDir(context, filename);

        filename = "NscreenDMR/service/RenderingControl.xml";
        copyAssetToAppDir(context, filename);

        filename = "NscreenDMR/icons/ic_large.png";
        copyAssetToAppDir(context, filename);

        filename = "NscreenDMR/icons/ic_small.png";
        copyAssetToAppDir(context, filename);

        filename = "NscreenDMR/icons/ic_large.jpg";
        copyAssetToAppDir(context, filename);

        filename = "NscreenDMR/icons/ic_small.jpg";
        copyAssetToAppDir(context, filename);
    }

    private static void copyAssetToAppDir(Context context, String filename) {
        InputStream isd = null;
        FileOutputStream fos = null;

        String sourcefilename = context.getFilesDir().getAbsolutePath() + "/"
                + filename;
        Log.d(TAG, "copyAssetToAppDir sourcefilename:" + sourcefilename);
        // create destination directory
        File sourceFile = new File(sourcefilename);

        // copy to destination
        try {
            isd = context.getAssets().open(filename);
            fos = context.openFileOutput(sourceFile.getName(),
                    Context.MODE_PRIVATE);
            while (isd.available() > 0) {
                byte[] b = new byte[1024];
                int bytesread = isd.read(b);
                fos.write(b, 0, bytesread);
            }
            if (fos != null) {
                fos.close();
                fos = null;
            }
            if (isd != null) {
                isd.close();
                isd = null;
            }
            String destfilename = context.getFilesDir().getAbsolutePath() + "/"
                    + sourceFile.getName();

            File targetFile = new File(destfilename);
            InputStream fileIn = new FileInputStream(targetFile);

            Log.d(TAG, "copyAssetToAppDir fileName=" + targetFile.getName()
                    + " InputStream.read=" + fileIn.available());
            if (fileIn != null) {
                fileIn.close();
                fileIn = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long fromHMS2Millis(String mhsIn) {
        int hasMillis = mhsIn.indexOf(".");
        long millis = 0;
        if (hasMillis != -1) {
            millis = Long.parseLong(mhsIn.split("\\.")[1]);
        }
        long seconds = 0;
        long minutes = 0;
        long hours = 0;

        String[] mhs = mhsIn.split("\\.")[0].split(":");
        if (mhs.length == 3) {
            seconds = Long.parseLong(mhs[2]);
            minutes = Long.parseLong(mhs[1]);
            hours = Long.parseLong(mhs[0]);
        } else if (mhs.length == 2) {
            seconds = Long.parseLong(mhs[1]);
            minutes = Long.parseLong(mhs[0]);
        } else {
            seconds = Long.parseLong(mhs[0]);
        }

        return millis + (seconds * 1000) + (minutes * 60 * 1000)
                + (hours * 60 * 60 * 1000);
    }

    public static String DisplayProgress(long frommili) {

        StringBuilder sBuilder = new StringBuilder();

        long hours = frommili / 3600000;
        if (hours < 10) {
            sBuilder.append("0");
            sBuilder.append(hours);
            sBuilder.append(":");
        } else {
            sBuilder.append(hours);
            sBuilder.append(":");
        }
        long minutes = (frommili - (hours * 3600000)) / 60000;
        if (minutes < 10) {
            sBuilder.append("0");
            sBuilder.append(minutes);
            sBuilder.append(":");
        } else {
            sBuilder.append(minutes);
            sBuilder.append(":");
        }
        long seconds = (frommili - (hours * 3600000) - minutes * 60000) / 1000;
        if (seconds < 10) {
            sBuilder.append("0");
            sBuilder.append(seconds);
        } else {
            sBuilder.append(seconds);
        }
        return sBuilder.toString();
    }

    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();
        Log.d(TAG, "getLocalMacAddress mac:" + mac);
        return mac;
    }

    private static final String SETTING_INFO = "Settings_Info";
    private static SharedPreferences mSettings = null;
    public static final String UDN_KEY = "UDN";

    public static void saveSharedPrefValue(Context mContext, String key,
            String value) {
        if (mSettings == null)
            mSettings = mContext.getSharedPreferences(SETTING_INFO, 0);
        mSettings.edit().putString(key, value).commit();
    }

    public static String getSharedPrefValue(Context mContext, String key,
            String def) {
        if (mSettings == null)
            mSettings = mContext.getSharedPreferences(SETTING_INFO, 0);
        return mSettings.getString(key, def);
    }

    public static String getLiveName(Context mContext, String path) {
        String temp = null;
        String fileName = path;
        try {
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1,
                    fileName.length());
            if (suffix.equalsIgnoreCase(MediaFormatUtils.M3U8)) {
                int start = fileName.lastIndexOf("/");
                int end = fileName.lastIndexOf(".");
                temp = fileName.substring(start + 1, end);
                return temp.toUpperCase();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mContext.getString(R.string.unknow_title);
    }

    public static String urlEncode(String url) {

        if (url.startsWith("http://")) {
            try {
                url = URLDecoder.decode(url.replaceAll("\\+", "%2B"), "UTF-8");
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
            }

            Log.d(TAG, "urlDecode urlNew:" + url);
            try {
                url = URLEncoder.encode(url, "UTF-8").replaceAll("%3A", ":")
                        .replaceAll("%2F", "/").replaceAll("%3F", "?")
                        .replaceAll("%3D", "=").replaceAll("%26", "&")
                        .replaceAll("\\+", "%20");
                Log.d(TAG, "urlEncode urlNew:" + url);
                return url;
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
                return url;
            }
        }
        return url;
    }
}
