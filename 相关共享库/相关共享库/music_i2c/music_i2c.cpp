#include <stdio.h>
#include <assert.h>
#include <limits.h>
#include <unistd.h>
#include <fcntl.h>
#include <sched.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <linux/i2c.h>
#include <linux/i2c.h>

#include <linux/videodev.h>
#include <hardware/hardware.h>
#include <hardware/aml_screen.h>

#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>

#include "value.h"



#include "jniUtils.h"
#include "keyevent.h"




#define TAG "MusicI2C"
#define I2C "/dev/i2c-0"

#define DEG




//#endif
BYTE TAS5711WriteReg(BYTE cDevAddr, BYTE cReg, const BYTE *cData, int cLength);
int init_main(int argc, char **argv);
int i2c_write_reg(char *dev, unsigned char *buf, unsigned slave_address, unsigned reg_address, int len);
int i2c_read_reg(char *dev, unsigned char *buf, unsigned slave_address, unsigned reg_address, int len);
int i2cfd=-1;  


//extern "C" {
//	
//	   void* getevent_main(void *device);
//	
//}

pthread_t 		  id_cmd;

static JavaVM* gc_jvm = NULL;
JNIEnv *Env=NULL;
jobject gs_object = NULL;
jclass gs_class;

jmethodID method_key;
int file=-1;
int slaveAddr = 0x34;
unsigned char buf[8]={0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
int bufint[4]={0,0,0,0};
void*  test_main(void* param){
		LOGI("===========test_main ***********  main============");
		
		
		char *cmd="/dev/input/event3";
    //getevent_main(1, &cmd);
    
    LOGI("finish shsjsjjjjjjjjjjjjjjjjjjjjjj");
    file=open("/dev/i2c-0", O_RDWR); 
    if(file==-1){
    	
    	printf("/dev/i2c-0  =====open error\n");
    	
    }else{
    	
    	printf("/dev/i2c-0 open ok %d\n", file);
    	
    }
    
   int  count=0; 
 // while(true){
    
    int res=ioctl(file, I2C_SLAVE, 0x36);  
    if (res != 0) {
          printf("I2C: Can't set slave address ==0x43 %d\n", res);
         
    }
//    res=ioctl(file, 0x34, 0x07);  
//    if (res != 0) {
//          printf("I2C: Can't set slave address 0x34\n");
//         
//    }
//    res=ioctl(file, 0x36, 0x07);  
//    if (res != 0) {
//          printf("I2C: Can't set slave address 0x36\n");
//         
//    }
   // res=write(file, buf, 1);
    res=read(file, bufint, 0x01);
   // if(res!=1){
    	 printf("res %s======>%d\n", buf, res);
    //}
 // }
    close(file);
    printf("======exit\n");
    return 0;
}


void OnKeyMsg(int keycode , int action){
	
	int err = 0;
	JNIEnv* env;

	if (!gc_jvm){
		
		return;
	}

	if (gc_jvm->AttachCurrentThread(&env, NULL) != JNI_OK) {

		return;
	}
  //LOGI(">>>>>>>>>>>>>>");
	env->CallVoidMethod(gs_object, method_key, keycode, action);

	
	if (gc_jvm->DetachCurrentThread() != JNI_OK) {
	       return ;
	}	
	
	
}
void *ToInstance(void * dev){
	
	
	  GetEvent *ge=new GetEvent();
	  ge->getevent_main(dev);
	  
	
	  return 0;
}

int main(int argc, char **argv){
	
	
//		char *cmd="/dev/input/event2";
//    pthread_create(&id_cmd, NULL, ToInstance, cmd);
		char *cmd1="/dev/input/event3";
    pthread_create(&id_cmd, NULL, ToInstance, cmd1);
    char *cmd2="/dev/input/event4";
    pthread_create(&id_cmd, NULL, ToInstance, cmd2);
    		
    char *cmd3="/dev/input/event5";
    pthread_create(&id_cmd, NULL, ToInstance, cmd3);	
    
    pthread_join(id_cmd, NULL);	
}  
void jni_i2c_open(JNIEnv* env, jobject thiz) {
	
	
	
	  if (env->GetJavaVM(&gc_jvm) < 0) {
		  return ;
	  }
	  Env=env;
	  gs_object=env->NewGlobalRef(thiz);
	  gs_class=env->GetObjectClass(thiz);
	  method_key = env->GetMethodID(gs_class, "onKeyMessage","(II)V");

	  LOGI("jni_i2c_open ");
	  if(i2cfd==-1){
	  	
	    i2cfd= open(I2C, O_RDWR);  
	    if (i2cfd<0) {  
	        LOGI("Error on opening the device file\n");  
	        return ;  
	    }  
	    ioctl(i2cfd, I2C_TIMEOUT, 2);  
	    ioctl(i2cfd, I2C_RETRIES, 1);
 
    }

	  init_main(5, NULL);
	  
	  	
		char *cmd="/dev/input/event2";
    pthread_create(&id_cmd, NULL, ToInstance, cmd);
		char *cmd1="/dev/input/event3";
    pthread_create(&id_cmd, NULL, ToInstance, cmd1);
    char *cmd2="/dev/input/event4";
    pthread_create(&id_cmd, NULL, ToInstance, cmd2);
    		
    char *cmd3="/dev/input/event5";
    pthread_create(&id_cmd, NULL, ToInstance, cmd3);	 

    char *cmd4="/dev/input/event1";
    pthread_create(&id_cmd, NULL, ToInstance, cmd4);	

    char *cmd5="/dev/input/event0";
    pthread_create(&id_cmd, NULL, ToInstance, cmd5);	
    
	  LOGI("jni_i2c_open  ok ...... key .....");
}
void jni_i2c_write(JNIEnv *env, jobject thiz,  jint regAddr, jintArray bufArr, jint len){
	
	
	      LOGI("jni_i2c_write ....");
	  
	      jint *bufInt;
	      unsigned char *bufByte;
	      int res = 0, i = 0, j = 0;
	      
	      if (len <= 0) {
	          LOGI("I2C: buf len <=0");
	         // goto err0;
	      }
	      
//	      bufInt = (jint *) malloc(len * sizeof(int));
//	      if (bufInt == 0) {
//	          LOGI("I2C: nomem");
//	          goto err0;
//	      }
	      bufByte = (unsigned char*) malloc(len);
	      if (bufByte == 0) {
	          LOGI("I2C: nomem");
	         // goto err1;
	      }
	     // LOGI(">>>>");
	      bufInt = env->GetIntArrayElements(bufArr,NULL); 
	    //  env->GetIntArrayRegion(bufArr, 0, len, bufInt);
	     
	      for (i = 0; i < len; i++)
	          bufByte[i] = bufInt[i];      
	      
       //LOGI("===%d, 0x%x, %d", regAddr, *bufByte, len);
	     i2c_write_reg(I2C, bufByte, TAS5711_Addr, regAddr, len); 
	
	    
	     env->ReleaseIntArrayElements(bufArr, bufInt, 0);
	     free(bufByte);
	     free(bufInt);
	    
	   
	
	
//	err2:
//	    free(bufByte);
//	err1:
//	    free(bufInt);
//	err0:
	    return ;

}
void jni_i2c_writedev(JNIEnv *env, jobject thiz,  jint devAddr, jint regAddr, jintArray bufArr, jint len){
	
	
	      LOGI("jni_i2c_writedev ....");
	     
	      jint *bufInt;
	      unsigned char *bufByte;
	      int res = 0, i = 0, j = 0;
	      
	      if (len <= 0) {
	
	          LOGI("I2C: buf len <=0");
	         // goto err0;
	      }
	      
//	      bufInt = (jint *) malloc(len * sizeof(int));
//	      if (bufInt == 0) {
//	          LOGI("I2C: nomem");
//	          goto err0;
//	      }
	      bufByte = (unsigned char*) malloc(len);
	      if (bufByte == 0) {
	          LOGI("I2C: nomem");
	         // goto err1;
	      }
	     // LOGI(">>>>");
	      bufInt = env->GetIntArrayElements(bufArr,NULL); 
	    //  env->GetIntArrayRegion(bufArr, 0, len, bufInt);
	     
	      for (i = 0; i < len; i++)
	          bufByte[i] = bufInt[i];      
	      
      
	     i2c_write_reg(I2C, bufByte, devAddr, regAddr, len); 
	   //  LOGI("===%d, 0x%x, %d", regAddr, *bufByte, len);
	    
	     env->ReleaseIntArrayElements(bufArr, bufInt, 0);
	     if(bufByte!=NULL) free(bufByte);
	  //   free(bufInt);
	    
	   
	
	
	
	
	
//	err2:
//	    free(bufByte);
//	err1:
//	    free(bufInt);
//	err0:
	    return ;

}
jintArray jni_i2c_read(JNIEnv *env, jobject thiz, jint regAddr, jint len){
	
	    BYTE buf[len];
	    i2c_read_reg(I2C, buf, TAS5711_Addr, regAddr, len);
      jintArray jint_arr_temp = env->NewIntArray(len);      
      jint* int_arr_temp = env->GetIntArrayElements(jint_arr_temp,NULL); 
      
      for(int i=0; i<len; i++){
           int_arr_temp[i]=buf[i];
         //  LOGI("===>0x%x", int_arr_temp[i]);
      }
      return jint_arr_temp;
  
} 

void jni_i2c_close(JNIEnv *env, jobject thiz){
	
      close(i2cfd);
      i2cfd=-1;
} 
void jni_i2c_reset(JNIEnv *env, jobject thiz){
	
	  LOGI("jni_i2c_reset ");
	  if(i2cfd==-1){
	    i2cfd= open(I2C, O_RDWR);  
	    if (i2cfd<0) {  
	        LOGI("Error on opening the device file\n");  
	        return ;  
	    }  
	    ioctl(i2cfd, I2C_TIMEOUT, 2);  
	    ioctl(i2cfd, I2C_RETRIES, 1); 
    }

	  init_main(5, NULL);
} 


static JNINativeMethod methods[] = {
	
  { "jni_i2c_open",                  "()V",                                     (void*) jni_i2c_open  },   
  { "jni_i2c_write",                 "(I[II)V",                                 (void*) jni_i2c_write }, 
  { "jni_i2c_writedev",              "(II[II)V",                                (void*) jni_i2c_writedev }, 
  { "jni_i2c_read",                  "(II)[I",                                  (void*) jni_i2c_read  },
  { "jni_i2c_reset",                 "()V",                                     (void*) jni_i2c_reset },    
  { "jni_i2c_close",                 "()V",                                     (void*) jni_i2c_close } 
            
};
	

int register_AndroidJNI_function(JNIEnv *env) {
     return jniRegisterNativeMethods(env, "com/mele/musicdaemon/I2C", methods, sizeof(methods) / sizeof(methods[0]));
}




/* This is the structure as used in the I2C_RDWR ioctl call */  
struct i2c_rdwr_ioctl_data {  
        struct i2c_msg __user *msgs;    /* pointers to i2c_msgs */  
        __u32 nmsgs;                    /* number of i2c_msgs */  
};  
  
int i2c_read_reg(char *dev, unsigned char *buf, unsigned slave_address, unsigned reg_address, int len)  
{  
    struct i2c_rdwr_ioctl_data work_queue;  
    unsigned char w_val = reg_address;  
    int ret;  
  
    if(i2cfd==-1){
	    i2cfd= open(I2C, O_RDWR);  
	    if (i2cfd<0) {  
	        printf("Error on opening the device file\n");  
	        return 0;  
	    }  
	    ioctl(i2cfd, I2C_TIMEOUT, 2);  
	    ioctl(i2cfd, I2C_RETRIES, 1); 
    }
  
    work_queue.nmsgs = 2;  
    work_queue.msgs = (struct i2c_msg*)malloc(work_queue.nmsgs *sizeof(struct  
            i2c_msg));  
    if (!work_queue.msgs) {  
        printf("Memory alloc error\n");  
        close(i2cfd);  
        return 0;  
    }  
    memset(work_queue.msgs, 0, work_queue.nmsgs *sizeof(struct  
            i2c_msg));
  
    (work_queue.msgs[0]).len = 1;  
    (work_queue.msgs[0]).addr = slave_address;  
    (work_queue.msgs[0]).buf = &w_val;  
  
    (work_queue.msgs[1]).len = len;  
    (work_queue.msgs[1]).flags = I2C_M_RD;  
    (work_queue.msgs[1]).addr = slave_address;  
    (work_queue.msgs[1]).buf = buf;  
  
    ret = ioctl(i2cfd, I2C_RDWR, (unsigned long) &work_queue);  
    if (ret < 0) {  
        printf("Error during I2C_RDWR ioctl with error code: %d\n", ret);  
        close(i2cfd);  
        free(work_queue.msgs);  
        return 0;  
    } else {
    	
    	  LOGI("read salve:%02x reg:%02x", slave_address, reg_address);    
        printf("read salve:%02x reg:%02x\n", slave_address, reg_address);
        for(int i=0; i<len; i++) LOGI("read value:%02x", buf[i]);  
       
        close(i2cfd); 
        i2cfd=-1; 
        free(work_queue.msgs);  
        return len;  
    }  
}  
  
int i2c_write_reg(char *dev, unsigned char *buf, unsigned slave_address, unsigned reg_address, int len)  
{  
    struct i2c_rdwr_ioctl_data work_queue;  
    unsigned char w_val = reg_address;  
    unsigned char w_buf[len+1];  
    int ret;  
    LOGI("write salve:%02x reg:%02x", slave_address, reg_address);

    w_buf[0] = reg_address;  
    
    if(i2cfd==-1){
	    i2cfd= open(dev, O_RDWR);  
	    if (i2cfd<0) {  
	        LOGI("Error on opening the device file\n");  
	        return 0;  
	    }  
	    ioctl(i2cfd, I2C_TIMEOUT, 2);  
	    ioctl(i2cfd, I2C_RETRIES, 1); 
    }
    work_queue.nmsgs = 1;  
    work_queue.msgs = (struct i2c_msg*)malloc(work_queue.nmsgs *sizeof(struct  
            i2c_msg));  
    if (!work_queue.msgs) {  
        LOGI("Memory alloc error\n");  
        close(i2cfd);  
        return 0;  
    }  
    memset(work_queue.msgs, 0, work_queue.nmsgs *sizeof(struct  
            i2c_msg));
 
  
    (work_queue.msgs[0]).len = 1 + len;  
    (work_queue.msgs[0]).addr = slave_address;  
    (work_queue.msgs[0]).buf = w_buf;  
  
    memcpy(w_buf + 1, buf, len);  
  
    ret = ioctl(i2cfd, I2C_RDWR, (unsigned long) &work_queue);  
    if (ret < 0) {  
        LOGI("Error during I2C_RDWR ioctl with error code: %d\n", ret);  
        close(i2cfd);  
        free(work_queue.msgs);  
        return 0;  
    } else {  
        printf("write salve:%02x reg:%02x value:%02x\n", slave_address, reg_address, buf[0]); 
        for(int i=0; i<len; i++) LOGI("write value:%02x", buf[i]);
        //LOGI("write salve:%02x reg:%02x value:%02x\n", slave_address, reg_address, buf[0]);  
       // close(i2cfd);  
        free(work_queue.msgs);  
        return len;  
    }  
}  
  
int init_main(int argc, char **argv)  
{  
    unsigned int fd;  
    unsigned int slave_address, reg_address;  
    unsigned r_w;  
    unsigned w_val;  
    unsigned char rw_val=0x0;  

//LOGI(">>>>>>>1 %d", i2cfd);
    if(i2cfd==-1){
	    i2cfd= open(I2C, O_RDWR);  
	    if (i2cfd<0) {  
	        LOGI("Error on opening the device file\n");  
	        return 0;  
	    }  
	    ioctl(i2cfd, I2C_TIMEOUT, 2);  
	    ioctl(i2cfd, I2C_RETRIES, 1); 
    }  
    
//    LOGI(">>>>>>>2 %d", i2cfd);
//    if (argc < 5) {  
//        printf("Usage:\n%s /dev/i2c-x start_addr reg_addr rw[0|1] [write_val]\n", argv[0]);  
//        return 0;  
//    }  
//  
//    fd = open(argv[1], O_RDWR);  
//  
//    if (!fd) {  
//        printf("Error on opening the device file %s\n", argv[1]);  
//        return 0;  
//    }  
//  
//    sscanf(argv[2], "%x", &slave_address);  
//    sscanf(argv[3], "%x", &reg_address);  
//    sscanf(argv[4], "%d", &r_w);  
//  
//    if (r_w == 0) {  
//        i2c_read_reg(argv[1], &rw_val, slave_address, reg_address, 1);  
//        printf("Read %s-%x reg 0x%x, read value:0x%x\n", argv[1], slave_address, reg_address, rw_val);  
//    } else {  
//        if (argc < 6) {  
//            printf("Usage:\n%s /dev/i2c-x start_addr reg_addr r|w[0|1] [write_val]\n", argv[0]);  
//            return 0;  
//        }  
//        sscanf(argv[5], "%x", &w_val);  
//        if ((w_val & ~0xff) != 0)  
//            printf("Error on written value %s\n", argv[5]);  
//          
//        rw_val = (unsigned char)w_val;  
//        printf("written value 0x02%x\n", rw_val);
//        i2c_write_reg(argv[1], &rw_val, slave_address, reg_address, 1);  
//    }  
  
  LOGI("PCA9555 write =============>\n");  
//#define PCA9555_INPORT_0        0x00
//#define PCA9555_INPORT_1        0x01
//#define PCA9555_OUTPORT_0       0x02
//#define PCA9555_OUTPORT_1       0x03
//#define PCA9555_POL_INV_0       0x04
//#define PCA9555_POL_INV_1       0x05
//#define PCA9555_CONFIG_0        0x06
//#define PCA9555_CONFIG_1        0x07  
  BYTE Reg_ca[]	= {0x00};
  BYTE Reg_cb[]	= {0x00};
  
  BYTE Reg_cc[]	= {0xFB};//P1
  BYTE Reg_cd[]	= {0xFD};//P0
 
  TAS5711WriteReg(0x27,0x06,Reg_ca,1);//P0 config
  TAS5711WriteReg(0x27,0x07,Reg_cb,1);  //P1 config
  TAS5711WriteReg(0x27,0x02,Reg_cd,1); 
  TAS5711WriteReg(0x27,0x03,Reg_cc,1);

  LOGI("PCA9555 ok =============>\n"); 
   
  LOGI("PCA2258 ok ----111--------->\n");  
  
  unsigned char ucLevel=12;
  unsigned char Reg_ce[]	= {0x20};
  Reg_ce[0]=ucLevel/10;
  unsigned char Reg_cf[]	= {0x8};
  Reg_cf[0]=ucLevel%10;
  TAS5711WriteReg(0x44,0x87,Reg_ce,0); 
  TAS5711WriteReg(0x44,0x99,Reg_cf,0);
  
  TAS5711WriteReg(0x44,0x47,Reg_ce,0); 
  TAS5711WriteReg(0x44,0x59,Reg_cf,0);
  
  LOGI("PCA2258 ok *****222*********%d, %d>\n", Reg_ce[0], Reg_cf[0]);  

  
	TAS5711WriteReg(TAS5711_Addr,0x1b,Reg_1b,1);

	TAS5711WriteReg(TAS5711_Addr,0x0a, Reg_0a, 1);
	
	TAS5711WriteReg(TAS5711_Addr,0x06,Reg_06_Mute,1);
	TAS5711WriteReg(TAS5711_Addr,0x0a,Reg_0a,1);
	TAS5711WriteReg(TAS5711_Addr,0x09,Reg_09,1);
	TAS5711WriteReg(TAS5711_Addr,0x08,Reg_08,1);
	TAS5711WriteReg(TAS5711_Addr,0x14,Reg_14,1);
	TAS5711WriteReg(TAS5711_Addr,0x13,Reg_13,1);
	TAS5711WriteReg(TAS5711_Addr,0x12,Reg_12,1);
	TAS5711WriteReg(TAS5711_Addr,0x11,Reg_11,1);
	TAS5711WriteReg(TAS5711_Addr,0x0e,Reg_0e,1);
	TAS5711WriteReg(TAS5711_Addr,0x20,Reg_20,4);
	TAS5711WriteReg(TAS5711_Addr,0x10,Reg_10,1);
	TAS5711WriteReg(TAS5711_Addr,0x0b,Reg_0b,1);
	TAS5711WriteReg(TAS5711_Addr,0x10,Reg_10,1);
	TAS5711WriteReg(TAS5711_Addr,0x1c,Reg_1c,1);
	TAS5711WriteReg(TAS5711_Addr,0x19,Reg_19,1);
	TAS5711WriteReg(TAS5711_Addr,0x25,Reg_25,4);
	
// Biquads 
	TAS5711WriteReg(TAS5711_Addr,0x50,Reg_50,4);
	TAS5711WriteReg(TAS5711_Addr,0x29,Reg_29,20);
//	TAS5711WriteReg(0x30,Reg_30,20);
	TAS5711WriteReg(TAS5711_Addr,0x2a,Reg_2a,20);
	TAS5711WriteReg(TAS5711_Addr,0x2b,Reg_2b,20);
	
	TAS5711WriteReg(TAS5711_Addr,0x2c,Reg_2c,20);		
	TAS5711WriteReg(TAS5711_Addr,0x2d,Reg_2d,20);		// n 
	TAS5711WriteReg(TAS5711_Addr,0x2e,Reg_2e,20);
	
	TAS5711WriteReg(TAS5711_Addr,0x2f,Reg_2f,20);
	TAS5711WriteReg(TAS5711_Addr,0x58,Reg_58,20);
	TAS5711WriteReg(TAS5711_Addr,0x59,Reg_59,20);

	TAS5711WriteReg(TAS5711_Addr,0x5a,Reg_5a,20);
	TAS5711WriteReg(TAS5711_Addr,0x5b,Reg_5b,20);
	
//DRCs
	TAS5711WriteReg(TAS5711_Addr,0x3a,Reg_3a,8);
	TAS5711WriteReg(TAS5711_Addr,0x3b,Reg_3b,8);
	TAS5711WriteReg(TAS5711_Addr,0x3c,Reg_3c,8);
	TAS5711WriteReg(TAS5711_Addr,0x40,Reg_40,4);
	TAS5711WriteReg(TAS5711_Addr,0x41,Reg_41,4);
	TAS5711WriteReg(TAS5711_Addr,0x42,Reg_42,4);
	TAS5711WriteReg(TAS5711_Addr,0x46,Reg_46,4);
	TAS5711WriteReg(TAS5711_Addr,0x39,Reg_39,8);
	TAS5711WriteReg(TAS5711_Addr,0x3d,Reg_3d,8);
	TAS5711WriteReg(TAS5711_Addr,0x3e,Reg_3e,8);
	TAS5711WriteReg(TAS5711_Addr,0x3f,Reg_3f,8);
	TAS5711WriteReg(TAS5711_Addr,0x43,Reg_43,4);
	TAS5711WriteReg(TAS5711_Addr,0x44,Reg_44,4);
	TAS5711WriteReg(TAS5711_Addr,0x45,Reg_45,4);
	TAS5711WriteReg(TAS5711_Addr,0x46,Reg_46,4);
	TAS5711WriteReg(TAS5711_Addr,0x52,Reg_52,12);
	TAS5711WriteReg(TAS5711_Addr,0x60,Reg_60,8);
	TAS5711WriteReg(TAS5711_Addr,0x53,Reg_53,16);
	TAS5711WriteReg(TAS5711_Addr,0x54,Reg_54,16);
	TAS5711WriteReg(TAS5711_Addr,0x56,Reg_56,4);
	TAS5711WriteReg(TAS5711_Addr,0x57,Reg_57,4);
	TAS5711WriteReg(TAS5711_Addr,0x51,Reg_51,12);
	TAS5711WriteReg(TAS5711_Addr,0x55,Reg_55,12);
	TAS5711WriteReg(TAS5711_Addr,0x52,Reg_52,12);
//	TAS5711WriteReg(TAS5711_Addr,0x07,Reg_07,1);
	TAS5711WriteReg(TAS5711_Addr,0x05,Reg_05,1);  

  
  TAS5711WriteReg(TAS5711_Addr,0x06,Reg_06_unMute,1); 
  


  //============================TAS5712_Addr==============================================
  LOGI("TAS5712_Addr Reg........");
	TAS5711WriteReg(TAS5712_Addr,0x1b,Reg_1b,1);

	TAS5711WriteReg(TAS5712_Addr,0x0a, Reg_0a, 1);
	
	TAS5711WriteReg(TAS5712_Addr,0x06,Reg_06_Mute,1);
	TAS5711WriteReg(TAS5712_Addr,0x0a,Reg_0a,1);
	TAS5711WriteReg(TAS5712_Addr,0x09,Reg_09,1);
	TAS5711WriteReg(TAS5712_Addr,0x08,Reg_08,1);
	TAS5711WriteReg(TAS5712_Addr,0x14,Reg_14,1);
	TAS5711WriteReg(TAS5712_Addr,0x13,Reg_13,1);
	TAS5711WriteReg(TAS5712_Addr,0x12,Reg_12,1);
	TAS5711WriteReg(TAS5712_Addr,0x11,Reg_11,1);
	TAS5711WriteReg(TAS5712_Addr,0x0e,Reg_0e,1);
	
	const BYTE Reg20[]	= {0x00, 0x01, 0x77, 0x72};
	TAS5711WriteReg(TAS5712_Addr,0x20,Reg20,4);
	const BYTE Reg21[]	= {0x00,0x00,0x42,0x03};
	TAS5711WriteReg(TAS5712_Addr,0x21,Reg21,4);
	
	
	TAS5711WriteReg(TAS5712_Addr,0x10,Reg_10,1);
	TAS5711WriteReg(TAS5712_Addr,0x0b,Reg_0b,1);
	TAS5711WriteReg(TAS5712_Addr,0x10,Reg_10,1);
	TAS5711WriteReg(TAS5712_Addr,0x1c,Reg_1c,1);
	
  const BYTE _Reg19[]	= {0x3A};
  const BYTE _Reg25[]	= {0x01, 0x10, 0x32, 0x45};	
	TAS5711WriteReg(TAS5712_Addr,0x19,_Reg19,1);
	TAS5711WriteReg(TAS5712_Addr,0x25,_Reg25,4);
	
// Biquads 
	TAS5711WriteReg(TAS5712_Addr,0x50,Reg_50,4);
	TAS5711WriteReg(TAS5712_Addr,0x29,Reg29,20);
	TAS5711WriteReg(TAS5712_Addr,0x30,Reg30,20);
	TAS5711WriteReg(TAS5712_Addr,0x2a,Reg2a,20);//d
	TAS5711WriteReg(TAS5712_Addr,0x2b,Reg2b,20);//d
	TAS5711WriteReg(TAS5712_Addr,0x2c,Reg2c,20);		
	TAS5711WriteReg(TAS5712_Addr,0x2d,Reg2d,20);		// n 
	TAS5711WriteReg(TAS5712_Addr,0x2e,Reg2e,20);	
	TAS5711WriteReg(TAS5712_Addr,0x2f,Reg2f,20);
	TAS5711WriteReg(TAS5712_Addr,0x58,Reg58,20);
	TAS5711WriteReg(TAS5712_Addr,0x59,Reg59,20);
	
  TAS5711WriteReg(TAS5712_Addr,0x31,Reg31,20);//d
	TAS5711WriteReg(TAS5712_Addr,0x32,Reg32,20);//d
	TAS5711WriteReg(TAS5712_Addr,0x33,Reg33,20);
  TAS5711WriteReg(TAS5712_Addr,0x34,Reg34,20);
  TAS5711WriteReg(TAS5712_Addr,0x35,Reg35,20);
  TAS5711WriteReg(TAS5712_Addr,0x36,Reg36,20);
  TAS5711WriteReg(TAS5712_Addr,0x5a,Reg5a,20);//d
	TAS5711WriteReg(TAS5712_Addr,0x5b,Reg5b,20);//d
	TAS5711WriteReg(TAS5712_Addr,0x5c,Reg5c,20);//d
	TAS5711WriteReg(TAS5712_Addr,0x5d,Reg5d,20);//d


	
//	TAS5711WriteReg(TAS5712_Addr,0x33,Reg33,20);




	
//DRCs
	TAS5711WriteReg(TAS5712_Addr,0x3a,Reg_3a,8);
	TAS5711WriteReg(TAS5712_Addr,0x3b,Reg_3b,8);
	TAS5711WriteReg(TAS5712_Addr,0x3c,Reg_3c,8);
	TAS5711WriteReg(TAS5712_Addr,0x40,Reg_40,4);
	TAS5711WriteReg(TAS5712_Addr,0x41,Reg_41,4);
	TAS5711WriteReg(TAS5712_Addr,0x42,Reg_42,4);
	TAS5711WriteReg(TAS5712_Addr,0x46,Reg_46,4);
	TAS5711WriteReg(TAS5712_Addr,0x39,Reg_39,8);
	TAS5711WriteReg(TAS5712_Addr,0x3d,Reg_3d,8);
	TAS5711WriteReg(TAS5712_Addr,0x3e,Reg_3e,8);
	TAS5711WriteReg(TAS5712_Addr,0x3f,Reg_3f,8);
	TAS5711WriteReg(TAS5712_Addr,0x43,Reg_43,4);
	TAS5711WriteReg(TAS5712_Addr,0x44,Reg_44,4);
	TAS5711WriteReg(TAS5712_Addr,0x45,Reg_45,4);
	TAS5711WriteReg(TAS5712_Addr,0x46,Reg_46,4);
	TAS5711WriteReg(TAS5712_Addr,0x52,Reg_52,12);
	TAS5711WriteReg(TAS5712_Addr,0x60,Reg60,8);
	TAS5711WriteReg(TAS5712_Addr,0x53,Reg53,16);//
	TAS5711WriteReg(TAS5712_Addr,0x54,Reg54,16);//
	TAS5711WriteReg(TAS5712_Addr,0x56,Reg_56,4);
	TAS5711WriteReg(TAS5712_Addr,0x57,Reg_57,4);
	TAS5711WriteReg(TAS5712_Addr,0x51,Reg51,12);
	TAS5711WriteReg(TAS5712_Addr,0x55,Reg55,12);
	TAS5711WriteReg(TAS5712_Addr,0x52,Reg52,12);
	TAS5711WriteReg(TAS5712_Addr,0x07,Reg_07,1);
	TAS5711WriteReg(TAS5712_Addr,0x05,Reg_05,1);  
  
  
  
  TAS5711WriteReg(TAS5712_Addr,0x06,Reg_06_unMute,1); 
  
  
  //*******************************************************
  
  
   close(i2cfd);
   i2cfd=-1; 
    return 0;  
}

BYTE TAS5711WriteReg(BYTE cDevAddr, BYTE cReg, const BYTE *cData, int cLength)
{
	//  usleep(200000);
	  i2c_write_reg(I2C, (BYTE *)cData, cDevAddr, cReg, cLength);  
	 
	  return 0;
}
