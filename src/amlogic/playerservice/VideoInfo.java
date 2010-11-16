package amlogic.playerservice;

public class VideoInfo {
	class TimeInfo
	{
		int mTotalTime;
		int mCurTime;
	}
	
	
	public final static int TIME_INFO_MSG = 1000;
	public final static int STATUS_CHANGED_INFO_MSG = 1000+1;
	

    //typedef enum { ... } player_status;
    public static final int PLAYER_UNKNOWN  = 0;
    public static final int PLAYER_STOPED   = 1;
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
    public static final int PLAYER_INITOK   = 13;

}
