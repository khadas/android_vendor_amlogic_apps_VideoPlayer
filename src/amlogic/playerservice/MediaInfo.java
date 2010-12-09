package amlogic.playerservice;

import amlogic.playerservice.AudioMediaInfo;
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
	
}
