/* //device/libs/android_runtime/android_os_Gpio.cpp
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#define LOG_TAG "com_dangs"
#define LOG_NDEBUG 0

#include "JNIHelp.h"
#include "jni.h"
#include "android_runtime/AndroidRuntime.h"
#include "utils/Errors.h"
#include "utils/String8.h"
#include "android_util_Binder.h"
#include <stdio.h>
#include <assert.h>
#include <binder/IServiceManager.h>
#include <binder/IPCThreadState.h>
#include "ISystemMixService.h"
#include <cutils/properties.h>
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include "boot.hpp"

using namespace android;

static sp<ISystemMixService> systemmixService;


int speed_arr[] = {B115200, B38400, B19200, B9600, B4800, B2400, B1200, B300,
                   B115200, B38400, B19200, B9600, B4800, B2400, B1200, B300, };
int name_arr[] = {115200, 38400, 19200, 9600, 4800, 2400, 1200,  300, 
                  115200, 38400, 19200, 9600, 4800, 2400, 1200,  300, };

static void init_native(JNIEnv *env){
		ALOGD("init  version 20181009");
		sp<IServiceManager> sm = defaultServiceManager();
		sp<IBinder> binder;
		do{
			binder = sm->getService(String16("dangs.systemmix"));
			if(binder != 0){
				break;
			}
			ALOGW("dangs systemmix service not published, waiting...");
			usleep(500000);
		}while(true);

		systemmixService = interface_cast<ISystemMixService>(binder);
}

static void throw_NullPointerException(JNIEnv *env, const char* msg){
		jclass clazz;
		clazz = env->FindClass("java/lang/NullPointerException");
		env->ThrowNew(clazz, msg);
}

static int setBootAnimation(JNIEnv *env, jobject clazz, jstring jpathName) {
    if(systemmixService == NULL || jpathName == NULL){
        throw_NullPointerException(env, "systemmix service has not start or the filepath is null!");
    }
    const char *pathName = env->GetStringUTFChars(jpathName, NULL);
    int ret;
    ret = systemmixService->setBootAnimation(pathName);
    env->ReleaseStringUTFChars(jpathName, pathName);
    return ret;
}

static int setBootLogo(JNIEnv *env, jobject clazz, jstring jpathName) {
    if(systemmixService == NULL || jpathName == NULL){
        throw_NullPointerException(env, "systemmix service has not start or the filepath is null!");
    }
    const char *pathName = env->GetStringUTFChars(jpathName, NULL);
    int ret;

    ret = systemmixService->setBootLogo(pathName);
    env->ReleaseStringUTFChars(jpathName, pathName);
    return ret;
}

static jstring getProperty_native(JNIEnv *env, jobject clazz, jstring key){
		jstring value = NULL;
		if(systemmixService == NULL || key == NULL){
			throw_NullPointerException(env,"systemmix service has not start, or key is null!");
		}
		const char *ckey = env->GetStringUTFChars(key, NULL);
		char *cvalue = new char[PROPERTY_VALUE_MAX];
		if(cvalue == NULL){
			env->ReleaseStringUTFChars(key, ckey);
			throw_NullPointerException(env,"fail to allocate memory for value");
		}

		int ret = systemmixService->getProperty(ckey, cvalue);
		if(ret > 0){
			value = env->NewStringUTF(cvalue);
			if(value == NULL){
				ALOGE("Fail in creating java string with %s", cvalue);
			}
		}
		delete[] cvalue;
		env->ReleaseStringUTFChars(key, ckey);
		return value;
}

static int setProperty_native(JNIEnv *env, jobject clazz, jstring key, jstring value){
		if(systemmixService == NULL){
			throw_NullPointerException(env,"systemmix service has not start!");
		}
		if(key == NULL || value == NULL){
			return -1;
		}else{
			const char *ckey = env->GetStringUTFChars(key, NULL);
			const char *cvalue = env->GetStringUTFChars(value, NULL);
			int ret = systemmixService->setProperty(ckey, cvalue);
			env->ReleaseStringUTFChars(key, ckey);
	        env->ReleaseStringUTFChars(value, cvalue);
			return ret;
		}
}


void set_speed(int fd, int speed){
  int   i; 
  int   status; 
  struct termios   Opt;
  tcgetattr(fd, &Opt); 
  for ( i= 0;  i < sizeof(speed_arr) / sizeof(int);  i++) { 
    if  (speed == name_arr[i]) {     
      tcflush(fd, TCIOFLUSH);     
      cfsetispeed(&Opt, speed_arr[i]);  
      cfsetospeed(&Opt, speed_arr[i]);   
      status = tcsetattr(fd, TCSANOW, &Opt);  
      if  (status != 0) {        
        perror("tcsetattr fd1");  
        return;     
      }    
      tcflush(fd,TCIOFLUSH);   
    }  
  }
}

int set_Parity(int fd,int databits,int stopbits,int parity)
{ 
    struct termios options; 
    if  ( tcgetattr( fd,&options)  !=  0) { 
        perror("SetupSerial 1");     
        return(false);  
    }
    options.c_cflag &= ~CSIZE; 
    switch (databits) /*设置数据位数*/
    {   
    case 7:     
        options.c_cflag |= CS7; 
        break;
    case 8:     
        options.c_cflag |= CS8;
        break;   
    default:    
        fprintf(stderr,"Unsupported data size\n"); return (false);  
    }
    switch (parity) 
    {   
        case 'n':
        case 'N':    
            options.c_cflag &= ~PARENB;   /* Clear parity enable */
            options.c_iflag &= ~INPCK;     /* Enable parity checking */ 
            break;  
        case 'o':   
        case 'O':     
            options.c_cflag |= (PARODD | PARENB); /* 设置为奇效验*/  
            options.c_iflag |= INPCK;             /* Disnable parity checking */ 
            break;  
        case 'e':  
        case 'E':   
            options.c_cflag |= PARENB;     /* Enable parity */    
            options.c_cflag &= ~PARODD;   /* 转换为偶效验*/     
            options.c_iflag |= INPCK;       /* Disnable parity checking */
            break;
        case 'S': 
        case 's':  /*as no parity*/   
            options.c_cflag &= ~PARENB;
            options.c_cflag &= ~CSTOPB;break;  
        default:   
            fprintf(stderr,"Unsupported parity\n");    
            return 0;  
        }  
    /* 设置停止位*/  
    switch (stopbits)
    {   
        case 1:    
            options.c_cflag &= ~CSTOPB;  
            break;  
        case 2:    
            options.c_cflag |= CSTOPB;  
           break;
        default:    
             fprintf(stderr,"Unsupported stop bits\n");  
             return (false); 
    } 
    /* Set input parity option */ 
    if (parity != 'n')   
        options.c_iflag |= INPCK; 
    tcflush(fd,TCIFLUSH);
    options.c_cc[VTIME] = 150; /* 设置超时15 seconds*/   
    options.c_cc[VMIN] = 0; /* Update the options and do it NOW */
    
    options.c_lflag  &= ~(ICANON | ECHO | ECHOE | ISIG);  /*Input*/
    options.c_oflag  &= ~OPOST;   /*Output*/
    if (tcsetattr(fd,TCSANOW,&options) != 0)   
    { 
        perror("SetupSerial 3");   
        return (false);  
    } 

//    options.c_oflag  &= ~OPOST;   /*Output*/
    return (true);  
}
static jobject serialport_open(JNIEnv *env, jobject thiz, jstring path, jint baudrate, jint flags)
{
		int fd;
		speed_t speed;
		jobject mFileDescriptor;


	
		{
			jboolean iscopy;
			const char *path_utf = env->GetStringUTFChars(path, &iscopy);
			ALOGI("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
			fd = open(path_utf, O_RDWR | flags);
			ALOGI("open() fd = %d", fd);
			env->ReleaseStringUTFChars(path, path_utf);
			if (fd == -1)
			{
				ALOGE("Cannot open port");		
				return NULL;
			}
		}

		
    set_speed(fd,baudrate);
    if (set_Parity(fd,8,1,'N') == false)  
    {
        ALOGE("Set Parity Error\n");
      
    }

		
		{
			jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
			jmethodID iFileDescriptor = env->GetMethodID( cFileDescriptor, "<init>", "()V");
			jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
			mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
			env->SetIntField( mFileDescriptor, descriptorID, (jint)fd);
		}

		return mFileDescriptor;
}


static void serialport_close(JNIEnv *env, jobject thiz, jobject fd)
{
	
		jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");
		jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");
		jint descriptor = env->GetIntField(fd, descriptorID);
		ALOGI("close(fd = %d)", descriptor);
		close(descriptor);
}

void write_node(char *path, char * value){
	
	
	  int fd = open(path, O_WRONLY);
    if(fd < 0){
        ALOGE("fail in open file %s", path);
        return ;
    }
    write(fd, value, strlen(value));  
	  close(fd);
}

static jboolean updategx8008(JNIEnv *env, jobject thiz, jstring mcupath, jstring dsppath, jstring cfgpath){
	
	
	  jboolean iscopy;
	  const char *dev = NULL, *boot_file = "leo";
		const char *path_mcu = env->GetStringUTFChars(mcupath, &iscopy);
	  const char *path_dsp = env->GetStringUTFChars(dsppath, &iscopy);
	  const char *path_cfg = env->GetStringUTFChars(cfgpath, &iscopy);
	  char cmd[1024]={0};
	  memset(cmd, 0, 1024);	
	  sprintf(cmd, "flash erase 0x000000 0x8000000; download 0 %s; download 0x100000 %s", path_mcu, path_dsp, path_cfg);	  
	  ALOGI("cmd %s", cmd);
	  env->ReleaseStringUTFChars(mcupath, path_mcu);
	  env->ReleaseStringUTFChars(dsppath, path_dsp);
	  env->ReleaseStringUTFChars(cfgpath, path_cfg);
	  
	  write_node("/sys/class/gpio/gpio14/direction", "out");
	  write_node("/sys/class/gpio/gpio174/direction", "out");
	  write_node("/sys/class/gpio/gpio174/value", "1");
	  write_node("/sys/class/gpio/gpio14/value", "0");
	  sleep(2);	
	  write_node("/sys/class/gpio/gpio14/value", "1");

		Boot boot;
	  boot.SetTransferMode(USBSLAVE);	  
	  bool ret=boot.BootCommand(dev, boot_file, cmd);
	  usleep(500);
	

		ALOGI("\nbootx update GX8008 finish ...\n");
	  write_node("/sys/class/gpio/gpio14/value", "0");
	  write_node("/sys/class/gpio/gpio174/value", "0");
		sleep(2);
	  write_node("/sys/class/gpio/gpio14/value", "1");
		
	  ALOGI("Restar GX8008 OK\n");
	  
	  sleep(2);
	  if(access("/dev/snd/pcmC1D0c", F_OK)==0){		
		   printf("/dev/snd/pcmC1D0c is ok\n");		
	  }else{
		   printf("/dev/snd/pcmC1D0c is error\n");
		   return 0;
	  } 
	
	  return ret;
}

static jstring runShell_native(JNIEnv *env, jobject clazz, jstring jcmd) {
	
	
		jstring value = NULL;
		if (systemmixService == NULL || jcmd == NULL) {
			throw_NullPointerException(env, "systemmix service has not start, or shell cmd is null!");
		}
		const char *cmd = env->GetStringUTFChars(jcmd, NULL);
		char *cvalue = new char[MAX_BUFFER_SIZE];
		if (cvalue == NULL) {
			env->ReleaseStringUTFChars(jcmd, cmd);
			throw_NullPointerException(env, "runshell_native() fail to allocate memory for value");
		}
	 
		int ret = systemmixService->runShell(cmd, cvalue);
		value = env->NewStringUTF(cvalue);
		if (value == NULL) {
			ALOGE("Fail in creating java string with %s", cvalue);
		}
		delete[] cvalue;
		env->ReleaseStringUTFChars(jcmd, cmd);
		return value;
		
		
}


static JNINativeMethod method_table[] = {
  { "nativeInit",             "()V",                                              (void*)init_native},
  { "nativeSetProperty",      "(Ljava/lang/String;Ljava/lang/String;)I",          (void*)setProperty_native },
  { "nativeGetProperty",      "(Ljava/lang/String;)Ljava/lang/String;",           (void*)getProperty_native },
  { "nativeSetBootAnimation", "(Ljava/lang/String;)I",                            (void*)setBootAnimation },
  { "nativeSetBootLogo",      "(Ljava/lang/String;)I",                            (void*)setBootLogo },
  { "serialportopen_native",  "(Ljava/lang/String;II)Ljava/io/FileDescriptor;",   (void*)serialport_open},
  { "serialportclose_native", "(Ljava/io/FileDescriptor;)V",                      (void*)serialport_close},
  { "nativeUpdateGX8008",     "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z",    (void*)updategx8008},
  { "nativeRunShell",         "(Ljava/lang/String;)Ljava/lang/String;",           (void*)runShell_native },  
};

static int register_android_os_AdManager(JNIEnv *env){
	return AndroidRuntime::registerNativeMethods(
		env, "com/dangzhi/DangZhiOSManager",method_table, NELEM(method_table));
}

jint JNI_OnLoad(JavaVM* vm, void* reserved){
	  JNIEnv* env = NULL;
    jint result = -1;

	ALOGD("AdManager JNI_OnLoad()");

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    if (register_android_os_AdManager(env) < 0) {
        ALOGE("ERROR: AdManager native registration failed\n");
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

bail:
    return result;
}

