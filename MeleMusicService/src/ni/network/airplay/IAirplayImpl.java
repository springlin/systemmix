package ni.network.airplay;

/**
 * Must be implemented by the user to provide the services required by the
 * airplay system as-well as receive notifications.
 * 
 * @author Pierre
 */
public interface IAirplayImpl {
    /*
     * Return the device's mac address.
     * @return the mac address in the format "9C:02:98:83:79:EB"
     */
    public String getMacAddress();

    /*
     * Return an Audio implementation.
     */
    public IAirplayAudioImpl getAudio();

    /*
     * Return an Event sink.
     */
    public IAirplayEventSink getEventSink();

	public void stopApp();
	
	public void showTips();
	public void ReStartService();
}
