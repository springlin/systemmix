#include <linux/input.h>

struct label {
    const char *name;
    int value;
};



class GetEvent
{
	  public:
	  	  
	  	  GetEvent();
	  	  ~GetEvent();
	  private: 	  
	  	
	  public:
	      struct pollfd *ufds;
        char *device_names[2];
        int nfds;
	      int open_device(const char *device, int print_flags);
	      int close_device(const char *device, int print_flags);
	      int read_notify(const char *dirname, int nfd, int print_flags);
	      void* getevent_main(void *device);
};	