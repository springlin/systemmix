package com.myun.net.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import android.content.Intent;
import android.hardware.SensorEvent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public class PeerMessage {
	static final String TAG = "PeerMessage";

	public static String NID_FLAG = "M-";
	
	static final int DEFAULT_PEER_MESSAGE_HEADER_SIZE = 256;
	
	public static final int PEER_MESSAGE_RESPONSE_MASK = 0xFFFF0000;

	public static final int PEER_MESSAGE_KEY_EVENT = 0x8001;
	public static final int PEER_MESSAGE_MOTION_EVENT = 0x8002;
	public static final int PEER_MESSAGE_MOUSE_EVENT = 0x8003;
	public static final int PEER_MESSAGE_SENSOR_EVENT = 0x8004;
	public static final int PEER_MESSAGE_INPUT_EVENT = 0x8005;
	

	public static final int PEER_MESSAGE_FIND_PEER = 0x9001;
	public static final int PEER_MESSAGE_CONNECT = 0x9002;
	public static final int PEER_MESSAGE_START_APP = 0x9003;
	public static final int PEER_MESSAGE_START_APP_PACKAGE = 0x9004;
	public static final int PEER_MESSAGE_SETUP_WIFI = 0x9005;
	public static final int PEER_MESSAGE_INSTALL_APK = 0x9007;
	public static final int PEER_MESSAGE_GET_INSTALL_PROGRESS = 0x9008;
	public static final int PEER_MESSAGE_UNINSTALL_APK = 0x9009;
	public static final int PEER_MESSAGE_GET_APK_VERSION = 0x900B;
	public static final int PEER_MESSAGE_GET_HARDWARE_VERSION = 0x900D;
	public static final int PEER_MESSAGE_GET_FIRMWARE_VERSION = 0x900F;
	public static final int PEER_MESSAGE_GET_IP_ADDRESS = 0x9011;
	public static final int PEER_MESSAGE_GET_NETWORK_AVAIABLE = 0x9015;
	public static final int PEER_MESSAGE_GET_WIFI_MODE = 0x901B;	
	public static final int PEER_MESSAGE_INSTALL_APK_PROGRESS = 0x901D;	
	public static final int PEER_MESSAGE_USER_CMD = 0xF000;
	public static final int PEER_MESSAGE_VCODE = 0xF001;	
	public static final int PEER_MESSAGE_POOL_FULL = 0xF002;	
	public static final int PEER_MESSAGE_HEART_BEAT = 0xF003;

	public static final int PEER_MESSAGE_REMOTE_PLAY = 0xF004;
	public static final int PEER_MESSAGE_REMOTE_NEXT = 0xF005;
	public static final int PEER_MESSAGE_REMOTE_PREVIEUS = 0xF006;
	public static final int PEER_MESSAGE_REMOTE_FORWORD = 0xF007;
	public static final int PEER_MESSAGE_REMOTE_BACK = 0xF008;
	public static final int PEER_MESSAGE_REMOTE_SEEK = 0xF009;
	public static final int PEER_MESSAGE_REMOTE_PAUSE = 0xF00A;
	public static final int PEER_MESSAGE_REMOTE_RESUME = 0xF00B;
	public static final int PEER_MESSAGE_REMOTE_EXIT = 0xF00C;
	public static final int PEER_MESSAGE_REMOTE_STOP = 0xF00D;
	public static final int PEER_MESSAGE_REMOTE_PLAY_INFO = 0xF00E;
	public static final int PEER_MESSAGE_GET_REMOTE_SSID = 0xF010;
	public static final int PEER_MESSAGE_FINISH_APP = 0xF011;
	

	public static final int PEER_MESSAGE_GET_REMOTE_WIFI_CONFIGED = 0xF016;
	public static final int PEER_MESSAGE_REMOTE_VOLUME = 0xF017;
	public static final int PEER_MESSAGE_SCAN_REMOTE_WIFI = 0xF018;
	public static final int PEER_MESSAGE_REMOTE_WIFI_LIST = 0xF019;
	public static final int PEER_MESSAGE_SYSTEM_UPGRADE = 0xF01A;
	
	public static final int PEER_MESSAGE_GET_SOFTWARE_VERSION = 0xF01B;
	public static final int PEER_MESSAGE_REMOTE_MUTE = 0xF01C;
	public static final int PEER_MESSAGE_KILL_APP = 0xF01D;
	public static final int PEER_MESSAGE_GET_REMOTE_VOLUME = 0xF01F;
	
	
	public static final int PEER_MESSAGE_GET_RANDOM_CODE = 0xF020;
	public static final int PEER_MESSAGE_CONFIG_AP = 0xF021;
	public static final int PEER_MESSAGE_CANCEL_CONFIG_AP = 0xF022;
	
	public static final int PEER_MESSAGE_FORGET_WIFI = 0xF023;
	public static final int PEER_MESSAGE_SET_WIFI_ENABLED = 0xF024;
	public static final int PEER_MESSAGE_GET_WIFI_ENABLED = 0xF025;
	public static final int PEER_MESSAGE_REMOTE_ALL_WIFI_LIST = 0xF026;
	
	public static final int PEER_MESSAGE_MIRROR_REQUEST = 0xF029;
	public static final int PEER_MESSAGE_MIRROR_END = 0xF02A;
	public static final int PEER_MESSAGE_FILE_DIR = 0xF02B;
	public static final int PEER_MESSAGE_PLAY_LOCAL_MEDIA = 0xF02C;
	public static final int PEER_MESSAGE_PPT = 0xF02D;
	public static final int PEER_MESSAGE_MIC = 0xF02E;
	public static final int PEER_MESSAGE_MUSIC_LIST = 0xF02F;
	public static final int PEER_MESSAGE_MUSIC_LIST_CLIENT = 0xF030;
	public static final int PEER_MESSAGE_FRESH_DEVICE_MUSIC = 0xF031;
	public static final int PEER_MESSAGE_MUSIC_SYNCH_STATUS = 0xF032;
	public static final int PEER_MESSAGE_MUSIC_DELETE = 0xF033;	
	public static final int PEER_MESSAGE_MUSIC_PLAY = 0xF034;
	public static final int PEER_MESSAGE_PLAY_CONTROL = 0xF035;	
	public static final int PEER_MESSAGE_PLAY_MODE = 0xF036;	
	public static final int PEER_MESSAGE_MUSIC_FAVORITES = 0xF037;	
	public static final int PEER_MESSAGE_MUSIC_FORM = 0xF038;		
	public static final int PEER_MESSAGE_APP_STATE = 0xF039;
	public static final int PEER_MESSAGE_TIME_CLOCK = 0xF03A;
	public static final int PEER_MESSAGE_SHUTDOWN = 0xF03B;	
	public static final int PEER_MESSAGE_DISCONNECT_WIFI = 0xF03C;		
	public static final int PEER_MESSAGE_MUSIC_EQ = 0xF03D;	
	public static final int PEER_MESSAGE_COPY = 0xF03E;	
	public static final int PEER_MESSAGE_REGISTER_USER = 0xF03F;	
	
	// ----------------------------------------------------------
	public static final int PEER_MESSAGE_INPUT_TYPE_KEYCODE = 0;
	public static final int PEER_MESSAGE_INPUT_TYPE_CHARSET = 1;
	
	public static final int PEER_MESSAGE_MOUSE_MOVE_EVENT = 1;
	public static final int PEER_MESSAGE_MOUSE_LKEYUP_EVENT = 2;
	public static final int PEER_MESSAGE_MOUSE_LKEYDOWN_EVENT = 3;
	public static final int PEER_MESSAGE_MOUSE_RKEYUP_EVENT = 4;
	public static final int PEER_MESSAGE_MOUSE_RKEYDOWN_EVENT = 5;
	// ----------------------------------------------------------
	
	
	public static final String MELE_NSCREEN_ACTION_WIFI_STATE_CHANGE = "MELE_NSCREEN_ACTION_WIFI_STATE_CHANGE";
	public static final String MELE_NSCREEN_ACTION_STATE_CHANGE = "MELE.NSCREEN.ACTION.STATE_CHANGE";
	public static final String MELE_NSCREEN_ACTION_SYSTEM_UPGRADE_EVENT = "MELE.NSCREEN.ACTION.SYSTEM_UPGRADE_EVENT";
	public static final String MELE_NSCREEN_ACTION_START_SYSTEM_UPGRADE = "MELE.NSCREEN.ACTION.START_SYSTEM_UPGRADE";
	public static final String MELE_NSCREEN_EXTRA_PEER_STATE = "MELE_NSCREEN_EXTRA_PEER_STATE";
	public static final String MELE_NSCREEN_EXTRA_PEER_INFO = "PEER_INFO";
	public static final String MELE_NSCREEN_EXTRA_ATTACH_PEER_COUNTER = "ATTACH_PEER_COUNTER";
	public static final String MELE_NSCREEN_EXTRA_ATTACH_INFO = "EXTRA_ATTACH_INFO";
	public static final String MELE_NSCREEN_EXTRA_SYSTEM_UPGRADE_VERSION = "SYSTEM_UPGRADE_VERSION";
	/**
	 * 0  : success <br/>
	 * -1 : fail <br/>
	 * -2 : vcode error <br/>
	 * -3 : pool full error
	 */
	public static final String MELE_NSCREEN_EXTRA_RESULT_CODE = "RESULT_CODE";
	
	public static final String MELE_NSCREEN_EXTRA_RESULT_SUCCESS = "0";
	public static final String MELE_NSCREEN_EXTRA_RESULT_FAIL = "-1";
	
	
	
	
	
	public static final String MELE_NSCREEN_PEER_STATE_ATTACH = "PEER_ATTACH";
	public static final String MELE_NSCREEN_PEER_STATE_DETACH = "PEER_DETACH";

		
	public static final String PEER_EXCEPTION_NAME_NOT_FOUND = "NameNotFoundException";

	public static final int VERSION = 0x00020000; //V2.0
	static Random random = new Random(SystemClock.uptimeMillis());
	
	public static class Header {		
		private int sequence;
		private int version;
		private int dataType;
		static Random random = new Random(SystemClock.uptimeMillis());
		static final short uuid = (short)random.nextInt();
		private static int auto_sequence = 0;

		public Header(int dataType) {
			this.sequence = (uuid << 16) | (auto_sequence & 0x0000FFFF);
			auto_sequence++;
			if (auto_sequence > 65535)
				auto_sequence = 0;
			this.version = VERSION;
			this.dataType = dataType;
		}
		
		public Header(int sequence, int version, int dataType) {
			this.sequence = sequence;
			this.version = version;
			this.dataType = dataType;
		}
		
		public int getDataType() {
			return dataType  & 0x0000FFFF;
		}
		
		public int getSequence() {
			return sequence;
		}	
		
		public int getVersion() {
			return version;
		}		
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	
	public static void initBuildHead(Header header, DataOutputStream oos){
		try {
			oos.writeInt(header.getSequence());
			oos.writeInt(header.getVersion());
			oos.writeInt(header.getDataType());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static class Builder {
		public static byte[] build(Header header) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				oos.flush();
				
				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] buildDefault(Header header) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;
			
			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);
				
				// Header
				initBuildHead(header, oos);
				oos.writeInt(0);
				oos.flush();
				
				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}
			
			return result;
		}

		public static byte[] build(Header header, byte[] userdata) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(userdata.length);
				oos.write(userdata);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}

		public static byte[] build(Header header, String peerInfo) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data

				oos.writeInt(peerInfo.getBytes().length);
				oos.write(peerInfo.getBytes());
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		public static byte[] buildCommand(Header header, String var, String cmd) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(var.getBytes().length);
				oos.write(var.getBytes());
				
				oos.writeInt(cmd.getBytes().length);
				oos.write(cmd.getBytes());
				

				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}		
		public static byte[] buildCommand_SetupWiFi(Header header, String ssid, String algorithm, String key) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(ssid.getBytes().length);
				oos.write(ssid.getBytes());
				
				oos.writeInt(algorithm.getBytes().length);
				oos.write(algorithm.getBytes());
				
				oos.writeInt(key.getBytes().length);
				oos.write(key.getBytes());
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		
		public static byte[] buildCommand_QueryWiFiList(Header header, String wifiList) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream();
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data

				oos.writeInt(wifiList.getBytes().length);
				oos.write(wifiList.getBytes());
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		
		public static byte[] buildCommand_QueryWiFiMode(Header header) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(0);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] buildCommand_InstallAPK(Header header, String appURL) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(appURL.getBytes().length);
				oos.write(appURL.getBytes());
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] buildCommand_UninstallAPK(Header header, String appName) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(appName.getBytes().length);
				oos.write(appName.getBytes());
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] buildResponse(Header header, int result) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] resp = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(result);
				oos.flush();

				resp = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return resp;
		}
		
		public static byte[] buildResponse(Header header, String result) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] resp = null;
			result = (result == null) ? result = "" : result;
			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(result.getBytes().length);
				if (result.length() > 0) {
					oos.write(result.getBytes());
				}
				oos.flush();

				resp = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return resp;
		}
		
		
		public static byte[] buildCommand(Header header, int result) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] resp = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(result);
				oos.flush();

				resp = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return resp;
		}
		
		public static byte[] buildCommand_QueryAppVersion(Header header, String appName) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(appName.getBytes().length);
				oos.write(appName.getBytes());
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		public static byte[] buildCommand_attach(Header header, String id, String extra ) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;
			
			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);
				
				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(id.getBytes().length);
				oos.write(id.getBytes());
				oos.writeInt(extra.getBytes().length);
				oos.write(extra.getBytes());
				oos.flush();
				
				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}
			
			return result;
		}
		
		
		public static byte[] buildCommand_show_photo(Header header, int command, String url) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;
			
			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);
				
				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(command);
				oos.writeInt(url.getBytes().length);
				oos.write(url.getBytes());
				oos.flush();
				
				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}
			
			return result;
		}
		
		
		public static byte[] buildCommand_exit(Header header, int code, String info) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;
			
			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);
				
				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(code);
				oos.writeInt(info.getBytes().length);
				oos.write(info.getBytes());
				oos.flush();
				
				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}
			
			return result;
		}
		public static byte[] buildCommand_mute_volume(Header header, int mute, int value) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;
			
			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);
				
				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(mute);
				oos.writeInt(value);
				oos.flush();
				
				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}
			
			return result;
		}
		
		
		
		public static byte[] buildCommand_volume(Header header, int value) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;
			
			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);
				
				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(value);
				oos.flush();
				
				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}
			
			return result;
		}
		
		public static byte[] buildCommand_mute(Header header, int isMute) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;
			
			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);
				
				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(isMute);
				oos.flush();
				
				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}
			
			return result;
		}
		
		
		public static byte[] buildCommand_QueryCurrAppID(Header header) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(0);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] buildCommand_QueryFirmwareVersion(Header header) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(0);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] buildCommand_QueryHardwareVersion(Header header) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(0);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] buildCommand_QueryIP(Header header) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(0);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] buildCommand_QuerySSID(Header header) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				
				//Data
				oos.writeInt(0);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}

		
		
		public static byte[] build(Header header, Intent intent) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);		

				// Data
				oos.writeInt(intent.getAction().getBytes().length);
				oos.write(intent.getAction().getBytes());
				
				Bundle bundle = intent.getExtras();

				if (bundle != null) {
					
					oos.writeInt(intent.getExtras().size());
					Log.d(TAG, "size = " + intent.getExtras().size());
					
					Set<String> set = bundle.keySet();
					for (String s : set) {
						
						Log.d(TAG, "key = " + s);
						Log.d(TAG, "value = " + intent.getStringExtra(s));
						oos.writeInt(s.getBytes().length);
						oos.write(s.getBytes());
						
						oos.writeInt(intent.getStringExtra(s).getBytes().length);
						oos.write(intent.getStringExtra(s).getBytes());
					}
				}else {
					oos.writeInt(0);
				}

				Uri uri = intent.getData();
				if (uri != null) {
					oos.writeInt(1);
					
					String url = uri.toString() == null ? "" : uri.toString();
					oos.writeInt(url.getBytes().length);
					oos.write(url.getBytes());
					
					String type = intent.getType() == null ? "" : intent.getType();
					oos.writeInt(type.getBytes().length);
					oos.write(type.getBytes());
				}else {
					oos.writeInt(0);
				}
				
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}

		public static byte[] build(Header header, KeyEvent e) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);

				// Data
				oos.writeInt(e.getAction());
				oos.writeInt(e.getKeyCode());
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}

		public static byte[] build(Header header, MotionEvent e) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);
				// Data
				oos.writeInt(e.getAction());

				int pointers = e.getPointerCount();

				if (getAndroidSDKVersion() > 8) {
					
					oos.writeInt(e.getPointerCount());
					PointerCoords pointerCoords = new PointerCoords();
					for (int i = 0; i < pointers; i++) {
						e.getPointerCoords(i, pointerCoords);
						oos.writeInt((int) (pointerCoords.x));
						oos.writeInt((int) (pointerCoords.y));
						oos.writeInt((int) (pointerCoords.pressure * 1000));
						oos.writeInt((int) (pointerCoords.orientation));
						oos.writeInt((int) (pointerCoords.touchMajor));
						oos.writeInt((int) (pointerCoords.touchMinor));
						oos.writeInt((int) (pointerCoords.toolMajor));
						oos.writeInt((int) (pointerCoords.toolMinor));
					}
					
				} else {
					oos.writeInt(1);
					oos.writeInt((int) (e.getX()));
					oos.writeInt((int) (e.getY()));
					oos.writeInt((int) (e.getPressure()));
					oos.writeInt(0);
					oos.writeInt(0);
					oos.writeInt(0);
					oos.writeInt(0);
					oos.writeInt(0);
				}

				oos.flush();

				Log.d(TAG, "build MotionEvent Ok dataType = " + header.getDataType());

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}

		public static byte[] build(Header header, SensorEvent e) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);

				// Data
				// oos.writeLong(e.timestamp);
				oos.writeInt(e.sensor.getType());
				oos.writeInt((int)(e.values[0] * 10000));
				oos.writeInt((int)(e.values[1] * 10000));
				oos.writeInt((int)(e.values[2] * 10000));
				oos.writeInt(e.accuracy);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] build(Header header, int code) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);

				// Data
				oos.writeInt(PEER_MESSAGE_INPUT_TYPE_KEYCODE);
				oos.writeInt(code);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] build(Header header, int length, String charset) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);

				// Data
				oos.writeInt(PEER_MESSAGE_INPUT_TYPE_CHARSET);
				oos.writeInt(length);
				oos.writeInt(charset.getBytes().length);
				oos.write(charset.getBytes());
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static byte[] build(Header header, int type, int x, int y) {
			ByteArrayOutputStream bos = null;
			DataOutputStream oos = null;
			byte[] result = null;

			try {
				bos = new ByteArrayOutputStream(DEFAULT_PEER_MESSAGE_HEADER_SIZE);
				oos = new DataOutputStream(bos);

				// Header
				initBuildHead(header, oos);

				// Data
				oos.writeInt(type);
				oos.writeInt(x);
				oos.writeInt(y);
				oos.flush();

				result = bos.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				close_helper(bos, oos);
			}

			return result;
		}
		
		public static void close_helper(ByteArrayOutputStream bos, OutputStream oos) {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	
	public static void initResolverHead(DataInputStream is){
		try {
			is.readInt();
			is.readInt();
			is.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class Resolver {
		public static Header getHeader(byte[] msg) {
			Header header = null;
			DataInputStream is = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				header = new PeerMessage.Header(is.readInt(), is.readInt(), is.readInt());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return header;
		}

		public static Header getHeader(DataInputStream is) throws Exception{
			Header header = null;
			header = new PeerMessage.Header(is.readInt(), is.readInt(), is.readInt());

			return header;
		}
		
		
		/**
		 * PeerInfo = ip,ID,name
		 * @param msg
		 * @return
		 */
		public static String getPeerInfo(byte[] msg){
			DataInputStream is = null;
			String result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				initResolverHead(is);
				
				result = readString(is);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		
		public static int getResultCode(byte[] msg){
			DataInputStream is = null;
			int result = 0;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));

				initResolverHead(is);
				
				result = is.readInt();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		
		public static String getResultString(byte[] msg){
			DataInputStream is = null;
			String result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));

				initResolverHead(is);
				
				
				int n = is.readInt();
				if (n > 0) {
					byte [] buf = new byte[n]; 
					is.readFully(buf);
					result = new String(buf);
				}else {
					result = "";
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		
		public static String[] getResultStringArray(byte[] msg){			
			DataInputStream is = null;
			String[] result = null;
	
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = new String[2];
				result[0] = readString(is);
				result[1] = readString(is);
	
	
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	
			return result;
		}
		public static byte[] getUserData(byte[] msg){
			DataInputStream is = null;
			byte[] buf = null;
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				initResolverHead(is);
				
				int len = is.readInt();
				buf = new byte[len];
				is.readFully(buf);
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return buf;
		}
		
		
		public static String[] getExitInfo(byte[] msg){
			DataInputStream is = null;
			String[] result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = new String[2];
				result[0] = String.valueOf(is.readInt());
				result[1] = readString(is);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		
		
		public static String[] getWifiInfo(byte[] msg) {
			DataInputStream is = null;
			String[] result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = new String[3];
				result[0] = readString(is);
				result[1] = readString(is);
				result[2] = readString(is);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		public static String[] getForgetWifiInfo(byte[] msg) {
			DataInputStream is = null;
			String[] result = null;
			
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = new String[2];
				result[0] = readString(is);
				result[1] = readString(is);
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return result;
		}
		
		public static String getAppURL(byte[] msg) {
			DataInputStream is = null;
			String result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = readString(is);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		
		public static String getAppName(byte[] msg) {
			DataInputStream is = null;
			String result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = readString(is);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		
		public static int getPosition(byte[] msg) {
			DataInputStream is = null;
			int result = -1;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = is.readInt();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		public static int getZoom(byte[] msg) {
			DataInputStream is = null;
			int result = -1;
			
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = is.readInt();
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return result;
		}
		
		
		public static String[] getAttachInfo(byte[] msg) {
			DataInputStream is = null;
			String[] result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);

				// -----------
				result = new String[2];
				result[0] = readString(is);
				result[1] = readString(is);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
            
			return result;
		}
		
		public static int getRotate(byte[] msg) {
			DataInputStream is = null;
			int result = -1;
			
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = is.readInt();
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return result;
		}
		
		public static int getVolume(byte[] msg) {
			DataInputStream is = null;
			int result = -1;
			
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = is.readInt();
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return result;
		}
		
		public static int getMute(byte[] msg) {
			DataInputStream is = null;
			int result = -1;
			
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				result = is.readInt();
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return result;
		}

		
		public static String[] getConfigAPInfo(byte[] msg) {
			DataInputStream is = null;
			String[] result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);

				// -----------
				result = new String[2];
				result[0] = readString(is);
				result[1] = readString(is);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
		
		
		public static String[] getShowPhoto(byte[] msg) {
			DataInputStream is = null;
			String[] result = null;
			result = new String[2];
			try {	
				is = new DataInputStream(new ByteArrayInputStream(msg));
				
				// -----------
				initResolverHead(is);
				
				// -----------
				result[0] = String.valueOf(is.readInt());
				result[1] = readString(is);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return result;
		}
		
		
		public static Intent getIntent(byte [] msg) {
			
			DataInputStream is = null;
			Intent intent = null;
			
			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				initResolverHead(is);
			
				intent = new Intent();
				
				String action = readString(is);
				//Log.d(TAG, "action:" + action);
				intent.setAction(action);
				

				int size = is.readInt();
				for (int i = 0; i < size; i++) {
					String key = readString(is);
					String value = readString(is);

					//Log.i(TAG, "intent key="+key+",value="+value);
					intent.putExtra(key, value);
				}
				
				
				int isDataAndType = is.readInt();
				if (isDataAndType == 1) {
					String uri = readString(is);
					String type = readString(is);
					
					intent.setDataAndType(Uri.parse(uri), type);
					Log.i(TAG, "intent uri="+uri+",type="+type);
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			return intent;
		}
		
		
		public static String[] getMediaInfo(byte[] msg) {
			DataInputStream is = null;
			String[] result = null;

			try {
				is = new DataInputStream(new ByteArrayInputStream(msg));
				// -----------
				initResolverHead(is);
				
				// -----------
				int len = is.readInt();

				if (len > 0) {
					result = new String[len];
					for (int i = 0; i < len; i++) {
						result[i] = readString(is);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}
	}
	
	private static int getAndroidSDKVersion() {
		int version = 8;
		try {
			version = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return version;
	}
	
	public synchronized static String readString(DataInputStream is) throws IOException {
		
		try {
			int n = is.readInt();
			if (n <= 0) {
				return "";
			}
			
			byte[] buf = new byte[n];
			is.readFully(buf);
			
			String result = new String(buf);
			buf = null;
			return result;
		} catch (java.lang.OutOfMemoryError e) {
			Log.e(TAG, "readString error ",e);
			return "";
		}
	}	
	
	@Deprecated
	public static String[] getWifiInfo(DataInputStream is) {
		String[] result = null;

		try {								
			result = new String[3];
			result[0] = readString(is);
			result[1] = readString(is);
			result[2] = readString(is);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	@Deprecated
	public static String getAppName(DataInputStream is) {
		String appName = null;

		try {
			appName = readString(is);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return appName;
	}
	@Deprecated
	public static Intent getIntent(DataInputStream is) {
		Intent intent = null;

		try {
			intent = new Intent();
			
			String action = readString(is);
			Log.d(TAG, "action:" + action);
			intent.setAction(action);

			int size = is.readInt();
			for (int i = 0; i < size; i++) {
				String key = readString(is);
				String value = readString(is);

				Log.i(TAG, "intent key="+key+",value="+value);
				intent.putExtra(key, value);
			}
			
			
			int isDataAndType = is.readInt();
			if (isDataAndType == 1) {
				String uri = readString(is);
				String type = readString(is);
				
				intent.setDataAndType(Uri.parse(uri), type);
				Log.i(TAG, "intent uri="+uri+",type="+type);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return intent;
	}
	
	
}
