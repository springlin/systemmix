/**
 * 
 */
package com.myun.net.protocol;







public interface Peer {	
	public static final int SERVER_UDP_PORT = 9696;
	public static final int HTTP_PORT = 9090;
	
	public int sendMessage(int channel, byte[] msg, int len);
	public int sendMessageNoWait(int channel, String data);	
	public int setListener(IPeerListener listener);
    public int closePeer();
   
    
    
}
