package com.myun.core;




import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.myun.utils.MusicFile;
import com.myun.utils.Utils;
import com.myun.core.MusicPlayer.MusicPlayerListener;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;






public class AppDownload extends Thread {

    public final String TAG="AppDownload";
	private Context mContext;
	public BlockingQueue<Map<String, String>> Asyn_bq=null;
    public File mFile=null;
	public String id=null, url=null;
	public int state=0;
	public boolean end=false;
	
	public AppDownload(Context context, BlockingQueue<Map<String, String>> bq) {

		this.mContext=context;
		this.Asyn_bq=bq;
		 
	}


	@Override
	public void run() {
		Utils.log(TAG+" Thread Start .......");
		while(true){
			
		
				   
			    try {
					Map<String, String> map=Asyn_bq.take();
					state=0;
					id=map.get("id");
					url=map.get("url");
					downloadapp(url);
					
					
					
					
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			
			
			
		}
		

	}



	synchronized public String downloadapp(String url) {
		
		Log.d(TAG, "HTTP url:" + url);
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			
			
			state=-1;			
			Log.d(TAG, "sdcard not exists: " + state);
			return null;
		}

		BufferedInputStream bufferedInputStream = null;
		FileOutputStream fileOutputStream = null;
		URLConnection connection = null;
		File file = null;
		
		int len = 0;
		String path = "";
		byte[] buffer = null;
		int totalLength;
		int wroteLength;
		int value;
		String outFile;
		PackageInfo info=null;		
        state=0;

		try {
			

		    
			connection = new URL(url).openConnection();
			connection.setConnectTimeout(60*1000);
			connection.setReadTimeout(60*1000);
			connection.connect();
			
			
			outFile = Utils.music_app_path + url.substring(url.lastIndexOf('/'));
			
			totalLength = connection.getContentLength();
			Log.d(TAG, "HTTP header/ContentLength:" + totalLength);

			if(getSDCardAvailable()<totalLength){
				
			
				state=-1;
				Log.d(TAG, "sdcard is full .....: " + state);
				return null;
			}
			
			buffer = new byte[2048];
//			path = outFile.substring(0, outFile.lastIndexOf("/"));
//
//			file = new File(path);
//			if (!file.exists()) {
//				boolean result = file.mkdirs();
//				Log.d(TAG, "Create directory " + path + " return " + result);                
//			}
						
			file = new File(outFile);
			if (!file.exists()) {
				file.createNewFile();
			}			

			bufferedInputStream = new BufferedInputStream(connection.getInputStream());			
			fileOutputStream = new FileOutputStream(outFile);		
			
			wroteLength = 0;
		
			
			while ((len = bufferedInputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, len);
				wroteLength += len;
				value = (int)((float)wroteLength / totalLength * 100) ;
				state = (value >= 100) ? 99 : value;	
				//Log.d(TAG, "download percent:" + state);
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			bufferedInputStream.close();
			
			
			info = mContext.getPackageManager().getPackageArchiveInfo(outFile, android.content.pm.PackageManager.GET_ACTIVITIES);
			Log.i("TAG", info.packageName+" installApk start"+outFile);				
			
			String ret="";
			if(installapplistener!=null){
				ret=installapplistener.installApp(outFile, info.packageName);
			}
			Log.d(TAG, "download APK finished! "+ret);
			
			
			
			state=100;

            
			Thread.sleep(2000);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.e(TAG, "download: error!");
			state=-1;
		}
		return info==null?null:info.packageName;
	}

    public String getDownloadStatus(){
    	
    	   if( id==null || id.equals("") ) return "-2:null";
    	   return  state+":"+id;
    	
    }
	private long getSDCardAvailable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();
			return availCount * blockSize;
		}
		return -1;
	}


    public InstallApplistener installapplistener=null;
	public void setInstallApplistener(InstallApplistener anstallapplistener) {
		this.installapplistener = anstallapplistener;
	}

	public interface InstallApplistener {
    	 public String installApp(String path, String packagename);
    }
}


