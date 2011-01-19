package com.farcore.playerservice;

import android.util.Log;

public class InternalSubtitleInfo {
	private static String TAG = "playermenu";
/*	public static final int UID_SUBTITLE_OFF = 0xFFFF;

	public int getUid() {
		return sub_uid;
	}
	
	public int getLanguage() {
		return lang;
	}
	
	public String getDesc() {
		return desc;
	}


	int sub_uid;
	int lang;		//not support
	String desc; 	//not support
		*/
	public static int getInsubNum()
	{
		Log.d(TAG, "*******************return sub num**********************"+insub_num);
		return insub_num;
	}
	
	private static int insub_num;// insub total num;
		
}

