package com.mele.musicdaemon;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

import com.farcore.AutoSuspend.DaemonService;
import com.farcore.AutoSuspend.R;











import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ILockInterface;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

public class RemoteService extends Service {
	private final static String TAG = "RemoteService";
    public final int port=8086;
    int sh=0, sw=0;
    public I2C i2c=null;
//    private ILockInterface mLockService;
//    boolean hasResource=false;
   // public key2touch keytouch=null;
	@Override
	public void onCreate() {
		
		super.onCreate();
		//keytouch=new key2touch();
		//keytouch.init();
         getWindowDisplay(this);
         i2c=new I2C(this);
       //  mLockService=ILockInterface.Stub.asInterface(ServiceManager.getService("MeleLock"));
         //jni_init_class();
		//startHttpServer("/",port);
         
         MemoryUtils.InitMemoryUtils(this);
         
		 Notification notification = new Notification(R.drawable.ic_launcher, "netservice is running",System.currentTimeMillis());
		 PendingIntent pintent=PendingIntent.getService(this, 0, new Intent("com.mele.musicservice"), 0);
		 notification.setLatestEventInfo(this, "NetService","netservice is running", pintent);
		
		//如果 id 为 0 ，那么状态栏的 notification 将不会显示。
		 startForeground(1, notification);
		 
		 Log.e(TAG, "*******onCreate  RemoteService .......");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//        if(hasResource){
//            try {
//               // mLockService.releaseResource();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
		System.exit(0);
	}
	Handler msghandler = new Handler(){  
	        @Override  
	        public void handleMessage(Message msg) {  

	        	
	        }  
	 };
	    
	 Toast  toast=null;  	
	 public void showmsg(final String msg){
		     msghandler.post(new Runnable(){  
		         public void run(){  
		             if(toast!=null) toast.cancel();
		             toast=Toast.makeText(getApplicationContext(), " "+msg, Toast.LENGTH_LONG);

		             toast.show();
		         }  
		     }); 
	}	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		

		 Log.e(TAG, "onStartCommand  RemoteService .......");
		 return START_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "OnBind RemoteServiceBinder");
		return new RemoteServiceBinder();
	}
    public  class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
        	Log.i(TAG, "installApk finish"+ returnCode);
        	installcode=returnCode;
        	
        }
    };
//    
//    public Semaphore semaphore=new Semaphore();
    public int  installcode=-10;

	private  class RemoteServiceBinder extends RemoteServiceAIDL.Stub{




		@Override
		public String installApp(String path, String packagename) throws RemoteException {
			
			PeerTools.installApk(RemoteService.this, path, packagename, new PackageInstallObserver());
		    while(installcode==-10){
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
			return installcode+"";
		}

    public void execCommand(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
		@Override
		public void startMirror(String ip, int port) throws RemoteException {
//            try {
//                execCommand("killdlna.sh");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//			try {
//				execCommand("wm density 320");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			Log.e(TAG, "startMirror");
		    //jnistart(port,ip);  
		}

		@Override
		public void stopMirror() throws RemoteException {
			
			
			    Log.e(TAG, "stopMirror");
			  //  jnistop();
//            if(hasResource) {
//                try {
//                    mLockService.releaseResource();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//                File file=new File("/data/system/runningMirror");
//                file.delete();
//                Intent intent=new Intent("com.mele.mirrocast.STOP");
//                sendBroadcast(intent);
//			    
    
			    System.exit(0);
		}

		@Override
		public void keyCode(int code) throws RemoteException {

			  sendKeyCode(code);
			   
		}

		@Override
		public void MouseEvent(float x, float y, int action){
		
		        
		
	            //x=x*sw/1280;
	           // y=y*sh/720;
	          //  log("x..."+x+"   y..."+y);
			    MotionEvent e=MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, x, y, 0);	 
			    sendMouseEvent(e);
	    }

		@Override
		public void killApp(String packagename) throws RemoteException {
		
			    RemoteService.this.killApp(packagename);
			    try {
					execCommand("wm density 160");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
		}

		@Override
		public void ioctrl(String cmd, String var) throws RemoteException {
		             
			      log("cmd ..."+cmd+"   var..."+var);
			      if(cmd==null || cmd.equals("")) return ;
			      if(cmd.equals("uninstall")){
			    	  
			    	  PeerTools.uninstallApk(RemoteService.this, var);
			    	      
			    	  
			    	  
			      }else if(cmd.equals("clearmemory")){
			    	  
			    	//  MemoryUtils.cleanMemory(RemoteService.this, var);
			    	  
			      }else if(cmd.equals("wm")){
			    	  try {
					    	  if(var.equals("160")){	
								 execCommand("wm density 160");	  
					    	  }else if(var.equals("320")){
					    		  execCommand("wm density 320");	 
					    	  }else if(var.equals("240")){
					    		  execCommand("wm density 240");	 
					    	  }
			    	  } catch (IOException e) {
							e.printStackTrace();
					  }
			      }
			       
			
		}

		@Override
		public void jni_i2c_open() throws RemoteException {
			
			i2c.jni_i2c_reset();
		}

		@Override
		public void jni_i2c_write(int addr, int[] buf, int len)
				throws RemoteException {
			    
			    i2c.jni_i2c_write(addr, buf, len);
			
		}
		@Override
		public void jni_i2c_writedev(int dev,int addr, int[] buf, int len)
				throws RemoteException {
			    
			    i2c.jni_i2c_writedev(dev, addr, buf, len);
			
		}
		@Override
		public int[] jni_i2c_read(int addr, int len) throws RemoteException {
			int buf[]=i2c.jni_i2c_read(addr, len);
			log("read value ...."+buf[0]);
			return buf;
		}

		@Override
		public void jni_i2c_close() throws RemoteException {
			
			
		}

	 }
	 public void killApp(String packageName){
		 
		//Log.e(TAG, "=====killApp: "+packageName);
		if (packageName != null && !"".equals(packageName)) {
			final ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);   
			am.forceStopPackage(packageName);
			Log.e(TAG, "killApp: "+packageName);
		}
	 }
	 public  void getWindowDisplay(Context context){
		   DisplayMetrics dm = new DisplayMetrics();
		   WindowManager windowMgr = (WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		   windowMgr.getDefaultDisplay().getMetrics(dm);

   		   sw = dm.widthPixels;
           sh = dm.heightPixels;
		   if(dm.widthPixels<dm.heightPixels){
			   		   sh = dm.widthPixels;
		               sw = dm.heightPixels;
		   }
		   
		   
		   Log.e(TAG, "ScreenInfo ===> sw:"+sw+"  sh:"+sh);
		   
		   
		   
	}
    private  void sendKeyCode(final int keyCode){  
        new Thread () {  
            public void run() { 
    	
    	        Log.i(TAG, keyCode+"keyCode");
                try {  
                    Instrumentation inst = new Instrumentation();  
                    inst.sendKeyDownUpSync(keyCode);  
                } catch (Exception e) {  
                    Log.e("Exception when sendPointerSync", e.toString());  
                }  
            }  
        }.start();  
    }
    private void sendMouseEvent(final MotionEvent event){  
    	
	    //	 keytouch.set_touch(event.getDeviceId(), (int)event.getX(), (int)event.getY(), event.getAction());
	        new Thread () {  
	            public void run() {  
        	
	                   mouseEvent(event);

	            }  
	        }.start();  
    }
    public void updateMirrorStatus(String var){
    	
    	log("updateMirrorStatus ....."+var);
		Intent i = new Intent("com.mirror.status");
		i.setData(Uri.parse("file://alarm"));
		i.putExtra("ip", var);
		sendBroadcast(i);
    	 
    	
    }
    private synchronized void mouseEvent(final MotionEvent event){
          try { 
          	
            //  Log.i(TAG, "x"+event.getX()+"y"+event.getY()+"action"+event.getAction());
              Instrumentation inst = new Instrumentation();  
          //    inst.setInTouchMode(true);
              inst.sendPointerSync(event);  
          } catch (Exception e) {  
              Log.e("Exception when sendPointerSync", e.toString());  
          }  
    }
    
    
    public void log(String msg){
    	Log.i(TAG, msg);
    }
	static {

		
		try {

			// System.loadLibrary("music_mirror");
		      
		    	    	
		    
		} catch(UnsatisfiedLinkError e) {

			Log.e("MirrorLib", "Couldn't load lib: "  + e.getMessage());
	    
		}

	}  	
//     public native void    jni_init_class();
//	 public native void    jnistart(int type, String ip);           //开始连接
//	 public native void    jnistop();           


}
