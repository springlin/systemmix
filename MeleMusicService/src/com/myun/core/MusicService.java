package com.myun.core;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;






































import com.myun.spring.airplay.NscreenAirplayService;
import com.myun.spring.dlna.NscreenDMRService;
import com.myun.utils.Semaphore;





import com.mele.musicdaemon.RemoteServiceAIDL;
import com.myun.net.protocol.IPeerListener;
import com.myun.net.protocol.Peer;
import com.myun.net.protocol.PeerMessage;
import com.myun.net.protocol.PeerServerIP;
import com.myun.net.protocol.PeerTools;
import com.myun.utils.KeyUtil;
import com.myun.utils.MusicFile;
import com.myun.utils.MusicForm;
import com.myun.utils.NanoHTTPD;
import com.myun.utils.PlaySoundPool;
import com.myun.utils.StorageDevices;
import com.myun.utils.TimeClock;
import com.myun.utils.Utils;
























import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;



import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;




public class MusicService extends Service {

	public final String TAG="MusicService";
	public final int videoport=29060, cmdport=29061;
	private Peer peer;	
	public  String Device="";
	public  StorageDevices sd=null;
    public  LinkedList<MusicFile> musiclist=new LinkedList<MusicFile>();
    public  LinkedList<MusicFile> sdcardlist=new LinkedList<MusicFile>();
    public  BlockingQueue<String> bq=new LinkedBlockingQueue<String>();
    public  LinkedList<MusicFile> clientlist=new LinkedList<MusicFile>();
	public  static MusicService instance=null;
    private LinkedList<String> templist=new LinkedList<String>();
    private BlockingQueue<MusicFile> Asyn_bq=new LinkedBlockingQueue<MusicFile>();
    private BlockingQueue<Map<String, String>> appdownload_bq=new LinkedBlockingQueue<Map<String, String>>();
    private MusicSynch musicsynch=null;
    public  MusicPlayer musicplayer=null;
    public  AppDownload appdownload=null;
    public  WifiAdmin wifiadmin=null;
    
    private String ClientDevice=null, ClientIP=null; 
    public  String RunStatus="";
    public  BlockingQueue<String> playlist=new LinkedBlockingQueue<String>();

    static  boolean ischeck_sd=false, is_sdcard_play=false, is_cancel_search=false;
    public  String playmode="", playing_path="";
    public  List<String> pre_playlist=Collections.synchronizedList(new LinkedList<String>());
    public  LinkedList<String> randomlist=new LinkedList<String>();
    public  LinkedList<MusicForm> musicformlist= new LinkedList<MusicForm>();
	public  Semaphore semaphore=new Semaphore();
	public  boolean ismirror=false;
    public  long time_start=0, MIndex=0;
    public  static int vol=0x50;
    public  String mirror_package=null;
    public  PlaySoundPool  playSoundPool=null;
    public  static int volmic1=0, volmic2=0, vol3=0, vol4=0, inputmode=0, vol5=16;
    public  int pt2258_channel1_vol[] = {79, 19, 18, 17, 16, 15, 14, 13, 12, 11, 
                                         10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};//21
    public  int pt2258_channel2_vol[] = {79, 70, 50, 30, 16, 15, 14, 13, 12, 11, 
    	                                 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};//21
    public  int pt2258_channel3_vol[] = {9,7,5 ,3, 0}; //5 
    public  int pt2258_channel4_vol[] = {79, 18, 11,10,9,8,7,6,5,3,2, 1, 0};//12
    public  int pt2258_channel5_vol[] = {0,4,6,8,9,10,11,12,13,14,15,16,29};//13
    
	@Override
	public void onCreate() {		
		super.onCreate();
		

		playSoundPool=new PlaySoundPool(this);
		loadTipSound();
		
	    
        Utils.getInstance(this, handler);
        Utils.rwAudioIO("0");
		peer = new PeerServerIP(this, "DEVICE");
		peer.setListener(ipeerlistener);
		sd=new StorageDevices(this);
		createBroadcastReceiver();
		
				
		setBootTimerClock();
		searchMusicRes();

		instance=this;
		musicsynch=new MusicSynch(this, Asyn_bq);//, clientlist
		musicsynch.start();
		musicsynch.setMusicsynchlistener(SynListener);
	
		onLoadPlayer();

		appdownload=new AppDownload(this, appdownload_bq);
		appdownload.setInstallApplistener(installapplistener);
		appdownload.start();		
		
		
		wifiadmin=new WifiAdmin(this);
		//wifiadmin.start();

		playmode=Utils.getVar("playmode");
		if(playmode.equals("0")){		   
		   playmode="cycling";
		   Utils.setVar("playmode",playmode);
		}
		vol=Utils.getVarInt("vol");
		if(vol==-1){
			vol=0x20;
			Utils.setVar("vol", vol);
		}
		//vol=0x20;
		
		volmic1=Utils.getVarInt("volmic1");
		if(volmic1==-1){
			volmic1=0;
			Utils.setVar("volmic1", volmic1);
		}
		volmic2=Utils.getVarInt("volmic2");
		if(volmic2==-1){
			volmic2=0;
			Utils.setVar("volmic2", volmic2);
		}
		vol3=Utils.getVarInt("vol3");
		if(vol3==-1){
			vol3=0;
			Utils.setVar("vol3", vol3);
		}
		vol4=Utils.getVarInt("vol4");
		if(vol4==-1){
			vol4=0;
			Utils.setVar("vol4", vol4);
		}
		vol5=Utils.getVarInt("vol5");
		if(vol5==-1){
			vol5=16;
			Utils.setVar("vol5", vol5);
		}
		if (NanoHTTPD.getInstance() == null) { 
			try {
			
				NanoHTTPD.startServer(Peer.HTTP_PORT+1, new File("/"));
			} catch (IOException e) {
				e.printStackTrace();
				

			}
        }		
		
		
		Intent intent  = new Intent("com.mele.musicdaemon.RemoteService");
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
		

	    
		Intent service2 = new Intent(this, NscreenDMRService.class);
		service2.putExtra("UPNP_PARAMETER", NscreenDMRService.UPNP_CREATE);
		startService(service2);
		
		
		startService(new Intent(this, NscreenAirplayService.class));
		
		//PeerServerIP.initS805Vol();
	    //handler.removeMessages(Utils.Cmd_Sleep);
	    Message msg=handler.obtainMessage(Utils.Cmd_Sleep, 1, 0);
	    handler.sendMessageDelayed(msg, 5000);
	    
	    
	    Log.i("CheckLib", "Build.MODEL: "+ Build.MODEL+"  Build.VERSION: "+Build.VERSION.SDK_INT);
	    Log.i(TAG, "CoreService Start ...............playmode"+playmode+"vol "+vol);
	}
	public void onLoadPlayer(){
		log("onload muiscplayer");
	    new Thread(new Runnable() {
  			@Override
  			public void run() {   
  				
						musicplayer=new MusicPlayer(MusicService.this, playlist/*, pre_playlist*/);		
						musicplayer.setMusicplayerlistener(MPListener);
						musicplayer.start();
						int count=0;
						
		  				while(true){

		  					try {
								Thread.sleep(60000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
		  					Utils.log("musicplayer playlist size:"+playlist.size());
		  					if(playlist.size()>0){
		  						
		  						  if(playlist.size()!=count){
		  							   count=playlist.size();
		  						  }else{
		  							   
		  							   Log.e(TAG,"MusicPlayer ReStart ...............!!!");
		  							   musicplayer.onDestroy();
		  							   musicplayer=null;
		  							   count=0;
		  							   System.exit(0);
		  							   musicplayer=new MusicPlayer(MusicService.this, playlist/*, pre_playlist*/);		
		  							   musicplayer.setMusicplayerlistener(MPListener);
		  							   musicplayer.start();

		  						  }
		  						  
		  						  
		  					    
		  					}else{
		  						count=0;
		  					}
		  					
		  				}
  				
  			}
	     }).start();
	}
	public String getSpeakerStatus(){//获取音响状态
		
		   if(!RunStatus.contains("mirrorapk")) RunStatus+=getMirrorStatus(mirror_package);
		
		   String status="";
		   status+="devname="+PeerMessage.NID_FLAG + PeerTools.getNID(this).replaceAll(":", "").substring(6)+"\n";
		   status+="sdcard="+Utils.getSDCardAvailable()+"/"+Utils.getSDCardSize(Utils.storage_card)+"\n";
		   if(sd.isMountedPath(Utils.extern_sdcard_path)) 
			   status+="extern_sdcard="+Utils.getSDCardAvailable(Utils.extern_sdcard_path)+"/"+Utils.getSDCardSize(Utils.extern_sdcard_path)+"\n";
		   status+="playmode="+playmode+"\n";
		   status+="volume="+PeerServerIP.getRemoteVolume()+"\n";
		  // status+=getMirrorStatus(mirror_package);
		   status+="\n\n";
		   return status;
	}
	
	public String getMirrorStatus(String packagename){
		   String ret="mirrorapk=";
		   if(packagename==null || packagename.equals("")) return ret+=";;";
		   
	       try {    
                ApplicationInfo info = getPackageManager().getApplicationInfo(packagename, 0);     
                ret+=info.loadLabel(getPackageManager()).toString()+"/"+packagename+";;";    
           } catch (NameNotFoundException e) {     
                e.printStackTrace();
                ret+=";;";
                mirror_package=null;
           }   	       
		   return ret;
	}
	public void setBootTimerClock(){
		
		 log("Reset AlarmClock");
		 LinkedList<TimeClock> list=Utils.sqlitehelper.queryAllClockTime(Utils.sqlitehelper.sdcard_db, null);
   	 
		 for(int i=0; i<list.size(); i++){
			 this.setTimerClock(list.get(i));
		 }
		 
	}
	
    public void showmsg(final String msg){
    	Utils.log("====>"+msg);
        handler.post(new Runnable(){  
            public void run(){  
                if(toast!=null) toast.cancel();
                toast=Toast.makeText(MusicService.this.getApplicationContext(), msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
    public Toast toast=null;
	public Dialog dialog=null;
	public SeekBar seekmic1=null, seekmic2=null, seek3=null, seek4=null, seek5=null, seek6=null;
	public void onCaraOKUIDg(boolean flag){
		if(dialog==null){
			dialog = new Dialog(this.getApplicationContext(), R.style.dialog);
			dialog.setContentView(R.layout.caraok_layout);
	        Window window = dialog.getWindow();
	        window.setWindowAnimations(R.style.child_popwin_anim_style);
	 	    View main_v=dialog.getWindow().findViewById(R.id.mainlayout);
	 	    FrameLayout.LayoutParams param=(FrameLayout.LayoutParams)main_v.getLayoutParams();
	 	    param.width=1100;
	 	    param.height=640;  
	        main_v.setLayoutParams(param); 
	        
	        
	        seekmic1=(SeekBar)main_v.findViewById(R.id.ctrl_bar1);
	        seekmic2=(SeekBar)main_v.findViewById(R.id.ctrl_bar2);
	        seek3=(SeekBar)main_v.findViewById(R.id.ctrl_bar3);
	        seek4=(SeekBar)main_v.findViewById(R.id.ctrl_bar4);
	        seek5=(SeekBar)main_v.findViewById(R.id.ctrl_bar5);
	        seek6=(SeekBar)main_v.findViewById(R.id.ctrl_bar6);
	        
	        seekmic1.setOnSeekBarChangeListener(seekbarlistener);
	        seekmic2.setOnSeekBarChangeListener(seekbarlistener);
	        seek3.setOnSeekBarChangeListener(seekbarlistener);
	        seek4.setOnSeekBarChangeListener(seekbarlistener);
	        seek5.setOnSeekBarChangeListener(seekbarlistener);
	        seek6.setOnSeekBarChangeListener(seekbarlistener);
	        
			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			dialog.setCanceledOnTouchOutside(true);
			
			
			
		}

        
        if(!dialog.isShowing()){
            seekmic1.setProgress(volmic1);
            seekmic2.setProgress(volmic2);
            seek3.setProgress(vol3);
            seek4.setProgress(vol4);
            seek5.setProgress(vol5); 
            seek6.setProgress(PeerServerIP.getVolume());

            dialog.show();
        }else{
        	dialog.dismiss();
        }
	       
	}
	
	public SeekBar.OnSeekBarChangeListener seekbarlistener=new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekbar, int progress,
				boolean fromUser) {
			
			 int value=seekbar.getProgress()-1;
			 if(value<0) value=0;
		     if(seekbar==seekmic1 ) {
		    	 
		    	 value=pt2258_channel1_vol[value];
		    	 
		     }else if(seekbar==seekmic2 ) {
		    	 
		    	 value=pt2258_channel2_vol[value];
		    	 
		     }else if(seekbar==seek3 ) {
		    	 
		    	 value=pt2258_channel4_vol[value];

		    	 
		     }else if(seekbar==seek4 ) {
		    	 
		    	 value=pt2258_channel5_vol[value];
		    	 
		     }
		     if(value<0) value=0; 
		     int buf10[]={value/10}, buf1[]={value%10};
		     Log.i("mic", seekbar.getProgress()+" value====>"+value+" 10Y="+buf10[0]+"  1Y="+buf1[0]);
		    
		     if( remoteservice ==null ) return ;
		     if(seekbar==seekmic1){
		    	    
					volmic1=seekbar.getProgress();
					try {
						buf1[0]=0x50|buf1[0];
					    MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x40|buf10[0], buf1, 1);
						//MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x50|buf1[0], buf1, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					Utils.setVar("volmic1", volmic1);
		    	    showmsg(""+seekbar.getProgress());
		     }else 	if(seekbar==seekmic2){
		    	    
		    	    volmic2=seekbar.getProgress();
					//int value=seekbar.getProgress()-1, buf10[]={value/10}, buf1[]={value%10};
					try {
						buf1[0]=0x90|buf1[0];
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x80|buf10[0], buf1, 1);
					//	MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x90|buf1[0], buf1, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					Utils.setVar("volmic2", volmic2);
					showmsg(""+seekbar.getProgress());
		     }else 	if(seekbar==seek3){
		    	    
		    	    vol3=seekbar.getProgress();
					int value3=seekbar.getProgress()-1;
					if(value3<0) value3=0;
					value3=0;
					int buf310[]={value3/10}, buf31[]={value3%10};
					Utils.log("pt2258_channel3_vol "+value3);
					try {
						buf31[0]=0x10|buf31[0];
					    MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x00|buf310[0], buf31, 1);
						//MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x10|buf31[0], buf31, 0);
						
					    
					    
					    buf1[0]=0x30|buf1[0];
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x20|buf10[0], buf1, 1);
						//MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x30|buf1[0], buf1, 0);
						
						
						
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					Utils.setVar("vol3", vol3);
					showmsg(""+seekbar.getProgress());
		     }else 	if(seekbar==seek4){
		    	 
		    	    vol4=seekbar.getProgress();
					//int value=seekbar.getProgress()-1, buf10[]={value/10}, buf1[]={value%10};
					try {
						buf1[0]=0x70|buf1[0];
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x60|buf10[0], buf1, 1);
						//MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x70|buf1[0], buf1, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					Utils.setVar("vol4", vol4);
					showmsg(""+seekbar.getProgress());
		     }else 	if(seekbar==seek5){
		    	 
		    	 
		    	 
				   int i=0;
				   for(i=0; i<Utils.out_Volume.length; i++){
					     
						   if(Utils.out_Volume[i]==vol){				
							   break;
						   }				
				   }
				   if(i==Utils.out_Volume.length) i=16;
				   
		    	 
		    	    value=seekbar.getProgress();
		    	    if(value-16>0)
		    	       showmsg("+"+(value-16));
		    	    else if(value-16<=0)
		    	       showmsg(""+(value-16));
		    	
		    	    	
				    value=value-16+i;
				    if(value<0) value=0;
				    else if(value>=32) value=31;
				    
				    
		    	    int buf[]={Utils.out_Volume[value]};
		    	    
		    	  
		    	    
		    	    
					log("setLowVolume index:"+value+" value:"+ buf[0]);
				
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x1a, 0x07, buf, 1);
						
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					vol5=seekbar.getProgress();
					Utils.setVar("vol5", seekbar.getProgress());
		    	    
		     }else 	if(seekbar==seek6){
		    	 
		    	     showmsg(""+seekbar.getProgress());
		    	     PeerServerIP.setVolume(seekbar.getProgress());

		     }
		     
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
                    
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekbar) {


			
			

		}
		
	};
	
	public void setCaraOK(int pos, int vol, int vol1){
		
		 int value=vol;
	     int buf10[]={value/10}, buf1[]={value%10};

	     Utils.log("setCaraOK ..."+pos+" vol"+vol);
	     if(pos==1){

				try {
				    MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x40|buf10[0], buf10, 0);
					MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x50|buf1[0], buf1, 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
	    	 
	     }else 	if(pos==2){
	    	    

				try {
					MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x80|buf10[0], buf10, 0);
					MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x90|buf1[0], buf1, 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}


	     }else 	if(pos==3){
	    	    
	    	    int buf310[]={vol1/10}, buf31[]={vol1%10};
				try {
					 MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x00|buf310[0], buf310, 0);
					 MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x10|buf31[0], buf31, 0);
					
					MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x20|buf10[0], buf10, 0);
					MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x30|buf1[0], buf1, 0);
					
				} catch (RemoteException e) {
					e.printStackTrace();
				}

	    	 
	     }else 	if(pos==4){
	    	 

				try {
					MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x60|buf10[0], buf10, 0);
					MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x70|buf1[0], buf1, 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
	    	 
	     }
		
		
	}
	
	public void loadTipSound(){
		
		 playSoundPool.loadSfx(R.raw.playlist1, 1);
		 playSoundPool.loadSfx(R.raw.playlist2, 2);
		 playSoundPool.loadSfx(R.raw.playlist3, 3);
		 playSoundPool.loadSfx(R.raw.playlist4, 4);
		 playSoundPool.loadSfx(R.raw.playlist5, 5);
		 playSoundPool.loadSfx(R.raw.sdcard, 6);
		 playSoundPool.loadSfx(R.raw.favorites, 7);
		 playSoundPool.loadSfx(R.raw.songlibrary, 8);
		 
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		 super.onStartCommand(intent, flags, startId);
		 
		if(intent!=null){
		 String act=intent.getStringExtra("action");
		 if(act!=null && act.equals("1")){
			 MusicService.instance.onCaraOKUIDg(false); 
		 }
		 
		 String mode=intent.getStringExtra("startmode");
		 if(mode!=null && mode.equals("boot")){
			  

			  // handler.sendEmptyMessage(Utils.Cmd_LED);
			   setLedMessage();
			 
		 }else if(mode!=null && mode.equals("alarm")){
			 
			 
         	String id=intent.getStringExtra("id");
         	log("Utils.SPEAKER_TIME_CLOCK....>"+id);
			LinkedList<TimeClock> list=Utils.sqlitehelper.queryAllClockTime(Utils.sqlitehelper.sdcard_db, null);
	    	 
	   		for(int i=0; i<list.size(); i++){
	   				 if(id.equals(list.get(i).getId())){
	   					 
	   					 handler.obtainMessage(Utils.Cmd_Time_Clock, list.get(i).getMuisformid()).sendToTarget();
	   					 break;
	   				 }
	   		 }
			 return START_STICKY;
		 }
		}
		 Notification notification = new Notification(R.drawable.ic_launcher, "netservice is running",System.currentTimeMillis());
		 PendingIntent pintent=PendingIntent.getService(this, 0, new Intent("com.myun.musicservice"), 0);
		 notification.setLatestEventInfo(this, "NetService","netservice is running", pintent);
		

		 startForeground(16, notification);
		 
		 Log.i(TAG,"CoreService onStartCommand ...............");
		 return START_STICKY;
	}
	public IPeerListener ipeerlistener=new IPeerListener(){


		@Override
		public String getMusicInfo() {
		
			
			StringBuilder ret=new StringBuilder();
		    ret.append(getSpeakerStatus());	
        
			
			MusicFile mfile=null;
			for(int i=0; i<musiclist.size(); i++){
				
				mfile=musiclist.get(i);
			    File f=new File(mfile.getPath());
			    if(!f.exists()){
			    	musiclist.remove(i);
			    	i--;
			    	continue;
			    }
			    
				ret.append(mfile.getPath()+"*>"+mfile.getArtist()+"*>"+mfile.getAlbum()+"*>"+mfile.getId()+"::");//+musiclist.get(i).getSd_path()+"<>"
				
			}
			log(ret.toString());
			
			if(!ischeck_sd){
	    	    new Thread(new Runnable() {
	      			@Override
	      			public void run() {   
	      				checkSDCarcMusicResource();
	      			}
	    	     }).start();
			}else{
				log("sdcard is running busy .....");
			}
			return ret.toString();
		}

		@Override
		public void newClientDevice(String ip, String id) {
			 log("=========>new Client Device ip "+ip+" id "+id);
			 ClientIP=ip;
			 ClientDevice=id;
		}

		@Override
		public int ClientMusicList(String info) {
		
			
			
			if(info==null || info.equals("")) return -1;
			if(!musicsynch.isAlive()){
				
				Utils.log("Restart .....MusicSynch Thread .....");
				musicsynch=new MusicSynch(instance, Asyn_bq);//, clientlist
				musicsynch.start();
				musicsynch.setMusicsynchlistener(SynListener);
			}
//	    	if(Utils.getSDCardAvailable()<Utils.Capacity && !sd.isMountedPath(Utils.extern_sdcard_path)){
//	    		
//	    	     if( !mContext.RunStatus.contains("full") ) mContext.RunStatus+="full;;";
//	    		  return (int) Utils.getSDCardAvailable();
//	    	}
			String musiclist[]=info.split("::");
			log("ClientMusicList...."+info);
			if(musiclist==null || musiclist.length< 2) return -1;
			ClientIP=musiclist[0];
			ClientDevice=musiclist[1];
			MusicFile mf=null;
			
			LinkedList<MusicFile> loadlist=new LinkedList<MusicFile>();		
			Iterator<MusicFile> iter = Asyn_bq.iterator(); 
			while(iter.hasNext()) { 
			    loadlist.add( iter.next() );
			}
			int i=0, j=0;			
			for(i=2; i<musiclist.length; i++){
				

				for(j=0; j<loadlist.size(); j++){
					if(loadlist.get(j).getPath().equals(musiclist[i])){
						 j=10000;
						 break;
					}
				}
				if(j==10000) continue;
				
				mf=Utils.getMusicObject(musiclist[i]);				
				mf.setDevicename(ClientDevice);
				mf.setHost(ClientIP);
				mf.setValid(true);
				
				
				try {
					Asyn_bq.put(mf);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
			return 0;
		}

		@Override
		public String getMusicServieRunStatus() {
			String ret=new String(RunStatus);//+getMirrorStatus(mirror_package)
			if(ret.length()>0)log("new RunStatus "+RunStatus);
			RunStatus="";
			return ret;
		}

		@Override
		public int startMusicPlayer(Intent i) {
			
			int ret=0;
			String path=i.getStringExtra("REMOTE_SONG_PATH");
			try {
			

				String synchpath=checkURL(path);
				if(synchpath==null){					
					return -1;
				}
				path=synchpath;

//				pre_playlist.clear();
				playlist.clear();
				playlist.put(path);
				
				
			} catch (InterruptedException e) {
			
				e.printStackTrace();
			}
			return ret;
		}
        public String checkURL(String url){
        	    
        	//    String local=null;       	      	    
        	    String path=null;
        	    String dev=null;
        	    
        	    
        		try {
        			path = URLDecoder.decode(url,"utf-8");
        		} catch (UnsupportedEncodingException e1) {
        			e1.printStackTrace();
        		}
        		
        		
				if(!path.contains("http://")){
					File f=new File(path);
					if(f!=null && !f.exists() ){
						log("error file is not exist ==>"+path);
						return null;
					}
					return url;	
				}
				
				
        	    int index=path.indexOf("?deviceid="), indexA=path.lastIndexOf("/");
        	    if(index!=-1){
        	    	 dev=path.substring(index+"?deviceid=".length(), path.length());
        	    	 if(indexA!=-1){
        	    		 
        	    		 String folder=Utils.getPathToFolderFolder(path);
        	    		// folder=(folder==null)?"":("/"+folder);
        	    		 
        	    		 path=path.substring(indexA, index);

        	    		 
        	    		 File f=new File(Utils.storage_card+"/music_service/device/"+dev+folder+path);
        	    		 log("seach >>>"+f.getPath());
        	    		 if(f.exists()){
        	    			 log("find ....dev:"+dev+"....path:"+path+"   local:"+f.getPath());
        	    			 return f.getPath();
        	    		 }
        	    	 }
        	    }
        	    index=url.indexOf("?deviceid=");
        	    return url.substring(0, index==-1?url.length():index);
        }
		@Override
		public void refreshDeviceMusicData(String info) {
		
			  log("refreshDeviceMusicData ................");
			  musiclist.clear();
	   		  LinkedList<String> list=sd.getMountedDevicesList();
	   		  for(int i=0; i<list.size(); i++){
	   			try {
	   				bq.put(list.get(i));
	   			} catch (InterruptedException e) {
	   	
	   				e.printStackTrace();
	   			}
	   		  } 
			    
		}

		@Override
		public String getMusicSynchStatus(String info) {
            
			if(info==null || info.length()<=0)  return null;
			String ret="ok";
			
			if(info.equals("pause")){
				musicsynch.mPause();

			}else if(info.equals("start")){
				musicsynch.mStart();
			}else if(info.equals("clear")){
				Asyn_bq.clear();
				log("getMusicSynchStatus "+ Asyn_bq.size());
			}else{
				ret="noknown";
			}
			
			
			
			
			return ret;
		}

		@Override
		public String getMusicPlayerStatus() {
			
			return musicplayer.getMusicPlayState();//+musicsynch.getSynchStatus();
		}

		@Override
		public void setMusicPlayerCmd(String cmd, String var) {
			 log("setMusicPlayerCmd"+cmd);
			if(cmd.equals("play")){
				
                if(ismirror  || mirror_package!=null) return ;
				Utils.sendSpeakerNotify(instance, "musicservice", false);
				Utils.sendSpeakerNotify(instance, "musicservice", true);
				//musicsynch.mPause();
				 musicplayer.onStart();
				
			}else if(cmd.equals("pause")){
				
				musicplayer.onPause();
				//musicsynch.mStart();

				
			}else if(cmd.equals("seek")){
				try{
				   musicplayer.onSeek( (var==null || var.equals(""))?0:Integer.parseInt(var));
				}catch(NumberFormatException e){
					log("error ..."+ e.toString());
				}
			}else if(cmd.equals("stop")){
	          	pre_playlist.clear();
            	musicsynch.mStart();
				musicplayer.onReset();
			}else if(cmd.equals("next")){
				String path=getNextPlayMusic(Utils.PLAY_NEXT);
				if(path!=null){
					try {
						playlist.clear();
						playlist.put(path);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					
					Utils.sendSpeakerNotify(instance, "musicservice", false);
					Utils.sendSpeakerNotify(instance, "musicservice", true);
				}
			}else if(cmd.equals("pre")){
				String path=getNextPlayMusic(Utils.PLAY_PRE);
				if(path!=null){
					try {
						playlist.clear();
						playlist.put(path);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Utils.sendSpeakerNotify(instance, "musicservice", false);
					Utils.sendSpeakerNotify(instance, "musicservice", true);
				}
			}else if(cmd.equals("playmode")){
				playmode=var;
				Utils.setVar("playmode", var);

				
				
				
				
				log("playmode ...."+ playmode);
			}
			
		}

		@Override
		public void deleteFileCmd(String cmd) {
			
		        if(cmd==null || cmd.equals("") || cmd.length()<2) return ;
		        handler.obtainMessage(Utils.Cmd_Del, cmd).sendToTarget();
			    
			   
		}

		@Override
		public void startMusicPlayerList(String info) {
			
			handler.removeMessages(Utils.Cmd_Time_DAR);
			Utils.sendSpeakerNotify(instance, "musicservice", false);
			Utils.sendSpeakerNotify(instance, "musicservice", true);
			//log("startMusicPlayerList..."+info);
			if(info==null || info.equals("") ) return ;
			synchronized (pre_playlist) {   
			 
					String items[]=info.split("::");
					String deviceid=items[1], ip=items[0],  path="", pathing=null;
					
					if(items.length<3) return ;
					
					if(items.length>3) pre_playlist.clear();
					
					File f=null;
					for(int i=2; i<items.length; i++){
				
						  path=items[i];
						  f=new File(path);
						  if(!f.exists()){
							  
							  
							  path=Utils.music_root_path+deviceid+Utils.getPathToFolderFolder(items[i])+"/"+Utils.getPathToFileName(items[i]);					  
							  f=new File(path);
						      if(!f.exists()){
						    	 
						    	    path="http://"+ip+":"+peer.HTTP_PORT+items[i];
									try {
										path=URLEncoder.encode(path,"utf-8");
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace();
									} 
									
						      }
						  }
						
						  log(">> play_path ..."+path+"\n");
						  if(pathing==null){
							  
							   pathing=path;
							   try {
								  playlist.clear();
							      playlist.put(pathing);
					           } catch (InterruptedException e) {
							      e.printStackTrace();
					           }
					           continue;
						  }
						  
						
						  pre_playlist.add(path);
						 
					}
			
			

			 }	
		}

		@Override
		public String getMusicFavorites(String var, String info) {
			       try {
					semaphore.take();
					} catch (InterruptedException e1) {
	
						e1.printStackTrace();
					}
			       log("getMusicFavorites var ..."+var+"   info....."+info);
			       String ret="";
			       if(var.equals("query")){
			    	   ret=Utils.sqlitehelper.queryMusicFavorites(Utils.sqlitehelper.sdcard_db);
			       }else if(var.equals("add")){
			    	   
			       }else if(var.equals("update")){
			    	   Utils.sqlitehelper.updateMusicFavorites(Utils.sqlitehelper.sdcard_db, info);
			       }else if(var.equals("delete")){
			    	   
			       }
			 
				   semaphore.release();
	
			       return ret;
		}

		@Override
		public String[] getMusicForm(String var, String info) {
			
					try {
						semaphore.take();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
			       log("var .."+var+"====info ..."+info);
			       String ret[]=new String[2];
			      // MusicForm mform=null;
			       if(var.equals("query")){
			    	   
			    	    musicformlist=Utils.sqlitehelper.queryAllMusicForm(Utils.sqlitehelper.sdcard_db, musicformlist);
			    	    ret[0]="query";
                        ret[1]=Utils.toMusicFormString(musicformlist, null);
			    	   
			    	   
			       }else if(var.equals("add")){
			    	   
			    	   LinkedList<MusicForm> list=Utils.getMusicFormList(null, info);
			    	   Utils.sqlitehelper.addMusicFormList(Utils.sqlitehelper.sdcard_db, list);
			    	   
			    	   ret[0]="add";
                       ret[1]="null";
			    	   
			       }else if(var.equals("update")){
			    	   
			    	   LinkedList<MusicForm> list=Utils.getMusicFormList(null, info);
			    	   for(int i=0; i<list.size(); i++)
			    		   Utils.sqlitehelper.updateMusicForm(Utils.sqlitehelper.sdcard_db, list.get(i));
			    	   ret[0]="update";
                       ret[1]="null"; 
			       }else if(var.equals("delete")){
			    	   
			    	   ret[0]="delete";
                       ret[1]="null"; 
			    	   if(info==null || info.equals("")) return ret;
			    	   LinkedList<MusicForm> list=Utils.getMusicFormList(null, info);
			    	   for(int i=0; i<list.size(); i++)
			    		   Utils.sqlitehelper.delete(Utils.sqlitehelper.sdcard_db, list.get(i));

                       
                       
			       }
			
				   semaphore.release();
		           log("musicform back ..."+ret[1]);
			       return ret;
			   
		}

		@Override
		public String onMirrorStart(String ip, String var) {
		
			if(var==null || var.equals("")) return "failed:-1";
			
			Utils.log("mirror .... "+var);
			String cmds[]=var.split(":");
			if(cmds[0].equals("request")){
				
				    int i=0;
				    Configuration newConfig = getResources().getConfiguration();  
		            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){  
		                i=1;
		            }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){  
		                i=0;
		            }
				    
				    return "ok:"+Utils.sw+"x"+Utils.sh+";H264;"+i;
				    
			}else if(cmds[0].equals("play")){
				
				    musicsynch.mPause();
				  //  musicplayer.onPause();
				    Utils.sendSpeakerNotify(instance, "mirror", true);
				    Utils.sendSpeakerNotify(instance, "mirror", false);
					if(ismirror){
						
					
						
						try {
							if(remoteservice!=null) remoteservice.stopMirror();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						
						
						remoteservice=null;
						Intent intent  = new Intent("com.mele.musicdaemon.RemoteService");	
						bindService(intent, connection, Context.BIND_AUTO_CREATE);
					    while(remoteservice==null){
						     try {
								 Thread.sleep(200);
							} catch (InterruptedException e) {					
								e.printStackTrace();
							}
					    }
					    
					    
					}
					

					
					ismirror=true;
					try {
						if(remoteservice!=null)remoteservice.startMirror(ip, videoport);
					} catch (RemoteException e) {
						e.printStackTrace();
						ismirror=false;
					}
					
					return "ok:1";
					
			}else{
				    return "failed:-1";
			}
			
			

		}

		@Override
		public void onMirrorStop() {
			
			Utils.log("onMirrorStop ......");
		    musicsynch.mStart();
			ismirror=false;
			if(!instance.RunStatus.contains("mirrorapk")) instance.RunStatus+=instance.getMirrorStatus(instance.mirror_package);
			
			try {
				if(remoteservice!=null){
					   //remoteservice.ioctrl("wm", "160");
					   remoteservice.stopMirror();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			
//			Intent home = new Intent(Intent.ACTION_MAIN);  
//			home.addCategory(Intent.CATEGORY_HOME);
//			home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(home); 
			if(MainActivity.instance!=null) MainActivity.instance.finish();
			
			Intent intent  = new Intent("com.mele.musicdaemon.RemoteService");
			bindService(intent, connection, Context.BIND_AUTO_CREATE);
			

			
		}

		@Override
		public void onKeyCode(int keycode) {
		
			if(keycode==KeyEvent.KEYCODE_VOLUME_DOWN){
				setOutVolume(false);
				return ;
			}else if(keycode==KeyEvent.KEYCODE_VOLUME_UP){
				setOutVolume(true);
				return ;
			}
			try {
				if(remoteservice!=null) remoteservice.keyCode(keycode);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onMouseEvent(MotionEvent event) {
		
			try {
				if(remoteservice!=null) remoteservice.MouseEvent(event.getX(), event.getY(), event.getAction());
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void installApp(String id, String url) {
			
			Map<String, String> map = new HashMap<String,  String>();
			map.put("id", id);
			map.put("url", url);
			try {
				appdownload.state=0;
				appdownload_bq.put(map);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getInstallAppProgress() {	
			String ret=appdownload.getDownloadStatus();
			log("getInstallAppProgress "+ret);
			return ret;
		}

		@Override
		public String getAppInfo(String info) {
			
			String ret="";
			if(info==null || info.equals("")){
				
				
				List<Map<String, Object>> list=Utils.fetch_installed_apps(MusicService.this);	
				for(int i=0; i<list.size(); i++){
					  ret+=list.get(i).get("version").toString()+":"
				           +list.get(i).get("appName").toString()+":"
				           +list.get(i).get("packageName").toString()+">";
				}
				
				
				return ret;
			}
			
			
			String items[]=info.split(":");
			for(String obj:items){
				
				ret+=obj+":"+Utils.checkPackage(instance, obj)+">";
				
			}
			
			
			return ret;
		}

		@Override
		public String[] getTimeClock(String var, String info) {
		
			try {
				semaphore.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	       log("var .."+var+"====info ..."+info);
	       String ret[]=new String[2];
	       TimeClock tc=null;
	       if(var.equals("query")){
	    	   
	    	    LinkedList<TimeClock> list=Utils.sqlitehelper.queryAllClockTime(Utils.sqlitehelper.sdcard_db, null);
	    	    ret[0]="query";
	    	    ret[1]="";
	    	    for(int i=0; i<list.size(); i++){
	    	    	 ret[1]+=Utils.getTimeClockStr(list.get(i))+">";
	    	    }
          
	    	   
	    	   log("getTimeClock query ..."+ret[1]);
	       }else if(var.equals("add")){
	    	   
	    	   tc=Utils.getTimeClock(info);
	    	   Utils.sqlitehelper.addTimeClockItem(Utils.sqlitehelper.sdcard_db, tc);
	    	   setTimerClock(tc);
	    	   ret[0]="add";
               ret[1]="null";
	    	   
	       }else if(var.equals("update")){
	    	   
	    	   tc=Utils.getTimeClock(info);
	    	   Utils.sqlitehelper.updateTimeClock(Utils.sqlitehelper.sdcard_db, tc);
	    	   ret[0]="update";
               ret[1]="null"; 
              // cancelTimeClock(tc);
               setTimerClock(tc);
               
	       }else if(var.equals("delete")){
	    	   
	    	   tc=Utils.getTimeClock(info);
	    	   ret[0]="delete";
               ret[1]="null"; 
               Utils.sqlitehelper.delete(Utils.sqlitehelper.sdcard_db, tc);
               cancelTimeClock(tc);
               
               
	       }
	
		   semaphore.release();

	       return ret;
		}

		@Override
		public String setShutdown(String cmd) {
            Utils.setShutdown(instance, cmd);			
			return "";
		}

		@Override
		public String getWiFiList() {
			
		//	String info=Utils.buildwifijson(wifiadmin.getListResult(), wifiadmin.wifiConfigList, wifiadmin.getSSID());
			//log("======>"+info);
			return "";
		}

		@Override
		public String setupWiFi(final String ssid, final String security, final String pwd) {
			
			new Thread(new Runnable(){

				@Override
				public void run() {
					
				//	wifiadmin.setupWiFi(ssid, security, pwd);
				}
				
			}).start();
			
			return null;
		}

		@Override
		public String setWiFiCtrl(int type, String ssid, boolean flag) {

			log("type "+type+" ssid "+ssid+"flag "+flag);
            if(type==0){//forgest
            	
            	// wifiadmin.removeWifi(ssid);
            	
            }else if(type==1){//disconnect
            	
            	//wifiadmin.disconnectWifi();
            	
            }else if(type==2){//enable
            	
            	//if(flag) wifiadmin.openNetCard();
            	//else wifiadmin.closeNetCard();
            	
            }
			return "";
		}

		@Override
		public String onMusicEQ(String str) {
			if(str==null || str.equals("")) return "";
			Utils.log("music_eq ...."+str);
			String ret="";
			if(str.contains("query")){
				
				    try {
					
				    	ret="query";
						int buf[]=remoteservice.jni_i2c_read(0x2F, 20);
						
//                        for(int i=0; i<buf.length; i++) ret+="0x"+Integer.toHexString(buf[i])+" ";
//                        Utils.log("0x2F ..."+ret);
//                        ret="query";
						int k=0, j=0;
						for(k=0; k<Utils.HEQ.length; k++){
							     int[] buf1=Utils.HEQ[k];
							     if(buf1[16]==0x0F) buf1[16]=0x03; if(buf1[4]==0x0F) buf1[4]=0x03;
							     for(j=0; j<buf1.length; j++){
							    	 if(buf[j]!=buf1[j]){
							    		 //Utils.log(">>>>>>>>"+buf[j]+"===="+buf1[j]);
							    		 j=-1;
							    		 break;
							    	 }
							     }
							     if(j!=-1){
							    	 ret+=":"+k;
							    	 k=-1;
							    	 break;
							     }

						}
						if(k!=-1) ret+=":8";
						
						
						buf=remoteservice.jni_i2c_read(0x2A, 20);
						k=0; j=0;
						for(k=0; k<Utils.LEQ.length; k++){
							     int[] buf1=Utils.LEQ[k];
							     if(buf1[16]==0x0F) buf1[16]=0x03; if(buf1[4]==0x0F) buf1[4]=0x03;
							     for(j=0; j<buf1.length; j++){
							    	 if(buf[j]!=buf1[j]){
							    		 j=-1;
							    		 break;
							    	 }
							     }
							     if(j!=-1){
							    	 ret+=":"+k;
							    	 k=-1;
							    	 break;
							     }

						}
						if(k!=-1) ret+=":8";
						
						
//                        ret="";
//                        for(int i=0; i<buf.length; i++) ret+="0x"+Integer.toHexString(buf[i])+" ";
//                        Utils.log("0x2F ..."+ret);
//						buf=remoteservice.jni_i2c_read(0x2A, 20);
//                        ret="";
//                        for(int i=0; i<buf.length; i++) ret+="0x"+Integer.toHexString(buf[i])+" ";
//                        Utils.log("0x2A ..."+ret);
                        
					 } catch (RemoteException e) {
						
						e.printStackTrace();
					 } catch (NumberFormatException e){
						 
					 }
				
				  //ret="query:8:8";

			}else if(str.contains("setH")){
				
				int index=str.indexOf(":");
				if(index!=-1){
					 try {
					    index=Integer.parseInt(str.substring(index+1));
						remoteservice.jni_i2c_writedev(0x1a, 0x2B, Utils.HEQ[index], Utils.HEQ[index].length);
						remoteservice.jni_i2c_writedev(0x1a, 0x32, Utils.HEQ[index], Utils.HEQ[index].length);
						//Utils.log("get Heq"+Utils.getVarInt("heq"));
						Utils.setVar("heq", index);
						
					 } catch (RemoteException e) {
						
						e.printStackTrace();
					 } catch (NumberFormatException e){
						 
					 }
					 
				}				
				ret="setH:ok";
			}else if(str.contains("setL")){
				
				int index=str.indexOf(":");
				if(index!=-1){
					 try {
					    index=Integer.parseInt(str.substring(index+1));
						remoteservice.jni_i2c_writedev(0x1a, 0x2B, Utils.LEQ[index], Utils.LEQ[index].length);
						remoteservice.jni_i2c_writedev(0x1a, 0x32, Utils.LEQ[index], Utils.LEQ[index].length);
						Utils.setVar("leq", index);
					 } catch (RemoteException e) {
						
						e.printStackTrace();
					 } catch (NumberFormatException e){
						 
					 }
					 
				}
				ret="setL:ok";
				
			}else{
				ret="unknown";
			}
			
			Utils.log("speaker to client music_eq ...."+ret);
			return ret;
		}

		@Override
		public void setVolTAS5711(int value) {
		
			
			
			
			int[] buf=new int[1];
			
			int i=value;
			if(i<0) i=0;
			else if(i>15) i=15;
	
			buf[0]=Utils.t_Volume[i];
			Utils.log("setVolTAS5711 "+i+"  "+buf[0]);
			
			try {
				remoteservice.jni_i2c_write(0x07, buf, 1);
			} catch (RemoteException e) {
				
				e.printStackTrace();
			}
			
		}

		@Override
		public void copyMusicFile(int channel, String info) {
			   if(info==null || info.equals("")) return ;
			   
			   File file=new File(Utils.storage_card+"/music_usb/"); 
			   if(!file.exists()) file.mkdir(); 
			   
			   String items[]=info.split("::");			   
			   copy_MusicFile(channel, items);
			   
			   
			   
			
		}

		@Override
		public int uninstallApp(String app_package) {
		
			if(Utils.checkPackage(instance, app_package)==-1) return -1;
			
			try {
				remoteservice.ioctrl("uninstall", app_package);
			} catch (RemoteException e) {
			
				e.printStackTrace();
			}
			
			
			return 0;
		}

		@Override
		public String registerUser(String info) {
			
			if(info==null || info.equals("")) return "";
			String userinfo="", ret=null;
			ret=Utils.getInfoItem(info, "cmd");
			if(ret.equals("query")){
				
				userinfo+="cmd=query\n";				
				ret=Utils.getVar("user");
				
				if(ret.equals("0")){
					userinfo+="status=unreg\n";
				}else{
					userinfo+="user="+ret+"\n";
					userinfo+="pwd="+Utils.getVar("pwd")+"\n";
					userinfo+="status=reg\n";
				}
				
			}else if(ret.equals("register")){
				
				userinfo+="cmd=register\n";
				ret=Utils.getInfoItem(info, "user");
				if(ret!=null){
					Utils.setVar("user", ret);
				}
				ret=Utils.getInfoItem(info, "pwd");
				if(ret!=null){
					Utils.setVar("pwd", ret);
				}
				userinfo+="status=ok\n";
			}
			log("UserInfo ...."+userinfo+"\n");	
			return userinfo;
		}

		@Override
		public void checkSystemUpdate(String cmd) {
			   if(cmd==null || cmd.equals("")) return ;
			   Utils.log("checkSystemUpdate ...."+ cmd);
			   if(cmd.equals("check")){			   
				      Intent i=new Intent("com.mele.UPDATE_CHECK_VERSION");
				      sendBroadcast(i);
			   }else if(cmd.equals("update")){
				      Intent i=new Intent("com.mele.UPDATE_START");
				      sendBroadcast(i);
			   }
			   
			
		}

		@Override
		public void startAPP(String packagename) {
		
			
			new Thread(new Runnable(){

				@Override
				public void run() {
					
								Intent i=new Intent(MusicService.this, MainActivity.class);
			                    i.putExtra("ui", "true");
			                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			                    startActivity(i);
				}
				
			}).start();

	    	try {
				remoteservice.ioctrl("clearmemory", null);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mirror_package=packagename;
			
			try {
				if(remoteservice!=null){
					if(packagename.equals("com.kugou.android")){
					   remoteservice.ioctrl("wm", "240");
					}else{
					   remoteservice.ioctrl("wm", "320");
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			


			
		}
		
	};
	

	public void copy_MusicFile(final int channel ,final String items[]){
		
		     new Thread(new Runnable(){

				@Override
				public void run() {
					   
					  
					  String item[]=items;
					  String name=null;
				      String src=Utils.music_usb;
			    	  if(Utils.getSDCardAvailable()<Utils.Capacity ){
			    		 if(sd.isMountedPath(Utils.extern_sdcard_path)){
			    			 src=Utils.extern_sdcard_path+"/music_usb/";
			    		 }else{
	 							    		  			    		  
	 			    		 if( !RunStatus.contains("full") )RunStatus+="full;;";
	 			    		 return ;
			    		 }

			    	  }					  
					  for(int i=0; i<item.length; i++){
						     
						  

						    name=items[i].substring( items[i].lastIndexOf("/")+1 );
						    Utils.copyFile(item[i], src+name);

					  }
					
					  peer.sendMessageNoWait(channel, "copy;;");
					  if( !RunStatus.contains("yes") )RunStatus+="yes;;";
					  
				}
		    	 
		     }).start();
		
	}
	
	
	//============================================================================================
    public static void killApp(String packageName){
	
    	Utils.log("killapp ..... "+ packageName);
    	
    	try {
    		
    		if(instance.mirror_package!=null && !instance.mirror_package.equals(packageName))
    			instance.remoteservice.killApp(packageName);
    		instance.mirror_package=null;
			instance.remoteservice.killApp(packageName);
			instance.remoteservice.ioctrl("clearmemory", null);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    	
    	if(!instance.RunStatus.contains("mirrorapk")) instance.RunStatus+=instance.getMirrorStatus(instance.mirror_package);
    }
	public MusicPlayer.MusicPlayerListener MPListener=new MusicPlayer.MusicPlayerListener(){

		@Override
		public void setMusicPlayerStatus(String status) {
			
			  if(status.equals("finish")){
				   if(pre_playlist.size()>0){
					   try {
						    playlist.clear();
						    String path=getAutoPlayMusic();
							if(path!=null) playlist.put(path);
							else musicsynch.mStart();
					   } catch (InterruptedException e) {
							e.printStackTrace();
					   }
				   }else{
					   musicsynch.mStart();
				   }
				   
			  }else if(status.equals("start")){
				   
				  String playmusic=musicplayer.getPlaymusic();  
				  if( playmusic!=null && playmusic.contains("http://") )
				       musicsynch.mPause();
				   
				   
			  }
		}
		
	};
	public MusicSynch.MusicSynchListener SynListener=new MusicSynch.MusicSynchListener(){

		@Override
		public void setMusicSynchStatus(String path) {
		
			    int i=0;
			    for(i=0; i<musiclist.size(); i++){
			    	if(musiclist.get(i).getPath().equals(path)){
			    		i=-1;
			    		break;
			    	}
			    }
			    if(i!=-1){
			    	
			    	  MusicFile mf=new MusicFile(Utils.getId(), path, Utils.getPathToFileName(path), "null", Utils.sqlitehelper.running_db);			    	  
			    	  Utils.sqlitehelper.addMusicItem(Utils.sqlitehelper.running_db, mf);
			    	  Utils.getMusicMsg(mf);
			    	  musiclist.add(mf);
			    	  RunStatus+="Syn:"+mf.getPath()+"*>"+mf.getArtist()+"*>"+mf.getAlbum()+"*>"+mf.getId()+";;";
			    	  log("MusicSynchStatus "+RunStatus);
			    }
			
			
			    
		}
		
	};
	public AppDownload.InstallApplistener installapplistener=new AppDownload.InstallApplistener(){

		@Override
		public String installApp(String path, String packagename) {
	       String ret=null;
			try {
				ret=remoteservice.installApp(path, packagename);
			} catch (RemoteException e) {
		
				e.printStackTrace();
			}
			return ret;
		}
		
	};
	public String  getAutoPlayMusic(){
		 
		   if( pre_playlist==null || pre_playlist.size()==0 ) return null;
		   synchronized (pre_playlist) { 
			   
			   
			   
			   
				   String music=musicplayer.getPlaymusic();
				   int index=-1;
				   log("playmode ..."+playmode);
				   if(playmode.equals("single_cycling")){ //锟斤拷锟斤拷循锟斤拷

				   }else if(playmode.equals("order")){//顺锟斤拷
					   
					   music=newPlayMusic(Utils.PLAY_NEXT, false);
				   }else if(playmode.equals("cycling")){//锟叫憋拷循锟斤拷
					   
					   music=newPlayMusic(Utils.PLAY_NEXT,true);
					   
				   }else if(playmode.equals("random")){//锟斤拷锟�
					   
		
				      if(randomlist.size()==0 || pre_playlist.indexOf(randomlist.get(0))==-1 ) {
						   
						   randomlist.clear();
						   randomlist.addAll(pre_playlist);
					   }
					   
					   
					   index=(int)(Math.random()*randomlist.size());					  
					   music=randomlist.get(index);
					   
					   randomlist.remove(index);
					   
					   
					  // index=(int)(Math.random()*pre_playlist.size());
					   log("index===="+pre_playlist.size()+"======"+index);
					 //  music=pre_playlist.get(index);
					  
					   
				   }else{  //锟斤拷锟斤拷
					   
					   music=null; 
				   }
		
				   return music;
		   }
	}
	
	public String newPlayMusic(int mode, boolean flag){
		
		  if( pre_playlist==null || pre_playlist.size()==0 ) return null;
		 
		  String playmusic=musicplayer.getPlaymusic();
		  //log("playmusic "+playmusic+">>>>>>>>>>"+pre_playlist.size());		  
		  if( playmusic==null || playmusic.equals("") ) return  pre_playlist.get(0);
		  
		  int index=pre_playlist.indexOf(playmusic);
		//  synchronized (pre_playlist) {  
			  if(index==-1 ){
				  for(int i=0;  i<pre_playlist.size(); i++){
					     if(playmusic.contains(pre_playlist.get(i))){
					    	  index=i;
					    	  break;
					     }
				  }
			  }
		  //}
		  if(index==-1)	  return  pre_playlist.get(0);
		  log("size ===="+pre_playlist.size()+" index ====="+index);
		  if(index!=-1){ 
			 // log(">>>111");
			  if(mode==Utils.PLAY_PRE){
				 // log(">>>000");
				    if(index==0) {
				    	if(!flag) return null;
				    	index=pre_playlist.size()-1;
				    }else index--;
				    
			  }else if(mode==Utils.PLAY_NEXT){
				   //log(">>>222");
				    if( (index+1)==pre_playlist.size()){
				    	if(!flag) return null;
				    	index=0;
				    }else index++;
			  }
			  playmusic=pre_playlist.get(index);
			 
		  }
		  log("playmusic>>>>>>>>>>>"+playmusic);
		  return playmusic;
	}
	
	public String getNextPlayMusic(int mode){
			
		    String music=null;
			
			
		   if( pre_playlist==null || pre_playlist.size()==0 ) return null;
		   synchronized (pre_playlist) { 
			   
				   music=musicplayer.getPlaymusic();
				   int index=-1;
				   
				   if(playmode.equals("random")){//闅忔満
					   	
					   if(randomlist.size()==0 || pre_playlist.indexOf(randomlist.get(0))==-1 ) {
						   
						   randomlist.clear();
						   randomlist.addAll(pre_playlist);
					   }
					   
					   
					   index=(int)(Math.random()*randomlist.size());					  
					   music=randomlist.get(index);
					   
					   randomlist.remove(index);

				   }else{
					   
					   music=newPlayMusic(mode, true); 
				   }
		           log("getNextPlayMusic ..."+playmode+"====>play:"+music);
				   return music;
		   }			

	}
    public void searchMusicRes(){
    	
    	   musiclist.clear();
    	   new Thread(new Runnable() {
      			@Override
      			public void run() {      				
      				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);    				
      				while(true){
      				    try {
      				    	
      				    	ischeck_sd=false;
							String path=bq.take();
						    ischeck_sd=true;
						    Utils.log("path ..."+path);
							String mount=path;
							if(mount.contains(Utils.storage_card)) {
								Utils.sqlitehelper.running_db=Utils.sqlitehelper.sdcard_db;
							    mount=Utils.storage_card;    
							}else {
								is_sdcard_play=true;
								pre_playlist.clear();
								Utils.sqlitehelper.getExternData(mount);
								Utils.sqlitehelper.running_db=Utils.sqlitehelper.extern_db;
								
								if(mount.contains("/storage/sdcard")){
									 Utils.checkMkdir(mount);
								}else{
									sdcardlist.clear();									
								}
								Utils.sqlitehelper.extern_db_list.add(Utils.sqlitehelper.extern_db);

							}
							LinkedList<MusicFile> list=new LinkedList<MusicFile>();
							Utils.sqlitehelper.queryAllMusic(Utils.sqlitehelper.running_db, list);
							
							if(list.size()>0)handler.obtainMessage(Utils.Cmd_Ret, list).sendToTarget();							
							
							
							long v=Utils.sqlitehelper.queryDeviceSpace(Utils.sqlitehelper.running_db);
							long v1=Utils.getSDCardAvailable(mount);
							
							log("sdcard info ==========>dbv: "+v+" v1: "+v1+" mount: "+ mount+" list.size()"+list.size());
							
							if(Math.abs(v1-v)>1024*512 || !mount.contains(Utils.storage_card)){
								
								log("SDcard Search Start:"+mount);
								Utils.sqlitehelper.updateDeviceSpace(Utils.sqlitehelper.running_db, v1);
								
								findmediafile(path, list);
								
								log("SDcard Search End:"+mount);
							}
							
							
							
							
						} catch (InterruptedException e) {		
							e.printStackTrace();
						}
      				}
      			}
      	  }).start();
    	  
    	   
    	   
    	   
    	   
   		  LinkedList<String> list=sd.getMountedDevicesList();
   		  for(int i=0; i<list.size(); i++){
   			try {
   				bq.put(list.get(i));
   			} catch (InterruptedException e) {
   	
   				e.printStackTrace();
   			}
   		  }   	   
    	   
    }
   
    public synchronized boolean checkSDCarcMusicResource(){
       
 	   boolean ret=false;
 	   if(ischeck_sd) return ret;
 	   ischeck_sd=true;
 	   log("===>checkSDCarcMusicResource ing .....");
 	   
//
// 	   
//	   LinkedList<String> list=sd.getMountedDevicesList();
//	   for(int i=0; i<list.size(); i++){
//   				    try {
//						
//							String mount=sd.getMountedPath(list.get(i));
//							if(mount==null) {
//								Utils.sqlitehelper.running_db=Utils.sqlitehelper.sdcard_db;
//							    mount=Utils.storage_card;    
//							}else {
//							
//								Utils.sqlitehelper.getExternData(mount);
//								Utils.sqlitehelper.running_db=Utils.sqlitehelper.extern_db;
//							}
//						
//							
//							semaphore.take();
//							long v=Utils.sqlitehelper.queryDeviceSpace(Utils.sqlitehelper.running_db);
//							long v1=Utils.getSDCardAvailable(mount);
//							
//							log(list.get(i) + " sdcard info ==========>dbv: "+v+" v1: "+v1);
//						//	if(Math.abs(v1-v)>1024*128){
//								
//							
//							LinkedList<MusicFile> surlist=new LinkedList<MusicFile>();
//							Utils.sqlitehelper.queryAllMusic(Utils.sqlitehelper.running_db, surlist);
//							log(surlist.size()+" ......start search update "+list.get(i));
//							Utils.sqlitehelper.updateDeviceSpace(Utils.sqlitehelper.running_db, v1);
//							 
//							semaphore.release();
//	
//								
//							findmediafile(list.get(i), surlist);
//								
//
//							//}
//							
//							
//							
//							
//						} catch (Exception e) {		
//							e.printStackTrace();
//						}
//
// 	   
// 	   
//
//	
//		}

	    ischeck_sd=false;
 	    return ret;
    }
    public synchronized boolean findmediafile(String path, LinkedList<MusicFile> list){
    	
    	if(path==null) return false;
    	String Path=null;
    	LinkedList<MusicFile> addlist=new LinkedList<MusicFile>();
    	templist.clear();
    	findmusicfile(path, list, addlist, path);
    	while(templist.size()>0){
    		
	    	Path=templist.getFirst();
	    	templist.removeFirst();
	    	findmusicfile(Path, list, addlist, path);
	
    	}
    	if(addlist.size()>0){
    		Utils.getMusicMsg(addlist);
    		handler.obtainMessage(Utils.Cmd_Ret, addlist).sendToTarget();
    	}
    	log("find search "+path);
    	
    	
    	return addlist.size()>0?true:false;
    
    	
    }
    public  void findmusicfile(String path, LinkedList<MusicFile> list, LinkedList<MusicFile> addlist, String root){
    	  
    	
	     File f = new File(path);
	     if(!f.exists()) return ;
	     File[] files = f.listFiles();
	     if(files==null) return ;
	     

		 for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if(file.getName().charAt(0)=='.') continue;
				if(file.isFile()){
					
				    if(Utils.getMIMEType(file.getPath())==1){
				    	int j=0;
		        		for(j=0; j<list.size(); j++){
		        			    if( list.get(j).getPath().equals( file.getPath() ) ){
		        			    	    j=-1;
		        			    	    break ;	        			    	
		        			    }
		        		}
				    	if(j!=-1){
			    		 try {
							semaphore.take();
						 } catch (InterruptedException e1) {
							e1.printStackTrace();
						 }
				    	  MusicFile mf=new MusicFile(Utils.getId(), file.getPath(), file.getName(), path, Utils.sqlitehelper.running_db);
				    	  Utils.sqlitehelper.addMusicItem(Utils.sqlitehelper.running_db, mf);
				    	  addlist.add(mf);
				    	
				    	  semaphore.release();

				    	  log("find music "+file.getPath());
				    	}
				    	
				    }
				    
				    
				}else if(file.isDirectory()){
					templist.add(file.getPath());
				}

		 }    
	  
	  
  }

	
	
    public static void invokeShutdown(){


//    	MusicService.instance.pre_playlist.clear();
//    	MusicService.instance.musicsynch.mStart();
//    	MusicService.instance.musicplayer.onReset();
    	
		try {
			if(instance.remoteservice!=null) instance.remoteservice.keyCode(KeyEvent.KEYCODE_POWER);
		} catch (RemoteException e) {
			e.printStackTrace();
		}


    }
    public static void invokeADS(String status){
    	       if(status==null || status.equals("")) return;
    	       if(status.equals("1")){
    	    	   instance.MIndex=-1;
    	    	   Utils.rwAudioIO("1");
    	    	   MusicService.instance.musicplayer.onPause();
    	       }else{
    	    	   Utils.rwAudioIO("0");
    	    	   instance.MIndex=0;
    	    	 //  MusicService.instance.musicplayer.onStart(); 
    	       }
    	       
    }
	@Override
	public IBinder onBind(Intent i) {
		
		
		
		return null;
	}

	@Override
	public void onDestroy() {	
		
		super.onDestroy();
		peer.closePeer();
		releaseBroadcastReceiver();
		
//		if(mLock.isHeld() == true)
//    		mLock.release();
		Log.e(TAG,"MusicService .... Exit !!! ");
		
		System.exit(0);
	}
	public  Handler handler = new Handler(){  
	        @Override  
	        public void handleMessage(Message msg) {  

	        	 switch(msg.what){
	        	 
	        	   case Utils.Cmd_Ret:
	        		   
	        		   LinkedList<MusicFile> list=(LinkedList<MusicFile>)msg.obj;
	        		   musiclist.addAll(list);
	        		   
	        		   Utils.log(TAG, "Utils.Cmd_Ret....."+musiclist.size());
	        		   for(int i=0; i<list.size();i++){
	        			     if(list.get(i).getPath().contains("/storage/external_storage/sda")){
	        			  
									pre_playlist.add(list.get(i).getPath());
                                    
	        			     }
	        			      
	        		   }
	        		   if(pre_playlist.size()>0 && is_sdcard_play){
	        			   
	        			       is_sdcard_play=false;
	        			       randomlist.clear();
	        			       musicplayer.onReset();
//	    					   try {
//	   						       String path=getAutoPlayMusic();
//								   if(path==null){
//									   path=pre_playlist.get(0);
//								   } 
//								   playlist.put(path);
//	   					       } catch (InterruptedException e) {
//	   							  e.printStackTrace();
//	   					       }
	        			   
	        			   
	        		   }
	        		   
	        		   if( !RunStatus.contains("yes") ) RunStatus+="yes"+";;";
	        		   

	        		   log("New Music Count: "+list.size());
	        		   
	        		   
	        		   
	        		   
	        		   break;
	        	   case Utils.Cmd_Del:
	        		   
		        		String cmd=msg.obj.toString();   
		   			    String[] items=cmd.split("\n");
					    if(items==null ) return ;

					    
						LinkedList<MusicFile> loadlist=new LinkedList<MusicFile>();		
						Iterator<MusicFile> iter = Asyn_bq.iterator(); 
						while(iter.hasNext()) { 
						    loadlist.add(iter.next());
						}
						
						
						
					    for(int i=1; i<items.length; i++){
					    	
					    	cmd=items[i];
                            
					        File f=new File(cmd);
					        if(f.exists()){
					        	f.delete();
					        	log("delete .... "+f.getPath());
					        }else{

					        	
					        	f=new File(Utils.music_root_path+items[0]+Utils.getPathToFolderFolder(cmd)+"/"+Utils.getPathToFileName(cmd));
					        	
					        	if(f.exists()) f.delete();
					        	
					        	
					        	for(int j=0; j<loadlist.size(); j++){
					        		 // log("...."+clientlist.get(j).getPath()); 
					        		  if(loadlist.get(j).getPath().equals(cmd)){
					        			  loadlist.get(j).setValid(false);
					        			  log("valid===>"+cmd);
					        			  loadlist.remove(j);
					        	
					        			  break;
					        		  }
					        	}
					        }
					        
					        
					        
					     
					        for(int j=0; j<musiclist.size(); j++){
					        	  MusicFile mf=musiclist.get(j);
					        	  if(mf.getPath().equals(cmd) || mf.getPath().contains(Utils.getPathToFolderandFile(cmd))){
					        		  
								      File file=new File(mf.getPath());
								      if(file.exists()){
								        	file.delete();
								      }
					        		  musiclist.remove(j);
					        		  Utils.sqlitehelper.delete(mf.getDb(), mf);
					        		  break;
					        	  }
					        }
					        
					        
					    }

	        		   
	        		   break;	        		   
	        	   case Utils.Cmd_SDCard:
	        		   
	        		   String mount=msg.obj.toString();
	        		   log("mount "+mount+" msg.arg1 "+msg.arg1);
	        		   if(msg.arg1==1 ){
                            
	        			   if(mount.contains("/storage/sdcard")){//锟斤拷展锟斤拷
	        				     Utils.checkMkdir(mount);	        				   
	        			   }
	        	   			try {
	        	   				bq.put(mount);
	        	   			} catch (InterruptedException e) {	        	   	
	        	   				e.printStackTrace();
	        	   			}
	        			   
	        	   			
	        	   			
	        		   }else if(msg.arg1==-1){
	        			   
	        			    pre_playlist.clear();
	        			    for(int i=0; i<musiclist.size(); i++){
	        			    	  if(musiclist.get(i).getPath().contains(mount)){
	        			    		  log("remove .... "+musiclist.get(i).getPath());
	        			    		  musiclist.remove(i);
	        			    		  i--;
	        			    	  }
	        			    }

	        			    if( !RunStatus.contains("yes") ) RunStatus+="yes"+";;";
	        		   }
	        		   
	        		   break;
	        	   case Utils.Cmd_Time_Clock:
	        		    String str=msg.obj.toString();
	        		    log("Alarm ....."+str);
	        		    
	        		    
	      	            if(str==null || str.equals("") || str.equals("library=") || str.equals("form=") ){
	      	            
	      	            	pre_playlist.clear();
					        for(int i=0; i<musiclist.size(); i++){				        	
					        		 pre_playlist.add(musiclist.get(i).getPath());							        	 
					        }
					        log("playing default music .........");
					        
	      	            }else if(str.contains("library=")){

	    	        	  String[] item=str.substring("library=".length()).split(":");
	    	        	  if(item.length>0){
	    	        		  
	    	        		  pre_playlist.clear();
	    	        		  for(int j=0; j<item.length; j++){
	    	        			    
	    					        for(int i=0; i<musiclist.size(); i++){				        	
	    					        	 MusicFile mf=musiclist.get(i);
	    					        	// Utils.log("item ..."+item[j]+"....mf "+ mf.getId());
	    					        	 if(mf.getId().equals(item[j])){
	    					        		 pre_playlist.add(musiclist.get(i).getPath());
	    					        		 break;
	    					        	 }					        	 
	    					        }
	    				        
	    	        	      }
	    	        		  
	    	        	  }

	    	    
	    	        	  
	    	          }else if(str.contains("form=")){
	    	        	  
	    	        	   str=str.substring("form=".length());
	    	        	    musicformlist=Utils.sqlitehelper.queryAllMusicForm(Utils.sqlitehelper.sdcard_db, musicformlist);
		        		    MusicForm mf=null;
		        		    for(int i=0; i<musicformlist.size(); i++){
		        		    	  if(musicformlist.get(i).getId().equals(str)){
		        		    		  mf=musicformlist.get(i);
		        		    		  break;
		        		    	  }
		        		    }
		        		    if(mf!=null){
		        		    	
		        		    	pre_playlist.clear();
		        		    	for(int j=0; j<mf.getMusiclist().size(); j++){
		        		    	     for(int i=0; i<musiclist.size(); i++){
		        					          		
		        		            			      if(mf.getMusiclist().get(j).equals(musiclist.get(i).getId())){    	
		        		            			    	  pre_playlist.add(musiclist.get(i).getPath());
		        		            			    	  break;
		        		            			      }
		        		            		 
		        		    	     }
		        	            }
		        		     }	
	    	        
	    	          }
	        		    

						 if(pre_playlist.size()>0){
							 
							   try {
								  playlist.clear();
							      playlist.put(pre_playlist.get(0));
					           } catch (InterruptedException e) {
							      e.printStackTrace();
					           }
							   
							   
							   int i=0;
							   for(i=0; i<Utils.out_Volume.length; i++){
								     
									   if(Utils.out_Volume[i]==vol){				
										   break;
									   }				
							   }
							   if(i==Utils.out_Volume.length) i=16;
							   
							   PeerServerIP.setSpeakerVolume(0);
							   handler.obtainMessage(Utils.Cmd_Sound_Mode, 0, i).sendToTarget();
						 }
						 
						 
	        		  
	        		    break;
	        	   case Utils.Cmd_Time_Key:{
	        		   
	        		    
	        		     if(msg.arg2==-1){
	        		    	removeMessages(Utils.Cmd_Time_Key);
	        		    	return ;
	        		     }
	        		   
	        		    // int vol=PeerServerIP.getRemoteVolume();
	        		     //log("vol===>"+vol);
	        		     
	        		     
	        		     if(msg.arg1==1 && msg.arg2!=-1){
	        		        // PeerServerIP.setSpeakerVolume(++vol);
	        		         setOutVolume(true);
	        		     }else if(msg.arg1==-1 && msg.arg2!=-1){
	        		    	// PeerServerIP.setSpeakerVolume(--vol); 
	        		    	 setOutVolume(false);
	        		     }else if(msg.arg1==-2 && msg.arg2!=-1 ){
	        		         PeerServerIP.setSpeakerVolume(0);
	        		         return ;
	        		     }
	        		    
	        		     
	        		     
	        		     if(msg.arg2==1){
	        		        msg= obtainMessage(Utils.Cmd_Time_Key, msg.arg1, 1);
	        		        sendMessageDelayed(msg, 300);
	        		     }
	        		     
	        		     
	        	       }
	        	       break;
	        	       
	        	   case Utils.Cmd_Time_DAR:
	        		   
	        		   ipeerlistener.setMusicPlayerCmd("stop", "");
	        		   MusicService.invokeShutdown();
	        		   break;
	        	   case Utils.Cmd_Sleep:
	        		   
	        		   if(msg.arg1==1 && remoteservice!=null){
	        			 Utils.log("Init Vol start ......"+vol);
	  					 try {
	  						    remoteservice.jni_i2c_open();
	  						    
	  						    int[] buf={vol};
	  						    remoteservice.jni_i2c_write(0x07, buf, 1);
	  		
	  							remoteservice.jni_i2c_writedev(0x1a, 0x07, buf, 1);
	  							
		 					    //int index=Utils.getVarInt("heq");
		 					   // if(index==-1) index=8;
		 					//	remoteservice.jni_i2c_write(0x2F, Utils.HEQ[index], Utils.HEQ[index].length);
		 					//	remoteservice.jni_i2c_write(0x36, Utils.HEQ[index], Utils.HEQ[index].length);
		 					 //   index=Utils.getVarInt("leq");
		 					 //   if(index==-1) index=8;
		 					//	remoteservice.jni_i2c_write(0x2A, Utils.LEQ[index], Utils.LEQ[index].length);
		 					//	remoteservice.jni_i2c_write(0x31, Utils.LEQ[index], Utils.LEQ[index].length);	 						
		 				
		 						
		 					    
		        		        
		 						 int value=volmic1-1;
		 						 if(value<0) value=0;
		 						 setCaraOK(1, pt2258_channel1_vol[value], 0);
		 						
		 						 value=volmic2-1;
		 						 if(value<0) value=0;
		 						 setCaraOK(2, pt2258_channel2_vol[value], 0);
		 						 
		 						 value=vol3-1;
		 						 if(value<0) value=0;
		 						 
		 						 int value3=pt2258_channel3_vol[(5*value/13)];
		 						 setCaraOK(3, pt2258_channel4_vol[value], value3);

		 						 
		 						 
		 						 value=vol4-1;
		 						 if(value<0) value=0;
		 						 setCaraOK(4, pt2258_channel5_vol[value], 0);
		 						 
								
							} catch (RemoteException e) {
								e.printStackTrace();
							}
	        			    Utils.setLedSwitch(true);
	        			    MusicService.instance.musicsynch.mStart();
	        			   
	        		   }else if(msg.arg1==0){
	        			   
	        			   
	        			    
	            	    	MusicService.instance.pre_playlist.clear();
	            	    	MusicService.instance.musicsynch.mPause();
	            	    	MusicService.instance.musicplayer.onReset();
	            	    	Utils.setLedSwitch(false);
	            	    	Utils.sendSpeakerNotify(instance, "all", false);
	            	    	
	            	    	Intent home = new Intent(Intent.ACTION_MAIN);  
	            	    	home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            	    	home.addCategory(Intent.CATEGORY_HOME);   
	            	    	startActivity(home); 
	            	    	
	            	    	if(ismirror) ipeerlistener.onMirrorStop();
	            	    	
	            	        Utils.log("sleep ....");
	            	    	try {
								remoteservice.ioctrl("clearmemory", null);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
	            	    	mirror_package=null;
	            	    	if(!RunStatus.contains("mirrorapk")) RunStatus+=getMirrorStatus(mirror_package);
	        		   }

	        		   break;
	        		   
	        	   case Utils.Cmd_Sound_Mode:
	        		    
	        		   if(msg.arg1<=msg.arg2){
	        			   msg.arg1++;
	        		       PeerServerIP.setSpeakerVolume(msg.arg1); 
	        		       Message m=this.obtainMessage(Utils.Cmd_Sound_Mode, msg.arg1, msg.arg2);
	        		       this.sendMessageDelayed(m, 3000);
	        		   }
	        		   break;
	        		   
	        	   case Utils.Cmd_AP:
	        		    startHotAp();
	        		    break;
	        		
	        	   case Utils.Cmd_LED:
	        		   
	        		   // setLedMessage();
	        		    int status=msg.arg1;
	        		    Utils.setLedSwitch(status==1?true:false);
	        		    Message eg=obtainMessage(Utils.Cmd_LED, status==1?0:1, 0);
	        		    sendMessageDelayed(eg, 1000);
	        		    
	        		    break;
	        	   case Utils.Cmd_SECOND:
	        		   
	        		      
				        try {
				          if(remoteservice!=null && (ismirror || mirror_package!=null) ){
				        	remoteservice.keyCode(msg.arg1==1?KeyEvent.KEYCODE_MEDIA_NEXT:KeyEvent.KEYCODE_MEDIA_PREVIOUS);
				          }
				        } catch (RemoteException e) {
					      e.printStackTrace();
				        }
			            if(!ismirror && mirror_package==null) ipeerlistener.setMusicPlayerCmd(msg.arg1==1?"next":"pre", ""); 
	        		    break;
	        	 }

	        }  
	};

	
	public MusicServiceReceiver mBroadcastReceiver=null;
	public void createBroadcastReceiver(){
	    IntentFilter filter = new IntentFilter();  
	    
	    filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);  
	    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);  
	    filter.addAction(Intent.ACTION_MEDIA_REMOVED);  
	    filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
	    filter.addAction(Intent.ACTION_MEDIA_EJECT); 
	    filter.addAction(Utils.SPEAKER_TIME_CLOCK);
	    filter.addAction(PeerTools.SPEAKER_NOTIFY_STATUS);
	    filter.addAction("com.speaker.key");
	    filter.addAction("com.mirror.status"); 
	    filter.addAction("com.mele.KEY_DOWN_BC");
	    
	    
	    filter.addDataScheme("file");  
	    mBroadcastReceiver = new MusicServiceReceiver();  
	    registerReceiver(mBroadcastReceiver, filter); 
	}
	public void releaseBroadcastReceiver(){
		
		try{
			
			if(mBroadcastReceiver!=null){
				this.unregisterReceiver(mBroadcastReceiver);
				mBroadcastReceiver=null;
			}
		
		}catch (IllegalArgumentException e) { 
			
	        if (e.getMessage().contains("Receiver not registered")) {  
	            // Ignore this exception. This is exactly what is desired  
	        } else {  
	        	// unexpected, re-throw   
	            throw e;  
	        }  
	    } 
	}
	

	public void setTimerClock(TimeClock tc){
		
		
		
		Intent i = new Intent(Utils.SETTING_TIME_CLOCK);
		i.putExtra("id", tc.getId());
		i.putExtra("time", tc.getTime());
		i.putExtra("timeperiod", tc.getTimeperiod());	
		sendBroadcast(i);
		
		

	}
	
	public void initVol(){
		PeerServerIP.initS805Vol();
	}
	
	
	public void systemUpdate(int var, int ret){
		
		  if(var==0){
			  
			  if(ret==1){
				  
				  if(!RunStatus.contains("sys:new")){
					  
					  
					  RunStatus+="sys:new;;";
					  
				  }
				  
				  
			  }else{
				  if(!RunStatus.contains("sys:isnew")){
					  
					  
					  RunStatus+="sys:isnew;;";
					  
				  }
			  }
			  
		  }else if(var==1){
			  
			  if(!RunStatus.contains("sys:ok")){
							  
				  RunStatus+="sys:ok;;";
                  
			  }
			  
			  
		  }
		
		
	}
	public void cancelTimeClock(TimeClock tc){
		
		Intent i=new Intent("com.farcore.alarmintent");
		PendingIntent pi = PendingIntent.getBroadcast(this, Integer.parseInt(tc.getId()) , i, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(pi);
		
	}
	
	public KeyUtil.onKeyClick keyclick=new KeyUtil.onKeyClick(){

		@Override
		public void onKeyClick(int keycode, int action) {
			
			        Utils.log("====>keycode"+keycode+" action"+action);
		            if(keycode==502 ){
		            	
		            	   if(action==1){
		            		   
		            		   handler.obtainMessage(Utils.Cmd_Time_Key, -1, 0).sendToTarget();
		            		   
		            	   }else if(action==2){
		            		   
		            		   handler.obtainMessage(Utils.Cmd_SECOND, -1, 0).sendToTarget();
		            		   
		            	   }else if(action==3){
		            		   handler.obtainMessage(Utils.Cmd_Time_Key, -1, 1).sendToTarget(); 
		            	   }
		            	
		            	
		            	
		            	
		            }else if(keycode==501 ){
		            	
		            	   if(action==1){
		            		   handler.obtainMessage(Utils.Cmd_Time_Key, 1, 0).sendToTarget();
		            	   }else if(action==2){
		            		   
		            		   handler.obtainMessage(Utils.Cmd_SECOND, 1, 0).sendToTarget();
		            		   
		            	   }else if(action==3){
		            		   handler.obtainMessage(Utils.Cmd_Time_Key, 1, 1).sendToTarget();
		            	   }
		            	
		            	
		            	
		            }
			
		}
		
	};
	class MusicServiceReceiver extends BroadcastReceiver {  
		  
		public MusicForm mkeyform=null;
		public KeyUtil keyutil=null;
		public int M=0;
		
        public MusicServiceReceiver() {
			super();
			keyutil=new KeyUtil(instance, "");
			keyutil.onkeyclick=keyclick;
		}


		@Override  
        public void onReceive(Context context, Intent intent) {
        	
        	String path=intent.getData().getPath();
        	String action=intent.getAction();
        	Message msg=null;
        	handler.removeMessages(Utils.Cmd_Time_DAR);
        	
        	//log("Receiver===="+path+" action "+action);
        	//handler.removeMessages(Utils.Cmd_SDCard);
            if (action.equals("android.intent.action.MEDIA_MOUNTED")){ 
            	
                Toast.makeText(getApplicationContext(),  "MEDIA_MOUNTED",   Toast.LENGTH_SHORT).show();  
                msg=handler.obtainMessage(Utils.Cmd_SDCard, 1, 0, path);
                handler.sendMessageDelayed(msg, 2000);
                
                
            }else if (action.equals("android.intent.action.MEDIA_REMOVED")) { 
            	
                Toast.makeText(getApplicationContext(), "MEDIA_REMOVED", Toast.LENGTH_SHORT).show();  
                msg=handler.obtainMessage(Utils.Cmd_SDCard, -1, 0, path);
                handler.sendMessageDelayed(msg, 2000);
                
            } else if (action.equals("android.intent.action.MEDIA_UNMOUNTED")) { 
            	
                Toast.makeText(getApplicationContext(), "MEDIA_UNMOUNTED",  Toast.LENGTH_SHORT).show();
                msg=handler.obtainMessage(Utils.Cmd_SDCard, -1, 0, path);
                handler.sendMessageDelayed(msg, 2000);
                
            } else if (action.equals("android.intent.action.MEDIA_BAD_REMOVAL")) {  
                Toast.makeText(getApplicationContext(), "MEDIA_BAD_REMOVAL", Toast.LENGTH_SHORT).show();  
            } else if (action.equals("android.intent.action.MEDIA_EJECT")) { 
            	
            	
            	for(int i=0; i<Utils.sqlitehelper.extern_db_list.size(); i++){
            		SQLiteDatabase db=Utils.sqlitehelper.extern_db_list.get(i);
	            	if(db!=null && db.getPath().contains(path)){
	            		Utils.log("====> Close sdcard:"+db.getPath());	            	   
	            		db.close();
//	            		try {
//							FileOutputStream outStream = new FileOutputStream(new File(db.getPath()));
//							outStream.flush();
//							outStream.close();
//	            		} catch (IOException e) {
//							e.printStackTrace();
//						} 
	            	    db=null;
	            	    Utils.sqlitehelper.extern_db_list.remove(i);
	            	}
            	}
            	musicplayer.onReset();
            	pre_playlist.clear();
            	musicsynch.mStart();
            	
                Toast.makeText(getApplicationContext(), "Close sdcard MEDIA_EJECT", Toast.LENGTH_SHORT).show();
                msg=handler.obtainMessage(Utils.Cmd_SDCard, -1, 0, path);
                handler.sendMessageDelayed(msg, 2000);
            } else if (action.equals(Utils.SPEAKER_TIME_CLOCK)){
            	String id=intent.getStringExtra("id");
            	log("Utils.SPEAKER_TIME_CLOCK ID:...."+id);
   			    LinkedList<TimeClock> list=Utils.sqlitehelper.queryAllClockTime(Utils.sqlitehelper.sdcard_db, null);
	    	 
	   			for(int i=0; i<list.size(); i++){
	   				 if(id.equals(list.get(i).getId()) ){
	   					 if( Utils.getTimeDiffer(list.get(i).getTime())>1000*60*2){
	   						 setTimerClock(list.get(i));
	   						 return ;
	   					 }
	   					 if(list.get(i).getTimeperiod().equals("")){
	   						Utils.sqlitehelper.delete(Utils.sqlitehelper.sdcard_db, list.get(i));
	   						log("delete timeClock "+ list.get(i).getName());
	   					 }
	   					 handler.obtainMessage(Utils.Cmd_Time_Clock, list.get(i).getMuisformid()).sendToTarget();	   					 
	   					 setTimerClock(list.get(i));
	   					 String dur=list.get(i).getDuration();
	   					 if(dur==null || dur.equals("")) return ;
	   					 int duration=-1;
	   					 if(dur.equals("5分钟")){
	   						duration=5*60*1000;
	   					 }else if(dur.equals("10分钟")){
	   						duration=10*60*1000; 
	   					 }else if(dur.equals("20分钟")){
	   						duration=20*60*1000;
	   					 }else if(dur.equals("30分钟")){
	   						duration=30*60*1000; 
	   					 }else if(dur.equals("60分钟")){
	   					   duration=60*60*1000;
	   					 }
	   					 if(duration!=-1){
	   					   Utils.log("TimeClock duration "+duration);
	   					   msg=handler.obtainMessage(Utils.Cmd_Time_DAR);
	   					   handler.sendMessageDelayed(msg, duration);
	   					 }
	   					 break;
	   				 }
	   			}
            }else if(action.equals(PeerTools.SPEAKER_NOTIFY_STATUS)){ 
                 String pclass=intent.getStringExtra("class");
            	 if(pclass!=null && !pclass.equals("musicservice"))
            	     musicplayer.onPause();
            	 if(pclass!=null && !pclass.equals("mirror")){
            		 
            		 if( !RunStatus.contains("mirror") ) RunStatus+="mirror:stop;;";
		     	     try {
		     	    		if(remoteservice!=null) remoteservice.ioctrl("clearmemory", null);
					 } catch (RemoteException e) {
								e.printStackTrace();
					 }
		     	     mirror_package=null;
		     	    if(ismirror){
		     	    	ipeerlistener.onMirrorStop();
		     	    }
		     	    if(!RunStatus.contains("mirrorapk")) RunStatus+=getMirrorStatus(mirror_package);
            	 }
            }else if(action.equals("com.mirror.status")){
            
            	 final String ip=intent.getStringExtra("ip");
            	 new Thread(new Runnable(){

					@Override
					public void run() {
								try {
									if(remoteservice!=null) remoteservice.stopMirror();
								} catch (RemoteException e) {
									e.printStackTrace();
								}
								remoteservice=null;
								

								
								Intent i  = new Intent("com.mele.musicdaemon.RemoteService");	
								bindService(i, connection, Context.BIND_AUTO_CREATE);
							    while(remoteservice==null){
								     try {
										 Thread.sleep(200);
									} catch (InterruptedException e) {					
										e.printStackTrace();
									}
							    }

							    if(!ismirror) {
							    	log("stop mirror .........");
							    	return ;
							    }
								//ismirror=true;
								try {
									if(remoteservice!=null) remoteservice.startMirror(ip, videoport);
								} catch (RemoteException e) {
									e.printStackTrace();
									ismirror=false;
								}
					}
            		 
            	 }).start();

						
            }else if(action.equals("com.speaker.key")){
            	  
            	  int keycode=intent.getIntExtra("keycode", -1);
            	  int value=intent.getIntExtra("action", -1);
            	  
            	  if(keycode!=502 && keycode!=501 && keycode!=204 && keycode!=206 && keycode!=116 &&value==1){
            		  setLedMessage();
            	  }
            	  Utils.log("keycode "+keycode+" value "+value);
            	  if(value!=2) keyutil.dispatchKeyEvent(new KeyEvent(0, 0, value==1?0:1, keycode, 0));
            	  
            	  if( (keycode==14  || keycode==559) && value==1){
            		  MusicService.instance.onCaraOKUIDg(true); 
            	  }
            	  
            	  if( ( keycode==558 || keycode==137) && value==1){
            	
            		     setAudioOutMode(inputmode);
            		     if(inputmode==0){
            		    	 showmsg("普通模式");
            		    	 inputmode=1;
            		     }else if(inputmode==1){
            		    	 showmsg("卡拉OK模式");
            		    	 inputmode=2;
            		     }else if(inputmode==2){
            		    	 showmsg("AUX_IN模式");
            		    	 inputmode=0;
            		     }
            		     
            		     Utils.log("inputmode "+inputmode);
            	  }else if(keycode==502 ){//
      		  
            		  if(value==0) handler.obtainMessage(Utils.Cmd_Time_Key, 1, -1).sendToTarget();
            		  
            	  }else if(keycode==105   && value==0){//&& !Utils.isHDMIPlugged()
	   				   try {
						   if(remoteservice!=null && (ismirror || mirror_package!=null) )remoteservice.keyCode(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
					   } catch (RemoteException e) {
						  e.printStackTrace();
					   }
            		 //  if(!ismirror  && mirror_package==null) ipeerlistener.setMusicPlayerCmd("pre", "");
            	  }else if(keycode==501 ){//
            		  

            		  if(value==0) handler.obtainMessage(Utils.Cmd_Time_Key, 1, -1).sendToTarget();
            		  
            	  }else if(keycode==106&& value==0){// && !Utils.isHDMIPlugged()  下一首
      				  try {
   					     if(remoteservice!=null && (ismirror || mirror_package!=null) ){
 	            	    	
 							  //remoteservice.ioctrl("clearmemory", mirror_package);
   						      remoteservice.keyCode(KeyEvent.KEYCODE_MEDIA_NEXT);
   					     }
					  } catch (RemoteException e) {
						  e.printStackTrace();
					  }
      				  
      				// if(!ismirror && mirror_package==null) ipeerlistener.setMusicPlayerCmd("next", "");
      				 
      				 
                  }else if( (keycode==206  ) && value==0){//播放按键
                	  
                	  
            		  String status=musicplayer.getMusicPlayState();
            		  status=musicplayer.doPlayStateInfo(status, "state");
                	  
                	  if(ismirror || mirror_package!=null || status==null || status.equals("") || status.equals("end") || status.equals("finish") || status.equals("pause") ) {
                		  if(isPackageTop("com.androidm.music") || isPackageTop("com.android.music")){
		         				   try {
		           					   if(remoteservice!=null)remoteservice.keyCode(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
		        				  } catch (RemoteException e) {
		        						  e.printStackTrace();
		        				  }
		                		  return ;
                		  }
                	  }
                	  
                	  

            		  
            		  if(status==null || status.equals("") || status.equals("end") || status.equals("finish")){
            			  
            			  if(isPackageTop("com.androidm.music") || isPackageTop("com.android.music")) return ;
            			  
       		    	      if(pre_playlist.size()==0){
	            			  for(int j=0; j<musiclist.size(); j++){
	 
	       		    	    	  pre_playlist.add(musiclist.get(j).getPath());
	 
	 		    	          }
            			  }         			  
            			  if(pre_playlist.size()>0) {
            				  try {
	            				  
	            				  String p=Utils.getVar("playpath");
	            				  if(!p.equals("0") && pre_playlist.indexOf(p)!=-1){
	            					  playlist.put(p);
	            				  }else{
	            					  p=getAutoPlayMusic();
	      							  if(p!=null) playlist.put(p);			
	            				  }
    				  		 } catch (InterruptedException e) {						
								e.printStackTrace();
							 }
            				  
            				  
            			  }
            			  
            			  
            		  }else{
            			  if( (isPackageTop("com.androidm.music")||isPackageTop("com.android.music")) && status.equals("pause")) return ;
            			  musicplayer.onStartorPause();
            		  }
            		     
            		  if(musicplayer.onGetPlayerStatus()){
            			   Utils.sendSpeakerNotify(instance, "musicservice", true);
            			   Utils.sendSpeakerNotify(instance, "musicservice", false);
            		  }
            		  
            		  
            	  }else if(( keycode==97  ) && value==0){//
            		  
            			try {
            				if(instance.remoteservice!=null) instance.remoteservice.keyCode(KeyEvent.KEYCODE_DPAD_CENTER);
            			} catch (RemoteException e) {
            				e.printStackTrace();
            			}
                  
            	  }else if( keycode==204 && value==1){
            		  
            		  time_start=SystemClock.uptimeMillis();
        			  msg=handler.obtainMessage(Utils.Cmd_AP);
        			  handler.sendMessageDelayed(msg, 5000);
            		  
            	  }else if( keycode==204 && value==0){

        			  long log=SystemClock.uptimeMillis()-time_start;
        			  if(log>5000){        				  
        				   //startHotAp();
        			  }else{
        				   handler.removeMessages(Utils.Cmd_AP);
        				   doMKey(); 
        			  }
            		 
            	  }else if( keycode==113 && value==0){

            		  if(!ismirror)// doMKey(); 
    					 try {
      						
    							
      						if( M>=0 && M<=2) {
      						   remoteservice.jni_i2c_writedev(0x1a, 0x2C, Utils.LOWL[M], Utils.LOWL[M].length);
      						   remoteservice.jni_i2c_writedev(0x1a, 0x33, Utils.LOWR[M], Utils.LOWR[M].length);
      						   String str="";
      						   if(M==0){
      						      str="动感";
      						      seek5.setProgress(18);
      						   }else if(M==1){
       						      str="标准";
       						      seek5.setProgress(15);
       						   }else if(M==2){
        						  str="轻柔";
        						  seek5.setProgress(11);
        					  }
      						   showmsg(str);
      						   
      						}
      					     M++;
      					     if(M>2) M=0;
      						log("M "+M);
      						
      					 } catch (RemoteException e) {
      						
      						e.printStackTrace();
      					 } catch (NumberFormatException e){
      						 
      					 }
            		  
            		  
            	  }else if(keycode==104 && value==0){//vol up
            		 // if(value==0) handler.obtainMessage(Utils.Cmd_Time_Key, 1, -1).sendToTarget();           		  
            		  //MusicService.instance.ipeerlistener.setVolTAS5711(PeerServerIP.getRemoteVolume()+1);
            		  setOutVolume(true);
                    		  
            	  }else if(keycode==109 && value==0){//vol down
            		//  if(value==0) handler.obtainMessage(Utils.Cmd_Time_Key, 1, -1).sendToTarget();
            		 // MusicService.instance.ipeerlistener.setVolTAS5711(PeerServerIP.getRemoteVolume()-1);
            		  setOutVolume(false);
            		  
            		  
            	  }
            	  
          
            }
  
        }  
  
    }
	public void setAudioOutMode(int mode){
		
			  if(mode==1){//caraok
				  
					int p1buf[]={0xff}, p0buf[]={0xfd};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x03, p1buf, 1);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x02, p0buf, 1);
						
						MusicService.instance.remoteservice.jni_i2c_writedev(0x1a, 0x2a, Utils.L_OK[0], 20);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x1a, 0x31, Utils.L_OK[1], 20);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
				    
			  }else if(mode==0){//gener
					int p1buf[]={0xfb}, p0buf[]={0xfd};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x03, p1buf, 1);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x02, p0buf, 1);
						
						MusicService.instance.remoteservice.jni_i2c_writedev(0x1a, 0x2a, Utils.L_NOOK[0], 20);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x1a, 0x31, Utils.L_NOOK[1], 20);
					} catch (RemoteException e) {
						e.printStackTrace();
					} 
				
					
			  }else if(mode==2){//anxin
				  
				  
				
					int p1buf[]={0xff}, p0buf[]={0xff};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x03, p1buf, 1);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x02, p0buf, 1);
						
						MusicService.instance.remoteservice.jni_i2c_writedev(0x1a, 0x2a, Utils.L_NOOK[0], 20);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x1a, 0x31, Utils.L_NOOK[1], 20);
					} catch (RemoteException e) {
						e.printStackTrace();
					} 
			  }
	}
	public int setOutVolume(boolean flag){
		if(remoteservice==null)  return 20;
		
		int i=0;
		int buf[]=null;
		try {
			buf=remoteservice.jni_i2c_read(0x07, 1);
			
			for(i=0; i<Utils.out_Volume.length; i++){
				     
				   if(Utils.out_Volume[i]==buf[0]){
					   break;
				   }				
			}
			
			i=flag?(i+1):(i-1);
			if(i>=32){	
				i=32;
			}else if(i<0){
				i=0;
			}

			this.vol=Utils.out_Volume[i];
			if( !RunStatus.contains("vol") ) RunStatus+="vol:"+i+";;";
			else{
				
				String items[]=RunStatus.split(";;");
				RunStatus="";
				for(int j=0; j<items.length; j++){
					
					   if(!items[j].contains("vol")){
						   RunStatus+=items[j]+";;";
					   }else{
						   RunStatus+="vol:"+i+";;";
					   }
					   
				}
				
			}

			//Utils.log("RunStatus ...."+RunStatus);
			log("setOutVolume index:"+i+" value:"+ buf[0]);
			Utils.setVar("vol", this.vol);
			showmsg(""+i);
			buf[0]=Utils.out_Volume[i];
	
			remoteservice.jni_i2c_write(0x07, buf, 1);
			if(i==0){
				remoteservice.jni_i2c_writedev(0x1a, 0x07, buf, 1);
				return i;
			}
			
			int index=vol5-16+i;
			if(index<=0) index=0;
			else if(index>=32) index=31;
			buf[0]=Utils.out_Volume[index];
			remoteservice.jni_i2c_writedev(0x1a, 0x07, buf, 1);
			
			
			
			//vol5=i;
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		
		
		return i;
	}
	public int getVolume(){
		
		if(remoteservice==null)  return 20;
		
		int i=0;
		int buf[]=null;
		try {
			buf=remoteservice.jni_i2c_read(0x07, 1);
			
			for(i=0; i<Utils.out_Volume.length; i++){
				     
				   if(Utils.out_Volume[i]==buf[0]){
					   return i;
				   }				
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 20;
		
	}
	public void setVolume(int Vol){
		
		 
		try{	
			if(Vol>32){	
				Vol=32;
			}else if(Vol<0){
				Vol=0;
			}
			this.vol=Utils.out_Volume[Vol];
			if( !RunStatus.contains("vol") ) RunStatus+="vol:"+Vol+";;";
			else{
				
				String items[]=RunStatus.split(";;");
				RunStatus="";
				for(int j=0; j<items.length; j++){
					
					   if(!items[j].contains("vol")){
						   RunStatus+=items[j]+";;";
					   }else{
						   RunStatus+="vol:"+Vol+";;";
					   }
					   
				}
				
			}

			
			Utils.setVar("vol", this.vol);
			int buf[]={Utils.out_Volume[Vol]};
			log("setOutVolume index:"+Vol+" value:"+ buf[0]);
			remoteservice.jni_i2c_write(0x07, buf, 1);
			remoteservice.jni_i2c_writedev(0x1a, 0x07, buf, 1);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}

	public void doMKey(){
	    log("MIndex "+MIndex);


	    if(MIndex==-1){
	    	
	    	String ret=Utils.rwAudioIO(null);
	   
	    	if(ret!=null && ret.equals("0")){
	    		
	    		Utils.rwAudioIO("1");
	    		
	    		
	    	}else{
	    		
	    		musicplayer.onStart();
	    		//musicplayer.onPause();
	    		Utils.rwAudioIO("0");
	    	}
	    	
	    	
	    	return ;
	    }
	    if(ismirror || mirror_package!=null) return ;
	    
	    randomlist.clear();
	    if(MIndex==0){//姝屽崟
	    	
	    	pre_playlist.clear();
	    	while(true) {
	    		mBroadcastReceiver.mkeyform=doMKeyMusicForm(mBroadcastReceiver.mkeyform);
	    		if(mBroadcastReceiver.mkeyform==null || MIndex==1 || pre_playlist.size()>0) break;
	    	}
	    	
	    	if(MIndex==1 && pre_playlist.size()==0){
	    		
	    		if(!doMKeySDcard()){
	    			
			        if(!doMKeyFavorite()){
			        	doMKeyLibrary();
			        	MIndex=0;
			        	//return ;
			        }else{
			        	MIndex=3;
			        }
	    			
	    			
	    			//MIndex=2;
	    		}else{
	    			MIndex=2;
	    		}
	    	}

		    
	    	
	    	 
	    }else if( MIndex==1 ){//SDcard
	    	
	    	if(!doMKeySDcard()){
	    		
		        if(!doMKeyFavorite()){
		        	doMKeyLibrary();
		        	MIndex=0;
		        	//return ;
		        }else{
		        	MIndex=3;
		        }
		        
	    	}else{
	    		MIndex=2;
	    	   
	    	}
	    	

	        
	    	
	    }else if(MIndex==2){//favorite
	       
	        if(!doMKeyFavorite()){
	        	doMKeyLibrary();
	        	MIndex=0;
	        	//return ;
	        }else{
	        	MIndex=3;
	        }
	        
	       // ddd
	       
	    }else if(MIndex==3){//姝屽簱
	        
	        doMKeyLibrary();
	        MIndex=0;
	        
	    }else{
	    	
	    	MIndex=0;
	    }
	    
	    
	    
	    if(pre_playlist.size()>0) {
	    	playlist.add(pre_playlist.get(0));
	    }else{
	    	musicplayer.onReset();
	    }

	}
	public MusicForm doMKeyMusicForm(MusicForm mform){
		
		    MusicForm mf=null;
		    
		    int index=-1;
		    musicformlist=Utils.sqlitehelper.queryAllMusicForm(Utils.sqlitehelper.sdcard_db, musicformlist);            		    
		    if(musicformlist.size()==0) { MIndex=1; return mf;}
		    
		    if(mform!=null){
		    	
		    	for(int n=0; n<musicformlist.size(); n++){
		    		   if(musicformlist.get(n).id.equals(mform.id)){
		    			   index=n;
		    			   break;
		    		   }
		    	}
		    }
		    index++;
		    if(index>=musicformlist.size()) index=0;
		    
		    
		    if(index==musicformlist.size()-1){
		    	
		    	 MIndex=1;

		    }
//		    else{
//		    	 index=0;
//		    }
		    mf=musicformlist.get(index);
//		    log("姝屽崟&&&"+mf.form);
		    pre_playlist.clear();
		    for(int i=0; i<mf.idlist.size(); i++){
		    	     
		    	   for(int j=0; j<musiclist.size(); j++){
		    		      if(musiclist.get(j).getId().equals( mf.idlist.get(i) )){
		    		    	  
		    		    	   log(mf.form+": "+musiclist.get(j).getName());
		    		    	   pre_playlist.add(musiclist.get(j).getPath());
		    		    	  
		    		    	  
		    		    	   break;
		    		      }

		    	   }

		    }
		   
		   if( pre_playlist.size()>0) {
			   
			   for(int i=1; i<=5; ++i){
				   if(mf.form.contains(""+i)){
					    playSoundPool.play(i, 0); 
					    break;
				   }
			   }
			   
		   }
	
		   return mf;
	}
	public boolean doMKeySDcard(){
		Utils.log("MKey doMKeySDcard");  
    	sdcardlist.clear();
    	pre_playlist.clear();
	    for(int i=0; i<musiclist.size();i++){
		        if(musiclist.get(i).getPath().contains("/storage/udisk")){
		  
					sdcardlist.add(musiclist.get(i));
                 
		         }
		      
	    }
	    
	    if(sdcardlist.size()==0) return false;
	    
	    for(int i=0; i<sdcardlist.size();i++){
			  	 
    			  
					pre_playlist.add(sdcardlist.get(i).getPath());
 
		      
	    }
	    playSoundPool.play(6, 0);   
		return true;
	}
	
	public boolean doMKeyFavorite(){
		  Utils.log("MKey doMKeyFavorite");  
		  pre_playlist.clear();
		  String ret=Utils.sqlitehelper.queryMusicFavorites(Utils.sqlitehelper.sdcard_db); 
		  if(ret==null || ret.equals("")) return false;
		  
		  String idlist[]=ret.split(">");
		  if(idlist==null ) return false;
		  
		  for(int i=0; i<idlist.length; i++){
			    
	    	   for(int j=0; j<musiclist.size(); j++){
	    		      if(musiclist.get(j).getId().equals( idlist[i] )){
	    		    	  
	    		    	   log("收藏夹:"+musiclist.get(j).getName());
	    		    	   pre_playlist.add(musiclist.get(j).getPath());
	    		    	  
	    		    	  
	    		    	   break;
	    		      }

	    	   }
			  
		  }
		  if( pre_playlist.size()==0) return false;
		  playSoundPool.play(7, 0);  
		  return true;
	}
	
	public boolean doMKeyLibrary(){
		  Utils.log("MKey doMKeyLibrary");  
	      pre_playlist.clear();
		  for(int j=0; j<musiclist.size(); j++){
	
		       if(!musiclist.get(j).getPath().contains("/storage/external_storage/sda"))
			      pre_playlist.add(musiclist.get(j).getPath());
	              //Utils.log(musiclist.get(j).getPath());
	      }
		        			  
		  if(pre_playlist.size()>0) {
			  playSoundPool.play(8, 0);
		  }
		  return true;
	}
	
	public void startHotAp(){
		   
		   // String ssid=wifiadmin.getSSID();
		   // if(ssid!=null && !ssid.equals("")){
		    //	wifiadmin.disconnectWifi();
		    //	
		  //  }
            //setLedMessage();
	}
	
	public void startHotApLed(int mode){
		
		   if(mode==1){
			   handler.obtainMessage(Utils.Cmd_LED, 0, 0).sendToTarget();
		   }else{			   
			   handler.removeMessages(Utils.Cmd_LED);
			   Utils.setLedSwitch(true);
		   }
		
	}
	
	public void setLedMessage(){
		
		    Utils.setLedSwitch(false);
		    handler.postDelayed(new Runnable(){
	
				@Override
				public void run() {	
					Utils.setLedSwitch(true);
		        }
		    }, 200);
	}
	public boolean isPackageTop(String packagename){
	
//		    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
//		    List<RunningTaskInfo> runningTasks = manager .getRunningTasks(1);  
//		    RunningTaskInfo cinfo = runningTasks.get(0);  
//		    ComponentName component = cinfo.topActivity;  
//		    Log.e("currentactivity", ">>>>>>>>>"+component.getClassName());
		
		  
//		    if(component.getPackageName().equals(packagename)){
//		    	//return true;
//		    }
		    
		    String pkg=getTaskPackname();
		    if(pkg==null || pkg.equals("")){
		       return true;
		    }else if(pkg.toLowerCase().contains("launcher")){
		    	return false;
		    }else{
		    	return true;
		    }
		    
		    
		    
	}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NewApi")
    private String getTaskPackname() {
    	
    	 
//    	if (isNoOption()){
//    		    	Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  
//    	            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent); 
//    	}
//    	

        
        String currentApp = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }else{
		    	Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  
	            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent); 
            }
            
            
        } else {
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        Log.e("TAG", "Current App in foreground is: " + currentApp);
        return currentApp;
    }
    private boolean isNoOption() {  
        PackageManager packageManager = getApplicationContext()  
                .getPackageManager();  
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,  
                PackageManager.MATCH_DEFAULT_ONLY);  
        return list.size() > 0;  
    }  
	public RemoteServiceAIDL remoteservice=null;
	public IServiceConnection connection = new IServiceConnection();
	
	private class IServiceConnection implements ServiceConnection{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "RemoteServiceAIDL 成功连接...");
			remoteservice = RemoteServiceAIDL.Stub.asInterface(service);
            


		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "RemoteServiceAIDL 断开...");
			remoteservice=null;
		}

	}
		
	@Override
	 public void onConfigurationChanged(Configuration newConfig) {
	
		  log("onConfigurationChanged >>>>"+newConfig.orientation);
		  if(newConfig.orientation == 2){
	               
			  RunStatus+="landscape"+";;";
			  
		  }else if(newConfig.orientation == 1){
			  RunStatus+="portrait"+";;";
		  }
		  super.onConfigurationChanged(newConfig);
    }	

    public void log(String msg){
    	Log.i(TAG, msg);
    }

}
