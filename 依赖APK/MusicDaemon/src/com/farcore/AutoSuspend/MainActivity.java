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
//	    long firstTime = SystemClock.elapsedRealtime();	// ����֮�����ڵ�����ʱ��(����˯��ʱ��)
//	    long systemTime = System.currentTimeMillis();
//	
//	    Calendar calendar = Calendar.getInstance();
//	 	calendar.setTimeInMillis(System.currentTimeMillis());
//	 	calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // ����ʱ����Ҫ����һ�£���Ȼ����8��Сʱ��ʱ���
//	 	calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
//	 	calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
//	 	calendar.set(Calendar.SECOND, 0);
//	 	calendar.set(Calendar.MILLISECOND, 0);
//	
//	 	// ѡ���ÿ�춨ʱʱ��
//	 	long selectTime = calendar.getTimeInMillis();	
//	
//	 	// �����ǰʱ��������õ�ʱ�䣬��ô�ʹӵڶ�����趨ʱ�俪ʼ
//	 	if(systemTime > selectTime) {
//	 		
//	 		calendar.add(Calendar.DAY_OF_MONTH, 1);
//	 		selectTime = calendar.getTimeInMillis();
//	 	}
//	
////	 	// ��������ʱ�䵽�趨ʱ���ʱ���
//	 	long time = selectTime - systemTime;
//			firstTime += time;
//	
//		long next=36000*36000;	
//
//	    // ��������ע��
//	    AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
//	    manager.setRepeating(AlarmManager.RTC_WAKEUP,
//	    		selectTime, next, sender);
//	
//	    Log.i("", "time ==== " + tc + ", selectTime ===== "+ selectTime + ", systemTime ==== " + systemTime + ", firstTime === " + firstTime);
//	
//	
//	}
}
