/**
 * 
 */
package com.mele.musicdaemon;







public interface Peer {	
	public static final int SERVER_UDP_PORT = 9696;
	
	public int sendMessage(int channel, byte[] msg, int len);
	public int sendMessageNoWait(byte[] msg, int len);	
	public int setListener(IPeerListener listener);

}
