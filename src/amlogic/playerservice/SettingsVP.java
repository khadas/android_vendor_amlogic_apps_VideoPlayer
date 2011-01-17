package amlogic.playerservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SettingsVP {

	public static final String SETTING_INFOS = "SETTING_Infos";
	private static SharedPreferences setting = null;
	private static String displaymode_path = "/sys/class/display/mode";
	private static String displayaxis_path = "/sys/class/display/axis";
	private static String video_axis_path = "/sys/class/video/axis";
	private static String TAG = "SettingVideoPlayer";
	public static String display_mode = null; 
	
	//==========preferences name==========
	public static final String[] pref_name = { 
											   "filename0",
											   "filename1",
											   "filename2",
											   "filename3",
											   "filename4",
											   "filename5",
											   "filename6",
											   "filename7",
											   "filename8",
											   "filename9",
											   "filetime0",
											   "filetime1",
											   "filetime2",
											   "filetime3",
											   "filetime4",
											   "filetime5",
											   "filetime6",
											   "filetime7",
											   "filetime8",
											   "filetime9",
											   "ResumeMode",
											   "PlayMode",
											   };
	
	
	public static void init(Context ct)
	{
		setting = ct.getSharedPreferences(SettingsVP.SETTING_INFOS, 
												Activity.MODE_PRIVATE);
	}
	
	public static Boolean getParaBoolean(String name)
	{
		Boolean para = setting.getBoolean(name, false);
		return para;
	}
	
	public static int getParaInt(String name)
	{
		int para = setting.getInt(name, 0);
		return para;
	}
	
	public static String getParaStr(String name)
	{
		String para = setting.getString(name, "");
		//Log.d("SettingsVP", "getParaStr() name:"+name+" content:"+para);
		return para;
	}
	
	public static boolean putParaStr(String name, String para)
	{
		return setting.edit()
		  		 .putString(name, para)
		  		 .commit();
	}
	
	public static boolean putParaInt(String name, int para)
	{
		return setting.edit()
		  		 .putInt(name, para)
		  		 .commit();
	}
	
	public static boolean putParaBoolean(String name, Boolean para)
	{
		return setting.edit()
		  		 .putBoolean(name, para)
		  		 .commit();
	}
	
	public static boolean setVideoLayoutMode()
	{
    	String buf = null;
    	String dispMode = null;
		File file = new File(displaymode_path);
		if (!file.exists()) {        	
        	return false;
        }
		file = new File(video_axis_path);
		if (!file.exists()) {        	
        	return false;
        }
		file = new File(displayaxis_path);
		if (!file.exists()) {        	
        	return false;
        }
		
		//read
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(displaymode_path), 32);
			BufferedReader in_axis = new BufferedReader(new FileReader(displayaxis_path), 32);
			try
			{
				dispMode = in.readLine();
				Log.d(TAG, "Current display mode: "+dispMode);
				if (dispMode.equals("panel"))
				{
					String dispaxis = in_axis.readLine();
					String[] axisstr = dispaxis.split(" ", 5);
					buf = "0,0,"+axisstr[2]+","+axisstr[3];
					Log.d(TAG, "Current display axis: "+buf);
				}
				else if (dispMode.equals("480p"))
				{
					buf = "0,0,720,480";
				}
				else if (dispMode.equals("720p"))
				{
					buf = "0,0,1280,720";
				}
				else if (dispMode.equals("1080p"))
				{
					buf = "0,0,1920,1080";
				}
				else
					buf = "0,0,1280,720";
				display_mode = dispMode;
			} finally {
    			in.close();
    			in_axis.close();
    		} 
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "IOException when read "+displaymode_path);
		} 
		
		//write
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(video_axis_path), 32);
    		try
    		{
    			out.write(buf);    
    			Log.d(TAG, "set video window as:"+buf);
    		} finally {
				out.close();
			}
			 return true;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "IOException when write "+video_axis_path);
			return false;
		}
	}
}
