package com.meson.videoplayer;

import android.media.MediaPlayer;
import android.util.Log;
import java.io.File;


public class MediaInfo{
    private static final String TAG = "MediaInfo";
    private static final boolean DEBUG = false;
    private MediaPlayer mp = null;
    private MediaPlayer.MediaInfo mInfo = null;
    
    public MediaInfo(MediaPlayer mediaPlayer) {
        mp = mediaPlayer;
    }
    
    public void initMediaInfo() {
        if(mp != null) {
            mInfo = mp.getMediaInfo();
        }
        if(DEBUG) printMediaInfo();
    }

    //@@--------this part for video info-------------------------------------------------------
    public String getFileName(String path) {
        String filename=null;
        if(path!=null&&path.startsWith("content")) {
            filename="null";
        }
        else {
            File f = new File(path);
            filename = f.getName();
            filename = filename.substring(0, filename.lastIndexOf("."));
        }
        return filename;
    }	

    public String getFileType(String path) {
        String ext = "unknown";
        String filename=null;
        //check file start with "content"
        if(path!=null&&path.startsWith("content")) {
            ext = "unknown";
        }
        else {
            int idx = path.lastIndexOf(".");
            if(idx < 0){
                ext = "unknown";
            }
            else {
                ext = path.substring(path.lastIndexOf(".")+1,path.length()).toLowerCase();
            }
        }
        return ext;
    }

    public String getFileType() {
        String str_type = "UNKNOWN";
        switch(mInfo.type) {
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
	
    public String getFileSize() {
        long fs = mInfo.file_size;
        String str_size = "0";
        if(fs <= 1024)
            str_size = "1KB";
        else if(fs <= 1024 * 1024) {
            fs /= 1024;
            fs += 1;
            str_size = fs + "KB";
        }
        else if (fs > 1024 * 1024) {
            fs /= 1024*1024;
            fs += 1;
            str_size = fs + "MB";
        }
        return str_size;
    }

    public String getResolution() {
        String str = null;
        if(mInfo != null) {
            str = mInfo.videoInfo[0].width + "*" + mInfo.videoInfo[0].height;
        }
        return str;
    }

    public int getDuration() {
        int ret = -1;
        if(mInfo != null) {
            ret = mInfo.duration;
        }
        return ret;
    }

    //@@--------this part for audio info-------------------------------------------------------
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
    public static final int AFORMAT_PCM_BLURAY = 16;
    public static final int AFORMAT_ALAC = 17;
    public static final int AFORMAT_VORBIS = 18;
    public static final int AFORMAT_AAC_LATM = 19;
    public static final int AFORMAT_APE   = 20;
    public static final int AFORMAT_EAC3   = 21;   
    public static final int AFORMAT_PCM_WIFIDISPLAY = 22;
    public static final int AFORMAT_UNSUPPORT = 23;
    public static final int AFORMAT_MAX = 24; 

    public int getCurAudioIdx() {
        int ret = -1;
        int aIdx = -1;
        if(mInfo != null) {
            aIdx = mInfo.cur_audio_index;// current audio track index, should tranfer to list index
            for(int i=0;i<mInfo.total_audio_num;i++) {
                if(mInfo.cur_audio_index == mInfo.audioInfo[i].index) {
                    ret = i; // current list index
                }
            }
        }
        return ret;
    }

    public int getAudioTotalNum() {
        int ret = -1;
        if(mInfo != null) {
            ret = mInfo.total_audio_num;
        }
        return ret;
    }

    public int getAudioIdx(int listIdx) {
        int ret = -1;
        if(mInfo != null) {
            ret = mInfo.audioInfo[listIdx].index;
        }
        return ret;
    }

    public int getAudioFormat(int i) {
        int ret = -1;
        if(mInfo != null) {
            ret = mInfo.audioInfo[i].aformat;
        }
        return ret;
    }

    public String getAudioFormatStr(int aFormat) {
        String type = null;
        switch(aFormat) {
            case AFORMAT_UNKNOWN:
                type = "UNKNOWN";
                break;
            case AFORMAT_MPEG:
                type = "MP3";
                break;
            case AFORMAT_PCM_S16LE:
                type = "PCM";
                break;
            case AFORMAT_AAC:
                type = "AAC";
                break;
            case AFORMAT_AC3:
                type = "AC3";
                break;
            case AFORMAT_ALAW:
                type = "ALAW";
                break;
            case AFORMAT_MULAW:
                type = "MULAW";
                break;
            case AFORMAT_DTS:
                type = "DTS";
                break;
            case AFORMAT_PCM_S16BE:
                type = "PCM_S16BE";
                break;
            case AFORMAT_FLAC:
                type = "FLAC";
                break;
            case AFORMAT_COOK:
                type = "COOK";
                break;
            case AFORMAT_PCM_U8:
                type = "PCM_U8";
                break;
            case AFORMAT_ADPCM:
                type = "ADPCM";
                break;
            case AFORMAT_AMR:
                type = "AMR";
                break;
            case AFORMAT_RAAC:
                type = "RAAC";
                break;
            case AFORMAT_WMA:
                type = "WMA";
                break;
            case AFORMAT_WMAPRO:
                type = "WMAPRO";
                break;
            case AFORMAT_PCM_BLURAY:
                type = "PCM_BLURAY";
                break;
            case AFORMAT_ALAC:
                type = "ALAC";
                break;
            case AFORMAT_VORBIS:
                type = "VORBIS";
                break;
            case AFORMAT_AAC_LATM:
                type = "AAC_LATM";
                break;
            case AFORMAT_APE:
                type = "APE";
                break;
            case AFORMAT_EAC3:
                type = "EAC3";
                break;
            case AFORMAT_PCM_WIFIDISPLAY:
                type = "PCM_WIFIDISPLAY";
                break;
            case AFORMAT_UNSUPPORT:
                type = "UNSUPPORT";
                break;
            case AFORMAT_MAX:
                type = "MAX";
                break;
            default:
                type = "UNKNOWN";
                break;
        }
        return type;
    }
    
    private void printMediaInfo() {
        Log.i(TAG,"[printMediaInfo]mInfo:"+mInfo);
        if(mInfo != null) {
            Log.i(TAG,"[printMediaInfo]filename:"+mInfo.filename+",duration:"+mInfo.duration+",file_size:"+mInfo.file_size+",bitrate:"+mInfo.bitrate+",type:"+mInfo.type);
            Log.i(TAG,"[printMediaInfo]cur_video_index:"+mInfo.cur_video_index+",cur_audio_index:"+mInfo.cur_audio_index+",cur_sub_index:"+mInfo.cur_sub_index);

            //----video info----
            Log.i(TAG,"[printMediaInfo]total_video_num:"+mInfo.total_video_num);
            for (int i=0;i<mInfo.total_video_num;i++) {
                Log.i(TAG,"[printMediaInfo]videoInfo i:"+i+",index:"+mInfo.videoInfo[i].index+",id:"+mInfo.videoInfo[i].id);
                Log.i(TAG,"[printMediaInfo]videoInfo i:"+i+",vformat:"+mInfo.videoInfo[i].vformat);
                Log.i(TAG,"[printMediaInfo]videoInfo i:"+i+",width:"+mInfo.videoInfo[i].width+",height:"+mInfo.videoInfo[i].height);
            }

            //----audio info----
            Log.i(TAG,"[printMediaInfo]total_audio_num:"+mInfo.total_audio_num);
            for (int j=0;j<mInfo.total_audio_num;j++) {
                Log.i(TAG,"[printMediaInfo]audioInfo j:"+j+",index:"+mInfo.audioInfo[j].index+",id:"+mInfo.audioInfo[j].id);//mInfo.audioInfo[j].id is useless for application
                Log.i(TAG,"[printMediaInfo]audioInfo j:"+j+",aformat:"+mInfo.audioInfo[j].aformat);
                Log.i(TAG,"[printMediaInfo]audioInfo j:"+j+",channel:"+mInfo.audioInfo[j].channel+",sample_rate:"+mInfo.audioInfo[j].sample_rate);
            }

            //----subtitle info got from google standard flow----
            Log.i(TAG,"[printMediaInfo]total_sub_num:"+mInfo.total_sub_num);
            for (int k=0;k<mInfo.total_sub_num;k++) {
                Log.i(TAG,"[printMediaInfo]subtitleInfo k:"+k+",index:"+mInfo.subtitleInfo[k].index+",id:"+mInfo.subtitleInfo[k].id+",sub_type:"+mInfo.subtitleInfo[k].sub_type);
                Log.i(TAG,"[printMediaInfo]subtitleInfo k:"+k+",sub_language:"+mInfo.subtitleInfo[k].sub_language);
            }
        }
    }

}

