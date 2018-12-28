package com.mele.musicdaemon;

/**
 * Created by 盛山 on 2015/4/20.
 */

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MeleAp {
    public static String TAG="MeleAp";
    public static String SOCKET_NAME="meleap";
    public static String CMD_START="start";
    public static String CMD_STOP="stop";
    byte buf[] = new byte[1024];
    public LocalSocket connect(){
        LocalSocket localSocket=new LocalSocket();
        LocalSocketAddress address=new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED);
        try {
            localSocket.connect(address);
            return  localSocket;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean writeCommand(LocalSocket socket,String _cmd) {
        byte[] cmd = _cmd.getBytes();
        OutputStream outputStream= null;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        int len = cmd.length;
        if ((len < 1) || (len > 1024))
            return false;
        buf[0] = (byte) (len & 0xff);
        buf[1] = (byte) ((len >> 8) & 0xff);
        try {
            outputStream.write(buf, 0, 2);
            outputStream.write(cmd, 0, len);
        } catch (IOException ex) {
            Log.e(TAG, "write error");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }
    private boolean readBytes(LocalSocket socket,byte buffer[], int len) {
        int off = 0, count;
        InputStream mIn= null;
        try {
            mIn = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        if (len < 0)
            return false;
        while (off != len) {
            try {
                count = mIn.read(buffer, off, len - off);
                if (count <= 0) {
                    Log.e(TAG, "read error " + count);
                    break;
                }
                off += count;
            } catch (IOException ex) {
                Log.e(TAG, "read exception");
                break;
            }
        }
        if (off == len)
            return true;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int readReply(LocalSocket socket) {
        int retval;
        if (!readBytes(socket,buf, 2))
            return -1;
        retval = (((int) buf[0]) & 0xff) | ((((int) buf[1]) & 0xff) << 8);
        return retval;
    }
    public int startAp(){
        LocalSocket socket=connect();
        if(null==socket){
            Log.e(TAG,"can't connect to socket");
            return -1;
        }
        if(writeCommand(socket,CMD_START)){
            int ret=readReply(socket);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(ret!=0){
                Log.e(TAG,"execute cmd failed");
                return -1;
            }
            return 0;
        }
        return -1;
    }
    public int stopAp(){
        LocalSocket socket=connect();
        if(null==socket){
            Log.e(TAG,"can't connect to socket");
            return -1;
        }
        if(writeCommand(socket,CMD_STOP)){
            int ret=readReply(socket);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(ret!=0){
                Log.e(TAG,"execute cmd failed");
                return -1;
            }
            return 0;
        }
        return -1;
    }
}
