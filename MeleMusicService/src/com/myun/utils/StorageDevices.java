package com.myun.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;





import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;

/**
 * manager of flash,sdcard,usbhost
 * 
 * 
 * 
 */
public class StorageDevices {
	private static String TAG = "DeviceManager";
	private ArrayList<String> totalDevicesList;
	private ArrayList<String> sdDevicesList;
	private ArrayList<String> usbDevicesList;
	private ArrayList<String> sataDevicesList;
	private ArrayList<String> internalDevicesList;
	private ArrayList<String> mountedDevicesList;
	private Context mContext;
	private StorageManager manager;

	public StorageDevices(Context mContext) {
		this.mContext = mContext;
	    int vs=Utils.getAndroidSDKVersion();
		if(vs>15 && vs<21){
		
//				totalDevicesList = new ArrayList<String>();
//				String[] volumeList;
//				manager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
//				volumeList = manager.getVolumePaths();
//				for (int i = 0; i < volumeList.length; i++) {
//					totalDevicesList.add(volumeList[i]);
//				}
//		
//				
//				internalDevicesList = new ArrayList<String>();
//				internalDevicesList.add(Environment.getExternalStorageDirectory().getPath());
//		
//				sdDevicesList = new ArrayList<String>();
//				usbDevicesList = new ArrayList<String>();
//				sataDevicesList = new ArrayList<String>();
//				String path;
//				for (int i = 0; i < totalDevicesList.size(); i++) {
//					path = totalDevicesList.get(i);
//					if (!path.equals(Environment.getExternalStorageDirectory().getPath())) {
//						if (path.contains("sdcard")) {
//							
//							sdDevicesList.add(path);
//						} else if (path.contains("sd")) {
//							
//							usbDevicesList.add(path);
//						} else if (path.contains("sata")) {
//							
//							sataDevicesList.add(path);
//						}
//					}
//				}
				
		}else{
			totalDevicesList = new ArrayList<String>();
			getSDCardPathEx();
			if(totalDevicesList.indexOf(Environment.getExternalStorageDirectory().getPath())==-1){
				totalDevicesList.add(Environment.getExternalStorageDirectory().getPath());
			}
			
		}
	}

	public void freshDevices(){
		
		    getSDCardPathEx();
	}
	public boolean isDevicesRootPath(String path) {
		for (int i = 0; i < totalDevicesList.size(); i++) {
			if (path.equals(totalDevicesList.get(i)))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public ArrayList<String> getTotalDevicesList() {
		return (ArrayList<String>) totalDevicesList.clone();
	}

	public LinkedList<String> getMountedDevicesList() {
		//String state=null;
		
		//int vs=Utils.getAndroidSDKVersion();
		freshDevices();
		LinkedList<String> mountedDevices = new LinkedList<String>();
		mountedDevices.addAll(totalDevicesList);
		
		try {
			
			for (int i = 0; i < totalDevicesList.size(); i++) {
				
//			    
//				if(vs>15 && vs<21){
//				    state = manager.getVolumeState(totalDevicesList.get(i));
//					if ( state.equals(Environment.MEDIA_MOUNTED) && testDevice(totalDevicesList.get(i))) {
//						
//						mountedDevices.add(totalDevicesList.get(i));
//					}
//				}else{
//					
//					//if (testDevice(totalDevicesList.get(i))) {
//						
//						mountedDevices.add(totalDevicesList.get(i));
//					//}
//				}
			}
		} catch (Exception rex) {
		}
		return mountedDevices;
	}
    public boolean isMountedPath(String path){
    	boolean ret=false;
//    	if(path==null || path.equals("")) return ret;
//    	String state=manager.getVolumeState(path);
//    	if (state.equals(Environment.MEDIA_MOUNTED) && testDevice(path)){
//    		return true;
//    	}
//    	
    	return ret;
    }
	public String getMountedPath(String path){
		
		String state;

		try {
			for (int i = 0; i < totalDevicesList.size(); i++) {
		
				if ( path.contains(totalDevicesList.get(i)) && !totalDevicesList.get(i).equals(Environment.getExternalStorageDirectory().getPath())  ) {
					return totalDevicesList.get(i);
				}
			}
		} catch (Exception rex) {
			
		}
		return null;
	}
	public boolean isInterStoragePath(String path) {
		if (internalDevicesList.contains(path)) {
			return true;
		}
		return false;
	}

	public boolean isSdStoragePath(String path) {
		if (sdDevicesList.contains(path)) {
			return true;
		}
		return false;
	}

	public boolean isUsbStoragePath(String path) {
		if (usbDevicesList.contains(path)) {
			return true;
		}
		return false;
	}

	public boolean isSataStoragePath(String path) {
		if (sataDevicesList.contains(path)) {
			return true;
		}
		return false;
	}



	public ArrayList<String> getSataDevicesList() {
		return (ArrayList<String>) sataDevicesList.clone();
	}

	public boolean hasMultiplePartition(String dPath) {
		try {
			File file = new File(dPath);
			String minor = null;
			String major = null;
			for (int i = 0; i < totalDevicesList.size(); i++) {
				if (dPath.equals(totalDevicesList.get(i))) {
					String[] list = file.list();
					for (int j = 0; j < list.length; j++) {
	
						int lst = list[j].lastIndexOf("_");
						if (lst != -1 && lst != (list[j].length() - 1)) {
							major = list[j].substring(0, lst);
							minor = list[j].substring(lst + 1, list[j].length());
							try {

								Integer.valueOf(major);
								Integer.valueOf(minor);
							} catch (NumberFormatException e) {
								
								return false;
							}
						} else {
							return false;
						}
					}
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			Log.e(TAG, "hasMultiplePartition() exception e");
			return false;
		}
	}

	/* 获取全部空间,..GB */
	public static long getTotalSize(String path) {
		StatFs statfs = new StatFs(path);
		long totalBlocks = statfs.getBlockCount();
		long blockSize = statfs.getBlockSize();
		long totalsize = blockSize * totalBlocks;
		return totalsize;
	}

	/* 获取可用空间 */
	public static long getAvailableSize(String path) {
		StatFs statfs = new StatFs(path);
		long blockSize = statfs.getBlockSize();
		long availBlocks = statfs.getAvailableBlocks();
		long availsize = blockSize * availBlocks;
		return availsize;
	}

	// 系统函数，字符串转换 long -String (kb)
	public static String formateFileSize(Context context, long size) {
		return Formatter.formatFileSize(context, size);
	}

	public void MapPartitionName(String devices) {//, SharePreferenceUtil spu
		/* 映射其分区名,要区分插拔时卸载失败导致的假分区 */
		File f = new File(devices);
		String partition;
		File[] list = f.listFiles();

		int j = 0;
		for (int i = 0; i < list.length; i++) {
			// partition = list[i].substring(list[i].lastIndexOf("/") + 1);
			partition = list[i].getAbsolutePath();
			try {
				Log.d("chen", "partition:" + partition + "   start--------");
				StatFs statFs = new StatFs(partition);
				Log.d("chen", "----------------------end");
				if (statFs.getBlockCount() == 0) {
					continue;
				}
			} catch (Exception e) {
				continue;
			}
			//if (j < map.length) {
				//spu.saveSharedPreferences(partition, map[j]);
			//	j++;
			//} else {
			//	spu.saveSharedPreferences(partition, mContext.getResources().getString(R.string.partitionOther));
			//}
		}

	}
	
	   public static boolean testDevice(String path){
		    
		    if(path==null || !new File(path).exists()) return false;
		    boolean flag=true;
//		    
//		    File dbFile = new File(path+"/test");
//            if(!dbFile.exists()){
//               try {                    
//                  flag = dbFile.createNewFile();
//               } catch (IOException e) {
//                  flag=false;
//                  e.printStackTrace();
//               }
//             }
          return flag;
	   }
	   
	public String getSDCardPathEx(){
		

			   totalDevicesList.clear();
			   totalDevicesList.add(Utils.storage_card);
			   
			   File f=new File("/storage");
			   
			   if(!f.exists()) return null;
			   File[] files = f.listFiles();
			   if(files==null) return null;
			     

			  for (int i = 0; i < files.length; i++) {
						File file = files[i];
						if(file.getName().charAt(0)=='.' || file.isFile()) continue;
						if(file.isDirectory() && testDevice(file.getPath()) && !file.getPath().equals("/storage/sdcard0") ){
			   
							   int index=file.getName().indexOf("sdcard");
							   if(index==0){
								   Log.e("Mount", "MountedPath ...."+file.getPath());
								   totalDevicesList.add(file.getPath());
							   }
							   index=file.getName().indexOf("udisk");
							   if(index==0){
								   Log.e("Mount", "MountedPath ...."+file.getPath());
								   totalDevicesList.add(file.getPath());
							   }
							   
							
						}
			  }

			  return Environment.getExternalStorageDirectory().getPath();
	   }	
}
