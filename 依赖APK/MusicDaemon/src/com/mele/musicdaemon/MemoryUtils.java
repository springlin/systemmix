package com.mele.musicdaemon;

import java.util.List;
import java.io.FileReader;

import android.os.Process;

import java.io.IOException;
import java.io.BufferedReader;

import android.util.Log;
import android.widget.EditText;
import android.content.Context;

import java.io.InputStreamReader;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;

/**
 * 文档描述:
 * 内存工具类
 * 
 * 包括内容:
 * 1 内存清理即杀掉进程的几种方式
 * 2 获取内存总大小及其可用大小
 * 3 判断栈顶Activity名及其所属进程名
 * 
 * 注意权限:
 * <uses-permission android:name="android.permission.GET_TASKS"/>  
 * <uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />
 * 
 * 文档日期:
 * 2014年5月30日10:01:55
 *
 */
public class MemoryUtils {
	
	public static String system_process[]={"com.mele.launcher","com.mele.musicdaemon.RemoteService","com.mele.dlna.service","system_process",
			                               "com.mele.airplay.service","com.farcore.AutoSuspend","com.amlogic.inputmethod.remote","com.mele.core",
			                               "system","com.android.systemui","android.process.media","com.android.musicfx","com.js.litchi",
			                               "com.android.onetimeinitializer","com.android.service.remotecontrol","com.amlogic.SubTitleService",
			                               "com.android.defcontainer","com.android.phone","com.tencent.qqpinyin","com.svox.pico","com.mele.ledbreath",
			                               "com.mele.autoconnectservice","com.android.smspush","com.android.printspooler","com.android.onetimeinitializer",
			                               "com.mele.factory","com.android.settings","com.mele.melesystemupdate","com.android.keychain", "com.tencent.qqpinyin.core", "com.tencent.qqpinyin.service"};
	                                    
	//----------> 以下为杀掉进程的几种方式

	
     private  static List<ApplicationInfo> allAppList=null;
	   
     public static void InitMemoryUtils(Context context) {
	        // 通过包管理器，检索所有的应用程序（包括卸载）与数据目录
	        PackageManager pm = context.getApplicationContext().getPackageManager();
	        allAppList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
	        pm.getInstalledPackages(0);
	  }
	   
	   
	  public static ApplicationInfo getApplicationInfo(String appName) {
	        if (appName == null) {
	            return null;
	        }
	        for (ApplicationInfo appinfo : allAppList) {
	        	
	        	
	            if (appName.equals(appinfo.processName)){
	            	
	                return appinfo;
	            }
	           Log.e("", "MM++++++"+appinfo.processName);  
	        }
	        return null;
     }	
	
	public static void cleanMemory(Context context, String unkillpackage) {
		
		RemoteService rs=(RemoteService)context;
		long beforeCleanMemory=getAvailMemory(context);
		System.out.println("---> 清理前可用内存大小:"+beforeCleanMemory+"M");
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		RunningAppProcessInfo runningAppProcessInfo = null;
		List<RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
		for (int i = 0; i < runningAppProcessInfoList.size(); ++i) {
			runningAppProcessInfo = runningAppProcessInfoList.get(i);
			String processName = runningAppProcessInfo.processName;
			//调用杀掉进程的方法
			

			//System.out.println("new ===:"+processName);
			
			//if(runningAppProcessInfo.importance>ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE){
			if(!is_system_need(processName) ){
				    
				  //  if( unkillpackage!=null && processName.equals(arg0)) continue;
					//killProcessBykillProcess(runningAppProcessInfo.pid);
					//killProcessByRestartPackage(context, processName);
					System.out.println("-------> 开始清理:"+processName);
					
					 String apk[]=runningAppProcessInfo.pkgList;
			         for (int j = 0; j < apk.length; j++) {  
			                String pkgName = apk[j];
			                rs.killApp(pkgName);  
			                Log.i("", "packageName " + pkgName + " at index " + j);  

			        }  
					
			}
		    //}
			// killProcessByRestartPackage(context, processName);
		}
		long afterCleanMemory=getAvailMemory(context);
		System.out.println("---> 清理后可用内存大小:"+afterCleanMemory+"M");
		System.out.println("---> 节约内存大小:"+(afterCleanMemory-beforeCleanMemory)+"M");
		System.out.println("共清理:"+(afterCleanMemory-beforeCleanMemory)+"M");
	}
	public static boolean checkPackage(String processName, String unkillpackage){
		   if(allAppList==null) return false;

		   ApplicationInfo ai=getApplicationInfo(processName);
		   if(ai!=null) System.out.println(unkillpackage+"---gg----:"+ai.packageName);
		   if(ai!=null && ai.packageName.equals(unkillpackage)){
			   return true;
		   }

		  return false;
	}
	
	public static boolean is_system_need(String app){
		
		 for(int i=0; i<system_process.length; i++){
			   if(system_process[i].equals(app)){
				   
				   return true;
				   
			   }
			 
			 
			 
		 }
		 return false;
		
	}
	//利用activityManager.restartPackage()方法杀死进程
	//该方法实际调用了activityManager.killBackgroundProcesses()方法
	public static void killProcessByRestartPackage(Context context,String packageName) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.restartPackage(packageName);
		System.gc();
	}
	
	
	//利用Process.killProcess(pid)杀死进程
	//注意事项:
	//1 该方式可自杀,即杀掉本进程
	//2 该方式可杀掉其他普通应用进程
	//3 该方式不可杀掉系统级应用即system/app应用
	public static void killProcessBykillProcess(int pid){
		Process.killProcess(pid);
	}
	
	
	//利用adb shell命令杀死进程
	public static void killProcessByAdbShell(int pid) {
		String cmd = "adb shell kill -9 " + pid;
		System.out.println("-------> cmd=" + cmd);
		try {
			java.lang.Process process = Runtime.getRuntime().exec(cmd);
			InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println("----> exec shell:" + line);
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 
	 //利用su进程的命令方式杀死进程
	 //1 得到su进程(super进程)
	 //  Runtime.getRuntime().exec("su");
	 //2 利用su进程执行命令
	 //  process.getOutputStream().write(cmd.getBytes());
	public static void killProcessBySu(int pid) {
		try {
			java.lang.Process process = Runtime.getRuntime().exec("su");
			String cmd = "kill -9 " + pid;
			System.out.println("-------> cmd = " + cmd);
			process.getOutputStream().write(cmd.getBytes());
			if ((process.waitFor() != 0)) {
				System.out.println("-------> su.waitFor()!= 0");
			} else {
				System.out.println("------->  su.waitFor()==0 ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	//----------> 以上为杀掉进程的几种方式
	

	
	
	
	//获取当前进程名
	public static String getCurrentProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()) {
			if (runningAppProcessInfo.pid == pid) {
                String processName=runningAppProcessInfo.processName;
				return processName;
			}
		}
		return null;
	}
	
	
	//获取栈顶Activity名称
	public static String getTopActivityName(Context context) {
		String topActivityName = null;
		ActivityManager activityManager = (ActivityManager) (context.getSystemService(android.content.Context.ACTIVITY_SERVICE));
		List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
		if (runningTaskInfos != null) {
			ComponentName f = runningTaskInfos.get(0).topActivity;
			String topActivityClassName = f.getClassName();
			String temp[] = topActivityClassName.split("\\.");
			topActivityName = temp[temp.length - 1];
		}
		return topActivityName;
	}
	
	
	
	//获取栈顶Activity所属进程的名称
	public static String getTopActivityProcessName(Context context) {
		String processName = null;
		ActivityManager activityManager = (ActivityManager) (context.getSystemService(android.content.Context.ACTIVITY_SERVICE));
		List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
		if (runningTaskInfos != null) {
			ComponentName componentName = runningTaskInfos.get(0).topActivity;
			String topActivityClassName = componentName.getClassName();
			int index = topActivityClassName.lastIndexOf(".");
			processName = topActivityClassName.substring(0, index);
		}
		return processName;
	}
	
	
	
	//获取内存总大小
	public static long getTotalMemory() {
		// 系统的内存信息文件
		String filePath = "/proc/meminfo";
		String lineString;
		String[] stringArray;
		long totalMemory = 0;
		try {
			FileReader fileReader = new FileReader(filePath);
			BufferedReader bufferedReader = new BufferedReader(fileReader,1024 * 8);
			// 读取meminfo第一行,获取系统总内存大小
			lineString = bufferedReader.readLine();
			// 按照空格拆分
			stringArray = lineString.split("\\s+");
			// 获得系统总内存,单位KB
			totalMemory = Integer.valueOf(stringArray[1]).intValue();
			bufferedReader.close();
		} catch (IOException e) {
		}
		return totalMemory / 1024;
	}
	
	
	
	//获取可用内存大小
	public static long getAvailMemory(Context context) {
		ActivityManager activityManager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo memoryInfo = new MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		return memoryInfo.availMem / (1024 * 1024);
	}
	

}
