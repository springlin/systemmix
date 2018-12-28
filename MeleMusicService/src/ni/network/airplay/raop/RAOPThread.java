package ni.network.airplay.raop;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import ni.common.util.Log;
import ni.network.airplay.AirplayFactory;
import ni.network.airplay.IAirplayImpl;
import ni.network.util.NetUtils;

/**
 * LaunchThread class which starts services
 * 
 * @author bencall
 */
public class RAOPThread extends Thread {
    private static final String TAG = "AirPlayServer";
	private final IAirplayImpl airplayImpl;
  //  private AudioBonjourEmitter emitter;
    private final String name;
    private volatile boolean stopThread = false;

	private AirPlayServer airPlayServer = null;
    /**
     * Constructor
     * 
     * @param name
     */
    public RAOPThread(final IAirplayImpl aAirplayImpl, final String name) {
        super();
        this.setName("RAOPThread-" + name);
        this.name = name;
        this.airplayImpl = aAirplayImpl;
    }

    private byte[] getHardwareAdress() {
        byte[] hwAddr = null;

        InetAddress local;
        try {
            local = InetAddress.getLocalHost();
            final NetworkInterface ni = NetworkInterface.getByInetAddress(local);
            if (ni != null) {
                final String[] as = airplayImpl.getMacAddress().split(":");
                hwAddr = new byte[as.length];
                int i = 0;
                for (final String a : as) {
                    hwAddr[i++] = Integer.valueOf(a, 16).byteValue();
                }
            }
        }
        catch (final UnknownHostException e) {
            e.printStackTrace();
        }
        catch (final SocketException e) {
            e.printStackTrace();
        }
        return hwAddr;
    }

    private String getStringHardwareAdress(final byte[] hwAddr) {
        final StringBuilder sb = new StringBuilder();
        for (final byte b : hwAddr) {
            sb.append(String.format("%02x", b));
        }
        Log.d("RAOPThread", sb.toString());
        return sb.toString();
    }
    ServerSocket servSock = null;
    Socket socket = null;
    Socket newSocket = null;
    RTSPResponder mRes = null;
    @Override
    public void run() {
        final int port = NetUtils.getPortInRange(AirplayFactory.RAOP_PORT_MIN, AirplayFactory.RAOP_PORT_MAX);
        Log.d("RAOPThread", "Airplay service started: " + port);

        
        try {
//        	try {
//				sleep(5000); //delay for network interface ready
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			} 
        	
            // We listen for new connections
            try {
                servSock = new ServerSocket(port);
            }
            catch (final IOException e) {
                servSock = new ServerSocket();
            }

            // DNS Emitter (Bonjour)
            final byte[] hwAddr = getHardwareAdress();
            //emitter = new AudioBonjourEmitter(name, getStringHardwareAdress(hwAddr), port);

            airPlayServer=AirPlayServer.getIstance();
            airPlayServer.devName=name;
            airPlayServer.setRtspPort(port);
            airPlayServer.run();
            
            if(servSock==null || servSock.isClosed()) return ;
            if(servSock!=null) servSock.setSoTimeout(1000);
            while (!stopThread) {
                try {
                	newSocket = servSock.accept();
                    Log.d("RAOPThread", "qvod raop got RAOP connection from " + newSocket.getInetAddress().getHostAddress());
                    if (null != socket && !socket.isClosed()  ) {
//                    	InetAddress add1=newSocket.getInetAddress(), add2=socket.getInetAddress();
//                    	Log.e(TAG, "add1 "+add1.getHostAddress()+"add2 "+add2.getHostAddress());
////                    	if(!add1.getHostAddress().equals(add2.getHostAddress()))
                    	    stopRTSP();
                    }
                    socket = newSocket;
                    mRes = new RTSPResponder(hwAddr, socket, airplayImpl);
                    mRes.start();
                }
                catch (final SocketTimeoutException e) {
                    // ignore
                }
            }

        }
        catch (final IOException e) {
//            throw new RuntimeException(e);

        } 
        finally {
            try {
                if (servSock != null) {
                    servSock.close(); // will stop all RTSPResponders.
                }
//                if (emitter != null) {
//                    emitter.stop();
//                }
            }
            catch (final IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("RAOPThread", "Airplay thread exit .........");
    }
    public synchronized void stopRTSP() {//guan
    	Log.d(TAG, TAG + " stopRTSP");
        

        if (socket != null) {
        	try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			socket = null;
        }
        if (mRes != null) {
        	mRes.stopThread();
        	try {
				mRes.join(10000);
			} catch (InterruptedException e){
				e.printStackTrace();
			}
        	mRes=null;
        }
    }
    
    
    public synchronized void stopThread() {
    	 Log.d(TAG, TAG + " stopThread");
         if (mRes != null) {
        	mRes.stopThread();
        	mRes=null;
         }
    	  stopThread = true;
          if (servSock != null) {
            try {
				servSock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // will stop all HTTPResponders.
			
			servSock = null;
          }
          if(airPlayServer!=null) airPlayServer.release();
          
          

        
    }
}
