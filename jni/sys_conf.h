#ifndef _SYS_CONF_H_
#define _SYS_CONF_H_

#ifdef __cplusplus
extern "C" {
#endif

#define PPMGR_IOC_MAGIC         'P'
#define PPMGR_IOC_ENABLE_PP     _IOW(PPMGR_IOC_MAGIC,0X01,unsigned int)

#define MODE_3D_DISABLE         0x00000000
#define MODE_3D_ENABLE          0x00000001
#define MODE_AUTO               0x00000002
#define MODE_2D_TO_3D           0x00000004
#define MODE_LR                 0x00000008
#define MODE_BT                 0x00000010
#define MODE_LR_SWITCH          0x00000020
#define MODE_FIELD_DEPTH        0x00000040
#define MODE_3D_TO_2D_L         0x00000080
#define MODE_3D_TO_2D_R         0x00000100

/*
Description: disable fb0 layer      
Comments:  disable osd display.          
*/
int SYS_disable_osd0(void);
/*
Description: enable fb0 layer      
Comments:  enable osd display.          
*/
int SYS_enable_osd0(void);


/*
Description:set colorkey      
Comments: refer to rgb565 as current color space.  
*/
int SYS_enable_colorkey(short key_rgb565);

/*
Description:disable colorkey      
Comments: clearall coloreky. 
*/
int SYS_disable_colorkey(void);   

int SYS_set_black_policy(int blackout);

int SYS_get_black_policy();

int SYS_set_tsync_enable(int enable);

int SYS_set_global_alpha(int alpha);
int SYS_get_osdbpp();

int SYS_set_video_preview_win(int x,int y,int w,int h);
int SYS_set_3D_mode(int mode);

/*
Description:set fullscreen
Comments: default 1280*720
*/
int SYS_set_video_fullscreen();


#ifdef __cplusplus
}
#endif

#endif //_SYS_CONF_H_

