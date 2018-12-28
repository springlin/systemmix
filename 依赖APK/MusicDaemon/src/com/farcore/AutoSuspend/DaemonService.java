package com.farcore.AutoSuspend;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;



import com.mele.musicdaemon.IPeerListener;
import com.mele.musicdaemon.MeleAp;
import com.mele.musicdaemon.Peer;
import com.mele.musicdaemon.PeerMessage;
import com.mele.musicdaemon.PeerServerIP;
import com.mele.musicdaemon.PeerMessage.Builder;
import com.mele.musicdaemon.PeerMessage.Header;




import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class DaemonService extends Service{

	public final String TAG="DaemonService";
	private Peer peer=null;
	public  int find_peer=0x01;
	public  int timeout_count=8;
//	public static final String SPEAKER_TIME_CLOCK="com.speaker.time.clock";	
	public static final String SETTING_TIME_CLOCK="com.setting.time.clock";
	public static final long DAY = 1000L * 60 * 60 * 24;
    public static final int MSG_START_AP=0x02;
    public static final int START_AP_DELAY=10*1000;
    public static final  int MSG_STOP_AP=0x03;
    public static final int STOP_AP_DELAY=2*1000;

  //  private MeleAp mMeleAP=new MeleAp();
	@Override
	public void onCreate() {
		super.onCreate();
		
		peer=new PeerServerIP(this, "DaemonService");
		peer.setListener(ipeerlistenter);
		
		
	//	runCommand("chmod 770 /dev/i2c-0");

		//checkService("com.mele.core");
        createBroadcastReceiver();
        initWifiManager();
		handler.sendEmptyMessageDelayed(find_peer, 1000);
		
//		Intent i = new Intent("com.mele.musicservice");
//		i.putExtra("startmode", "boot");
//		startService(i);
//		
		log("Start "+TAG+" ....................");
		
	}
    private void decideApState(){
        ConnectivityManager connectivityManager=(ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
            handler.removeMessages(MSG_START_AP);
            handler.removeMessages(MSG_STOP_AP);
            handler.sendEmptyMessageDelayed(MSG_STOP_AP,STOP_AP_DELAY);
        }else{
            handler.removeMessages(MSG_STOP_AP);
            handler.removeMessages(MSG_START_AP);
            handler.sendEmptyMessageDelayed(MSG_START_AP,START_AP_DELAY);
        }
    }

    private BroadcastReceiver mWifiStateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            decideApState();
        }
    };

    private void initWifiManager(){
        WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wifiConfigurations=wifiManager.getConfiguredNetworks();
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiStateReceiver,filter);
        if (wifiConfigurations==null||wifiConfigurations.isEmpty()){
          //  mMeleAP.startAp();
            sendStickyBroadcast(new Intent("wifi_ap_start"));
        }else {
            decideApState();
        }
    }

	private IPeerListener ipeerlistenter=new IPeerListener(){

		@Override
		public void startMusicService(String info) {
			
		      
			   timeout_count=8;
			  // log(info);
		}
		
	};
	public Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if(msg.what==find_peer){
				
				
				new Thread(new Runnable(){

					@Override
					public void run() {
				
					    byte[] info = PeerMessage.Builder.build(new PeerMessage.Header(PeerMessage.PEER_MESSAGE_FIND_PEER));
				        peer.sendMessageNoWait(info, info.length);
				        handler.sendEmptyMessageDelayed(find_peer, 1200);
				        timeout_count--;
				        
				        if(timeout_count==0){
				        	log("timeout_out ......");
				        	startMusicService();
				        	timeout_count=8;
				        }
				        
				        
					}
					
				}).start();

			}
            else if(msg.what==MSG_START_AP){
//                if(mMeleAP.startAp()!=0){
//                    handler.sendEmptyMessageDelayed(MSG_START_AP,2*1000);
//                }else{           
//            		sendStickyBroadcast(new Intent("wifi_ap_start"));
//            		log("wifi_ap_start............");
//                }
            }
            else if(msg.what==MSG_STOP_AP){
//                if(mMeleAP.stopAp()!=0){
//                    handler.sendEmptyMessageDelayed(MSG_STOP_AP,2*1000);
//                }else{
//                	log("wifi_ap_stop...........................");
//                	sendStickyBroadcast(new Intent("wifi_ap_stop"));
//                }
            }
		}
		
	};
	public void checkService(String packagename){
		 ActivityManager mActivityManager =(ActivityManager)getSystemService(ACTIVITY_SERVICE);
		 List<ActivityManager.RunningServiceInfo> ServiceList = mActivityManager.getRunningServices(40);
		 
		 for(int i=0; i<ServiceList.size(); i++){
			 
			       ComponentName cn=ServiceList.get(i).service;
			       log(ServiceList.size()+"package name :"+cn.getPackageName());
			      if(cn.getPackageName().equals(packagename)){
			          try {
			        	  Intent intent=new Intent();
			        	  intent.setComponent(cn);
                          stopService(intent);  
                      } catch (SecurityException sEx) {  
                          
                          System.out.println(" deny the permission");  
                      }
			      }
			 
		 }
		 
		 
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		timeout_count=8;
		return START_STICKY;
	}
	
	public boolean startMusicService(){
		   
		   boolean ret=true;
//		   if(!checkPackage("com.myun.core")){
//			   log("com.myun.core  error is not exit !!!");
//			   return false;
//		   }
		   
	  	  Intent intent=new Intent("com.myun.musicservice");
	  	  ComponentName cn=new ComponentName("com.myun.core","com.myun.core.MusicService");
	      stopService(intent);  	      
	      log("end MusicService ..................................");
	      
	      
	      
		   Intent i=new Intent("com.myun.musicservice");
		   startService(i);
		   
		   log("start MusicService ..................................");
		   return ret;
	}
	
	/**
  	 * 检测该包名所对应的应用是否存在
  	 * @param packageName
  	 * @return
  	 */
  	public boolean checkPackage(String packageName) 
  	{  
	    if (packageName == null || "".equals(packageName))  
	        return false;  
	    try 
	    {  
	        getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);  
	        return true;  
	    } 
	    catch (NameNotFoundException e) 
	    {  
	        return false;  
	    }  
	} 
	@Override
	public void onDestroy() {
		releaseBroadcastReceiver();
        unregisterReceiver(mWifiStateReceiver);
		super.onDestroy();
	}



	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
    public void log(String msg){
    	Log.i(TAG, msg);
    }
    
    
	public ReceiverListener mBroadcastReceiver=null;
	public void createBroadcastReceiver(){
	    IntentFilter filter = new IntentFilter();   
	    filter.addAction(SETTING_TIME_CLOCK); 
	    mBroadcastReceiver = new ReceiverListener();  
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

	        } else {  
	
	            throw e;  
	        }  
	    } 
	}

   public  void showmsg(final String msg){
		  handler.post(new Runnable(){  
		         public void run(){  
		            
		        	 Toast toast=Toast.makeText(getApplicationContext(), " "+msg, Toast.LENGTH_LONG);

		             toast.show();
		         }  
		     }); 
	}	
	public void setTimerClock(String timepos, String timeperiod, String id){
		
		
		String t[]=timepos.split(":"), week=StringData();
		
		Intent intent = new Intent("com.farcore.alarmintent");
		intent.putExtra("id", id);
		PendingIntent sender = PendingIntent.getBroadcast(this,  Integer.parseInt(id), intent, PendingIntent.FLAG_UPDATE_CURRENT);
	
	 //   long firstTime = SystemClock.elapsedRealtime();
	    long systemTime = System.currentTimeMillis();
	
	    Calendar calendar = Calendar.getInstance();
	 	calendar.setTimeInMillis(System.currentTimeMillis());
	 	calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); 
	 	calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
	 	calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
	 	calendar.set(Calendar.SECOND, 0);
	 	calendar.set(Calendar.MILLISECOND, 0);
	
	 	
	    // 进行闹铃注册
	    AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);	
	    
	    
	 	// 选择的每天定时时间
	 	long selectTime = calendar.getTimeInMillis();	
	
	 	// 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
//	 	if(systemTime > selectTime) {
//	 		log("设置的时间小于当前时间");
//	 		calendar.add(Calendar.DAY_OF_MONTH, 1);
//	 		selectTime = calendar.getTimeInMillis();
//	 	}
	

	 	if( (timeperiod==null || timeperiod.equals("") || timeperiod.contains(week)) &&  (systemTime <= selectTime) ){
	 		
	 		manager.set(AlarmManager.RTC_WAKEUP,selectTime, sender);
	 		log("timepos="+timepos+" timeperiod="+timeperiod+" id="+id+" 闹钟将在"+timepos+"启动");
	 		
	 	}else if(timeperiod!=null){
	 		
	 		int w=Integer.parseInt(week), d=0, i=0;
	 		for(i=0; i<timeperiod.length(); i++){
	 			    d=Integer.parseInt(timeperiod.charAt(i)+"");
	 			    if(d>w){
	 			    	
	 			    	
	 			 		calendar.add(Calendar.DAY_OF_MONTH, d-w);
	 			 		selectTime = calendar.getTimeInMillis();
	 			    	manager.set(AlarmManager.RTC_WAKEUP,selectTime, sender);
	 			    	i=-1;
	 			    	log("timepos="+timepos+" timeperiod="+timeperiod+" id="+id+" 闹钟将在"+(d-w)+"天后启动");
	 			    	break;
	 			    }
	 			
	 		}
	 		if(i!=-1){
	 			 
	 			 if(timeperiod.equals("")){
	 				 
	 				 log("闹钟无效 ...."+"timepos="+timepos+" timeperiod="+timeperiod+" id="+id);
	 				 return ;
	 			 } else {
	 				 d=Integer.parseInt(timeperiod.charAt(0)+"")+1; 
	 			 }
	 			 d=6-w+d;
		 		 calendar.add(Calendar.DAY_OF_MONTH, d);
		 		 selectTime = calendar.getTimeInMillis();
		    	 manager.set(AlarmManager.RTC_WAKEUP,selectTime, sender);
		    	 log("timepos="+timepos+" timeperiod="+timeperiod+" id="+id+" 闹钟将在"+d+"天后启动");
	 		}
	 		
	 		
	 	}else{
	 		log("闹钟无效 ...."+"timepos="+timepos+" timeperiod="+timeperiod+" id="+id);
	 	}
	 	
	 	
	 	
	 	
	 	
	 	
//	     
//		long next=DAY;	
//	    if(timeperiod.equals("每周重复")){
//	    	  next=next*7;
//	    	  manager.setRepeating(AlarmManager.RTC_WAKEUP, selectTime,  next,  sender);
//	    }else if(timeperiod.equals("一次性")){
//	    	  manager.setRepeating(AlarmManager.RTC_WAKEUP, selectTime,  next,  sender); 
//	    }else{
//	    	  
//	    }
//
//	   // manager.setRepeating(AlarmManager.RTC_WAKEUP, selectTime,  next,  sender);
//	    
//	    Log.i(TAG, timepos+ ", selectTime ===== "+ selectTime + ", systemTime ==== " + systemTime );
//	
//	    log("设置["+t[0]+":"+t[1]+"]闹铃成功! ");
	}
	public void cancelTimeClock(String  id){
		
		Intent i=new Intent("com.farcore.alarmintent");
		PendingIntent pi = PendingIntent.getBroadcast(this, Integer.parseInt(id) , i, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(pi);
		
	}
	
	
    public static void runCommand(String command)
    {
    	Log.e("","runCommand ---- "+command);
        try {
            java.lang.Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(command+"\n");
            dos.writeBytes("exit\n");
            dos.flush();

            p.waitFor();
            DataInputStream dis = new DataInputStream(p.getInputStream());
            DataInputStream des = new DataInputStream(p.getErrorStream());
            while (dis.available() > 0)
            	dis.readLine();// Log.d(TAG, "stdout: "+dis.readLine()+"\n");
            while (des.available() > 0)
            	des.readLine();// Log.d(TAG, "stderr: "+des.readLine()+"\n");
           // Log.d(TAG, "return: "+p.exitValue());
        } catch (Exception e) {
            Log.d("TAG", "exception: "+e);
        }
        
        Log.e("","runCommand === "+command);
    }
	class ReceiverListener extends BroadcastReceiver {  
		  
        @Override  
        public void onReceive(Context context, Intent intent) {
        	
        	String action=intent.getAction();
        	String timepos=intent.getStringExtra("time");
        	String timeperiod=intent.getStringExtra("timeperiod");
        	String id=intent.getStringExtra("id");
        	
        	Log.e(TAG, "action.   ......"+ action);
            if (action.equals(SETTING_TIME_CLOCK)){
            	setTimerClock(timepos, timeperiod, id);
            }
  
        }  
  
    } 
	
    public  String StringData(){  
         final Calendar c = Calendar.getInstance();  
         c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
         
         int  i=c.get(Calendar.DAY_OF_WEEK);
         String mWay=""+(i-1);  
         
        if("0".equals(mWay)){  
            mWay ="天";  
        }else if("1".equals(mWay)){  
            mWay ="一";  
        }else if("2".equals(mWay)){  
            mWay ="二";  
        }else if("3".equals(mWay)){  
            mWay ="三";  
        }else if("4".equals(mWay)){  
            mWay ="四";  
        }else if("5".equals(mWay)){  
            mWay ="五";  
        }else if("6".equals(mWay)){  
            mWay ="六";  
        }  
        log("今天是星期"+mWay+ getCurTime("yyyy-MM-dd HH:mm:ss"));
        return String.valueOf(""+(i-1));  
 
    } 
    
    public static String getCurTime(String type){
	    	SimpleDateFormat DateFormat =new SimpleDateFormat(type);
	    	Date   curDate = new Date(System.currentTimeMillis());//获取当前时间
	    	return DateFormat.format(curDate);  
    }
}
