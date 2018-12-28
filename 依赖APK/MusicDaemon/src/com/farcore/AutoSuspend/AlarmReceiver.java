package com.farcore.AutoSuspend;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;


public class AlarmReceiver extends BroadcastReceiver {
	
	public static final String SPEAKER_TIME_CLOCK="com.speaker.time.clock";	
	
	@Override
    public void onReceive(Context context, Intent intent) {
		
		
		String id=intent.getStringExtra("id");
		Toast.makeText(context, "ID:"+id+"闹铃响了", Toast.LENGTH_LONG).show();
		Log.e("AlarmReceiver","ID:"+id+"闹铃响了");
		if(id==null || id.equals("")){
			  return ;
		}

		
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		long time=SystemClock.uptimeMillis();
		pm.wakeUp(time);		
		

		Intent i = new Intent(SPEAKER_TIME_CLOCK);
		i.putExtra("id", id);
		i.setData(Uri.parse("file://alarm"));
		context.sendBroadcast(i);
     

		
    }

}
