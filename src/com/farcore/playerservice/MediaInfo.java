package com.farcore.playerservice;

import java.io.File;

import com.farcore.playerservice.AudioMediaInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class MediaInfo {
	
    public static final Parcelable.Creator<MediaInfo> CREATOR = new
    Parcelable.Creator<MediaInfo>() {
    	public MediaInfo createFromParcel(Parcel in) {
    	    return new MediaInfo();
    	}

    	public MediaInfo[] newArray(int size) {
    	    return null;
    	}

    };

    public void writeToParcel(Parcel reply, int parcelableWriteReturnValue) {
		// TODO Auto-generated method stub
		
	}

	public boolean hasAudioTrack() {
    	return (ainfo != null && ainfo.length > 0);    	
	}

    public int getAudioTrackCount() {
    	if (ainfo == null)
    		return 0;
		return ainfo.length;
	}

    public AudioMediaInfo[] getAudioTracks() {
    	return ainfo;
    }
    
    public AudioMediaInfo getAudioTrack(int idx) {
    	if (ainfo == null)
    		return null;
    	
    	if (idx < 0 || idx >= ainfo.length)
    		return null;
    	
    	return ainfo[idx];
    }


	public AudioMediaInfo[] ainfo;
	public int seekable;
	private int filetype;
	private long filesize = 0;
	public int duration;
	private int width;
	private int height;
    public int drm_check;
	
	public String getFileName(String path)
	{
		File f = new File(path);
		String filename = f.getName();
		filename = filename.substring(0, filename.lastIndexOf("."));
		return filename;
	}
	
	public String getResolution()
	{
		return width + "*" + height;
	}
	
	public String getFileType()
	{
		String str_type = "UNKNOWN";
		switch (filetype)
		{
		case 0:
			break;
		case 1:
			str_type = "AVI";
			break;
		case 2:
			str_type = "MPEG";
			break;
		case 3:
			str_type = "WAV";
			break;
		case 4:
			str_type = "MP3";
			break;
		case 5:
			str_type = "AAC";
			break;
		case 6:
			str_type = "AC3";
			break;
		case 7:
			str_type = "RM";
			break;
		case 8:
			str_type = "DTS";
			break;
		case 9:
			str_type = "MKV";
			break;
		case 10:
			str_type = "MOV";
			break;
		case 11:
			str_type = "MP4";
			break;
		case 12:
			str_type = "FLAC";
			break;
		case 13:
			str_type = "H264";
			break;
		case 14:
			str_type = "M2V";
			break;
		case 15:
			str_type = "FLV";
			break;
		case 16:
			str_type = "P2P";
			break;
		case 17:
			str_type = "ASF";
			break;
		default:
			break;
		}
		return str_type;
	}
	
	public String getFileSize()
	{
		long fs = filesize;
		String str_size = "0";
		if (fs <= 1024)
			str_size = "1KB";
		else if (fs <= 1024 * 1024)
		{
			fs /= 1024;
			fs += 1;
			str_size = fs + "KB";
		}
		else if (fs > 1024 * 1024)
		{
			fs /= 1024*1024;
			fs += 1;
			str_size = fs + "MB";
		}
		return str_size;
	}
}
