package com.mele.musicdaemon;
 interface RemoteServiceAIDL {

	String installApp(String path, String packagename);
    void startMirror(String ip, int port);
    void stopMirror();	
    void keyCode(int code);
    void MouseEvent(float x, float y, int action);
    void killApp(String packagename);
    void ioctrl(String cmd, String var);
    void jni_i2c_open(); 	
	void jni_i2c_write(int addr, inout int[] buf, int len); 
	void jni_i2c_writedev(int dev, int addr, inout int[] buf, int len); 	
	int[] jni_i2c_read(int addr, int len); 	
	void jni_i2c_close(); 	
    
}
