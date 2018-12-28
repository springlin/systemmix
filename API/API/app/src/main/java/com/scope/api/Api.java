
package com.scope.api;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.WallpaperManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceControl;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Spring on 2018/3/1.
 */

public class Api implements IApi{

    public final String tag="Api";
    public Context context;
    public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    public static final String EXTRA_KEY_CONFIRM ="android.intent.action.EXTRA_KEY_CONFIRM";
   // public SharedPreferences sp=null;
    public Api(Context context)
    {
        this.context=context;

      //  sp=context.getSharedPreferences ("scope", Context.MODE_MULTI_PROCESS);

        Utils.grantPermission(context, "com.scope.api", 43);//
        Utils.grantPermission(context, "com.scope.api", 59);
        Utils.grantPermission(context, "com.scope.api", 60);
    }

    @Override
    public boolean shutdownDevice() {


        Intent intent = new Intent(ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        return true;
    }

    @Override
    public boolean sleepDevice() {

          PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
          Utils.getClassAll(pm.getClass().getName());
          try{

              Method method=Utils.getClass(pm.getClass().getName(), "goToSleep");
              method.setAccessible(true);
              method.invoke(pm, new Object[]{SystemClock.uptimeMillis()});

          }catch (InvocationTargetException e) {
                e.printStackTrace();
          }catch (IllegalAccessException e) {
                e.printStackTrace();
          }



        return true;
    }

    @Override
    public boolean requestKeyControl(int keycode) {



        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.key");
        if(str==null) str="";
        if(str.contains(keycode+"")) return true;
        str+=keycode+"/";


        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.key", str);
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.key");
        Utils.log("keylist---"+ret);
        return true;
    }

    @Override
    public boolean releaseKeyControl(int keycode) {

        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.key");
        if(str==null) str="";
        str=str.replace(keycode+"/", "");
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.key", keycode==-1?"":str);
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.key");
        Utils.log("keylist---"+ret);
        return true;
    }

    @Override
    public boolean hideHomeSoftKey(boolean hide) {

        Uri uri = android.provider.Settings.System.getUriFor("scope.settings.update");
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.home", hide?"no":"yes");
        context.getContentResolver().notifyChange(uri, null);
        return true;
    }

    @Override
    public boolean hideRecentSoftKey(boolean hide) {

        Uri uri = android.provider.Settings.System.getUriFor("scope.settings.update");
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.recent", hide?"no":"yes");
        context.getContentResolver().notifyChange(uri, null);


        return true;
    }

    @Override
    public boolean hideBackSoftKey(boolean hide){
        Uri uri = android.provider.Settings.System.getUriFor("scope.settings.update");
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.back", hide?"no":"yes");
        context.getContentResolver().notifyChange(uri, null);
        return true;
    }

    @Override
    public boolean disableStatusBarPanel(boolean disable) {


        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName ("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, (disable?0x00010000:0x00000000));
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean showStatusBarPanel(boolean show) {



        try {
            Object service = context.getSystemService("statusbar");
            Class<?> statusbarManager = Class
                    .forName("android.app.StatusBarManager");
            Method expand = null;
            if (service != null) {
                expand = statusbarManager.getMethod(show?"expandNotificationsPanel":"collapsePanels");
                expand.setAccessible(true);
                expand.invoke(service);
            }

        } catch (Exception e) {
        }
        return true;
    }

    @Override
    public boolean wakeupDevice() {

        sendKeyEvent(KeyEvent.KEYCODE_WAKEUP);

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard();

        return true;
    }

    @Override
    public boolean rebootDevice() {

        Intent intent2 = new Intent(Intent.ACTION_REBOOT);
        intent2.putExtra("nowait", 1);
        intent2.putExtra("interval", 1);
        intent2.putExtra("window", 0);
        context.sendBroadcast(intent2);
        return false;
    }

    @Override
    public boolean setScreenOffTime(int time) {


        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,time);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public boolean enableMassStorage(boolean enable) {




        Utils.setSystemProperties("persist.sys.scope.sdcard", enable?"yes":"no");
        if(!enable){//允许SD卡


            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            String sdpath=android.os.Environment.getExternalStorageDirectory().toString();


            try {
                Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
                IBinder binder = (IBinder) method.invoke(null, "mount");
                IMountService iMountService = IMountService.Stub.asInterface(binder);


                StorageVolume[] storageVolumes;

                Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumeList");
                storageVolumes = (StorageVolume[]) getVolumeList.invoke(storageManager);
                Method getVolumeState = StorageManager.class.getDeclaredMethod("getVolumeState", String.class);
                for (StorageVolume storageVolume : storageVolumes) {

                    Method getPath = StorageVolume.class.getMethod("getPath");
                    String path = (String) getPath.invoke(storageVolume);


                    String state = (String) getVolumeState.invoke(storageManager, path);

                    if (Environment.MEDIA_MOUNTED.equals(state)) {

                        try {
                            if(!path.equals(sdpath)) {
                                Utils.log("extern storage path--->" + path);
                                iMountService.unmountVolume(path, true, true);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        return true;
    }

    @Override
    public boolean formatStorage(String path) {


            if(path==null || path.equals("") ) return false;

            StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
          //  Utils.getClassAll(mStorageManager.getClass().getName());

            try {

                Method getVolumeList = mStorageManager.getClass().getMethod("getVolumes");

                final List<VolumeInfo> vols=(List<VolumeInfo>)getVolumeList.invoke(mStorageManager);

                for (VolumeInfo vol : vols) {
                            Utils.log("get Id:"+vol.getDiskId()+" "+vol.getPath().getPath()+ "  "+vol.getDescription());
                            if(path.equals(vol.getPath().getPath())){

                                  Method partitionPublic = StorageManager.class.getDeclaredMethod("partitionPublic", String.class);
                                  if(partitionPublic!=null){

                                      partitionPublic.invoke(mStorageManager, new Object[]{vol.getDiskId()});
                                  }
                                  break;
                            }

                }

            }  catch (Exception e) {
                e.printStackTrace();
            }


            return true;
    }

    @Override
    public List<String> getExternSDCardList() {

        ArrayList<String > list=new ArrayList<String >();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String sdpath=android.os.Environment.getExternalStorageDirectory().toString();


        try {

            StorageVolume[] storageVolumes;

            Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumeList");
            storageVolumes = (StorageVolume[]) getVolumeList.invoke(storageManager);
            Method getVolumeState = StorageManager.class.getDeclaredMethod("getVolumeState", String.class);
            for (StorageVolume storageVolume : storageVolumes) {

                Method getPath = StorageVolume.class.getMethod("getPath");
                String path = (String) getPath.invoke(storageVolume);
                String state = (String) getVolumeState.invoke(storageManager, path);

                if (Environment.MEDIA_MOUNTED.equals(state)) {

                    if(!path.equals(sdpath)) {
                          list.add(path);
                          Utils.log("extern path =="+path);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }





        return list;

    }

    @Override
    public boolean disableOTG(boolean disable) {
        return false;
    }

    @Override
    public boolean checkOTGState() {
        return false;
    }

    @Override
    public boolean setUSBConnectionType(String type) {

        if(type==null || type.equals("")) return false;
        type=type.toLowerCase();

        UsbManager usb_service = (UsbManager)context.getSystemService(Context.USB_SERVICE);
      //  Utils.getClassAll(usb_service.getClass().getName());
        Method method=Utils.getClass(usb_service.getClass().getName(), "setCurrentFunction");
        try {
        if(method==null) return false;


           method.invoke(usb_service, new Object[]{type, true});


        }catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getUSBConnectionType() {

        String result=Utils.getSystemProperties("sys.usb.state");
        Utils.log("getUSBConnectionType--->"+result);
        return result;


    }

    @Override
    public boolean setScreenOn(boolean on) {


       if(!on)
           sleepDevice();
        else
           wakeupDevice();

        return false;
    }

    @Override
    public boolean setShutDownTime(boolean enable, int hour, int minute, int dayOfWeek) {
        return false;
    }

    @Override
    public boolean setBootTime(boolean enable, int hour, int minute, int dayOfWeek) {
        return false;
    }

    @Override
    public Bitmap captureScreen() {


        Display mDisplay;
        DisplayMetrics mDisplayMetrics;
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
        float[] dims = {mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels};
        return SurfaceControl.screenshot((int) dims[0], (int) dims[1]);

    }

    @Override
    public int sendKeyEvent(final int keycode) {

        Utils.log("send keyevent "+keycode);
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keycode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
        return 0;
    }

    @Override
    public boolean addNetworkRuleWhitelist(List<String> addrList) {
        String str=null;

        if(addrList.size()>0){

            List<String> list=getNetworkRuleWhitelist();
            if(list.size()==0){
                Utils.execCommand("iptables -F OUTPUT",null);
                Utils.execCommand("iptables -A OUTPUT -j REJECT",null);
            }else{
                for(int i=0; i<list.size(); i++){
                      if(addrList.contains(list.get(i))){
                          addrList.remove(list.get(i));
                      }
                }
            }

        }

        for(int i=0; i<addrList.size(); i++){

            String cmd="iptables -I OUTPUT 1 -d "+addrList.get(i)+" -j ACCEPT";
            String ret=Utils.execCommand(cmd,null);
            Utils.log("\""+cmd+"\"  ret="+ret);

        }
        Utils.execCommand("iptables-save",null);
        return true;
    }

    @Override
    public List<String> getNetworkRuleWhitelist() {

        List<String> list=new ArrayList<>();
        String str=Utils.execCommand("iptables -L OUTPUT", null);
      //  Utils.log("str===="+str);
        if(str!=null && !str.equals("")){

              boolean fg=false;
              String item[]=str.split("\n");
              for(int i=0; i<item.length; i++){

                     if(item[i].equals("Chain OUTPUT (policy ACCEPT)")){
                            fg=true;
                            continue;
                     }

                     if(fg){

                         String ip=Utils.getStringIP(item[i]);
                         if(ip!=null){
                             Utils.log("====>"+ip);
                             list.add(ip);
                         }
                     }
                     if(fg && item[i].equals("")){
                            fg=false;
                            break;
                     }


              }



        }

        return list;
    }

    @Override
    public boolean addNetworkRuleWhitelistURL(String urlkey) {




            List<String> list=getNetworkRuleWhiteURLlist();
            if(list.size()==0){
                Utils.execCommand("iptables -F OUTPUT",null);
                Utils.execCommand("iptables -A OUTPUT -p tcp -m string --string Host: --algo bm -j MARK --set-mark 1",null);
                Utils.execCommand("iptables -A OUTPUT -p tcp -m mark --mark 1 -j REJECT", null);

            }else{

                    if(list.contains(urlkey)){
                        return false;
                    }

            }







            String cmd=" iptables -I OUTPUT 2 -p tcp -m mark --mark 1 -m string --string "+urlkey+" --algo bm -j ACCEPT";
            String ret=Utils.execCommand(cmd,null);
            Utils.log("\""+cmd+"\"  ret="+ret);


            Utils.execCommand("iptables-save",null);


            return false;
    }

    @Override
    public List<String> getNetworkRuleWhiteURLlist() {

        List<String> list=new ArrayList<>();
        String str=Utils.execCommand("iptables -L OUTPUT", null);
       //   Utils.log("str===="+str);
        if(str!=null && !str.equals("")){


            String item[]=str.split("\n");
            for(int i=0; i<item.length; i++){

                if(item[i].indexOf("ACCEPT")==0){

                    String key= Utils.getStringKey(item[i]);
                    Utils.log("key====="+key);
                    list.add(key);
                }

            }



        }

        return list;
    }

    @Override
    public boolean clearNetworkRule() {


        String str=Utils.execCommand("iptables -F OUTPUT", null);
        return true;
    }

    @Override
    public boolean addNetworkRuleWhitelistURL(List<String> urllist) {


        List<String> list=getNetworkRuleWhiteURLlist();
        if(list.size()==0){
            Utils.execCommand("iptables -F OUTPUT",null);
            Utils.execCommand("iptables -A OUTPUT -p tcp -m string --string Host: --algo bm -j MARK --set-mark 1",null);
            Utils.execCommand("iptables -A OUTPUT -p tcp -m mark --mark 1 -j REJECT", null);

        }else{

            for(String urlkey : urllist) {
                if (list.contains(urlkey)) {
                   urllist.remove(urlkey);
                }
            }

        }



        for(String urlkey : urllist) {

            String cmd=" iptables -I OUTPUT 2 -p tcp -m mark --mark 1 -m string --string "+urlkey+" --algo bm -j ACCEPT";
            String ret=Utils.execCommand(cmd,null);
            Utils.log("\""+cmd+"\"  ret="+ret);
        }






        Utils.execCommand("iptables-save",null);

        return false;
    }

    @Override
    public boolean addNetworkRuleBlacklist(List<String> addrList) {

        String str=null;

        if(addrList.size()>0){

            List<String> list=getNetworkRuleWhitelist();
            if(list.size()==0){
                Utils.execCommand("iptables -F OUTPUT",null);

            }else{
                for(int i=0; i<list.size(); i++){
                    if(addrList.contains(list.get(i))){
                        addrList.remove(list.get(i));
                    }
                }
            }

        }

        for(int i=0; i<addrList.size(); i++){

            String cmd="iptables -A OUTPUT -d "+addrList.get(i)+" -j REJECT";
            String ret=Utils.execCommand(cmd,null);
            Utils.log("\""+cmd+"\"  ret="+ret);

        }
        Utils.execCommand("iptables-save",null);


        return false;
    }

    @Override
    public boolean addNetworkRuleBlacklistURL(String urlkey) {


        List<String> list=getNetworkRuleWhiteURLlist();
        if(list.size()==0){
            Utils.execCommand("iptables -F OUTPUT",null);
            Utils.execCommand("iptables -A OUTPUT -p tcp -m string --string Host: --algo bm -j MARK --set-mark 1",null);
            Utils.execCommand("iptables -A OUTPUT -p tcp -m mark --mark 1 -j ACCEPT", null);

        }else{

            if(list.contains(urlkey)){
                return false;
            }

        }







        String cmd=" iptables -I OUTPUT 2 -p tcp -m mark --mark 1 -m string --string "+urlkey+" --algo bm -j REJECT";
        String ret=Utils.execCommand(cmd,null);
        Utils.log("\""+cmd+"\"  ret="+ret);


        Utils.execCommand("iptables-save",null);
        return false;
    }

    @Override
    public List<String> getRunningTasks() {

        List<String> list= new ArrayList<String>();
        UsageStatsManager usageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        long ts = System.currentTimeMillis();

        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, ts - 20000, ts);
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            Utils.log("queryUsageStats faild ....");
            return list;
        }

        UsageStats recentStats = null;

        for (UsageStats usageStats : queryUsageStats) {
            if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                recentStats = usageStats;
            }
        }
       // ActivityManager.RunningTaskInfo rinfo=new ActivityManager.RunningTaskInfo();
      //  rinfo.topActivity= new ComponentName(recentStats.getPackageName(), ""); ;
        list.add(recentStats.getPackageName());
        Utils.log("Top Activity=>"+recentStats.getPackageName());
        return list;

    }

    @Override
    public boolean killApplicationProcess(String packageName) {

        ActivityManager  mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Class<?> mClass = Class.forName("android.app.ActivityManager");
            Method[] methods=mClass.getDeclaredMethods();

            for(int i=0;i<methods.length;i++){
                Method method=methods[i];
                System.out.println("method name : "+method.getName());
                if(method.getName().equals("forceStopPackage")){
                    method.invoke(mAm, new Object[]{packageName});
                    return true;
                  //  break;
                }
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean enableInstallation() {
        return false;
    }

    @Override
    public boolean enableInstalApp(boolean enable) {




        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.installapk", enable?"true":"false");
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk");
        Utils.log("ret---"+ret);


        return true;
    }

    @Override
    public boolean enableUninstallApp(boolean enable) {


        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.uninstallapk", enable?"true":"false");
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk");
        Utils.log("ret---"+ret);
        return true;
    }



    @Override
    public boolean setApkState(String packageName, boolean enable) {

        PackageManager packageManager = context.getPackageManager();

        if (!enable) {

            packageManager.setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,0);
        } else {

            packageManager.setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        }
        return true;
    }

    @Override
    public boolean installPackage(String packagePath) {
        Utils.installSilentWithReflection(context, packagePath);
        return true;
    }

    @Override
    public boolean uninstallPackage(String packageName, boolean keepData) {

        Utils.uninstallSilentWithReflection(context, packageName);
        return true;
    }

    @Override
    public boolean addInstallPackageWhiteList(List<String> packageNames) {

        if(packageNames==null || packageNames.size()==0) return false;

        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.white");
        if(str==null) str="";
        for(int i=0; i<packageNames.size(); i++){

             if(str.contains(packageNames.get(i))) continue;
             str+=packageNames.get(i)+"/";

        }
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.installapk.white", str);
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.white");
        Utils.log("ret---"+ret);
        return true;
    }

    @Override
    public List<String> getInstallPackageWhiteList() {

        ArrayList<String> list=new ArrayList<String>();
        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.white");
        if(str==null || str.equals("")) return list;
        Utils.log(str);

        String strs[]=str.split("/");
        for(int i=0; i<strs.length; i++){

            if(!strs[i].equals("")){
                list.add(strs[i]);
            }
        }

        return list;
    }

    @Override
    public boolean removeInstallPackageWhiteList(List<String> packageNames) {

        if(packageNames==null ) return false;
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.white");
        if(ret==null || ret.equals("")) return false;

        for(int i=0; i<packageNames.size(); i++){

            String str=packageNames.get(i)+"/";
            ret=ret.replace(str, "");

        }
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.installapk.white", packageNames.size()==0?"":ret);
        ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.white");
        Utils.log("ret---"+ret);
        return true;
    }

    @Override
    public boolean addInstallPackageBlackList(List<String> packageNames) {
        if(packageNames==null || packageNames.size()==0) return false;

        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.black");
        if(str==null) str="";
        for(int i=0; i<packageNames.size(); i++){

            if(str.contains(packageNames.get(i))) continue;
            str+=packageNames.get(i)+"/";

        }
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.installapk.black", str);
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.black");
        Utils.log("ret---"+ret);
        return true;
    }

    @Override
    public List<String> getInstallPackageBlackList() {
        ArrayList<String> list=new ArrayList<String>();
        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.black");
        if(str==null || str.equals("")) return list;
        Utils.log(str);

        String strs[]=str.split("/");
        for(int i=0; i<strs.length; i++){

            if(!strs[i].equals("")){
                list.add(strs[i]);
            }
        }

        return list;
    }

    @Override
    public boolean removeInstallPackageBlackList(List<String> packageNames) {
        if(packageNames==null ) return false;
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.black");
        if(ret==null || ret.equals("")) return false;

        for(int i=0; i<packageNames.size(); i++){

            String str=packageNames.get(i)+"/";
            ret=ret.replace(str, "");

        }
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.installapk.black", packageNames.size()==0?"":ret);
        ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.installapk.black");
        Utils.log("ret---"+ret);
        return true;
    }

    @Override
    public boolean addUnInstallPackageWhiteList(List<String> packageNames) {

        if(packageNames==null || packageNames.size()==0) return false;

        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.white");
        if(str==null) str="";
        for(int i=0; i<packageNames.size(); i++){

            if(str.contains(packageNames.get(i))) continue;
            str+=packageNames.get(i)+"/";

        }
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.uninstallapk.white", str);
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.white");
        Utils.log("ret---"+ret);
        return true;
    }

    @Override
    public List<String> getUnInstallPackageWhiteList() {

        ArrayList<String> list=new ArrayList<String>();
        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.white");
        if(str==null || str.equals("")) return list;
        Utils.log(str);

        String strs[]=str.split("/");
        for(int i=0; i<strs.length; i++){

            if(!strs[i].equals("")){
                list.add(strs[i]);
            }
        }

        return list;
    }

    @Override
    public boolean removeUnInstallPackageWhiteList(List<String> packageNames) {

        if(packageNames==null ) return false;
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.white");
        if(ret==null || ret.equals("")) return false;

        for(int i=0; i<packageNames.size(); i++){

            String str=packageNames.get(i)+"/";
            ret=ret.replace(str, "");

        }
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.uninstallapk.white", packageNames.size()==0?"":ret);
        ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.white");
        Utils.log("ret---"+ret);
        return true;
    }

    @Override
    public boolean addUnInstallPackageBlackList(List<String> packageNames) {
        if(packageNames==null || packageNames.size()==0) return false;

        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.black");
        if(str==null) str="";
        for(int i=0; i<packageNames.size(); i++){

            if(str.contains(packageNames.get(i))) continue;
            str+=packageNames.get(i)+"/";

        }
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.uninstallapk.black", str);
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.black");
        Utils.log("ret---"+ret);
        return true;
    }

    @Override
    public List<String> getUnInstallPackageBlackList() {
        ArrayList<String> list=new ArrayList<String>();
        String str=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.black");

        if(str==null || str.equals("")) return list;
        Utils.log(str);


        String strs[]=str.split("/");
        for(int i=0; i<strs.length; i++){

            if(!strs[i].equals("")){
                list.add(strs[i]);
            }
        }

        return list;
    }

    @Override
    public boolean removeUnInstallPackageBlackList(List<String> packageNames) {
        if(packageNames==null ) return false;
        String ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.black");
        if(ret==null || ret.equals("")) return false;

        for(int i=0; i<packageNames.size(); i++){

            String str=packageNames.get(i)+"/";
            ret=ret.replace(str, "");

        }
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.uninstallapk.black", packageNames.size()==0?"":ret);
        ret=android.provider.Settings.System.getString(context.getContentResolver(), "scope.settings.uninstallapk.black");
        Utils.log("ret---"+ret);
        return true;
    }


    @Override
    public boolean enableAutoRotate(boolean enable) {

        Uri uri = android.provider.Settings.System.getUriFor("accelerometer_rotation");
        android.provider.Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation", enable?1:0);
        context.getContentResolver().notifyChange(uri, null);
        return true;
    }

    @Override
    public boolean setDesktopWallpaper(String uri) {


        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try {
            wallpaperManager.setBitmap(BitmapFactory.decodeFile(uri));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getDeviceInfo() {
        return DevInfo.getDeviceInfo(context);
    }

    @Override
    public boolean disableMultiUser(boolean disabled) {
        return Utils.setSystemProperties("fw.show_multiuserui", disabled?"false":"true");
    }

    @Override
    public boolean disableCamera(boolean disable) {

           return Utils.setSystemProperties("persist.sys.scope.camera", disable?"false":"true");
//        String result="";
//        try {
//            Class<?> c = Class.forName("android.os.SystemProperties");
//            Utils.getClassAll(c.getName());
//            Method set = c.getMethod("set", String.class,String.class);
//            set.invoke(c, "persist.sys.scope.camera",disable?"false":"true");
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
       // return true;

    }

    @Override
    public boolean turnOnBluetooth(boolean enable) {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null) return false;

        if(enable){
            if(!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
            return true;
        }else{
            if(bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
        return true;
    }

    @Override
    public boolean setMicrophoneMute(boolean disable) {

       Utils.setSystemProperties("persist.sys.scope.record",disable?"false":"true");


        return true;
    }

    @Override
    public boolean openUSBDebug(boolean enable) {


         //   Settings.Secure.putInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
        Uri uri = Settings.System.getUriFor(Settings.Global.ADB_ENABLED);
        Settings.Global.putInt(context.getContentResolver(), Settings.Global.ADB_ENABLED,  enable?1:0);
        context.getContentResolver().notifyChange(uri, null);
        return true;

    }

    @Override
    public boolean hideSettingsDeveloperOptions(boolean hide) {

//        mDevHitCountdown = context.getSharedPreferences(DevelopmentSettings.PREF_FILE,
//                Context.MODE_PRIVATE).getBoolean(DevelopmentSettings.PREF_SHOW,
//                android.os.Build.TYPE.equals("eng")) ? -1 : TAPS_TO_BE_A_DEVELOPER;
        Uri uri = Settings.System.getUriFor("scope.settings.develop");
        Settings.System.putInt(context.getContentResolver(), "scope.settings.develop", hide?0:1);
        context.getContentResolver().notifyChange(uri, null);
        return true;
    }

    @Override
    public boolean checkSettingDeveloperMode() {


        int val=Settings.System.getInt(context.getContentResolver(), "scope.settings.develop", 0);

        return val==1?true:false;
    }

    @Override
    public boolean hideSettingsResetMenu(boolean hide) {
        Uri uri = Settings.System.getUriFor("scope.settings.reset");
        Settings.System.putInt(context.getContentResolver(), "scope.settings.reset", hide?0:1);
        context.getContentResolver().notifyChange(uri, null);
        return true;
    }

    @Override
    public boolean doMasterClear(boolean mEraseSdCard) {

        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        intent.putExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", mEraseSdCard);
        context.sendBroadcast(intent);
        return false;
    }

    @Override
    public boolean disableBluetoothShare(boolean disable) {

        return false;
    }

    @Override
    public boolean addSSID(String ssid, String password, int type, boolean isdefault) {
        return WifiApi.getInstance(context).addConnect(ssid, password, type, isdefault);
    }

    @Override
    public boolean turnOnWifi(boolean enable) {
        return WifiApi.getInstance(context).OpenWifi(enable);
    }

    @Override
    public boolean disableWifiDirect(boolean disable) {
        return false;
    }

    @Override
    public boolean forgetAllWifi() {
        return WifiApi.getInstance(context).forgetAllWiFi();
    }

    @Override
    public boolean setBrightness(int value) {

        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
        context.getContentResolver().notifyChange(uri, null);
        return false;
    }

    @Override
    public boolean setScreenLightMinValue(int min) {

        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, min);
        context.getContentResolver().notifyChange(uri, null);
        return true;
    }

    @Override
    public boolean setScreenLightMaxValue(int max) {
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, max);
        context.getContentResolver().notifyChange(uri, null);
        return true;
    }

    @Override
    public boolean setLocatingMethod(int mode) {


        Uri uri = Settings.System.getUriFor(Settings.Secure.LOCATION_MODE);
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,mode);
        context.getContentResolver().notifyChange(uri, null);

        return false;
    }

    @Override
    public boolean enableMultiWindow(boolean enable) {

        UserManager um= ((UserManager) context.getSystemService(Context.USER_SERVICE));
        Utils.getClassAll(um.getClass().getName());
        return false;
    }

    @Override
    public boolean enableBatterySaving(boolean enable) {


        Uri uri = Settings.System.getUriFor("low_power");
        Settings.Global.putInt(context.getContentResolver(), "low_power", enable ? 1 : 0);
        context.getContentResolver().notifyChange(uri, null);
        return false;
    }

    @Override
    public boolean enableSuperBatterySaving(boolean enable) {
        return enableBatterySaving(enable);
    }

    @Override
    public boolean setCustomLauncher(String packageName, String className) {


         try {
                PackageManager mPm = context.getPackageManager();
                ArrayList<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();

                Method method=Utils.getClass(mPm.getClass().getName(), "getHomeActivities");
                if(method==null) return false;
                ComponentName currentDefaultHome =(ComponentName) method.invoke(mPm, new Object[]{homeActivities});


                IntentFilter mHomeFilter = new IntentFilter(Intent.ACTION_MAIN);
                mHomeFilter.addCategory(Intent.CATEGORY_HOME);
                mHomeFilter.addCategory(Intent.CATEGORY_DEFAULT);
                ComponentName[] mHomeComponentSet = new ComponentName[homeActivities.size()];
                currentDefaultHome=null;
                for (int i = 0; i < homeActivities.size(); i++) {
                    final ResolveInfo candidate = homeActivities.get(i);
                    final ActivityInfo info = candidate.activityInfo;
                    ComponentName activityName = new ComponentName(info.packageName, info.name);
                    mHomeComponentSet[i] = activityName;
                    if(info.packageName.equals(packageName)){
                        currentDefaultHome = activityName;

                    }
                }
                if(currentDefaultHome==null) return false;

                method=Utils.getClass(mPm.getClass().getName(), "replacePreferredActivity");
                if(method==null) return false;


                 method.invoke(mPm, new Object[]{mHomeFilter, IntentFilter.MATCH_CATEGORY_EMPTY, mHomeComponentSet, currentDefaultHome});

                 return true;
            }catch (InvocationTargetException e) {
                e.printStackTrace();
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
    }

    @Override
    public boolean clearCustomLauncher() {

        setCustomLauncher("com.android.launcher3", null);
        return false;
    }

    @Override
    public boolean disableStatusBarNotification(boolean disable) {

        AppOpsManager mAppOps=(AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        Method method=Utils.getClass(mAppOps.getClass().getName(), "setMode");

        PackageManager packageManager = context.getPackageManager();


        List<ApplicationInfo> applicationList = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for(ApplicationInfo appinfo : applicationList){
            try{
                method.invoke(mAppOps, new Object[]{11, appinfo.uid, appinfo.packageName, (disable?AppOpsManager.MODE_IGNORED:AppOpsManager.MODE_ALLOWED)});

            }catch (InvocationTargetException e) {
                e.printStackTrace();
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            Utils.log(appinfo.packageName+" "+appinfo.uid);
        }


        return true;
    }

    @Override
    public boolean disableLockScreenNotification(boolean disable) {

        AppOpsManager mAppOps=(AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        Method method=Utils.getClass(mAppOps.getClass().getName(), "setMode");

        PackageManager packageManager = context.getPackageManager();


        List<ApplicationInfo> applicationList = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for(ApplicationInfo appinfo : applicationList){
            try{
                method.invoke(mAppOps, new Object[]{24, appinfo.uid, appinfo.packageName, (disable?AppOpsManager.MODE_IGNORED:AppOpsManager.MODE_ALLOWED)});

            }catch (InvocationTargetException e) {
                e.printStackTrace();
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            Utils.log(appinfo.packageName+" "+appinfo.uid);
        }
        return true;
    }

    @Override
    public boolean grantPermission(String packageName, String permission )  {

        if(packageName==null || packageName.equals("")) return false;
        try{
                PackageManager packageManager = context.getPackageManager();
                ApplicationInfo appinfo = packageManager.getApplicationInfo(packageName, 0);
                AppOpsManager mAppOps=(AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                Method method=Utils.getClass(mAppOps.getClass().getName(), "setMode");
                method.setAccessible(true);


                int code=Utils.getManifestpermissionCode(permission);
                if(code==-1) {
                    Utils.log(permission+" can't find code");
                    return false;
                }
                method.invoke(mAppOps, new Object[]{code, appinfo.uid, appinfo.packageName, AppOpsManager.MODE_IGNORED});

                int flag=mAppOps.checkOp(AppOpsManager.OPSTR_CALL_PHONE, appinfo.uid, appinfo.packageName);
                Utils.log(appinfo.packageName+" "+appinfo.uid+" "+code+"=="+flag);//+"=="+flag



                return true;
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        return false;
    }

    @Override
    public boolean grantAllPermissions(String packageName) {
        if(packageName==null || packageName.equals("")) return false;
        try{
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appinfo = packageManager.getApplicationInfo(packageName, 0);
            AppOpsManager mAppOps=(AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            Method method=Utils.getClass(mAppOps.getClass().getName(), "setMode");
            method.setAccessible(true);


           for(int i=0; i<Utils.sOpPerms.length; i++){

                if(Utils.sOpPerms[i]!=null) {
                    Utils.log(appinfo.packageName + "---- " + i +"  "+ appinfo.uid);
                    method.invoke(mAppOps, new Object[]{i, appinfo.uid, appinfo.packageName, AppOpsManager.MODE_ALLOWED});
                }

            }



            return true;
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean disablesScreenTouch(boolean disable) {
        Uri uri = android.provider.Settings.System.getUriFor("scope.settings.touch");
        android.provider.Settings.System.putString(context.getContentResolver(), "scope.settings.touch", disable?"no":"yes");
        context.getContentResolver().notifyChange(uri, null);
        return true;
    }

    @Override
    public boolean setDefaultApk(String packageName, String key) {

        return false;
    }

    @Override
    public boolean toggleGps(boolean status) {


        LocationManager locMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isopen = locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Utils.log("gps ====> "+isopen);
        if(isopen!=status) {

            Settings.Secure.setLocationProviderEnabled(context.getContentResolver(),LocationManager.GPS_PROVIDER, status);
            String value = LocationManager.PROVIDERS_CHANGED_ACTION;
            if (status) {
                value = "+" + LocationManager.GPS_PROVIDER;
            } else {
                value = "-" + LocationManager.GPS_PROVIDER;
            }
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, value);

        }

        return false;
    }
}




