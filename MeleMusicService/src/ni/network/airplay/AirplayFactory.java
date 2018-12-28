package ni.network.airplay;

import java.security.InvalidParameterException;


import ni.network.airplay.raop.RAOPThread;

/**
 * This is the main class of the Airplay library, it allows you to configure and
 * create the implemented Airplay services.
 * 
 * @author Pierre
 */
public class AirplayFactory {

    public static final int FEATURE_VIDEO = 1 << 0; // video supported
    public static final int FEATURE_PHOTO = 1 << 1; // photo supported
    public static final int FEATURE_VIDEOFAIRPLAY = 1 << 2; // video protected with FairPlay DRM
    public static final int FEATURE_VIDEOVOLUMECONTROL = 1 << 3; // volume control supported for videos
    public static final int FEATURE_VIDEOHTTPLIVESTREAMS = 1 << 4; // http live streaming supported
    public static final int FEATURE_SLIDESHOW = 1 << 5; // slideshow supported
    public static final int FEATURE_SCREEN = 1 << 7; // mirroring supported
    public static final int FEATURE_SCREENROTATE = 1 << 8; // screen rotation supported
    public static final int FEATURE_AUDIO = 1 << 9; // audio supported
    public static final int FEATURE_AUDIOREDUNDANT = 1 << 11; // audio packet redundancy supported
    public static final int FEATURE_FPSAPV2PT5 = 1 << 12; // AES_GCM	FairPlay secure auth supported
    public static final int FEATURE_PHOTOCACHING = 1 << 13; // photo preloading supported

    /**
     * Airplay server features flags. (default emulates the AppleTV)
     */
     public static final int FEATURES = 0x1000297F;//0x10002900|//ff & ~FEATURE_VIDEO & ~FEATURE_PHOTO & ~FEATURE_SCREEN ;//0x121029FF;////119;
//        FEATURE_VIDEO |
//        FEATURE_PHOTO |
       //  FEATURE_AUDIO ;
//        FEATURE_VIDEOHTTPLIVESTREAMS |
//        FEATURE_SLIDESHOW |
////        FEATURE_SCREEN |
//        FEATURE_VIDEOVOLUMECONTROL;
//    	10751 | ~(FEATURE_SCREEN|FEATURE_PHOTOCACHING|FEATURE_AUDIOREDUNDANT|FEATURE_FPSAPV2PT5);
    /**
     * Airplay server model version. (default emulates the AppleTV)
     */
    public static final String MODEL = "AppleTV3,1";
    
//    public static final String MODEL = "AppleTV2,1";
    /**
     * Airplay server version. (default emulates the AppleTV)
     */
 //  public static final String SRCVERS = "150.33";
 //   public static final String SRCVERS = "101.33";
    public static final String SRCVERS = "104.33";
    /**
     * The minimum port number allowed for the RAOP protocol.
     */
    public static final int RAOP_PORT_MIN = 6201;
    /**
     * The maximum port number allowed for the RAOP protocol.
     */
    public static final int RAOP_PORT_MAX = 6300;
    /**
     * The minimum port number allowed for the Airplay protocol.
     */
    public static final int AIRPLAY_PORT_MIN = 6401;
    /**
     * The maximum port number allowed for the Airplay protocol.
     */
    public static final int AIRPLAY_PORT_MAX = 6500;
    /**
     * The port number allowed for the AirMirror protocol.
     */
    public static final int AIRMIRROR_PORT = 7100;

    /**
     * Starts the RAOP (Remote Audio Output Protocol) service.
     * 
     * @param impl is an implementation of {@link IAirplayImpl} that implements
     *            the required services.
     * @param serviceName is the name of the service as it'll be displayed by
     *            the Bonjour/Zeroconf DNS.
     * @return the {@link RAOPThread} created
     * @throws InvalidParameterException if the implementation is invalid,
     *             doesn't provide an {@link IAirplayEventSink} or doesn't
     *             provide an {@link IAirplayAudioImpl} implementation.
     */
    static public RAOPThread startRAOP(final IAirplayImpl impl, final String serviceName) throws InvalidParameterException
    {
        if (impl == null)
            throw new InvalidParameterException("Implementation must be provided !");
        if (impl.getEventSink() == null)
            throw new InvalidParameterException("Implementation must provide an EventSink implementation !");
        if (impl.getAudio() == null)
            throw new InvalidParameterException("Implementation must provide an Audio implementation !");

        final RAOPThread t = new RAOPThread(impl, serviceName);
        t.start();
        return t;
    }


}
