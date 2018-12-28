package com.myun.spring.dlna;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.myun.net.protocol.PeerTools;
import com.myun.spring.dlna.utils.NscreenConstants;

public class NscreenDMRServiceBootReciver extends BroadcastReceiver {
	private final String TAG = "NscreenDMRServiceBootReciver";
	private static String appType  = null;
	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent != null) {
			final String action = intent.getAction();
			if (action.equals(PeerTools.SPEAKER_NOTIFY_STATUS)) {

                        String pclass=intent.getStringExtra("class");
           	            if(pclass!=null && !pclass.equals("dlnaservice"))
						NscreenDMRService.upnpDev.playAction.corePause();
						appType  = null;
                        Log.d(TAG, "stop AVTransport11.....");
				

				
			} else if (intent.getAction().equals(
					WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				// 鐩戝惉WIFI鐨勮繛鎺ョ殑缃戠粶鐘舵�
				Log.d(TAG, "onReceive action=" + action);

				NetworkInfo networkInfo = (NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

				if (networkInfo.isConnected()) {
					WifiManager wifiManager = (WifiManager) context
							.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					int tempIP = wifiInfo.getIpAddress();
					Log.d(TAG, "onReceive Connected tempIP:" + tempIP
							+ ";historyIP:" + NscreenDMRService.HISTROY_IP);
					if (networkInfo.isConnected() && tempIP != 0) {
						Intent service = new Intent(
								"com.qvod.nscreen.dmr.NscreenDMRService");
						if (tempIP != NscreenDMRService.HISTROY_IP) {
							Log.d(TAG, "onReceive restart the dmr");
							NscreenDMRService.HISTROY_IP = tempIP;
//							NscreenDMRService.UPNP_PARAMETER = NscreenDMRService.UPNP_RESTART;
							service.putExtra("UPNP_PARAMETER", NscreenDMRService.UPNP_RESTART);
						} else {
							Log.d(TAG, "onReceive notify the dmr");
//							NscreenDMRService.UPNP_PARAMETER = NscreenDMRService.UPNP_NOTIFY;
							service.putExtra("UPNP_PARAMETER", NscreenDMRService.UPNP_NOTIFY);
						}
						context.startService(service);
					}
				}
			}
		}
	}
}
