package com.farcore.videoplayer;

import android.os.storage.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.farcore.videoplayer.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

public class FileList extends ListActivity {
	private List<File> listFiles =null;
	private List<File> listVideos =null;
	private List<String> items=null;
	private List<String> paths=null;
	private List<String> currentlist=null;
	private String currenturl = null;
	private String root_path = "/mnt";
	private String extensions ;
	
	private TextView tileText;
	private File file;
	private static String TAG = "player_FileList";
	
	 private final StorageEventListener mListener = new StorageEventListener() {
	        public void onUsbMassStorageConnectionChanged(boolean connected)
	        {
	        	//this is the action when connect to pc
	        	return ;
	        }
	        public void onStorageStateChanged(String path, String oldState, String newState)
	        {
	        	if (newState == null || path == null) 
	        		return;
	        	
	        	if(newState.compareTo("mounted") == 0)
	        	{
	        		if(PlayList.getinstance().rootPath==null 
	        		|| PlayList.getinstance().rootPath.equals(root_path))
	        			BrowserFile(root_path); 
	        	}
	        	else if(newState.compareTo("unmounted") == 0
	        			|| newState.compareTo("removed") == 0)
	        	{
	        		if(PlayList.getinstance().rootPath.startsWith(path)
	        		|| PlayList.getinstance().rootPath.equals(root_path))
	        			BrowserFile(root_path);
	        	}
	        	
	        }
	        
	};
	    
    @Override
    public void onResume() {
        super.onResume();
        if( !(new File(PlayList.getinstance().rootPath).exists()))
		{
			PlayList.getinstance().rootPath =root_path;
			BrowserFile(PlayList.getinstance().rootPath);
		}
        
        StorageManager m_storagemgr = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		m_storagemgr.registerListener(mListener);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        StorageManager m_storagemgr = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        m_storagemgr.unregisterListener(mListener);
    }
    
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		extensions = getResources().getString(R.string.support_video_extensions);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.main);
	    
	    currentlist = new ArrayList<String>();
	    
		if(PlayList.getinstance().rootPath==null)
			PlayList.getinstance().rootPath =root_path;
	    	
	    BrowserFile(PlayList.getinstance().rootPath);
	    
	    Button home = (Button) findViewById(R.id.Button_home);
	    home.setOnClickListener(new View.OnClickListener() 
	    {

            public void onClick(View v) 
            {
            	FileList.this.finish();
            	PlayList.getinstance().rootPath =null;
            } 
       
	    });
	    Button exit = (Button) findViewById(R.id.Button_exit);
	    exit.setOnClickListener(new View.OnClickListener() 
	    {

            public void onClick(View v) 
            {
            	
                if(paths == null) 
                {
                	FileList.this.finish();
                	PlayList.getinstance().rootPath =null;
                }
                else
                {
                	if(paths.isEmpty())
                	{
                		FileList.this.finish();
                		PlayList.getinstance().rootPath =null;
                	}
                	file = new File(paths.get(0).toString());
                	currenturl =file.getParentFile().getParent();
                	if(file.getParent().compareToIgnoreCase(root_path)!=0)
                		BrowserFile(currenturl);
                	else
                	{
            			FileList.this.finish();
            			PlayList.getinstance().rootPath =null;
                	}
                }
                
            } 
       
	    });
        
	}
	
	private void BrowserFile(String filePath) {
		int i = 0;
		file = new File(filePath);
		listFiles = new ArrayList<File>();
	    items=new ArrayList<String>();
	    paths=new ArrayList<String>();
	    searchFile(file);
	    if(listFiles.isEmpty()) {
	    	Toast.makeText(FileList.this, R.string.str_no_file, Toast.LENGTH_SHORT).show();
	    	//paths =currentlist;
	    	paths.clear();
	    	paths.addAll(currentlist);
	    	return;
	    }
	    Log.d(TAG, "BrowserFile():"+filePath);
	    PlayList.getinstance().rootPath =filePath;
	    
	    File [] fs = new File[listFiles.size()];
	    for(i=0;i<listFiles.size();i++) {
	    	fs[i] = listFiles.get(i);
	    }
	    Arrays.sort(fs, new MyComparator(MyComparator.NAME_ASCEND));   
	    
	    for(i=0;i<fs.length;i++)
	    {
	    	File tempF = fs[i];
	    	String tmppath = tempF.getName();
	    	
		    //change device name;	
	    	if(filePath.equals("/mnt"))
	    	{
	    		String tpath = tempF.getAbsolutePath();  
	    	
	    		if (tpath.equals("/mnt/flash"))
	    			 tmppath = "nand";
	    		else if((!tpath.equals("/mnt/sdcard"))&&tpath.startsWith("/mnt/sd"))
	    			 tmppath = "usb"+" "+tpath.substring(5);//5 is the len of "/mnt/"
	    		//delete used folder
	    		if((!tpath.equals("/mnt/asec"))&&(!tpath.equals("/mnt/secure"))&&
	    			(!tpath.equals("/mnt/obb")))
	    		{
	    			items.add(tmppath);
	    	    	paths.add(tempF.getPath());
	    		}
	    	}
	    	else
	    	{
	    		items.add(tmppath);
	    		paths.add(tempF.getPath());
	    	}
		 }
		    
	    tileText =(TextView) findViewById(R.id.TextView_path);
	    tileText.setText(catShowFilePath(filePath));
	    setListAdapter(new MyAdapter(this,items,paths));
	}

    private String catShowFilePath(String path) {
    	String text = null;
    	
    	if(path.startsWith("/mnt/flash"))
    		text=path.replaceFirst("/mnt/flash","/mnt/nand");
    	else if(path.startsWith("/mnt/sda"))
    		text=path.replaceFirst("/mnt/sda","/mnt/usb sda");
    	else if(path.startsWith("/mnt/sdb"))
    		text=path.replaceFirst("/mnt/sdb","/mnt/usb sdb");
    	//else if(path.startsWith("/mnt/sdcard"))
    		//text=path.replaceFirst("/mnt/sdcard","sdcard");
    	return text;
    }
    
	public void searchFile(File file)
	{
	    File[] the_Files;
	    the_Files = file.listFiles(new MyFilter(extensions));
	
	    if(the_Files == null)
	    {
		  Toast.makeText(FileList.this, R.string.str_no_file, Toast.LENGTH_SHORT).show();
		  return;
		 }

	    for(int i=0;i<the_Files.length;i++) 
	    {
	    	File tempF = the_Files[i];
	    	
	    	if (tempF.isDirectory())
	    	{
	    		if(!tempF.isHidden())
	    		    listFiles.add(tempF);   	
	    	} 
	    	else
	    	{
	    		try {
	    			listFiles.add(tempF);
	    		} 
	    		catch (Exception e) {
	    			return;
	    		}
	    	}
	    }
	}

	@Override
	protected void onListItemClick(ListView l,View v,int position,long id) {
		File file = new File(paths.get(position));
		currentlist.clear();
		currentlist.addAll(paths);
	    //currentlist =paths;
	    if(file.isDirectory()) 
	    	BrowserFile(paths.get(position));
	    else 
	    {
		//stopMediaPlayer();
	    	//file = new File(file.getParent());
	    	int pos = filterDir(file);
	    	//Log.i(TAG, "play path:"+file.getPath()+", pos:"+Integer.toString(pos));
	    	if(pos < 0) 
	    		return;
	    	PlayList.getinstance().rootPath= file.getParent();
	    	//int dircount =listFiles.size()-listVideos.size();
	    	
	    	//if(dircount>=0&&(position-dircount)>=0)
			//PlayList.getinstance().setlist(paths, position-dircount);
			//else
			//PlayList.getinstance().setlist(paths, position);
	    	PlayList.getinstance().setlist(paths, pos);
	    	showvideobar();
	    }
	}
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if (keyCode == KeyEvent.KEYCODE_BACK) {        	
            if(paths == null) 
            {
            	FileList.this.finish();
            	PlayList.getinstance().rootPath =null;
            }
            else
            {
            	if(paths.isEmpty())
            	{
            		FileList.this.finish();
            		PlayList.getinstance().rootPath =null;
            	}
            	file = new File(paths.get(0).toString());
            	currenturl =file.getParentFile().getParent();
            	if(file.getParent().compareToIgnoreCase(root_path)!=0)
            		BrowserFile(currenturl);
            	else
            	{
        			FileList.this.finish();
        			PlayList.getinstance().rootPath =null;
            	}
            }  
            return true;                 
        }
        return super.onKeyDown(keyCode, event); 
    }
	private void showvideobar() {
		//* new an Intent object and ponit a class to start
		Intent intent = new Intent();
		intent.setClass(FileList.this, playermenu.class);

		startActivity(intent);
		FileList.this.finish();
	}
	
	public int filterDir(File file)
	{
		int pos = -1;
	    File[] the_Files;
	    File parent = new File(file.getParent());
	    the_Files = parent.listFiles(new MyFilter(extensions));
	
	    if(the_Files == null)
	    	return pos;
	    
	    pos = 0;
	    listVideos = new ArrayList<File>();
	    for(int i=0;i<the_Files.length;i++) 
	    {
	    	File tempF = the_Files[i];
	    	
	    	if (tempF.isFile())
	    	{
	    		listVideos.add(tempF);
	    	} 
	    }
	    
	    paths=new ArrayList<String>();
    	File [] fs = new File[listVideos.size()];
	    for(int i=0;i<listVideos.size();i++) {
	    	fs[i] = listVideos.get(i);
	    }
	    Arrays.sort(fs, new MyComparator(MyComparator.NAME_ASCEND));   
	    
	    for(int i=0;i<fs.length;i++) {
	    	File tempF = fs[i];
	    	if(tempF.getPath().equals(file.getPath())) {
	    		pos = i;
	    	}
	    	paths.add(tempF.getPath());
	    }
	    return pos;
	}
    
    //option menu
    private final int MENU_ABOUT = 0;
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, MENU_ABOUT, 0, R.string.str_about);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
	        case MENU_ABOUT:
				try {
					Toast.makeText(FileList.this, " VideoPlayer \n Version: " +
		        			FileList.this.getPackageManager().getPackageInfo("com.farcore.videoplayer", 0).versionName,
		        			Toast.LENGTH_SHORT)
		        			.show();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	return true;
        }
        return false;
    }
    
    public void stopMediaPlayer()//stop the backgroun music player
    {
    	Intent intent = new Intent();
    	intent.setAction("com.android.music.musicservicecommand.pause");
    	intent.putExtra("command", "stop");
    	this.sendBroadcast(intent);
    }
}
