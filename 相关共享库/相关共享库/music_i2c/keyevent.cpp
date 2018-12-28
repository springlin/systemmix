#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/inotify.h>
#include <sys/limits.h>
#include <sys/poll.h>
#include <linux/input.h>
#include <errno.h>

#include "keyevent.h"
#include "jniUtils.h"

extern void OnKeyMsg(int keycode , int action);


enum {
    PRINT_DEVICE_ERRORS     = 1U << 0,
    PRINT_DEVICE            = 1U << 1,
    PRINT_DEVICE_NAME       = 1U << 2,
    PRINT_DEVICE_INFO       = 1U << 3,
    PRINT_VERSION           = 1U << 4,
    PRINT_POSSIBLE_EVENTS   = 1U << 5,
    PRINT_INPUT_PROPS       = 1U << 6,
    PRINT_HID_DESCRIPTOR    = 1U << 7,

    PRINT_ALL_INFO          = (1U << 8) - 1,

    PRINT_LABELS            = 1U << 16,
};


GetEvent::GetEvent(){
}

GetEvent::~GetEvent(){
}





int GetEvent::open_device(const char *device, int print_flags)
{
    int version;
    int fd;
    struct pollfd *new_ufds;
    char **new_device_names;
    char name[80];
    char location[80];
    char idstr[80];
    struct input_id id;

    fd = open(device, O_RDWR);
    if(fd < 0) {
       
        LOGI("could not open %s, %s\n", device, strerror(errno));
        return -1;
    }
    
    if(ioctl(fd, EVIOCGVERSION, &version)) {
       
        LOGI("could not get driver version for %s, %s\n", device, strerror(errno));
        return -1;
    }
    if(ioctl(fd, EVIOCGID, &id)) {
      
        LOGI("could not get driver id for %s, %s\n", device, strerror(errno));
        return -1;
    }
    name[sizeof(name) - 1] = '\0';
    location[sizeof(location) - 1] = '\0';
    idstr[sizeof(idstr) - 1] = '\0';
    if(ioctl(fd, EVIOCGNAME(sizeof(name) - 1), &name) < 1) {
        LOGI("could not get device name for %s, %s\n", device, strerror(errno));
        name[0] = '\0';
    }
    if(ioctl(fd, EVIOCGPHYS(sizeof(location) - 1), &location) < 1) {
        LOGI("could not get location for %s, %s\n", device, strerror(errno));
        location[0] = '\0';
    }
    if(ioctl(fd, EVIOCGUNIQ(sizeof(idstr) - 1), &idstr) < 1) {
        LOGI("could not get idstring for %s, %s\n", device, strerror(errno));
        idstr[0] = '\0';
    }

//    new_ufds = (struct pollfd *)realloc(ufds, sizeof(ufds[0]) * (nfds + 1));
//    if(new_ufds == NULL) {
//        LOGI("out of memory\n");
//        return -1;
//    }
//    ufds = new_ufds;
//    new_device_names = (char **)realloc(device_names, sizeof(device_names[0]) * (nfds + 1));
//    if(new_device_names == NULL) {
//        LOGI("out of memory\n");
//        return -1;
//    }
//    device_names = new_device_names;


    ufds[nfds].fd = fd;
    ufds[nfds].events = POLLIN;
    device_names[nfds] = strdup(device);
    nfds++;

    return 0;
}

int GetEvent::close_device(const char *device, int print_flags)
{
    int i;
    for(i = 1; i < nfds; i++) {
        if(strcmp(device_names[i], device) == 0) {
            int count = nfds - i - 1;
            if(print_flags & PRINT_DEVICE)
                printf("remove device %d: %s\n", i, device);
            free(device_names[i]);
            memmove(device_names + i, device_names + i + 1, sizeof(device_names[0]) * count);
            memmove(ufds + i, ufds + i + 1, sizeof(ufds[0]) * count);
            nfds--;
            return 0;
        }
    }
    if(print_flags & PRINT_DEVICE_ERRORS)
        LOGI("remote device: %s not found\n", device);
    return -1;
}

int GetEvent::read_notify(const char *dirname, int nfd, int print_flags)
{
    int res;
    char devname[PATH_MAX];
    char *filename;
    char event_buf[512];
    int event_size;
    int event_pos = 0;
    struct inotify_event *event;

    res = read(nfd, event_buf, sizeof(event_buf));
    if(res < (int)sizeof(*event)) {
        if(errno == EINTR)
            return 0;
        LOGI("could not get event, %s\n", strerror(errno));
        return 1;
    }
    //printf("got %d bytes of event information\n", res);

    strcpy(devname, dirname);
    filename = devname + strlen(devname);
    *filename++ = '/';

    while(res >= (int)sizeof(*event)) {
        event = (struct inotify_event *)(event_buf + event_pos);
        //printf("%d: %08x \"%s\"\n", event->wd, event->mask, event->len ? event->name : "");
        if(event->len) {
            strcpy(filename, event->name);
            if(event->mask & IN_CREATE) {
                open_device(devname, print_flags);
            }
            else {
                close_device(devname, print_flags);
            }
        }
        event_size = sizeof(*event) + event->len;
        res -= event_size;
        event_pos += event_size;
    }
    return 0;
}



void* GetEvent::getevent_main(void *device)
{
    int c;
    int i;
    int res;
    int pollres;
    int get_time = 0;
    int print_device = 0;
    char *newline = "\n";
    uint16_t get_switch = 0;
    struct input_event event;
    int version;
    int print_flags = 0;
    int print_flags_set = 0;
    int dont_block = -1;
    int event_count = 0;
    int sync_rate = 0;
    int64_t last_sync_time = 0;
   // const char *device = NULL;
    const char *device_path = "/dev/input";

    opterr = 0;

    nfds = 1;
    ufds = new struct pollfd[2];
    ufds[0].fd = inotify_init();
    ufds[0].events = POLLIN;

    device_names[0]=new char[64];
    device_names[1]=new char[64];
    
    res = open_device((char *)device, print_flags);
    if(res < 0) {
        return NULL;
    }


    printf("start  ....... ");



    while(1) {
    	
    	
        pollres = poll(ufds, nfds, -1);
       // printf("poll %d, returned %d\n", nfds, pollres);
        if(ufds[0].revents & POLLIN) {
            read_notify(device_path, ufds[0].fd, print_flags);
        }
        for(i = 1; i < nfds; i++) {
            if(ufds[i].revents) {
                if(ufds[i].revents & POLLIN) {
                    res = read(ufds[i].fd, &event, sizeof(event));
                    if(res < (int)sizeof(event)) {
                        LOGI("could not get event\n");
                        return 0;
                    }

                    if(event.type==1)OnKeyMsg(event.code , event.value);
                    printf("%04x %04x %08x", event.type, event.code, event.value);
                    LOGI("%04x %04x %08x", event.type, event.code, event.value);
                    if(sync_rate && event.type == 0 && event.code == 0) {
                        int64_t now = event.time.tv_sec * 1000000LL + event.time.tv_usec;
                        if(last_sync_time)
                            printf(" rate %lld", 1000000LL / (now - last_sync_time));
                        last_sync_time = now;
                    }
                    printf("%s", newline);
                    if(event_count && --event_count == 0)
                        return 0;
                }
            }
        }
    }

    return 0;
}
