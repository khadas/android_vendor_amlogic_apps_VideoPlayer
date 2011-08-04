package com.farcore.videoplayer;

import android.os.storage.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.os.SystemProperties;

import com.subtitleparser.*;
import com.subtitleview.SubtitleView;
import android.content.Context;
import com.farcore.playerservice.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.res.Configuration;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.*;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class playermenu extends Activity {
	private static String TAG = "playermenu";
	private static String codec_mips = null;
	private static String InputFile = "/sys/class/audiodsp/codec_mips";
	private static String OutputFile = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
	
    /** Called when the activity is first created. */
	private int totaltime = 0;
	private int curtime = 0;
	private int playPosition = 0;
	private int cur_audio_stream = 0;
	private int total_audio_num = 0;
	private int cur_audio_channel = 0;
	
    private final int PLAY_RESUME = 0;
    private final int PLAY_MODE = 1;
    private final int AUDIOTRACK = 2;
    private final int DISPLAY = 3;
    private final int BRIGHTNESS = 4;
	private final int PLAY3D = 5;
	private final int PLAYCHAPTERS = 6;
	private final int AUDIO_CHANNEL = 7;

	private boolean backToFileList = false;
	private boolean progressSliding = false;
	private boolean INITOK = false;
	private boolean FF_FLAG = false;
	private boolean NOT_FIRSTTIME = false;
	private static final int MID_FREESCALE = 0x10001;
    private boolean fb32 = false;
    
    //for repeat mode;
	private boolean playmode_switch = true;
    private static int m_playmode = 1;
    private static final int REPEATLIST = 1;
    private static final int REPEATONE = 2;
  
    private SeekBar myProgressBar = null;
    private ImageButton play = null;
    private ImageButton fastforword = null;
    private ImageButton fastreverse = null;
	private TextView cur_time = null;
	private TextView total_time = null;
	private LinearLayout infobar = null;
	private LinearLayout morbar = null;
	private LinearLayout subbar = null;
	private LinearLayout otherbar = null;
	private LinearLayout infodialog = null;
	private AlertDialog confirm_dialog = null;
	private BroadcastReceiver mReceiver = null;

	Timer timer = new Timer();
	Toast toast = null;
	public MediaInfo bMediaInfo = null;
	private static int PRE_NEXT_FLAG = 0;
	private int resumeSecond = 8;
	private int player_status = VideoInfo.PLAYER_UNKNOWN;
	
	//for subtitle
	private SubtitleUtils subMange = null;
	private SubtitleView  subTitleView = null;
	private subview_set   sub_para = null;
	private int sub_switch_state = 0;
	private int sub_font_state = 0;
	private int sub_color_state = 0;
	private TextView t_subswitch =null ;
	private TextView t_subsfont=null ;
	private TextView t_subscolor=null ;
	private TextView morebar_tileText =null;
    private boolean touchVolFlag = false;
	private WindowManager mWindowManager;
	private int[] angle_table = {0, 1, 2, 3};
	private String[] m_brightness= {"1","2","3","4","5","6"};
	private int mode_3d = 0;
    private enum Mode_3D {
        MODE_DISABLE,
        MODE_LR,
        MODE_BT,
        MODE_LR_SWITCH,
        MODE_3D_TO_2D_L,
        MODE_3D_TO_2D_R,
        MODE_2D_TO_3D,
        MODE_FIELD_DEPTH,
    }
	
	private int[] string_3d_id = {
	    R.string.setting_3d_diable,
		R.string.setting_3d_lr,
		R.string.setting_3d_bt,
		R.string.setting_3d_lr_switch,
		R.string.setting_3d_2d_l,
		R.string.setting_3d_2d_r,
		R.string.setting_3d_2d,
		R.string.setting_3d_field_depth,
	};
	
	private static final String ACTION_HDMISWITCH_MODE_CHANGED =
		"com.amlogic.HdmiSwitch.HDMISWITCH_MODE_CHANGED";
	
	private boolean mSuspendFlag = false;
	PowerManager.WakeLock mScreenLock = null;

	public void setAngleTable() {
		if(SystemProperties.get("ro.sf.hwrotation").equals("90")) {
			angle_table[0] = 1;
			angle_table[1] = 2;
			angle_table[2] = 3;
			angle_table[3] = 0;
		}
		else if(SystemProperties.get("ro.sf.hwrotation").equals("180")) {
			angle_table[0] = 2;
			angle_table[1] = 3;
			angle_table[2] = 0;
			angle_table[3] = 1;
		}
		else if(SystemProperties.get("ro.sf.hwrotation").equals("270")) {
			angle_table[0] = 3;
			angle_table[1] = 0;
			angle_table[2] = 1;
			angle_table[3] = 2;
		}
		else {
			angle_table[0] = 0;
			angle_table[1] = 1;
			angle_table[2] = 2;
			angle_table[3] = 3;
		}
	}
	
    private SimpleAdapter getMorebarListAdapter(int id, int pos) {
        return new SimpleAdapter(this, getMorebarListData(id, pos),
            R.layout.list_row, new String[] {
                "item_name", "item_sel"
            }, new int[] {
                R.id.Text01, R.id.imageview,
            });
    }

    private List<? extends Map<String, ?>> getMorebarListData(int id, int pos) {
        // TODO Auto-generated method stub
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;

        switch (id) {
            case PLAY_RESUME:
            case PLAY_MODE:
                map = new HashMap<String, Object>();
                map.put("item_name", getResources().getString(R.string.str_on));
                map.put("item_sel", R.drawable.item_img_unsel);
                list.add(map);
                map = new HashMap<String, Object>();
                map.put("item_name", getResources().getString(R.string.str_off));
                map.put("item_sel", R.drawable.item_img_unsel);
                list.add(map);

                list.get(pos).put("item_sel", R.drawable.item_img_sel);
                break;

            case AUDIOTRACK:
                if (AudioTrackOperation.AudioStreamFormat.size() < bMediaInfo.getAudioTrackCount())
                    AudioTrackOperation.setAudioStream(bMediaInfo);
                int size_as = AudioTrackOperation.AudioStreamFormat.size();
                for (int i = 0; i < size_as; i++) {
                    map = new HashMap<String, Object>();
                    map.put("item_name", AudioTrackOperation.AudioStreamFormat.get(i));
                    map.put("item_sel", R.drawable.item_img_unsel);
                    list.add(map);
                }
                list.get(pos).put("item_sel", R.drawable.item_img_sel);
                break;

            case DISPLAY:
                map = new HashMap<String, Object>();
                map.put("item_name", getResources().getString(R.string.setting_displaymode_normal));
                map.put("item_sel", R.drawable.item_img_unsel);
                list.add(map);
                map = new HashMap<String, Object>();
                map.put("item_name", getString(R.string.setting_displaymode_fullscreen));
                map.put("item_sel", R.drawable.item_img_unsel);
                list.add(map);
                map = new HashMap<String, Object>();
                map.put("item_name", "4:3");
                map.put("item_sel", R.drawable.item_img_unsel);
                list.add(map);
                map = new HashMap<String, Object>();
                map.put("item_name", "16:9");
                map.put("item_sel", R.drawable.item_img_unsel);
                list.add(map);
                map = new HashMap<String, Object>();
                map.put("item_name", getResources().getString(R.string.setting_displaymode_normal_noscaleup));
                map.put("item_sel", R.drawable.item_img_unsel);
                list.add(map);

                list.get(pos).put("item_sel", R.drawable.item_img_sel);
                break;

            case BRIGHTNESS:
                int size_bgh = m_brightness.length;
                for (int i = 0; i < size_bgh; i++) {
                    map = new HashMap<String, Object>();
                    map.put("item_name", m_brightness[i].toString());
                    map.put("item_sel", R.drawable.item_img_unsel);
                    list.add(map);
                }

                list.get(pos).put("item_sel", R.drawable.item_img_sel);
                break;
			case PLAY3D:
			    int size_3d =  string_3d_id.length;
				for (int i = 0; i < size_3d; i++) {
                  map = new HashMap<String, Object>();
                    map.put("item_name", getResources().getString(string_3d_id[i]));
                    map.put("item_sel", R.drawable.item_img_unsel);
                    list.add(map);
				}
				
				list.get(pos).put("item_sel", R.drawable.item_img_sel);
			    break;
			case PLAYCHAPTERS:
			    break;	
			
            case AUDIO_CHANNEL:
				map = new HashMap<String, Object>();
                map.put("item_name", getResources().getString(R.string.str_stereo_auidochannel));
                map.put("item_sel", R.drawable.item_img_unsel);
				list.add(map);
				map = new HashMap<String, Object>();
                map.put("item_name", getResources().getString(R.string.str_left_auidochannel));
                map.put("item_sel", R.drawable.item_img_unsel);
				list.add(map);
				map = new HashMap<String, Object>();
				map.put("item_name", getResources().getString(R.string.str_right_auidochannel));
                map.put("item_sel", R.drawable.item_img_unsel);
				list.add(map);
 
				list.get(pos).put("item_sel", R.drawable.item_img_sel);
	
            default:
                break;
        }

        return list;
    }

    private void videobar() {
        if (fb32) {
			if(SystemProperties.getBoolean("mbx.3D_Bright.enable", true))
            {
				setContentView(R.layout.layout_morebar32);
			}
			else
			{
				setContentView(R.layout.layout_morebar32_mbx);
			}
        } else {
			if(SystemProperties.getBoolean("mbx.3D_Bright.enable", true))
            {
				setContentView(R.layout.layout_morebar);
			}
			else
			{
				setContentView(R.layout.layout_morebar_mbx);
			}
        }
    	
    	subTitleView = (SubtitleView) findViewById(R.id.subTitle_more);
    	subTitleView.setTextColor(sub_para.color);
    	subTitleView.setTextSize(sub_para.font);
    	openFile(sub_para.sub_id);
		
		subbar = (LinearLayout)findViewById(R.id.LinearLayout_sub);
		subbar.setVisibility(View.GONE);
		
		otherbar = (LinearLayout)findViewById(R.id.LinearLayout_other);
		morebar_tileText = (TextView)findViewById(R.id.more_title);
		otherbar.setVisibility(View.GONE);
		
		infodialog = (LinearLayout)findViewById(R.id.dialog_layout);
		infodialog.setVisibility(View.GONE);
    	morbar = (LinearLayout)findViewById(R.id.morebarLayout);
    	if (SettingsVP.display_mode.equals("480p")) {
    		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) morbar.getLayoutParams();
    		if(SettingsVP.panel_resolution.equals("800x600")) {
    			linearParams.width = 720;
    			linearParams.bottomMargin = 140;
    		}
    		else if(SettingsVP.panel_resolution.equals("800x480")) {
    			linearParams.width = 720;
    			linearParams.bottomMargin = 10;
    		}
    		else {
    			linearParams.width = 720;
    			linearParams.bottomMargin = 140;
    		}
    		morbar.setLayoutParams(linearParams);
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
    				WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
    	morbar.requestFocus();
    	
    	ImageButton resume = (ImageButton) findViewById(R.id.ResumeBtn);
    	resume.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
			    otherbar.setVisibility(View.VISIBLE);
				morbar.setVisibility(View.GONE);
				morebar_tileText.setText(R.string.setting_resume);
				
				ListView listView = (ListView)findViewById(R.id.AudioListView);
                listView.setAdapter(getMorebarListAdapter(PLAY_RESUME, SettingsVP.getParaBoolean("ResumeMode")
                    ? 0 : 1));
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					    if (position == 0)
						    SettingsVP.putParaBoolean("ResumeMode", true);
						else if (position == 1)
						    SettingsVP.putParaBoolean("ResumeMode", false);
						otherbar.setVisibility(View.GONE);
						morbar.setVisibility(View.VISIBLE);
					}
				});
				otherbar.requestFocus();
			} 
		});
    	
    	ImageButton playmode = (ImageButton) findViewById(R.id.PlaymodeBtn);
    	if(playmode_switch) {
    		playmode.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View v) {
    				otherbar.setVisibility(View.VISIBLE);
    				morbar.setVisibility(View.GONE);
    				morebar_tileText.setText(R.string.setting_playmode);
    				ListView listView = (ListView)findViewById(R.id.AudioListView);
                    listView.setAdapter(getMorebarListAdapter(PLAY_MODE, m_playmode - 1));
    				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {	
    				    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				    	if (position == 0)
    				    		m_playmode = REPEATLIST;
    				    	else if (position == 1)
    				    		m_playmode = REPEATONE;
    				    	otherbar.setVisibility(View.GONE);
    				    	morbar.setVisibility(View.VISIBLE);
    				    }
    				});
    				otherbar.requestFocus();
    			}
    		});
    	}
    	else {
    		playmode.setImageDrawable(getResources().getDrawable(R.drawable.mode_disable));
    	}
		if(SystemProperties.getBoolean("mbx.3D_Bright.enable", true))
        {
    	ImageButton play3d = (ImageButton) findViewById(R.id.Play3DBtn);
    	play3d.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
                otherbar.setVisibility(View.VISIBLE);
                morbar.setVisibility(View.GONE);
                	
                morebar_tileText.setText(R.string.setting_3d_mode);
                ListView listView = (ListView)findViewById(R.id.AudioListView);
                listView.setAdapter(getMorebarListAdapter(PLAY3D, mode_3d));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					    mode_3d = position;
                    	switch (position) {
                    		case 0:
							    try {
								    m_Amplayer.Set3Dmode(Mode_3D.MODE_DISABLE.ordinal());
								} 
								catch(RemoteException e) {
								    e.printStackTrace();
								}
                    			break;
                    		case 1:
							    try {
								    m_Amplayer.Set3Dmode(Mode_3D.MODE_LR.ordinal());
								} 
								catch(RemoteException e) {
								    e.printStackTrace();
								}
                    			break;
                    		case 2:
							    try {
								    m_Amplayer.Set3Dmode(Mode_3D.MODE_BT.ordinal());
								} 
								catch(RemoteException e) {
								    e.printStackTrace();
								}
                    			break;
                    		case 3:
							    try {
								    m_Amplayer.Set3Dmode(Mode_3D.MODE_LR_SWITCH.ordinal());
								} 
								catch(RemoteException e) {
								    e.printStackTrace();
								}
                    			break;
                    		case 4:
							    try {
								    m_Amplayer.Set3Dmode(Mode_3D.MODE_3D_TO_2D_L.ordinal());
								} 
								catch(RemoteException e) {
								    e.printStackTrace();
								}
                    			break;
                    		case 5:
							    try {
								    m_Amplayer.Set3Dmode(Mode_3D.MODE_3D_TO_2D_R.ordinal());
								} 
								catch(RemoteException e) {
								    e.printStackTrace();
								}
                    			break;
                    		case 6:
							    try {
								    m_Amplayer.Set3Dmode(Mode_3D.MODE_2D_TO_3D.ordinal());
								} 
								catch(RemoteException e) {
								    e.printStackTrace();
								}
                    			break;
                    		case 7:
							    try {
								    m_Amplayer.Set3Dmode(Mode_3D.MODE_FIELD_DEPTH.ordinal());
								} 
								catch(RemoteException e) {
								    e.printStackTrace();
								}
                    			break;
                    		default:
                    			break;
                    	}
                    	otherbar.setVisibility(View.GONE);
                    	morbar.setVisibility(View.VISIBLE);
                    }
                });    
                otherbar.requestFocus();
            } 
    	});
		}
		ImageButton audiochannel = (ImageButton) findViewById(R.id.AudioChannelBtn);
		audiochannel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				otherbar.setVisibility(View.VISIBLE);
    			morbar.setVisibility(View.GONE);
    			morebar_tileText.setText(R.string.setting_audiochannel);
    			ListView listView = (ListView)findViewById(R.id.AudioListView);
                listView.setAdapter(getMorebarListAdapter(AUDIO_CHANNEL, cur_audio_channel));
    			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {	
				    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						Log.d(TAG,"set audio channel "+String.valueOf(arg2));
						cur_audio_channel = arg2;
						try
						{
							m_Amplayer.SwitchAudioChannel(cur_audio_channel);
						}
						catch (RemoteException e) {
							e.printStackTrace();
						}
						otherbar.setVisibility(View.GONE);
				    	morbar.setVisibility(View.VISIBLE);
				    }	
				});
				otherbar.requestFocus();
			}
		});

    	ImageButton audiotrack = (ImageButton) findViewById(R.id.ChangetrackBtn);
    	audiotrack.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			otherbar.setVisibility(View.VISIBLE);
    			morbar.setVisibility(View.GONE);
    			morebar_tileText.setText(R.string.setting_audiotrack);
    			ListView listView = (ListView)findViewById(R.id.AudioListView);
                listView.setAdapter(getMorebarListAdapter(AUDIOTRACK, cur_audio_stream));
    			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {	
    			    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    			    	if (bMediaInfo.getAudioTrackCount()>1) {
    			    		try {
    			    			m_Amplayer.SwitchAID(AudioTrackOperation.AudioStreamInfo.get(arg2).audio_id);
    			    			Log.d("audiostream","change audio stream to: " + arg2);
    			    			cur_audio_stream = arg2;
    			    		}
    			    		catch (RemoteException e) {
    			    			e.printStackTrace();
    			    		}
    			    		try {
    			    			m_Amplayer.GetMediaInfo();
    			    		} 
    			    		catch (RemoteException e) {
    			    			e.printStackTrace();
    			    		}
    			    	}
    			    	otherbar.setVisibility(View.GONE);
    			    	morbar.setVisibility(View.VISIBLE);
    			    }	
    			});
    			otherbar.requestFocus();
    		} 
    	});
    	
    	ImageButton subtitle = (ImageButton) findViewById(R.id.SubtitleBtn);
    	subtitle.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			if(sub_para.totalnum<=0) {
    				Toast toast =Toast.makeText(playermenu.this, R.string.sub_no_subtitle,Toast.LENGTH_SHORT );
    				toast.setGravity(Gravity.BOTTOM,110,0);
    				toast.setDuration(0x00000001);
    				toast.show();
    				return;
    			}
    			subbar.setVisibility(View.VISIBLE);
    			morbar.setVisibility(View.GONE);
    			subtitle_control();
    			subbar.requestFocus();
    		}
    		
    		String color_text[]={ 
    				playermenu.this.getResources().getString(R.string.color_white),
    				playermenu.this.getResources().getString(R.string.color_yellow),
        			playermenu.this.getResources().getString(R.string.color_blue)
        	};

    		private void subtitle_control() {
    			t_subswitch =(TextView)findViewById(R.id.sub_swith111);
    			t_subsfont =(TextView)findViewById(R.id.sub_font111);
    			t_subscolor =(TextView)findViewById(R.id.sub_color111);
    			
    			sub_switch_state = sub_para.curid;
    			sub_font_state = sub_para.font;
    			
    			if(sub_para.color==android.graphics.Color.WHITE)
    				sub_color_state =0;
    			else if(sub_para.color==android.graphics.Color.YELLOW)
    				sub_color_state =1;
    			else
    				sub_color_state =2;
    			
    			if(sub_para.curid==sub_para.totalnum)
    				t_subswitch.setText(R.string.str_off);
    			else
    				t_subswitch.setText(String.valueOf(sub_para.curid+1)+"/"+String.valueOf(sub_para.totalnum));
    			
    			t_subsfont.setText(String.valueOf(sub_font_state));
    			t_subscolor.setText(color_text[sub_color_state]);
    			
    			Button ok = (Button) findViewById(R.id.button_ok);
    			ok.setOnClickListener(new View.OnClickListener() {	
    			    public void onClick(View v) {
    			    	sub_para.curid = sub_switch_state;
    			    	sub_para.font = sub_font_state;
    			    	
    			    	if(sub_para.curid==sub_para.totalnum )
    			    		sub_para.sub_id =null;
    			    	else
    			    		sub_para.sub_id =subMange.getSubID(sub_para.curid);
    			    	
    			    	if(sub_color_state==0)
    			    		sub_para.color =android.graphics.Color.WHITE;
    			    	else if(sub_color_state==1) 
    			    		sub_para.color =android.graphics.Color.YELLOW;
    			    	else
    			    		sub_para.color =android.graphics.Color.BLUE;
    			    	
    			    	subbar.setVisibility(View.GONE);
    			    	videobar();
    			    } 
    			});
    			Button cancel = (Button) findViewById(R.id.button_canncel);
    			cancel.setOnClickListener(new View.OnClickListener() {
  		            public void onClick(View v) {
  		            	subbar.setVisibility(View.GONE);
  		            	videobar();
  		            } 
  		        });
    			ImageButton Bswitch_l = (ImageButton) findViewById(R.id.switch_l);	
    			ImageButton Bswitch_r = (ImageButton) findViewById(R.id.switch_r);
    			ImageButton Bfont_l = (ImageButton) findViewById(R.id.font_l);	
    			ImageButton Bfont_r = (ImageButton) findViewById(R.id.font_r);
    			ImageButton Bcolor_l = (ImageButton) findViewById(R.id.color_l);	
    			ImageButton Bcolor_r = (ImageButton) findViewById(R.id.color_r);
    			Bswitch_l.setOnClickListener(new View.OnClickListener() {
  					public void onClick(View v) {
  						if(sub_switch_state <= 0)
  							sub_switch_state =sub_para.totalnum;
   						else
   							sub_switch_state --;
   							
  						if(sub_switch_state==sub_para.totalnum)
  							t_subswitch.setText(R.string.str_off);
  						else
  							t_subswitch.setText(String.valueOf(sub_switch_state+1)+"/"+String.valueOf(sub_para.totalnum));
   		            } 
  				});
  				Bswitch_r.setOnClickListener(new View.OnClickListener() {
  					public void onClick(View v) {
  						if(sub_switch_state >= sub_para.totalnum)
  							sub_switch_state =0;
   						else
   							sub_switch_state ++;
   							
  						if(sub_switch_state==sub_para.totalnum)
  							t_subswitch.setText(R.string.str_off);
  						else
  							t_subswitch.setText(String.valueOf(sub_switch_state+1)+"/"+String.valueOf(sub_para.totalnum));;
   		            } 
  				});
  				
  				if (sub_para.sub_id != null) {
	  				if(sub_para.sub_id.filename.equals("INSUB")||sub_para.sub_id.filename.endsWith(".idx")) {
  						TextView font =(TextView)findViewById(R.id.font_title);
						TextView color =(TextView)findViewById(R.id.color_title);
							
						font.setTextColor(android.graphics.Color.LTGRAY);
						color.setTextColor(android.graphics.Color.LTGRAY);
							
  						t_subsfont.setTextColor(android.graphics.Color.LTGRAY);
  						t_subscolor.setTextColor(android.graphics.Color.LTGRAY);	
  							
  					    Bfont_l.setEnabled(false);
  	  					Bfont_r.setEnabled(false);
  	  					Bcolor_l.setEnabled(false);
  	  					Bcolor_r.setEnabled(false);
  	  					Bfont_l.setImageResource(R.drawable.fondsetup_larrow_disable);
  	  					Bfont_r.setImageResource(R.drawable.fondsetup_rarrow_disable);
  	  					Bcolor_l.setImageResource(R.drawable.fondsetup_larrow_disable);
  	  					Bcolor_r.setImageResource(R.drawable.fondsetup_rarrow_disable);
  	  					return;
	  				}
  				}
  				Bfont_l.setOnClickListener(new View.OnClickListener() {
  					public void onClick(View v) {
  						if(sub_font_state > 12)
  							sub_font_state =sub_font_state-2;
  						else
  							sub_font_state =30;
  							 
  						t_subsfont.setText(String.valueOf(sub_font_state));	 
   		            } 
  				});
  				Bfont_r.setOnClickListener(new View.OnClickListener() {
  					public void onClick(View v) {
  						if(sub_font_state < 30)
  							sub_font_state =sub_font_state +2;
  						else
  							sub_font_state =12;
  							
  						t_subsfont.setText(String.valueOf(sub_font_state));
   		            } 
  				});
  					
  				Bcolor_l.setOnClickListener(new View.OnClickListener() {
  					public void onClick(View v) {
  						if(sub_color_state<= 0)
  							sub_color_state=2;
   						else 
   							sub_color_state-- ;
   							 
   						t_subscolor.setText(color_text[sub_color_state]);
   		            } 
  				});
  				Bcolor_r.setOnClickListener(new View.OnClickListener() {
  					public void onClick(View v) {
  						if(sub_color_state>=2)
   							sub_color_state=0;
    					else 
    						sub_color_state++ ;
    							 
  						t_subscolor.setText(color_text[sub_color_state]);
   		            } 
  				});
			} 
    	});
    	
    	ImageButton display = (ImageButton) findViewById(R.id.DisplayBtn);
    	display.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
                otherbar.setVisibility(View.VISIBLE);
                morbar.setVisibility(View.GONE);
                	
                morebar_tileText.setText(R.string.setting_displaymode);
                ListView listView = (ListView)findViewById(R.id.AudioListView);
                listView.setAdapter(getMorebarListAdapter(DISPLAY, ScreenMode.getScreenMode()));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    	switch (position) {
                    		case ScreenMode.NORMAL:
                    			ScreenMode.setScreenMode("0");
                    			break;
                    		case ScreenMode.FULLSTRETCH:
                    			ScreenMode.setScreenMode("1");
                    			break;
                    		case ScreenMode.RATIO4_3:
                    			ScreenMode.setScreenMode("2");
                    			break;
                    		case ScreenMode.RATIO16_9:
                    			ScreenMode.setScreenMode("3");
                    			break;
                            case ScreenMode.NORMAL_NOSCALEUP:
                                ScreenMode.setScreenMode("4");
                    			break;
                    		default:
                    			break;
                    	}
                    	otherbar.setVisibility(View.GONE);
                    	morbar.setVisibility(View.VISIBLE);
                    }
                });    
                otherbar.requestFocus();
            } 
    	});
    	if(SystemProperties.getBoolean("mbx.3D_Bright.enable", true))
        {
    	ImageButton brigtness = (ImageButton) findViewById(R.id.BrightnessBtn);
    	brigtness.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
                otherbar.setVisibility(View.VISIBLE);
                morbar.setVisibility(View.GONE);
                morebar_tileText.setText(R.string.setting_brightness);
                ListView listView = (ListView)findViewById(R.id.AudioListView);
				int mBrightness = 0;
				try {
					mBrightness = Settings.System.getInt(playermenu.this.getContentResolver(), 
						   Settings.System.SCREEN_BRIGHTNESS);
				} 
				catch (SettingNotFoundException e) {
					e.printStackTrace();
				}
				int item;
				if (mBrightness <= (android.os.Power.BRIGHTNESS_DIM + 10))
					item = 0;
				else if (mBrightness <= (android.os.Power.BRIGHTNESS_ON * 0.2f))
					item = 1;
				else if (mBrightness <= (android.os.Power.BRIGHTNESS_ON * 0.4f))
					item = 2;
				else if (mBrightness <= (android.os.Power.BRIGHTNESS_ON * 0.6f))
					item = 3;
				else if (mBrightness <= (android.os.Power.BRIGHTNESS_ON * 0.8f))
					item = 4;
				else
					item = 5;

                listView.setAdapter(getMorebarListAdapter(BRIGHTNESS, item));
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						int brightness;
                        switch(position) {
                        	case 0:
                        	 	brightness = android.os.Power.BRIGHTNESS_DIM + 10;
                        		break;
                        	case 1:
                        		brightness = (int)(android.os.Power.BRIGHTNESS_ON * 0.2f);
                        		break;
                        	case 2:
                        		brightness = (int)(android.os.Power.BRIGHTNESS_ON * 0.4f);
                        	 	break;
                        	case 3:
                        		brightness = (int)(android.os.Power.BRIGHTNESS_ON * 0.6f);
                        	 	break;	 
							case 4:
                        		brightness = (int)(android.os.Power.BRIGHTNESS_ON * 0.8f);
                        	 	break;
							case 5:
                        		brightness = android.os.Power.BRIGHTNESS_ON;
                        	 	break;
                        	default:
								brightness = android.os.Power.BRIGHTNESS_DIM + 30;
                        		break;
                        }
                        try {
					        IPowerManager power = IPowerManager.Stub.asInterface(
					            ServiceManager.getService("power"));
					        if (power != null) {
					        	power.setBacklightBrightness(brightness);
					        	Settings.System.putInt(playermenu.this.getContentResolver(), 
				                    	Settings.System.SCREEN_BRIGHTNESS, brightness);
					        }
					    } 
                        catch (RemoteException doe) {
					    }  
                        otherbar.setVisibility(View.GONE);
                        morbar.setVisibility(View.VISIBLE);
                    }
                });
				otherbar.requestFocus();
			} 
    	}); 
    	}
    	ImageButton backtovidebar = (ImageButton) findViewById(R.id.BackBtn);
    	backtovidebar.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
                if (fb32) {
                    setContentView(R.layout.infobar32);
                } else {
                    setContentView(R.layout.infobar);
                }
                initinfobar();
                ImageButton morebtn = (ImageButton) findViewById(R.id.moreBtn);
                morebtn.requestFocus();
    			if(!SettingsVP.display_mode.equals("480p"))
    				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    		} 
    	}); 
    	
    	ImageButton fileinformation = (ImageButton) findViewById(R.id.InfoBtn);
    	fileinformation.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
				infodialog.setVisibility(View.VISIBLE);
				morbar.setVisibility(View.GONE);
				TextView title = (TextView)findViewById(R.id.info_title);
				title.setText(R.string.str_file_information);
					
				String fileinf = null;
				TextView filename = (TextView)findViewById(R.id.filename);
                fileinf = playermenu.this.getResources().getString(R.string.str_file_name)
        			+ "\t: " + bMediaInfo.getFileName(PlayList.getinstance().getcur());
				filename.setText(fileinf);

				TextView filetype = (TextView)findViewById(R.id.filetype);
                fileinf = playermenu.this.getResources().getString(R.string.str_file_format)
        			+ "\t: " + bMediaInfo.getFileType();
				filetype.setText(fileinf);
					
				TextView filesize = (TextView)findViewById(R.id.filesize);
                fileinf = playermenu.this.getResources().getString(R.string.str_file_size)
        			+ "\t: " + bMediaInfo.getFileSize();
				filesize.setText(fileinf);
					
				TextView resolution = (TextView)findViewById(R.id.resolution);
                fileinf = playermenu.this.getResources().getString(R.string.str_file_resolution)
        			+ "\t: " + bMediaInfo.getResolution();
                resolution.setText(fileinf);
					
				TextView duration = (TextView)findViewById(R.id.duration);
                fileinf = playermenu.this.getResources().getString(R.string.str_file_duration)
        			+ "\t: " + secToTime(bMediaInfo.duration, true);
				duration.setText(fileinf);
					
				Button ok = (Button)findViewById(R.id.info_ok);
				ok.setText("OK");
				ok.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
                        infodialog.setVisibility(View.GONE);
                        morbar.setVisibility(View.VISIBLE);
					}
				});
				infodialog.requestFocus();								
            } 
    	}); 
    }
    
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            touchVolFlag = false;
            waitForHide();
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        Log.i(TAG, "onKeyDown " + keyCode);

        if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
            if (infobar.getVisibility() == View.VISIBLE)
                waitForHide();
        }

        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            touchVolFlag = true;
        } 
		else if (keyCode == KeyEvent.KEYCODE_POWER) {
    		if (player_status == VideoInfo.PLAYER_RUNNING) {
    			try {
    				m_Amplayer.Pause();
    			} 
    			catch(RemoteException e) {
    				e.printStackTrace();
    			}
    		}
    		mSuspendFlag = true;
    		openScreenOffTimeout();
    		return true;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if (morbar!=null)  {
	        	morbar=null;
                if (fb32) {
                    setContentView(R.layout.infobar32);
                } 
				else {
                    setContentView(R.layout.infobar);
                }
	        	initinfobar();
	        	return(true);
	        }
    		else {
    			if(m_Amplayer == null)
					return (true);
                // close sub
    			Intent selectFileIntent = new Intent();
				selectFileIntent.setClass(playermenu.this, FileList.class);
				//close sub;
				if(subTitleView!=null)
					subTitleView.closeSubtitle();	
                if (!fb32) {
                    // Hide the view with key color
                    LinearLayout layout = (LinearLayout) findViewById(R.id.BaseLayout1);
                    if (layout != null) {
                        layout.setVisibility(View.INVISIBLE);
                        layout.invalidate();
                    }
                }
				//stop play
				backToFileList = true;
				if(m_Amplayer != null)
					Amplayer_stop();
				startActivity(selectFileIntent);
				playermenu.this.finish();
				return true;
    		}
    	}
		else if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_9) {
    		if (infobar.getVisibility() == View.VISIBLE)
	    		hide_infobar();
	    	else {
	    		play.requestFocus();
		    	show_menu();
                waitForHide();
	    	}
			return (true);
		}
    	else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            play.requestFocus();

            if (infobar.getVisibility() != View.VISIBLE) {
                show_menu();
                waitForHide();
            }

			if (player_status == VideoInfo.PLAYER_RUNNING) {
				try	{
					m_Amplayer.Pause();
				} 
				catch(RemoteException e) {
					e.printStackTrace();
				}
			}
			else if (player_status == VideoInfo.PLAYER_PAUSE) {
				try	{
					m_Amplayer.Resume();
				} 
				catch(RemoteException e)	{
					e.printStackTrace();
				}
			}
			else if (player_status == VideoInfo.PLAYER_SEARCHING) {
				try	{
					if (FF_FLAG)
						m_Amplayer.FastForward(0);
					else
						m_Amplayer.BackForward(0);
				} 
				catch(RemoteException e) {
					e.printStackTrace();
				}
			}
			return (true);
		}
    	else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
			if (!INITOK)
				return false;
            ImageButton preItem = (ImageButton) findViewById(R.id.PreBtn);
            preItem.requestFocus();

            if (infobar.getVisibility() != View.VISIBLE) {
                show_menu();
                waitForHide();
            }

			ResumePlay.saveResumePara(PlayList.getinstance().getcur(), curtime);
			String filename = PlayList.getinstance().moveprev();
			toast.cancel();
			toast.setText(filename);
			toast.show();
			playPosition = 0;
			if(m_Amplayer == null)
				return false; 
			else
				Amplayer_stop();
			PRE_NEXT_FLAG = 1;	 		
    	}
    	else if(keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
			if (!INITOK)
				return false;
            ImageButton nextItem = (ImageButton) findViewById(R.id.NextBtn);
            nextItem.requestFocus();

            if (infobar.getVisibility() != View.VISIBLE) {
                show_menu();
                waitForHide();
            }

			ResumePlay.saveResumePara(PlayList.getinstance().getcur(), curtime);
			String filename = PlayList.getinstance().movenext();
			toast.cancel();
			toast.setText(filename); 
			toast.show();
			playPosition = 0;
			if(m_Amplayer == null)
				return false;
			else
				Amplayer_stop();
			PRE_NEXT_FLAG = 1;		    		   		
    	}
    	else if(keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
			if (!INITOK)
				return false;

            fastforword.requestFocus();

            if (infobar.getVisibility() != View.VISIBLE) {
                show_menu();
                waitForHide();
            }

			if (player_status == VideoInfo.PLAYER_SEARCHING) {
				try	{
					m_Amplayer.FastForward(0);
				} 
				catch(RemoteException e) {
					e.printStackTrace();
				}
			}
			else {
				try	{
					m_Amplayer.FastForward(2);
				} 
				catch(RemoteException e) {
					e.printStackTrace();
				}
				FF_FLAG = true;
			}		 
    	}
    	else if(keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
			if (!INITOK)
				return false;

            fastreverse.requestFocus();

            if (infobar.getVisibility() != View.VISIBLE) {
                show_menu();
                waitForHide();
            }

            if (player_status == VideoInfo.PLAYER_SEARCHING) {
                try {
                    m_Amplayer.BackForward(0);
                } 
				catch (RemoteException e) {
                    e.printStackTrace();
                }
            } 
			else {
                try {
                    m_Amplayer.BackForward(2);
                } 
				catch (RemoteException e) {
                    e.printStackTrace();
                }
                FF_FLAG = false;
            }
        } 
    	else if (keyCode == KeyEvent.KEYCODE_MEDIA_REPEAT) {
    		videobar();
    		ImageButton subtitle = (ImageButton) findViewById(R.id.PlaymodeBtn);
    		subtitle.requestFocusFromTouch();
    		return (true);
    	}
      	else if (keyCode == KeyEvent.KEYCODE_MEDIA_INFO) {
    		videobar();
    		ImageButton subtitle = (ImageButton) findViewById(R.id.InfoBtn);
    		subtitle.requestFocusFromTouch();
    		return (true);
    	}
      	else if (keyCode == KeyEvent.KEYCODE_MEDIA_RATIO) {
    		videobar();
    		ImageButton subtitle = (ImageButton) findViewById(R.id.DisplayBtn);
    		subtitle.requestFocusFromTouch();
    		return (true);
    	}
      	else if (keyCode == KeyEvent.KEYCODE_MEDIA_SUB_T) {
    		videobar();
    		ImageButton subtitle = (ImageButton) findViewById(R.id.SubtitleBtn);
    		subtitle.requestFocusFromTouch();
    		return (true);
    	}
      	else if (keyCode == KeyEvent.KEYCODE_MEDIA_AUDIO) {
    		videobar();
    		ImageButton subtitle = (ImageButton) findViewById(R.id.ChangetrackBtn);
    		subtitle.requestFocusFromTouch();
    		return (true);
    	}
        else
        	return super.onKeyDown(keyCode, msg);
    	return (true);
    }
    
    public void onCreate(Bundle savedInstanceState) {
        fb32 = SystemProperties.get("sys.fb.bits", "16").equals("32");

        if (fb32) {
            setTheme(R.style.theme_trans);
        }

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (AmPlayer.getProductType() == 1)
        	AmPlayer.disable_freescale(MID_FREESCALE);
        //fixed bug for green line
        FrameLayout foreground = (FrameLayout)findViewById(android.R.id.content);
        foreground.setForeground(null);

        if (fb32) {
            setContentView(R.layout.infobar32);
        } else {
            setContentView(R.layout.infobar);
        }
        toast = Toast.makeText(playermenu.this, "", Toast.LENGTH_SHORT);

        infobar = (LinearLayout) findViewById(R.id.infobarLayout);
        if (infobar != null)
            infobar.setVisibility(View.GONE);

        mScreenLock = ((PowerManager)this.getSystemService(Context.POWER_SERVICE)).newWakeLock(
        		PowerManager.SCREEN_BRIGHT_WAKE_LOCK,TAG);
        closeScreenOffTimeout();
        Intent it = this.getIntent();
        playmode_switch = true;
        if (it.getData() != null) {
        	if (it.getData().getScheme().equals("file")) {
        		List<String> paths = new ArrayList<String>();
                paths.add(it.getData().getPath());
                PlayList.getinstance().setlist(paths, 0);
        	}
        	else {
                Cursor cursor = managedQuery(it.getData(), null, null, null, null);
                cursor.moveToFirst();

                int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                if ((index == -1) || (cursor.getCount() <= 0)) {
                    Log.d(TAG, "Cursor empty or failed\n"); 
                }
                else {
                    List<String> paths = new ArrayList<String>();
                    cursor.moveToFirst();

                    paths.add(cursor.getString(index));
                    PlayList.getinstance().setlist(paths, 0);
                    
                    playmode_switch = false;
                    Log.d(TAG, "index = " + index);
                    Log.d(TAG, "From content providor DATA:" + cursor.getString(index));
                    Log.d(TAG, " -- MIME_TYPE :" + 
                    		cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)));
                }
        	}
        }
		mode_3d = 0;
        SettingsVP.init(this);
        SettingsVP.setVideoLayoutMode();
        SettingsVP.enableVideoLayout();
		subinit();
		initinfobar();
		IntentFilter intentFilter = new IntentFilter(ACTION_HDMISWITCH_MODE_CHANGED);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (ACTION_HDMISWITCH_MODE_CHANGED.equals(intent.getAction())) {			 
					Intent selectFileIntent = new Intent();
					selectFileIntent.setClass(playermenu.this, FileList.class);	
					backToFileList = true;
					startActivity(selectFileIntent);
					playermenu.this.finish();
				}
			}
		};
		registerReceiver(mReceiver, intentFilter);
		
        mWindowManager = getWindowManager();
        setAngleTable();
        
		if (SettingsVP.getParaBoolean("ResumeMode"))
			resumePlay();
		else {
			if (!NOT_FIRSTTIME)
    			StartPlayerService();
        	else
        		Amplayer_play();
		}

        if (infobar != null) {
            infobar.setVisibility(View.VISIBLE);
            ImageButton browser = (ImageButton) findViewById(R.id.BrowserBtn);
            browser.requestFocus();
        }
    }
    
    protected void subinit() {
        subMange = new SubtitleUtils(PlayList.getinstance().getcur());
        sub_para = new subview_set();
         
        sub_para.totalnum = 0;
        sub_para.curid = 0;
        sub_para.color = android.graphics.Color.WHITE;
    	sub_para.font=20;
        sub_para.sub_id = null;
    }
    
    private static String do_exec(String[] cmd) {
        String s = "\n";
        try {
            java.lang.Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cmd.toString();
    }
    
    protected void initinfobar() {
    	//set subtitle
    	subTitleView = (SubtitleView) findViewById(R.id.subTitle);
    	subTitleView.setTextColor(sub_para.color);
    	subTitleView.setTextSize(sub_para.font);
    	openFile(sub_para.sub_id);
	
        ImageButton browser = (ImageButton)findViewById(R.id.BrowserBtn);
        ImageButton more = (ImageButton)findViewById(R.id.moreBtn);
        ImageButton preItem = (ImageButton)findViewById(R.id.PreBtn);
        ImageButton nextItem = (ImageButton)findViewById(R.id.NextBtn);
        play = (ImageButton)findViewById(R.id.PlayBtn);
        fastforword = (ImageButton)findViewById(R.id.FastForward);
        fastreverse = (ImageButton)findViewById(R.id.FastReverse);
        infobar = (LinearLayout)findViewById(R.id.infobarLayout);
        if (SettingsVP.display_mode.equals("480p"))
        {
        	infobar = (LinearLayout)findViewById(R.id.infobarLayout);
        	LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) infobar.getLayoutParams();
        	if(SettingsVP.panel_resolution.equals("800x600")) {
            	linearParams.width = 710;
            	linearParams.height = 90;
            	linearParams.leftMargin = 5;
            	linearParams.bottomMargin = 140;
        	}
        	else if(SettingsVP.panel_resolution.equals("800x480")) {
            	linearParams.width = 710;
            	linearParams.height = 90;
            	linearParams.leftMargin = 5;
            	linearParams.bottomMargin = 10;
        	}
        	else {
            	linearParams.width = 710;
            	linearParams.height = 90;
            	linearParams.leftMargin = 5;
            	linearParams.bottomMargin = 140;
        	}
        	linearParams.gravity = -1;
        	infobar.setLayoutParams(linearParams);
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        myProgressBar = (SeekBar)findViewById(R.id.SeekBar02);
    	cur_time = (TextView)findViewById(R.id.TextView03);
    	total_time = (TextView)findViewById(R.id.TextView04);
    	cur_time.setText(secToTime(curtime, false));
    	total_time.setText(secToTime(totaltime, true));
    	if (bMediaInfo != null)
    	{
	    	if (bMediaInfo.seekable == 0)
			{
				myProgressBar.setEnabled(false);
				fastforword.setEnabled(false);
				fastreverse.setEnabled(false);
				fastforword.setImageResource(R.drawable.ff_disable);
				fastreverse.setImageResource(R.drawable.rewind_disable);
			}
    	}
    	
        browser.setOnClickListener(new ImageButton.OnClickListener()
    	{
			public void onClick(View v) 
			{
			// TODO Auto-generated method stub
				Intent selectFileIntent = new Intent();
				selectFileIntent.setClass(playermenu.this, FileList.class);
				//close sub;
				if(subTitleView!=null)
					subTitleView.closeSubtitle();	
				//stop play
				backToFileList = true;
				if(m_Amplayer != null)
					Amplayer_stop();
				startActivity(selectFileIntent);
				playermenu.this.finish();
			}
		});
        
        preItem.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!INITOK)
					return;
				ResumePlay.saveResumePara(PlayList.getinstance().getcur(), curtime);
				String filename = PlayList.getinstance().moveprev();
				toast.cancel();
				toast.setText(catShowFilePath(filename));
				toast.show();
				playPosition = 0;
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
				if (!INITOK)
					return;
				ResumePlay.saveResumePara(PlayList.getinstance().getcur(), curtime);
				String filename = PlayList.getinstance().movenext();
				toast.cancel();
				toast.setText(catShowFilePath(filename)); 
				toast.show();
				playPosition = 0;
				if(m_Amplayer == null)
					return;
				else
					Amplayer_stop();
				PRE_NEXT_FLAG = 1;
			}
        });
        
        if (player_status == VideoInfo.PLAYER_RUNNING)
			play.setImageResource(R.drawable.pause);
        play.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (player_status == VideoInfo.PLAYER_RUNNING)
				{
					try	{
						m_Amplayer.Pause();
					} catch(RemoteException e) {
						e.printStackTrace();
					}
				}
				else if (player_status == VideoInfo.PLAYER_PAUSE)
				{
					try	{
						m_Amplayer.Resume();
					} catch(RemoteException e)	{
						e.printStackTrace();
					}
				}
				else if (player_status == VideoInfo.PLAYER_SEARCHING)
				{
					try	{
						if (FF_FLAG)
							m_Amplayer.FastForward(0);
						else
							m_Amplayer.BackForward(0);
					} catch(RemoteException e) {
						e.printStackTrace();
					}
				}
			}
        });
                
        fastforword.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				if (!INITOK)
					return;
				if (player_status == VideoInfo.PLAYER_SEARCHING)
				{
					try	{
						m_Amplayer.FastForward(0);
					} catch(RemoteException e) {
						e.printStackTrace();
					}
				}
				else
				{
					try	{
						m_Amplayer.FastForward(2);
					} catch(RemoteException e) {
						e.printStackTrace();
					}
					FF_FLAG = true;
				}
			}
        });
        
        fastreverse.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				if (!INITOK)
					return;
				if (player_status == VideoInfo.PLAYER_SEARCHING)
				{
					try	{
						m_Amplayer.BackForward(0);
					} catch(RemoteException e) {
						e.printStackTrace();
					}
				}
				else
				{
					try	{
						m_Amplayer.BackForward(2);
					} catch(RemoteException e) {
						e.printStackTrace();
					}
					FF_FLAG = false;
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
        
        if (curtime != 0)
        	myProgressBar.setProgress(curtime*100/totaltime);
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
				waitForHide();
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) 
			{
				// TODO Auto-generated method stub
				timer.cancel();
				progressSliding = true;
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) 
			{
				// TODO Auto-generated method stub
				
			}
		});
        
        waitForHide();
    }
	
    private String catShowFilePath(String path) {
    	String text = null;
    	if(path.startsWith("/mnt/flash"))
    		text=path.replaceFirst("/mnt/flash","/mnt/flash");
    	else if(path.startsWith("/mnt/sda"))
    		text=path.replaceFirst("/mnt/sda","/mnt/sda");
    	else if(path.startsWith("/mnt/sdb"))
    		text=path.replaceFirst("/mnt/sdb","/mnt/sdb");
    	else if(path.startsWith("/mnt/sdcard"))
    		text=path.replaceFirst("/mnt/sdcard","/mnt/sdcard");
    	return text;
    }
    
    public static int setCodecMips()
	{
    	int tmp;
    	String buf = null;
		File file = new File(InputFile);
		if (!file.exists()) {        	
        	return 0;
        }
		file = new File(OutputFile);
		if (!file.exists()) {        	
        	return 0;
        }
		//read
		try {
			BufferedReader in = new BufferedReader(new FileReader(InputFile), 32);
			try {
				codec_mips = in.readLine();
				Log.d(TAG, "file content:"+codec_mips);
				tmp = Integer.parseInt(codec_mips)*2;
				buf = Integer.toString(tmp);
			} finally {
    			in.close();
    		} 
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "IOException when read "+InputFile);
		} 
		
		//write
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(OutputFile), 32);
    		try {
    			out.write(buf);    
    			Log.d(TAG, "set codec mips ok:"+buf);
    		} finally {
				out.close();
			}
			 return 1;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "IOException when write "+OutputFile);
			return 0;
		}
	}
    
    public static int setDefCodecMips() {
    	File file = new File(OutputFile);
		if (!file.exists()) {        	
        	return 0;
        }
		if (codec_mips == null)
			return 0;
    	try {
			BufferedWriter out = new BufferedWriter(new FileWriter(OutputFile), 32);
    		try {
    			out.write(codec_mips);    
    			Log.d(TAG, "set codec mips ok:"+codec_mips);
    		} finally {
				out.close();
			}
			 return 1;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "IOException when write "+OutputFile);
			return 0;
		}
    }
    
    protected void closeScreenOffTimeout() {
    	if(mScreenLock.isHeld() == false)
    		mScreenLock.acquire();
    }
    
    protected void openScreenOffTimeout() {
    	if(mScreenLock.isHeld() == true)
    		mScreenLock.release();
    }
    
    protected void waitForHide() {
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
                if (!touchVolFlag) {
                    Message message = Message.obtain();
                    message.what = 0x3c;       
                    handler.sendMessage(message);     
                }   
            }
        };   
        
        timer.cancel();
        timer = new Timer();
    	timer.schedule(task, 3000);
    }
    
    protected void ResumeCountdown() {
    	final Handler handler = new Handler(){   	  
            public void handleMessage(Message msg) {   
                switch (msg.what) {       
                case 0x3d:
                	if (confirm_dialog.isShowing()) {
	                	if (resumeSecond > 0) {
	                		String cancel = playermenu.this.getResources().getString(R.string.str_cancel);
	                		confirm_dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
	                			.setText(cancel+" ( "+(--resumeSecond)+" )");
	                		ResumeCountdown();
	                	}
	                	else {
	                		playPosition = 0;
				        	confirm_dialog.dismiss();
				        	resumeSecond = 8;
	                	}
                	}
                    break;       
                }       
                super.handleMessage(msg);   
            }  
        };
		   
        TimerTask task = new TimerTask(){   
            public void run() {   
                Message message = Message.obtain();
                message.what = 0x3d;       
                handler.sendMessage(message);     
            }   
               
        };   
        Timer resumeTimer = new Timer();
        resumeTimer.schedule(task, 1000);
    }
    
    protected void hide_infobar() {
    	infobar.setVisibility(View.GONE);
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
    			WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    protected void show_menu() {
    	infobar.setVisibility(View.VISIBLE);
    	if(!SettingsVP.display_mode.equals("480p"))
    		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    public boolean onTouchEvent (MotionEvent event) {
    	super.onTouchEvent(event);
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
	    	if (infobar.getVisibility() == View.VISIBLE)
	    		hide_infobar();
	    	else {
		    	show_menu();
		    	waitForHide();
	    	}
    	}
    	return true;
    }
    
    private String secToTime(int i, Boolean isTotalTime) {
		String retStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (i <= 0) {
			if (isTotalTime && i<0)
				return "99:59:59";
			else
				return "00:00:00";
		}
		else {
			minute = i/60;
			if (minute < 60) {
				second = i%60;
				retStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
			}
			else {
				hour = minute/60;
				if (hour > 99)
					return "99:59:59";
				minute = minute%60;
				second = i%60;
				retStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
			}
		}
		return retStr;
	}
	
	private String unitFormat(int i) {
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Integer.toString(i);
		else
			retStr = Integer.toString(i);
		return retStr;
    }
	
	@Override
    public void onDestroy() {
        ResumePlay.saveResumePara(PlayList.getinstance().getcur(), curtime);
        //close sub;
        if(subTitleView!=null)
        	subTitleView.closeSubtitle();
        backToFileList = true;
        Amplayer_stop();
        if(m_Amplayer != null)
			try {
				m_Amplayer.DisableColorKey();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        StopPlayerService();
        setDefCodecMips();
        openScreenOffTimeout();
        SettingsVP.disableVideoLayout();
        SettingsVP.setVideoRotateAngle(0);
        unregisterReceiver(mReceiver);
        if (AmPlayer.getProductType() == 1) //1:MID 0:other
        	AmPlayer.enable_freescale(MID_FREESCALE);
        
        super.onDestroy();
    }

	@Override
    public void onPause() {
        super.onPause();
        StorageManager m_storagemgr = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        m_storagemgr.unregisterListener(mListener);
        if (mSuspendFlag){
            if (player_status == VideoInfo.PLAYER_RUNNING) {
                try{
                    m_Amplayer.Pause();
                } 
				catch(RemoteException e) {
                    e.printStackTrace();
                }
            }
            mSuspendFlag = false;
        }
        else {
            if (!backToFileList){
			    PlayList.getinstance().rootPath =null;
            }
            finish();
        }
    }
    
	//=========================================================
    private Messenger m_PlayerMsg = new Messenger(new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    			case VideoInfo.TIME_INFO_MSG:
    				//Log.i(TAG,"get time "+secToTime((msg.arg1)/1000));
    		    	cur_time.setText(secToTime((msg.arg1)/1000, false));
    		    	total_time.setText(secToTime(msg.arg2, true));
    		    	curtime = msg.arg1/1000;
    		    	totaltime = msg.arg2;
    		    	
                    boolean mVfdDisplay = SystemProperties.getBoolean("hw.vfd", false);
                    if (mVfdDisplay) {
                        String[] cmdtest = {
                            "/system/bin/sh",
                            "-c",
                            "echo" + " "
                                + cur_time.getText().toString().substring(1)
                                + " " + "> /sys/devices/platform/m1-vfd.0/led"
                        };
                        do_exec(cmdtest);
                    }

    		    	//for subtitle tick;
    		    	if (player_status == VideoInfo.PLAYER_RUNNING) {
    		    		if(subTitleView!=null&&sub_para.sub_id!=null)
    		    			subTitleView.tick(msg.arg1);
    		    	}
    		    	if (totaltime == 0)
						myProgressBar.setProgress(0);
					else {
						if (!progressSliding)
							myProgressBar.setProgress(msg.arg1/1000*100/totaltime);
					}
    				break;
    			case VideoInfo.STATUS_CHANGED_INFO_MSG:
    				player_status = msg.arg1;
    				
    				switch(player_status) {
					case VideoInfo.PLAYER_RUNNING:
						play.setImageResource(R.drawable.pause);
						break;
					case VideoInfo.PLAYER_PAUSE:
					case VideoInfo.PLAYER_SEARCHING:	
						play.setImageResource(R.drawable.play);
						break;
					case VideoInfo.PLAYER_EXIT:						
						if (PRE_NEXT_FLAG == 1 || (!backToFileList) ) {
    						Log.d(TAG,"to play another file!");
							//new PlayThread().start();
							if (SettingsVP.getParaBoolean("ResumeMode")) {
								if (resumePlay() == 0)
									Amplayer_play();
							} else {
								playPosition = 0;
								Amplayer_play();
							}
    						PRE_NEXT_FLAG = 0;
							progressSliding = false;
    					}
						if(subTitleView!=null)
							subTitleView.closeSubtitle();
						sub_para.totalnum = 0;
						cur_audio_stream = 0;
						InternalSubtitleInfo.setInsubNum(0);
						
						boolean mVfdDisplay_exit = SystemProperties.getBoolean("hw.vfd", false);
						if (mVfdDisplay_exit) {
						    String[] cmdtest = {
                                    "/system/bin/sh",
                                    "-c",
                                    "echo"
                                        + " "
                                        + "0:00:00"
                                        + " "
                                        + "> /sys/devices/platform/m1-vfd.0/led"
							};
							do_exec(cmdtest);
                        }
						break;
					case VideoInfo.PLAYER_STOPED:
						break;
					case VideoInfo.PLAYER_PLAYEND:
						try	{
							m_Amplayer.Close();
						} 
						catch(RemoteException e) {
							e.printStackTrace();
						}
						ResumePlay.saveResumePara(PlayList.getinstance().getcur(), 0);
						playPosition = 0;
						if (m_playmode == REPEATLIST)
							PlayList.getinstance().movenext();
						AudioTrackOperation.AudioStreamFormat.clear();
						AudioTrackOperation.AudioStreamInfo.clear();
						INITOK = false;
						PRE_NEXT_FLAG = 1;
						break;
					case VideoInfo.PLAYER_ERROR:
						String InfoStr = null;
						InfoStr = Errorno.getErrorInfo(msg.arg2);
						Toast.makeText(playermenu.this, "Status Error:"+InfoStr, Toast.LENGTH_LONG)
							.show();
						if (msg.arg2 == Errorno.FFMPEG_OPEN_FAILED
								|| msg.arg2 == Errorno.PLAYER_UNSUPPORT_VCODEC
								|| msg.arg2 == Errorno.DECODER_INIT_FAILED
								|| msg.arg2 == Errorno.PLAYER_UNSUPPORT
								|| msg.arg2 == Errorno.PLAYER_RD_FAILED) {
							Intent selectFileIntent = new Intent();
							selectFileIntent.setClass(playermenu.this, FileList.class);
							//close sub;
							if(subTitleView!=null)
								subTitleView.closeSubtitle();	
							//stop play
							backToFileList = true;
							if(m_Amplayer != null) {
								try	{
									m_Amplayer.Close();
								} 
								catch(RemoteException e) {
									e.printStackTrace();
								}
							}
							startActivity(selectFileIntent);
							playermenu.this.finish();
						}
						break;
					case VideoInfo.PLAYER_INITOK:
						INITOK = true;
						NOT_FIRSTTIME = true;
						try {
							bMediaInfo = m_Amplayer.GetMediaInfo();
						} 
						catch(RemoteException e) {
							e.printStackTrace();
						}
						if (bMediaInfo.drm_check == 0) {
						    try {
							    m_Amplayer.Play();
                            } 
							catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
						sub_para.totalnum =subMange.getExSubTotal()+InternalSubtitleInfo.getInsubNum();
						sub_para.curid = subMange.getCurrentInSubtitleIndexByJni();
						if (sub_para.curid == 0xff)
						    sub_para.curid = sub_para.totalnum;
						if(sub_para.totalnum>0)
				    		sub_para.sub_id =subMange.getSubID(sub_para.curid);
						else
						    sub_para.sub_id = null;
						openFile(sub_para.sub_id);
						if (bMediaInfo.seekable == 0) {
							myProgressBar.setEnabled(false);
							fastforword.setEnabled(false);
							fastreverse.setEnabled(false);
							fastforword.setImageResource(R.drawable.ff_disable);
							fastreverse.setImageResource(R.drawable.rewind_disable);
						}
						else {
							myProgressBar.setEnabled(true);
							fastforword.setEnabled(true);
							fastreverse.setEnabled(true);
							fastforword.setImageResource(R.drawable.ff);
							fastreverse.setImageResource(R.drawable.rewind);
						}
						if (setCodecMips() == 0)
				        	Log.d(TAG, "setCodecMips Failed");
						break;
					case VideoInfo.PLAYER_SEARCHOK:
						progressSliding = false;
						break;
					case VideoInfo.DIVX_AUTHOR_ERR:
					    Log.d(TAG, "Authorize Error");
						try {
						    DivxInfo divxInfo;
							divxInfo = m_Amplayer.GetDivxInfo();
							new AlertDialog.Builder(playermenu.this)
							.setTitle("Authorization Error")
							.setMessage("This player is not authorized to play this DivX protected video")
							.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int whichButton) {
								    Intent selectFileIntent = new Intent();
									selectFileIntent.setClass(playermenu.this, FileList.class);
									// close sub;
									if (subTitleView != null)
									    subTitleView.closeSubtitle();
									if (!fb32) {
									    // Hide the view with key color
										LinearLayout layout = (LinearLayout) findViewById(R.id.BaseLayout1);
										if (layout != null) {
                                            layout.setVisibility(View.INVISIBLE);
                                            layout.invalidate();
                                        }
									}
									// stop play
									backToFileList = true;
									if (m_Amplayer != null)
									    Amplayer_stop();
									startActivity(selectFileIntent);
									playermenu.this.finish();
								}
							}).show();
						} 
						catch (RemoteException e) {
						    e.printStackTrace();
                        }
                        break;
                    case VideoInfo.DIVX_EXPIRED:
                        Log.d(TAG, "Authorize Expired");
                        try {
                            DivxInfo divxInfo;
                            divxInfo = m_Amplayer.GetDivxInfo();
                            String s = "This rental has "
                                + msg.arg2
                                + " views left\nDo you want to use one of your "
                                + msg.arg2 + " views now";
                            new AlertDialog.Builder(playermenu.this)
							.setTitle("View DivX(R) VOD Rental")
							.setMessage(s)
							.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Intent selectFileIntent = new Intent();
                                    selectFileIntent.setClass(playermenu.this, FileList.class);
                                    // close sub;
                                    if (subTitleView != null)
                                        subTitleView.closeSubtitle();
                                    if (!fb32) {
                                        // Hide the view with key color
                                        LinearLayout layout = (LinearLayout) findViewById(R.id.BaseLayout1);
                                        if (layout != null) {
                                            layout.setVisibility(View.INVISIBLE);
                                            layout.invalidate();
                                        }
                                    }
                                    // stop play
                                    backToFileList = true;
                                    if (m_Amplayer != null)
                                        Amplayer_stop();
                                    startActivity(selectFileIntent);
                                    playermenu.this.finish();
                                }
                            }).show();
                        } 
						catch (RemoteException e) {
                            e.printStackTrace();
                        }
						break;
                    case VideoInfo.DIVX_RENTAL:
                        Log.d(TAG, "Authorize rental");
                        try {
                            DivxInfo divxInfo;
                            divxInfo = m_Amplayer.GetDivxInfo();
                            String s = "This rental has "
                                + msg.arg2
                                + " views left\nDo you want to use one of your "
                                + msg.arg2 + " views now?";
                            new AlertDialog.Builder(playermenu.this)
							.setTitle("View DivX(R) VOD Rental")
							.setMessage(s)
							.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // finish();
                                    try {
                                        m_Amplayer.Play();
                                    } 
									catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
							.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int whichButton) {
                                    Intent selectFileIntent = new Intent();
                                    selectFileIntent.setClass(playermenu.this, FileList.class);
                                    // close sub;
                                    if (subTitleView != null)
                                        subTitleView.closeSubtitle();
                                    if (!fb32) {
                                        // Hide the view with key color
                                        LinearLayout layout = (LinearLayout) findViewById(R.id.BaseLayout1);
										if (layout != null) {
                                            layout.setVisibility(View.INVISIBLE);
                                            layout.invalidate();
                                        }
                                    }
                                    // stop play
                                    backToFileList = true;
                                    if (m_Amplayer != null)
                                        Amplayer_stop();
                                    startActivity(selectFileIntent);
                                    playermenu.this.finish();
                                }
                            }).show();
                        } 
						catch (RemoteException e) {
                            e.printStackTrace();
                        }
						break;
					default:
						break;
    				}
    				break;
    			case VideoInfo.AUDIO_CHANGED_INFO_MSG:
    				total_audio_num = msg.arg1;
    				cur_audio_stream = msg.arg2;
    				break;
    			case VideoInfo.HAS_ERROR_MSG:
					String errStr = null;
					errStr = Errorno.getErrorInfo(msg.arg2);
					Toast.makeText(playermenu.this, errStr, Toast.LENGTH_LONG)
						.show();
    				break;
    			default:
    				super.handleMessage(msg);
    				break;
    		}
    	}
    });   
	
    public Player m_Amplayer = null;
    private void Amplayer_play() {
        // stop music player
        Intent intent = new Intent();
        intent.setAction("com.android.music.musicservicecommand.pause");
        intent.putExtra("command", "stop");
        this.sendBroadcast(intent);

    	try {
    		if(morbar!=null) {	
    			if((otherbar!=null) && (otherbar.getVisibility() == View.VISIBLE))
    				otherbar.setVisibility(View.GONE);
    			if((infodialog!=null) && (infodialog.getVisibility() == View.VISIBLE))
    				infodialog.setVisibility(View.GONE);
    			if((subbar!=null) && (subbar.getVisibility() == View.VISIBLE))
    				subbar.setVisibility(View.GONE);
    			
	            morbar.setVisibility(View.VISIBLE);
    		}
    	
			m_Amplayer.Open(PlayList.getinstance().getcur(), playPosition);
			//reset sub;
			subTitleView.setText("");
			subinit();
			subTitleView.setTextColor(sub_para.color);
	    	subTitleView.setTextSize(sub_para.font);
            // openFile(sub_para.sub_id);
		}
		catch(RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private void Amplayer_stop() {
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
		AudioTrackOperation.AudioStreamFormat.clear();
		AudioTrackOperation.AudioStreamInfo.clear();
		INITOK = false;
    }
    
    ServiceConnection m_PlayerConn = new ServiceConnection() {
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
				Log.e(TAG, "set client fail!");
			}
			
			//auto play
			Log.d(TAG,"to play files!");
			try {
				final short color = ((0x8 >> 3) << 11) 
									| ((0x30 >> 2) << 5) 
									| ((0x8 >> 3) << 0);
				m_Amplayer.SetColorKey(color);
				Log.d(TAG, "set colorkey() color=" + color);
			}
			catch(RemoteException e) {
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

    public void StartPlayerService() {
    	Intent intent = new Intent();
    	ComponentName hcomponet = new ComponentName("com.farcore.videoplayer","com.farcore.playerservice.AmPlayer");
    	intent.setComponent(hcomponet);
    	this.startService(intent);
    	this.bindService(intent, m_PlayerConn, BIND_AUTO_CREATE);
    }
    
    public void StopPlayerService() {
    	this.unbindService(m_PlayerConn);
    	Intent intent = new Intent();
    	ComponentName hcomponet = new ComponentName("com.farcore.videoplayer","com.farcore.playerservice.AmPlayer");
    	intent.setComponent(hcomponet);
    	this.stopService(intent);
    	m_Amplayer = null;
    }

    private String setSublanguage() {
    	String type=null;
    	String able=getResources().getConfiguration().locale.getCountry();
	
    	if(able.equals("TW"))  
    		 type ="BIG5";
    	else if(able.equals("JP"))
    		  type ="cp932";
    	else if(able.equals("KR"))
    		  type ="cp949";
    	else if(able.equals("IT")||able.equals("FR")||able.equals("DE"))
    		  type ="iso88591";
    	else
    		  type ="GBK";
    	
    	return type;
    }
    
	private void openFile(SubID filepath) {
		setSublanguage();	
		
		if(filepath==null)
			return;
		
		try {
			if(subTitleView.setFile(filepath,setSublanguage())==Subtitle.SUBTYPE.SUB_INVALID)
				return;
		} catch (Exception e) {
			Log.d(TAG, "open:errrrrrrrrrrrrrrr");
			e.printStackTrace();
		}
	
	}
	
	private int resumePlay() {
		final int pos = ResumePlay.check(PlayList.getinstance().getcur());
		Log.d(TAG, "resumePlay() pos is :"+pos);
		if (pos > 0) {
			confirm_dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.setting_resume)  
				.setMessage(R.string.str_resume_play) 
				.setPositiveButton(R.string.str_ok,  
					new DialogInterface.OnClickListener() {  
			            public void onClick(DialogInterface dialog, int whichButton) {  
			                playPosition = pos;
			            }  
			        })  
			    .setNegativeButton(playermenu.this.getResources().getString(R.string.str_cancel) + " ( "+resumeSecond+" )",  
				    new DialogInterface.OnClickListener() {  
				        public void onClick(DialogInterface dialog, int whichButton) {  
				        	playPosition = 0;
				        }  
				    })  
			    .show(); 
			confirm_dialog.setOnDismissListener(new myAlertDialogDismiss());
			ResumeCountdown();
			return pos;
		}
		if (!NOT_FIRSTTIME)
			StartPlayerService();
		return pos;
	}
	
	private class myAlertDialogDismiss implements DialogInterface.OnDismissListener {
		public void onDismiss(DialogInterface arg0) {
			// TODO Auto-generated method stub
			if (!NOT_FIRSTTIME)
    			StartPlayerService();
        	else
        		Amplayer_play();
        	resumeSecond = 8;
		}
		
	}

	 private final StorageEventListener mListener = new StorageEventListener() {
	        public void onUsbMassStorageConnectionChanged(boolean connected) {
	        	//this is the action when connect to pc
	        	return ;
	        }
	        public void onStorageStateChanged(String path, String oldState, String newState) {
	        	if (newState == null || path == null) 
	        		return;
	        	
	        	if(newState.compareTo("unmounted") == 0||newState.compareTo("removed") == 0) {
	        		if(PlayList.getinstance().rootPath!=null) {
		        		if(PlayList.getinstance().rootPath.startsWith(path)) {

		        			Intent selectFileIntent = new Intent();
		    				selectFileIntent.setClass(playermenu.this, FileList.class);
		    				//close sub;
		    				if(subTitleView!=null)
		    					subTitleView.closeSubtitle();		
		    				//stop play
		    				backToFileList = true;
		    				if(m_Amplayer != null)
		    					Amplayer_stop();
		    				PlayList.getinstance().rootPath=null;
		    				startActivity(selectFileIntent);
		    				playermenu.this.finish();
		        		}
	        		}
	        	}
	        }
	        
	    };
	    
	    @Override
	    public void onResume() {
	        super.onResume();
            closeScreenOffTimeout();

	        int getRotation = mWindowManager.getDefaultDisplay().getRotation();
	        Log.d("sensor", "rotate angle: "+Integer.toString(getRotation));
	        if((getRotation >= 0) && (getRotation <= 3))
	        	SettingsVP.setVideoRotateAngle(angle_table[getRotation]);
	        StorageManager m_storagemgr = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			m_storagemgr.registerListener(mListener);
	    }
	    
	    @Override
	    public void onConfigurationChanged(Configuration config) {
	    	try {
	    		super.onConfigurationChanged(config);
	    		
	            int getRotation = mWindowManager.getDefaultDisplay().getRotation();
	            Log.d("sensor", "rotate angle: "+Integer.toString(getRotation));
	            if((getRotation < 0) || (getRotation > 3))
	            	return;
	            SettingsVP.setVideoRotateAngle(angle_table[getRotation]);
	            
	    		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    		} 
	    		else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	    			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    		}
	    	} 
	    	catch (Exception ex) {
	    	
	    	}
	    }
}

class subview_set{
	public int totalnum; 
	public int curid;
	public int color;
	public int font; 
	public SubID sub_id;
}
