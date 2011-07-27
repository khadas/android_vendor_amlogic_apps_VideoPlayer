package com.farcore.playerservice;

import com.farcore.playerservice.MediaInfo;
import com.farcore.playerservice.DivxInfo;

interface Player
{
	int Init();

	int Open(String filepath, int position);
	int Play();
	int Pause();
	int Resume();
	int Stop();
	int Close();
	MediaInfo GetMediaInfo();
	DivxInfo GetDivxInfo();
	int SwitchAID(int id);
	int SwitchAudioChannel(int id);

	int  SetColorKey(int color);
	void DisableColorKey();
	int GetOsdBpp();

	int Seek(int time);
	int Set3Dmode(int mode);
	int FastForward(int speed);
	int BackForward(int speed);
	
	int	RegisterClientMessager(IBinder hbinder);
}