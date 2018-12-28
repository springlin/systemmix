package com.scope.api;

/**
 * Created by Administrator on 2018/3/6.
 */

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;


public class WifiApi {
    private static final String TAG = "WifiApi";

    private  WifiManager mWifiManager;

    private WifiInfo mWifiInfo;

    private ScanResult mresult;
    private Context mcontext;
    public static WifiApi WifiApi=null;

    public static WifiApi getInstance(Context context){
             if(WifiApi==null){
                 WifiApi=new WifiApi(context);
             }
             return WifiApi;
    }

    public WifiApi(Context context){
        mcontext = context;

        mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        OpenWifi(true);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }


    public boolean OpenWifi(boolean enable){
        boolean bRet = true;

        if(enable){
            if (!mWifiManager.isWifiEnabled()){
                bRet = mWifiManager.setWifiEnabled(true);
            }
        }else{
            if (mWifiManager.isWifiEnabled()){
                bRet = mWifiManager.setWifiEnabled(false);
            }
        }
        return bRet;
    }


    public boolean getScanResultsBySSID(String ssid){
        List<ScanResult> list = mWifiManager.getScanResults();
        for (ScanResult result : list){
            if (TextUtils.equals(result.SSID, ssid)){
                mresult=result;
                return true;
            }
        }
        return false;
    }

    public WifiConfiguration IsExsits(String SSID){
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\""+SSID+"\"")){
                return existingConfig;
            }
        }
        return null;
    }
    public boolean addConnect(String ssid, String password, int type, boolean isdefault){


        WifiConfiguration configuration = configWifiInfo(ssid, password, type);
        if(configuration==null) return false;
        int netId = configuration.networkId;
        if (netId == -1) {
            netId = mWifiManager.addNetwork(configuration);
        }
        mWifiManager.enableNetwork(netId, true);
        return true;

    }

    public  WifiConfiguration configWifiInfo(String SSID, String password, int type) {
        WifiConfiguration config = null;

        if (mWifiManager != null) {
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            if(existingConfigs==null) return null;
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig == null) continue;
                if (existingConfig.SSID.equals("\"" + SSID + "\"")  /*&&  existingConfig.preSharedKey.equals("\""  +  password  +  "\"")*/) {
                    config = existingConfig;
                    break;
                }
            }
        }
        if (config == null) {
            config = new WifiConfiguration();
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // 分为三种情况：0没有密码1用wep加密2用wpa加密
        if (type == 0) {// WIFICIPHER_NOPASSwifiCong.hiddenSSID = false;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == 1) {  //  WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 2) {   // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    public boolean forgetAllWiFi(){


        List<WifiConfiguration> listeners = mWifiManager.getConfiguredNetworks();
        Iterator<WifiConfiguration> iterator = listeners.iterator();

        try {

            Method[] methods=mWifiManager.getClass().getDeclaredMethods();
            Method method = null;
            for(int i=0;i<methods.length;i++){
                method=methods[i];
                System.out.println("method name : "+method.getName());
                if(method.getName().equals("forget")){
                    break;
                }else{
                    method=null;
                }
            }
            if(method==null) return false;

            method.setAccessible(true);

            while(iterator.hasNext()){
                WifiConfiguration wifilist = iterator.next();
                Utils.log("SSID = " + wifilist.SSID + " netId = " + String.valueOf(wifilist.networkId));
                //mWifiManager.forget(wifilist.networkId, null);
                Object invoke = method.invoke(mWifiManager, new Object[]{wifilist.networkId, null});
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}