package com.scope.api;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by Spring on 2018/3/1.
 */

public interface IApi  {


//=========  电源  ==============
    public boolean shutdownDevice();
    public boolean sleepDevice();
    public boolean wakeupDevice();
    public boolean rebootDevice();
    public boolean setScreenOffTime(int time);
    public boolean setScreenOn(boolean on);


    boolean setShutDownTime(boolean enable, int hour, int minute, int dayOfWeek);
    boolean setBootTime(boolean enable, int hour, int minute, int dayOfWeek);
//==========USB/SD============================

    boolean enableMassStorage(boolean enable);
    boolean formatStorage(String path);
    boolean disableOTG(boolean disable);
    boolean checkOTGState();
    boolean setUSBConnectionType(String type);
    String getUSBConnectionType();
    List<String>  getExternSDCardList();

//========== 按键 ============================
    int sendKeyEvent(int keycode);
    boolean requestKeyControl(int keycode);
    boolean releaseKeyControl(int keycode);
    boolean hideHomeSoftKey(boolean hide);
    boolean hideRecentSoftKey(boolean hide);
    boolean hideBackSoftKey(boolean hide);
    boolean disableStatusBarPanel(boolean disable);
    boolean showStatusBarPanel(boolean show);

//========== 网络白名单 ======================
    boolean addNetworkRuleWhitelist(List<String> addrList);
    boolean addNetworkRuleBlacklist(List<String> addrList);
    List<String>  getNetworkRuleWhitelist();
    boolean addNetworkRuleWhitelistURL(String url);
    boolean addNetworkRuleWhitelistURL(List<String> urllist);
    boolean addNetworkRuleBlacklistURL(String url);
    List<String>  getNetworkRuleWhiteURLlist();
    boolean clearNetworkRule();

//==========应用安装和卸载=====================
    List<String> getRunningTasks();
    boolean killApplicationProcess(String packageName);
    boolean enableInstallation();
    boolean enableInstalApp(boolean enable);
    boolean enableUninstallApp(boolean enable);
    boolean installPackage(String packagePath);
    boolean uninstallPackage(String packageName, boolean keepData);
    boolean setApkState(String packageName, boolean enble);


    boolean addInstallPackageWhiteList(List<String> packageNames);
    List<String> getInstallPackageWhiteList();
    boolean  removeInstallPackageWhiteList(List<String> packageNames);

    boolean addInstallPackageBlackList(List<String> packageNames);
    List<String> getInstallPackageBlackList();
    boolean  removeInstallPackageBlackList(List<String> packageNames);


    boolean addUnInstallPackageWhiteList(List<String> packageNames);
    List<String> getUnInstallPackageWhiteList();
    boolean  removeUnInstallPackageWhiteList(List<String> packageNames);

    boolean addUnInstallPackageBlackList(List<String> packageNames);
    List<String> getUnInstallPackageBlackList();
    boolean  removeUnInstallPackageBlackList(List<String> packageNames);

//=================WiFi==========================
    boolean addSSID(String ssid, String password, int type, boolean isdefault);
    boolean turnOnWifi(boolean enable);
    boolean disableWifiDirect(boolean disable);
    boolean forgetAllWifi();

//=====================应用授权====================
    boolean grantPermission(String packageName,String permission );
    boolean grantAllPermissions(String packageName);

//====================其它===========================
    boolean enableAutoRotate(boolean enable);
    boolean setDesktopWallpaper(String uri);
    String getDeviceInfo();
    boolean disableMultiUser(boolean disabled);
    boolean disableCamera(boolean disable);
    boolean turnOnBluetooth(boolean enable);
    boolean disableBluetoothShare(boolean disable);
    boolean setMicrophoneMute(boolean disable);
    Bitmap captureScreen();

    boolean openUSBDebug(boolean enable);
    boolean hideSettingsDeveloperOptions(boolean hide);
    boolean checkSettingDeveloperMode();
    boolean setBrightness(int value);
    boolean setScreenLightMinValue(int min);
    boolean setScreenLightMaxValue(int max);
    boolean setLocatingMethod(int mode);
    boolean enableMultiWindow(boolean enable);
    boolean enableBatterySaving(boolean enable);
    boolean enableSuperBatterySaving(boolean enable);
    boolean setCustomLauncher(String packageName, String className);
    boolean clearCustomLauncher();
    boolean disableStatusBarNotification(boolean disable);
    boolean disableLockScreenNotification(boolean disable);
    boolean hideSettingsResetMenu(boolean hide);
    boolean doMasterClear(boolean mEraseSdCard);
    boolean disablesScreenTouch(boolean disable);
    boolean setDefaultApk(String packageName, String key);
    boolean toggleGps(boolean status);
}
