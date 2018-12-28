#ifndef VALUE
#define VALUE



typedef unsigned char BYTE;
#define TAS5711_Addr	0x1b
#define TAS5712_Addr	0x1a

const BYTE Reg_06_Mute[]	= {0x3f};
const BYTE Reg_06_unMute[]	= {0x00};
//#ifdef DD_202C
const BYTE Reg_1b[]	= {0x00};
const BYTE Reg_06[]	= {0x00};
const BYTE Reg_0a[]	= {0x30};
const BYTE Reg_09[]	= {0x30};
const BYTE Reg_08[]	= {0x30};
const BYTE Reg_14[]	= {0x54};
const BYTE Reg_13[]	= {0xac};
const BYTE Reg_12[]	= {0x54};
const BYTE Reg_11[]	= {0xac};
const BYTE Reg_0e[]	= {0x91};
const BYTE Reg_20[]	= {0x00, 0x10, 0x77, 0x72};	//{0x00, 0x01, 0x77, 0x72,};
const BYTE Reg_10[]	= {0x02};
const BYTE Reg_0b[]	= {0x00};
//const BYTE Reg_10[]	= {0x02};
const BYTE Reg_1c[]	= {0x02};
const BYTE Reg_19[]	= {0x30};
const BYTE Reg_25[]	= {0x01, 0x02, 0x13, 0x45};



// Biquads 
const BYTE Reg_50[]	= {0x00, 0x00, 0x00, 0x10};

const BYTE Reg_29[]	= {	0x00, 0x79, 0x54, 0x91,	0x0f, 0x86, 0x32, 0xb7,
						0x00, 0x3f, 0x66, 0xad,	0x00, 0x79, 0xcd, 0x49,	0x0f, 0xc7, 0x44, 0xc1};

const BYTE Reg_30[]	= {	0x00, 0x79, 0x54, 0x91,	0x0f, 0x86, 0x32, 0xb7,
						0x00, 0x3f, 0x66, 0xad,	0x00, 0x79, 0xcd, 0x49,	0x0f, 0xc7, 0x44, 0xc1};

const BYTE Reg_2a[]	= {	0x00, 0x7d, 0xe3, 0x20,	0x0f, 0x53, 0xa7, 0xe5,
						0x00, 0x36, 0x92, 0xf9,	0x00, 0xac, 0x58, 0x1b,	0x0f, 0xcb, 0x89, 0xe7};

const BYTE Reg_2b[]	= {	0x00, 0x5a, 0xfd, 0xaf,	0x00, 0xb5, 0xfb, 0x5e,
						0x00, 0x5a, 0xfd, 0xaf,	0x0f, 0x57, 0x15, 0x75,	0x0f, 0xbc, 0xf3, 0xd0};

const BYTE Reg_2c[]	= {	0x00, 0x7f, 0x8f, 0x3c,	0x0f, 0x00, 0xe1, 0x87,
						0x00, 0x7f, 0x8f, 0x3c,	0x00, 0xff, 0x1d, 0x5d,	0x0f, 0x80, 0xe0, 0x6b};

const BYTE Reg_2d[]	= {	0x00, 0x80, 0x46, 0xBD, 0x0F, 0x00, 0xB9, 0xD7,
	          0x00, 0x7F, 0x03, 0x77, 0x00, 0xFF, 0x46, 0x29, 0x0F, 0x80, 0xB5, 0xCB};

const BYTE Reg_2e[]	= {	0x00, 0x7f, 0x90, 0x25,	0x0f, 0x02, 0x70, 0x34,
						0x00, 0x7e, 0x11, 0xac,	0x00, 0xfd, 0x8f, 0xcc,	0x0f, 0x82, 0x5e, 0x2e};
	
const BYTE Reg_2f[]	= {	0x00, 0x7f, 0xf8, 0x54,	0x0f, 0x00, 0x65, 0xc7,
						0x00, 0x7f, 0xa6, 0xf4,	0x00, 0xff, 0x9a, 0x39,	0x0f, 0x80, 0x60, 0xb8};	

const BYTE Reg_58[]	= {	0x00, 0x80, 0x06, 0x0e,	0x0f, 0x00, 0x43, 0x6f,
						0x00, 0x7f, 0xb9, 0xaa,	0x00, 0xff, 0xbc, 0x91,	0x0f, 0x80, 0x40, 0x47};	


const BYTE Reg_59[]	= {	0x00, 0x7D, 0x2C, 0xFB, 0x0F, 0x19, 0x95, 0xB9,
	          0x00, 0x74, 0x17, 0x76, 0x00, 0xE6, 0x6A, 0x47, 0x0F, 0x8E, 0xBB, 0x8E};	


const BYTE Reg_31[]	= {	0x00, 0x7d, 0xe3, 0x20,	0x0f, 0x53, 0xa7, 0xe5,
						0x00, 0x36, 0x92, 0xf9,	0x00, 0xac, 0x58, 0x1b,	0x0f, 0xcb, 0x89, 0xe7};

const BYTE Reg_32[]	= {	0x00, 0x5a, 0xfd, 0xaf,	0x00, 0xb5, 0xfb, 0x5e,
						0x00, 0x5a, 0xfd, 0xaf,	0x0f, 0x57, 0x15, 0x75,	0x0f, 0xbc, 0xf3, 0xd0};

const BYTE Reg_33[]	= {	0x00, 0x7f, 0x8f, 0x3c,	0x0f, 0x00, 0xe1, 0x87,
						0x00, 0x7f, 0x8f, 0x3c,	0x00, 0xff, 0x1d, 0x5d,	0x0f, 0x80, 0xe0, 0x6b};

const BYTE Reg_34[]	= {	0x00, 0x80, 0x46, 0xBD, 0x0F, 0x00, 0xB9, 0xD7, 
	          0x00, 0x7F, 0x03, 0x77, 0x00, 0xFF, 0x46, 0x29, 0x0F, 0x80, 0xB5, 0xCB};

const BYTE Reg_35[]	= {	0x00, 0x7f, 0x90, 0x25,	0x0f, 0x02, 0x70, 0x34,
						0x00, 0x7e, 0x11, 0xac,	0x00, 0xfd, 0x8f, 0xcc,	0x0f, 0x82, 0x5e, 0x2e};

const BYTE Reg_36[]	= {	0x00, 0x7f, 0xf8, 0x54,	0x0f, 0x00, 0x65, 0xc7,
						0x00, 0x7f, 0xa6, 0xf4,	0x00, 0xff, 0x9a, 0x39,	0x0f, 0x80, 0x60, 0xb8};

const BYTE Reg_5c[]	= {	0x00, 0x80, 0x06, 0x0e,	0x0f, 0x00, 0x43, 0x6f,
						0x00, 0x7f, 0xb9, 0xaa,	0x00, 0xff, 0xbc, 0x91,	0x0f, 0x80, 0x40, 0x47};

const BYTE Reg_5d[]	= {	0x00, 0x7D, 0x2C, 0xFB, 0x0F, 0x19, 0x95, 0xB9,
	          0x00, 0x74, 0x17, 0x76, 0x00, 0xE6, 0x6A, 0x47, 0x0F, 0x8E, 0xBB, 0x8E};

const BYTE Reg_5a[]	= {	0x00, 0x00, 0x05, 0x83,	0x00, 0x00, 0x0b, 0x06,
						0x00, 0x00, 0x05, 0x83,	0x00, 0xfb, 0x42, 0xc1,	0x0f, 0x84, 0xa7, 0x33};

const BYTE Reg_5b[]	= {	0x00, 0x80, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00,
						0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00};

// DRC


const BYTE Reg_3a[]	= {	0x00, 0x00, 0x0a, 0xeb,	0x00, 0x7f, 0xf5, 0x14};
const BYTE Reg_3b[]	= {	0x00, 0x00, 0x1b, 0x4b,	0x00, 0x7f, 0xe4, 0xb4};
const BYTE Reg_3c[]	= {	0x00, 0x00, 0x05, 0x75,	0x00, 0x7f, 0xfa, 0x8a};

const BYTE Reg_40[]	= {0xfd, 0x42, 0x68, 0xa7};
const BYTE Reg_41[]	= {0x0f, 0xa4, 0x92, 0x4f};
const BYTE Reg_42[]	= {0x00, 0x08, 0x42, 0x10};
const BYTE Reg_46[]	= {0x00, 0x00, 0x00, 0x01};
const BYTE Reg_39[]	= {	0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00};

const BYTE Reg_3d[]	= {	0x00, 0x00, 0x22, 0x1c,	0x00, 0x7f, 0xdd, 0xe1};
const BYTE Reg_3e[]	= {	0x00, 0x2b, 0x9e, 0x00,	0x00, 0x54, 0x61, 0xfe};
const BYTE Reg_3f[]	= {	0x00, 0x00, 0x22, 0x1c,	0x00, 0x7f, 0xdd, 0xe1};

const BYTE Reg_43[]	= {0xfd, 0x82, 0x30, 0x98};
const BYTE Reg_44[]	= {0x0f, 0xaa, 0xaa, 0xac};
const BYTE Reg_45[]	= {0x00, 0x08, 0x42, 0x10};
//const BYTE Reg_46[]	= {0x00, 0x00, 0x00, 0x00};

const BYTE Reg_52[]	= {	0x00, 0x80, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00};
const BYTE Reg_60[]	= {	0x00, 0x00, 0x00, 0x00,	0x00, 0x80, 0x00, 0x00};

const BYTE Reg_53[]	= {	0x00, 0x80, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00,
						0x00, 0x00, 0x00, 0x00,	0x00, 0x80, 0x00, 0x00};

const BYTE Reg_54[]	= {	0x00, 0x80, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00,
						0x00, 0x00, 0x00, 0x00,	0x00, 0x80, 0x00, 0x00};

const BYTE Reg_56[]	= {0x00, 0x80, 0x00, 0x00};
const BYTE Reg_57[]	= {0x00, 0x02, 0x00, 0x00};

const BYTE Reg_51[]	= {	0x00, 0x80, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00};
const BYTE Reg_55[]	= {	0x00, 0x80, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00};
//const BYTE Reg_52[]	= {	0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00};

const BYTE Reg_05[]	= {0x00};
const BYTE Reg_07[]		= {0x36};

const BYTE t_Volume[ 32 + 1]=
{
	0xff,		// 0
	0x80,		// -40dB
	0x7a,		// V2

	0x74,
	0x6e,
	0x68,		// V5
	
	0x62,
	0x5c,
	0x56,		// V8
	
	0x50,
	0x4c,
	0x48,		// V11

	0x44,		// V12 -10dB
	0x41,
	0x3e,		// V14
	
	0x3c,
	0x3a,
	0x38,		// V17
	
	0x36,
	0x34,
	0x32,		// V20
	
	0x30,
	0x2e,
	0x2c,		// V23
	
	0x2a,
	0x28,
	0x26,		// V26
	
	0x24,
	0x22,
	0x20,		// V29
	
	0x1e,
	0x1c,
	0x18		// V32 14dB
	
};



#endif