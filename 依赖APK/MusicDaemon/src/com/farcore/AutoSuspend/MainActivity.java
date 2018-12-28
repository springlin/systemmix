package com.farcore.AutoSuspend;

import java.util.Calendar;
import java.util.TimeZone;



import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static MainActivity instance=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent i=new Intent("com.mele.musicdaemon.DaemonService");
		startService(i);
       // instance=this;
		//setTimerClock("11:42");
		finish();
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	
	
		return true;
	}
//	public static void invokeResume(){
//		PowerManager pm = (PowerManager)instance.getSystemService(Context.POWER_SERVICE);
//		long time=SystemClock.uptimeMillis();
//		pm.wakeUp(time);	
//	}
//	public void setTimerClock(String tc){
//		
//		
//		String t[]=tc.split(":");
//		final String INTENT = "com.farcore.alarmintent";  
//		  Intent intent = new Intent(INTENT);  
//          intent.setClass(this, AlarmReceiver.class);   
//		PendingIntent sender = PendingIntent.getBroadcast(this,  Integer.parseInt("36"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
//	
//	    long firstTime = SystemClock.elapsedRealtime();	// 开机之后到现在的运行时间(包括睡眠时间)
//	    long systemTime = System.currentTimeMillis();
//	
//	    Calendar calendar = Calendar.getInstance();
//	 	calendar.setTimeInMillis(System.currentTimeMillis());
//	 	calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 这里时区需要设置一下，不然会有8个小时的时间差
//	 	calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
//	 	calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
//	 	calendar.set(Calendar.SECOND, 0);
//	 	calendar.set(Calendar.MILLISECOND, 0);
//	
//	 	// 选择的每天定时时间
//	 	long selectTime = calendar.getTimeInMillis();	
//	
//	 	// 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
//	 	if(systemTime > selectTime) {
//	 		
//	 		calendar.add(Calendar.DAY_OF_MONTH, 1);
//	 		selectTime = calendar.getTimeInMillis();
//	 	}
//	
////	 	// 计算现在时间到设定时间的时间差
//	 	long time = selectTime - systemTime;
//			firstTime += time;
//	
//		long next=36000*36000;	
//
//	    // 进行闹铃注册
//	    AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
//	    manager.setRepeating(AlarmManager.RTC_WAKEUP,
//	    		selectTime, next, sender);
//	
//	    Log.i("", "time ==== " + tc + ", selectTime ===== "+ selectTime + ", systemTime ==== " + systemTime + ", firstTime === " + firstTime);
//	
//	
//	}
}
