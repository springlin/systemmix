package com.myun.core;



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
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.myun.utils.MusicFile;
import com.myun.utils.Utils;
import com.myun.core.MusicPlayer.MusicPlayerListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;





public class MusicSynch extends Thread {

	private final static int ID_NOTIFICATION = 0x123;
	private int mFileSize;
	private int mDownLoadedSize;

	private long mAvailableSpace;
	private MusicService mContext;
	public BlockingQueue<MusicFile> Asyn_bq=null;
//	public LinkedList<MusicFile> loadlist=null;
	public MusicFile loading_mf=null;
    public File mFile=null;
    public MusicSynchListener musicsynchlistener=null;
    
	public MusicSynch(MusicService context, BlockingQueue<MusicFile> bq) {//, 

		this.mContext=context;
		this.Asyn_bq=bq;
		//this.loadlist=loadlist;
	}



	static final int FILE_CAN_NOT_CREATE = 1;
	static final int NET_ERROR = 2;
	static final int NOT_ENOUGH_SPACE = 3;



	public void onCancel() {
		
		
	}
    public void mPause(){
    	mStop=true;
    	Utils.log("Synch Music Status :"+mStop);
    }
    public void mStart(){
    	
    	mStop=false;
    	Utils.log("Synch Music Status :"+mStop);
    }
	@Override
	public void run() {
		Utils.log("MusicSynch Thread Start .......");
		File newFile=null;
		while(true){
			
			   try{
				   
				    while(mStop) { sleep(1000); Utils.log("MusicSync ...Sleep");}
				    loading_mf=null;
			    	loading_mf=Asyn_bq.take();
			    	//loadlist.remove(mf);
			    	if(!loading_mf.isValid()) { Utils.log("valid "+loading_mf.getName()); continue;}
			    	
			    	String mSavePath=Utils.storage_card+"/music_service/device/"+loading_mf.getDevicename();
			    	mAvailableSpace = getSDCardAvailable();
			    	
			    	//Utils.log("V:"+ Utils.getSDCardAvailable()*1024+"C:"+Utils.Capacity);
			    	if(  Utils.getSDCardAvailable() < Utils.Capacity  ){
			    		
			    		  if(mContext.sd.isMountedPath(Utils.extern_sdcard_path)){
			    		  
				    		  mAvailableSpace = Utils.getSDCardAvailable(Utils.extern_sdcard_path);
				    		  mSavePath=Utils.extern_sdcard_path+"/music_service/device/"+loading_mf.getDevicename();
				    		  
				    		  
			    		  }else{
			    			  if( !mContext.RunStatus.contains("full") ) mContext.RunStatus+="full;;";
			    			  Utils.log("±¾µØ´æ´¢¿¨ÒÑÂú");
			    			  continue;
			    		  }
			    		  
			    		  
			    	}

			    	newFile = new File(mSavePath);
			        if (!newFile.exists()) {
				         boolean isCreate = newFile.mkdirs();
				         if (!isCreate) {					      
					         continue;
				         }
				         

			       }
	
					mSavePath+=Utils.getPathToFolderFolder(loading_mf.getPath());
					newFile = new File(mSavePath);
			        if (!newFile.exists()) {
				         if (!newFile.mkdirs()) {
					         continue;
				         }
			        }
			       Utils.log("mSavePath ..."+mSavePath);
				  
				   int ret=downFile(loading_mf, mSavePath);
				   if( ret==-1 ){
					   if(mFile!=null && mFile.exists()) { 
						   mFile.delete(); mFile=null;
					   }
				   }else if(ret==1){
					   
					   Utils.log("Success download "+loading_mf.getName());
					   if(musicsynchlistener!=null) musicsynchlistener.setMusicSynchStatus(mFile.getPath());
					   
				   }else{
					   Utils.log("Exists "+loading_mf.getName());
				   }
				   
				   
				} catch (InterruptedException e) {
					if(mFile!=null && mFile.exists()) { mFile.delete(); mFile=null;}
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					if(mFile!=null && mFile.exists()) { mFile.delete(); mFile=null;}
				} catch (IOException e) {
					if(mFile!=null && mFile.exists()) { mFile.delete(); mFile=null;}
					//cancelDownload();
					e.printStackTrace();
				}catch(ConcurrentModificationException e){
					
				}
			
			
			
		}
		

	}



	private int downFile(MusicFile mf, String path) throws IOException, FileNotFoundException {

		
		String filename = mf.getName();

		File file = new File(path +"/"+filename);
		
		mFile=file;
		
		if (file.exists() && file.length()>0) {
			Utils.log("file download "+ file.getName() +"file.length: "+file.length());
			return 0;
		}
	

		String _path=mf.getPath();
		Utils.log("_path "+_path);
        try {
    			_path = URLEncoder.encode(_path,"utf-8").replace("%2F", "/");
    	} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    	}
		
        
        
		URL myURL = new URL("http://"+mf.getHost()+":"+Utils.HttpPort+_path+"?load=true");
		Utils.log("myURL  "+myURL.toString()+"\n");
		URLConnection conn = myURL.openConnection();
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
		conn.connect();
		InputStream is = conn.getInputStream();
		mFileSize = conn.getContentLength();
		
		
		
		if (mAvailableSpace < mFileSize) {
			return -1;
		}
		if (is == null) {
			return -1;
		}		


		FileOutputStream fos = new FileOutputStream(file);
		byte buf[] = new byte[4096];
		mDownLoadedSize = 0;
	//	mHandler.sendEmptyMessage(0);
		
		do {
			
			int numread = is.read(buf);
			if (-1 == numread) {
				break;
			}
			fos.write(buf, 0, numread);
			mDownLoadedSize += numread;


		} while (!mStop);
		

		try {
			is.close();		
			fos.close();
			
		} catch (IOException e) {
		}
		
		
		if (mFileSize == mDownLoadedSize) {
			return 1;
		}else{
			return -1;
		}
			
	}

	private boolean mStop = false;

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

	public void cancelDownload() {
		mStop = true;
//		mHandler.removeCallbacksAndMessages(null);
//		if (mFile != null && mFile.exists()) {
//			mFile.deleteOnExit();
//		}
		
		//mHandler.sendEmptyMessage(-1);
	}

	public String getSynchStatus(){
		  String status="";
		  
		  if(loading_mf!=null)
			  status="synch="+Utils.getPathToFolderandFile(loading_mf.getPath())+"\n";
		
		  return status;
	}

	private long getSystemAvailable() {
		File root = Environment.getDataDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long availCount = sf.getAvailableBlocks();
		return availCount * blockSize;
	}
	
    public MusicSynchListener getMusicSynchlistener() {
		return musicsynchlistener;
	}
	public void setMusicsynchlistener(MusicSynchListener musicsynchlistener) {
		this.musicsynchlistener = musicsynchlistener;
	}

	public interface MusicSynchListener {
    	 public void setMusicSynchStatus(String status);
    }
}

