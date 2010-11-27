package amlogic.playerservice;


import amlogic.playerservice.Player;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


public class AmPlayer extends Service {
    static 
    {
    	System.loadLibrary("amplayerjni");
    };
    private static final String TAG = "amplayer";
    private static Messenger mClient = null;
    private static int player_status = 0;
    
    /**jni interface */
    public static native int native_startcmd(String filename);
    public static native int native_sendcmd(String cmd);
    public native int native_reqstate();//!!!should not be static
    public static native int native_setglobalalpha(int alpha);
    public static native int native_getosdbpp();
    public static native int native_enablecolorkey(short key_rgb565);
    public static native int native_disablecolorkey();
	
	
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		mClient = null;
		return m_player;
	}
	public boolean onUnbind (Intent intent)
	{
		try {
			m_player.Stop();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			m_player.Close();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mClient = null;
		return true;
	}
	
	public Player.Stub m_player = new Player.Stub()
	{
		public int Init() throws RemoteException {
			return 0;
		}

		public int Open(String filepath) throws RemoteException {
			native_startcmd(filepath);
			return 0;
		}

		public int Play() throws RemoteException {
			//startGetStates();
			return 0;
		}

		public int Pause() throws RemoteException {
			native_sendcmd("pause");
			return 0;
		}

		public int Resume() throws RemoteException {
			native_sendcmd("resume");
			return 0;
		}

		public int Stop() throws RemoteException {
			//stopGetStates();
			native_sendcmd("stop");
			return 0;
		}

		public int Close() throws RemoteException {
			return 0;
		}

		public int FastForward(int speed) throws RemoteException {
			native_sendcmd("forward:"+String.valueOf(speed));
			return 0;
		}

		public int BackForward(int speed) throws RemoteException {
			native_sendcmd("backward:"+String.valueOf(speed));
			return 0;
		}

		public int SetColorKey(int color) throws RemoteException {
			native_enablecolorkey((short) color);
			return 0;
		}
		
		public void DisableColorKey() throws RemoteException {
			native_disablecolorkey();
		}

		public int Seek(int time) throws RemoteException {
			native_sendcmd("search:"+String.valueOf(time));
			return 0;
		}

		public int RegisterClientMessager(IBinder hbinder)
				throws RemoteException {
			mClient = new Messenger(hbinder);
			return 0;
		}
	};
	
	//get info every 0.5s,and send it to client
	Handler mhandler = new Handler();
    private Runnable mGetState = new Runnable() {
        public void run() {
        	AmPlayer.this.native_reqstate();
        	mhandler.postDelayed(mGetState, 500);
        }
    };
    private void startGetStates() {
    	mhandler.removeCallbacks(mGetState);
    	mhandler.postDelayed(mGetState, 500);
    }
    private void stopGetStates() {
    	mhandler.removeCallbacks(mGetState);
    }

	public static void onUpdateState(int last_sta, int status, int full_time,
			int current_time, int last_time, int error_no)
	{
		Message message = new Message();
		message.what = VideoInfo.TIME_INFO_MSG;
		message.arg1 = current_time;
		message.arg2 = full_time;
		try {
			mClient.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//send message for status changed
		if(player_status != status)
		{
			player_status = status;
			Message s_message = new Message();
			s_message.what = VideoInfo.STATUS_CHANGED_INFO_MSG;
			s_message.arg1 = player_status;
			if (player_status == VideoInfo.PLAYER_ERROR)
				s_message.arg2 = error_no;
			Log.d(TAG,"player status changed to: " + player_status);
			try {
				mClient.send(s_message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

}
