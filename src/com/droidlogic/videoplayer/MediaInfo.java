package com.droidlogic.videoplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.File;

import com.droidlogic.app.MediaPlayerExt;

public class MediaInfo {
        private static final String TAG = "MediaInfo";
        private static final boolean DEBUG = false;
        private static Context mContext = null;
        private MediaPlayerExt mp = null;
        private MediaPlayerExt.MediaInfo mInfo = null;

        /*class VideoInfo {
                public int index;
                public int id;
                public String vformat;
                public int width;
                public int height;
        }

        class AudioInfo {
                public int index;
                public int id; //id is useless for application
                public int aformat;
                public int channel;
                public int sample_rate;
        }

        class SubtitleInfo {
                public int index;
                public int id;
                public int sub_type;
                public String sub_language;
        }

        class mMediaInfo {
                public String filename;
                public int duration;
                public String file_size;
                public int bitrate;
                public int type;
                public int cur_video_index;
                public int cur_audio_index;
                public int cur_sub_index;

                public int total_video_num;
                public VideoInfo[] videoInfo;

                public int total_audio_num;
                public AudioInfo[] audioInfo;

                public int total_sub_num;
                public SubtitleInfo[] subtitleInfo;
        }
        private mMediaInfo mInfo = null;*/

        public MediaInfo (MediaPlayerExt mediaPlayer, Context context) {
            mp = mediaPlayer;
            mContext = context;
        }

        public void initMediaInfo() {
            if (mp != null) {
                mInfo = mp.getMediaInfo();
            }
            if (DEBUG) { printMediaInfo(); }
        }

        //@@--------this part for video info-------------------------------------------------------
        public String getFileName (String path) {
            String filename = null;
            if (path != null && path.startsWith ("content")) {
                filename = "null";
            }
            else {
                File f = new File (path);
                filename = f.getName();
                filename = filename.substring (0, filename.lastIndexOf ("."));
            }
            return filename;
        }

        public String getFileType (String path) {
            String ext = "unknown";
            String filename = null;
            //check file start with "content"
            if (path != null && path.startsWith ("content")) {
                ext = "unknown";
            }
            else {
                int idx = path.lastIndexOf (".");
                if (idx < 0) {
                    ext = "unknown";
                }
                else {
                    ext = path.substring (path.lastIndexOf (".") + 1, path.length()).toLowerCase();
                }
            }
            return ext;
        }

        public String getFileType() {
            String str_type = "UNKNOWN";
            if (mInfo != null) {
                switch (mInfo.type) {
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
            }
            return str_type;
        }

        /*public String getFileSize() {
            long fs = mInfo.file_size;
            String str_size = "0";
            if (fs <= 1024)
                str_size = "1KB";
            else if (fs <= 1024 * 1024) {
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
        }*/

        public String getFileSize() {
            return mInfo.file_size;
        }

        public String getResolution() {
            String str = null;
            if (mInfo != null && getVideoTotalNum() > 0) {
                str = mInfo.videoInfo[0].width + "*" + mInfo.videoInfo[0].height;
            }
            return str;
        }

        public int getVideoTotalNum() {
            int num = 0;
            if (mInfo != null) {
                num = mInfo.total_video_num;
            }
            return num;
        }

        public int getVideoWidth() {
            int width = -1;
            if (mInfo != null && getVideoTotalNum() > 0) {
                width = mInfo.videoInfo[0].width;
            }
            return width;
        }

        public int getVideoHeight() {
            int height = -1;
            if (mInfo != null && getVideoTotalNum() > 0) {
                height = mInfo.videoInfo[0].height;
            }
            return height;
        }

        public int getDuration() {
            int ret = -1;
            if (mInfo != null) {
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
        public static final int AFORMAT_AC3   = 3;
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
        public static final int AFORMAT_DRA = 23;
        public static final int AFORMAT_SIPR = 24;
        public static final int AFORMAT_TRUEHD = 25;
        public static final int AFORMAT_MPEG1 = 26;
        public static final int AFORMAT_MPEG2 = 27;
        public static final int AFORMAT_UNSUPPORT = 28;
        public static final int AFORMAT_MAX = 29;

        public int getCurAudioIdx() {
            int ret = -1;
            int aIdx = -1;
            if (mInfo != null && getAudioTotalNum() > 0) {
                aIdx = mInfo.cur_audio_index;// current audio track index, should tranfer to list index
                for (int i = 0; i < mInfo.total_audio_num; i++) {
                    if (mInfo.cur_audio_index == mInfo.audioInfo[i].index) {
                        ret = i; // current list index
                    }
                }
            }
            return ret;
        }

        public int getAudioTotalNum() {
            int ret = -1;
            if (mInfo != null) {
                ret = mInfo.total_audio_num;
            }
            return ret;
        }

        public int getAudioIdx (int listIdx) {
            int ret = -1;
            if (mInfo != null && getAudioTotalNum() > 0) {
                ret = mInfo.audioInfo[listIdx].index;
            }
            return ret;
        }

        public int getAudioFormat (int i) {
            int ret = -1;
            if (mInfo != null && getAudioTotalNum() > 0) {
                ret = mInfo.audioInfo[i].aformat;
            }
            return ret;
        }

        public String getAudioFormatStr (int aFormat) {
            String type = null;
            switch (aFormat) {
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
                case AFORMAT_DRA:
                    type = "DRA";
                    break;
                case AFORMAT_SIPR:
                    type = "SIPR";
                    break;
                case AFORMAT_TRUEHD:
                    type = "TRUEHD";
                    break;
                case AFORMAT_MPEG1:
                    type = "MP1";
                    break;
                case AFORMAT_MPEG2:
                    type = "MP2";
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

        //@@--------this part for DTS Asset check -------------------------------------------------------------
        public boolean checkAudioisDTS (int aFormat) {
            boolean ret = false;
            if (aFormat == AFORMAT_DTS) {
                ret = true;
            }
            return ret;
        }

        //@@--------this part for certification check-------------------------------------------------------------
        public static final int CERTIFI_Dolby  = 1;
        public static final int CERTIFI_Dolby_Plus  = 2;
        public static final int CERTIFI_DTS  = 3;
        public int checkAudioCertification (int aFormat) {
            int ret = -1;
            if (aFormat == AFORMAT_AC3) {
                ret = CERTIFI_Dolby;
            }
            else if (aFormat == AFORMAT_EAC3) {
                ret = CERTIFI_Dolby_Plus;
            }
            else if (aFormat == AFORMAT_DTS) {
                ret = CERTIFI_DTS;
            }
            // add more ...
            return ret;
        }

        //@@--------this part for media info show on OSD--------------------------------------------------------
        //media info no, must sync with media_info_type in MediaPlayer.h
        public static final int MEDIA_INFO_UNKNOWN = 1;
        public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
        public static final int MEDIA_INFO_RENDERING_START = 3;
        public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
        public static final int MEDIA_INFO_BUFFERING_START = 701;
        public static final int MEDIA_INFO_BUFFERING_END = 702;
        public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
        public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
        public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
        public static final int MEDIA_INFO_METADATA_UPDATE = 802;
        public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
        public static final int MEDIA_INFO_AMLOGIC_BASE = 8000;
        public static final int MEDIA_INFO_AMLOGIC_VIDEO_NOT_SUPPORT = MEDIA_INFO_AMLOGIC_BASE + 1;
        public static final int MEDIA_INFO_AMLOGIC_AUDIO_NOT_SUPPORT = MEDIA_INFO_AMLOGIC_BASE + 2;
        public static final int MEDIA_INFO_AMLOGIC_NO_VIDEO = MEDIA_INFO_AMLOGIC_BASE + 3;
        public static final int MEDIA_INFO_AMLOGIC_NO_AUDIO = MEDIA_INFO_AMLOGIC_BASE + 4;
        public static final int MEDIA_INFO_AMLOGIC_SHOW_DTS_ASSET = MEDIA_INFO_AMLOGIC_BASE + 5;
        public static final int MEDIA_INFO_AMLOGIC_SHOW_DTS_EXPRESS = MEDIA_INFO_AMLOGIC_BASE + 6;
        public static final int MEDIA_INFO_AMLOGIC_SHOW_DTS_HD_MASTER_AUDIO = MEDIA_INFO_AMLOGIC_BASE + 7;

        public static boolean needShowOnUI (int info) {
            boolean ret = false;
            if ( (info == MEDIA_INFO_AMLOGIC_VIDEO_NOT_SUPPORT)
                    || (info == MEDIA_INFO_AMLOGIC_AUDIO_NOT_SUPPORT)
                    || (info == MEDIA_INFO_AMLOGIC_NO_VIDEO)
                    || (info == MEDIA_INFO_AMLOGIC_NO_AUDIO)) {
                ret = true;
            }
            return ret;
        }

        public static String getInfo (int info, Context context) {
            String infoStr = null;
            switch (info) {
                case MEDIA_INFO_AMLOGIC_VIDEO_NOT_SUPPORT:
                    infoStr = context.getResources().getString (R.string.unsupport_video_format); //"Unsupport Video format";
                    break;
                case MEDIA_INFO_AMLOGIC_AUDIO_NOT_SUPPORT:
                    infoStr = context.getResources().getString (R.string.unsupport_audio_format); //"Unsupport Audio format";
                    break;
                case MEDIA_INFO_AMLOGIC_NO_VIDEO:
                    infoStr = context.getResources().getString (R.string.file_have_no_video); //"file have no video";
                    break;
                case MEDIA_INFO_AMLOGIC_NO_AUDIO:
                    infoStr = context.getResources().getString (R.string.file_have_no_audio); //"file have no audio";
                    break;
                default:
                    break;
            }
            return infoStr;
        }

        private void printMediaInfo() {
            Log.i (TAG, "[printMediaInfo]mInfo:" + mInfo);
            if (mInfo != null) {
                Log.i (TAG, "[printMediaInfo]filename:" + mInfo.filename + ",duration:" + mInfo.duration + ",file_size:" + mInfo.file_size + ",bitrate:" + mInfo.bitrate + ",type:" + mInfo.type);
                Log.i (TAG, "[printMediaInfo]cur_video_index:" + mInfo.cur_video_index + ",cur_audio_index:" + mInfo.cur_audio_index + ",cur_sub_index:" + mInfo.cur_sub_index);
                //----video info----
                Log.i (TAG, "[printMediaInfo]total_video_num:" + mInfo.total_video_num);
                for (int i = 0; i < mInfo.total_video_num; i++) {
                    Log.i (TAG, "[printMediaInfo]videoInfo i:" + i + ",index:" + mInfo.videoInfo[i].index + ",id:" + mInfo.videoInfo[i].id);
                    Log.i (TAG, "[printMediaInfo]videoInfo i:" + i + ",vformat:" + mInfo.videoInfo[i].vformat);
                    Log.i (TAG, "[printMediaInfo]videoInfo i:" + i + ",width:" + mInfo.videoInfo[i].width + ",height:" + mInfo.videoInfo[i].height);
                }
                //----audio info----
                Log.i (TAG, "[printMediaInfo]total_audio_num:" + mInfo.total_audio_num);
                for (int j = 0; j < mInfo.total_audio_num; j++) {
                    Log.i (TAG, "[printMediaInfo]audioInfo j:" + j + ",index:" + mInfo.audioInfo[j].index + ",id:" + mInfo.audioInfo[j].id); //mInfo.audioInfo[j].id is useless for application
                    Log.i (TAG, "[printMediaInfo]audioInfo j:" + j + ",aformat:" + mInfo.audioInfo[j].aformat);
                    Log.i (TAG, "[printMediaInfo]audioInfo j:" + j + ",channel:" + mInfo.audioInfo[j].channel + ",sample_rate:" + mInfo.audioInfo[j].sample_rate);
                }
                //----subtitle info got from google standard flow----
                Log.i (TAG, "[printMediaInfo]total_sub_num:" + mInfo.total_sub_num);
                for (int k = 0; k < mInfo.total_sub_num; k++) {
                    Log.i (TAG, "[printMediaInfo]subtitleInfo k:" + k + ",index:" + mInfo.subtitleInfo[k].index + ",id:" + mInfo.subtitleInfo[k].id + ",sub_type:" + mInfo.subtitleInfo[k].sub_type);
                    Log.i (TAG, "[printMediaInfo]subtitleInfo k:" + k + ",sub_language:" + mInfo.subtitleInfo[k].sub_language);
                }
            }
        }

}

