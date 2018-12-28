package com.myun.spring.dlna.api;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.content.Intent;
import android.util.Log;









import com.myun.core.MusicPlayer;
import com.myun.core.MusicService;
import com.myun.spring.dlna.NscreenRendererDevice;
import com.myun.spring.dlna.utils.NscreenConstants;
import com.myun.utils.Utils;

/**
 * 
 * N+ 骞冲彴鎿嶄綔瀹炵幇DLNA鎾斁鎿嶄綔鎺ュ彛
 * 
 * @author 娆ч槼鍗犳煴
 * @date 2012-6-8 涓嬪崍04:17:49
 * 
 */
public class NscreenDLNAMediaPlayAction implements IDlnaMediaPlayAction {
	private final String TAG = "NscreenDLNAMediaPlayAction";
	private Timer timer = null;
	private TimerTask timerTask = null;
	private final int REFRESH_TIME = 1000;
	private Context mContext = null;
	private NscreenRendererDevice mDevice = null;
//	private PeerClientAdapter peerClientAdapter;
	private String cachePlayState = null;
	private boolean initPlay = false;
	public  MusicPlayer musicplayer=null;
    public  BlockingQueue<String> playlist=new LinkedBlockingQueue<String>();
    public  String runState=null;
    
	public NscreenDLNAMediaPlayAction(Context context,
			NscreenRendererDevice device) {
		this.mContext = context;
		this.mDevice = device;
		
	    musicplayer=new MusicPlayer(context, playlist/*, pre_playlist*/);		
		
	    musicplayer.start();
	}

	@Override
	public int dlnaPlay(Intent intent) {
		if(!intent.getAction()
				.equals(NscreenConstants.REMOTE_PLAY_ACTION_PHOTO)){
			stopTimerTask();
		}
		
		int result = -1;
		String _path=intent.getStringExtra(NscreenConstants.REMOTE_SONG_PATH);
		if (isNotEmpty() && _path!=null && _path.length()>0){
		//	result = peerClientAdapter.startApp(intent);
			Utils.sendSpeakerNotify(mContext, "dlnaservice", true);
			Utils.sendSpeakerNotify(mContext, "dlnaservice", false);
		    Log.e("DLNA","DLNA  will path ........."+_path);
		    try {
				playlist.put(_path);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		if (intent.getAction().equals(NscreenConstants.REMOTE_PLAY_ACTION_PHOTO)) {
//			// if (isDeviceNotEmpty()) {
//			// mDevice.setStateVariableAVT(
//			// NscreenRendererDevice.SERVICETYPE_AVT_ID,
//			// "TransportState", "PLAYING");
//			// }
//		} else {
			cachePlayState = null;
			//stopTimerTask();
			initPlay = true;
			startTimerTask();
//		}
		return result;
	}

	@Override
	public int dlnaExit(int exit, String packageName) {
		stopTimerTask();
		cachePlayState = null;
		initPlay = false;
		int result = -1;
		//if (isNotEmpty()) musicplayer.onReset();
			//result = peerClientAdapter.remoteExit(exit, packageName);
		return result;
	}

	@Override
	public int dlnaPause() {
		int result = 0;
		if (isNotEmpty()) {
			musicplayer.onPause();
			if (isDeviceNotEmpty()) {
				mDevice.sendPlayStateLastChange(
						NscreenRendererDevice.SERVICETYPE_AVT_ID,
						"TransportState", "PAUSED_PLAYBACK");
				// mDevice.setTransportActions("Play, Stop, Seek, X_DLNA_SeekTime");
			}
		}
		return result;
	}

	@Override
	public int dlnaResume() {
		Log.d(TAG, ">>dlnaResume");
		int result = 0;
		if (isNotEmpty()) {
			
			Utils.sendSpeakerNotify(mContext, "dlnaservice", true);
			Utils.sendSpeakerNotify(mContext, "dlnaservice", false);
			musicplayer.onStart();
			if (isDeviceNotEmpty()) {
				mDevice.sendPlayStateLastChange(
						NscreenRendererDevice.SERVICETYPE_AVT_ID,
						"TransportState", "PLAYING");
				// mDevice.setTransportActions("Pause, Stop, Seek, X_DLNA_SeekTime");
			}
		}
		return result;
	}

	@Override
	public int dlnaStop(boolean flag) {
		stopTimerTask();
		cachePlayState = null;
		initPlay = false;
		int result = 0;
		if (flag) {
			if (isNotEmpty())
				 musicplayer.onPause();
			if (isDeviceNotEmpty()) {
				mDevice.sendPlayStateLastChange(
						NscreenRendererDevice.SERVICETYPE_AVT_ID,
						"TransportState", "STOPPED");
			}
		}
		return result;
	}

	@Override
	public int dlnaMute(int isMute) {
		int result = -1;
//		if (isNotEmpty())
//			result = peerClientAdapter.remoteMute(isMute);
		return result;
	}

	@Override
	public int dlnaSetVolume(int volume) {
		int result = -1;
//		if (isNotEmpty())
//			result = peerClientAdapter.remoteVolume(volume);
		return result;
	}

	@Override
	public int dlnaSeek(int times) {
		int result = -1;
		if (isNotEmpty())
			 musicplayer.onSeek(times);
		return 0;
	}

	@Override
	public void init() {
		initNscreenAdapter();
	}

	@Override
	public void release() {
		stopTimerTask();
		cachePlayState = null;
		initPlay = false;
		releaseNscreenAdapter();
	}

	@Override
	public int getCurrPositionDlna() {
		
		currPositionDlna=musicplayer.getCurrentPosition();
		return currPositionDlna;
	}

	@Override
	public int getTotalPositionDlna() {
		
		totalPositionDlna=musicplayer.getgetDuration();
		return totalPositionDlna;
	}

	@Override
	public boolean isPlayingDlna() {
		isPlayingDlna=musicplayer.onGetPlayerStatus();
		return isPlayingDlna;
	}

	@Override
	public boolean isDlnaPlayRuning() {
		return isDlnaPlayRuning;
	}

	private void startTimerTask() {
		if (timerTask == null && timer == null) {
			timerTask = new RefreshRmotePlayStateInfo();
			timer = new Timer(true);
			timer.schedule(timerTask, 2 * REFRESH_TIME, REFRESH_TIME);
		}
	}
	
	private void stopTimerTask() {
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		resetData();
	}

	private class RefreshRmotePlayStateInfo extends TimerTask {
		@Override
		public void run() {
			// if (isNotEmpty())
			// peerClientAdapter.getRemoteMediaNotice();
			if (isNotEmpty()){
				String status=musicplayer.getMusicPlayState();
				playerStatus(status);
			}
				
		}
	}

	private void playerStatus(String info){
		
		//Log.i(TAG, "onInfoReceive info = " + info);
		String state=musicplayer.doPlayStateInfo(info, "state");
		
		if(runState==null || !runState.equals(state)){
			runState=state;
			Log.i(TAG, "runState==>"+runState);
			
			if(runState.equals("running")){
				if (isDeviceNotEmpty()) {
						Log.i(TAG,"Music,the N+DMR state is info PLAYING initPlay="+initPlay);
						if(initPlay){
							mDevice.sendPlayLastChange(
									NscreenRendererDevice.SERVICETYPE_AVT_ID,
									"TransportState", "PLAYING");
							initPlay = false;
							Log.i(TAG, "===runState==>"+runState);
						} else {
							if(!"PLAYING".equals(mDevice.getCurrDMRPlayState()))
									mDevice.sendPlayStateLastChange(
											NscreenRendererDevice.SERVICETYPE_AVT_ID,
											"TransportState", "PLAYING");
							
							Log.i(TAG, "11===runState==>"+runState);
						}
						
						cachePlayState = state;
				
			   }
				
			}else if(runState.equals("pause")){
				if (isDeviceNotEmpty()) {
					if(!"PAUSED_PLAYBACK".equals(mDevice.getCurrDMRPlayState()))
							mDevice.sendPlayStateLastChange(
									NscreenRendererDevice.SERVICETYPE_AVT_ID,
									"TransportState",
									"PAUSED_PLAYBACK");
					cachePlayState = state;
					Log.i(TAG,
							"Music,the N+DMR state is info PAUSED_PLAYBACK");
			    } else {
				Log.e(TAG,
						"Music,the N+DMR mDevice is null can not PAUSED_PLAYBACK");
			    }
			}else if(runState.equals("end") || runState.equals("finish")){
				
				if (isDeviceNotEmpty()) {
				mDevice.sendPlayStateLastChange(
						NscreenRendererDevice.SERVICETYPE_AVT_ID,
						"TransportState", "STOPPED");
				currPositionDlna = totalPositionDlna;
				cachePlayState = state;
				Log.i(TAG,
						"Music,the N+DMR state is info STOPPED");
			   } else {
				Log.e(TAG,
						"Music,the N+DMR mDevice is null can not STOPPED");
			   }
				
			}
		}
		
		
	}
	private void resetData() {
		currPositionDlna = 0;
		totalPositionDlna = 0;
		isPlayingDlna = false;
		isDlnaPlayRuning = false;
		cachePlayState = null;
	}

	public void initNscreenAdapter() {
		Log.i(TAG,"initNscreenAdapter create the peerClientAdapter mContext = "
						+ mContext);


//		peerClientAdapter.setMediaNotice2(new IRemoteMediaNotice2() {
//			@Override
//			public void onInfoReceive(int arg0, String playerInfo) {
//				Log.d(TAG, "playerInfo = " + playerInfo.replace("\n", ";"));
//				if (playerInfo != null && !"".equals(playerInfo)) {
//					if (playerInfo.startsWith("END;")) {
//						//resetData();
//						Log.d(TAG, "End stopTimerTask");
//						stopTimerTask();
//					} else {
//						// String[] commons = playerInfo.split("\n");
//						if (NscreenConstants.REMOTE_APP_TYPE_VIDEO
//								.equals(getValue(playerInfo, "type="))) {
//							parseComm(playerInfo);
//							String state = getValue(playerInfo, "state=");
//							if (cachePlayState == null
//									|| !cachePlayState.equals(state)) {
//								if ("playing".equals(state)) {
//									if (isDeviceNotEmpty()) {
//										Log.i(TAG,
//												"Video,the N+DMR state is info PLAYING initPlay="+initPlay);
//										if(initPlay){
//											mDevice.sendPlayLastChange(
//													NscreenRendererDevice.SERVICETYPE_AVT_ID,
//													"TransportState", "PLAYING");
//											initPlay = false;
//										} else {
//											if(!"PLAYING".equals(mDevice.getCurrDMRPlayState()))
//													mDevice.sendPlayStateLastChange(
//															NscreenRendererDevice.SERVICETYPE_AVT_ID,
//															"TransportState", "PLAYING");
//										}
//										
//										cachePlayState = state;
//										
//									} else {
//										Log.e(TAG,
//												"Video,the N+DMR mDevice is null can not PLAYING");
//									}
//								} else if ("paused".equals(state)) {
//									if (isDeviceNotEmpty()) {
//										if(!"PAUSED_PLAYBACK".equals(mDevice.getCurrDMRPlayState()))
//												mDevice.sendPlayStateLastChange(
//														NscreenRendererDevice.SERVICETYPE_AVT_ID,
//														"TransportState",
//														"PAUSED_PLAYBACK");
//										cachePlayState = state;
//										Log.i(TAG,
//												"Video,the N+DMR state is info PAUSED_PLAYBACK");
//									} else {
//										Log.e(TAG,
//												"Video,the N+DMR mDevice is null can not PAUSED_PLAYBACK");
//									}
//								} else if ("finished".equals(state)
//										|| "exit".equals(state)) {
//									if (isDeviceNotEmpty()) {
//										mDevice.sendPlayStateLastChange(
//												NscreenRendererDevice.SERVICETYPE_AVT_ID,
//												"TransportState", "STOPPED");
//										currPositionDlna = totalPositionDlna;
//										cachePlayState = state;
//										Log.i(TAG,
//												"Video,the N+DMR state is info STOPPED");
//									} else {
//										Log.e(TAG,
//												"Video,the N+DMR mDevice is null can not STOPPED");
//									}
//								}
//							}
//						} else if (NscreenConstants.REMOTE_APP_TYPE_MUSIC
//								.equals(getValue(playerInfo, "type="))) {
//							parseComm(playerInfo);
//							String state = getValue(playerInfo, "state=");
//							if (cachePlayState == null
//									|| !cachePlayState.equals(state)) {
//								if ("playing".equals(state)) {
//									if (isDeviceNotEmpty()) {
//										Log.i(TAG,
//												"Music,the N+DMR state is info PLAYING initPlay="+initPlay);
//										if(initPlay){
//											mDevice.sendPlayLastChange(
//													NscreenRendererDevice.SERVICETYPE_AVT_ID,
//													"TransportState", "PLAYING");
//											initPlay = false;
//										} else {
//											if(!"PLAYING".equals(mDevice.getCurrDMRPlayState()))
//													mDevice.sendPlayStateLastChange(
//															NscreenRendererDevice.SERVICETYPE_AVT_ID,
//															"TransportState", "PLAYING");
//										}
//										
//										cachePlayState = state;
//										
//									} else {
//										Log.e(TAG,
//												"Music,the N+DMR mDevice is null can not PLAYING");
//									}
//								} else if ("paused".equals(state)) {
//									if (isDeviceNotEmpty()) {
//										if(!"PAUSED_PLAYBACK".equals(mDevice.getCurrDMRPlayState()))
//												mDevice.sendPlayStateLastChange(
//														NscreenRendererDevice.SERVICETYPE_AVT_ID,
//														"TransportState",
//														"PAUSED_PLAYBACK");
//										cachePlayState = state;
//										Log.i(TAG,
//												"Music,the N+DMR state is info PAUSED_PLAYBACK");
//									} else {
//										Log.e(TAG,
//												"Music,the N+DMR mDevice is null can not PAUSED_PLAYBACK");
//									}
//								} else if ("finished".equals(state)
//										|| "exit".equals(state)) {
//									if (isDeviceNotEmpty()) {
//										mDevice.sendPlayStateLastChange(
//												NscreenRendererDevice.SERVICETYPE_AVT_ID,
//												"TransportState", "STOPPED");
//										currPositionDlna = totalPositionDlna;
//										cachePlayState = state;
//										Log.i(TAG,
//												"Music,the N+DMR state is info STOPPED");
//									} else {
//										Log.e(TAG,
//												"Music,the N+DMR mDevice is null can not STOPPED");
//									}
//								}
//							}
//						}
//					}
//				} else {
//
//				}
//			}
//		});
	}

	public void releaseNscreenAdapter() {
		Log.i(TAG, "releaseNscreenAdapter the peerClientAdapter mContext = "
				+ mContext);
//		if (isNotEmpty()) {
//			peerClientAdapter.release(mContext);
//			peerClientAdapter = null;
//		}
	}

	private boolean isNotEmpty() {
//		if (peerClientAdapter != null)
//			return true;
//		else {
//			Log.e(TAG, "isNotEmpty peerClientAdapter is null and recreated");
//			// 鐩墠杩樻病鏈夋煡鍑轰负浠�箞鏈夋椂涓虹┖鐨勫師鍥狅紝鏆傛椂鐢ㄨ繖绉嶆柟寮忎唬鏇�
//			initNscreenAdapter();
//		}
		return true;
	}

	private boolean isDeviceNotEmpty() {
		if (mDevice != null)
			return true;
		else
			Log.e(TAG, "isDeviceNotEmpty mDevice is null");
		return false;
	}

	private int currPositionDlna = 0;
	private int totalPositionDlna = 0;
	private boolean isPlayingDlna = false;
	private boolean isDlnaPlayRuning = false;

	private void parseCommVideo(String[] commons) {
		totalPositionDlna = Integer.valueOf(commons[0]);
		currPositionDlna = Integer.valueOf(commons[1]);
		isDlnaPlayRuning = Boolean.parseBoolean(commons[2]);
	}

	private void paseCommMusic(String[] commons) {
		totalPositionDlna = Integer.valueOf(commons[0]);
		currPositionDlna = Integer.valueOf(commons[1]);
		isDlnaPlayRuning = Boolean.parseBoolean(commons[2]);
	}

	private void parseComm(String common) {
		totalPositionDlna = Integer.valueOf(getValue(common, "duration="));
		currPositionDlna = Integer.valueOf(getValue(common, "position="));
	}

	private static String getValue(String common, String key) {
		try {
			common = common.substring(common.indexOf(key) + key.length(),
					common.indexOf("\n", common.indexOf(key)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return common;
	}

	@Override
	public void corePause() {
		musicplayer.onPause();
		
	}
}
