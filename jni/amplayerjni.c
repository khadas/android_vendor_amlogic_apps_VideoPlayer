
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <string.h>
#include <syslog.h>
#include <unistd.h>
#include <errno.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <signal.h>
#include <getopt.h>
#include <player.h>
#include <log_print.h>

#ifndef ANDROID
#include <version.h>
#endif

#include <dlfcn.h>

#include <controler.h>



static global_ctrl_para_t player_para;


int osd_blank(char *path,int cmd)
{
	int fd;
	char  bcmd[16];
	fd=open(path, O_CREAT|O_RDWR | O_TRUNC, 0644);
	if(fd>=0)
		{
		sprintf(bcmd,"%d",cmd);
		write(fd,bcmd,strlen(bcmd));
		close(fd);
		return 0;
		}
	return -1;
}

#ifndef ANDROID
int load_extern_lib(global_ctrl_para_t *p,const char *libs)
{
	void* fd;
	static int lib_index=0;
	char *ps,*pp,*op,*buf; 
	
	buf=MALLOC(strlen(optarg)+20);
	pp=optarg;
	op=optarg;
	if(buf==NULL)
		{
		log_print("extern lib memory problem %s\n",libs);
		return -1;
		}
	
	while(op!=NULL)
		{	
			int len;
			pp=strstr(op,",");
			ps=buf+10;
			if(pp!=NULL)
				{
				len=pp-op;
				memcpy(ps,op,len);
				ps[(int)(len)]='\0';
				op=pp+1;
				}
			else
				{
				strcpy(ps,op);
				len=strlen(ps);
				op=NULL;
				}
			if(lib_index>10)
			{
				log_print("too many lib register,10 is max\n");
				goto error;
			}
			if(strstr(ps,".so")==NULL)
			{	
				ps-=3;
				ps[0]='l';
				ps[1]='i';
				ps[2]='b';
				len+=3;
				ps[len++]='.';
				ps[len++]='s';
				ps[len++]='o';
				ps[len++]='\0';
			}
			fd=dlopen(ps,RTLD_NOW);
			if(fd==NULL)
			{
				log_print("open extern lib:%s failed\n",ps);
				goto error;
			}
			log_print("opened extern lib:%s\n",ps);
			p->externlibfd[lib_index++]=fd;	
		
		}
	return 0;
error:	
	free(buf);
	return -1;
}

void release_extern_lib(global_ctrl_para_t *p)
{
	void * fd;
	int i=0;
	while(i<10)
		{
		fd=p->externlibfd[i++];
		if(fd!=NULL)	
			dlclose(fd);
		else
			break;
		}
}

#endif

void  print_usage(char *execname)
{
	log_print("\r\n");
	log_print("usage:\r\n");
	log_print("%s  filename <-v vid> <-a aid> <-n> <-c> <-l>\r\n",execname);
	log_print("\t-v --videoindex :video index,for ts stream\r\n");
	log_print("\t-a --audioindex :auido index,for ts stream\r\n");
	log_print("\t-L --list :the file is a playlist\r\n");
	log_print("\t-m mode :player control mode\r\n");
	log_print("\t\t[shell] shell mode\r\n");
	log_print("\t\t[socket] socket mode\r\n");
	log_print("\t\t[dbus] dbus mode\r\n");
	log_print("\t-e extern<,extern1,extern2> :add extern libraries\r\n");
	log_print("\t-l --loop :loop play\r\n");
	log_print("\t-n --nosound :play without sound\r\n");
	log_print("\t-c --novideo :play without video\r\n");
	log_print("\t-b --background :running in the background\r\n");
	log_print("\t-o --clearosd :clear osd\r\n");
    log_print("\t-k --clearblack :clear blackout\r\n");
    log_print("\t-d --setup the second player\r\n");
	log_print("\r\n");
}

int parser_option(int argc, char *argv[],global_ctrl_para_t *p)
{
	int c;
	int i;
	
	
const char cshort_options[] = "v:a:m:e:Lblncokhu?";

const struct option clong_options[] = {
	{"videoindex", 1, 0, 'v'},
	{"audioindex", 1, 0, 'a'},
	{"mode", 1, 0, 'm'},
	{"extern", 1, 0, 'e'},
	{"playlist", 0, 0, 'L'},	
	{"background", 0, 0, 'b'},
	{"loop", 0, 0, 'l'},	
	{"nosound", 0, 0, 'n'},	
	{"novideo", 0, 0, 'c'},	
	{"clearosd", 0, 0, 'o'},
	{"clearblack",0, 0,'k'},	
	{"help", 0, 0, 'h'},
    {"subtitle", 0, 0, 'u'},
	{0, 0, 0, 0}
     };

    if(argc<=1)
    {
     log_print("not set input file name\n");
        return -1;
    } 
	p->g_play_ctrl_para.video_index = -1;
	p->g_play_ctrl_para.audio_index = -1;
    p->g_play_ctrl_para.sub_index   = -1;
	
	p->control_mode[0]='\0';
    while ((c = getopt_long(argc, argv, cshort_options, clong_options, NULL)) != -1 ) 
	{
        osd_blank("/sys/class/tsync/enable", 1);
        log_print("parse_option c=%c\n", c);
	 	switch(c) 
        {
			case 'v':
				sscanf(optarg,"%d",&i);
				if(i>=0)
					p->g_play_ctrl_para.video_index = i;
				else
					{
					  log_print("unsupported video type\n");
		                        return -1;
					}
				break;
			case 'a':
				sscanf(optarg,"%d",&i);
				if(i>=0)
					p->g_play_ctrl_para.audio_index = i;
				else
					{
					  log_print("unsupported video type\n");
		                       return -1;
					}
				break;
			case 'm':
				MEMCPY(p->control_mode,optarg,MIN(strlen(optarg),16));
				break;
#ifndef ANDROID
			case 'e':
				if(load_extern_lib(p,optarg)!=0)
					{
					log_print("load extern libraries failed,%s\n",optarg);
					return -1;
					}
				break;
#endif
			case 'b':
                log_print("parse_option p->background=1\n");
				p->background=1;
				break;
			case 'l':
				p->g_play_ctrl_para.loop_mode = 1;
				break;	
			case 'n':
                log_print("parse_option p->g_play_ctrl_para.nosound=1\n");
				p->g_play_ctrl_para.nosound = 1;
				break;
            case 'u':
                p->g_play_ctrl_para.hassub = 1;
                break;
			case 'c':
				p->g_play_ctrl_para.novideo = 1;
				break;
			case 'o':
				osd_blank("/sys/class/graphics/fb0/blank",1);
				osd_blank("/sys/class/graphics/fb1/blank" ,1);
				break;
            case 'k':
                osd_blank("/sys/class/video/blackout_policy", 0);
                break;             
			case 'h':	
				return -1;	
			case ':':	
				  log_print("unsupported option\n");
				return -1;
			case '?':
				log_print("unsupported option =%s\n",optarg);
				return -1;
				break;
			case 0:
			default:
				log_print("unsupported option =%s\n",optarg);
				break;
	 	}
    }
    p->g_play_ctrl_para.file_name = NULL;
    if(optind>=0 && optind<argc)
    {
        char * file=argv[optind];
        p->g_play_ctrl_para.file_name=MALLOC(strlen(file)+1);
        if(p->g_play_ctrl_para.file_name==NULL)
        {
            log_print("alloc memory for file failed ,file=%s!\n",file);
            return -1;
        }
        strcpy(p->g_play_ctrl_para.file_name,file);
    }
    log_print("parse_option got file name %s", p->g_play_ctrl_para.file_name);

	return 0;
}
static void signal_handler(int signum)
{   
	log_print("Get signum=%x\n",signum);
	player_progress_exit();	
	signal(signum, SIG_DFL);
	raise (signum);
}
static void daemonize()
{
    pid_t pid, sid;


    if (getppid() == 1)
        return;

    pid = fork();
    if (pid < 0) {
        log_print("daemonize failed on fork");
        exit(0);
    }
    if (pid > 0)
        exit(0);


    sid = setsid();
    if (sid < 0)
        exit(-1);

    umask(0);

    if ((chdir("/")) < 0)
        exit(0);


    signal(SIGCHLD, SIG_IGN);
    signal(SIGTSTP, SIG_IGN);
    signal(SIGTTOU, SIG_IGN);
    signal(SIGTTIN, SIG_IGN);
    signal(SIGHUP, signal_handler);
    signal(SIGTERM, signal_handler);
	signal(SIGSEGV, signal_handler);
}

#ifndef ANDROID
int main(int argc,char *argv[])
#else
static int player_main(int argc, char *argv[])
#endif
{
    int i;
#ifdef ANDROID
	log_print("\n*********Amplayer Android************\n\n");
#else
    log_print("\n*********Amplayer version: %d************\n\n",SVN_VERSION);
#endif
	MEMSET(&player_para,0,sizeof(player_para));
	basic_controlers_init();
#ifdef ANDROID
    register_bc_controler();
#endif
	if(parser_option(argc,argv,&player_para)<0)
	{
		print_usage(argv[0]);
		return 0;
	}
#ifdef _DBUS
	register_dbus_controler();
#endif
	player_init();	
	
	if(start_controler(&player_para)!=0)
	{
		log_print("Can't get any controlers ,exit now\n");
		return -1;
	}
	if(player_para.background)
	{
#ifndef ANDROID
		if(log_open(LOG_FILE)<0)
			{
			log_print("open log file failed %s\n",LOG_FILE);
			return -1;
			}
		log_print("\n\n start log %s\n",LOG_FILE);
		daemonize();
#endif
	}
	else
	{
		  signal(SIGINT, signal_handler);
	}
#ifdef ANDROID
    return controler_run_bg(&player_para);
#else
	controler_run(&player_para);
	release_extern_lib(&player_para);
	//player_progress_exit();
	return 0;
#endif
}

////////////////////////////////////////////////////////////////////////////////
// JNI Interface
////////////////////////////////////////////////////////////////////////////////

#include <jni.h>
#include <android/log.h>

#define  LOG_TAG    "amplayer_jni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#include <string.h>
//todo..
//#include <osd/osd_main.h>
#ifndef FBIOPUT_OSD_SRCCOLORKEY
#define  FBIOPUT_OSD_SRCCOLORKEY    0x46fb
#endif

#ifndef FBIOPUT_OSD_SRCKEY_ENABLE
#define  FBIOPUT_OSD_SRCKEY_ENABLE  0x46fa
#endif

static void
startcmd(JNIEnv *env, jobject obj, jstring filename) 
{	
    const char *fn = (*env)->GetStringUTFChars(env, filename, NULL);
    char argv0[] = "amplayer";
    char noaudio[] = "-n";
    char background[] = "-b";
    char mode[] = "-m";
    char bc[] = "bc";
    char *argv[] = {argv0, background, mode, bc, fn};
    //char *argv[] = {argv0, noaudio, background, mode, bc, argvfn};

    LOGI("amplayer-JNI startcmd1(%s) enter", fn);

    optind = 1;
    player_main( sizeof(argv) / sizeof(argv[0]), argv);

    LOGI("amplayer-JNI startcmd(%s) exit", fn);

    (*env)->ReleaseStringUTFChars(env, filename, fn);
}

static jint
sendcmd(JNIEnv *env, jobject obj, jstring cmd) 
{	
	const char *cmdc = (*env)->GetStringUTFChars(env, cmd, NULL);
	LOGI("amplayer-JNI sendcmd(%s) enter", cmdc);
    bc_control_set_command(cmdc);
	LOGI("amplayer-JNI sendcmd(%s) exit", cmdc);

	(*env)->ReleaseStringUTFChars(env, cmd, cmdc);

	return 0;
}

//static int g_has_last_info = 0;  //FIXME
static player_info_t g_last_info;

static jint
reqstate(JNIEnv *env, jobject obj)
{
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "onUpdateState", "(IIIIII)V");
    if (mid == NULL)
        return -2;
    (*env)->CallVoidMethod(env, obj, mid,
                           g_last_info.last_sta,
                           g_last_info.status,
                           g_last_info.full_time,
                           g_last_info.current_time,
                           g_last_info.last_time,
                           g_last_info.error_no);
    return 0;
}

void
vm_update_state(player_info_t *info)
{
    MEMCPY(&g_last_info, info, sizeof(player_info_t));
    g_last_info.name = NULL;
}

/** 
 *  Instead of attaching/detaching threads to call a Java method,
 *  make the Java app call getstate() to poll for the player state.
 */
/*
void
vm_update_state(player_info_t *info)
{
    int status;
    JNIEnv *env;
    jclass appclass;
    jmethodID method;
    if (gJavaVM == NULL) {
        LOGE("vm_upd_st gJavaVM");
        return;
    }
    status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL);
    if (status < 0) {
        LOGE("vm_update_state() failed to attach thread");
        return;
    }
    if (env == NULL) {
        LOGE("vm_upd_st env");
        return;
    }
    appclass = (*env)->GetObjectClass(env, gAppObject);
    if (!appclass) {
        LOGE("vm_update_state() failed to get class reference");
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
        return;
    }
    method = (*env)->GetStaticMethodID(env, appclass, 
                                       "onUpdateState",
                                       "(IIIIII)V");
    if (!method) {
        LOGE("vm_update_state() failed to get method ID");
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
        return;
    }
    (*env)->CallStaticVoidMethod(env, appclass, method,
                                 info->last_sta, info->status, info->full_time,
                                 info->current_time, info->last_time, info->error_no);
    (*gJavaVM)->DetachCurrentThread(gJavaVM);
}
*/

static jint
enablecolorkey(JNIEnv *env, jobject obj, jshort key_rgb565) 
{
    int ret = -1;
    int fd_fb0 = open("/dev/graphics/fb0", O_RDWR);
    if (fd_fb0 >= 0) {
        uint32_t myKeyColor = key_rgb565;
        uint32_t myKeyColor_en = 1;
        log_print("enablecolorkey color=%#x\n", myKeyColor);
        ret = ioctl(fd_fb0, FBIOPUT_OSD_SRCCOLORKEY, &myKeyColor);
        ret += ioctl(fd_fb0, FBIOPUT_OSD_SRCKEY_ENABLE, &myKeyColor_en);
        close(fd_fb0);
    }
    return ret;
}

static jint
disablecolorkey(JNIEnv *env, jobject obj) 
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

static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "native_startcmd", "(Ljava/lang/String;)I",
            (void*) startcmd },
    { "native_sendcmd", "(Ljava/lang/String;)I",
            (void*) sendcmd },
    { "native_enablecolorkey", "(S)I",
            (void*) enablecolorkey },
    { "native_disablecolorkey", "()I",
            (void*) disablecolorkey },
	{ "native_reqstate", "()I",
            (void*) reqstate },
};
/*
static JNINativeMethod gPVMethods[] = {
    { "native_reqstate", "()I",
            (void*) reqstate },
};
*/
//JNIHelp.h ????
#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif
static int registerNativeMethods(JNIEnv* env, const char* className,
                                 const JNINativeMethod* methods, int numMethods)
{
    int rc;
    jclass clazz;
    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'\n", className);
        return -1;
    }
    if (rc = ((*env)->RegisterNatives(env, clazz, methods, numMethods)) < 0) {
        LOGE("RegisterNatives failed for '%s' %d\n", className, rc);
        return -1;
    }
    return 0;
}

JNIEXPORT jint
JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jclass * localClass;

    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("GetEnv failed!");
        return -1;
    }
 
    if (registerNativeMethods(env, "amlogic/playerservice/AmPlayer",
                              gMethods, NELEM(gMethods)) < 0)
        return -1;
//    if (registerNativeMethods(env, "amlogic/videoplayer/playermenu",
   //                           gPVMethods, NELEM(gPVMethods)) < 0)
      //  return -1;
    return JNI_VERSION_1_4;
}


