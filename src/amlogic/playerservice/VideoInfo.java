package amlogic.playerservice;

public class VideoInfo {
	class TimeInfo
	{
		int mTotalTime;
		int mCurTime;
	}
	
	public final static int TIME_INFO_MSG = 1000;
	public final static int STATUS_CHANGED_INFO_MSG = 1000+1;
	public final static int AUDIO_CHANGED_INFO_MSG = 1000+2;
	public final static int HAS_ERROR_MSG = 1000+3;

    //typedef enum { ... } player_status;
    public static final int PLAYER_UNKNOWN  = 0;
    /*public static final int PLAYER_STOPED   = 1;
    public static final int PLAYER_RUNNING  = 2;
    public static final int PLAYER_PAUSE    = 3;
    public static final int PLAYER_SEARCHING= 4;
    public static final int PLAYER_SEARCHOK = 5;
    public static final int PLAYER_INITING  = 6;
    public static final int PLAYER_ERROR    = 7;
    public static final int PLAYER_PLAYEND  = 8;
    public static final int PLAYER_START    = 9;
    public static final int PLAYER_FF_END   = 10;
    public static final int PLAYER_FB_END   = 11;
    public static final int PLAYER_BUFFERING= 12;
    public static final int PLAYER_INITOK   = 13;*/
    
	/******************************
	* 0x1000x: 
	* player do parse file
	* decoder not running
	******************************/
    public static final int PLAYER_INITING  	= 0x10001;
    public static final int PLAYER_INITOK   	= 0x10002;	

	/******************************
	* 0x2000x: 
	* playback status
	* decoder is running
	******************************/
    public static final int PLAYER_RUNNING  	= 0x20001;
    public static final int PLAYER_BUFFERING 	= 0x20002;
    public static final int PLAYER_PAUSE    	= 0x20003;
    public static final int PLAYER_SEARCHING	= 0x20004;
	
    public static final int PLAYER_SEARCHOK 	= 0x20005;
    public static final int PLAYER_START    	= 0x20006;	
    public static final int PLAYER_FF_END   	= 0x20007;
    public static final int PLAYER_FB_END   	= 0x20008;

	/******************************
	* 0x3000x: 
	* player will exit	
	******************************/
    public static final int PLAYER_ERROR		= 0x30001;
    public static final int PLAYER_PLAYEND  	= 0x30002;	
    public static final int PLAYER_STOPED   	= 0x30003; 

}
