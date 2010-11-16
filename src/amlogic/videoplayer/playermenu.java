package amlogic.videoplayer;

import java.util.Timer;
import java.util.TimerTask;
import amlogic.playerservice.Player;
import amlogic.playerservice.VideoInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.*;
import android.view.WindowManager.LayoutParams;
import android.widget.*;

public class playermenu extends Activity {
	private String TAG = "playermenu";
    /** Called when the activity is first created. */
	private int totaltime = 0;
	private static final int TV_PANEL = 1;
    private static final int PLAY_MODE = 2;
    private static final int AUDIO_TRACE = 3;
    private static final int SUBTITLE_SET = 4;
    private static final int DISPLAY_MODE = 5;
    private static final int BRIGHTNESS_SET = 6;
    
    private static int playmode = 1;
    private static final int REPEATLIST = 1;
    private static final int REPEATONE = 2;
  
    private SeekBar myProgressBar = null;
    private ImageButton play = null;
	private TextView cur_time = null;
	private TextView total_time = null;
	private LinearLayout infobar = null;
	private LinearLayout morbar = null;
	Timer timer = new Timer();
	private static int PRE_NEXT_FLAG = 0;
	private int player_status = VideoInfo.PLAYER_UNKNOWN;
	
    protected Dialog onCreateDialog(int id) {
    	 switch (id) {
         case TV_PANEL:
             return new AlertDialog.Builder(playermenu.this)
                 .setTitle(R.string.str_vtvorpanel)
                 .setItems(R.array.tv_panel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create();
         case PLAY_MODE:
             return new AlertDialog.Builder(playermenu.this)
                 .setTitle(R.string.str_vplaymode)
                 .setItems(R.array.play_mode, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                    	 if (which == 0)
                    		 playmode = REPEATLIST;
                    	 else if (which == 1)
                    		 playmode = REPEATONE;
                     }
                 })
                 .create(); 
         case AUDIO_TRACE:
             return new AlertDialog.Builder(playermenu.this)
                 .setTitle(R.string.str_vaudio)
                 .setItems(R.array.audio_trace, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create();
         case SUBTITLE_SET:
             return new AlertDialog.Builder(playermenu.this)
                 .setTitle(R.string.str_vsubtitle)
                 .setItems(R.array.subtitle, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create();
         case DISPLAY_MODE:
             return new AlertDialog.Builder(playermenu.this)
                 .setTitle(R.string.str_vdisplay)
                 .setItems(R.array.display, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create(); 
         case BRIGHTNESS_SET:
             return new AlertDialog.Builder(playermenu.this)
                 .setTitle(R.string.str_brightness)
                 .setItems(R.array.brightness, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                    	 WindowManager.LayoutParams lp = getWindow().getAttributes();	
                    	 switch(which)
                    	 {
                    	 case 0:
                    		 lp.screenBrightness = 0.2f;
                    		 break;
                    	 case 1:
                    		 lp.screenBrightness = 0.5f;
                    		 break;
                    	 case 2:
                    		 lp.screenBrightness = 0.7f;
                    		 break;
                    	 case 3:
                    		 lp.screenBrightness = 1.0f;
                    		 break;	 
                    	 default:
                    		  break;
                    	 }
                    	 getWindow().setAttributes(lp);
                    	}
                 })
                 .create();
         }
         return null;
    }
@Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch (id) {
         case PLAY_MODE: 
         case AUDIO_TRACE:	 
         case BRIGHTNESS_SET: 
         case DISPLAY_MODE:
         case SUBTITLE_SET:	 
            WindowManager wm = getWindowManager();
           Display display = wm.getDefaultDisplay();
             LayoutParams lp = dialog.getWindow().getAttributes();
             if (display.getHeight() > display.getWidth()) {             
                lp.width = (int) (display.getWidth() * 1.0);            
                } else {                        
                         lp.width = (int) (display.getWidth() * 0.5);                    
                }
             dialog.getWindow().setAttributes(lp);
             break;
         }
    }  
    private void videobar() {
    		
    		setContentView(R.layout.layout_imagebutton);
    		morbar = (LinearLayout)findViewById(R.id.morebarLayout);
    		ImageButton panelortv = (ImageButton) findViewById(R.id.ImageButton01);
            panelortv.setOnClickListener(new View.OnClickListener() 
    	    {
                public void onClick(View v) 
                {
                	sendBroadcast( new Intent("com.amlogic.HdmiSwitch.FAST_SWITCH"));
                } 
    	    });
            
            ImageButton playmode = (ImageButton) findViewById(R.id.ImageButton02);
            playmode.setOnClickListener(new View.OnClickListener() 
    	    {
                public void onClick(View v) 
                {
                	showDialog(PLAY_MODE);
                } 
    	    });
            ImageButton audiotrace = (ImageButton) findViewById(R.id.ImageButton03);
            audiotrace.setOnClickListener(new View.OnClickListener() 
    	    {
                public void onClick(View v) 
                {
                	showDialog(AUDIO_TRACE);
                } 
    	    });
            ImageButton sutitle = (ImageButton) findViewById(R.id.ImageButton04);
            sutitle.setOnClickListener(new View.OnClickListener() 
    	    {
                public void onClick(View v) 
                {
                	showDialog(SUBTITLE_SET);
                } 
    	    });
            ImageButton display = (ImageButton) findViewById(R.id.ImageButton05);
            display.setOnClickListener(new View.OnClickListener() 
    	    {
                public void onClick(View v) 
                {
                	showDialog(DISPLAY_MODE);
                } 
    	    });
            ImageButton brigtness = (ImageButton) findViewById(R.id.ImageButton06);
            brigtness.setOnClickListener(new View.OnClickListener() 
    	    {
                public void onClick(View v) 
                {
                	showDialog(BRIGHTNESS_SET);
                } 
    	    }); 
            ImageButton backtovidebar = (ImageButton) findViewById(R.id.ImageButton07);
            backtovidebar.setOnClickListener(new View.OnClickListener() 
    	    {
                public void onClick(View v) 
                {
                	setContentView(R.layout.infobar);
                	initinfobar();
                } 
    	    }); 
    	}
    
    public boolean onKeyDown(int keyCode, KeyEvent msg) 
    {
    	if (keyCode == KeyEvent.KEYCODE_BACK) 
    	{
    		if (morbar!=null) 
	        {
	        	morbar=null;
	        	setContentView(R.layout.infobar);
	        	initinfobar();
	        	return(true);
	          
	        }
    		else
    		{
    			if(m_Amplayer == null)
					return (true);
    			//stop play
    			Amplayer_stop();
				return super.onKeyDown(keyCode, msg);
    		}
    	}
        else
		 return super.onKeyDown(keyCode, msg);
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.infobar);
        initinfobar();
        
    }
    protected void initinfobar()
    {
        ImageButton browser = (ImageButton)findViewById(R.id.BrowserBtn);
        ImageButton more = (ImageButton)findViewById(R.id.moreBtn);
        ImageButton preItem = (ImageButton)findViewById(R.id.PreBtn);
        ImageButton nextItem = (ImageButton)findViewById(R.id.NextBtn);
        play = (ImageButton)findViewById(R.id.PlayBtn);
        ImageButton fastforword = (ImageButton)findViewById(R.id.FastForward);
        ImageButton fastreverse = (ImageButton)findViewById(R.id.FastReverse);
        infobar = (LinearLayout)findViewById(R.id.infobarLayout);
        myProgressBar = (SeekBar)findViewById(R.id.SeekBar02);
    	cur_time = (TextView)findViewById(R.id.TextView03);
    	total_time = (TextView)findViewById(R.id.TextView04);
    	cur_time.setText(secToTime(0));
    	total_time.setText(secToTime(0));
        
        browser.setOnClickListener(new ImageButton.OnClickListener()
    	{
			public void onClick(View v) 
			{
			// TODO Auto-generated method stub
				Intent selectFileIntent = new Intent();
				selectFileIntent.setClass(playermenu.this, FileList.class);
					
				//stop play
				if(m_Amplayer != null)
					Amplayer_stop();
				startActivity(selectFileIntent);
				playermenu.this.finish();
			}
		});
        
        preItem.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String filename = PlayList.getinstance().moveprev();
				Toast toast = Toast.makeText(playermenu.this, filename, Toast.LENGTH_LONG); 
				toast.show();
				if(m_Amplayer == null)
					return;
				//stop play
				else
					Amplayer_stop();
				PRE_NEXT_FLAG = 1;
			}
        });
        
        nextItem.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String filename = PlayList.getinstance().movenext();
				Toast toast = Toast.makeText(playermenu.this, filename, Toast.LENGTH_LONG);
				toast.show();
				if(m_Amplayer == null)
					return;
				else
					Amplayer_stop();
				PRE_NEXT_FLAG = 1;
			}
        });
        
        play.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (player_status == VideoInfo.PLAYER_RUNNING)
				{
					try
					{
						m_Amplayer.Pause();
					}
					catch(RemoteException e)
					{
						e.printStackTrace();
					}
				}
				else if (player_status == VideoInfo.PLAYER_PAUSE)
				{
					try
					{
						m_Amplayer.Resume();
					}
					catch(RemoteException e)
					{
						e.printStackTrace();
					}
				}
			}
        });
                
        fastforword.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				try
				{
					m_Amplayer.FastForward();
				}
				catch(RemoteException e)
				{
					e.printStackTrace();
				}
			}
        });
        
        fastreverse.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				try
				{
					m_Amplayer.BackForward();
				}
				catch(RemoteException e)
				{
					e.printStackTrace();
				}
			}
        });
        
        more.setOnClickListener(new ImageButton.OnClickListener()
    	{
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				videobar();
			}
		});
        
        myProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
        {
    
			public void onStopTrackingTouch(SeekBar seekBar) 
			{
				// TODO Auto-generated method stub
				int dest = myProgressBar.getProgress();
				int pos = totaltime * dest / 100;
				try
				{
					m_Amplayer.Seek(pos);
				}
				catch(RemoteException e)
				{
					e.printStackTrace();
				}
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) 
			{
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) 
			{
				// TODO Auto-generated method stub
				
			}
		});
        
        waitForHide();
		StartPlayerService();
    }
	
    protected void waitForHide()	//infobar auto hide
    {
    	final Handler handler = new Handler(){   
    		  
            public void handleMessage(Message msg) {   
                switch (msg.what) {       
                case 0x3c:       
                	hide_infobar();
                    break;       
                }       
                super.handleMessage(msg);   
            }
               
        };   
        TimerTask task = new TimerTask(){   
      
            public void run() {   
                Message message = new Message();       
                message.what = 0x3c;       
                handler.sendMessage(message);     
            }   
               
        };   
        
        timer.cancel();
        timer = new Timer();
    	timer.schedule(task, 3000);
    }
    
    protected void hide_infobar()
    {
    	infobar.setVisibility(View.GONE);
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
    			WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    protected void show_menu()
    {
    	infobar.setVisibility(View.VISIBLE);
    	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    public boolean onTouchEvent (MotionEvent event)
    {
    	super.onTouchEvent(event);
    	show_menu();
    	waitForHide();
    	return true;
    }
    
    private String secToTime(int i)
	{
		String retStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (i <= 0)
			return "00:00:00";
		else
		{
			minute = i/60;
			if (minute < 60)
			{
				second = i%60;
				retStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
			}
			else
			{
				hour = minute/60;
				if (hour > 99)
					return "99:59:59";
				minute = minute%60;
				retStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
			}
		}
		return retStr;
	}
	
	private String unitFormat(int i)
	{
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Integer.toString(i);
		else
			retStr = Integer.toString(i);
		return retStr;
    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        
        Amplayer_stop();
        StopPlayerService();
    }

    
    
	//=========================================================
    private Messenger m_PlayerMsg = new Messenger(new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		switch(msg.what)
    		{
    			case VideoInfo.TIME_INFO_MSG:
    				Log.i(TAG,"get time "+secToTime(msg.arg1));
    		    	cur_time.setText(secToTime(msg.arg1));
    		    	total_time.setText(secToTime(msg.arg2));
    		    	totaltime = msg.arg2;
    		    	if (totaltime == 0)
						myProgressBar.setProgress(0);
					else
						myProgressBar.setProgress(msg.arg1*100/totaltime);
    				break;
    			case VideoInfo.STATUS_CHANGED_INFO_MSG:
    				player_status = msg.arg1;
    				switch(player_status)
    				{
					case VideoInfo.PLAYER_RUNNING:
						play.setBackgroundResource(R.drawable.pause_button);
						break;
					case VideoInfo.PLAYER_PAUSE:
						play.setBackgroundResource(R.drawable.play_button);
						break;
					case VideoInfo.PLAYER_STOPED:
						if (PRE_NEXT_FLAG == 1)
    					{
    						Log.d(TAG,"to play another file!");
    						Amplayer_play();
    						PRE_NEXT_FLAG = 0;
    					}
						break;
					case VideoInfo.PLAYER_PLAYEND:
						if (playmode == REPEATLIST)
							PlayList.getinstance().movenext();
						
						Amplayer_play();
						break;
					default:
						break;
    				}
    				break;
    			default:
    				super.handleMessage(msg);
    				break;
    		}
    	}
    });
    
	
    public Player m_Amplayer = null;
    private void Amplayer_play()
    {
    	try
		{
			m_Amplayer.Open(PlayList.getinstance().getcur());
		}
		catch(RemoteException e)
		{
			e.printStackTrace();
		}
		try
		{
			m_Amplayer.Play();
		}
		catch(RemoteException e)
		{
			e.printStackTrace();
		}
    }
    
    private void Amplayer_stop()
    {
    	try {
			m_Amplayer.Stop();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			m_Amplayer.Close();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    ServiceConnection m_PlayerConn = new ServiceConnection()
    {
		public void onServiceConnected(ComponentName name, IBinder service) {
			m_Amplayer = Player.Stub.asInterface(service);

			try {
				m_Amplayer.Init();
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.d(TAG,"init fail!");
			}
			try {
				m_Amplayer.RegisterClientMessager(m_PlayerMsg.getBinder());
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.d(TAG,"set client fail!");
			}
			
			//auto play
			Log.d(TAG,"to play files!");
			try
			{
				m_Amplayer.SetColorKey(0x18c3);
			}
			catch(RemoteException e)
			{
				e.printStackTrace();
			}
			Amplayer_play();
		}

		public void onServiceDisconnected(ComponentName name) {
			try {
				m_Amplayer.Stop();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			try {
				m_Amplayer.Close();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			m_Amplayer = null;
		}
    };
    public void StartPlayerService()
    {
    	Intent intent = new Intent();
    	ComponentName hcomponet = new ComponentName("amlogic.videoplayer","amlogic.playerservice.AmPlayer");
    	intent.setComponent(hcomponet);
    	this.startService(intent);
    	this.bindService(intent, m_PlayerConn, BIND_AUTO_CREATE);
    }
    
    public void StopPlayerService()
    {
    	this.unbindService(m_PlayerConn);
    	Intent intent = new Intent();
    	ComponentName hcomponet = new ComponentName("amlogic.videoplayer","amlogic.playerservice.AmPlayer");
    	intent.setComponent(hcomponet);
    	this.stopService(intent);
    	m_Amplayer = null;
    }

}