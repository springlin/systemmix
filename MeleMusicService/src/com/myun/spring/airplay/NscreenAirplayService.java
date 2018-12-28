/**
 * 
 */
package com.myun.spring.airplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import ni.network.airplay.AirplayFactory;
import ni.network.airplay.IAirplayAudioImpl;
import ni.network.airplay.IAirplayEventSink;
import ni.network.airplay.IAirplayImpl;
import ni.network.airplay.raop.AirPlayServer;
import ni.network.airplay.raop.RAOPThread;
import ni.network.plist.NSArray;
import ni.network.plist.NSDictionary;
import ni.types.Vector3f;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
//import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.widget.Toast;




















import com.myun.net.protocol.PeerTools;
import com.myun.utils.Utils;
import com.myun.core.R;

/**
 * @author Administrator
 * 
 */
public class NscreenAirplayService extends Service implements IAirplayImpl, IAirplayEventSink {
	public final String TAG = "AirPlayServer";

	
	public static final String ETHERNET_STATE_CHANGED_ACTION = "android.net.ethernet.ETHERNET_STATE_CHANGED";
	public static final String EXTRA_ETHERNET_STATE = "ethernet_state";
	public static final String ETHERNET_IFACE_STATE_CHANGED_ACTION = "android.net.ethernet.ETHERNET_IFACE_STATE_CHANGED";
	public static final String EXTRA_ETHERNET_IFACE_STATE = "ethernet_iface_state";
	public static final String ETHERNET_LINK_STATE_CHANGED_ACTION = "android.net.ethernet.ETHERNET_LINK_STATE_CHANGED";
	public static final String EXTRA_ETHERNET_LINK_STATE = "ethernet_link_state";
	    
    public int ethCurrentState = ETHER_STATE_DISCONNECTED;
    public int ethCurrentIfaceState = ETHER_IFACE_STATE_DOWN;
    public static final int ETHER_STATE_DISCONNECTED=0;
    public static final int ETHER_STATE_CONNECTING=1;
    public static final int ETHER_STATE_CONNECTED=2;
    public static final int ETHER_IFACE_STATE_DOWN = 0;
    public static final int ETHER_IFACE_STATE_UP = 1;
    public static final int ETHER_LINK_STATE_DOWN = 0;
    public static final int ETHER_LINK_STATE_UP = 1;    
	    
	private final int WIFI = 0;
	private final int ETHERNET = 1;
	private int networkMode = ETHERNET; 
	
	
	WifiManager.MulticastLock lock;
	String macAddress;
	AndroidAudioTrack audioTrack;


	private RAOPThread mRARaopThread;



	private WifiManager wifiManager;
	private WifiInfo wifiInfo;
	
	private BroadcastReceiver mReceiver = null;
	private Handler myHandle = new MyHandle();

	private boolean isFirst;
	private AudioManager audioManager;
	private int programType = 1; // 0: video; 1: audio

	

	private int playDuration;
	private int playPosition;
	private float videoRate = 1.0f;

	private String lastSessionId = "";
	public byte[] mPixel=new byte[1280*720*4];
    public int Errcount=0;
	
	
	public FileOutputStream fout=null;
	public static NscreenAirplayService aas=null;
	public static boolean run_fg=false;
	
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();
		
		init();
		register();
		
		isFirst = true;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		 Notification notification = new Notification(R.drawable.ic_launcher, "netservice is running",System.currentTimeMillis());
		 PendingIntent pintent=PendingIntent.getService(this, 0, new Intent("com.myun.musicservice"), 0);
		 notification.setLatestEventInfo(this, "AirPlay Service","netservice is running", pintent);

		 startForeground(20, notification);
		 
		 return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (lock != null) {
			lock.release();
		}
		unregister();
		
	    Intent localIntent = new Intent();
        localIntent.setClass(this, NscreenAirplayService.class);  
        startService(localIntent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	

	

    

    

	private void init() {

		wifiManager = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		lock = wifiManager.createMulticastLock("bonjourAirplay");
		lock.setReferenceCounted(true);
		lock.acquire();

		wifiInfo = wifiManager.getConnectionInfo();
		macAddress = wifiInfo.getMacAddress();
		audioTrack = new AndroidAudioTrack(this);
		ConnectivityManager con=(ConnectivityManager)getSystemService(Activity.CONNECTIVITY_SERVICE);  
		boolean wifi=con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting(); 
		boolean ethe=con.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).isConnectedOrConnecting(); 
	    if(wifi){
	    	networkMode=WIFI;
	    	Utils.log("networkMode=WIFI");
	    	myHandle.sendEmptyMessageDelayed(MSG_READY_SERVER, 1000);
	    }else if(ethe){
	    	networkMode=ETHERNET;
	    	Utils.log("networkMode=ETHERNET");
	    	myHandle.sendEmptyMessageDelayed(MSG_READY_SERVER, 1000);
	    }else{
	    	networkMode=ETHERNET;
	    	Utils.log("networkMode=ETHERNET");
	    }
		
		
		register();
	}

	
	private void register() {

		IntentFilter filter = new IntentFilter();
		
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(PeerTools.SPEAKER_NOTIFY_STATUS);
		filter.addAction("wifi_ap_start");
		filter.addAction("wifi_ap_stop");
		filter.addAction("android.net.ethernet.ETH_STATE_CHANGED");
		filter.addAction("android.net.ethernet.STATE_CHANGED");
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				//Log.i(TAG, "Airplay action=" + action);

				if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
					
				
						
						NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
						State networkState = networkInfo.getState();
						
						if (networkState == NetworkInfo.State.CONNECTED) {
							
							//Log.i(TAG, "--------isFirst=" + isFirst);
							//onNetworkStateChanged();
							//startServer();
							Log.i(TAG, "NETWORK CONNECTED");
							
						}  else if (networkState == NetworkInfo.State.DISCONNECTED) {
							
							//stopServer();
							Log.i(TAG, "NETWORK DISCONNECTED");
						}
						

				
					

				}else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
					
					SupplicantState supplicantState = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);	
	                if (supplicantState.equals(SupplicantState.COMPLETED)) {		                   
	                     Utils.log("WIFI SupplicantState.COMPLETED");  
	                         if(networkMode==ETHERNET)  stopServer();
	                        	 
	                         networkMode=WIFI;
	                 		 startServer();
	                 	 
	                 	 
	                } else if (supplicantState.equals(SupplicantState.DISCONNECTED)) {	
	                	 Utils.log("WIFI SupplicantState.DISCONNECTED");
	                	 if(networkMode==WIFI)   stopServer();
	                }  
				
				}else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {


				}else  if("android.net.ethernet.ETH_STATE_CHANGED".equals(action)) {
	            	 
	            	  
	            	  int state=intent.getIntExtra("eth_state", -1);
	            	  Utils.log(state+" eth --------->"+action);
	            	  
	            	  if(state==1){
	            		  if(networkMode==WIFI)  stopServer();
	            		  networkMode=ETHERNET;
	            		  startServer();

	            	  }else if(state==4){

	            		  if(networkMode==ETHERNET)  stopServer();
	            	  }
	            	
	            }else if (PeerTools.SPEAKER_NOTIFY_STATUS.equals(action)) {

					String var=intent.getStringExtra("class");

					if(mRARaopThread != null && var!=null && !var.equals("airplay")){
						mRARaopThread.stopRTSP();
						Utils.log("airplay .......stop");
					}
					
					
				}else if ("wifi_ap_start".equals(action)) {
					
					Log.i(TAG, "AIRPlay_HOT_AP_START ....... !!!!");
					//startServer();
					
				}else if ("wifi_ap_stop".equals(action)) {
					
					Log.i(TAG, "AIRPlay_HOT_AP_STOP ....... !!!!");
					//stopServer();
					
				}else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
	
					 
					 myHandle.removeMessages(MSG_SCREEN);
					 Message msg=myHandle.obtainMessage(MSG_SCREEN, 0, 0);
					 myHandle.sendMessageDelayed(msg, 3000);
					
					// stopServer();
					
				}else if (Intent.ACTION_SCREEN_ON.equals(action)) {
					
					 myHandle.removeMessages(MSG_SCREEN);
					 Message msg=myHandle.obtainMessage(MSG_SCREEN, 1, 0);
					 myHandle.sendMessageDelayed(msg, 3000);
					
					//if(isWifiConnect()) startServer();
					
				}
			}


		};

		registerReceiver(mReceiver, filter);

	}

	private void unregister() {
		if (mReceiver != null) {
			this.unregisterReceiver(mReceiver);
		}
	}


	
	public boolean checkConnection(Context context) {//网络是否连接
		boolean connected = false;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm != null) {
			NetworkInfo[] netInfo = cm.getAllNetworkInfo();
			for (NetworkInfo ni : netInfo) {
				if (ni.isConnected() && ni.isAvailable()) {
					connected = true;
					break;
				}
			}
		}
		Log.d(TAG, "checkConnection connected=" + connected);
		return connected;
	}
    public boolean checkNet(){
       try {
	
		    	for(final NetworkInterface iface: Collections.list(NetworkInterface.getNetworkInterfaces())) {
		    		if ( iface.isLoopback() ){
		    			continue;
		    		}
		    	
					if ( iface.isPointToPoint()){
							continue;
					}
				
		    		if ( ! iface.isUp()){
		    			continue;
		    		}
		    		
		        	ArrayList<InetAddress> list=Collections.list(iface.getInetAddresses());
		        	//android.util.Log.d(TAG, list.size()+"=====");
		            if(list.size()==1) return false;
		
		    	}
    	
	 	}catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
	private void startServer() {
		

		if(mRARaopThread == null ){//&& checkNet()
			
			android.util.Log.d(TAG, "startServer startRAOP");
			mRARaopThread = AirplayFactory.startRAOP(this, getDeviceIDFullName(this));
		}else{
			android.util.Log.d(TAG, "startServer isRunning .......");
		}
		
		android.util.Log.d(TAG, "startServer DONE");

	}

	private void stopServer() {
		isFirst = true;
		android.util.Log.d(TAG, "stopAirPlayServer");
		
		

		
		if(mRARaopThread != null){
			mRARaopThread.stopThread();
			mRARaopThread = null;
		}
		//android.os.Process.killProcess(android.os.Process.myPid());
		android.util.Log.d(TAG, "stopServer DONE");
	}

	public static String getDeviceIDFullName(Context context) {
		return PeerTools.getDeviceName(context);//com.qvod.nscreen.core.PeerTools.getDeviceID(context);
	}
	

	@Override
	public void eventPlayVideo(String sessionId, final String url, final float startPos) {
		this.lastSessionId = sessionId;
		Message msg = new Message();
		msg.what = MSG_SHOW_VIDEO;
		msg.obj = new String[] { url ,String.valueOf(startPos)};
		myHandle.sendMessage(msg);
	}

	@Override
	public String getMacAddress() {
		return macAddress;
	}

	@Override
	public IAirplayAudioImpl getAudio() {
		return audioTrack;
	}

	@Override
	public IAirplayEventSink getEventSink() {
		return this;
	}

	@Override
	public float getPlayDuration() {
		Log.i("AIRPLAY", "----getPlayDuration=" + playDuration);
		return playDuration / 1000;
	}

	@Override
	public float getPlayPosition() {
		Log.i("AIRPLAY", "----getPlayposition=" + playPosition);
		return playPosition / 1000;
	}

	@Override
	public void getPlayerInfo() {
		
	}

	@Override
	public void stopApp() {
		// TODO Auto-generated method stub
		Log.e(TAG, TAG + " stopApp");
		
		Utils.sendSpeakerNotify(this, "airplay", true);
		Utils.sendSpeakerNotify(this, "airplay", false);
	}
	@Override
	public void ReStartService(){
		stopServer();
	}
	@Override
	public void showTips() {
		myHandle.sendEmptyMessageDelayed(MSG_PLAY_MUSIC, 6000);
	}

	@Override
	public boolean eventGetPlaybackInfo(String sessionId, NSDictionary dic) {
		this.lastSessionId = sessionId;
		
		fillPlaybackInfo(dic, true, getPlayDuration(), 0, getPlayPosition(), true);

		return true;

	}



	@Override
	public boolean eventScrub(String sessionId, float position) {
		this.lastSessionId = sessionId;
		playPosition = (int) position * 1000;
		
		return true;
	}

	@Override
	public void eventNewReversedConnection(String aPurpose, String aSessionId) {
		this.lastSessionId = aSessionId;
	}



	@Override
	public String getPhotoOutputDirectory() {
		return Constant.BASE_PHOTO_PATH;
	}



	@Override
	public void eventSetVideoRate(String sessionId, float aVideoRate, int reason) {
		this.lastSessionId = sessionId;
		

	}

	@Override
	public void eventSetVolume(String sessionId, float volume) {
		this.lastSessionId = sessionId;
		
	}

	void fillPlaybackInfo(NSDictionary dic, boolean isReady, float duration, float cacheDuration, float position, boolean isPlaying) {
		if (!isReady) {
			dic.put("readyToPlay", false);
		} else {
			dic.put("duration", duration);
			dic.put("position", position);
			// dic.put("rate", getPlayRate());
			dic.put("rate", videoRate);
			dic.put("readyToPlay", true);
			// buffered infos
			{
				NSDictionary dicLoadedTimeRanges = new NSDictionary();
				// dicLoadedTimeRanges.put("duration", cacheDuration);
				dicLoadedTimeRanges.put("duration", duration);
				dicLoadedTimeRanges.put("start", 0.0);
				dic.put("loadedTimeRanges", new NSArray(dicLoadedTimeRanges));
			}
			// seekable infos
			{
				NSDictionary dicSeekableRanges = new NSDictionary();
				dicSeekableRanges.put("duration", duration);
				dicSeekableRanges.put("start", 0.0);
				dic.put("seekableTimeRanges", new NSArray(dicSeekableRanges));
			}
			// playback infos
			{
				dic.put("playbackBufferEmpty", true);
				dic.put("playbackBufferFull", false);
				dic.put("playbackLikelyToKeepUp", true);
			}
		}
	}
	
	





	
	
	public static final int MSG_SHOW_VIDEO = 0x0001;
	public static final int MSG_SHOW_IMAGE = 0x0002;
	public static final int MSG_READY_SERVER = 0x0003;
	public static final int MSG_VIDEO_PLAY = 0x0004;
	public static final int MSG_VIDEO_PAUSE = 0x0005;
	public static final int MSG_PLAY_MUSIC = 0x0006;
	public static final int MSG_IS_CREATE = 0x0007;
	public static final int MSG_LOAD_PHOTO_TIMEOUT = 0x0008;
	public static final int MSG_SCREEN = 0x0009;

	class MyHandle extends Handler {

		@Override
		public void handleMessage(Message msg) {
			int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

			switch (msg.what) {
			case MSG_SHOW_VIDEO:
				programType = 0; // set video type
				String[] arr = (String[]) msg.obj;
				//showVideo(arr);
				if (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) < maxVolume / 2) {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0);
				}
				break;

			case MSG_SHOW_IMAGE:
				String path = (String) msg.obj;
			//	showImage(path);
				//break;

			case MSG_READY_SERVER:
				Log.v(TAG, "MSG_READY_SERVER.............. ");
				//if(isWifiConnect()) 
					startServer();
			//	register();
				
				//myHandle.sendEmptyMessageDelayed(MSG_IS_CREATE, 1000);
				
				break;
			case MSG_PLAY_MUSIC:
				if (programType == 1) { // audio program type
					//Toast.makeText(NscreenAirplayService.this, R.string.airtunes_toast, Toast.LENGTH_LONG).show();

					if (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) < maxVolume / 2) {
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0);
					}
				}
				break;
			case MSG_VIDEO_PAUSE:
				if (videoRate == IAirplayEventSink.SET_VIDEO_RATE_REASON_PAUSED) {
					// if (playRate == 1) {
					
					Log.w("AAAAAAAAA", "Remote pause!!!");
					// }
				}
				break;

			case MSG_IS_CREATE:
				Log.v(TAG, "MSG_IS_CREATE.............. isFirst=" + isFirst);
				isFirst = false;
				break;

			case MSG_LOAD_PHOTO_TIMEOUT:
				
				break;

			case MSG_SCREEN:
			     if(msg.arg1==1){
			    	 if(isWifiConnect()) startServer();
			     }else{
			    	 
					 if(mRARaopThread != null ){
							mRARaopThread.stopRTSP();
							Utils.log("airplay .......stop");
					 }
			    	 stopServer();

			     }
				
				
				break;

			}
			super.handleMessage(msg);
		}

	}
    public boolean isWifiConnect() {  
	      ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);  
	      NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
	      return mWifi.isConnected();  
 }  
	
	public static OnSubTitleSwitchListener switchListener=null;
	public static void setOnSubTitleSwitchListener(OnSubTitleSwitchListener listen) {
		switchListener = listen;
	}	
	public interface OnSubTitleSwitchListener {
		public void onSubTitleSwitchListener(int w, int h);
	}	
    public void updateplayer(int w, int h){
  
    }

	@Override
	public Vector3f getPlaybackInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void notifyLoadingPhoto() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyLoadingPhotoTimeOut() {
		// TODO Auto-generated method stub
		
	}
    

	
}
