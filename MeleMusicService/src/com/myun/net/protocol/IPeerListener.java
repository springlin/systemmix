/**
 * 
 */
package com.myun.net.protocol;

import android.content.Intent;
import android.view.MotionEvent;

/**
 * @author Administrator
 *
 */
public interface IPeerListener {

//      public String getFileDirInfo(String path);
      
      public String getMusicInfo();
      
      public void newClientDevice(String ip, String id);
      
      public int ClientMusicList(String info);	
      
      public String getMusicServieRunStatus();
      
      public int startMusicPlayer(Intent i);
      
      public void refreshDeviceMusicData(String info);
      
      public String getMusicSynchStatus(String info);
      
      public String getMusicPlayerStatus();
      
      public void setMusicPlayerCmd(String cmd, String var);
      
      public void deleteFileCmd(String cmd);
      
      public void startMusicPlayerList(String info);
      
      public String getMusicFavorites(String var, String info);
      
      public String[] getMusicForm(String var, String info);
      
  	  public String onMirrorStart(String ip, String var);
  	  
  	  public void onMirrorStop();
  	  
      public void onKeyCode(final int keycode);
      
      public void onMouseEvent(final MotionEvent event);
      
      public void installApp(String id, String url);
      
      public int  uninstallApp(String app_package);
      
      public String getInstallAppProgress();
      
      public String getAppInfo(String info);
      
      public String[] getTimeClock(String var, String info);
      
      public String setShutdown(String cmd);
      
      public String getWiFiList();
      
      public String setupWiFi(String ssid, String security, String pwd);
      
      public String setWiFiCtrl(int type, String ssid, boolean flag); 

      public String onMusicEQ(String str);
      
      public void setVolTAS5711(int value);
      
      public void copyMusicFile(int channel, String info);
      
      public String registerUser(String info);
      
      public void checkSystemUpdate(String cmd);
      
      public void startAPP(String packagename);
      
}
