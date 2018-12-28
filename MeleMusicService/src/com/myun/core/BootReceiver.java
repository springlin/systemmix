package com.myun.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		
		
		
		Intent i = new Intent("com.myun.musicservice");
		i.putExtra("startmode", "boot");
		i.setPackage("com.myun.core");
		context.startService(i);
	}

	
}