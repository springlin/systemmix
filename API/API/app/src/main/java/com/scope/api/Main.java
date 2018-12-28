

package com.scope.api;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class Main extends Activity {

    public Api api=null;
    public Button powerbtn, rebootbtn;
    public EditText sleepedit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
      //  hideBottomUIMenu();

        setContentView(R.layout.main_layout);
        if(api==null) api=new Api(Main.this);

        LinearLayout powerlayout=(LinearLayout)findViewById(R.id.powerlayout);

        for(int i=0; i<powerlayout.getChildCount(); i++){

                if(powerlayout.getChildAt(i) instanceof Button){
                    powerlayout.getChildAt(i).setOnClickListener(clickListener_power);
                }

        }
        LinearLayout keylayout=(LinearLayout)findViewById(R.id.keylayout);
        Intent t=null;
        for(int i=0; i<keylayout.getChildCount(); i++){

            if(keylayout.getChildAt(i) instanceof Button){
                keylayout.getChildAt(i).setOnClickListener(clickListener_key);
            }

        }


        LinearLayout apklayout=(LinearLayout)findViewById(R.id.apklayout);

        for(int i=0; i<apklayout.getChildCount(); i++){

            if(apklayout.getChildAt(i) instanceof Button){
                apklayout.getChildAt(i).setOnClickListener(clickListener_apk);
            }

        }

        LinearLayout wifilayout=(LinearLayout)findViewById(R.id.wifilayout);

        for(int i=0; i<apklayout.getChildCount(); i++){

            if(wifilayout.getChildAt(i) instanceof Button){
                wifilayout.getChildAt(i).setOnClickListener(clickListener_wifi);
            }

        }

        LinearLayout whiteiplayout=(LinearLayout)findViewById(R.id.whiteiplayout);

        for(int i=0; i<apklayout.getChildCount(); i++){

            if(whiteiplayout.getChildAt(i) instanceof Button){
                whiteiplayout.getChildAt(i).setOnClickListener(clickListener_whiteip);
            }

        }

        LinearLayout otherlayout=(LinearLayout)findViewById(R.id.otherlayout);

        for(int i=0; i<otherlayout.getChildCount(); i++){

            if(otherlayout.getChildAt(i) instanceof Button){
                otherlayout.getChildAt(i).setOnClickListener(clickListener_other);
            }

        }
        sleepedit = (EditText)findViewById(R.id.sleepedit);
//        powerbtn=(Button)findViewById(R.id.shutdownid);
//        powerbtn.setOnClickListener(clickListener);
//        rebootbtn=(Button)findViewById(R.id.rebootid);
//        rebootbtn.setOnClickListener(clickListener);
//         api.wakeupDevice();
//         api.releaseKeyControl(-1);
//         api.disablesScreenTouch(true);
//         api.releaseKeyControl(-1);
//         h.sendEmptyMessageDelayed(0, 10000);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
      //  hideBottomUIMenu();


        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                //布局位于状态栏下方
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                //全屏
                View.SYSTEM_UI_FLAG_FULLSCREEN|
                //隐藏导航栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT>=19){
            uiOptions |= 0x00001000;
        }else{
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        this.getWindow().getDecorView().setSystemUiVisibility(uiOptions);

    }
    public void initUI(){

    }

    protected void hideBottomUIMenu(){






        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
    public Handler h= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){

                case 0:
                  //  api.wakeupDevice();
                   // this.sendEmptyMessageDelayed(0, 3000);
                    api.disablesScreenTouch(false);
                break;

            }

        }
    };



    public View.OnClickListener clickListener_power=new View.OnClickListener(){
        @Override
        public void onClick(View v) {


               if(v.getId()==R.id.shutdownid) {
                    api.shutdownDevice();
               }else if(v.getId()==R.id.rebootid){
                    api.rebootDevice();
               }else  if(v.getId()==R.id.sleepid) {
                    api.sleepDevice();
                    h.sendEmptyMessageDelayed(0, 10000);
               }else if(v.getId()==R.id.wakeupid){
                    api.wakeupDevice();
               }else if(v.getId()==R.id.setsleeptimeid){

                    String txt=sleepedit.getText().toString();
                    if(txt!=null && !txt.equals("")){
                        int value=Integer.parseInt(txt);
                        if(value==-1) value=Integer.MAX_VALUE;
                        api.setScreenOffTime(value);
                    }

               }
        }
    };
    public View.OnClickListener clickListener_key=new View.OnClickListener(){
        @Override
        public void onClick(View v) {

                if(v.getId()==R.id.gethomekeyid) {
                    api.sendKeyEvent(KeyEvent.KEYCODE_HOME);
                }else if(v.getId()==R.id.getbackkeyid){
                    api.sendKeyEvent(KeyEvent.KEYCODE_BACK);
                }else  if(v.getId()==R.id.getmenukeyid) {
                    api.sendKeyEvent(KeyEvent.KEYCODE_MENU);
                }else if(v.getId()==R.id.sethomekeyid){

                    api.killApplicationProcess("com.android.apkinstaller");
                }
        }
    };
    public View.OnClickListener clickListener_apk=new View.OnClickListener(){
        @Override
        public void onClick(View v) {


            if(v.getId()==R.id.instalbacklid) {
                  api.installPackage("/sdcard/qq.apk");
              //  api.releaseKeyControl(-1);
                //  api.enableMassStorage(true);
               // api.disableCamera(true);
           //     api.requestKeyControl(KeyEvent.KEYCODE_POWER);
             //   api.disableStatusBarPanel(false);

            }else if(v.getId()==R.id.uninstalbacklid){
                  // api.uninstallPackage("com.tencent.mobileqq", true);
                  // api.getRunningTasks();
                 // api.enableAutoRotate(true);
               // api.turnOnBluetooth(false);
              //  api.getDeviceInfo();
             //   api.openUSBDebug(true);
                // h.removeMessages(0);
               // h.sendEmptyMessageDelayed(0, 1000);
             //   api.captureScreen();
                //api.enableMassStorage(true);
           //     api.doMasterClear(false);
//                api.setUSBConnectionType("ptp");
//                api.getUSBConnectionType();
              //  api.getUSBConnectionType();
             //  api.enableMassStorage(false);
              //  api.requestKeyControl(KeyEvent.KEYCODE_VOLUME_DOWN);
                //  api.enableMassStorage(false);
              //  api.hideSettingsResetMenu(getrue);
               // api.getExternSDCardList();
               // api.getExternSDCardList();
              //  api.formatStorage("/storage/59D5-110E");
              // api.setMicrophoneMute(true);
               //   api.openUSBDebug(true);
             //   api.disableCamera(true);
                //
              //  Bitmap bmp=api.captureScreen();
                   api.toggleGps(true);

//                 api.hideRecentSoftKey(false);
         //       api.hideBackSoftKey(false);
        //        api.hideHomeSoftKey(false);
         //       api.hideSettingsDeveloperOptions(false);
//                api.showStatusBarPanel(true);
//                api.setScreenOn(false);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        api.setScreenOn(true);
//                    }
//                }, 5000);
             //   api.disableStatusBarPanel(true);
              //  api.enableMultiWindow(true);
          //      api.setApkState("com.android.chrome", true);

            //    cleanAll();
              //  api.installPackage("/sdcard/qq.apk");
                api.getDeviceInfo();
            }else  if(v.getId()==R.id.intall_yes) {

                api.enableInstalApp(true);

            }else if(v.getId()==R.id.install_no){
                api.enableInstalApp(false);





            }else  if(v.getId()==R.id.uninstall_yes) {

//                ArrayList<String> list=new ArrayList<String>();
//                list.add("com.tencent.mobileqq");
//                list.add("com.scope.api");
//                list.add("com.scope.map");
//
//                api.addInstallPackageBlackList(list);
               // api.enableUninstallApp(true);
                api.openUSBDebug(true);
            }else if(v.getId()==R.id.uninstall_no){
                ArrayList<String> list=new ArrayList<String>();

                //api.removeInstallPackageBlackList(list);
                api.enableUninstallApp(true);
             //   api.hideSettingsResetMenu(false);
            }
        }
    };

//    public void cleanAll() {
//        try {
//            PackageManager packageManager = this.getPackageManager();
//            Method localMethod = packageManager.getClass().getMethod("freeStorageAndNotify", Long.TYPE,
//                    IPackageDataObserver.class);
//            Long localLong = Long.valueOf(getEnvironmentSize() - 1L);
//            Object[] arrayOfObject = new Object[2];
//            arrayOfObject[0] = localLong;
//            localMethod.invoke(packageManager, localLong, new IPackageDataObserver.Stub() {
//
//                @Override
//                public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
////                    Toast.makeText(getApplicationContext(), "清除状态： " + succeeded, Toast.LENGTH_SHORT).show();
//                    Log.i("TAG","清除状态： " + succeeded);
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static long getEnvironmentSize()
//    {
//        File localFile = Environment.getDataDirectory();
//        long l1;
//        if (localFile == null)
//            l1 = 0L;
//        while (true)
//        {
//
//            String str = localFile.getPath();
//            StatFs localStatFs = new StatFs(str);
//            long l2 = localStatFs.getBlockSize();
//            l1 = localStatFs.getBlockCount() * l2;
//            return l1;
//        }
//    }


    public View.OnClickListener clickListener_wifi=new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            if(v.getId()==R.id.wifiaddssidid) {
                  api.addSSID("SEA-Dev", "88888888", 2, true);
            }else if(v.getId()==R.id.wifiopenid){
                  api.turnOnWifi(true);
            }else  if(v.getId()==R.id.wifidireckid) {
            //      api.grantAllPermissions("com.tencent.mobileqq");
                  api.grantPermission("com.tencent.mobileqq", Manifest.permission.CALL_PHONE);
            }else if(v.getId()==R.id.wififorgetallid){
                  api.forgetAllWifi();
            }
        }
    };

    public View.OnClickListener clickListener_whiteip=new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            if(v.getId()==R.id.addwhiteipid) {

;

       //       api.addNetworkRuleWhitelistURL("ifeng");
//                api.addNetworkRuleWhitelistURL("jd");
 //              api.addNetworkRuleBlacklistURL("taobao");

 //               ArrayList<String> list=new ArrayList<String>();
 //               list.add("ifeng");
 //               list.add("jd");
 //               list.add("taobao");
 //               api.addNetworkRuleWhitelistURL(list);
                api.setMicrophoneMute(false);
            }else if(v.getId()==R.id.getwhiteipid){
               // api.getNetworkRuleWhiteURLlist();
  //              api.clearNetworkRule();
            }
        }
    };


    public View.OnClickListener clickListener_other=new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            if(v.getId()==R.id.openadbid) {
                  api.disableCamera(true);
            }else if(v.getId()==R.id.developid){
                  api.hideSettingsDeveloperOptions(false);
            }else if(v.getId()==R.id.resetid){
                  api.hideSettingsResetMenu(false);
            }else if(v.getId()==R.id.sdcardid){
                  api.enableMassStorage(false);
            }
        }
    };
}
