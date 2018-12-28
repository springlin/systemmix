package com.scope.api;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/3/5.
 */

public class DevInfo {


    public static Context ct=null;
    public static String getDeviceInfo(Context _ct) {

        ct=_ct;
      //  DisplayMetrics metric = new DisplayMetrics();
      //  ((Activity)ct).getWindowManager().getDefaultDisplay().getMetrics(metric);
      //  PackageManager pkgM= ct.getPackageManager();

        String totalSDMemory = String.valueOf(getSDCardMemory()[0] / (1024.0 * 1024.0 * 1024.0));
        totalSDMemory = totalSDMemory.substring(0, totalSDMemory.indexOf(".") + 3);
        String availableMemory = String.valueOf(getSDCardMemory()[1] / (1024.0 * 1024.0 * 1024.0));
        availableMemory = availableMemory.substring(0, totalSDMemory.indexOf(".") + 3);
  //      sb.append("机身存储 : " + totalSDMemory + "G"+  " ; 可用 : "+ availableMemory+"G"+"\n"  );

        JSONObject result = new JSONObject();
        try {


            JSONObject info = new JSONObject();
            result.put("devinfo", info);

            info.put("buildVer", Build.VERSION.SDK_INT+"");
            info.put("wifiMac", tryGetWifiMac(ct));
            info.put("wifiSSID", tryGetWifiSSID(ct));
            info.put("hardDisk", totalSDMemory+"G");
            info.put("hardDiskUsed", availableMemory+"G");
            info.put("memory", getTotalMemory());
            info.put("memoryUsed",getAvailMemory() );
            info.put("temperature",getCpuTemp()+ "℃");
            info.put("cpu", getCpuName());
            info.put("cpuRate", getMinCpuFreq() + "-"+ getMaxCpuFreq() +"MHZ");
            info.put("battery", "100%");
            info.put("timestamp", getTime());
            info.put("runTime", (SystemClock.uptimeMillis() / 1000000)+"s");
            info.put("screenLocked", getScreenLocked()?"true":"false");



        }catch (JSONException e) {
            e.printStackTrace();
        }
       // Utils.log("getDeviceInfo----->"+result.toString());
        return result.toString();


//        StringBuilder sb = new StringBuilder();
//        sb.append("BUILD VERSION : " + Build.VERSION.SDK_INT + "\n" );
//        sb.append("WiFi MAC : " + tryGetWifiMac(ct) + "\n" );
//        sb.append("WiFi SSID : " + tryGetWifiSSID(ct) + "\n" );
//        sb.append("CPU型号 : " + getCpuName() + "\n" );
//        sb.append("CPU核心数 : " + getNumberOfCPUCores() + "\n" );
//        sb.append("CPU频率 : " + getMinCpuFreq() + "-"+ getMaxCpuFreq() +" MHZ "+"\n" );
//        sb.append("屏幕尺寸 : " + metric.heightPixels + " * "+ metric.widthPixels+"\n"  );
//        sb.append("运行内存 : " + getTotalMemory() + "\n");
//        String totalSDMemory = String.valueOf(getSDCardMemory()[0] / (1024.0 * 1024.0 * 1024.0));
//        totalSDMemory = totalSDMemory.substring(0, totalSDMemory.indexOf(".") + 3);
//        String availableMemory = String.valueOf(getSDCardMemory()[1] / (1024.0 * 1024.0 * 1024.0));
//        availableMemory = availableMemory.substring(0, totalSDMemory.indexOf(".") + 3);
//        sb.append("机身存储 : " + totalSDMemory + "G"+  " ; 可用 : "+ availableMemory+"G"+"\n"  );
//        StringBuilder append = sb.append("屏幕密度 : " + metric.density + "\n");
//
//        Utils.log(""+append.toString());
    }
    public static boolean getScreenLocked(){

        KeyguardManager mKeyguardManager = (KeyguardManager) ct.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();

    }
    public static String getTime(){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
        //SystemClock.elapsedRealtimeNanos() / 1000000
    }

    private static String tryGetWifiMac(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        if (wi == null || wi.getMacAddress() == null) {
            return null;
        }
        if ("02:00:00:00:00:00".equals(wi.getMacAddress().trim())) {
            return null;
        } else {
            return wi.getMacAddress().trim();
        }
    }
    private static String tryGetWifiSSID(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        if (wi == null || wi.getMacAddress() == null) {
            return null;
        }
        return wi.getSSID().replace("\"", "");
    }
    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {

            return 1;
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = -1;
        } catch (NullPointerException e) {
            cores = -1;
        }
        return cores;
    }
    public static int getCpuTemp(){

        try {
            FileReader fr = new FileReader("/sys/class/thermal/thermal_zone0/temp");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            return Integer.parseInt(text.trim() )/1000;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
           return 0;
    }
    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };


    private static String isSupport(boolean pValue){
        return (pValue ? "支持" : "不支持");
    }

    public static long[] getSDCardMemory() {
        long[] sdCardInfo=new long[2];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long bCount = sf.getBlockCount();
            long availBlocks = sf.getAvailableBlocks();

            sdCardInfo[0] = bSize * bCount;
            sdCardInfo[1] = bSize * availBlocks;
        }
        return sdCardInfo;
    }

    /**
     * 获取手机内存大小
     *
     * @return
     */
    private static String getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString = null;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
               // Log.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();
        } catch (IOException e) {
        }
//        return Formatter.formatFileSize(getBaseContext(), initial_memory);// Byte转换为KB或者MB，内存大小规格化
        return String.valueOf(Integer.valueOf(arrayOfString[1]).intValue() / 1024) + "M";
    }

    /**
     * 获取当前可用内存大小
     *
     * @return
     */
    private static String getAvailMemory() {
        ActivityManager am = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return Formatter.formatFileSize(ct, mi.availMem);
    }

    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        int maxFreq = Integer.parseInt(result.trim())/(1024);
        return String.valueOf(maxFreq);
    }

    // 获取CPU最小频率（单位KHZ）
    public static String getMinCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        int minFreq = Integer.parseInt(result.trim())/(1024);
        return String.valueOf(minFreq);
    }

    // 实时获取CPU当前频率（单位KHZ）
    public static String getCurCpuFreq() {
        String result = "N/A";
        try {
            FileReader fr = new FileReader(
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim() + "Hz";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String getScreenSizeOfDevice2() {// 获取屏幕尺寸，英寸
        Point point = new Point();
        ((Activity)ct).getWindowManager().getDefaultDisplay().getRealSize(point);
        DisplayMetrics dm = ct.getResources().getDisplayMetrics();
        double x = Math.pow(point.x / dm.xdpi, 2);
        double y = Math.pow(point.y / dm.ydpi, 2);
        double screenInches = Math.sqrt(x + y);
        String inches = String.valueOf(screenInches);
        return inches.substring(0,inches.indexOf(".")+3);
    }
}
