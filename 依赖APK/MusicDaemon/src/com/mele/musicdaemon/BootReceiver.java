package com.mele.musicdaemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		
		
		
		Intent i = new Intent("com.mele.musicdaemon.DaemonService");
		context.startService(i);
	}

}