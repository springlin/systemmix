package ni.network.airplay;

import ni.network.plist.NSDictionary;
import ni.types.Vector3f;

/**
 * The Airplay sink should be implemented by the user to be notified of the
 * airplay events.
 * <p>
 * Note that the sink's methods are called from within the AirplayThread.
 * </p>
 * 
 * @author Pierre Renaux
 */
public interface IAirplayEventSink {
    /**
     * New reversed connection started.
     *
     * This event is called when a new reversed connection started. Specifically when a
     * proper reverse connection has been established which denote a full
     * connection with the other airplay side.
     *
     * Note that previous connections are basically never closed properly so
     * there's no reliable way of getting a proper close message.
     */
    public void eventNewReversedConnection(final String aPurpose, final String aSessionId);

    /**
     * A picture has been received and should be displayed.
     * 
     * @param b the buffer containing the JPEG data of the picture
     * @param off the offset of the first byte being the JPEG data in the buffer
     * @param len the length of the JPEG data in the buffer
     */
//    public void eventShowPicture(String assetAction, String sessionId, final String fileName, final byte b[], final int off, final int len);
//    public void eventShowSlideshows(String sessionId, final String fileName, final byte b[], final int off, final int len);
//    public void eventShowPicture(final String path);

    /**
     * A video URL has been received and should start playing.
     * 
     * @param url is the url of the video to play, usually a HTTP url pointing
     *            to a MP4 file.
     * @param startPosPercent is the requested starting position in percent of the
     *            video playback.
     */
    public void eventPlayVideo(String sessionId, String url, float startPosPercent);    

    static final int PLAYBACK_INFO_DURATION = 0;
    static final int PLAYBACK_INFO_POSITION = 1;
    static final int PLAYBACK_INFO_RATE = 2;    
    
    /**
     * Get the playback infos.
     * 
     * See PLAYBACK_INFO_* for which component is what. Can be accessed with the .setAt & .getAt methods. 
     */
    public Vector3f getPlaybackInfo();
    
    /**
     * Retrieve the photo's cache directory.
     * @return The path of the directory where photos are cached, for example: "/mnt/sdcard/com.qvod.nscreen.adapter.media/"
     */
    public String getPhotoOutputDirectory();
    
    /**
     * The airplay thread is notifying our app that its currently loading a photo.
     */
    public void notifyLoadingPhoto();
    
    /**
     * The airplay thread is notifying our app that the photo loading timedout (failed).
     */
    public void notifyLoadingPhotoTimeOut();
    
    /**
     * The video is changed when starting video playback.
     */
    public static final int SET_VIDEO_RATE_REASON_PLAYING = 1;
    /**
     * The video is changed as a result of pausing the video.
     */
    public static final int SET_VIDEO_RATE_REASON_PAUSED = 0;
    /**
     * The video is changed as a result of stopping the video.
     */
    public static final int SET_VIDEO_RATE_REASON_STOPPED = 2;    
    
    /**
     * Change the video playback rate.
     *
     * @param the new playback rate, 0 is paused, 1 is the normal speed.
     */
    public void eventSetVideoRate(String sessionId, float videoRate, int reason);
    
    /**
     * Change the sound volume.
     */
    public void eventSetVolume(String sessionId, float volume);
	
	
	
//    public static final int PLAY_EVENT_PLAY = 0x0001;
//    public static final int PLAY_EVENT_RATE = 0x0002;
//    public static final int PLAY_EVENT_SCRUB = 0x0003;
//    public static final int PLAY_EVENT_STOP = 0x0004;
//    public static final int PLAY_EVENT_VOLUME = 0x0005;
//    
//    public static final int PLAY_STATE_PLAYING = 0x0100;
//    public static final int PLAY_STATE_PAUSED = 0x0101;
//    public static final int PLAY_STATE_STOPED = 0x0102;
    
    
//    public void eventPlayControl(int what, Object obj);
    public void getPlayerInfo();
    public float getPlayDuration();
    public float getPlayPosition();
//    public float getPlayRate();
    
    
//    public float loadingPhoto();
//    public void loadingPhotoTimeOut();
    
    
    
// /**
//     * Change the video playback rate.
//     *
//     * @param the new playback rate, 0 is paused, 1 is the normal speed.
//     */
//    public void eventSetVideoRate(String sessionId, float videoRate);

//    /**
//     * Stop the video playback.
//     */
//    public void eventStopVideo(String sessionId);

    /**
     * Should scrub to the specified position.
     *
     * @param position the video position in seconds.
     *
     * @return false if you don't handle this message
     */
    public boolean eventScrub(String sessionId, float position);

    /**
     * Retrieve the current playback position.
     *
     * @param durationAndPosition, durationAndPosition[0] must be set to the
     *        video's duration, durationAndPosition[1] must be set to the
     *        video's position.
     *
     * @return false if you don't handle this message
     */
//    public boolean eventGetScrub(String sessionId, float[] durationAndPosition);
    /**
     * Get the playback infos for the latest video.
     *
     * (see "GET /playback-info" at http://nto.github.com/AirPlay.html#video-httprequests)
     *
     * duration	                real	playback duration in seconds
     * position	                real	playback position in seconds
     * rate	                    real	playback rate
     * readyToPlay	            boolean	ready to play
     * playbackBufferEmpty	    boolean	buffer empty
     * playbackBufferFull	    boolean	buffer full
     * playbackLikelyToKeepUp	boolean	playback likely to keep up
     * loadedTimeRanges	        array	array of loaded time ranges
     * seekableTimeRanges	    array	array of seekable time ranges
     *
     * @return false if you don't handle this message
     */
    public boolean eventGetPlaybackInfo(String sessionId, NSDictionary dic);
    

}
