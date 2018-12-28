/**
 * 
 */
package com.myun.net.protocol;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.myun.core.MusicService;








import com.myun.utils.Utils;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.SystemClock;
import android.util.Log;



public class PeerServerIP implements Peer {

	private static final String TAG = "PeerServerIP";

	private DatagramSocket socket = null;

	private IPeerListener peerListener = null;



	private Context mContext;
	private String localPeerName;
	private String randomCode = null;

	public static AudioManager audioManager;
	
	private HashMap<Integer, String> channelMap = new HashMap<Integer, String>();
	private Lock lock = new ReentrantLock();


	public PeerServerIP(Context context, String peerName) {
		this.mContext = context;
		localPeerName = peerName;
		
		new Thread(new UDPMessageLooper(), "UDPMessageLooper").start();
		
		
		audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public int sendMessage(int channel, byte[] msg, int len) {
		int result = -1;
		try {
			String sendChannel = channelMap.get(0);
			if (sendChannel != null && !"".equals(sendChannel)) {

				String[] info = channelMap.get(0).split(":");
				String host = info[0];
				String port = info[1];

//				Log.i(TAG, "---------------------------------send Host: " + host + ", Port: " + port);

				DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByName(host), Integer.valueOf(port));
				socket.send(packet);
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public int sendMessageNoWait(int channel, String data) {

		lock.lock();
		int result = -1;
		if(channel>=channelMap.size()) return result;
		
		try {
			String sendChannel = channelMap.get(channel);
			if (sendChannel != null && !"".equals(sendChannel)) {

				String[] info = channelMap.get(channel).split(":");
				String host = info[0];
				String port = info[1];

				PeerMessage.Header respHeader = new PeerMessage.Header(0x0001, PeerMessage.VERSION, PeerMessage.PEER_MESSAGE_RESPONSE_MASK | PeerMessage.PEER_MESSAGE_HEART_BEAT);
				
				byte[] msg=PeerMessage.Builder.buildResponse(respHeader, data);
				
				Log.e(TAG, "CallBack-------------------send Host: " + host + ", Port: " + port);

				
				DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByName(host), Integer.valueOf(port));
				socket.send(packet);

				channelMap.remove(channel);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
        lock.unlock();
		return result;
	}
    public IPeerListener getPeerListener() {
		return peerListener;
	}

//	public void setPeerListener(IPeerListener peerListener) {
//		this.peerListener = peerListener;
//	}
	@Override
	public int setListener(IPeerListener listener) {
		this.peerListener = listener;
		return 0;
	}

	
	
	
	private class UDPMessageLooper implements Runnable {

		private DatagramPacket sendPacket;
		HashMap <String, Integer> seqMap = new HashMap<String, Integer>();
		int type = 0;
		int nowUID = 0;
		int nowSEQ = 0;
		int nowVER = 0;	
		String key = "";
		
//		private boolean k(){
//			boolean result = false;
//			key = String.valueOf(nowUID) + "[" + String.valueOf(type) + "]";
//			if (nowVER >= 0x00030000) { //Only check sequence when version 3.0 or later						
//				if (seqMap.get(key) != null) {
//					int lastSEQ = seqMap.get(key).intValue();
//					if (lastSEQ == 65535) {
//                        if (nowSEQ == lastSEQ) {
//							Log.w(TAG, "Sequence error1! discard packet!" + " key:" + key + ", nowSEQ:" + nowSEQ + ", lastSEQ:" + lastSEQ);
//							result = true;
//						}
//					}else {
//						if (nowSEQ <= lastSEQ) {
//							Log.w(TAG, "Sequence error2! discard packet!" + " key:" + key + ", nowSEQ:" + nowSEQ + ", lastSEQ:" + lastSEQ);
//							result = true;
//						}
//					}
//				}
//				//Log.d(TAG, "Update UID:" + nowUID + ", SEQ:" + nowSEQ + " from: " + recvPacket.getAddress().getHostAddress() + " port:" + recvPacket.getPort());
//				seqMap.put(key, nowSEQ);
//			}
//			
//			return result;
//		}
		
		@Override
		public void run() {			
			int result;
			int lastSeq_startApp = -1;
			int lastSeq_startAppPackage = -1;
			int lastSeq_setupWifi = -1;
			int lastSeq_installApk = -1;
			int lastSeq_remoteShowPhoto = -1;
			int lastSeq_userCmd = -1;
			int lastSeq_scanWifi = -1;
			
			byte[] msg;
			byte[] buffer = new byte[1024*1024];
			final DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
			
			try {
				socket = new DatagramSocket(Peer.SERVER_UDP_PORT);
				socket.setBroadcast(true);
				socket.setReuseAddress(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i(TAG, "Peer.SERVER_UDP_PORT : "+Peer.SERVER_UDP_PORT);
			while (true) {
				try {
					recvPacket.setLength(buffer.length);
					socket.receive(recvPacket);

					byte[] data = recvPacket.getData();
					PeerMessage.Header header = PeerMessage.Resolver.getHeader(data);
					
					type = header.getDataType();
					nowUID = header.getSequence() & 0xFFFF0000;
					nowSEQ = header.getSequence() & 0x0000FFFF;
					nowVER = header.getVersion();	
					

					final PeerMessage.Header respHeader = new PeerMessage.Header(header.getSequence(), PeerMessage.VERSION, PeerMessage.PEER_MESSAGE_RESPONSE_MASK | type);

					switch (type) {
					case PeerMessage.PEER_MESSAGE_FIND_PEER:
						// ip,ID,name
						String peerInfo = PeerTools.getLocalIpAddress(mContext)+";" + PeerMessage.NID_FLAG + PeerTools.getNID(mContext).replaceAll(":", "").substring(6) + ";"+ ((localPeerName != null) ? localPeerName : "");
						msg = PeerMessage.Builder.build(respHeader, peerInfo);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						// Log.i(TAG, "Send PEER_MESSAGE_FIND_PEER ACK  peerInfo="+peerInfo);
						break;
					case PeerMessage.PEER_MESSAGE_REMOTE_PLAY_INFO:
						String playerInfo = null;
						if (peerListener != null) {
							try {
								playerInfo=peerListener.getMusicPlayerStatus();
								msg = PeerMessage.Builder.build(respHeader, playerInfo);
								sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
								socket.send(sendPacket);
								
							} catch (Exception e) {
								
							}
						} else {
							msg = PeerMessage.Builder.build(respHeader, "end\n");
							sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
							socket.send(sendPacket);
						}

						break;

					case PeerMessage.PEER_MESSAGE_HEART_BEAT:
						String status=null;
						if (peerListener != null) {
							status=peerListener.getMusicServieRunStatus();
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, status);
						sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getByName("255.255.255.255"), Peer.SERVER_UDP_PORT+1);
						socket.send(sendPacket);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						break;

					case PeerMessage.PEER_MESSAGE_CONNECT:
						String[] attachInfo = PeerMessage.Resolver.getAttachInfo(data);
		
						String id = attachInfo[0];
						String nid = PeerTools.getNID(mContext);
						
						if (id != null && nid != null && id.equals(PeerMessage.NID_FLAG + nid.replaceAll(":", "").substring(6))) {
							msg = PeerMessage.Builder.buildResponse(respHeader, 0);
							sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
							socket.send(sendPacket);

//							Intent i = new Intent(PeerMessage.MELE_NSCREEN_ACTION_STATE_CHANGE);
//							i.putExtra(PeerMessage.MELE_NSCREEN_EXTRA_PEER_STATE, PeerMessage.MELE_NSCREEN_PEER_STATE_ATTACH);
//							i.putExtra(PeerMessage.MELE_NSCREEN_EXTRA_ATTACH_INFO, attachInfo[1]);
//							mContext.sendBroadcast(i);
							
							if (peerListener != null) {
								peerListener.newClientDevice(recvPacket.getAddress().getHostAddress(), attachInfo[1]);
							}		
							Log.i(TAG, "Send PEER_MESSAGE_CONNECT ACK  success ,attachInfo[0]=" + attachInfo[0] + ",attachInfo[1]=" + attachInfo[1]);
						} else {
							msg = PeerMessage.Builder.buildResponse(respHeader, -1);
							sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
							socket.send(sendPacket);
							Log.i(TAG, "Send PEER_MESSAGE_CONNECT ACK  fail");
						}
						break;
						
					case PeerMessage.PEER_MESSAGE_MUSIC_LIST:
						
						
						String musicinfo=null;
						if (peerListener != null) {

							musicinfo=peerListener.getMusicInfo();
						}																		
					    msg = PeerMessage.Builder.buildResponse(respHeader, musicinfo);

					    Utils.log("send music data size :"+msg.length);
						DataInputStream is = new DataInputStream(new ByteArrayInputStream(msg));
						
						byte[] buf=new byte[48*1024];
						int size=0;
						while( (size=is.read(buf))!=-1 ){
								 sendPacket = new DatagramPacket(buf, size,  recvPacket.getAddress(), recvPacket.getPort());
				                 socket.send(sendPacket);
						}						
						break;
					case PeerMessage.PEER_MESSAGE_MUSIC_LIST_CLIENT:
						
						
						String clientinfo=PeerMessage.Resolver.getResultString(data);
						int len=0;
						if (peerListener != null) {
							len=peerListener.ClientMusicList(clientinfo);							
						}	
					    msg = PeerMessage.Builder.buildResponse(respHeader, len);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;						
					case PeerMessage.PEER_MESSAGE_KEY_EVENT:					    
						if (peerListener != null) {
							peerListener.onKeyCode(PeerPlatformLayer.injectKeyEvent(data));
						}
						break;
					case PeerMessage.PEER_MESSAGE_MOTION_EVENT:
						
						if (peerListener != null) {
							peerListener.onMouseEvent( PeerPlatformLayer.injectMotionEvent(data));
						}					
						break;
					case PeerMessage.PEER_MESSAGE_START_APP:
						
						if (checkBadCommand()) {
							break;
						}
						
						
						if (header.getSequence() == lastSeq_startApp) {
							Log.w(TAG, "startApp repeat packet! discade it!");
							break;
						}
						lastSeq_startApp = header.getSequence();
						
					
						
					
						//Log.i(TAG, "startApp.......lastSeq_startApp="+lastSeq_startApp);
						
						Intent intent = PeerMessage.Resolver.getIntent(data);
						result=0;
						if (peerListener != null) {
							result=peerListener.startMusicPlayer(intent);
						}
	
						msg = PeerMessage.Builder.buildResponse(respHeader, result);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);

						// Save last IP and port
						//channelMap.put(0, recvPacket.getAddress().getHostAddress() + ":" + recvPacket.getPort());
						break;
						
					case PeerMessage.PEER_MESSAGE_KILL_APP:	
						
						if (checkBadCommand()) {
							break;
						}
						
						final String killPackageName = PeerMessage.Resolver.getResultString(data);
						new Thread(new Runnable() {

							@Override
							public void run() {
								int killResult;
								try {

									Log.w(TAG, "kill PackageName " + killPackageName);
									MusicService.killApp(killPackageName);
									killResult = 0;


								} catch (Exception e) {
									Log.e(TAG, "Exception", e);
									killResult = -1;
								}

								byte[] msg = PeerMessage.Builder.buildResponse(respHeader, killResult);
								sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
								try {
									socket.send(sendPacket);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}).start();
							

						break;	
					case PeerMessage.PEER_MESSAGE_START_APP_PACKAGE:	
						
						if (checkBadCommand()) {
							break;
						}
						
						if (header.getSequence() == lastSeq_startAppPackage) {
							Log.w(TAG, "startAppPackage repeat packet! discade it!");
							break;
						}
						lastSeq_startApp = header.getSequence();
						
						try {
							Log.i(TAG, "startAppPackage.......lastSeq_startAppPackage="+lastSeq_startAppPackage);
							String packageName = PeerMessage.Resolver.getResultString(data);
							if (peerListener != null) {
								peerListener.startAPP(packageName);
							}
							mContext.startActivity(mContext.getPackageManager().getLaunchIntentForPackage(packageName));
//							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							mContext.startActivity(intent);
							result = 0;
						} catch (ActivityNotFoundException e) {
							Log.e(TAG, "ActivityNotFoundException", e);
							result = -1;
						}
						
						msg = PeerMessage.Builder.buildResponse(respHeader, result);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						// Save last IP and port
						channelMap.put(0, recvPacket.getAddress().getHostAddress() + ":" + recvPacket.getPort());
						break;

					case PeerMessage.PEER_MESSAGE_SETUP_WIFI:
						
						if (checkBadCommand()) {
							break;
						}
						
						if (header.getSequence() == lastSeq_setupWifi) {
							Log.w(TAG, "setupWifi repeat packet! discade it!");
							break;
						}
						lastSeq_setupWifi = header.getSequence();
						
						String[] info = PeerMessage.Resolver.getWifiInfo(data);
						Log.i(TAG, "WifiInfo: ssid=" + info[0] + ",security=" + info[1] + ",password=" + info[2]);
						if (peerListener != null) {
							peerListener.setupWiFi(info[0], info[1], info[2]);
						}
						
//						Intent i = new Intent(PeerMessage.MELE_NSCREEN_ACTION_WIFI_STATE_CHANGE);
//						i.putExtra(PeerMessage.MELE_NSCREEN_EXTRA_RESULT_CODE, info[0]);
//						mContext.sendBroadcast(i);

						msg = PeerMessage.Builder.buildResponse(respHeader, 0);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						// send repeat packets
						socket.send(sendPacket);
						//socket.send(sendPacket);
						//socket.send(sendPacket);

						
						//Thread.sleep(2 * 1000);

						
						break;
					case PeerMessage.PEER_MESSAGE_INSTALL_APK:
						
						if (checkBadCommand()) {
							break;
						}
						
						if (header.getSequence() == lastSeq_installApk) {
							Log.w(TAG, "installApk repeat packet! discade it!");
							break;
						}
						lastSeq_installApk = header.getSequence();
						
						
						String appitems[]=PeerMessage.Resolver.getResultStringArray(data);
						if (peerListener != null) {
							peerListener.installApp(appitems[0], appitems[1]);
						}	
						
	
						msg = PeerMessage.Builder.buildResponse(respHeader, 0);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						Log.i(TAG, "Send PEER_MESSAGE_INSTALL_APK response!!!");

						break;
					case PeerMessage.PEER_MESSAGE_GET_INSTALL_PROGRESS:
						
						
						String res = "";
						if (peerListener != null) {
							res=peerListener.getInstallAppProgress();    
						}	
						msg = PeerMessage.Builder.buildResponse(respHeader, res);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						//Log.i(TAG, "Send PEER_MESSAGE_GET_INSTALL_APK_PROGRESS "+res);

						break;
					case PeerMessage.PEER_MESSAGE_UNINSTALL_APK:
						Log.i(TAG, "uninstall start...");
						String appName = PeerMessage.Resolver.getAppURL(data);

						Log.i(TAG, "uninstall " + appName);
						
						result=-1;
						if (peerListener != null) {
							result=peerListener.uninstallApp(appName);    
						}	
						Log.i(TAG, "uninstall finish...");

						msg = PeerMessage.Builder.buildResponse(respHeader, result);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						break;
					case PeerMessage.PEER_MESSAGE_GET_APK_VERSION:
						String packageName = PeerMessage.Resolver.getAppName(data);
						PackageInfo checkPackageInfo = null;
						PackageManager manager = mContext.getPackageManager();
						String version = "";
						try {
							checkPackageInfo = manager.getPackageInfo(packageName, 0);
							version = checkPackageInfo.versionName;
						} catch (NameNotFoundException e) {
							version = PeerMessage.PEER_EXCEPTION_NAME_NOT_FOUND;
						}

						msg = PeerMessage.Builder.buildResponse(respHeader, version);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);

						break;
					case PeerMessage.PEER_MESSAGE_GET_SOFTWARE_VERSION:
						
						
						
					       String sw_version = "";   
					       try {   
					           // ---get the package info---   
					           PackageManager pm = mContext.getPackageManager();   
					           PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);   
					           sw_version = pi.versionName;   
					       } catch (Exception e) {   
					           Log.e("VersionInfo", "Exception", e);   
					       }  
						Log.i(TAG, "sw_version="+sw_version);
						msg = PeerMessage.Builder.buildResponse(respHeader, sw_version);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						break;
						
					case PeerMessage.PEER_MESSAGE_GET_HARDWARE_VERSION:
						String hw_version = android.os.Build.MODEL;

						msg = PeerMessage.Builder.buildResponse(respHeader, hw_version);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						break;
					case PeerMessage.PEER_MESSAGE_GET_FIRMWARE_VERSION:
						String fw_version = android.os.Build.VERSION.SDK;
						msg = PeerMessage.Builder.buildResponse(respHeader, fw_version);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						break;

					case PeerMessage.PEER_MESSAGE_GET_IP_ADDRESS:
						String ip = PeerTools.getLocalIpAddress(mContext);

						msg = PeerMessage.Builder.buildResponse(respHeader, ip);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						break;
					case PeerMessage.PEER_MESSAGE_GET_NETWORK_AVAIABLE:
						msg = PeerMessage.Builder.buildResponse(respHeader, 0);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						break;
					case PeerMessage.PEER_MESSAGE_GET_WIFI_MODE:
//						WifiAp apManager = new WifiAp(mContext);
//
//						msg = PeerMessage.Builder.buildResponse(respHeader, apManager.IsWifiApEnabled() ? 1 : 0);
//						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
//						socket.send(sendPacket);
						break;
					case PeerMessage.PEER_MESSAGE_GET_REMOTE_SSID:
//						String ssid = WifiAdmin.getInstance(mContext).getSsid();
//
//						msg = PeerMessage.Builder.buildResponse(respHeader, ssid);
//						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
//						socket.send(sendPacket);
						break;
					case PeerMessage.PEER_MESSAGE_GET_REMOTE_WIFI_CONFIGED:
//						int wifiConfiged = WifiAdmin.getInstance(mContext).getWifiConfiged();
//
//						msg = PeerMessage.Builder.buildResponse(respHeader, wifiConfiged);
//						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
//						socket.send(sendPacket);
						break;
						
					case PeerMessage.PEER_MESSAGE_SET_WIFI_ENABLED:
						//send command success
						
					    int enable = PeerMessage.Resolver.getResultCode(data);
						if (peerListener != null) {
							peerListener.setWiFiCtrl(2, "", enable==1?true:false);    
						}	
						Log.i(TAG, "PEER_MESSAGE_SET_WIFI_ENABLED start .........."+enable);
						msg = PeerMessage.Builder.buildCommand(respHeader, 0);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						Log.i(TAG, "PEER_MESSAGE_SET_WIFI_ENABLED end ..........");
//						
//						Thread.sleep(1000);
//						//1: enable 0: disable
//						
//						boolean enabledResult = WifiAdmin.getInstance(mContext).setWifiEnabled(enable == 1 ? true : false);

						break;
					case PeerMessage.PEER_MESSAGE_GET_WIFI_ENABLED:
						
//						enabledResult = WifiAdmin.getInstance(mContext).getWifiEnabled();
//						//1: enable 0: disable
//						msg = PeerMessage.Builder.buildCommand(respHeader, enabledResult ? 1 : 0);
//						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
//						socket.send(sendPacket);
						break;
					case PeerMessage.PEER_MESSAGE_FORGET_WIFI:
					
					    String ssid  = PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							peerListener.setWiFiCtrl(0, ssid, true);    
						}	
						Log.i(TAG, "PEER_MESSAGE_FORGET_WIFI ssid .........."+ssid);
						msg = PeerMessage.Builder.buildCommand(respHeader, 0);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
					case PeerMessage.PEER_MESSAGE_DISCONNECT_WIFI:
						
					    String ssidA  = PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							peerListener.setWiFiCtrl(1, ssidA, true);    
						}	
						Log.i(TAG, "PEER_MESSAGE_DISCONNECT_WIFI ssid .........."+ssidA);
						msg = PeerMessage.Builder.buildCommand(respHeader, 0);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);						
						break;
					case PeerMessage.PEER_MESSAGE_REMOTE_EXIT:
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);


						break;

					case PeerMessage.PEER_MESSAGE_REMOTE_STOP:
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						if (peerListener != null) {
							peerListener.setMusicPlayerCmd("stop", null);
						}	

						break;
					case PeerMessage.PEER_MESSAGE_REMOTE_PAUSE:
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						if (peerListener != null) {
							peerListener.setMusicPlayerCmd("pause", null);
						}	

						break;
					case PeerMessage.PEER_MESSAGE_REMOTE_PLAY:
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						if (peerListener != null) {
							peerListener.setMusicPlayerCmd("play", null);
						}	

						break;
						
					case PeerMessage.PEER_MESSAGE_REMOTE_NEXT:
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						if (peerListener != null) {
							peerListener.setMusicPlayerCmd("next", null);
						}	

						break;
					case PeerMessage.PEER_MESSAGE_REMOTE_PREVIEUS:
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						if (peerListener != null) {
							peerListener.setMusicPlayerCmd("pre", null);
						}	

						break;
					case PeerMessage.PEER_MESSAGE_PLAY_MODE:
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						final String var= PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							peerListener.setMusicPlayerCmd("playmode", var);
						}	

						break;
					

					case PeerMessage.PEER_MESSAGE_REMOTE_SEEK:
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
                        final int position = PeerMessage.Resolver.getPosition(data);
						if (peerListener != null) {
							peerListener.setMusicPlayerCmd("seek", position+"");
						}	
	
						break;

					case PeerMessage.PEER_MESSAGE_REMOTE_VOLUME:
						 int value = PeerMessage.Resolver.getVolume(data);
						 //Log.e("volume","volume  ... "+ value);
						// int max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
						 if(value>=0 && value<=32){
//							 if (peerListener != null) {
//                              
//								 peerListener.setVolTAS5711(value);
//								 
//							 }
//							 value=value*max/100;
//							 audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
							 setSpeakerVolume(value);
							 value=10000;
						 }else{
							 
							 value=getRemoteVolume();
							 //value=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
							 //value=100*value/max;
						 }
						
						 msg = PeerMessage.Builder.buildResponse(respHeader, value);
						 sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						 socket.send(sendPacket);
						 break;
					case PeerMessage.PEER_MESSAGE_REMOTE_MUTE:
						
						int sur=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						if(getMuteState()==1)
						    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
						else
							audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
						 
					     msg = PeerMessage.Builder.buildResponse(respHeader, sur);
						 sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						 socket.send(sendPacket);
						 break;

					case PeerMessage.PEER_MESSAGE_MIC:
						
						if (checkBadCommand()) {
							break;
						}
						
						if (header.getSequence() == lastSeq_remoteShowPhoto) {
							Log.w(TAG, "remoteShowPhoto repeat packet! discade it!");
							break;
						}
						lastSeq_remoteShowPhoto = header.getSequence();						
						
						msg = PeerMessage.Builder.buildDefault(respHeader);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);

						if (peerListener != null) {


						}
						break;





					
					case PeerMessage.PEER_MESSAGE_SCAN_REMOTE_WIFI:
						
						if (checkBadCommand()) {
							break;
						}
						
						if (header.getSequence() == lastSeq_scanWifi) {
							Log.w(TAG, "scanWifi repeat packet! discade it!");
							break;
						}
						lastSeq_scanWifi = header.getSequence();
						

						
						break;
						
					case PeerMessage.PEER_MESSAGE_REMOTE_WIFI_LIST:
						
						
						 String wifi_info="";

						 if (peerListener != null) {
							 wifi_info=peerListener.getWiFiList();
						 }
						 msg = PeerMessage.Builder.buildResponse(respHeader, wifi_info);
						 sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						 socket.send(sendPacket);
						 
						
						 break;
					case PeerMessage.PEER_MESSAGE_REMOTE_ALL_WIFI_LIST:
						

						
						break;
						
					case PeerMessage.PEER_MESSAGE_GET_REMOTE_VOLUME:
						int mute = getMuteState();
						int volume = getRemoteVolume();
						msg = PeerMessage.Builder.buildCommand_mute_volume(respHeader, mute, volume);
//						Log.i(TAG, "PEER_MESSAGE_REMOTE_WIFI_LIST..wifiList="+wifiList);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
					case PeerMessage.PEER_MESSAGE_CANCEL_CONFIG_AP:
						
//						i = new Intent("MELE_NSCREEN_ACTION_AP_STATE_CHANGE");
//						i.putExtra("config_ap_state", WifiApManager.AP_CONFIG_STATE_CANCEL);
//						mContext.sendBroadcast(i);
//						
//						msg = PeerMessage.Builder.buildDefault(respHeader);
//						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
//						socket.send(sendPacket);
						
						break;

					case PeerMessage.PEER_MESSAGE_CONFIG_AP:
						if (checkBadCommand()) {
							break;
						}
						
						int configAPResult = -1;
						
						//configs[0]= random code , configs[1]= passphrase 
						final String[] configs = PeerMessage.Resolver.getConfigAPInfo(data);
						Log.i(TAG, "PEER_MESSAGE_CONFIG_AP configs[0]="+configs[0]+", configs[1]="+configs[1]);
						if (configs != null && configs.length > 1) {
							if (randomCode != null && randomCode.equals(configs[0])) {
								
								if (configs[1] != null && !"".equals(configs[1])) {
									
									//send config success message
									configAPResult = 0;
									randomCode = null;
									
									msg = PeerMessage.Builder.buildResponse(respHeader, configAPResult);
									sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
									socket.send(sendPacket);
									
									Thread.sleep(2000);
									
									//start config ap
									//new WifiApManager().configAp(WifiApManager.AP_CONFIG_STATE_NEW, PeerTools.getDeviceID(mContext), WifiApManager.WPA, configs[1], mContext);
								}
								
							}else {
								//random code error
								configAPResult = -2;
							}
							
						}
						
						msg = PeerMessage.Builder.buildResponse(respHeader, configAPResult);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						
						break;
						
						

					case PeerMessage.PEER_MESSAGE_MIRROR_REQUEST:

						 String Var=PeerMessage.Resolver.getResultString(data);

						 if (peerListener != null) {
							 Var=peerListener.onMirrorStart(recvPacket.getAddress().getHostAddress(), Var);
						 }
						 msg = PeerMessage.Builder.buildResponse(respHeader, Var);
						 sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						 socket.send(sendPacket);
						 
						break;
					case PeerMessage.PEER_MESSAGE_MIRROR_END:

						

						 if (peerListener != null) {
							 peerListener.onMirrorStop();
						 }
						 
					     msg = PeerMessage.Builder.buildResponse(respHeader, 1);
						 sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						 socket.send(sendPacket);
						 
					     break;
					case PeerMessage.PEER_MESSAGE_FILE_DIR:

						 String path=PeerMessage.Resolver.getResultString(data);
						 String ret=null;
						 if (peerListener != null) {

						 }						
						
						
					     msg = PeerMessage.Builder.buildResponse(respHeader, ret);
					  
						 sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						 socket.send(sendPacket);

					     break;

					case PeerMessage.PEER_MESSAGE_FRESH_DEVICE_MUSIC:
						
						if (peerListener != null) {
							peerListener.refreshDeviceMusicData(null);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, 1);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						 break;
					case PeerMessage.PEER_MESSAGE_MUSIC_SYNCH_STATUS:
						
						String synchstatus=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							synchstatus=peerListener.getMusicSynchStatus(synchstatus);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, synchstatus);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
					case PeerMessage.PEER_MESSAGE_MUSIC_DELETE:
						
						String cmd=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							peerListener.deleteFileCmd(cmd);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, 1);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
					case PeerMessage.PEER_MESSAGE_MUSIC_PLAY:
						
						String infolist=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							peerListener.startMusicPlayerList(infolist);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, 1);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
						
					case PeerMessage.PEER_MESSAGE_MUSIC_FORM:
						
						String items[]=PeerMessage.Resolver.getResultStringArray(data);
						if (peerListener != null) {
							items=peerListener.getMusicForm(items[0], items[1]);
						}
						msg = PeerMessage.Builder.buildCommand(respHeader, items[0], items[1]);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
						
					case PeerMessage.PEER_MESSAGE_MUSIC_FAVORITES:
						
						String favorites_items[]=PeerMessage.Resolver.getResultStringArray(data);
						String str="";
						if (peerListener != null) {
							str=peerListener.getMusicFavorites(favorites_items[0], favorites_items[1]);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, str);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
					case PeerMessage.PEER_MESSAGE_APP_STATE:
						
						String packageinfo=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							packageinfo=peerListener.getAppInfo(packageinfo);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, packageinfo);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
						
					case PeerMessage.PEER_MESSAGE_TIME_CLOCK:
						
						String timeclock[]=PeerMessage.Resolver.getResultStringArray(data);
						if (peerListener != null) {
							timeclock=peerListener.getTimeClock(timeclock[0], timeclock[1]);
						}
						msg = PeerMessage.Builder.buildCommand(respHeader, timeclock[0], timeclock[1]);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						break;
					case PeerMessage.PEER_MESSAGE_SHUTDOWN:
						
						String t=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							t=peerListener.setShutdown(t);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, 1);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						 break;
					case PeerMessage.PEER_MESSAGE_MUSIC_EQ:
						
						String eq=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							eq=peerListener.onMusicEQ(eq);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, eq);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						break;
						
					case PeerMessage.PEER_MESSAGE_COPY:
				
						int channel=channelMap.size();
						String copyinfo=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							peerListener.copyMusicFile(channel, copyinfo);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, 0);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						channelMap.put(channel, recvPacket.getAddress().getHostAddress() + ":" + recvPacket.getPort());
						break;						
				
					case PeerMessage.PEER_MESSAGE_REGISTER_USER:
						
						String userinfo=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							userinfo=peerListener.registerUser(userinfo);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, userinfo);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						
						break;
					case PeerMessage.PEER_MESSAGE_SYSTEM_UPGRADE:
						
						
						String sys=PeerMessage.Resolver.getResultString(data);
						if (peerListener != null) {
							peerListener.checkSystemUpdate(sys);
						}
						msg = PeerMessage.Builder.buildResponse(respHeader, 1);
						sendPacket = new DatagramPacket(msg, msg.length, recvPacket.getAddress(), recvPacket.getPort());
						socket.send(sendPacket);
						
						
						break;
					default:
						Log.e(TAG, "Unknown UDP frame type!!!");
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();

					//
					// if (socket != null) {
					// socket.close();
					// socket = null;
					// }
					// break;
				}
			}
		}


		
	}
	
	
	


	
	

	public static int getRemoteVolume() {
		
		
	    return  MusicService.instance.getVolume();
		// return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}
	
	public static void setSpeakerVolume(int vol){
		
//		 int max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		 if(vol>max) vol=max;
//		 else if(vol<0) vol=0;
//		 audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
         
		 MusicService.instance.setVolume(vol);
	}
	public static int getVolume() {
		 return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}
	public static void setVolume(int vol){
		 int max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		 if(vol>max) vol=max;
		 else if(vol<0) vol=0;
		 audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
	}
	public static void initS805Vol(){
		
		//Utils.log("initS805Vol"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		if( audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)!=14)			
		    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 14, 0);
		    
		
	}
    
	private int getMuteState() {
		int statusFlag = (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) ? 1: 0; 
		int vv = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if((statusFlag == 1 && vv == 0) ) { 
			return 1;
		}else{
			return 0;
		}
	}

	public boolean checkBadCommand(){
		 
		
		return false;
	}

	@Override
	public int closePeer() {
		
		
		if (socket != null) {
		 socket.close();
		 socket = null;
		}
		return 0;
	}
	
}
