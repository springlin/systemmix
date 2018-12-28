package ni.network.airplay.raop;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.util.Log;




/**
 * Android AirPlay Server Implementation
 * 
 * @author Rafael Almeida
 *
 */
public class AirPlayServer  {

	private static final Logger LOG = Logger.getLogger(AirPlayServer.class.getName());
	public static String TAG="AirPlayServer";
	/**
	 * The AirTunes/RAOP service type
	 */
	static final String AIR_TUNES_SERVICE_TYPE = "_raop._tcp.local.";
	
	
	public String devName="";
	/**
	 * The AirTunes/RAOP M-DNS service properties (TXT record)
	 */
	static final Map<String, String> AIRTUNES_SERVICE_PROPERTIES = map(
		"txtvers", "1",
		"tp", "UDP",
		"ch", "2",
		"ss", "16",
		"sr", "44100",
		"pw", "false",
		"sm", "false",
		"sv", "false",
		"ek", "1",
		"et", "0,1",
		"cn", "0,1",
		"vn", "3"
	);
	
	private static AirPlayServer instance = null;
	public static AirPlayServer getIstance(){
		if(instance == null){
			instance = new AirPlayServer();
		}
		return instance;
	}
	

	/**
	 * JmDNS instances (one per IP address). Used to unregister the mDNS services
	 * during shutdown.
	 */
	protected List<JmDNS> jmDNSInstances;
	
	/**
	 * The AirTunes/RAOP RTSP port
	 */
	private int rtspPort = 5000; //default value
	
	private AirPlayServer(){
		//create executor service
//		executorService = Executors.newCachedThreadPool();
//		
//		//create channel execution handler
//		channelExecutionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(4, 0, 0));
//	
//		//channel group
//		channelGroup = new DefaultChannelGroup();
		
		//list of mDNS services
		jmDNSInstances = new java.util.LinkedList<JmDNS>();
	}

	public int getRtspPort() {
		return rtspPort;
	}

	public void setRtspPort(int rtspPort) {
		this.rtspPort = rtspPort;
	}

	public void run() {
		
		startService();
	}

	private void startService() {
		/* Make sure AirPlay Server shuts down gracefully */
    	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				onShutdown();
			}
    	}));
    	
    	LOG.info("VM Shutdown Hook added sucessfully!");
    	

		
        LOG.info("Launched RTSP service on port " + getRtspPort());

        //get Network details
        NetworkUtils networkUtils = NetworkUtils.getInstance();
        
        String hostName = networkUtils.getHostUtils();//devName;//
		String hardwareAddressString = networkUtils.getHardwareAddressString();
        Log.e(TAG, "hostName "+hostName+" hardwareAddressString  "+hardwareAddressString);
		try {
	    	/* Create mDNS responders. */
	        synchronized(jmDNSInstances) {
		    	for(final NetworkInterface iface: Collections.list(NetworkInterface.getNetworkInterfaces())) {
		    		if ( iface.isLoopback() ){
		    			continue;
		    		}
		    		if ( iface.isPointToPoint()){
		    			continue;
		    		}
		    		if ( ! iface.isUp()){
		    			continue;
		    		}
	
//		    		ArrayList<InetAddress> list=Collections.list(iface.getInetAddresses());
//		    		if(list.size()<2)
//						try {
//							list.add(InetAddress.getByName("localhost"));
//						} catch (UnknownHostException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
		    		
		    		
		    		for(final InetAddress addr: Collections.list(iface.getInetAddresses())) {
		    			if ( ! (addr instanceof Inet4Address)  ){
		    				continue;
		    			}
	
						try {
							/* Create mDNS responder for address */
							Log.e(TAG, "addr ...."+addr.toString());
					    	final JmDNS jmDNS = JmDNS.create(addr, hostName + "-jmdns");
					    	jmDNSInstances.add(jmDNS);
	
					        /* Publish RAOP service */
					        final ServiceInfo airTunesServiceInfo = ServiceInfo.create(
					    		AIR_TUNES_SERVICE_TYPE,
					    		hardwareAddressString + "@" + devName+"("+"ÒôÏì"+")",
					    		getRtspPort(),
					    		0 /* weight */, 0 /* priority */,
					    		AIRTUNES_SERVICE_PROPERTIES
					    	);
					        jmDNS.registerService(airTunesServiceInfo);
							LOG.info("Registered AirTunes service '" + airTunesServiceInfo.getName() + "' on " + addr);
						}
						catch (final Throwable e) {
							LOG.log(Level.SEVERE, "Failed to publish service on " + addr, e);
						}
		    		}
		    	}
	        }
	        
		} 
		catch (SocketException e) {
			LOG.log(Level.SEVERE, "Failed register mDNS services", e);
		}
	}

	//When the app is shutdown
	public void onShutdown() {
		/* Close channels */
	//	final ChannelGroupFuture allChannelsClosed = channelGroup.close();

		/* Stop all mDNS responders */
		synchronized(jmDNSInstances) {
			for(final JmDNS jmDNS: jmDNSInstances) {
				try {
					jmDNS.unregisterAllServices();
					//LOG.info("Unregistered all services on " + jmDNS.getInterface());
				}
//				catch (final IOException e) {
//					LOG.log(Level.WARNING, "Failed to unregister some services", e);
//					
//				}
				catch (NullPointerException e){
					LOG.log(Level.WARNING, "null NullPointerException", e);
				}
			}
		}

         
		
	}
    public void release(){
    	
    	onShutdown();
    	instance = null;
    }
	/**
	 * Map factory. Creates a Map from a list of keys and values
	 * 
	 * @param keys_values key1, value1, key2, value2, ...
	 * @return a map mapping key1 to value1, key2 to value2, ...
	 */
	private static Map<String, String> map(final String... keys_values) {
		assert keys_values.length % 2 == 0;
		final Map<String, String> map = new java.util.HashMap<String, String>(keys_values.length / 2);
		for(int i=0; i < keys_values.length; i+=2)
			map.put(keys_values[i], keys_values[i+1]);
		return Collections.unmodifiableMap(map);
	}



}
