/**
 * 
 */
package com.mele.musicdaemon;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;





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

	
	private HashMap<Integer, String> channelMap = new HashMap<Integer, String>();

	public PeerServerIP(Context context, String peerName) {
		this.mContext = context;
		localPeerName = peerName;
		
		new Thread(new UDPMessageLooper(), "UDPMessageLooper").start();

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
	public int sendMessageNoWait(byte[] msg, int len) {

		int result = -1;
		try {

				
//				Log.i(TAG, "---------------------------------send Host: " + host + ", Port: " + port);

				DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByName("127.0.0.1"), Peer.SERVER_UDP_PORT);
				socket.send(packet);



		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
    public IPeerListener getPeerListener() {
		return peerListener;
	}

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

		
		@Override
		public void run() {			

			
			byte[] msg;
			byte[] buffer = new byte[2*1024];
			final DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
			
			try {
				socket = new DatagramSocket();
				socket.setBroadcast(true);
				socket.setReuseAddress(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
						 String item[]=PeerMessage.Resolver.getPeerInfo(data).split(";");
						 
						 if(peerListener!=null){
							 peerListener.startMusicService(item[0]+item[1]+item[2]);
						 }
						 //Log.i(TAG, "Send PEER_MESSAGE_FIND_PEER ACK  peerInfo="+item.toString());
						break;
	
					default:
						Log.e(TAG, "Unknown UDP frame type!!!");
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		}


		
	}
	
	
	


	
	


	
}
