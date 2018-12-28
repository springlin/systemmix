package com.myun.spring.dlna.api;

import android.content.Context;
import android.util.Log;

import com.myun.spring.dlna.NscreenRendererDevice;
import com.myun.spring.dlna.api.IDlnaMediaPlayAction;
import com.myun.spring.dlna.api.NscreenDLNAMediaPlayAction;

public class DMRFactory {
    private static final String TAG = "DMRFactory";

    public static int PLAYER_NSCREEN = 0X1;

    public static IDlnaMediaPlayAction getInstance(int type, Context context,
            NscreenRendererDevice device) {
        Log.d(TAG, "getInstance type:" + type);
        synchronized (DMRFactory.class) {
            if (type == PLAYER_NSCREEN) {
                return new NscreenDLNAMediaPlayAction(context, device);
            }
            return null;
        }
    }
}
