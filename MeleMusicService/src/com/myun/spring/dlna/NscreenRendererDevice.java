package com.myun.spring.dlna;

import java.io.IOException;
import java.io.StringReader;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceStateTable;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.xml.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.myun.core.R;
import com.myun.spring.dlna.api.DMRFactory;
import com.myun.spring.dlna.api.IDlnaMediaPlayAction;
import com.myun.spring.dlna.bean.MediaItem;
import com.myun.spring.dlna.utils.DIDL_XMLHandler;
import com.myun.spring.dlna.utils.MediaFormatUtils;
import com.myun.spring.dlna.utils.NscreenConstants;
import com.myun.spring.dlna.utils.Utils;


public class NscreenRendererDevice extends Device implements ActionListener,
		QueryListener {

	private final String TAG = "NscreenRendererDevice";
	public static final String SERVICETYPE_AVT_TYPE = "urn:schemas-upnp-org:service:AVTransport:1";
	public static final String SERVICETYPE_CM_TYPE = "urn:schemas-upnp-org:service:ConnectionManager:1";
	public static final String SERVICETYPE_RC_TYPE = "urn:schemas-upnp-org:service:RenderingControl:1";

	public static final String SERVICETYPE_AVT_ID = "urn:upnp-org:serviceId:AVTransport";
	public static final String SERVICETYPE_CM_ID = "urn:upnp-org:serviceId:ConnectionManager";
	public static final String SERVICETYPE_RC_ID = "urn:upnp-org:serviceId:RenderingControl";
	public final  int DEFAULT_HTTP_PORT = 0x4B64;
	

	private Context mContext = null;
	public IDlnaMediaPlayAction playAction = null;
	private AudioManager audioManager = null;

	private final static String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
	private final static String NOT_IMPLEMENTED_I4 = "2147483647";// means
																	// NOT_IMPLEMENTED
	private final static String DEFAULT_TIME = "00:00:00";
	private Intent intent = null;
	private boolean isInit = false;
	private boolean isStop = true;
	private String currentURI = "";
	private String currentURIMetaData = "";
	private String protocolInfo = "";
	private String currMediaDuration = DEFAULT_TIME;

	private String unknowSongName = null;
	private String unknowArtistName = null;
	private String unknowAlbumName = null;

	private String currDMRPlayState = "NO_MEDIA_PRESENT";

	// 琛ㄧず鏈夎繘琛孲top鐨勬搷浣滐紝灏变笉瑕佽繘琛屽叾浠栫殑娓呯悊宸ヤ綔
	private boolean isStopAction = false;
	// 褰撳墠鎾斁濯掍綋鐨勭被鍨�
	// private String currPlayMediaType = null;
	// 姝ｅ湪鎾斁鐨勫獟浣撹矾寰�
	private String currPlayMediaUrl = null;
	private int volumePercent = 0;
	private int MAX_VOLUME = 0;
	
	private Timer mTimer = null;
	private long mLastNotifyTime = 0L;
	private long mMaxEventRate = 200L;
	private NotifyTask mNotifyTask = null;
	private ServiceStateTable mEventNotifyList = new ServiceStateTable();
	private int cacheSeekTime = 0;
	
	public String getCurrPlayMediaUrl() {
		return currPlayMediaUrl;
	}

	// public String getCurrPlayMediaType() {
	// return currPlayMediaType;
	// }

	public void setCurrDMRPlayState(String state) {
		currDMRPlayState = state;
	}
	
	public String getCurrDMRPlayState() {
		return currDMRPlayState;
	}

	public NscreenRendererDevice(Context context) {
		super();
		this.mContext = context;
	}

	public NscreenRendererDevice(String descriptionFileName, Context context)
			throws InvalidDescriptionException {
		super(descriptionFileName);
		setHTTPPort(DEFAULT_HTTP_PORT);
		setNMPRMode(true);
		this.mContext = context;
		mTimer = new Timer("DMR notify event Timer");
		audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		unknowSongName = mContext.getString(R.string.unknown_song_name);
		unknowArtistName = mContext.getString(R.string.unknown_artist_name);
		unknowAlbumName = mContext.getString(R.string.unknown_album_name);
		protocolInfo = mContext.getString(R.string.sinkProtocolInfo)
				.replaceAll(" ", "");
		Log.d(TAG, "######create DMR descriptionFileName:"
				+ descriptionFileName);
		Log.d(TAG, this.getDeviceType());
		Log.d(TAG, ((Service) this.getServiceList().get(0)).getControlURL());
		Log.d(TAG, ((Service) this.getServiceList().get(0)).getDescriptionURL());
		Log.d(TAG, ((Service) this.getServiceList().get(0)).getEventSubURL());
		Log.d(TAG, ((Service) this.getServiceList().get(0)).getSCPDURL());
		Log.d(TAG, ((Service) this.getServiceList().get(0)).getActionList()
				.size() + "");
		Log.d(TAG, ((Service) this.getServiceList().get(1)).getControlURL());
		Log.d(TAG, ((Service) this.getServiceList().get(1)).getDescriptionURL());
		Log.d(TAG, ((Service) this.getServiceList().get(1)).getEventSubURL());
		Log.d(TAG, ((Service) this.getServiceList().get(1)).getSCPDURL());
		Log.d(TAG, ((Service) this.getServiceList().get(1)).getActionList()
				.size() + "");
		Log.d(TAG, ((Service) this.getServiceList().get(2)).getControlURL());
		Log.d(TAG, ((Service) this.getServiceList().get(2)).getDescriptionURL());
		Log.d(TAG, ((Service) this.getServiceList().get(2)).getEventSubURL());
		Log.d(TAG, ((Service) this.getServiceList().get(2)).getSCPDURL());
		Log.d(TAG, ((Service) this.getServiceList().get(2)).getActionList()
				.size() + "");

		setWirelessMode(true);
		getService(SERVICETYPE_CM_ID)
    		.getStateVariable("SourceProtocolInfo").setValue("");
		getService(SERVICETYPE_CM_ID)
	    	.getStateVariable("SinkProtocolInfo").setValue(protocolInfo);
		getService(SERVICETYPE_CM_ID)
			.getStateVariable("CurrentConnectionIDs").setValue("0");
		this.getService(SERVICETYPE_AVT_ID)
				.getStateVariable("A_ARG_TYPE_InstanceID").setValue("0");
		this.getService(SERVICETYPE_RC_ID)
				.getStateVariable("A_ARG_TYPE_InstanceID").setValue("0");

		//this.getService(SERVICETYPE_AVT_ID).getStateVariable("LastChange")
		//		.setValue("");
		//this.getService(SERVICETYPE_RC_ID).getStateVariable("LastChange")
		//		.setValue("");
		setCurrDMRPlayState("NO_MEDIA_PRESENT");
		this.getService(SERVICETYPE_AVT_ID).getStateVariable("TransportState")
				.setValue("NO_MEDIA_PRESENT");
		this.getService(SERVICETYPE_AVT_ID).getStateVariable("TransportStatus")
				.setValue("OK");
		this.getService(SERVICETYPE_AVT_ID)
				.getStateVariable("TransportPlaySpeed").setValue("1");
		this.getService(SERVICETYPE_AVT_ID).getStateVariable("CurrentTrack")
				.setValue("0");
		this.getService(SERVICETYPE_AVT_ID).getStateVariable("NumberOfTracks")
				.setValue("0");
		// setTransportActions("");
		this.getService(SERVICETYPE_AVT_ID)
				.getStateVariable("CurrentRecordQualityMode")
				.setValue(NOT_IMPLEMENTED);
		this.getService(SERVICETYPE_AVT_ID)
				.getStateVariable("NextAVTransportURI")
				.setValue(NOT_IMPLEMENTED);
		this.getService(SERVICETYPE_AVT_ID)
				.getStateVariable("NextAVTransportURIMetaData")
				.setValue(NOT_IMPLEMENTED);
		this.getService(SERVICETYPE_RC_ID).getStateVariable("Mute")
				.setValue(getStreamMute() ? "1" : "0");
		initLastChange();
		setActionListener(this);
		setQueryListener(this);

		playAction = DMRFactory.getInstance(DMRFactory.PLAYER_NSCREEN, context,
				this);
		playAction.init();
		Log.d(TAG, "######create DMR success");
	}

	@Override
	public boolean queryControlReceived(StateVariable stateVariable) {
		// 鎺ユ敹鏉ヨ嚜鎺у埗鐐规煡璇㈡帶鍒剁殑浜嬩欢
		Log.d(TAG,
				"queryControlReceived name:" + stateVariable.getName()
						+ ";DataType:" + stateVariable.getDataType()
						+ ";DefaultValue:" + stateVariable.getDefaultValue()
						+ ";Value:" + stateVariable.getValue());

		return false;
	}
	
	@Override
	public boolean actionControlReceived(Action action) {
		// 鎺ユ敹浠庢帶鍒剁偣鐨勬搷浣滄帶鍒剁殑浜嬩欢
		if (action.getService().getServiceType().equals(SERVICETYPE_CM_TYPE)) {
			if (action.getName().equals("GetProtocolInfo")) {
				Log.d(TAG, "GetProtocolInfo protocolInfo");
				// 璁剧疆濯掍綋鎺ユ敹鍗忚
				action.getArgument("Source").setValue("");
				action.getArgument("Sink").setValue(protocolInfo);
			} else if (action.getName().equals("GetCurrentConnectionIDs")) {
				Log.d(TAG,
						"GetCurrentConnectionIDs ConnectionIDs:"
								+ action.getArgumentValue("ConnectionIDs"));
			} else if (action.getName().equals("GetCurrentConnectionInfo")) {
				Log.d(TAG,
						"GetCurrentConnectionInfo ConnectionID:"
								+ action.getArgumentValue("ConnectionID"));
				action.getArgument("RcsID").setValue(-1);
				action.getArgument("AVTransportID").setValue(-1);
				action.getArgument("ProtocolInfo").setValue("");
				action.getArgument("PeerConnectionManager").setValue("");
				action.getArgument("PeerConnectionID").setValue(-1);
				action.getArgument("Direction").setValue("Input");
				action.getArgument("Status").setValue("Unknown");
			}
			return true;
		} else if (action.getService().getServiceType()
				.equals(SERVICETYPE_RC_TYPE)) {
			// 闊抽噺鎺у埗
			if (action.getName().equals("SetMute")) {
				String channel = action.getArgumentValue("Channel");
				String desiredMute = action.getArgumentValue("DesiredMute");
				if (null == desiredMute || "".equals(desiredMute)) {
					desiredMute = "0";
				}
				int result = -1;
				int temp = Integer.valueOf(desiredMute);
				if (isNotEmpty()) {
					result = playAction.dlnaMute(temp);
				}

				if (result == 0) {
					boolean isMute = false;
					if (audioManager != null) {
						isMute = getStreamMute();
						desiredMute = isMute ? "1" : "0";
					}
					Log.d(TAG, "SetMute channel:" + channel + ";DesiredMute:"
							+ desiredMute + ";result:" + result + ";isMute:"
							+ isMute + ";temp:" + temp);
					action.getService().getStateVariable("Mute")
							.setValue(desiredMute);
					sendSoundLastChange(action,"Mute");
				}
			} else if (action.getName().equals("GetMute")) {
				String currMute = "0";
				boolean isMute = false;
				if (audioManager != null) {
					isMute = getStreamMute();
					currMute = isMute ? "1" : "0";
				}
				action.getArgument("CurrentMute").setValue(
						String.valueOf(currMute));
				Log.d(TAG, "GetMute currMute:" + currMute + ";isMute:" + isMute);
			} else if (action.getName().equals("SetVolume")) {
				String channel = action.getArgumentValue("Channel");
				String desiredVolume = action.getArgumentValue("DesiredVolume");
				Log.d(TAG, "SetVolume Channel:" + channel + ";DesiredVolume:"
						+ desiredVolume);
				volumePercent = Integer.parseInt(desiredVolume);
				if(volumePercent > 100)
					volumePercent = 100;
				
				if (audioManager != null) {
					int volume = (int) Math.ceil(((volumePercent / 100.f) * MAX_VOLUME));
					if (volume > MAX_VOLUME)
						volume = MAX_VOLUME;
					if (isNotEmpty())
						playAction.dlnaSetVolume(volume);
					String desiredMute = action.getService().getStateVariable("Mute").getValue();
					Log.d(TAG, "SetVolume volumePercent:" + volumePercent + ";volume:"+ volume
							+";desiredMute="+desiredMute);
					action.getService().getStateVariable("Volume").setValue(volumePercent);
					sendSoundLastChange(action,"Volume");
					if(volume > 0 && "1".equals(desiredMute))
						sendSoundLastChange(action,"Mute");
				}
			} else if (action.getName().equals("GetVolume")) {
				if (audioManager != null) {
					if(isInit)
						volumePercent = getVolumePercent();
					action.getArgument("CurrentVolume").setValue(
							String.valueOf(volumePercent));
					Log.d(TAG, "GetVolume volume:" + volumePercent);
				}
			}
			return true;
		} else if (action.getService().getServiceType()
				.equals(SERVICETYPE_AVT_TYPE)) {
			if (action.getName().equals("SetAVTransportURI")) {
				// 璁剧疆褰撳墠鎾斁鐨勮矾寰勪俊鎭�
				int InstanceID = action.getArgumentIntegerValue("InstanceID");

				currentURI = action.getArgumentValue("CurrentURI");

				Log.d(TAG, "SetAVTransportURI InstanceID:" + InstanceID
						+ ";CurrentURI=" + currentURI);

				currentURIMetaData = action
						.getArgumentValue("CurrentURIMetaData");

				// 鍏堟鏌ュ湴鍧�俊鎭槸鍚﹀畬濂�
				boolean hasCurrURI = true;
				boolean hasCurrURIMetaData = true;
				if (currentURI == null || currentURI.equals(""))
					hasCurrURI = false;
				if (currentURIMetaData == null || currentURIMetaData.equals(""))
					hasCurrURIMetaData = false;

				Log.d(TAG, "SetAVTransportURI hasCurrURI:" + hasCurrURI
						+ ";hasCurrURIMetaData:" + hasCurrURIMetaData+"============="+currentURIMetaData);

				if (!hasCurrURI && !hasCurrURIMetaData)
					return false;

				if (hasCurrURIMetaData) {
					/** Handling XML */
					SAXParserFactory spf = null;
					SAXParser sp = null;
					DIDL_XMLHandler myXMLHandler = null;
					XMLReader xr = null;
					InputSource is = null;
					try {
						spf = SAXParserFactory.newInstance();
						sp = spf.newSAXParser();
						xr = sp.getXMLReader();
						myXMLHandler = new DIDL_XMLHandler();
						xr.setContentHandler(myXMLHandler);
						// 瑙ｅ喅鑵捐瑙嗛PAD鏈変簺鍦板潃娌℃湁鐗堟湰瑙ｆ瀽鐨勯棶棰�
						String xmlTemp = currentURIMetaData.replaceAll("&",
								"&amp;");
						xmlTemp=new String(xmlTemp.getBytes(), "utf-8");
						is = new InputSource(new StringReader(xmlTemp));
						xr.parse(is);
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (is != null)
							is = null;
						xr = null;
						sp = null;
						spf = null;
					}

					intent = null;
					isInit = true;
					isStop = true;
					cacheSeekTime = 0;
					volumePercent = getVolumePercent();
					
					String resurl = null;
					String duration = null;

					if (myXMLHandler.AudioItemList.size() > 0 || (currentURI != null && !currentURI.equals(""))) {
						
						if(myXMLHandler.AudioItemList.size() > 0){
								MediaItem media = myXMLHandler.AudioItemList
										.get(myXMLHandler.AudioItemList.firstKey());
		
								ListIterator<TreeMap<String, String>> lires = media
										.getResList().listIterator();
								if (lires.hasNext()) {
									TreeMap<String, String> resdata = lires.next();
									resurl = resdata.get("res");
									duration = resdata.get("duration");
									if (duration != null) {
										currMediaDuration = duration;
										action.getService()
												.getStateVariable(
														"CurrentTrackDuration")
												.setValue(duration);
									}
								} else {
									Log.e(TAG, "AudioItemList is null");
									setCurrDMRPlayState("STOPPED");
									sendPlayStateLastChange(action, "TransportState",
											"STOPPED");
									return true;
								}

								// 寮�惎闊充箰鎾斁搴旂敤

						}else{
							resurl=currentURI;
						}
						intent = new Intent(
								NscreenConstants.REMOTE_PLAY_ACTION_AUDIO);
						intent.putExtra(NscreenConstants.REMOTE_SONG_PATH,
								resurl);


					}else {
						Log.e(TAG, "no mediaItem found");
						return true;
					}
				} else {
					intent = null;
					isInit = true;
					isStop = true;
					cacheSeekTime = 0;
					volumePercent = getVolumePercent();
					// 杩囨护甯︽湁鍚庣紑鐨勫湴鍧�
					int mediaFormat = MediaFormatUtils
							.getMediaFormat(currentURI);
					// 濡傛灉娌℃湁甯︽湁鍚庣紑锛屽垯璇锋眰褰撳墠鐨勫湴鍧�殑HTTP澶撮儴淇℃伅
					if (mediaFormat == MediaFormatUtils.UNKOWN)
						mediaFormat = MediaFormatUtils
								.getMediaFormatByContentType(currentURI);
					Log.d(TAG, "SetAVTransportURI mediaFormat:" + mediaFormat);
					String resurl = currentURI;

					if (mediaFormat == MediaFormatUtils.VIDIO
							|| mediaFormat == MediaFormatUtils.LIVE
							|| mediaFormat == MediaFormatUtils.UNKOWN) {
						intent = new Intent(
								NscreenConstants.REMOTE_PLAY_ACTION_VIDEO);
						intent.putExtra(NscreenConstants.REMOTE_VIDEO_PATH,
								Utils.urlEncode(resurl));
						intent.putExtra(
								NscreenConstants.REMOTE_PLAY_TYPE,
								String.valueOf(NscreenConstants.REMOTE_PLAYTYPE_HTTP));
						intent.putExtra(NscreenConstants.REMOTE_VIDEO_NAME,
								Utils.getLiveName(mContext, resurl));
						intent.putExtra(NscreenConstants.REMOTE_VIDEO_POSITION,
								String.valueOf(0));
					} else if (mediaFormat == MediaFormatUtils.AUDIO) {
						intent = new Intent(
								NscreenConstants.REMOTE_PLAY_ACTION_AUDIO);
						intent.putExtra(NscreenConstants.REMOTE_SONG_PATH,
								Utils.urlEncode(resurl));
						intent.putExtra(NscreenConstants.REMOTE_LYRIC_PATH, "");
						intent.putExtra(NscreenConstants.REMOTE_THUMB_PATH, "");
						intent.putExtra(NscreenConstants.REMOTE_SONG_NAME,
								unknowSongName);
						intent.putExtra(NscreenConstants.REMOTE_SONG_TAG_NAME,
								unknowSongName);
						intent.putExtra(
								NscreenConstants.REMOTE_SONG_TAG_ARTISTS,
								unknowArtistName);
						intent.putExtra(NscreenConstants.REMOTE_SONG_TAG_ALBUM,
								unknowAlbumName);

						intent.putExtra(NscreenConstants.REMOTE_SONG_POSITION,
								String.valueOf(0));
					} else if (mediaFormat == MediaFormatUtils.PICTURE) {
						StringBuffer url = new StringBuffer();
						url.append(NscreenConstants.MOVE_TO_CURRENT);
						url.append(mContext.getString(R.string.photo_flag));
						url.append(Utils.urlEncode(resurl));

						intent = new Intent(
								NscreenConstants.REMOTE_PLAY_ACTION_PHOTO);
						intent.putExtra(NscreenConstants.REMOTE_PIC_PATH,
								url.toString());
						intent.putExtra(NscreenConstants.REMOTE_PIC_CMD, String
								.valueOf(NscreenConstants.MOVE_TO_CURRENT));
					}
				}

				if (isNotEmpty()) {
					isStopAction = false;
					currPlayMediaUrl = null;
					if (intent.getAction().equals(NscreenConstants.REMOTE_PLAY_ACTION_PHOTO)) {
						        currPlayMediaUrl = intent.getStringExtra(
								NscreenConstants.REMOTE_PIC_PATH).split(
								mContext.getString(R.string.photo_flag))[1];
					}

					setCurrDMRPlayState("STOPPED");
					action.getService().getStateVariable("TransportState")
							.setValue("STOPPED");
					// setTransportActions("Stop");
					action.getService().getStateVariable("CurrentTrack")
							.setValue("1");
					action.getService().getStateVariable("NumberOfTracks")
							.setValue("1");
					intent.putExtra("REMOTE_PLAY_FROM", "PROTOCOL_DLNA");
					intent.putExtra("SRC_PLATFORM", "android");
					intent.putExtra("SRC_PROTOCOL", "dlna");
					intent.putExtra("SRC_APP", "dlna");
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					sendAVTransportURILastChange(action);
					//playAction.dlnaPlay(intent);
				}
				return true;
			} else if (action.getName().equals("Play")) {
				isStopAction = false;
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				String Speed = action.getArgumentValue("Speed");
				Log.d(TAG, "Play InstanceID:" + InstanceID + ";Speed:" + Speed
						+ ";isInit:" + isInit + ";isStop:" + isStop);
				action.getArgument("Speed").getRelatedStateVariable()
						.setValue(Speed);
				// 鎾斁鎿嶄綔
				if (!isInit) {
					if (isNotEmpty()) {
						playAction.dlnaResume();
					}
				} else {
					isInit = false;
					if (isStop && intent != null && isNotEmpty()) {
						isStop = false;
						//setCurrDMRPlayState("TRANSITIONING");
						//action.getService().getStateVariable("TransportState")
						//		.setValue("TRANSITIONING");
						// setTransportActions("Stop");
						//sendPlayStateLastChange(action, "TransportState", "TRANSITIONING");
						playAction.dlnaPlay(intent);
					}
				}

				return true;
			} else if (action.getName().equals("Pause")) {
				// 鏆傚仠鎿嶄綔
				if (isNotEmpty())
					playAction.dlnaPause();
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				Log.d(TAG, "Pause InstanceID:" + InstanceID);
				return true;
			} else if (action.getName().equals("Stop")) {
				isStopAction = true;
				// 鍏堥�鐭ユ挱鏀惧櫒锛岃繘琛屽仠姝㈡搷浣�
				if (isNotEmpty())
					playAction.dlnaStop(true);
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				Log.d(TAG, "Stop InstanceID:" + InstanceID);
				// 閫氱煡DLNA鏀瑰彉鐘舵�
				resetData();
				//setCurrDMRPlayState("STOPPED");
				//action.getService().getStateVariable("TransportState")
				//		.setValue("STOPPED");
				return true;
			} else if (action.getName().equals("Seek")) {
				// 蹇繘
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				String target = action.getArgumentValue("Target");
				int times = (int) Utils.fromHMS2Millis(target);
				Log.d(TAG, "Seek InstanceID:" + InstanceID + ";target:"
						+ target + ";times:" + times);
				if (isNotEmpty() && cacheSeekTime != times){
					playAction.dlnaSeek(times);
					cacheSeekTime = times;
				}
				return true;
			} else if (action.getName().equals("GetTransportInfo")) {
				// 鑾峰彇褰撳墠鐨勮繍琛岀姸鎬�
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				// String currentTransportState = action
				// .getArgument("CurrentTransportState")
				// .getRelatedStateVariable().getValue();
				String currentTransportStatus = action
						.getArgument("CurrentTransportStatus")
						.getRelatedStateVariable().getValue();
				String currentSpeed = action.getArgument("CurrentSpeed") 
						.getRelatedStateVariable().getValue();

				Log.d(TAG, "GetTransportInfo InstanceID:" + InstanceID
						+ ";CurrentTransportState=" + currDMRPlayState
						+ ";CurrentTransportStatus=" + currentTransportStatus
						+ ";CurrentSpeed=" + currentSpeed);

				action.getArgument("CurrentTransportState").setValue(currDMRPlayState);
				action.getArgument("CurrentTransportStatus").setValue("OK");
				action.getArgument("CurrentSpeed").setValue("1");
				// currentTransportState = null;
				currentTransportStatus = null;
				currentSpeed = null;
				return true;
			} else if (action.getName().equals("GetPositionInfo")) {
				// 鑾峰彇褰撳墠杩涘害淇℃伅
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				// Track: return current track
				String currentTrack = action.getArgument("Track")
						.getRelatedStateVariable().getValue();
				action.getArgument("Track").setValue("1");

				// TrackDuration
				String trackDuration = DEFAULT_TIME;
				int totalPosition = 0;
				if (isNotEmpty()) {
					totalPosition = playAction.getTotalPositionDlna();
					if (totalPosition > 0)
						trackDuration = Utils.DisplayProgress(totalPosition);
				}

				action.getArgument("TrackDuration").setValue(trackDuration);
				action.getArgument("TrackMetaData").setValue(currentURIMetaData);
				action.getArgument("TrackURI").setValue(currentURI);

				String RelTime = DEFAULT_TIME;
				if (isNotEmpty()) {
					int currPosition = playAction.getCurrPositionDlna();
					if (currPosition > 0)
						RelTime = Utils.DisplayProgress(currPosition);
				}
				
				// RelTime =
				action.getArgument("RelTime").setValue(RelTime);
				// AbsTime = NOT_IMPLEMENTED
				action.getArgument("AbsTime").setValue(RelTime);
				// RelCount = NOT_IMPLEMENTED_I4
				action.getArgument("RelCount").setValue(NOT_IMPLEMENTED_I4);
				// AbsCount = NOT_IMPLEMENTED_I4
				action.getArgument("AbsCount").setValue(NOT_IMPLEMENTED_I4);
				Log.d(TAG, "GetPositionInfo InstanceID:" + InstanceID
						+ ";CurrentTrack=" + currentTrack + ";TrackDuration="
						+ trackDuration + ";RelTime=" + RelTime);
				currentTrack = null;
				//trackDuration = null;
				return true;
			} else if (action.getName().equals("GetMediaInfo")) {
				// 鑾峰彇褰撳墠濯掍綋淇℃伅
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				Log.d(TAG, "GetMediaInfo InstanceID:" + InstanceID);
				action.getArgument("NrTracks").setValue("1");
				String trackDuration = DEFAULT_TIME;
				int totalPosition = 0;
				if (isNotEmpty()) {
					totalPosition = playAction.getTotalPositionDlna();
					if (totalPosition > 0)
						trackDuration = Utils.DisplayProgress(totalPosition);
				}

				action.getArgument("MediaDuration").setValue(trackDuration);
				action.getArgument("CurrentURI").setValue(currentURI);
				action.getArgument("CurrentURIMetaData").setValue(
						currentURIMetaData);
				action.getArgument("NextURI").setValue(NOT_IMPLEMENTED);
				action.getArgument("NextURIMetaData").setValue(NOT_IMPLEMENTED);
				action.getArgument("PlayMedium").setValue("NONE");
				action.getArgument("RecordMedium").setValue(NOT_IMPLEMENTED);
				action.getArgument("WriteStatus").setValue(NOT_IMPLEMENTED);
				//trackDuration = null;
				return true;
			} else if (action.getName().equals("GetDeviceCapabilities")) {
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				action.getArgument("PlayMedia").setValue("NONE,NETWORK");
				action.getArgument("RecMedia").setValue(NOT_IMPLEMENTED);
				action.getArgument("RecQualityModes").setValue(NOT_IMPLEMENTED);
				Log.d(TAG, "GetDeviceCapabilities InstanceID:" + InstanceID);
				return true;
			} else if (action.getName().equals("GetCurrentTransportActions")) {
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				action.getArgument("Actions").setValue(
						"Pause,Play,Stop,Seek,X_DLNA_SeekTime");
				Log.d(TAG, "GetCurrentTransportActions InstanceID:" + InstanceID);
				return true;
			} else if (action.getName().equals("GetTransportSettings")) {
				int InstanceID = action.getArgumentIntegerValue("InstanceID");
				action.getArgument("PlayMode").setValue("NORMAL");
				action.getArgument("RecQualityMode").setValue(NOT_IMPLEMENTED);
				Log.d(TAG, "GetTransportSettings InstanceID:" + InstanceID);
				return true;
			}
		}
		return true;
	}

	
	public void sendPlayStateLastChange(String ServiceId, String varname,
			String varvalue) {
		Log.d(TAG, "sendPlayStateLastChange varvalue:" + varvalue);
		setCurrDMRPlayState(varvalue);
		getService(ServiceId).getStateVariable(varname).setValue(varvalue);
		addToNotifyList(getService(ServiceId).getStateVariable(varname));
		addTransportActions(getService(ServiceId),varvalue);
		sendNotifyLastChange(getService(ServiceId));
	}

	private void sendPlayStateLastChange(Action action, String varname,
			String varvalue) {
		Log.d(TAG, "Overloaded sendPlayStateLastChange varvalue:" + varvalue);
		action.getService().getStateVariable(varname).setValue(varvalue);
		addToNotifyList(action.getService().getStateVariable(varname));
		addTransportActions(action.getService(),varvalue);
		sendNotifyLastChange(action.getService());
	}
	
	private void addTransportActions(Service service,String varvalue){
		String actions = "";
		if("PLAYING".equals(varvalue)){
			actions = "Pause, Stop, Seek, X_DLNA_SeekTime";
		} else if ("PAUSED_PLAYBACK".equals(varvalue)){
			actions = "Play, Stop, Seek, X_DLNA_SeekTime";
		} else if ("TRANSITIONING".equals(varvalue)){
			actions = "Stop";
		} else if ("STOPPED".equals(varvalue)){
			actions = "Play";
		} else if ("NO_MEDIA_PRESENT".equals(varvalue)){
			actions = "Play, Stop";
		}
		Log.d(TAG, "addTransportActions actions:" + actions);
		service.getStateVariable("CurrentTransportActions").setValue(actions);
		addToNotifyList(service.getStateVariable("CurrentTransportActions"));
	}
	
	public void sendPlayLastChange(String ServiceId, String varname,
			String varvalue) {
		Log.d(TAG, "sendPlayLastChange varvalue:" + varvalue);
		String trackDuration = DEFAULT_TIME;
		int totalPosition = 0;
		if (isNotEmpty()) {
			totalPosition = playAction.getTotalPositionDlna();
			if (totalPosition > 0)
				trackDuration = Utils.DisplayProgress(totalPosition);
		}
		if(!DEFAULT_TIME.equals(trackDuration))
			currMediaDuration = trackDuration;
		setCurrDMRPlayState(varvalue);
		getService(ServiceId).getStateVariable(varname).setValue(varvalue);
		addToNotifyList(getService(ServiceId).getStateVariable(varname));
		getService(ServiceId).getStateVariable("CurrentTrackDuration").setValue(currMediaDuration);
		addToNotifyList(getService(ServiceId).getStateVariable("CurrentTrackDuration"));
		getService(ServiceId).getStateVariable("CurrentMediaDuration").setValue(currMediaDuration);
		addToNotifyList(getService(ServiceId).getStateVariable("CurrentMediaDuration"));
		getService(ServiceId).getStateVariable("CurrentTransportActions").setValue("Pause,Stop,Seek,X_DLNA_SeekTime");
		addToNotifyList(getService(ServiceId).getStateVariable("CurrentTransportActions"));
		notifyLastChange(getService(ServiceId),null);
	}
	
	private void sendAVTransportURILastChange(Action action){
		Log.d(TAG, "sendAVTransportURILastChange ");
		addToNotifyList(action.getService().getStateVariable("TransportState"));
		action.getService().getStateVariable("NumberOfTracks").setValue("1");
		addToNotifyList(action.getService().getStateVariable("NumberOfTracks"));
		action.getService().getStateVariable("CurrentTrack").setValue("1");
		addToNotifyList(action.getService().getStateVariable("CurrentTrack"));
		action.getService().getStateVariable("CurrentTrackDuration").setValue(currMediaDuration);
		addToNotifyList(action.getService().getStateVariable("CurrentTrackDuration"));
		action.getService().getStateVariable("CurrentMediaDuration").setValue(currMediaDuration);
		addToNotifyList(action.getService().getStateVariable("CurrentMediaDuration"));
		action.getService().getStateVariable("AVTransportURI").setValue(currentURI);
		addToNotifyList(action.getService().getStateVariable("AVTransportURI"));
		action.getService().getStateVariable("AVTransportURIMetaData").setValue(currentURIMetaData);
		addToNotifyList(action.getService().getStateVariable("AVTransportURIMetaData"));
		action.getService().getStateVariable("CurrentTrackURI").setValue(currentURI);
		addToNotifyList(action.getService().getStateVariable("CurrentTrackURI"));
		action.getService().getStateVariable("CurrentTrackMetaData").setValue(currentURIMetaData);
		addToNotifyList(action.getService().getStateVariable("CurrentTrackMetaData"));
		action.getService().getStateVariable("CurrentTransportActions").setValue("Play,Stop,Seek,X_DLNA_SeekTime");
		addToNotifyList(action.getService().getStateVariable("CurrentTransportActions"));
		
		notifyLastChange(action.getService(),null);
	}
	
	private void sendSoundLastChange(Action action, String varname){
		Log.d(TAG, "sendSoundLastChange varname="+varname);
		notifyLastChange(action.getService(),varname);
	}
	
	public void stopDev() {
		resetData();
		sendPlayStateLastChange(SERVICETYPE_AVT_ID, "TransportState", "STOPPED");
		if (isNotEmpty())
			playAction.release();
		stopNotifyTimer();
		this.stop();
	}
    
	private boolean isNotEmpty() {
		if (playAction != null)
			return true;
		else
			Log.e(TAG, "isNotEmpty playAction is null");
		return false;
	}

	private void resetData() {
		resetInitVar();
		resetDmrData();
	}
	
	public void setInitVar(){
		isInit = false;
		isStop = false;
	}
	
	private void resetInitVar(){
		isInit = true;
		isStop = true;
		cacheSeekTime = 0;
		volumePercent = getVolumePercent();
	}

	private void resetDmrData(){
		new Thread() {
			public void run() {
				getService(SERVICETYPE_AVT_ID).getStateVariable("AVTransportURI")
					.setValue("");
				getService(SERVICETYPE_AVT_ID)
					.getStateVariable("AVTransportURIMetaData").setValue("");
				getService(SERVICETYPE_AVT_ID)
					.getStateVariable("CurrentMediaDuration")
					.setValue(DEFAULT_TIME);
				getService(SERVICETYPE_AVT_ID)
					.getStateVariable("CurrentTrackDuration")
					.setValue(DEFAULT_TIME);
				getService(SERVICETYPE_AVT_ID)
					.getStateVariable("RelativeTimePosition")
					.setValue(DEFAULT_TIME);
				getService(SERVICETYPE_AVT_ID)
					.getStateVariable("AbsoluteTimePosition")
					.setValue(DEFAULT_TIME);
				getService(SERVICETYPE_AVT_ID)
					.getStateVariable("NextAVTransportURI")
					.setValue(NOT_IMPLEMENTED);
				getService(SERVICETYPE_AVT_ID)
					.getStateVariable("NextAVTransportURIMetaData")
					.setValue(NOT_IMPLEMENTED);
				getService(SERVICETYPE_AVT_ID).getStateVariable("CurrentTrack")
					.setValue("0");
				getService(SERVICETYPE_AVT_ID).getStateVariable("NumberOfTracks")
					.setValue("0");
				initLastChange();
			};
		}.start();
	}
	// public void setTransportActions(String action) {
	// this.getService(SERVICETYPE_AVT_ID)
	// .getStateVariable("CurrentTransportActions").setValue(action);
	// }

	public void stopTimerTask() {
		if (isStopAction) {
			Log.d(TAG, "stopTimerTask we have cleared,so cancel");
			return;
		}
		resetInitVar();
		sendPlayStateLastChange(NscreenRendererDevice.SERVICETYPE_AVT_ID,
				"TransportState", "STOPPED");
		resetDmrData();
		Log.d(TAG, "stopTimerTask and reset.");
	}

	/**
	 * 鍙栧緱褰撳墠鏄惁闈欓煶
	 * 
	 * @return true 闈欓煶 false 闈為潤闊�
	 */
	private boolean getStreamMute() {
		if (audioManager != null) {
			int statusFlag = (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) ? 1
					: 0;
			int volume = audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			Log.d(TAG, "statusFlag = " + statusFlag + "   volumeValue = "
					+ volume);
			if ((statusFlag == 1 && volume == 0)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	private int getVolumePercent(){
		int volume = 0;
		int percent = 0;
		if (audioManager != null) {
			volume = audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
			percent = (int)(volume / (MAX_VOLUME * 1.f) * 100);
		}
		
		Log.d(TAG, "getVolumePercent volume= " + volume + " ;percent = "
				+ percent);
		return percent;
	}
	
	private void sendNotifyLastChange(Service service) {
		notifyLastChange(service,null);
	}
	
	private void notifyLastChange(Service service,String param){
		long l1 = System.currentTimeMillis() - mLastNotifyTime;
	    long l2 = 0L;
	    if (l1 < mMaxEventRate)
	        l2 = mMaxEventRate - l1;
	    mTimer.purge();
	    mNotifyTask = new NotifyTask(service,param);
	    
	    if(null != param && "Mute".equals(param)) {
	    	//Due to call dlnaMute (), remoteMute () for asynchronous, 
			//if immediately call getStreamMute () may lead to obtain value is not correct, 
			//so dormancy 100 ms 
	    	mTimer.schedule(mNotifyTask, 100);
	    } else {
	    	mTimer.schedule(mNotifyTask, l2);
	    }
	    
	}
	
	private class NotifyTask extends TimerTask {
		private Service service;
		private String param;
		public NotifyTask(Service service,String param) {
			this.service = service;
			this.param = param;
		}
		
	    public void run() {
	    	if(param == null) {
	    		sendNotifyNow(service);
	    	} else {
	    		mLastNotifyTime = System.currentTimeMillis();
	    		service.getStateVariable("LastChange").setValue(createSoundEvent(param));
	    	}
	    }
	}
	
	private void sendNotifyNow(Service service) {
		if(mEventNotifyList.size() <= 0) {
			Log.d(TAG, "sendNotifyNow mEventNotifyList size=0,so cancel.");
			return;
		}
		ServiceStateTable localStateVariableList = (ServiceStateTable)this.mEventNotifyList.clone();
	    this.mEventNotifyList.removeAllElements();
		String serviceId = service.getServiceID();
		String xmlns = "";
		if(serviceId.equals(NscreenRendererDevice.SERVICETYPE_AVT_ID)) {
			xmlns = "urn:schemas-upnp-org:event-1-0/AVT/";
		} else if(serviceId.equals(NscreenRendererDevice.SERVICETYPE_CM_ID)) {
			xmlns = "urn:schemas-upnp-org:event-1-0/CMS/";
		} else if(serviceId.equals(NscreenRendererDevice.SERVICETYPE_RC_ID)) {
			xmlns = "urn:schemas-upnp-org:event-1-0/RCS/";
		}
		
		mLastNotifyTime = System.currentTimeMillis();
		service.getStateVariable("LastChange").setValue(createLastChangeValue(localStateVariableList,xmlns));
	}
	
	private void stopNotifyTimer() {
	    mTimer.cancel();
	    mTimer.purge();
	    mTimer = null;
	}
	
	private void addToNotifyList(StateVariable paramStateVariable){
		if(!isExists(paramStateVariable))
			mEventNotifyList.add(paramStateVariable);
	}
	
	private boolean isExists(StateVariable paramStateVariable){
		if(mEventNotifyList != null) {
			for (int i = 0; i < mEventNotifyList.size(); i++) {
				StateVariable stateVariable = (StateVariable)mEventNotifyList.get(i);
				if(stateVariable.getName().equals(paramStateVariable.getName())
						&& stateVariable.getValue().equals(paramStateVariable.getValue())){
					Log.d(TAG, "isExists name="+paramStateVariable.getName()+";value="+paramStateVariable.getValue()
							+"; has Existed!");
					return true;
				}
			}
		}
		return false;
	}
	
	private String createLastChangeValue(ServiceStateTable paramStateVariableList,String xmlns){
		Node localNode1 = new Node("Event");
	    localNode1.setAttribute("xmlns", xmlns);
	    Node localNode2 = new Node("InstanceID");
	    localNode2.setAttribute("val", "0");
	    localNode1.addNode(localNode2);
	    for (int i = 0; i < paramStateVariableList.size(); ++i)
	    {
	      StateVariable localStateVariable = (StateVariable)paramStateVariableList.get(i);
	      Node localNode3 = new Node(localStateVariable.getName());
	      localNode3.setAttribute("val", localStateVariable.getValue());
	      localNode2.addNode(localNode3);
	    }
	    //Log.d(TAG, "createLastChangeValue localNode1="+localNode1.toString());
	    return localNode1.toString();
	}
	
	private String createSoundEvent(String varname) {
		String varvalue = "";
		if("Mute".equals(varname)) {
			boolean isMute = false;
			if (audioManager != null) {
				isMute = getStreamMute();
				varvalue = isMute ? "1" : "0";
			}
		} else if("Volume".equals(varname)){
			varvalue = String.valueOf(volumePercent);
		}
		this.getService(SERVICETYPE_RC_ID).getStateVariable(varname).setValue(varvalue);
		String event = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\"><InstanceID val=\"0\"><"
  				 + varname + " Channel=\"Master\" val=\"" + varvalue + "\" /></InstanceID></Event>";
		//Log.d(TAG, "createSoundEvent event="+event);
		return event;
	}
	
	private void initLastChange(){
		StringBuffer avtLastChange = new StringBuffer();
		avtLastChange.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">");
		avtLastChange.append("<InstanceID val=\"0\"><CurrentPlayMode val=\"NORMAL\"/>");
		avtLastChange.append("<RecordStorageMedium val=\"NOT_IMPLEMENTED\"/><CurrentTrackURI val=\"\"/>");
		avtLastChange.append("<CurrentTrackDuration val=\"00:00:00\"/><CurrentMediaDuration val=\"00:00:00\"/>");
		avtLastChange.append("<CurrentRecordQualityMode val=\"NOT_IMPLEMENTED\"/><AVTransportURI val=\"\"/>");
		avtLastChange.append("<TransportState val=\"NO_MEDIA_PRESENT\"/><CurrentTrackMetaData val=\"\"/>");
		avtLastChange.append("<NextAVTransportURI val=\"\"/><PossibleRecordQualityModes val=\"NOT_IMPLEMENTED\"/>");
		avtLastChange.append("<CurrentTrack val=\"0\"/><NextAVTransportURIMetaData val=\"NOT_IMPLEMENTED\"/>");
		avtLastChange.append("<PlaybackStorageMedium val=\"NONE\"/><CurrentTransportActions val=\"\"/>");
		avtLastChange.append("<RecordMediumWriteStatus val=\"NOT_IMPLEMENTED\"/><AVTransportURIMetaData val=\"\"/>");
		avtLastChange.append("<PossiblePlaybackStorageMedia val=\"NONE,NETWORK\"/><NumberOfTracks val=\"0\"/>");
		avtLastChange.append("<PossibleRecordStorageMedia val=\"NOT_IMPLEMENTED\"/><TransportStatus val=\"OK\"/>");
		avtLastChange.append("<TransportPlaySpeed val=\"1\"/></InstanceID></Event>");
		//Log.d(TAG, "resetLastChange avtLastChange = "+avtLastChange.toString());
		getService(SERVICETYPE_AVT_ID).getStateVariable("LastChange").setValue(avtLastChange.toString());
		
		StringBuffer rcLastChange = new StringBuffer();
		rcLastChange.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">");
		rcLastChange.append("<InstanceID val=\"0\"><Volume Channel=\"Master\" val=\"");
		rcLastChange.append(getVolumePercent());
		rcLastChange.append("\"/><Mute Channel=\"Master\" val=\"");
		rcLastChange.append(getStreamMute() ? "1" : "0");
		rcLastChange.append("\"/><PresetNameList val=\"FactoryDefaults\"/></InstanceID></Event>");
		//Log.d(TAG, "resetLastChange rcLastChange = "+rcLastChange.toString());
		getService(SERVICETYPE_RC_ID).getStateVariable("LastChange").setValue(rcLastChange.toString());
	}
}
