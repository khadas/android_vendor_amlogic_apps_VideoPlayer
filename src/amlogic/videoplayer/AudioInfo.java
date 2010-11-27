package amlogic.videoplayer;

import java.util.ArrayList;
import java.util.List;

public class AudioInfo {
	public static List<String> AudioStreamFormat = new ArrayList<String>();
	public static List<ASInfo> AudioStreamInfo = new ArrayList<ASInfo>();
	
	public static class ASInfo {
		public int audio_id;
		public String audio_format;
	}
    //audio format
    public static final int AFORMAT_UNKNOWN = -1;
    public static final int AFORMAT_MPEG   = 0;
    public static final int AFORMAT_PCM_S16LE = 1;
    public static final int AFORMAT_AAC   = 2;
    public static final int AFORMAT_AC3   =3;
    public static final int AFORMAT_ALAW = 4;
    public static final int AFORMAT_MULAW = 5;
    public static final int AFORMAT_DTS = 6;
    public static final int AFORMAT_PCM_S16BE = 7;
    public static final int AFORMAT_FLAC = 8;
    public static final int AFORMAT_COOK = 9;
    public static final int AFORMAT_PCM_U8 = 10;
    public static final int AFORMAT_ADPCM = 11;
    public static final int AFORMAT_AMR  = 12;
    public static final int AFORMAT_RAAC  = 13;
    public static final int AFORMAT_WMA  = 14;
    public static final int AFORMAT_WMAPRO   = 15;
    public static final int AFORMAT_MAX    = 16;
	
	public static String getAudioFormat(int aformat)
	{
		String type = null;
		switch(aformat)
		{
		case AudioInfo.AFORMAT_UNKNOWN:
			break;
		case AudioInfo.AFORMAT_MPEG:
			type = "MPEG";
			break;
		case AudioInfo.AFORMAT_PCM_S16LE:
			type = "PCM_S16LE";
			break;
		case AudioInfo.AFORMAT_AAC:
			type = "AAC";
			break;
		case AudioInfo.AFORMAT_AC3:
			type = "AC3";
			break;
		case AudioInfo.AFORMAT_ALAW:
			type = "ALAW";
			break;
		case AudioInfo.AFORMAT_MULAW:
			type = "MULAW";
			break;
		case AudioInfo.AFORMAT_DTS:
			type = "DTS";
			break;
		case AudioInfo.AFORMAT_PCM_S16BE:
			type = "PCM_S16BE";
			break;
		case AudioInfo.AFORMAT_FLAC:
			type = "FLAC";
			break;
		case AudioInfo.AFORMAT_COOK:
			type = "COOK";
			break;
		case AudioInfo.AFORMAT_PCM_U8:
			type = "PCM_U8";
			break;
		case AudioInfo.AFORMAT_ADPCM:
			type = "ADPCM";
			break;
		case AudioInfo.AFORMAT_AMR:
			type = "AMR";
			break;
		case AudioInfo.AFORMAT_RAAC:
			type = "RAAC";
			break;
		case AudioInfo.AFORMAT_WMA:
			type = "WMA";
			break;
		case AudioInfo.AFORMAT_WMAPRO:
			type = "WMAPRO";
			break;
		case AudioInfo.AFORMAT_MAX:
			break;
		default:
			break;
		}
		return type;
	}
}
