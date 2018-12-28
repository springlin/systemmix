/*
 * ttt device head file
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


#ifndef	_MYUNLED_ANDROID_H_
#define	_MYUNLED_ANDROID_H_

#include <linux/cdev.h>
#include <linux/semaphore.h>

#define	MYUNLED_DEVICE_NODE_NAME		"myunled"
#define	MYUNLED_DEVICE_FILE_NAME		"myunled"
#define	MYUNLED_DEVICE_PROC_NAME		"myunled"
#define	MYUNLED_DEVICE_CLASS_NAME		"myunled"

struct myunled_android_dev{
	int val;
	struct semaphore sem;
	struct cdev dev;
};

#endif
