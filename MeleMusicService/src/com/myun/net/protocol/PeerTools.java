/**
 * 
 */
package com.myun.net.protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;



public class PeerTools {

	private static final String TAG = "PeerTools";
	private static final String DEFAULT_MAC = "00:00:00:00:00:00";

	private static Runtime runtime = null;
	private static Process process = null;
	public static final String SPEAKER_NOTIFY_STATUS ="com.speaker.notify_status";
	
	public synchronized static String execCommand(String command) {
		StringBuffer stringBuffer = new StringBuffer();
		String line = "";
		try {
			runtime = Runtime.getRuntime();
			process = runtime.exec(command);
			process.waitFor();
			InputStream inputStream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputStream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

			while (null != (line = bufferedreader.readLine())) {
				stringBuffer.append(line);
				stringBuffer.append('\n');
			}
			bufferedreader.close();
			inputstreamreader.close();
			inputStream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
		return stringBuffer.toString();
	}

	@SuppressLint("NewApi")
	public static String getBcastAddress() {

		String bcastAddress = "255.255.255.255";
		if (Integer.valueOf(android.os.Build.VERSION.SDK) > 8) {

			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();

					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isMulticastAddress()) {

							if (!inetAddress.isLoopbackAddress()) {

								List<InterfaceAddress> list = intf.getInterfaceAddresses();
								Iterator<InterfaceAddress> it = list.iterator();

								while (it.hasNext()) {
									InterfaceAddress ia = it.next();
									return ia.getBroadcast().getHostAddress().toString();
								}

							}

						}
					}
				}
			} catch (java.lang.NoSuchMethodError ex) {
				ex.printStackTrace();

			} catch (SocketException e) {
				e.printStackTrace();
			}

		} else {
			return bcastAddress;
		}
		return null;
	}

	public static String getLocalIpAddress(Context context) {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					String ipAddress = inetAddress.getHostAddress();
					if (!inetAddress.isLoopbackAddress() && isIPV4Address(ipAddress)) {
						return ipAddress;
					} else {
						return getWifiIpAddress(context);
					}

				}
			}
		} catch (SocketException ex) {
			return getWifiIpAddress(context);
		}
		return "";
	}

	public static String getLocalWiFiMAC_java() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				Log.i(TAG, "getDisplayName="+intf.getDisplayName());
				if ("wlan0".equals(intf.getName())) {

					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {

							byte[] mac = intf.getHardwareAddress();
							StringBuffer sb = new StringBuffer();
							for (int i = 0; i < mac.length; i++) {
								if (i != 0) {
									sb.append(":");
								}
								String s = Integer.toHexString(mac[i] & 0xFF);
								sb.append(s.length() == 1 ? 0 + s : s);
							}

							return sb.toString().toUpperCase();
						}
					}
					
				}
			}
		} catch (Exception ex) {

		}
		return "";
	}

	public static String getLocalWiFiMAC(Context context) {
		
		return getMacAddressByFile();
		

	}

	
	public static String loadFileAsString(String filePath) throws java.io.IOException{
	    StringBuffer fileData = new StringBuffer(1000);
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    char[] buf = new char[1024];
	    int numRead=0;
	    while((numRead=reader.read(buf)) != -1){
	        String readData = String.valueOf(buf, 0, numRead);
	        fileData.append(readData);
	    }
	    reader.close();
	    return fileData.toString();
	}
	
	public static String readBuildPropFile(String cmd) throws java.io.IOException{
		BufferedReader reader = new BufferedReader(new FileReader("/system/build.prop"));
		String result = null;
		String line = null;
		while((line = reader.readLine()) != null){
			if (line.contains(cmd)) {
				if (line.contains("=")) {
					result = line.split("=")[1];
				}
				break;
			}
		}
		reader.close();
		return result;
	}
	
	public static String readPropFile(String file, String cmd) throws java.io.IOException{
		if (file != null && !"".equals(file)) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String result = null;
			String line = null;
			while((line = reader.readLine()) != null){
				if (line.startsWith("#")) {
					continue;
				}
				if (line.startsWith(cmd)) {
					if (line.contains("=")) {
						result = line.split("=")[1];
					}
					break;
				}
			}
			reader.close();
			
			return result;
		}
		
		return null;
	}

	/*
	 * Get the STB MacAddress
	 */
	public static String getMacAddressByFile(){
	    try {
	        return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
	    } catch (IOException e) {
	        
	        try {
				return loadFileAsString("/sys/class/net/wlan1/address").toUpperCase().substring(0, 17);
			} catch (IOException e1) {
				
				
				try {
					return loadFileAsString("/sys/class/net/wlan0/address").toUpperCase().substring(0, 17);
				} catch (IOException e2) {
				}
				
				
			}
	        
	    }
	    
	    return DEFAULT_MAC.toUpperCase().substring(0, 17);
	}
	
	public static String getDeviceName(Context mContext){
		 return PeerMessage.NID_FLAG + PeerTools.getNID(mContext).replaceAll(":", "").substring(6);
	}
	private static boolean isIPV4Address(String address) {
		if (address != null) {
			String ipmatches = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
			return address.matches(ipmatches);
		} else
			return false;
	}

	public static String getWifiIpAddress(Context context) {
		if (context != null) {
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			int i = info.getIpAddress();
			String ip = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
			return ip == null ? "" : ip;
		} else
			return "";
	}

//	public static boolean installApk(Context ctx, String apkPath) {
//		boolean result = false;
//		final Object obj = new Object();
//		synchronized (obj) {
//
//			
//			android.content.pm.PackageInfo info = ctx.getPackageManager().getPackageArchiveInfo(apkPath, android.content.pm.PackageManager.GET_ACTIVITIES);
//			Log.i(TAG, "installApk start"+apkPath+info.packageName);
//
//			
//	           int installFlags = 0;
//	            PackageManager pm = ctx.getPackageManager();
//	            try {
//	                PackageInfo pi = pm.getPackageInfo(info.packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
//	                if(pi != null) {
//	               //     installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
//	                }
//	            } catch (NameNotFoundException e) {}
//	            
//	            PackageInstallObserver observer = new PackageInstallObserver();
//	          //  pm.installPackage(Uri.fromFile(new File(apkPath)), observer, installFlags, info.packageName);
//		}
//
//		Log.i(TAG, "installApk finish");
//
//		return true;
//	}
	
//	public static boolean installApk(Context ctx, String apkPath, String packagename) {
//		boolean result = false;
//		final Object obj = new Object();
//		synchronized (obj) {
//
//			
//
//			
//	           int installFlags = 0;
//	            PackageManager pm = ctx.getPackageManager();
//	            try {
//	                PackageInfo pi = pm.getPackageInfo(packagename, PackageManager.GET_UNINSTALLED_PACKAGES);
//	                if(pi != null) {
//	                 //   installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
//	                }
//	            } catch (NameNotFoundException e) {}
//	            
//	           // PackageInstallObserver observer = new PackageInstallObserver();
//	           // pm.installPackage(Uri.fromFile(new File(apkPath)), observer, installFlags, packagename);
//		}
//
//		Log.i(TAG, "installApk finish");
//
//		return true;
//	}	
//   public static class PackageInstallObserver extends IPackageInstallObserver.Stub {
//        public void packageInstalled(String packageName, int returnCode) {
//        	Log.i(TAG, "installApk finish"+ returnCode);
//        }
//    };
	public static boolean uninstallApk(Context ctx, String apkPackage) {

		boolean result = false;

		final Object obj = new Object();
		synchronized (obj) {

			Log.i(TAG, "uninstallApk start");


			while (!result) {
				try {
					Log.i(TAG, "uninstallApk wait....");
					obj.wait();
					Log.i(TAG, "uninstallApk wait stop");
					break;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

//			result = pm.isPackageDeleteResult();
		}

		Log.i(TAG, "uninstallApk finish");

		return result;
	}


	//
	
	public static String getDeviceID(Context context) {
		String nid = getNID(context);
		return PeerMessage.NID_FLAG + nid.replaceAll(":", "").substring(6);
		
	}
	
	public static String getNID(Context context) {
		String mac = null;
		
//		if (mac == null) {
//			SharedPreferences sp = context.getSharedPreferences(PeerMessage.PEER_SHARED_PREFERENCES_NID, 0);
//			String spMac = sp.getString(PeerMessage.PEER_SHARED_PREFERENCES_NID_KEY, "");
//			if (!"".equals(spMac)) {
//				mac = spMac;
//			} else {
				mac = getLocalWiFiMAC(context);
//				sp.edit().putString(PeerMessage.PEER_SHARED_PREFERENCES_NID_KEY, mac);
//			}
//		}
//		if (mac == null) {
//			mac = SystemProperties.get(PeerMessage.PEER_SETTING_DEVICE_ID, PeerMessage.PEER_SETTING_DEFAULT_DEVICE_ID);
//		}
		return mac;
	}

	public static String decodeMac(String mac) {

		long result = 0;

		if (mac == null || mac.length() <= 0) {
			mac = "FF:FF:FF:FF:FF:FF";
		}

		String[] macArr = mac.split(":");
		for (int i = 0; i < macArr.length; i++) {
			int k = Integer.parseInt(macArr[i], 16);
			result = (result << 8) + k;
		}

		result = result * 12345678;
		if (result < 0) {
			result = -result;
		}

		String str = "000000000000" + Long.toString(result);

		return str.substring(str.length() - 5);
	}

	
	public static int getPidByProcessName(Context mContext, String processName){
		int pid = -1;
		
		if (processName != null && !"".equals(processName)) {
			
			ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> mRunningProcess = mActivityManager.getRunningAppProcesses();
			
			for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
				
				if (processName.equals(amProcess.processName)) {
					pid = amProcess.pid;
				}
			}
		}
		
		return pid;
		
	}
	
	public static void killApp(Context mContext, String packageName){
		if (packageName != null && !"".equals(packageName)) {
			final ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);   
			//am.forceStopPackage(packageName);
		}
	}
	
	public static String randomCode(){
		int i = (int) (Math.random()*9000+1000);
		return String.valueOf(i);
	}
	



}
