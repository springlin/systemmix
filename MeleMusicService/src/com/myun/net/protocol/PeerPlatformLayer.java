package com.myun.net.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Properties;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

public  class PeerPlatformLayer {		
	private static final String TAG = "PeerPlatformAdapter";


    public static int injectKeyEvent(byte[] msg) {
    	
    	int keyCode=-1;
    	try {
    		
    		DataInputStream is = new DataInputStream(new ByteArrayInputStream(msg));
    		PeerMessage.initResolverHead(is);
    		
    	    int action  = is.readInt();
			keyCode = is.readInt();
		
			Log.i(TAG, "injectNativeKeyEvent atction:"+action+"  keyCode "+keyCode);

			
			//sendKeyCode(keyCode);


			
		} catch (Exception e) {
			e.printStackTrace();
		} 	
        return keyCode;
    }
    
    public static MotionEvent injectMotionEvent(byte[] msg) {
    	
    	MotionEvent e=null;
    	try {

    		DataInputStream   is = new DataInputStream(new ByteArrayInputStream(msg));
    		PeerMessage.initResolverHead(is);
		    
			int action     = is.readInt();
			int pointCount = is.readInt();
			
           
            
			for (int i = 0; i < pointCount; i++){  
		        int x          = is.readInt();
				int y          = is.readInt();

				
				//Log.i(TAG, "injectMotionEvent atction:"+action+"  x "+x+" y "+y);
				
				//x=1920*x/1280;
				//y=1080*y/720;
			

				e = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, x, y, 0);	 
			   
				return e;
				//sendMouseEvent(e);
					 
				
			}

			
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
        return e;
    }
    
    private static void sendKeyCode(final int keyCode){  
        new Thread () {  
            public void run() {  
                try {  
                    Instrumentation inst = new Instrumentation();  
                    inst.sendKeyDownUpSync(keyCode);  
                } catch (Exception e) {  
                    Log.e("Exception when sendPointerSync", e.toString());  
                }  
            }  
        }.start();  
    }
    private static void sendKeyCodeA(final KeyEvent keyCode){  
        new Thread () {  
            public void run() {  
                try {  
                    Instrumentation inst = new Instrumentation();  
                    inst.sendKeySync(keyCode);  
                } catch (Exception e) {  
                    Log.e("Exception when sendPointerSync", e.toString());  
                }  
            }  
        }.start();  
    } 
    private static void sendMouseEvent(final MotionEvent event){  
        new Thread () {  
            public void run() {  
            	
            	// _sendMouseEvent(event);
                try { 
                	
  
                    Instrumentation inst = new Instrumentation();  
                //    inst.setInTouchMode(true);
                    inst.sendPointerSync(event);  
                } catch (Exception e) {  
                    Log.e("Exception when sendPointerSync", e.toString());  
                }  
                
                
            }  
        }.start();  
    }   
    synchronized private static void _sendMouseEvent(final MotionEvent event){  

                try { 
                	
  
                    Instrumentation inst = new Instrumentation();  
                    inst.setInTouchMode(true);
                    inst.sendPointerSync(event);  
                   // Thread.sleep(10);
                } catch (Exception e) {  
                    Log.e("Exception when sendPointerSync", e.toString());  
                }  
                
                

    }   
    
    
    

 


	
}
