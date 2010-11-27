package amlogic.playerservice;

interface Player
{
	int Init();

	int Open(String filepath);
	int Play();
	int Pause();
	int Resume();
	int Stop();
	int Close();
	int GetMediaInfo();
	int SwitchAID(int id);

	int  SetColorKey(int color);
	void DisableColorKey();

	int Seek(int time);
	int FastForward(int speed);
	int BackForward(int speed);
	
	int	RegisterClientMessager(IBinder hbinder);
}