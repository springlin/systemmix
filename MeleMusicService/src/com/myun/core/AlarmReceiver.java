package com.myun.core;

import com.myun.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;


public class AlarmReceiver extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
		
		String action=intent.getAction();
		Log.d("onReceive","onReceive "+action);
		if(action.equals("com.myun.shutdown.AlarmReceiver")){
			
			Toast.makeText(context, "关机", Toast.LENGTH_LONG).show();		
			MusicService.invokeShutdown();
			
		}else if(action.equals("android.intent.action.ACTION_SHUTDOWN")){
			
			MusicService.invokeShutdown();
			
		}else{
			
			  String status=""+intent.getIntExtra("status", -1);		
			  MusicService.invokeADS(status);
		}
	
    }

}
