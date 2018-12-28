package ni.network.airplay;

public interface IAirplayAudioImpl {
    /**
     * Called when the audio player is done with the audio.
     */
    public void audioRelease();

    /**
     * Called when the audio buffer should start playing.
     */
    public void audioPlay();

    /**
     * Called when the audio buffer should stop playing.
     */
    public void audioStop();

    /**
     * Write audio data to the audio buffer. The data format is stereo, PCM
     * 16bit, 44100Hz.
     * 
     * @param data is the 16bit audio data to write
     * @param len is the length of the audio data to write
     */
    public void audioWrite(short[] data, int len);
}
