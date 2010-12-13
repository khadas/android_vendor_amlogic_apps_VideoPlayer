package amlogic.playerservice;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SettingsVP {

	public static final String SETTING_INFOS = "SETTING_Infos";
	private static SharedPreferences setting = null;
	
	
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
		Log.d("SettingsVP", "getParaStr() name:"+name+" content:"+para);
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
}
