package com.myun.core;

import java.util.LinkedList;
import java.util.List;




import com.myun.spring.airplay.NscreenAirplayService;
import com.myun.utils.Utils;





import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

public class WifiAdmin extends Thread{
	private final static String TAG = "WifiAdmin";
	private StringBuffer mStringBuffer = new StringBuffer();
	private List<ScanResult> listResult;
	private ScanResult mScanResult;

	private WifiManager mWifiManager;

	private WifiInfo mWifiInfo;
	
	public List<WifiConfiguration> wifiConfigList;
	
	WifiLock mWifiLock;
	public Context context;
    static public String conn_ssid=null, status=null;
    public Handler mhandler; 
    public Looper mLooper;
    private BroadcastReceiver mBroadcastReceiver = null;

	public WifiAdmin(Context context) {
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		if(!mWifiManager.isWifiEnabled()){
			openNetCard();
		}
		mWifiInfo = mWifiManager.getConnectionInfo();
		this.context=context;
		getConfiguration();
		registerBroadcastReceiver();
	}


	public void openNetCard() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	public void closeNetCard() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}


	public void checkNetCardState() {
		if (mWifiManager.getWifiState() == 0) {
			Log.i(TAG, "网卡正在关闭");
		} else if (mWifiManager.getWifiState() == 1) {
			Log.i(TAG, "网卡已经关闭");
		} else if (mWifiManager.getWifiState() == 2) {
			Log.i(TAG, "网卡正在打开");
		} else if (mWifiManager.getWifiState() == 3) {
			Log.i(TAG, "网卡已经打开");
		} else {
			Log.i(TAG, "---_---晕......没有获取到状态---_---");
		}
	}


	public void scan() {
		mWifiManager.startScan();
		listResult = mWifiManager.getScanResults();
		if (listResult != null) {
			//Log.i(TAG, "当前区域存在无线网络，请查看扫描结果");
		} else {
			Log.i(TAG, "当前区域没有无线网络");
		}
	}


	public String getScanResult() {

		scan();
		listResult = mWifiManager.getScanResults();
//		if (listResult != null) {
//			for (int i = 0; i < listResult.size(); i++) {
//				mScanResult = listResult.get(i);
//				mStringBuffer = mStringBuffer.append("NO.").append(i + 1)
//						.append(" :").append(mScanResult.SSID).append("->")
//						.append(mScanResult.BSSID).append("->")
//						.append(mScanResult.capabilities).append("->")
//						.append(mScanResult.frequency).append("->")
//						.append(mScanResult.level).append("->")
//						.append(mScanResult.describeContents()).append("\n\n");
//			}
//		}
//		Log.i(TAG, mStringBuffer.toString());
		return "";
	}

	@Override
	public void run() {
		super.run();
		
		   Utils.log("WifiAdmin Thread Start .......");
	
		   Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
		   
	       Looper.prepare(); 
	       mLooper  = Looper.myLooper();
	       mhandler = new Handler(Looper.myLooper()) {
	          public void handleMessage(Message msg) {          
	
	    	
	    	      if(msg.what==0){	    	    	  
	                  getScanResult();	
	    	      }else if(msg.what==-1){
	    	    	  quit();
	    	      }
	    	   }     
	       }; 
	       mhandler.sendEmptyMessage(0);
	       Looper.loop(); 
	       Utils.log("WifiAdmin exit ....");
	
	}

	public void connect() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		
	}

	public List<ScanResult> getListResult() {
		getConfiguration();
		mhandler.sendEmptyMessage(0);
		
 		List<ScanResult> list=new LinkedList<ScanResult>();
 		if(listResult!=null)  list.addAll(listResult);
		return list;
	}


	public void disconnectWifi() {
		int netId = getNetworkId();
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
		mWifiInfo = null;
	}


	public void checkNetWorkState() {
		if (mWifiInfo != null) {
			Log.i(TAG, "网络正常工作");
		} else {
			Log.i(TAG, "网络已断开");
		}
	}


	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}


	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}


	public void acquireWifiLock() {
		mWifiLock.acquire();
	}


	public void releaseWifiLock() {

		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	public void creatWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}


	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的BSSID
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}
	public String getSSID() {
		
		
		if(!isWifiConnect()) return null;
		mWifiInfo = mWifiManager.getConnectionInfo();

		return ((mWifiInfo == null) ? null : mWifiInfo.getSSID()).replace("\"", "");
	}
	// 得到WifiInfo的所有信息包
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}
    public synchronized String setupWiFi(String ssid, String security, String pwd){
       
    	conn_ssid=ssid;
    	status="";
    	Utils.log("ssid"+ssid+" security "+security+" pwd "+pwd);
 		int wifiItemId = IsConfiguration("\""+ssid+"\"");
		Log.i(TAG,"wifiItemId "+String.valueOf(wifiItemId));
		if(wifiItemId != -1){
			if(ConnectWifi(wifiItemId)){//连接指定WIFI
				
				Utils.log("成功连接WiFi"+ssid);
			}else if(pwd!=null){
				Utils.log("密码可能有错，尝试新建连接...");
				removeWifi(ssid);
				int netId = AddWifiConfig(listResult,ssid, pwd);
				Utils.log("重新添加AddWifiConfig id "+String.valueOf(netId));
				if(netId != -1){
					getConfiguration();//添加了配置信息，要重新得到配置信息
					if(ConnectWifi(netId)){
						
						mWifiManager.saveConfiguration();
						Utils.log("成功连接WiFi"+ssid);
					}
				}else{
					Utils.log("网络连接错误");

					
				}
				
				
				
			}

			
		}else{
         
			int netId = AddWifiConfig(listResult,ssid, pwd);
			Log.i(TAG,"AddWifiConfig id "+String.valueOf(netId));
			if(netId != -1){
				getConfiguration();//添加了配置信息，要重新得到配置信息
				if(ConnectWifi(netId)){
					
					mWifiManager.saveConfiguration();
					Utils.log("成功连接WiFi"+ssid);

				}
			}else{
				Utils.log("网络连接错误");
				
			}
		}
		Utils.log("setupWiFi finish");
    	return "ok";
    }
    
	public WifiConfiguration getWifiConfiguration(int wifiId){
		for(int i = 0; i < wifiConfigList.size(); i++){
			WifiConfiguration wifi = wifiConfigList.get(i);
			if(wifi.networkId == wifiId){
				return wifi;
			}
		}
		return null;
	}
	public void getConfiguration(){
		if(wifiConfigList!=null) wifiConfigList.clear();
		//wifiConfigList = mWifiManager.getConfiguredNetworks();//得到配置好的网络信息
		//for(int i =0;i<wifiConfigList.size();i++){
			//Log.i(TAG,"wifiConfigList "+wifiConfigList.get(i).SSID+":"+String.valueOf(wifiConfigList.get(i).networkId));
		
		//}
	}
	
	public int IsConfiguration(String SSID){
		
		
		Log.i("IsConfiguration", "size:"+String.valueOf(wifiConfigList.size()));
		for(int i = 0; i < wifiConfigList.size(); i++){
			Utils.log(wifiConfigList.get(i).SSID+" "+String.valueOf( wifiConfigList.get(i).networkId));
			if(wifiConfigList.get(i).SSID.contains(SSID)){//地址相同
				return wifiConfigList.get(i).networkId;
			}
		}
		return -1;
	}	
	
	public WifiConfiguration getConfiguration(String SSID){
		Log.i("IsConfiguration", "size:"+String.valueOf(wifiConfigList.size()));
		for(int i = 0; i < wifiConfigList.size(); i++){
			Utils.log(wifiConfigList.get(i).SSID+" "+String.valueOf( wifiConfigList.get(i).networkId));
			if(wifiConfigList.get(i).SSID.contains(SSID)){//地址相同
				return wifiConfigList.get(i);
			}
		}
		return null;
	}
	public boolean ConnectWifi(int wifiId){
		for(int i = 0; i < wifiConfigList.size(); i++){
			WifiConfiguration wifi = wifiConfigList.get(i);
			if(wifi.networkId == wifiId){
				while(!(mWifiManager.enableNetwork(wifiId, true))){//激活该Id，建立连接
					Log.i(TAG,"ConnectWifi "+String.valueOf(wifiConfigList.get(wifiId).status));//status:0--已经连接，1--不可连接，2--可以连接
				}
				return true;
			}
		}
		return false;
	}
	
	public int AddWifiConfig(List<ScanResult> wifiList,String ssid,String pwd){
		int wifiId = -1;
		for(int i = 0;i < wifiList.size(); i++){
			ScanResult wifi = wifiList.get(i);
			if(wifi.SSID.equals(ssid)){
				
				String var=wifi.capabilities;
				Log.i("AddWifiConfig","AddWifiConfig "+ssid+ " wifi.capabilities "+var);
				if(var!=null && !var.equals("")) var=var.toLowerCase();
				
				int type=3;
				if(var!=null && var.contains("wpa") ) type=3;
				else if(var!=null && var.contains("wep")) type=2;
				else type=1;
				WifiConfiguration wifiCong = CreateWifiInfo(ssid, pwd, type);//new WifiConfiguration();
//				wifiCong.SSID = "\""+wifi.SSID+"\"";//\"转义字符，代表"
//				wifiCong.preSharedKey = "\""+pwd+"\"";//WPA-PSK密码
//				wifiCong.hiddenSSID = false;
//				wifiCong.status = WifiConfiguration.Status.ENABLED;
				wifiId = mWifiManager.addNetwork(wifiCong);//将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
				if(wifiId != -1){
					return wifiId;
				}
			}
		}
		return wifiId;
	}
	
	  public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
	        Log.i(TAG, "SSID:" + SSID + ",password:" + Password);
	        WifiConfiguration config = new WifiConfiguration();
	        config.allowedAuthAlgorithms.clear();
	        config.allowedGroupCiphers.clear();
	        config.allowedKeyManagement.clear();
	        config.allowedPairwiseCiphers.clear();
	        config.allowedProtocols.clear();
	        config.SSID = "\"" + SSID + "\"";
	 
	        
	        
	        WifiConfiguration tempConfig = this.IsExsits(SSID);
	 
	        if (tempConfig != null) {
	            mWifiManager.removeNetwork(tempConfig.networkId);
	            mWifiManager.saveConfiguration();
	            Log.i(TAG, "removeNetwork ....."+tempConfig.SSID);
	        } else {
	           
	        }
	 
	        if (Type == 1) // WIFICIPHER_NOPASS
	        {
	            Log.i(TAG, "Type =1.");
	            config.wepKeys[0] =  "\"" + "\"";
	            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	            config.wepTxKeyIndex = 0;
	        }
	        if (Type == 2) // WIFICIPHER_WEP
	        {
	            Log.i(TAG, "Type =2.");
	            config.hiddenSSID = true;
	            config.wepKeys[0] = "\"" + Password + "\"";
	            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
	            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	            config.wepTxKeyIndex = 0;
	        }
	        if (Type == 3) // WIFICIPHER_WPA
	        {
	 
	            Log.i(TAG, "Type =3.");
	            config.preSharedKey = "\"" + Password + "\"";
	 
	            config.hiddenSSID = true;
	            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	            config.status = WifiConfiguration.Status.ENABLED;
	        }
	        return config;
	}
	  
	private WifiConfiguration IsExsits(String SSID) { // 查看以前是否已经配置过该SSID  
	        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
	        for (WifiConfiguration existingConfig : existingConfigs) {
	            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
	                return existingConfig;
	            }
	        }
	        return null;
	}
    public boolean isWifiConnect() {  
	      ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);  
	      NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
	      return mWifi.isConnected();  
   }  
   public void removeWifi(String ssid){  
	   int netId=IsConfiguration(ssid);
	   Utils.log("removeWifi"+netId);
	   if(netId!=-1){
		   mWifiManager.removeNetwork(netId);
		   mWifiManager.saveConfiguration();
		   getConfiguration();
	   }
   }                
                                  
	public void quit() {
		   
		   if(mLooper!=null){
			   
		      mLooper.quit();
		      mLooper=null;
		      
		   }
		   
	}
	private void unregisterBroadcastReceiver() {
		if (mBroadcastReceiver != null) {
			try {
				context.unregisterReceiver(mBroadcastReceiver);
			} catch (Exception e) {
			}
		}
	}	
	private void registerBroadcastReceiver() {
	
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
		intentFilter.addAction("com.myun.UPDATE_CHECK_RET");
		intentFilter.addAction("com.myun.UPDATE_DOWANLOAD_OK");
		intentFilter.addAction("wifi_ap_start");
		intentFilter.addAction("wifi_ap_stop");
		intentFilter.addAction("com.mele.KEY_DOWN_BC");

		
		context.registerReceiver(mBroadcastReceiver = new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Utils.log("WiFiAdmin BroadcastReceiver......"+action);
				if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
					
					SupplicantState supplicantState = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);	
	                if (supplicantState.equals(SupplicantState.COMPLETED)) {		                   
	                     Utils.log("SupplicantState.COMPLETED");
	                     //context.startService(new Intent(context, NscreenAirplayService.class));	  
	                } else if (supplicantState.equals(SupplicantState.DISCONNECTED)) {	
	                	Utils.log("SupplicantState.DISCONNECTED");
	                	status="dis";
	                } else if (supplicantState.equals(SupplicantState.AUTHENTICATING)) {	
	                	Utils.log("SupplicantState.AUTHENTICATING");
	                	status="authe";
	                } else if (supplicantState.equals(SupplicantState.ASSOCIATING)) {	
	                	Utils.log("SupplicantState.ASSOCIATING");
	                	status="associat";
	                } 
	                int  error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 2);
	                if(error==WifiManager.ERROR_AUTHENTICATING){
	                	Utils.log("SupplicantState.ERROR_AUTHENTICATING");
	                	status="error_authe";
	                }
               }else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            	   
            	    MusicService.instance.handler.removeMessages(Utils.Cmd_Sleep);
            	    Message msg=MusicService.instance.handler.obtainMessage(Utils.Cmd_Sleep, 0, 0);
            	    MusicService.instance.handler.sendMessageDelayed(msg, 120);
            	   
            	   
               }else if (action.equals(Intent.ACTION_SCREEN_ON)) {
            	   
            	   
	           	    MusicService.instance.handler.removeMessages(Utils.Cmd_Sleep);
	           	    Message msg=MusicService.instance.handler.obtainMessage(Utils.Cmd_Sleep, 1, 0);
	           	    MusicService.instance.handler.sendMessageDelayed(msg, 120);
            	   
               }else if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
            	   
            	   // MusicService.instance.initVol();
            	    
               }else if (action.equals("com.myun.UPDATE_CHECK_RET")) {

            	    MusicService.instance.systemUpdate(0, intent.getIntExtra("check_ret", -1));
            	   
               }else if (action.equals("com.myun.UPDATE_DOWANLOAD_OK")) {

            	   MusicService.instance.systemUpdate(1,0);
               }else if (action.equals("wifi_ap_start")) {

            	   MusicService.instance.startHotApLed(1);
               }else if (action.equals("wifi_ap_stop")) {

            	   MusicService.instance.startHotApLed(-1);
               }else if (action.equals("com.mele.KEY_DOWN_BC")) {

            	   int keycode=intent.getIntExtra("KeyCode", -1);
            	   Utils.log("wifi  keycode ..."+keycode);
            	   Intent i = new Intent();
                   i.setData(Uri.parse("file://"));
            	   if(keycode==132){
         	  
	            	   i.setAction("com.speaker.key");
	            	   i.putExtra("keycode", 2);
	            	   i.putExtra("action", 1);
	            	   context.sendBroadcast(i);
            	   }else if(keycode==131){
         	  
	            	   i.setAction("com.speaker.key");
	            	   i.putExtra("keycode", 3);
	            	   i.putExtra("action", 1);
	            	   context.sendBroadcast(i);
            	   }else if(keycode==134){//++
         	  
	            	   i.setAction("com.speaker.key");
	            	   i.putExtra("keycode", 104);
	            	   i.putExtra("action", 0);
	            	   context.sendBroadcast(i);
            	   }else if(keycode==133){//++
         	  
	            	   i.setAction("com.speaker.key");
	            	   i.putExtra("keycode", 109);
	            	   i.putExtra("action", 0);
	            	   context.sendBroadcast(i);
            	   }

            	   
            	   
               }
				
			}
		}, intentFilter);
	}
}
