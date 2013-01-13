package com.farcore.videoplayer;

import android.util.Log;

public class Config
{
        public static final boolean mDebug = false;
        
        public static void Logd(String msg1, String msg2)
        {
                if(mDebug)
                {
                        Log.d(msg1, msg2);
                }
        }
}
