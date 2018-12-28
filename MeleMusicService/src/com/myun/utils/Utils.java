package com.myun.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONStringer;

import com.myun.core.WifiAdmin;



import com.myun.net.protocol.PeerTools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class Utils {

	public static String TAG="MusicService";
	public static int HttpPort=9090;
	public static Handler mhandler=null;
	public static Context context=null;
	public static final int Cmd_Ret=0x001;
	public static final int Cmd_SDCard=0x002;
	public static final int Cmd_Del=0x003;
	public static final int Cmd_Time_Clock=0x004;
	public static final int Cmd_Time_Key=0x005;
	public static final int Cmd_Time_DAR=0x006;	
	public static final int Cmd_Sleep=0x007;	
	public static final int Cmd_Sound_Mode=0x008;	
	public static final int Cmd_AP=0x009;
	public static final int Cmd_LED=0x00A;
	public static final int Cmd_SECOND=0x00B;
	
	public static final int PLAY_NEXT=0x004;
	public static final int PLAY_PRE=0x005;
	public static SQLiteHelper sqlitehelper=null;
	public static SharedPreferences config=null;
	public static final String SPEAKER_TIME_CLOCK="com.speaker.time.clock";
	public static final String SETTING_TIME_CLOCK="com.setting.time.clock";
	public static final String ADX_STATUS_BC_NAME = "com.myun.ADX_STATUS_BC";

	public static String storage_card=Environment.getExternalStorageDirectory().getPath();
	public static String music_root_path=Environment.getExternalStorageDirectory().getPath()+"/music_service/device/";
	public static String music_app_path=Environment.getExternalStorageDirectory().getPath()+"/music_service/app";
	public static String music_usb=Environment.getExternalStorageDirectory().getPath()+"/music_usb/";
	
	public static String extern_sdcard_path="/storage/external_storage/sdcard1";
	
	public static int count_id=0;
	public static final long DAY = 1000L * 60 * 60 * 24;
	public static final int CLOCKID=128;
	public static int sw=720, sh=1280;
	public static long Capacity=1024L*1024L*64; //*MB
    //0x2A
    public static int LEQ[][]= {
    	 {0x00, 0x7F, 0x2F, 0x08, 0x0F, 0x04, 0x6A, 0x3D, 0x00, 0x7C, 0x6E, 0x70, 0x00, 0xFB, 0x8F, 0xF0, 0x0F, 0x84, 0x5C, 0xB4},//-8
    	 {0x00, 0x7F, 0x54, 0x30, 0x0F, 0x04, 0x0A, 0x46, 0x00, 0x7C, 0xA8, 0xC7, 0x00, 0xFB, 0xF1, 0x3E, 0x0F, 0x83, 0xFE, 0x8C},//-7
    	 {0x00, 0x7F, 0x77, 0xAC, 0x0F, 0x03, 0xA4, 0x35, 0x00, 0x7C, 0xEA, 0xB8, 0x00, 0xFC, 0x58, 0x82, 0x0F, 0x83, 0x9A, 0x53},//-6
    	 {0x00, 0x7F, 0x8E, 0x53, 0x0F, 0x03, 0x8B, 0xD9, 0x00, 0x7C, 0xEC, 0xD9, 0x00, 0xFC, 0x71, 0x6C, 0x0F, 0x83, 0x82, 0x19},//-5
    	 {0x00, 0x7F, 0xA5, 0x24, 0x0F, 0x03, 0x72, 0x93, 0x00, 0x7C, 0xEF, 0xB9, 0x00, 0xFC, 0x8B, 0x41, 0x0F, 0x83, 0x68, 0xF6},//-4
    	 {0x00, 0x7F, 0xBB, 0xE5, 0x0F, 0x03, 0x5A, 0x06, 0x00, 0x7C, 0xF1, 0xF5, 0x00, 0xFC, 0xA4, 0x5A, 0x0F, 0x83, 0x50, 0x85},//-3
    	 {0x00, 0x7F, 0xD2, 0x9D, 0x0F, 0x03, 0x42, 0x2F, 0x00, 0x7C, 0xF3, 0x8E, 0x00, 0xFC, 0xBC, 0xBD, 0x0F, 0x83, 0x38, 0xC0},//-2
    	 {0x00, 0x7F, 0xE9, 0x4E, 0x0F, 0x03, 0x2B, 0x08, 0x00, 0x7C, 0xF4, 0x83, 0x00, 0xFC, 0xD4, 0x6E, 0x0F, 0x83, 0x21, 0xA4},//-1
    	 {0x00,0x80,0x00,0x00,0x0F,0x01,0x59,0x66,0x00,0x7E,0xAA,0xA3,0x00,0xFE,0xA6,0x9A,0x0F,0x81,0x55,0x5D },//0q
    	 {0x00,0x80,0x14,0xD3,0x0F,0x01,0x59,0x66,0x00,0x7E,0x95,0xCF,0x00,0xFE,0xA6,0x9A,0x0F,0x81,0x55,0x5D },//1
    	 {0x00,0x80,0x2C,0x31,0x0F,0x01,0x59,0x66,0x00,0x7E,0x7E,0x71,0x00,0xFE,0xA6,0x9A,0x0F,0x81,0x55,0x5D },//2
    	 {0x00,0x80,0x46,0x69,0x0F,0x01,0x59,0x66,0x00,0x7E,0x64,0x39,0x00,0xFE,0xA6,0x9A,0x0F,0x81,0x55,0x5D },//3
    	 {0x00,0x80,0x73,0xDB,0x0F,0x01,0x59,0x66,0x00,0x7E,0x36,0xC7,0x00,0xFE,0xA6,0x9A,0x0F,0x81,0x55,0x5D },//4
    	 {0x00, 0x80, 0x72, 0x12, 0x0F, 0x02, 0xAD, 0x9B, 0x00, 0x7C, 0xEC, 0xD9, 0x00, 0xFD, 0x55, 0x22, 0x0F, 0x82, 0xA3, 0xD3},//5
    	 {0x00, 0x80, 0x88, 0xE5, 0x0F, 0x02, 0x99, 0x9C, 0x00, 0x7C, 0xEA, 0xB8, 0x00, 0xFD, 0x69, 0xB0, 0x0F, 0x82, 0x8F, 0xAF},//6
    	 {0x00, 0x80, 0xAC, 0xB6, 0x0F, 0x02, 0xBA, 0xCD, 0x00, 0x7C, 0xA8, 0xC7, 0x00, 0xFD, 0x49, 0xB5, 0x0F, 0x82, 0xAF, 0x04},//7
    	 {0x00, 0x80, 0xD2, 0x4F, 0x0F, 0x02, 0xD2, 0xBC, 0x00, 0x7C, 0x6E, 0x71, 0x00, 0xFD, 0x33, 0x21, 0x0F, 0x82, 0xC5, 0x1D},//8

    };
	//0x2F
    public static int HEQ[][]= {
    	 {0x00, 0x48, 0xB5, 0x06, 0x0F, 0xF1, 0x9C, 0x62, 0x00, 0x0C, 0xFA, 0x85, 0x00, 0x5A, 0x0A, 0x40, 0x0F, 0xDE, 0xA9, 0xD2},//-3  // -8
         {0x00, 0x4F, 0x16, 0xE5, 0x0F, 0xF3, 0x49, 0x73, 0x00, 0x0D, 0xEE, 0x5F, 0x00, 0x4E, 0x33, 0x4B, 0x0F, 0xE1, 0x7D, 0xFE},//-2 //-7
         {0X00, 0x55, 0xF9, 0xEC, 0x0F, 0xF6, 0xE8, 0x41, 0x00, 0x0E, 0xEB, 0xD7, 0x00, 0x3F, 0xD3, 0xCD, 0x0F, 0xE4, 0x5E, 0x2E}, //-6
         {0x00, 0x5B, 0xD7, 0x3F, 0x0F, 0xF3, 0x03, 0x15, 0x00, 0x10, 0x15, 0x10, 0x00, 0x3C, 0x0A, 0x0C, 0x0F, 0xE5, 0x06, 0x8F},//-1 //-5
         {0X00, 0x62, 0x24, 0x72, 0x0F, 0xEE, 0xD5, 0x9A, 0x00, 0x11, 0x5E, 0xAC, 0x00, 0x37, 0xF7, 0x7F, 0x0F, 0xE5, 0xAF, 0xC7},//-4
         {0x00, 0x68, 0xE0, 0xE9, 0x0F, 0xEA, 0x27, 0xD0, 0x00, 0x12, 0xCC, 0xBA, 0x00, 0x33, 0xDC, 0x7E, 0x0F, 0xE6, 0x4E, 0x0F},//+1 //-3
         {0X00, 0x70, 0x14, 0x33, 0x0F, 0xE4, 0xED, 0xE2, 0x00, 0x14, 0x63, 0x3E, 0x00, 0x2F, 0xB9, 0x93, 0x0F, 0xE6, 0xE1, 0x19},//-2
	     {0x00,0x80,0x14,0xD3,0x0F,0x01,0x59,0x66,0x00,0x7E,0x95,0xCF,0x00,0xFE,0xA6,0x9A,0x0F,0x81,0x55,0x5D},//+2, //-1
	     {0x00,0x80,0x14,0xD3,0x0F,0x01,0x59,0x66,0x00,0x7E,0x95,0xCF,0x00,0xFE,0xA6,0x9A,0x0F,0x81,0x55,0x5D},//+3, // 0
	     {0x00,0x80,0x14,0xD3,0x0F,0x01,0x59,0x66,0x00,0x7E,0x95,0xCF,0x00,0xFE,0xA6,0x9A,0x0F,0x81,0x55,0x5D},//+4, //+1
	     {0x00, 0x92, 0x2E, 0xC6, 0x0F, 0xC9, 0x7E, 0xE3, 0x00, 0x1C, 0xB0, 0x6E, 0x00, 0x1E, 0xEA, 0x8F, 0x0F, 0xE8, 0xB7, 0x5A},//+5,  // +2
	     {0x00, 0x9C, 0x38, 0x00, 0x0F, 0xC0, 0xB4, 0x96, 0x00, 0x1F, 0x5C, 0x1C, 0x00, 0x1A, 0xA9, 0x09, 0x0F, 0xE9, 0x0E, 0x44},//+6, // +3
	     {0x00, 0xA6, 0xF0, 0xEE, 0x0F, 0xB7, 0x01, 0xAE, 0x00, 0x22, 0x51, 0x92, 0x00, 0x16, 0x63, 0x4B, 0x0F, 0xE9, 0x58, 0x86},//+7, // +4
	     {0x00, 0xB2, 0x65, 0x48, 0x0F, 0xAC, 0x52, 0x85, 0x00, 0x25, 0x98, 0x39, 0x00, 0x12, 0x19, 0xFD, 0x0F, 0xE9, 0x95, 0xFB},//+8// +5
	     {0x00, 0xBE, 0x90, 0x6F, 0x0F, 0xA0, 0xF9, 0x95, 0x00, 0x29, 0x23, 0x62, 0x00, 0x0D, 0x89, 0x82, 0x0F, 0xE9, 0xC9, 0x17},//+6
	     {0x00, 0xCF, 0x28, 0x6B, 0x0F, 0x81, 0x70, 0x5B, 0x00, 0x31, 0x5F, 0xE1, 0x00, 0x14, 0x93, 0x3C, 0x0F, 0xE9, 0x74, 0x1C},//+7
	     {0x00, 0xE1, 0x57, 0xA7, 0x0F, 0x61, 0x7C, 0x53, 0x00, 0x3A, 0xB0, 0x51, 0x00, 0x19, 0x54, 0xF6, 0x0F, 0xE9, 0x26, 0xBF},//+8
				    	
    };
    
    public static int L_NOOK[][]= {
   
        {0x00,0x00,0x06,0xA8,0x00,0x00,0x0D,0x50,0x00,0x00,0x06,0xA8,0x00,0xFA,0xC9,0x76,0x0F,0x85,0x1B,0xE9}, //2a
        {0x00,0x00,0x06,0xA8,0x00,0x00,0x0D,0x50,0x00,0x00,0x06,0xA8,0x00,0xFA,0xC9,0x76,0x0F,0x85,0x1B,0xE9},//31
    };
    
    public static int L_OK[][]= {
   	     {0x00,0x00,0x0C,0x4A,0x00,0x00,0x18,0x94,0x00,0x00,0x0C,0x4A,0x00,0xF8,0xE4,0x62,0x0F,0x86,0xEA,0x75}, //2a
         {0x00,0x00,0x0C,0x4A,0x00,0x00,0x18,0x94,0x00,0x00,0x0C,0x4A,0x00,0xF8,0xE4,0x62,0x0F,0x86,0xEA,0x75},//31
    };
    
    public static int LOWL[][]= {
    	   
        {0x00,0x7F,0xB8,0x71,0x0F,0x00,0x8F,0x1E,0x00,0x7F,0xB8,0x71,0x00,0xFF,0x70,0x70,0x0F,0x80,0x8E,0xAB}, //2a
        {0x00,0x7F,0x94,0xC4,0x0F,0x00,0xD6,0x77,0x00,0x7F,0x94,0xC4,0x00,0xFF,0x28,0xD5,0x0F,0x80,0xD5,0xC4},//31
        {0x00,0x7F,0x48,0xB2,0x0F,0x01,0x6E,0x9C,0x00,0x7F,0x48,0xB2,0x00,0xFE,0x90,0x63,0x0F,0x81,0x6D,0x9A}
    };
    
    public static int LOWR[][]= {
   	     {0x00,0x7F,0xB8,0x71,0x0F,0x00,0x8F,0x1E,0x00,0x7F,0xB8,0x71,0x00,0xFF,0x70,0x70,0x0F,0x80,0x8E,0xAB}, //2a
         {0x00,0x7F,0x94,0xC4,0x0F,0x00,0xD6,0x77,0x00,0x7F,0x94,0xC4,0x00,0xFF,0x28,0xD5,0x0F,0x80,0xD5,0xC4},
         {0x00,0x7F,0x48,0xB2,0x0F,0x01,0x6E,0x9C,0x00,0x7F,0x48,0xB2,0x00,0xFE,0x90,0x63,0x0F,0x81,0x6D,0x9A}//31
    };
    
	public String musicpackage[]={"com.kugou.android","com.tencent.qqmusic","cn.kuwo.player","com.sds.android.ttpod",
            "com.ting.mp3.android","fm.xiami.main","cmccwm.mobilemusic","com.netease.cloudmusic",
            "com.ximalaya.ting.android","fm.qingting.qtradio","com.duotin.fm","com.douban.radio",
            "com.yinyuetai.ui"};	
	public static int t_Volume[]={
			0xff,	//0	// 0 0
//			0x80,		// -40dB
//			0x7a,		// V2

//			0x74,
//			0x6e,
//			0x68,		// V5
			
//			0x62,
//			0x5c,
//			0x56,		// V8
			
//			0x50,
//			0x4c,
//			0x48,		// V11

//			0x44,		// V12 -10dB
//			0x41,
//			0x3e,		// V14
			
//			0x3c,
//			0x3a,
//			0x38,	//1	// V17 //1
			
//			0x36,
//			0x34,
//			0x32,		// V20
			
//			0x30,
//			0x2e,
//			0x2c,		// V23
			
//			0x2a,
//			0x28,  //2     
//			0x26,		// V26
			
//			0x24,
//			0x22, //3
//			0x20,	//4	// V29
//			0x1f,   //5
			//0x1d,   //6
			//0x1b,   //7
			0x1a,
			//0x19,
			0x18,	//8	// V32 14dB
			0x17,
			0x16,   //9
			0x15,
			0x14,   //10 
			0x13,
			0x12,	//11	// V32 14dB
			0x11,
			0x10,   //12
			0x0f,
			0x0e,   //13
			0x0d,
			0x0c,   //14
			
			0x0a    //15
	};	

	public static int out_Volume[]={
		0xff,		// 0
//		0x80,		// -40dB
		0x7a,		// V2

//		0x74,
		0x6e,
//		0x68,		// V5
		
		0x62,
//		0x5c,
		0x56,		// V8
		
		0x50,
//		0x4c,
		0x48,		// V11

		0x44,		// V12 -10dB
		0x41,
		0x3e,		// V14
		
		0x3c,
//		0x3a,
		0x38,		// V17
		
		0x36,
		0x34,
		0x32,		// V20
		
		0x30,
		0x2e,
		0x2c,		// V23
		
		0x2a,
		0x28,
		0x26,		// V26
		
		0x24,
		0x22,
		0x20,		// V29
		
		0x1e,
		0x1c,
		0x18,		// V32 14dB
		
		

	//	0x17,

		0x15,

		0x13,
       // 0x12,
		0x11,
		//0x10,   //12
		0x0f,
		//0x0e,   //13
		0x0d,
		//0x0c,   //14
		
		0x0a    //15
    };		
    public static void getInstance(Context context, Handler handler){
    	
    	
    	      mhandler=handler;
    	      Utils.context=context;
    	      sqlitehelper=new SQLiteHelper(context,"melemusic.db", null, 1);
    	      sqlitehelper.sdcard_db=sqlitehelper.getWritableDatabase();
    	      config = context.getSharedPreferences("musicconfig", Activity.MODE_PRIVATE);
    	      getWindowDisplay(context);
    	      
    	      checkMkdir(storage_card);
    }
	
    public static String getId(){
		   
		   return Math.abs(new Random().nextInt(10000))+""+(count_id++);
		   
	}	
    public static String getVar(String cmd){
	   return config.getString(cmd, "0"); 
    }
    
	public static int getVarInt(String cmd){
	 	   return config.getInt(cmd, -1); 
    }
    public static void setVar(String cmd, String var){
	   SharedPreferences.Editor editor = config.edit(); 
	   editor.putString(cmd, var); 
	   editor.commit();
    }
    public static void setVar(String cmd, int var){
	   SharedPreferences.Editor editor = config.edit(); 
	   editor.putInt(cmd, var); 
	   editor.commit(); ; 
    }
	public static String getInfoItem(String info, String name){
		
		
		int indef=info.indexOf(name);
		if(indef==-1) return null;
		
		return info.substring(indef+1+name.length(), info.indexOf("\n", indef));
		
	}
	public  static int  getMIMEType(String path) {
		int ret=path.lastIndexOf(".");
        if(ret==-1) return -1;
		
		String end = path.substring(ret + 1, path.length()).toLowerCase();

		if ((end.equals("aac") || end.equals("mp3") || end.equals("mid")|| end.equals("xmf") || end.equals("ogg") || end.equals("ac3") || end.equals("ape")|| end.equals("flac")|| end.equals("wma")|| end.equals("pac") || end.equals("ogg") || end.equals("wav"))) {
			ret = 1;
		}else {
			ret =-1;
		}
		
		return ret;
		
   }
   public static void checkMkdir(String path){
	      
		  File file=new File(path+"/music_service"); 
		  if(!file.exists()) file.mkdir();
		  file=new File(path+"/music_service/app"); 
		  if(!file.exists()) file.mkdir();
		  file=new File(path+"/music_service/xml"); 
		  if(!file.exists()) file.mkdir(); 
		  file=new File(path+"/music_service/device"); 
		  if(!file.exists()) file.mkdir();   			  
		  file=new File(path+"/music_usb/"); 
		  if(!file.exists()) file.mkdir(); 
		  Log.i(TAG,"SDCard path: "+path+" SDCardAvailable==>"+getSDCardAvailable(path)/1024/1024+"M");

   }

	   
  public static void showmsg(final String msg){
	  mhandler.post(new Runnable(){  
	         public void run(){  
	            
	        	 Toast toast=Toast.makeText(context.getApplicationContext(), " "+msg, Toast.LENGTH_LONG);

	             toast.show();
	         }  
	     }); 
	 }	
	public static  boolean getMusicInfo(MusicFile mfile) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            
            retriever.setDataSource(mfile.getPath());
          //  String ret = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
          //  int dur=Integer.parseInt(ret);
            String artist=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album =retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);  
            String title =retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);  
        
            //String log="------------------->dur "+" artist "+artist+" album "+album+" title "+title;
            //log(log);
          //  log("=====>"+getEncoding(artist));
            String encode=getEncoding(title);
            if(encode!=null && encode.equals("ISO-8859-1")){
		
				   if(artist!=null)artist=new String(artist.getBytes("ISO-8859-1"), "GBK");
				   if(album!=null)album=new String(album.getBytes("ISO-8859-1"), "GBK");
				   if(title!=null)title=new String(title.getBytes("ISO-8859-1"), "GBK");
				   //Log.e("",">>>>>>>>>>>>>>");
            }else{
            	//Log.e("","<<<<<<<<<<");
            }
            mfile.setAlbum(album);
            mfile.setArtist(artist);
            
            

        } catch (UnsupportedEncodingException e) {
				
		    e.printStackTrace();
			 
        } catch(IllegalArgumentException ex) {
            return false;
        } catch (RuntimeException ex) {
        	return false;
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
             
            }
        }
        return true;
    }
   

   public  static String getEncoding(String str) {
	    if(str==null || str.equals("")) return "";
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return  encode;
            }
        } catch (Exception exception) {
        
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;	     
            }
        } catch (Exception exception) {
        	
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return  encode;
            }
        } catch (Exception exception2) {
        
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;

            }
        } catch (Exception exception3) {
        
        }	        
        return "";
    }
   
   public static  void getMusicMsg(LinkedList<MusicFile> list) {
	          
	           if(list==null) return ;
	            ContentResolver resolver = context.getContentResolver(); 
	            Cursor c=resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, null, null, null);   
	            c.moveToFirst();  
	            MusicFile mfile=null;
	            
	            ArrayList<Map<String, String>> alist=new  ArrayList<Map<String, String>>();
	            while (c.moveToNext()) { 
	            	            	
		            //  String name = c.getString(c.getColumnIndexOrThrow(Audio.Media.TITLE)); 			           
		              String album = c.getString(c.getColumnIndexOrThrow(Audio.Media.ALBUM));  			           
		              String artist = c.getString(c.getColumnIndexOrThrow(Audio.Media.ARTIST));  			          
		              String path = c.getString(c.getColumnIndexOrThrow(Audio.Media.DATA)); 			            
		             // int duration = c.getInt(c.getColumnIndexOrThrow(Audio.Media.DURATION));  
	             
		             // int size  =  c.getInt(c.getColumnIndexOrThrow(Audio.Media.SIZE)); 

		              Map<String, String> map = new HashMap<String,  String>();
		              map.put("path", path);
		              map.put("artist", artist);
		              map.put("album", album);
		              alist.add(map);

	           } 
	           int i=0, j=0;
	           for(i=0; i<list.size(); i++){
	            	    
	            	    mfile=list.get(i);
	            	    if(mfile.getArtist().equals("null") && mfile.getAlbum().equals("null")){
	            	         
	            	    	 for(j=0; j<alist.size(); j++){
	            	    	     if(alist.get(j).get("path").equals(mfile.getPath())){
	            	    	    	    mfile.setArtist(alist.get(j).get("artist"));
	            	    	    	    mfile.setAlbum(alist.get(j).get("album"));
	            	    	    	    j=-1;
	            	    		        break;
	            	    	     }
	            	    	 }
	            	    	 if(j!=-1){
	            	    		 getMusicInfo(mfile);
	            	    	 }
	            	    	 
	            	    	 if(!mfile.getArtist().equals("null") || !mfile.getAlbum().equals("null")){
	            	    		  Utils.sqlitehelper.updateMusicinfo(mfile.getDb(), mfile);
	            	    	 }
	            	    }
	            	  
	            	  
	          }
	       
	  }
	   public static  void getMusicMsg(MusicFile mf) {
	       
	       if(mf==null) return ;
	        ContentResolver resolver = context.getContentResolver(); 
	        Cursor c=resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, null, null, null);   
	        c.moveToFirst();  
	        MusicFile mfile=null;
	        
	        ArrayList<Map<String, String>> alist=new  ArrayList<Map<String, String>>();
	        while (c.moveToNext()) { 
	        	            	
	            //  String name = c.getString(c.getColumnIndexOrThrow(Audio.Media.TITLE)); 			           
	              String album = c.getString(c.getColumnIndexOrThrow(Audio.Media.ALBUM));  			           
	              String artist = c.getString(c.getColumnIndexOrThrow(Audio.Media.ARTIST));  			          
	              String path = c.getString(c.getColumnIndexOrThrow(Audio.Media.DATA)); 			            
	             // int duration = c.getInt(c.getColumnIndexOrThrow(Audio.Media.DURATION));  
	         
	             // int size  =  c.getInt(c.getColumnIndexOrThrow(Audio.Media.SIZE)); 
	
	              Map<String, String> map = new HashMap<String,  String>();
	              map.put("path", path);
	              map.put("artist", artist);
	              map.put("album", album);
	              alist.add(map);
	
	       } 
	       c.close();
	       int j=0;
	  
	        	    
	        	    mfile=mf;
	        	    if(mfile.getArtist().equals("null") && mfile.getAlbum().equals("null")){
	        	         
	        	    	 for(j=0; j<alist.size(); j++){
	        	    	     if(alist.get(j).get("path").equals(mfile.getPath())){
	        	    	    	    mfile.setArtist(alist.get(j).get("artist"));
	        	    	    	    mfile.setAlbum(alist.get(j).get("album"));
	        	    	    	    j=-1;
	        	    		        break;
	        	    	     }
	        	    	 }
	        	    	 if(j!=-1){
	        	    		 getMusicInfo(mfile);
	        	    	 }
	        	    	 
	        	    	 if(!mfile.getArtist().equals("null") || !mfile.getAlbum().equals("null")){
	        	    		  Utils.sqlitehelper.updateMusicinfo(mfile.getDb(), mfile);
	        	    	 }
	        	    }
	        	  
	        	  
	 
	   
	   }
		public  static long getSDCardAvailable() {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				File sdcardDir = Environment.getExternalStorageDirectory();
				StatFs sf = new StatFs(sdcardDir.getPath());
				long blockSize = sf.getBlockSize();
				long availCount = sf.getAvailableBlocks();
				return availCount * blockSize;
			}
			return -1;
		}
		public  static long getSDCardAvailable(String path) {
			//String state = Environment.getExternalStorageState();
			//if (Environment.MEDIA_MOUNTED.equals(state)) {
				File sdcardDir = new File(path);
				if(!sdcardDir.exists()) return -1;
				StatFs sf = new StatFs(sdcardDir.getPath());
				long blockSize = sf.getBlockSize();
				long availCount = sf.getAvailableBlocks();
				return availCount * blockSize;
			
			//return -1;
		}
		
		public  static long getSDCardSize(String path) {
			//String state = Environment.getExternalStorageState();
			//if (Environment.MEDIA_MOUNTED.equals(state)) {
				File sdcardDir = new File(path);
				if(!sdcardDir.exists()) return -1;
				StatFs sf = new StatFs(sdcardDir.getPath());
				long blockSize = sf.getBlockSize();
				long availCount = sf.getBlockCount();
				return availCount * blockSize;
			
			//return -1;
		}
	    public static MusicFile getMusicObject(String path){
	    	
	 	   if(path==null || path.length()<=1) return null;
	 	   int index=path.lastIndexOf("/");
	 	   MusicFile mf=new MusicFile();
	 	   mf.setId(getId());
	 	   mf.setPath(path);
	 	   if(index!=-1){
	 		   mf.setName(path.substring(index+1));
	 	   }
	
	 	   
	 	   
	        
	 	  // Log.d(TAG, mf.toString());
	 	   return mf;
	    }
	    public static String getPathToFolder(String path){
	    	    String name="";
	    	
		 	    int index=path.lastIndexOf("/");
		 	    name=path.substring(0, (index==-1)?path.length():index);
		 	    index=name.lastIndexOf("/");
		 	    name=name.substring((index==-1)?0:index,  name.length());
	  
	    	    return name;	
	    }
	    public static String getPathToFolderFolder(String path){

    	    String items[]=path.split("/");
    	    if(items==null || items.length<=1) return "";
    	    if(items.length==2){
    	    	return "/"+items[items.length-2];
    	    }else
    	    	return "/"+items[items.length-3]+"/"+items[items.length-2];

        }	    
	    public static String getPathToFileName(String path){
    	    String name="";
    	
	 	    int index=path.lastIndexOf("/");
	 	    name=path.substring((index==-1)?0:(index+1));
    	    return name;	
        }
	    public static String getPathToFolderandFile(String path){
		    String name="";
		
	 	    int index=path.lastIndexOf("/");
	 	    name=path.substring(0, (index==-1)?path.length():index);
	 	    index=name.lastIndexOf("/");
	 	    name=path.substring((index==-1)?0:(index+1),  path.length());
	
		    return name;
	    
	   }
	    public static LinkedList<MusicForm> getMusicFormList(LinkedList<MusicForm> list,String info){
	    	
	    	if(list==null) list=new LinkedList<MusicForm>();
	    	else list.clear();
	    	
		    MusicForm mform=null;
		    String items[]=info.split(":"); 
		    for(int i=0; i<items.length; i++){
		    	
		    	String items_child[]=items[i].split(">");
		    	mform=new MusicForm(items_child[0], items_child[1]);
		    	
		    	if(!items_child[2].equals("null")){
			    	String items_child_cell[]= items_child[2].split("\\*");
			    	for(int j=0; j<items_child_cell.length; j++)
			    		mform.addMusicObj(items_child_cell[j]);
		    	}
		    	list.add(mform);
		    }
	
	    	return list;
	    }
	    
	    public static String toMusicFormString(LinkedList<MusicForm> list, String var){
	    	
	    	   String ret="";
	    	   if(var!=null) ret+=var+":";
		       MusicForm mform=null;
		  
		    	   
	    	   for(int i=0; i<list.size(); i++){
	    		     mform=list.get(i);
	    		     ret+=mform.getId()+">"+mform.getForm()+">";
	    		     if(mform.getMusiclist().size()>0){
	    		        for(int j=0; j<mform.getMusiclist().size(); j++)
	    		    	    ret+=mform.getMusiclist().get(j)+"*";
	    		     }else{
	    		    	ret+="null";
	    		     }
	    		     ret+=":";
	    	  }
	
	    	  return ret;
	    }
	    public static void log(String log){
	    	 Log.i(TAG, log);
	    }
	    
	    public static void log(String tag, String log){
	   	     Log.i(tag, log);
	    }
	    
		/**
	  	 * 检测该包名所对应的应用是否存在
	  	 * @param packageName
	  	 * @return
	  	 */
	  	public static int checkPackage(Context context,String packageName) 
	  	{  
		    if (packageName == null || "".equals(packageName))  
		        return -1;  
		    try 
		    {  
		    	context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
		    	
		        return context.getPackageManager().getPackageInfo(packageName, 0).versionCode;  
		    } 
		    catch (NameNotFoundException e) 
		    {  
		        return -1;  
		    }catch (Exception e) {   
		        return -1;   
		    }   
		}
	  	
		  public static String getTimeClockStr(TimeClock tc){
			    String ret="";
			    
			    ret+=tc.getId()+";"+tc.getName()+";"+tc.getMuisformid()+";"+tc.getTime()+";"+tc.getTimeperiod()+";"+tc.getVolume_mode()+";"+tc.getDuration();
			    
			    
			    return ret;
		  }
	      public static TimeClock getTimeClock(String str){
	    	  
	    	  
	    	     TimeClock tc=new TimeClock();
	    	     
	    	     
	    	     String items[]=str.split(";");
	    	     
	    	     tc.setId(items[0]);
	    	     tc.setName(items[1]);
	    	     tc.setMuisformid(items[2]);
	    	     tc.setTime(items[3]);
	    	     tc.setTimeperiod(items[4]);
	    	     tc.setVolume_mode(items[5]);
	    	     if(items.length==7)tc.setDuration(items[6]);
	    	     
	    	     
	    	     return tc;
	      }
	      
		  public  static void getWindowDisplay(Context context){
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
		  
		public static List fetch_installed_apps(Context context) {
			
			String label;
			Map<String, Object> map;
			PackageManager pm = context.getPackageManager();			
	        List<PackageInfo> packs = pm.getInstalledPackages(0);    
            List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
	        for(PackageInfo pi:packs){    
	              map = new HashMap<String, Object>();    
	            
		          if((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)==0&& (pi.applicationInfo.flags&ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)==0){    
	               	            
		        	            if(pi.applicationInfo.packageName.equals("com.myun.core") || pi.applicationInfo.packageName.equals("com.tencent.qqpinyin")) continue;
					            map.put("version", pi.versionCode);   
					            map.put("appName", pi.applicationInfo.loadLabel(pm));//应用程序名称    
					            map.put("packageName", pi.applicationInfo.packageName);//应用程序包名  
					            
					            String str=MD5(pi.applicationInfo.packageName);
					            
					            if(!new File(music_app_path+"/"+str).exists()){
					            	
					            	BitmapDrawable bd = (BitmapDrawable)pi.applicationInfo.loadIcon(pm);					            	
					            	//log("------------->"+bd);
					            	writefile(music_app_path+"/"+str, bd.getBitmap());			            	
					            	
					            }
					            
					           // System.out.println(pi.applicationInfo.packageName+"::"+pi.applicationInfo.className);  
					            Log.i(TAG, pi.versionCode+"  "+pi.applicationInfo.packageName+"::"+pi.applicationInfo.loadLabel(pm));			
				                list.add(map);
		          }    
	              

			}

			return list;
		}
		
		public static String MD5(String str)  
		{  
		       MessageDigest md5 = null;  
		       try  
		       {  
		           md5 = MessageDigest.getInstance("MD5"); 
		       }catch(Exception e)  
		       {  
		           e.printStackTrace();  
		           return "";  
		       }  
		         
		       char[] charArray = str.toCharArray();  
		       byte[] byteArray = new byte[charArray.length];  
		         
		       for(int i = 0; i < charArray.length; i++)  
		       {  
		           byteArray[i] = (byte)charArray[i];  
		       }  
		       byte[] md5Bytes = md5.digest(byteArray);  
		         
		       StringBuffer hexValue = new StringBuffer();  
		       for( int i = 0; i < md5Bytes.length; i++)  
		       {  
		           int val = ((int)md5Bytes[i])&0xff;  
		           if(val < 16)  
		           {  
		               hexValue.append("0");  
		           }  
		           hexValue.append(Integer.toHexString(val));  
		       }  
		       return hexValue.toString().substring(8, 24);  
		}
		public static void setShutdown(Context context, String tc){
			
		
			
			
			
		    AlarmManager manager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
			Intent intent = new Intent("com.myun.shutdown.AlarmReceiver");
			intent.putExtra("id", Utils.CLOCKID);
			PendingIntent sender = PendingIntent.getBroadcast(context,  Utils.CLOCKID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			if(tc==null || tc.equals("")){
				manager.cancel(sender);
				Utils.showmsg("已取消设置关机! ");
				return ;
			}
			
			String t[]=tc.split(":");
		  
		    long systemTime = System.currentTimeMillis();
		
		    Calendar calendar = Calendar.getInstance();
		 	calendar.setTimeInMillis(System.currentTimeMillis());
		 	calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 这里时区需要设置一下，不然会有8个小时的时间差
		 	calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
		 	calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
		 	calendar.set(Calendar.SECOND, 0);
		 	calendar.set(Calendar.MILLISECOND, 0);
		
		 	// 选择的每天定时时间
		 	long selectTime = calendar.getTimeInMillis();	
		
         
		 	if(systemTime > selectTime) {
		 		Utils.showmsg("设置的时间小于当前时间");
		 		calendar.add(Calendar.DAY_OF_MONTH, 1);
		 		selectTime = calendar.getTimeInMillis();
		 		return ;
		 	}

		    // 进行闹铃注册		  
		    manager.set(AlarmManager.RTC_WAKEUP,selectTime, sender);
		    Utils.showmsg(t[0]+":"+t[1]+"设置关机闹铃成功! ");
	}
		
    public static String buildwifijson(List<ScanResult> listResult, List<WifiConfiguration> listconfig, String name){
		  
		  log("connect ssid "+name);
		  
		  if(name!=null && WifiAdmin.conn_ssid!=null && !name.equals(WifiAdmin.conn_ssid)){
		    	WifiAdmin.conn_ssid=null;
		  }
		  JSONStringer jsonText = new JSONStringer(); 
		  try {  
			     
			    // 首先是{，对象开始。object和endObject必须配对使用  
			    jsonText.object();  
			      
			    jsonText.key("wifilist");  
			
			    jsonText.array(); 
			    String var=null, ssid=null;
			    for(int i=0; i<listResult.size(); i++){
			    	
			    	ssid=listResult.get(i).SSID;
			    	if(ssid.equals("")) continue;
				    jsonText.object();  
				    jsonText.key("ssid").value(ssid);  
				    jsonText.key("bssid").value(listResult.get(i).BSSID); 
				    
				    var=listResult.get(i).capabilities;
				   // log(ssid+" === var......"+var);
				    if(var.contains("WPA2-PSK-CCMP") || var.contains("WPA-PSK-CCMP")|| var.contains("WPA2-PSK-CCMP+TKIP") || var.contains("WPA-PSK-CCMP+TKIP")|| var.contains("WEP")){
		        		 
		        		 int j=0;
		        		 for(j=0; j<listconfig.size(); j++){
		        			 
		        			     if(ssid.equals(listconfig.get(j).SSID.replace("\"", ""))){
		        			    	 j=-1;
		        			    	// log("capabilities ...."+ssid);
		        			    	 break;
		        			     }
		        			 
		        		 }
				  
				         jsonText.key("capabilities").value((j==-1)?"trueok":"true"); 
				       
		        		 
		        	}else{
		        		jsonText.key("capabilities").value("false"); 
		        	}
				    
				    //jsonText.key("frequency").value(listResult.get(i).frequency);  
				    jsonText.key("level").value(listResult.get(i).level);
				    //log("==>"+name+"==="+listResult.get(i).SSID);
				    if(name!=null && name.equals(ssid)){
				        jsonText.key("status").value("true"); 
				        if(WifiAdmin.conn_ssid!=null && ssid.equals(WifiAdmin.conn_ssid))
				        	WifiAdmin.conn_ssid=null;
				    }else if(WifiAdmin.conn_ssid!=null){
				    	
				    	if(WifiAdmin.conn_ssid.equals(ssid)){
				    	    jsonText.key("status").value(WifiAdmin.status);
				    	   // WifiAdmin.conn_ssid=null;
				    	}else{
				    		jsonText.key("status").value("false"); 
				    	}
				    }else{
				    	jsonText.key("status").value("false"); 
				    }
				 //   jsonText.key("ssid").value(listResult.get(i).);   
				    jsonText.endObject();   
			         
			    }
			    
			   // jsonText.value("12345678").value("87654321");  
			    jsonText.endArray();  
			      

			    jsonText.endObject();  
			} catch (JSONException ex) {  
			    throw new RuntimeException(ex);  
			} 
		    
		    return jsonText.toString();
	  }
	    
	   synchronized public static void writefile(String path, Bitmap bp){

			//if(!issd) return ;
		        File f = new File(path);
		        if(f.exists()) return ;
		        FileOutputStream fOut = null;
		        try {
		                fOut = new FileOutputStream(f);
		        } catch (FileNotFoundException e) {
		                e.printStackTrace();
		        }
		        bp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		        try {
		                fOut.flush();
		        } catch (IOException e) {
		                e.printStackTrace();
		        }
		        try {
		                fOut.close();
		        } catch (IOException e) {
		                e.printStackTrace();
		        }
		}
	   
	   public static boolean copyFile(String oldPath, String newPath) {
		   
		   log("oldPath "+oldPath+" newPath"+newPath);
	       try {   
	          // int bytesum = 0;   
	           int byteread = 0;   
	           File oldfile = new File(oldPath);   
	           if (oldfile.exists()) { //文件存在时   
	               InputStream inStream = new FileInputStream(oldPath); //读入原文件   
	               FileOutputStream fs = new FileOutputStream(newPath);   
	               byte[] buffer = new byte[1024*64];   
 
	               while ( (byteread = inStream.read(buffer)) != -1) {   
	                  // bytesum += byteread; //字节数 文件大小   
	                   //System.out.println(bytesum);   
	                   fs.write(buffer, 0, byteread);   
	               }   
	               inStream.close(); 
	               fs.close();
	           }   
	       }   
	       catch (Exception e) {   
	           System.out.println("复制文件"+oldPath+"操作出错");   
	           e.printStackTrace();   
	           return false;
	       }   
	       return true;
	   } 
	   
	   @SuppressWarnings("resource")
	public static String rwAudioIO(String value) {
		   
		       String ret="";
//		       File afile=new File("/sys/devices/platform/gpio-pwm.0/ad_sw");
//		       if(!afile.exists()){
//		    	   log("error /sys/devices/platform/gpio-pwm.0/ad_sw  not exsit");
//		    	   return ret;
//		       }
//		       FileOutputStream fs=null;
//		       try {
//		    	   
//		    	   
//				       if(value!=null){				    	 
//							fs = new FileOutputStream("/sys/devices/platform/gpio-pwm.0/ad_sw", false);
//						    fs.write(value.getBytes(), 0, value.getBytes().length);	
//						    fs.close();
//				       }else{
//				    	    
//			                FileInputStream finStream = new FileInputStream("/sys/devices/platform/gpio-pwm.0/ad_sw"); //读入原文件   
//			                byte[] buffer = new byte[64];
//			                int byteread = finStream.read(buffer);
//			                ret=new String(buffer, 0 ,1);
//			                
//			                log(byteread+"  /sys/devices/platform/gpio-pwm.0/ad_sw "+ret);
//				       }
//
//				       
//				} catch (FileNotFoundException e) {
//					
//					  e.printStackTrace();
//				}  catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}    
		    	   
		       return ret;
		       
		       
		   
	   }
	   
	   public static void setLedSwitch(boolean var){
		   
		   
//	       String ret=var?"1":"0";
//	       File afile=new File("/sys/devices/platform/gpio-pwm.0/breath_enable");
//	       if(!afile.exists()){
//	    	   log("error /sys/devices/platform/gpio-pwm.0/breath_enable  not exsit");
//	       }
//	       FileOutputStream fs=null;
//	       try { 
//				fs = new FileOutputStream("/sys/devices/platform/gpio-pwm.0/breath_enable", false);
//			    fs.write(ret.getBytes(), 0, ret.getBytes().length);	
//			    fs.close();
//
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}  catch (IOException e) {
//				e.printStackTrace();
//			}    
	    	   
	   }
	   public static boolean isHDMIPlugged() {
	        String status = readSysfs("/sys/devices/platform/gpio-pwm.0/hdmi_sta");
	        //log(">>>>>>"+status);
	        if ("1".equals(status))
	            return true;
	        else
	            return false;
	    }
	    
	    public static String readSysfs(String path) {
	        if (!new File(path).exists()) {
	            log("File not found: " + path);
	            return null; 
	        }
	 
	        String str = null;
	        StringBuilder value = new StringBuilder();
	        

	         //   log("readSysfs path:" + path);
	        
	        try {
	            FileReader fr = new FileReader(path);
	            BufferedReader br = new BufferedReader(fr);
	            try {
	                while ((str = br.readLine()) != null) {
	                    if(str != null)
	                        value.append(str);
	                };
					fr.close();
					br.close();
	                if(value != null)
	                    return value.toString();
	                else 
	                    return null;
	            } catch (IOException e) {
	                e.printStackTrace();
	                return null;
	            }
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	   public static void sendSpeakerNotify(Context context ,String startclass, boolean flag){
		   
			Intent i = new Intent(PeerTools.SPEAKER_NOTIFY_STATUS);
			if(flag) i.setData(Uri.parse("file://alarm"));
			i.putExtra("class", startclass);
			context.sendBroadcast(i);
		   
	   }
	   
	   public static long getTimeDiffer(String pos){
		   
		    if(pos==null || !pos.contains(":")) return -1;
		    
		    String t[]=pos.split(":");
		    long systemTime = System.currentTimeMillis();
			
		    Calendar calendar = Calendar.getInstance();
		 	calendar.setTimeInMillis(System.currentTimeMillis());
		 	calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); 
		 	calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
		 	calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
		 	calendar.set(Calendar.SECOND, 0);
		 	calendar.set(Calendar.MILLISECOND, 0);
		 	//new Date(time)
		 	// 选择的每天定时时间
		 	long selectTime = calendar.getTimeInMillis();
		 	
		 	long dif=Math.abs(systemTime-selectTime);
		 	log("different ....."+dif/(1000*60));
		 	return dif;
		 	
	   }
	   public static int getAndroidSDKVersion() {
	        int version = 0;
	        try {
	            version = Integer.valueOf(android.os.Build.VERSION.SDK);
	            Log.i(TAG,"getAndroidSDKVersion "+version);
	        } catch (NumberFormatException e) {
	         //   CSLog.e(Tool.class, e.toString());
	        }
	        return version;
	    }
}
