package amlogic.videoplayer;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.Dialog;

public class videobar extends Activity {
	private static final int TV_PANEL = 1;
    private static final int PLAY_MODE = 2;
    private static final int AUDIO_TRACE = 3;
    private static final int SUBTITLE_SET = 4;
    private static final int DISPLAY_MODE = 5;
    private static final int BRIGHTNESS_SET = 6;
  

    protected Dialog onCreateDialog(int id) {
    	 switch (id) {
         case TV_PANEL:
             return new AlertDialog.Builder(videobar.this)
                 .setTitle(R.string.str_vtvorpanel)
                 .setItems(R.array.tv_panel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create();
         case PLAY_MODE:
             return new AlertDialog.Builder(videobar.this)
                 .setTitle(R.string.str_vplaymode)
                 .setItems(R.array.play_mode, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create(); 
         case AUDIO_TRACE:
             return new AlertDialog.Builder(videobar.this)
                 .setTitle(R.string.str_vaudio)
                 .setItems(R.array.audio_trace, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create();
         case SUBTITLE_SET:
             return new AlertDialog.Builder(videobar.this)
                 .setTitle(R.string.str_vsubtitle)
                 .setItems(R.array.subtitle, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create();
         case DISPLAY_MODE:
             return new AlertDialog.Builder(videobar.this)
                 .setTitle(R.string.str_vdisplay)
                 .setItems(R.array.display, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create(); 
         case BRIGHTNESS_SET:
             return new AlertDialog.Builder(videobar.this)
                 .setTitle(R.string.str_brightness)
                 .setItems(R.array.brightness, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {

                       
                     }
                 })
                 .create();
         }
         return null;
    }
	        	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_imagebutton);
	
		
		 ImageButton panelortv = (ImageButton) findViewById(R.id.ImageButton01);
	        panelortv.setOnClickListener(new View.OnClickListener() 
		    {
	            public void onClick(View v) 
	            {
	            	showDialog(TV_PANEL);
	            } 
		    });
	        
	        ImageButton playmode = (ImageButton) findViewById(R.id.ImageButton02);
	        playmode.setOnClickListener(new View.OnClickListener() 
		    {
	            public void onClick(View v) 
	            {
	            	showDialog(PLAY_MODE);
	            } 
		    });
	        ImageButton audiotrace = (ImageButton) findViewById(R.id.ImageButton03);
	        audiotrace.setOnClickListener(new View.OnClickListener() 
		    {
	            public void onClick(View v) 
	            {
	            	showDialog(AUDIO_TRACE);
	            } 
		    });
	        ImageButton sutitle = (ImageButton) findViewById(R.id.ImageButton04);
	        sutitle.setOnClickListener(new View.OnClickListener() 
		    {
	            public void onClick(View v) 
	            {
	            	showDialog(SUBTITLE_SET);
	            } 
		    });
	        ImageButton display = (ImageButton) findViewById(R.id.ImageButton05);
	        display.setOnClickListener(new View.OnClickListener() 
		    {
	            public void onClick(View v) 
	            {
	            	showDialog(DISPLAY_MODE);
	            } 
		    });
	        ImageButton brigtness = (ImageButton) findViewById(R.id.ImageButton06);
	        brigtness.setOnClickListener(new View.OnClickListener() 
		    {
	            public void onClick(View v) 
	            {
	            	showDialog(BRIGHTNESS_SET);
	            } 
		    }); 
	}
}