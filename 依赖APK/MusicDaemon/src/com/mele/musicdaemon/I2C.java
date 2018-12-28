package com.mele.musicdaemon;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class I2C {

	public Context context;
	public I2C(Context context) {
		super();
		this.context=context;
		jni_i2c_open();
	}
	public void onKeyMessage(int keycode, int action){
		
		
		Intent i = new Intent("com.speaker.key");
		i.putExtra("keycode", keycode);
		i.putExtra("action", action);		
		i.setData(Uri.parse("file://alarm"));
		context.sendBroadcast(i);
		
		Log.d("Key","keycode "+keycode+" action"+ action);
		
	}
	static {

		
		try {

			 System.loadLibrary("music_i2c");   	    	
		    
		} catch(UnsatisfiedLinkError e) {

			Log.e("MirrorLib", "Couldn't load lib: "  + e.getMessage());
	    
		}

		
	} 
	public native void  jni_i2c_open(); 	
	public native void  jni_i2c_write(int addr, int[] buf, int len); 
	public native void  jni_i2c_writedev(int dev, int addr, int[] buf, int len); 
	public native int[] jni_i2c_read(int addr, int len); 	
	public native void  jni_i2c_close(); 
	public native void  jni_i2c_reset();
}
