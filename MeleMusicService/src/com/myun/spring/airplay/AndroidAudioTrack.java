package com.myun.spring.airplay;

import ni.network.airplay.IAirplayAudioImpl;
import ni.network.airplay.IAirplayImpl;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AndroidAudioTrack implements IAirplayAudioImpl {
    AudioTrack track = null;
    int state = 0; //0:stoped; 1: playing;
    IAirplayImpl aService;
    
    public AndroidAudioTrack(final IAirplayImpl impl) {
		// TODO Auto-generated constructor stub
    	this.aService = impl;
	}


    @Override
    public void audioRelease() {
    	Log.d("", "guan audioRelease");
    	state = 0;
    	if (null != track) {
    		track.release();
    	} else {
    		Log.d("", "guan audioRelease null track");
    	}
    }

    @Override
    public void audioPlay() {    	 
    	Log.d("", "guan audioPlay");
        if (track != null) {
            try {
				track.stop();
				track.release();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        track = new AudioTrack(AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                44100 * 2 * 4,
                AudioTrack.MODE_STREAM);
        track.play();
    }

    @Override
    public void audioStop() {
    	Log.d("", "guan audioStop");
    	state = 0; 
    	if (null == track) {
    		return;
    	}
        try {
			track.stop();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void audioWrite(final short[] data, final int len) {
    	if (state == 0) {
    		//send event
    		Log.d("", "guan audioWrite");
    		aService.showTips();//start play music and show toast
    		state = 1;
    	}
		track.write(data, 0, len);
    }

}
