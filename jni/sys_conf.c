#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include  <linux/fb.h>
#include <errno.h>
#include "sys_conf.h"
#include <cutils/log.h>
#include <dlfcn.h> 
#include <sys/ioctl.h>
#include <fcntl.h>

static int set_fb0_blank(int blank)
{
    int fd;
    char *path = "/sys/class/graphics/fb0/blank" ;   
    char  bcmd[16];
    memset(bcmd,0,16);
    fd=open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);
    if(fd>=0){
        sprintf(bcmd,"%d",blank);
        write(fd,bcmd,strlen(bcmd));
        close(fd);
        return 0;
    }
    return -1;                  
}

int SYS_disable_osd0(void)
{
    set_fb0_blank(1);   
    return 0;
}

int SYS_enable_osd0(void)
{
    set_fb0_blank(0);
    return 0;
}
//============================================
//about colorkey

#ifndef FBIOPUT_OSD_SRCCOLORKEY
#define  FBIOPUT_OSD_SRCCOLORKEY    0x46fb
#endif

#ifndef FBIOPUT_OSD_SRCKEY_ENABLE
#define  FBIOPUT_OSD_SRCKEY_ENABLE  0x46fa
#endif


#ifndef FBIOPUT_OSD_SET_GBL_ALPHA
#define  FBIOPUT_OSD_SET_GBL_ALPHA  0x4500
#endif

int SYS_enable_colorkey(short key_rgb565)
{
    int ret = -1;
    int fd_fb0 = open("/dev/graphics/fb0", O_RDWR);
    if (fd_fb0 >= 0) {
        uint32_t myKeyColor = key_rgb565;
        uint32_t myKeyColor_en = 1;
        printf("enablecolorkey color=%#x\n", myKeyColor);
        ret = ioctl(fd_fb0, FBIOPUT_OSD_SRCCOLORKEY, &myKeyColor);
        ret += ioctl(fd_fb0, FBIOPUT_OSD_SRCKEY_ENABLE, &myKeyColor_en);
        close(fd_fb0);
    }
    return ret;
}
int SYS_disable_colorkey(void)
{
    int ret = -1;
    int fd_fb0 = open("/dev/graphics/fb0", O_RDWR);
    if (fd_fb0 >= 0) {
        uint32_t myKeyColor_en = 0;
        ret = ioctl(fd_fb0, FBIOPUT_OSD_SRCKEY_ENABLE, &myKeyColor_en);
        close(fd_fb0);
    }
    return ret;

}


int SYS_set_black_policy(int blackout)
{
    int fd;
    int bytes;
    char *path = "/sys/class/video/blackout_policy";
    char  bcmd[16];
    fd=open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);
    if(fd>=0)
    {
        sprintf(bcmd,"%d",blackout);
        bytes = write(fd,bcmd,strlen(bcmd));
        close(fd);
        return 0;
    }
    return -1;
    
}

int SYS_get_black_policy()
{
    int fd;
    int black_out = 0;
    char *path = "/sys/class/video/blackout_policy";
    char  bcmd[16];
    fd=open(path, O_RDONLY);
    if(fd>=0)
    {       
        read(fd,bcmd,sizeof(bcmd));       
        black_out = strtol(bcmd, NULL, 16);       
        black_out &= 0x1;
        close(fd);      
    }
    return black_out;

}

int SYS_set_tsync_enable(int enable)
{
    int fd;
    char *path = "/sys/class/tsync/enable";    
    char  bcmd[16];
    fd=open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);
    if(fd>=0)
    {
        sprintf(bcmd,"%d",enable);
        write(fd,bcmd,strlen(bcmd));
        close(fd);
        return 0;
    }
    return -1;
    
}

int SYS_set_global_alpha(int alpha){
    int ret = -1;   
    int fd_fb0 = open("/dev/graphics/fb0", O_RDWR); 
    if (fd_fb0 >= 0) {   
        uint32_t myAlpha = alpha;  
        ret = ioctl(fd_fb0, FBIOPUT_OSD_SET_GBL_ALPHA, &myAlpha);    
        close(fd_fb0);   

    }   
    return ret;
}
int SYS_get_osdbpp(){
       int ret = 16;   
       int fd_fb0 = open("/dev/graphics/fb0", O_RDWR);   
       if (fd_fb0 >= 0) {     
            struct fb_var_screeninfo vinfo; 
            ioctl(fd_fb0, FBIOGET_VSCREENINFO, &vinfo);  
            close(fd_fb0);      
            ret = vinfo.bits_per_pixel;
       }   
       return ret;
}
//===========================================
int SYS_set_video_preview_win(int x,int y,int w,int h)
{
    int fd;
    char *path = "/sys/class/video/axis";    
    char  bcmd[32];

    memset(bcmd,0,sizeof(bcmd));
    
    fd=open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);
    if(fd>=0)
    {
        
        sprintf(bcmd,"%d %d %d %d",x,y,w,h);
        write(fd,bcmd,strlen(bcmd));
        close(fd);
        return 0;
    }
    return -1;    
}

#define VIDEO_SCREEN_W 1280
#define VIDEO_SCREEN_H 720

int SYS_set_video_fullscreen(){
    int fd;
    char *path = "/sys/class/video/axis";    
    char  bcmd[32];

    memset(bcmd,0,sizeof(bcmd));
    
    fd=open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);
    if(fd>=0)
    {
        
        sprintf(bcmd,"%d %d %d %d",0,0,VIDEO_SCREEN_W,VIDEO_SCREEN_H);
        write(fd,bcmd,strlen(bcmd));
        close(fd);
        return 0;
    }
    return -1;      
}

static int AddVfmPath(char *path)
{
    FILE * fp;
    
    fp = fopen("/sys/class/vfm/map", "w");
    
    if(fp != NULL) {
        fprintf(fp, "%s", path);
    } else {
        LOGE("VideoPlayer open /sys/class/vfm/map ERROR(%s)!!\n", strerror(errno));
        return -1;
    }

    fclose(fp);
    return 0;
}
static int RmVfmDefPath(void)
{
    int fd, ret;
    char str[]="rm default";
    
    fd = open("/sys/class/vfm/map", O_RDWR);

    if(fd < 0) {
        LOGE("VideoPlayer open /sys/class/vfm/map ERROR(%s)!!\n",strerror(errno));
            close(fd);
            return 0;
    } else {
        ret = write(fd, str, sizeof(str));
    }
    close(fd);
    return ret;
}

static int Open3DPpmgr(int commd)
{
    int ppmgrfd = open("/dev/ppmgr", O_RDWR);

    if(ppmgrfd < 0) {

        LOGE("VideoPlayer open ppmgr, error (%s)\n", strerror(errno));
        return ppmgrfd;
    }
    int ret = -1;
    switch((int)commd)
    {
        case 0:
            ret = ioctl(ppmgrfd, PPMGR_IOC_ENABLE_PP, MODE_3D_DISABLE);
            #ifdef LOGD_3D_FUNCTION
            LOGD("VideoPlayer 3D fucntion (0: Disalbe!!)\n");
            #endif
            break;
        case 1:
            ret = ioctl(ppmgrfd, PPMGR_IOC_ENABLE_PP, (MODE_3D_ENABLE|MODE_LR));
            #ifdef LOGD_3D_FUNCTION
            LOGD("VideoPlayer 3D fucntion (4: L/R!!)\n");
            #endif
            break;
        case 2:
            ret = ioctl(ppmgrfd, PPMGR_IOC_ENABLE_PP, (MODE_3D_ENABLE|MODE_BT));
            #ifdef LOGD_3D_FUNCTION
            LOGD("=VDIN CPP=> 3D fucntion (5: B/T!!)\n");
            #endif
            break;
        case 3:
            ret = ioctl(ppmgrfd, PPMGR_IOC_ENABLE_PP, (MODE_3D_ENABLE|MODE_LR_SWITCH));
            #ifdef LOGD_3D_FUNCTION
            LOGD("VideoPlayer 3D fucntion (6: LR SWITCH!!)\n");
            #endif
            break;
        case 4:
            ret = ioctl(ppmgrfd, PPMGR_IOC_ENABLE_PP, (MODE_3D_ENABLE|MODE_3D_TO_2D_L));
            #ifdef LOGD_3D_FUNCTION
            LOGD("VideoPlayer 3D function (8: 3D_TO_2D_L!!)\n");
            #endif
            break;
        case 5:
            ret = ioctl(ppmgrfd, PPMGR_IOC_ENABLE_PP, (MODE_3D_ENABLE|MODE_3D_TO_2D_R));
            #ifdef LOGD_3D_FUNCTION
            LOGD("VideoPlayer 3D function (9: 3D_TO_2D_R!!)\n");
            #endif
            break;
        case 6:
            ret = ioctl(ppmgrfd, PPMGR_IOC_ENABLE_PP, (MODE_3D_ENABLE|MODE_2D_TO_3D));
            #ifdef LOGD_3D_FUNCTION
            LOGD("VideoPlayer 3D fucntion (3: 2D->3D!!)\n");
            #endif
            break;
        case 7:
            ret = ioctl(ppmgrfd, PPMGR_IOC_ENABLE_PP, (MODE_3D_ENABLE|MODE_FIELD_DEPTH));
            #ifdef LOGD_3D_FUNCTION
            LOGD("=VDIN CPP=> 3D function (7: FIELD_DEPTH!!)\n");
            #endif
            break;
    }

    if(ret < 0)
        LOGE("VideoPlayer set 3D function error");
    close(ppmgrfd);
    return ret;
}

int SYS_set_3D_mode(int mode)
{
    RmVfmDefPath();
    int ret = -1;
    switch(mode) {
        case 0:
            ret = AddVfmPath("add default decoder amvideo");
			usleep(100);
            Open3DPpmgr(0);//disable
            break;

        default:
            ret = AddVfmPath("add default decoder ppmgr amvideo");
			usleep(100);
            Open3DPpmgr(mode);
            break;
    }
    return ret;
}
