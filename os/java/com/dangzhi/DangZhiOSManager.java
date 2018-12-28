package com.dangzhi;

import android.content.Context;
import android.os.IDangZhiService;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DangZhiOSManager {
	
	  public  final String TAG="DangZhiOSManager";
	  private static Context context=null;
    private IDangZhiService mService;
    public  static final int DANGS_AI_MIC=0x01;
    public  static final int DANGS_BLE_MIC=0x02;
    public  static String Dangs = "dangzhi";
    private static DansCallBack dangscallback=null;
    private static FileDescriptor mFd=null;
    private static boolean exit=false;

    public DangZhiOSManager(Context ctx, IDangZhiService service) {    
        this.mService = service;
        this.context=ctx;    	  
        Log.d(TAG,"DangZhiOSManager this : " + this+" IDangZhiService "+service);
    }

    public void setAmpliflerVol(int value) {
        try {
            this.mService.setAmpliflerVol(value);
        } catch (Exception var3) {
            Log.e(TAG, var3.toString());
            var3.printStackTrace();
        }

    }

    public int getAmpliflerVol() {
        try {
            return this.mService.getAmpliflerVol();
        } catch (Exception var2) {
            Log.e(TAG, var2.toString());
            var2.printStackTrace();
            
        }
        return -1;
    }

    public boolean powerAmplifler(boolean flag) {
        try {
            return this.mService.powerAmplifler(flag);
        } catch (Exception var3) {
            Log.e(TAG, var3.toString());
            var3.printStackTrace();

        }
        return false;
    }

    public int setAmpliflerTone(int type, int val) {
        try {
            this.mService.setAmpliflerTone(type, val);
            return 0;
        } catch (Exception var4) {
            Log.e(TAG, var4.toString());
            var4.printStackTrace();            
        }
        return -1;
    }
    
    public int getFanSpeed() {
        try {
            return this.mService.getFanSpeed();
        } catch (Exception var2) {
            Log.e(TAG, var2.toString());
            var2.printStackTrace();           
        }
        return -1;
    }

    public boolean setFanSpeed(int var) {
        try {
            return this.mService.setFanSpeed(var);
        } catch (Exception var3) {
            Log.e(TAG, var3.toString());
            var3.printStackTrace();
        }
        return false;        
    }

    public int getTemperature() {
        try {
            return this.mService.getTemperature();
        } catch (Exception var4) {
            Log.e(TAG, var4.toString());
            var4.printStackTrace();
            
        }
        return -1;
    }
    
    public int setMicMode(int mode){
    	
    	  if(mode==DANGS_AI_MIC){

    	  	    setProperty("persist.sys.dangs.mic","primary");
    	  }else if(mode==DANGS_BLE_MIC){

              setProperty("persist.sys.dangs.mic","blehid");
              
    	  }
    	  return 1;
    }
    public int getMicMode(){

    	 String str=getProperty("persist.sys.dangs.mic");
    	 if(str!=null && str.equals("primary")){
    	 	
    	 	  return DANGS_AI_MIC;
    	 	
    	 }else if(str!=null && str.equals("blehid")){
    	 	
    	 	  return DANGS_BLE_MIC;
    	 	
    	 }
       return -1; 
    }
		public  String getProperty(String key){
		    return nativeGetProperty(key);
		}


		public  void setProperty(String key, String value){
			if(key != null && value != null){
				nativeSetProperty(key,value);
			}
		}
   	public  int setBootAnimation(String path){
     		Log.d(TAG,"setBootanimation : " + path);
        return nativeSetBootAnimation(path);
    }
    


    public void closeSerialPort(){

    	  exit=false;
    	  dangscallback=null;

    }

    public boolean openSerialPort(DansCallBack dangscallback){

        this.dangscallback=dangscallback;
        
        if(mFd!=null && mFd.valid()){
        	  Log.d(TAG,"mFd is opened "); 
        	  return false;
        } 
        
        mFd=serialportopen_native("/dev/ttyS1", 115200, 0);
        if(mFd.valid()){
        	new ReadThread().start();
        	return true;        	      	
        }
        return false;
    }
    
    public void writeSerialPort(byte[] buffer, int offset, int size){

         if(mFd!=null && mFd.valid()){

             try {
                 FileOutputStream mFileOutputStream=new FileOutputStream(mFd);
                 mFileOutputStream.write(buffer, offset, size);
                 mFileOutputStream.flush();
                 mFileOutputStream.close();  
             }catch (IOException e){
                 
             }

             
         }

    }

    
    public String getHPstate(){

        try {
        	
            FileReader fr = new FileReader("/sys/class/switch/h2w/state");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
           
            return text;
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
    public void setOnDansCallBackinfo(DansCallBack dangscallback){

        this.dangscallback=dangscallback;

    }
    public interface DansCallBack{

         void onDansCallBackinfo(int code, String info);

    }


	  public  int setBootLogo(String path){
		    Log.d(TAG,"setBootlogo : " + path);
        return nativeSetBootLogo(path);
    } 
    
    public boolean updateGX8008(String mcupath, String dsppath, String cfgpath){
    	
    	 return nativeUpdateGX8008(mcupath, dsppath, cfgpath);
    	 
    }
    public String getGX8008Version(){
    	
    	   
    	  String version=null; 
    	  write_node("/sys/class/gpio/gpio14/direction", "out");
        write_node("/sys/class/gpio/gpio14/value", "0");
        try{
            Thread.sleep(20);
        } catch (Exception e) {

        }

        mFd=serialportopen_native("/dev/ttyS1", 115200, 0);

        byte[] buffer = new byte[512];
        FileInputStream mFileInputStream = new FileInputStream(mFd);
        int size = 0,  cn=0;
        exit=true;
        write_node("/sys/class/gpio/gpio14/value", "1");
        while(exit &&  cn<200){
            try{
                if(mFileInputStream.available()<2){
                    Thread.sleep(10);
                    cn++;
                    continue;
                }
                cn=0;
                size+= mFileInputStream.read(buffer, size, 512-size);
                String info=new String(buffer, 0, size);
                if(info.contains("->Dsp")){

                    version=getFirmwareVersion("Cpu", info);
                    Log.d(TAG, size+"=>"+info);
                    break;
                }
                if(size>=512) break;

            } catch (Exception e) {
               Log.d(TAG, e.toString());
            }
        }
        Log.d(TAG, "find firmware version "+ version);
        if(mFd!=null) serialportclose_native(mFd);
        mFd=null;
        exit=false;
        return version;
    }
    
    public static void write_node(String path, String str){

        try {
            FileWriter fileWritter = new FileWriter(path,true);
            fileWritter.write(str);
            fileWritter.close();
        }catch (IOException e){


        }

    }    
    public static String read_node(String path){


        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            return br.readLine();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return "";

    }
    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();

//		    	  write_node("/sys/class/gpio/gpio14/direction", "out");
//		        write_node("/sys/class/gpio/gpio14/value", "0");
//		        try{
//		            Thread.sleep(20);
//		        } catch (Exception e) {
//
//		        }
//		        write_node("/sys/class/gpio/gpio14/value", "1");
		        
            byte[] buffer = new byte[512];
            FileInputStream mFileInputStream = new FileInputStream(mFd);
            exit=true;
            while (exit && read_node("/sys/class/gpio/gpio14/value").equals("1")){



                try {
                	
                	 if(mFileInputStream.available()<=0){
                        Thread.sleep(15);
                        continue;
                   }
                   int size = mFileInputStream.read(buffer, 0, 512);
                   if (size > 2){

                        String info=new String(buffer, 0, size), version=null, var=null;
         
//                        if( dangscallback!=null && (version=getFirmwareVersion("Cpu", info))!=null ){
//                        	
//                        	  dangscallback.onDansCallBackinfo(2, version);
//                        	  exit=false;   
//                        	                      	
//                        }else 
                        if( dangscallback!=null && (var=getVar(info))!=null ) {
                        	
                            dangscallback.onDansCallBackinfo(1, var);
                            
                        }
                        Log.d(TAG, size+" "+new String(buffer, 0, size));

                   }
                   
                   
                   
                } catch (Exception e) {
                    Log.e(TAG, "serialport error " +e.toString());
                }

            }
            serialportclose_native(mFd);
            dangscallback=null;
            mFd=null;
            Log.e(TAG, " serialport read thread exit ");
        }
    }
    
    public void setSoundSleepTimes(int times){ 	
    	  android.provider.Settings.System.putString(context.getContentResolver(), "dangs.settings.aisound.sleeptime", ""+times);
    }
    
    public int getSoundSleepTimes(){   	
        return android.provider.Settings.System.getInt(context.getContentResolver(), "dangs.settings.aisound.sleeptime", 30);
    }
    
	 	public static String runShell(String cmd){
			return nativeRunShell(cmd);	  	
		}   
		
    private String getVar(String str){


        if(str==null || str.equals("")  || !str.contains("kws")) return null;
        String items[]=str.split("\\s+");
        if(items.length>2){
            return items[2];
        }
        return null;

    }
    private String getFirmwareVersion(String key,  String str){

        if(str==null || str.equals("") || key==null || key.equals("")) return null;
        int start=str.indexOf("["), end=str.indexOf("]");

        if( start==-1 || end==-1  || start>=end ) return null;

        String str1=str.substring(start, end), items[]=str1.split(" ");
        if(str1.contains(key) && items.length>2) return items[2];
        return null;
    }
    static {
    	System.loadLibrary("usbapi");
		  System.loadLibrary("admanager_jni");
		  nativeInit();
    }





		private  static native void nativeInit();
		private  native int nativeSetBootAnimation(String pathName);
		private  native int nativeSetBootLogo(String pathName);
	  private  native String nativeGetProperty(String key);
		private  native int nativeSetProperty(String key, String value);
    private  native FileDescriptor serialportopen_native(String fdpath, int baudrate, int flags);     
    private  native void    serialportclose_native(FileDescriptor fd);  
    private  native boolean   nativeUpdateGX8008(String mcupath, String dsppath, String cfgpath); 
    private static native String nativeRunShell(String cmd); 
}