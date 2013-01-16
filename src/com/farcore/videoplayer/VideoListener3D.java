package com.farcore.videoplayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import android.os.SystemProperties;
import android.view.WindowManagerPolicy;
import android.widget.Toast;
import android.app.SystemWriteManager; 

public class VideoListener3D
{
    public VideoListener3D(Context context)
    {
        mContext = context;
    }
    
    private Context mContext = null;
    
    private boolean mDebug = false;
    private void Logd(String msg)
    {
        if(mDebug)
        {
            Log.d(mClassName, msg);
        }
    }
    
    private final String mClassName = getClass().getName();
    
    private final String mVideoFormatFile = "/sys/module/amvideo/parameters/video_3d_format";
    private final String mHDMIConfigFile = "/sys/class/amhdmitx/amhdmitx0/config";
    private final String mHDMIPlugFile = "/sys/class/amhdmitx/amhdmitx0/hpd_state";
    private final String mHDMIDisp3DFile = "/sys/class/amhdmitx/amhdmitx0/disp_cap_3d";
    private final String mRequest2XScaleFile = "/sys/class/graphics/fb0/request2XScale";     
    
    private int mLastUIOption = 0;
    private int mLastVideoFormat = 0;  
    private boolean mLastPlugState = false;
    private boolean mLast3DSupport = false;

	private static SystemWriteManager sw; 
	public static void setSystemWrite(SystemWriteManager sysWrite)
	{
		sw = sysWrite;
	}
    
    public boolean is3DSupport()
    {
        return isDisp3DSupportable();
    }
    
    private int getUIOption()
    {
        return playermenu.getMBX3DStatus();
    }
    
    private int getVideoFormat()
    {
        String value = null;
        int format = 0;
        value = readSysFile(mVideoFormatFile);
        
        try
        {
            format = Integer.parseInt(value);
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
        }
        
        return format;
    }
    
    private boolean getPlugState()
    {
        String value = null;
        int state = 0;
        boolean result = false;
        
        value = readSysFile(mHDMIPlugFile);
        
        try
        {
            state = Integer.parseInt(value);
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
        }
        
        result = state == 0 ? false : true;
        
        return result;
    }
    
    public static final String mKeyVideoMode3D = "mbx.video.mode.3d"; 
    
    private void doChange(int uiOption, int videoFormat, boolean plugState)
    {
        Logd("uiOption is: " + uiOption);
        Logd("videoFormat is: " + videoFormat);
        Logd("plugState is: " + plugState);
        
        Logd("mLastUIOption is: " + mLastUIOption);
        Logd("mLastVideoFormat is: " + mLastVideoFormat);
        Logd("mLastPlugState is: " + mLastPlugState);
        
        if(uiOption != mLastUIOption)
        {
            switch(uiOption)
            {
            case 0:
            {
                changeTo2DMode();
            }
            break;
            
            case 1:
            {
                switch(mLastVideoFormat)
                {
                case TVIN_TFMT_2D:
                {
                    changeTo2DMode();
                }
                break;
                
                case TVIN_TFMT_3D_LRH_OLOR:
                case TVIN_TFMT_3D_LRH_OLER:
                case TVIN_TFMT_3D_LRH_ELOR:
                case TVIN_TFMT_3D_LRH_ELER:
                {
                    changeTo3DLRMode();
                }
                break;
                    
                case TVIN_TFMT_3D_TB:
                {
                    changeTo3DTBMode();
                }
                break;
                }
            }
            break;
                
            case 2:
            {
                changeTo3DLRMode();
            }
            break;
                
            case 3:
            {
                changeTo3DTBMode();
            }
            break;
            }
            
            mLastUIOption = uiOption;
        }
        
        if(videoFormat != mLastVideoFormat)
        {
            switch(videoFormat)
            {
            case TVIN_TFMT_2D:
            {
                switch(mLastUIOption)
                {
                case 0:
                case 1:
                {
                    changeTo2DMode();
                }
                break;
                }
            }
            break;
            
            case TVIN_TFMT_3D_LRH_OLOR:
            case TVIN_TFMT_3D_LRH_OLER:
            case TVIN_TFMT_3D_LRH_ELOR:
            case TVIN_TFMT_3D_LRH_ELER:
            {
                switch(mLastUIOption)
                {
                case 0:
                {
                    changeTo2DMode();
                }
                break;
                
                case 1:
                {
                    changeTo3DLRMode();
                }
                break;
                
                case 2:
                {
                    changeTo3DLRMode();
                }
                break;
                
                case 3:
                {
                    changeTo3DTBMode();
                }
                break;
                }
            }
            break;
                
            case TVIN_TFMT_3D_TB:
            {
                switch(mLastUIOption)
                {
                case 0:
                {
                    changeTo2DMode();
                }
                break;
                
                case 1:
                {
                    changeTo3DTBMode();
                }
                break;
                
                case 2:
                {
                    changeTo3DLRMode();
                }
                break;
                
                case 3:
                {
                    changeTo3DTBMode();
                }
                break;
                }
            }
            break;
            }
            
            mLastVideoFormat = videoFormat;
        }
        
        if(plugState != mLastPlugState)
        {
            if(plugState)
            {
                mLastPlugState = plugState;
                
                mLast3DSupport = isDisp3DSupportable();
                
                if(mLast3DSupport)
                {
                    int option = mLastUIOption;
                    int format = mLastVideoFormat;
                    mLastUIOption = -1;
                    mLastVideoFormat = -1;
                    
                    doChange(option, format, mLastPlugState);
                }
                else
                {
                    changeTo2DMode();
                }
            }
            else
            {
                mLastPlugState = plugState;
                changeTo2DMode();
            }
        }
    }
    
    private BroadcastReceiver mHDMIPlugReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (WindowManagerPolicy.ACTION_HDMI_HW_PLUGGED.equals(intent.getAction()))
            {
                boolean plugState = intent.getBooleanExtra(WindowManagerPolicy.EXTRA_HDMI_PLUGGED_STATE, 
                        false);
                if(plugState)
                {
                    onPlugHDMI();
                }
                else
                {
                    onUnplugHDMI();
                }
            }
        }
    };
    
    private boolean isDisp3DSupportable()
    {
        boolean result = false;
    	String str = readSysFile(mHDMIDisp3DFile);
    	
    	if(str == null)
    	{
    	    result = false;
    	}
    	else if(str.contains("FramePacking") || str.contains("TopBottom") || str.contains("SidebySide"))
    	{
    		result = true;
    	}
    	
    	return result;
    }
        
    private final int TVIN_TFMT_2D = 0;
    private final int TVIN_TFMT_3D_LRH_OLOR = 1;
    private final int TVIN_TFMT_3D_LRH_OLER = 2;
    private final int TVIN_TFMT_3D_LRH_ELOR = 3;
    private final int TVIN_TFMT_3D_LRH_ELER = 4;
    private final int TVIN_TFMT_3D_TB = 5;
        
    private void changeTo2DMode()
    {
	  	sw.setProperty(mKeyVideoMode3D, "0");     
	  	
	  	writeSysFile(mHDMIConfigFile, "3doff");
	  	
        if(getCurrentOutputmode().contains("720p"))
        {
        	writeSysFile(mRequest2XScaleFile, "16 1280 720");
        }
        else if(getCurrentOutputmode().contains("1080p"))
        {
        	writeSysFile(mRequest2XScaleFile, "8");
        }        
    }
        
    private void changeTo3DLRMode()
    {
        if(mLast3DSupport)
        {
      		sw.setProperty(mKeyVideoMode3D, "1");
      		
      		writeSysFile(mHDMIConfigFile, "3dlr");
      		
            if(getCurrentOutputmode().contains("720p"))
            {
                    writeSysFile(mRequest2XScaleFile, "17");
            }
            else if(getCurrentOutputmode().contains("1080p"))
            {
                    writeSysFile(mRequest2XScaleFile, "8 1");
            }
        }
        else
        {
            Toast.makeText(mContext, R.string.not_support_3d, Toast.LENGTH_LONG).show();
        }
    }
        
    private void changeTo3DTBMode()
    {
        if(mLast3DSupport)
        {
    	  	sw.setProperty(mKeyVideoMode3D, "2");
    	  	
    	  	writeSysFile(mHDMIConfigFile, "3dtb");
    	  	
            if(getCurrentOutputmode().contains("720p"))
            {
                    writeSysFile(mRequest2XScaleFile, "18");
            }
            else if(getCurrentOutputmode().contains("1080p"))
            {
                    writeSysFile(mRequest2XScaleFile, "8 2");
            }
        }
        else
        {
            Toast.makeText(mContext, R.string.not_support_3d, Toast.LENGTH_LONG).show();
        }
    }
        
    private String getCurrentOutputmode()
    {
        String outputmode = null;
        
        outputmode = sw.getProperty("ubootenv.var.outputmode");
        
        return outputmode;
    }
    
    private final int START_CHECK_MSG = 100001;
    private final int STOP_CHECK_MSG = 100002;
    
    private Handler mCheckHandler = new Handler()
    {
        public void handleMessage (Message msg)
        {
            super.handleMessage(msg);
            
            switch(msg.what)
            {
            case START_CHECK_MSG:
            {
                Logd("checking... current time is: " + System.currentTimeMillis());
                
                onChangeVideoFormat();        
                
                Message start_msg = mCheckHandler.obtainMessage(START_CHECK_MSG);
                mCheckHandler.sendMessageDelayed(start_msg, 500);
            }
            break;
            
            case STOP_CHECK_MSG:
            {
                Logd("stop checking... current time is: " + System.currentTimeMillis());
                
                boolean moreMsg = hasMessages(START_CHECK_MSG);
                
                Logd("moreMsg is: " + moreMsg);
                
                mCheckHandler.removeMessages(START_CHECK_MSG);
                
                // changeTo2DMode();
                
                sw.setProperty(mKeyVideoMode3D, "0"); 
                
                Logd("checking exit... set to 2d mode");
            }
            break;
            }
        }
    };
    
    private IntentFilter mFilter = null;
    public void startCheck()
    {
        mLastUIOption = getUIOption();
        mLastVideoFormat = getVideoFormat();
        mLastPlugState = getPlugState();
        mLast3DSupport = isDisp3DSupportable();
        
        Logd("startCheck(), mLastUIOption is: " + mLastUIOption);
        Logd("startCheck(), mLastVideoFormat is: " + mLastVideoFormat);
        Logd("startCheck(), mLastPlugState is: " + mLastPlugState);
        Logd("startCheck(), mLast3DSupport is: " + mLast3DSupport);
        
        mFilter = new IntentFilter();
        mFilter.addAction(WindowManagerPolicy.ACTION_HDMI_HW_PLUGGED);
        mContext.registerReceiver(mHDMIPlugReceiver, mFilter);
        
        Logd("start check...");
        
        Message start_msg = mCheckHandler.obtainMessage(START_CHECK_MSG);
        mCheckHandler.sendMessageAtFrontOfQueue(start_msg);
    }
    
    public void stopCheck()
    {
        mContext.unregisterReceiver(mHDMIPlugReceiver);
        
        Message stop_msg = mCheckHandler.obtainMessage(STOP_CHECK_MSG);
        mCheckHandler.sendMessageAtFrontOfQueue(stop_msg);
    }
    
    public void changeUIOption()
    {
        onChangeUIOption();
    }
    
    private void onChangeUIOption()
    {
        int option = getUIOption();
        
        Logd("onChangeUIOption(), option is: " + option);

        doChange(option, mLastVideoFormat, mLastPlugState);
    }
    
    private void onChangeVideoFormat()
    {
        int format = getVideoFormat();
        
        Logd("onChangeVideoFormat(), format is: " + format);
        
        doChange(mLastUIOption, format, mLastPlugState);
    }
    
    private void onPlugHDMI()
    {
        Logd("onPlugHDMI(), plugState is: true");
        
        doChange(mLastUIOption, mLastVideoFormat, true);
    }
    
    private void onUnplugHDMI()
    {
        Logd("onUnplugHDMI(), plugState is: false");
        
        doChange(mLastUIOption, mLastVideoFormat, false);
    }
    
    private boolean writeSysFile(String pathname, String value)
    {
        boolean result = true;
        /*
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        
        try
        {
            fileWriter = new FileWriter(pathname);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
        bufferedWriter = new BufferedWriter(fileWriter);
        
        try
        {
            bufferedWriter.write(value);
            bufferedWriter.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
        try
        {
            bufferedWriter.close();
            fileWriter.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }*/

		sw.writeSysfs(pathname,value);
        
        return result;
    }
    
    private String readSysFile(String pathname)
    {
        String message = null;
        
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        
        try
        {
            fileReader = new FileReader(pathname);
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        
        bufferedReader = new BufferedReader(fileReader);
        
        try
        {
            message = bufferedReader.readLine();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
        try
        {
            bufferedReader.close();
            fileReader.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
        return message;
    }
}
