package com.myun.spring.dlna;

import org.cybergarage.upnp.UPnP;
import org.cybergarage.upnp.device.InvalidDescriptionException;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;





import com.myun.core.R;
import com.myun.net.protocol.PeerTools;
import com.myun.spring.dlna.utils.Utils;


public class NscreenDMRService extends Service {

    private final String TAG = "NscreenDMRService";
    public static NscreenRendererDevice upnpDev = null;
    public static int HISTROY_IP = 0;

    public static final int UPNP_UNKNOW = 0X0;
    public static final int UPNP_CREATE = 0X1;
    public static final int UPNP_RESTART = 0X2;
    public static final int UPNP_STOP = 0X3;
    public static final int UPNP_NOTIFY = 0X4;
    // 默认为unknow类型
    public static int UPNP_PARAMETER = UPNP_UNKNOW;
    // 解决启动的时候随机报错的现象，保证系统启动后DMR启动完成后在启动网络重启DMR事件
    private boolean isBootStart = false;

    @Override
    public void onCreate() {
        super.onCreate();
        register();
  
        Log.d(TAG, getApplicationContext()
                + "#######onCreate####### UPNP_PARAMETER:" + UPNP_PARAMETER);
        if (UPNP_PARAMETER == UPNP_UNKNOW) {
            execCommon(UPNP_CREATE);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if(intent == null) {
    		Log.d(TAG, "#######onStartCommand####### intent="+intent);
    		return super.onStartCommand(intent, flags, startId);
    	}
    	Log.d(TAG, "#######onStartCommand#######");
//        int key = UPNP_PARAMETER;
        int key =  intent.getIntExtra("UPNP_PARAMETER", UPNP_UNKNOW);
        Log.d(TAG, "onStartCommand UPNP_PARAMETER:" + key);
        switch (key) {
        case UPNP_CREATE:
            execCommon(UPNP_CREATE);
            break;
        case UPNP_STOP:
            execCommon(UPNP_STOP);
            break;
        case UPNP_RESTART:
            execCommon(UPNP_RESTART);
            break;
        case UPNP_NOTIFY:
            execCommon(UPNP_NOTIFY);
            break;
        case UPNP_UNKNOW:
            Log.e(TAG, "onStartCommand key is UPNP_UNKNOW");
            break;
        }
		 Notification notification = new Notification(R.drawable.ic_launcher, "netservice is running",System.currentTimeMillis());
		 PendingIntent pintent=PendingIntent.getService(this, 0, new Intent("com.myun.musicservice"), 0);
		 notification.setLatestEventInfo(this, "DLNA Service","netservice is running", pintent);
		

		 startForeground(19, notification);
		 
		 
		 
		 return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "#######onBind#######");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "#######onDestroy#######");
        execCommon(UPNP_STOP);
        super.onDestroy();
        unRegister();
    }

    private void execCommon(int common) {
        final int temp = common;
        Log.d(TAG, "execCommon temp:" + temp + ";isBootStart:" + isBootStart);
        new Thread() {
            public void run() {
                switch (temp) {
                case UPNP_CREATE:
                    isBootStart = true;
                    startUPNP();
                    isBootStart = false;
                    break;
                case UPNP_RESTART:
                    while (isBootStart) {
                        Log.d(TAG,
                                "wait for the boot start action isBootStart:"
                                        + isBootStart);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    reStartUPNP();
                    notifyUPNP();
                    break;
                case UPNP_STOP:
                    isBootStart = false;
                    stopUPNP();
                    break;
                case UPNP_NOTIFY:
                    notifyUPNP();
                    break;
                default:
                    break;
                }
            };
        }.start();
    }

    private void startUPNP() {
        Utils.createServiceFile(getApplicationContext());
        try {
        	//UPnP.setTimeToLive(8);
            UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
            upnpDev = new NscreenRendererDevice(getFilesDir().getAbsolutePath()
                    + "/NscreenRenderer.xml", getApplicationContext());
            // String deviceName = getString(R.string.device_name,
            // Utils.getNID(getApplicationContext()));
            String deviceName = PeerTools.getDeviceName(this);
            upnpDev.setFriendlyName(deviceName);
            upnpDev.setSerialNumber(Utils.getSerialNumber(getApplicationContext()));
            String temp = Utils.getUDN(getApplicationContext());
            String udn = Utils.getSharedPrefValue(getApplicationContext(),
                    Utils.UDN_KEY, null);
            Log.d(TAG, "startUPNP temp:" + temp + ";udn:" + udn);
            if (udn == null || "".equals(udn)) {
                udn = temp;
                Utils.saveSharedPrefValue(getApplicationContext(),
                        Utils.UDN_KEY, udn);
            }
            upnpDev.setUDN(udn);
        } catch (InvalidDescriptionException e) {
            e.printStackTrace();
        }
        upnpDev.start();
    }

    private void reStartUPNP() {
        stopUPNP();
        startUPNP();
    }

    private void stopUPNP() {
        if (upnpDev != null) {
            Log.d(TAG, "stopUPNP ");
            upnpDev.stopDev();
            upnpDev = null;
        }
    }

    private void notifyUPNP() {
        if (upnpDev != null) {
            Log.d(TAG, "notifyUPNP ");
            upnpDev.announce();
        }
    }
    
    private BroadcastReceiver mReceiver;
    private void register() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(PeerTools.SPEAKER_NOTIFY_STATUS);
		 mReceiver = new NscreenDMRServiceBootReciver();
		 registerReceiver(mReceiver, filter);
	}
    
    private void unRegister() {
    	if(null!=mReceiver)
    		unregisterReceiver(mReceiver);
	}

}
