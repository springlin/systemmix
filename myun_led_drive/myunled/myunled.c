/*
 * myunled device c file
 * 
 * Copyright (C) 2013 brantyou Open Source Project
 * Copyright (C) 2013,2013 brantyou Inc.
 *
 * Author: brantyou <brantyou@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include <linux/init.h>
#include <linux/module.h>
#include <linux/types.h>
#include <linux/fs.h>
#include <linux/proc_fs.h>
#include <linux/device.h>
#include <asm/uaccess.h>
#include <linux/slab.h>


#include <linux/module.h>
#include <linux/init.h>
#include <linux/device.h>
#include <linux/cdev.h>
#include <linux/fs.h>
#include <asm/uaccess.h>
#include <asm/io.h>

#include "myunled.h"

//#define DEG

#define STB 3
#define CLK 0
#define DIO 1
#define GPIO1 4
#define GPIO2 5
#define HUDIR 6
#define HU 7

#define WRITE_REG(Addr, Value) ((*(volatile unsigned long *)(Addr)) = (Value))
#define READ_REG(Addr)         (*(volatile unsigned long *)(Addr))


static volatile unsigned long *GPIO5_REG = NULL;
static volatile unsigned long *GPIO5_DIR = NULL;
static volatile unsigned long *GPIO5_IE = NULL;


static volatile unsigned long *GPIO5_0_DAT = NULL; //CLK
static volatile unsigned long *GPIO5_1_DAT = NULL; //DIO
static volatile unsigned long *GPIO5_3_DAT = NULL; //STB


static volatile unsigned long *GPIO3_4_CTRLREG = NULL;//0xF8A21070
static volatile unsigned long *GPIO3_5_CTRLREG = NULL;//0xF8A21074
static volatile unsigned long *GPIO3_DIR = NULL;
static volatile unsigned long *GPIO3_IE = NULL;
static volatile unsigned long *GPIO3_4_DAT = NULL;
static volatile unsigned long *GPIO3_5_DAT = NULL;

static volatile unsigned long *GPIO1_6_CTRLREG = NULL;//0xF8A21038
static volatile unsigned long *GPIO1_DIR = NULL;
static volatile unsigned long *GPIO1_IE = NULL;
static volatile unsigned long *GPIO1_6_DAT = NULL; 

// * Master and slave devices number variables
static int myunled_major = 0;
static int myunled_minor = 0;

// * device types and device variables
static struct class* myunled_class = NULL;
static struct myunled_android_dev* myunled_dev = NULL;

// * traditional method of operation of the device file
static int     myunled_open(struct inode* inode, struct file* flip);
static int     myunled_release(struct inode* inode, struct file* flip);
static ssize_t myunled_read(struct file* flip, char __user* buf, size_t count, loff_t* f_pos);
static ssize_t myunled_write(struct file* flip, const char __user* buf, size_t count, loff_t* f_pos);
static int     myunled_ioctl(struct file *filp, unsigned int cmd, unsigned long arg);



// * the method of operation of the device file table
static struct file_operations myunled_fops = {
	.owner = THIS_MODULE,
	.open = myunled_open,
	.release = myunled_release,
	.read = myunled_read,
	.write = myunled_write,
	.unlocked_ioctl = myunled_ioctl,
};




// * open the device methods
static int myunled_open(struct inode* inode, struct file* flip)
{
	struct myunled_android_dev* dev;

	
	// save the device struct to the private area
	dev = container_of(inode->i_cdev, struct myunled_android_dev, dev);
	flip->private_data = dev;


  
  WRITE_REG(GPIO5_REG, ( ((~0xC) & READ_REG(GPIO5_REG)) | 0x80 )  );
  WRITE_REG(GPIO5_DIR,  (READ_REG(GPIO5_REG)| 0x0B )  );
  WRITE_REG(GPIO5_IE,   (READ_REG(GPIO5_IE)&(~0x0B))  );
  
  printk(KERN_ALERT"[myunled]: myunled_open().\n");
	return 0;
}

// * release
static int myunled_release(struct inode* inode, struct file* filp)
{
	printk(KERN_ALERT"[myunled]: myunled_release().\n");
	return 0;
}

// * read
static ssize_t myunled_read(struct file* filp, char __user* buf, size_t count, loff_t* f_pos)
{
	ssize_t err = 0;
	struct myunled_android_dev* dev = filp->private_data;

	printk(KERN_ALERT"[myunled]: myunled_read().\n");
	// async access
	if(down_interruptible( &(dev->sem) )){
		return -ERESTARTSYS;
	}

	if(count < sizeof(dev->val) ){
		goto out;
	}

	// 
	if(copy_to_user(buf, &(dev->val), sizeof(dev->val) )){
		err = -EFAULT;
		goto out;
	}

	err = sizeof(dev->val);

out:
	up(&(dev->sem));

	return err;
}

// * write
static ssize_t myunled_write(struct file* filp, const char __user* buf, size_t count, loff_t* f_pos)
{
	struct myunled_android_dev* dev = filp->private_data;
	ssize_t err = 0;
	
	printk(KERN_ALERT"[myunled]: myunled_write().\n");
	// async access
	if(down_interruptible( &(dev->sem) )){
		return -ERESTARTSYS;
	}
	
	if(count != sizeof(dev->val) ){
		goto out;
	}
	
	// save the buffer value to device registers
	if( copy_from_user( &(dev->val), buf, count) ){
		err = -EFAULT;
		goto out;
	}
	
	err = sizeof(dev->val);

out:
	up(&(dev->sem));
	return err;
}
static int myunled_ioctl(struct file *filp, unsigned int cmd, unsigned long arg){
	
	
	  int var=0;
	  copy_from_user(&var,(int*)arg, sizeof(int));

#ifdef DEG	  
	  printk("*********>[myunled]: myunled_ioctl Cmd%d  Arg%d.\n", cmd, var);
#endif	  
	  
	  switch(cmd){
	  	
	  	  case DIO:	
#ifdef DEG	  	  	
	  	  	        printk("DIO\n");
	  	  	        printk("READ_REG(Addr)%x\n",  READ_REG(GPIO5_1_DAT));  
#endif	  	  	           	
	  	  	    	  WRITE_REG(GPIO5_1_DAT,   (var>0?0xFF:0x0)  );
	  	  	    	 		
	      break;	  	
	  	  case CLK:
#ifdef DEG		  	  	
	  	  		  	  printk("CLK\n");
	  	  	        printk("READ_REG(Addr)%x\n",  READ_REG(GPIO5_0_DAT)); 
#endif	  		  	  	            	
	  	  	    	  WRITE_REG(GPIO5_0_DAT,   (var>0?0xFF:0x0)  );
	  	  	
	  	  	
	      break;	
	      case STB:
#ifdef DEG		  	  	
	  	  	        printk("STB\n");
	  	  	        printk("READ_REG(Addr)%x\n",  READ_REG(GPIO5_3_DAT));   
#endif	  		  	  	          	
	  	  	    	  WRITE_REG(GPIO5_3_DAT,   (var>0?0xFF:0x0)  );
	  	  	
	  	  	
	      break;		  	
	  	  default:
	  	  	       
	  	  	       printk("unkown cmd\n");
	  	  	
	  	  break; 	
	  	
	  	
	  	
	  }
	  return 0;
}






// * init device
static int __myunled_setup_dev(struct myunled_android_dev* dev)
{
	int err;
	dev_t devno = MKDEV(myunled_major, myunled_minor);
	
	memset(dev, 0, sizeof(struct myunled_android_dev) );
	
	cdev_init( &(dev->dev), &myunled_fops);
	dev->dev.owner = THIS_MODULE;
	dev->dev.ops = &myunled_fops;
	
	// registe charater device
	err = cdev_add( &(dev->dev), devno, 1);
	if(err){
		return err;
	}
	

	dev->val = 0;
	
	return 0;
}

// * load module
static int __init myunled_init(void)
{
	int err = -1;
	dev_t dev = 0;
	struct device* temp = NULL;
	
	printk(KERN_ALERT"[myunled]: Initializing myunled device.\n");
	
	// malloc master and slave device number
	err = alloc_chrdev_region( &dev, 0, 1, MYUNLED_DEVICE_NODE_NAME);
	if(err < 0){
		printk(KERN_ALERT"[myunled]: Failed to alloc char dev region.\n");
		goto fail;
	}
	
	myunled_major = MAJOR(dev);
	myunled_minor = MINOR(dev);
	
	// alloc myunled device struct valiriable
	myunled_dev = kmalloc( sizeof(struct myunled_android_dev), GFP_KERNEL);
	if(!myunled_dev){
		err = -ENOMEM;
		printk(KERN_ALERT"[myunled]: Failed to alloc myunled_dev.\n");
		goto unregister;
	}
	
	// init device
	err = __myunled_setup_dev(myunled_dev);
	if(err){
		printk(KERN_ALERT"[myunled]: Failed to setup dev:%d.\n", err);
		goto cleanup;
	}

	// create device type directory myunled on /sys/class/
	myunled_class = class_create(THIS_MODULE, MYUNLED_DEVICE_CLASS_NAME);
	if(IS_ERR(myunled_class)){
		err = PTR_ERR(myunled_class);
		printk(KERN_ALERT"[myunled]: Failed to create myunled class.\n");
		goto destroy_cdev;
	}
	
	// create device file myunled on /dev/ and /sys/class/myunled
	temp = device_create(myunled_class, NULL, dev, "%s", MYUNLED_DEVICE_FILE_NAME);
	if(IS_ERR(temp)){
		err = PTR_ERR(temp);
		printk(KERN_ALERT"Failed to create myunled device.\n");
		goto destroy_class;
	}

	
	dev_set_drvdata(temp, myunled_dev);

	
	GPIO5_REG	= ioremap(0xF8000000+0x0044,4);
	GPIO5_DIR	= ioremap(0xF8000000+0x4400,4);	
	GPIO5_IE	= ioremap(0xF8000000+0x4410,4);
	
	
	GPIO5_0_DAT	= ioremap(0xF8000000+0x4004,4);	//CLK
	GPIO5_1_DAT	= ioremap(0xF8000000+0x4008,4);//DIO
	GPIO5_3_DAT	= ioremap(0xF8000000+0x4020,4);//STB
	
		
	
	printk(KERN_ALERT"[myunled]: Successed to initialize myunled device.\n");
	return 0;
destroy_device:
	device_destroy(myunled_class, dev);

destroy_class:
	class_destroy(myunled_class);	

destroy_cdev:
	cdev_del(&myunled_dev->dev);

cleanup:
	kfree(myunled_dev);

unregister:
	unregister_chrdev_region(MKDEV(myunled_major, myunled_minor), 1);

fail:
	return err;
}

// * unload module
static void __exit myunled_exit(void)
{
	dev_t devno = MKDEV(myunled_major, myunled_minor);
	
	printk(KERN_ALERT"[myunled]: Destroy myunled device.\n");
	
	// destroy device type and device
	if(myunled_class){
		device_destroy(myunled_class, MKDEV(myunled_major, myunled_minor) );
		class_destroy(myunled_class);
	}
	
	// delete character device and release device memory
	if(myunled_dev){
		cdev_del(&(myunled_dev->dev) );
		kfree(myunled_dev);
	}
	
	// destroy device number
	unregister_chrdev_region(devno, 1);
	
	
  iounmap(GPIO5_REG);
	iounmap(GPIO5_DIR);
	iounmap(GPIO5_IE);
	iounmap(GPIO5_0_DAT);
	iounmap(GPIO5_1_DAT);
	iounmap(GPIO5_3_DAT);
	
}

MODULE_LICENSE("GPL");
MODULE_DESCRIPTION("Myun LED Device");

module_init(myunled_init);
module_exit(myunled_exit);