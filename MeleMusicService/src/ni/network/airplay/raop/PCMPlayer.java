package ni.network.airplay.raop;

import ni.common.util.Log;
import ni.network.airplay.IAirplayAudioImpl;

/**
 * Will create a new thread and play packets added to the ring buffer and set as
 * ready
 * 
 * @author bencall
 */
public class PCMPlayer extends Thread {
    private final IAirplayAudioImpl audioImpl;
    private final AudioSession session;
    private volatile long fix_volume = 0x10000;
    private short rand_a, rand_b;
    private final AudioBuffer audioBuf;
    private boolean stopThread = false;

    public PCMPlayer(final AudioSession session, final AudioBuffer audioBuf, final IAirplayAudioImpl aAudioImpl) {
        super();
        this.setName("PCMPlayerThread");
        this.session = session;
        this.audioBuf = audioBuf;
        this.audioImpl = aAudioImpl;
        audioImpl.audioPlay();
    }

    @Override
    public void run() {
        boolean fin = stopThread;
        int[] outbuf = null;
        short[] input = null;

        while (!fin) {
            final int[] buf = audioBuf.getNextFrame();
            
            // Stop
            synchronized (this) {
                Thread.yield();
                fin = this.stopThread;
                if (fin) {
                	Log.d("", "guan PCMPlayer thread exit");
                	break;
                }
            }
            if (buf == null) {
                continue;
            }

            if (outbuf == null || outbuf.length < session.OUTFRAME_BYTES()) {
                outbuf = new int[session.OUTFRAME_BYTES()];
            }
            final int k = stuff_buffer(session.getFilter().bf_playback_rate, buf, outbuf);

            if (input == null || input.length < outbuf.length) {
                input = new short[outbuf.length];
            }

            for (int i = 0; i < outbuf.length; i++) {
                input[i] = (short)outbuf[i];
            }

            audioImpl.audioWrite(input, k * 2);

        }

        audioImpl.audioStop();
        audioImpl.audioRelease();
    }

    public synchronized void stopThread() {
        this.stopThread = true;
    }

    private int stuff_buffer(final double playback_rate, final int[] input, final int[] output)
    {
        int stuffsamp = session.getFrameSize();
        int stuff = 0;
        double p_stuff;

        p_stuff = 1.0 - Math.pow(1.0 - Math.abs(playback_rate - 1.0), session.getFrameSize());

        if (Math.random() < p_stuff) {
            stuff = playback_rate > 1.0 ? -1 : 1;
            stuffsamp = (int)(Math.random() * (session.getFrameSize() - 2));
        }

        int j = 0;
        int l = 0;
        for (int i = 0; i < stuffsamp; i++) { // the whole frame, if no stuffing
            output[j++] = dithered_vol(input[l++]);
            output[j++] = dithered_vol(input[l++]);
        }

        if (stuff != 0) {
            if (stuff == 1) {
                // interpolate one sample
                output[j++] = dithered_vol((input[l - 2] + input[l]) >> 1);
                output[j++] = dithered_vol((input[l - 1] + input[l + 1]) >> 1);
            }
            else if (stuff == -1) {
                l -= 2;
            }
            for (int i = stuffsamp; i < session.getFrameSize() + stuff; i++) {
                output[j++] = dithered_vol(input[l++]);
                output[j++] = dithered_vol(input[l++]);
            }
        }
        return session.getFrameSize() + stuff;
    }

    public void setVolume(final double vol) {
        fix_volume = (long)vol;
        Log.d("PCMPlayer", "setVolume:" + vol);
    }

    private short dithered_vol(final int sample) {
        long out;
        rand_b = rand_a;
        rand_a = (short)(Math.random() * 65535);

        out = sample * fix_volume;
        if (fix_volume < 0x10000) {
            out += rand_a;
            out -= rand_b;
        }
        return (short)(out >> 16);
    }
}
