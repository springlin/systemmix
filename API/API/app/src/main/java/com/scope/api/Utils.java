package com.scope.api;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/3/1.
 */

public class Utils {
     public static final String tag="API";



    public static String[] sOpPerms = new String[] {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            null,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG,
            android.Manifest.permission.READ_CALENDAR,
            android.Manifest.permission.WRITE_CALENDAR,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            null, // no permission required for notifications
            null, // neighboring cells shares the coarse location perm
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_SMS,
            null, // no permission required for writing sms
            android.Manifest.permission.RECEIVE_SMS,
            null,
            android.Manifest.permission.RECEIVE_MMS,
            android.Manifest.permission.RECEIVE_WAP_PUSH,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_SMS,
            null, // no permission required for writing icc sms
            android.Manifest.permission.WRITE_SETTINGS,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            null,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            null, // no permission for playing audio
            null, // no permission for reading clipboard
            null, // no permission for writing clipboard
            null, // no permission for taking media buttons
            null, // no permission for taking audio focus
            null, // no permission for changing master volume
            null, // no permission for changing voice volume
            null, // no permission for changing ring volume
            null, // no permission for changing media volume
            null, // no permission for changing alarm volume
            null, // no permission for changing notification volume
            null, // no permission for changing bluetooth volume
            android.Manifest.permission.WAKE_LOCK,
            null, // no permission for generic location monitoring
            null, // no permission for high power location monitoring
            android.Manifest.permission.PACKAGE_USAGE_STATS,
            null, // no permission for muting/unmuting microphone
            null, // no permission for displaying toasts
            null, // no permission for projecting media
            null, // no permission for activating vpn
            null, // no permission for supporting wallpaper
            null, // no permission for receiving assist structure
            null, // no permission for receiving assist screenshot
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ADD_VOICEMAIL,
            Manifest.permission.USE_SIP,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.USE_FINGERPRINT,
            Manifest.permission.BODY_SENSORS,
            null,
            null,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            null, // no permission for turning the screen on
            Manifest.permission.GET_ACCOUNTS,
            null, // no permission for running in background
     };
     public static void log(String msg){
         if(msg!=null) Log.i(tag, msg);

     }
    public static void installSilentWithReflection(Context context, String filePath) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Method method = packageManager.getClass().getDeclaredMethod("installPackage",
                    new Class[] {Uri.class, IPackageInstallObserver.class, int.class, String.class} );
            method.setAccessible(true);
            File apkFile = new File(filePath);
            Uri apkUri = Uri.fromFile(apkFile);

            Object invoke = method.invoke(packageManager, new Object[]{apkUri, new IPackageInstallObserver.Stub() {
                @Override
                public void packageInstalled(String pkgName, int resultCode) throws RemoteException {
                    Utils.log("packageInstalled = " + pkgName + "; resultCode = " + resultCode);
                }
            }, Integer.valueOf(2), ""});

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void uninstallSilentWithReflection(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();

            Utils.getClassAll(packageManager.getClass().getName());
            Method method = packageManager.getClass().getDeclaredMethod("deletePackage",
                    new Class[] {String.class, IPackageDeleteObserver.class, int.class} );
            method.setAccessible(true);


            Object invoke = method.invoke(packageManager, new Object[]{packageName, new IPackageDeleteObserver.Stub() {
                @Override
                public void packageDeleted(String pkgName, int resultCode) throws RemoteException {
                    Utils.log("packageUnInstalled = " + pkgName + "; resultCode = " + resultCode);
                }
            }, Integer.valueOf(2)});

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Method getClass(String className, String packageName){
         try{
            Class<?> mClass = Class.forName(className);
            Method[] methods=mClass.getDeclaredMethods();

            for(int i=0;i<methods.length;i++){
                Method method=methods[i];
                //System.out.println("method name : "+method.getName());
                if(method.getName().equals(packageName)){

                    return method;

                }
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getClassAll(String className){
        try{
            Class<?> mClass = Class.forName(className);
            Method[] methods=mClass.getDeclaredMethods();

            for(int i=0;i<methods.length;i++){
                Method method=methods[i];
                System.out.println("method name : "+method.getName());

            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String execCommand(String command,  ShellCommandListener listener)  {

        int exitCode = -1;
        CommandResult result = null;
        if (command == null && listener!=null) {
            result = new CommandResult(exitCode, null, null);
            listener.onCommandFinished(result);
        }

        Process process = null;
        BufferedReader successReader = null;
        BufferedReader errorReader = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        try {
                DataOutputStream os = null;
                process = Runtime.getRuntime().exec("su" );
                os = new DataOutputStream(process.getOutputStream());

                os.write(command.getBytes());
                os.writeBytes("\n");
                os.flush();

                os.writeBytes("exit\n");
                os.flush();

                exitCode = process.waitFor();
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();

                successReader = new BufferedReader(new InputStreamReader( process.getInputStream()));
                errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s = null;
                while ((s = successReader.readLine()) != null) {
                  //  Utils.log("=="+s+"==");
                    successMsg.append(s + "\n");
                }
                while ((s = errorReader.readLine()) != null) {
                    errorMsg.append(s + "\n");
                }

                if (exitCode == -257) {
                    throw new TimeoutException();
                }


                if (os != null) {
                    os.close();
                }
                if (successReader != null) {
                    successReader.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
        }catch (InterruptedException e) {
        }catch (TimeoutException e) {
        }catch (IOException e) {
            e.printStackTrace();
        }

        if (process != null) {
            process.destroy();
        }

  //      Utils.log(successMsg == null ? null: successMsg.toString());
        if(errorMsg != null && errorMsg.length()>1) Utils.log("error:"+  errorMsg.toString());
        if (listener != null) {
            result = new CommandResult(exitCode, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null : errorMsg.toString());
            listener.onCommandFinished(result);
        }
        String ret=(successMsg == null ? null : successMsg.toString());
//        if(successMsg != null && !successMsg.equals("")) {
//            ret=successMsg.toString();
//        }else if(errorMsg != null){
//            ret=errorMsg.toString();
//        }
        return  ret;
    }
    public static String getStringIP(String str) {

        String regex = "\\d+\\.\\d+\\.\\d+\\.\\d+(/\\d+)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            String ip = matcher.group(0);
            return ip;

        }
        return null;
    }

    public static String getStringKey(String str) {

        String regex ="\"(.*?)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            String s = matcher.group(0).replace("\"","");
            return s;

        }
        return null;
    }
    public static class CommandResult {


        public int exitCode;

        public String successMsg;

        public String errorMsg;

        public CommandResult(int result) {
            this.exitCode = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.exitCode = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "exitCode=" + exitCode + "; successMsg=" + successMsg
                    + "; errorMsg=" + errorMsg;
        }
    }

    public interface ShellCommandListener {
        public void onCommandFinished(CommandResult result);
    }

    public static void execCommandB(String command) throws IOException, InterruptedException {

        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);

        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        // read the ls output
        String line = "";
        StringBuilder sb = new StringBuilder(line);
        while ((line = bufferedreader.readLine()) != null) {
            //System.out.println(line);
            sb.append(line);
            sb.append('\n');
        }

        if (proc.waitFor() != 0) {
            System.err.println("exit value = " + proc.exitValue());
        }
    }
    public static int getManifestpermissionCode(String permiss){
        int code=-1;

        if(permiss==null) return code;
        for(int i=0; i<sOpPerms.length; i++){

            String str=sOpPerms[i];
            if(str!=null && str.equals(permiss)){
                return i;
            }
        }

        return code;
    }

    public static boolean grantPermission(Context context, String packageName, int code )  {

        if(packageName==null || packageName.equals("")) return false;
        try{
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appinfo = packageManager.getApplicationInfo(packageName, 0);
            AppOpsManager mAppOps=(AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            Method method=Utils.getClass(mAppOps.getClass().getName(), "setMode");
            method.setAccessible(true);
            if(code==-1) {
                return false;
            }
            method.invoke(mAppOps, new Object[]{code, appinfo.uid, appinfo.packageName, AppOpsManager.MODE_ALLOWED});
            Utils.log(appinfo.packageName+" "+appinfo.uid+" "+code);

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

    public static void unMount(Context mContext) {
        try {
            log("issfvs");
            StorageManager mSD = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
            List<Object> list = (List<Object>) StorageManager.class.getMethod("getVolumes").invoke(mSD);
            log("list" + list.toString());
            for (int i = 0; i < list.size(); i++) {
                Object volume = list.get(i);
                if (volume != null) {
                    String id = (String) Class.forName("android.os.storage.VolumeInfo").getMethod("getId").invoke(volume);
                    log("is " + id);
                    int type = (int) Class.forName("android.os.storage.VolumeInfo").getMethod("getType").invoke(volume);
                    log( "is " + type);
                    // public
                    if (type == 0) {
                        StorageManager.class.getMethod("unmount", String.class).invoke(mSD, id);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log( e.getStackTrace().toString());

        }
    }
    public static boolean setSystemProperties(String key, String value){


        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
           // Utils.getClassAll(c.getName());
            Method set = c.getMethod("set", String.class,String.class);
            set.invoke(c, key,value);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static String getSystemProperties(String key){


        String result="";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");

            Method get = c.getMethod("get", String.class);
            result=(String)get.invoke(c, key);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return result;

    }


}
