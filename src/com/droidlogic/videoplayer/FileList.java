package com.droidlogic.videoplayer;

import android.os.storage.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.droidlogic.videoplayer.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.os.SystemProperties;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.ProgressBar;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Message;

public class FileList extends ListActivity {
        private static final String ROOT_PATH = "/storage";
        private static final String SHEILD_EXT_STOR = "/storage/sdcard0/external_storage";
        private static final String NAND_PATH = "/storage/sdcard0";
        private static final String SD_PATH = "/storage/external_storage/sdcard1";
        private static final String USB_PATH = "/storage/external_storage";

        private boolean listAllFiles = true;
        private List<File> listFiles = null;
        private List<File> listVideos = null;
        private List<String> items = null;
        private List<String> paths = null;
        private List<String> currentlist = null;
        private String currenturl = null;
        private String root_path = ROOT_PATH;
        private String extensions ;
        private static String ISOpath = null;

        private TextView tileText;
        private TextView nofileText;
        private TextView searchText;
        private ProgressBar sp;
        private boolean isScanning = false;
        private boolean isQuerying = false;
        private int scanCnt = 0;
        private File file;
        private static String TAG = "FileList";
        Timer timer = new Timer();
        Timer timerScan = new Timer();

        private int item_position_selected, item_position_first, fromtop_piexl;
        private ArrayList<Integer> fileDirectory_position_selected;
        private ArrayList<Integer> fileDirectory_position_piexl;
        private int pathLevel = 0;
        private final String iso_mount_dir = "/storage/external_storage/VIRTUAL_CDROM";
        private Uri uri;

        private final StorageEventListener mListener = new StorageEventListener() {
            public void onUsbMassStorageConnectionChanged (boolean connected) {
                //this is the action when connect to pc
                return ;
            }
            public void onStorageStateChanged (String path, String oldState, String newState) {
                Log.d (TAG, "onStorageStateChanged: " + path + " oldState=" + oldState + " newState=" + newState + ", PlayList.getinstance().rootPath:" + PlayList.getinstance().rootPath);
                if (newState == null || path == null) {
                    return;
                }
                if (newState.compareTo ("mounted") == 0) {
                    if (PlayList.getinstance().rootPath == null
                    || PlayList.getinstance().rootPath.equals (root_path)) {
                        BrowserFile (root_path);
                    }
                }
                else if (newState.compareTo ("unmounted") == 0
                || newState.compareTo ("removed") == 0) {
                    if (PlayList.getinstance().rootPath.startsWith (path)
                            || PlayList.getinstance().rootPath.equals (root_path)
                    || (PlayList.getinstance().rootPath.startsWith (SD_PATH) && path.equals ("/storage/sdcard1"))) {
                        BrowserFile (root_path);
                    }
                }
            }
        };

        private void waitForRescan() {
            final Handler handler = new Handler() {
                public void handleMessage (Message msg) {
                    switch (msg.what) {
                        case 0x5c:
                            isScanning = false;
                            prepareFileForList();
                            timerScan.cancel();
                            break;
                    }
                    super.handleMessage (msg);
                }
            };
            TimerTask task = new TimerTask() {
                public void run() {
                    Message message = Message.obtain();
                    message.what = 0x5c;
                    handler.sendMessage (message);
                }
            };
            timer.cancel();
            timer = new Timer();
            timer.schedule (task, 500);
        }

        private void waitForScanFinish() {
            final Handler handler = new Handler() {
                public void handleMessage (Message msg) {
                    switch (msg.what) {
                        case 0x6c:
                            scanCnt--;
                            isScanning = false;
                            prepareFileForList();
                            break;
                    }
                    super.handleMessage (msg);
                }
            };
            TimerTask task = new TimerTask() {
                public void run() {
                    Message message = Message.obtain();
                    message.what = 0x6c;
                    handler.sendMessage (message);
                }
            };
            timerScan.cancel();
            timerScan = new Timer();
            timerScan.schedule (task, 20000);
        }

        private BroadcastReceiver mScanListener = new BroadcastReceiver() {
            @Override
            public void onReceive (Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_MEDIA_UNMOUNTED.equals (action)) {
                    prepareFileForList();
                }
                else if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals (action)) {
                    if (!isScanning) {
                        isScanning = true;
                        setListAdapter (null);
                        showSpinner();
                        scanCnt++;
                        waitForScanFinish();
                    }
                }
                else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals (action)) {
                    if ( (isScanning) && (scanCnt == 1)) {
                        scanCnt--;
                        waitForRescan();
                    }
                }
                /*else if(Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                }
                else if(Intent.ACTION_MEDIA_EJECT.equals(action)) {
                }*/
            }
        };

        @Override
        public void onResume() {
            super.onResume();
            if (!listAllFiles) {
                File file = null;
                if (PlayList.getinstance().rootPath != null) {
                    file = new File (PlayList.getinstance().rootPath);
                }
                if ( (file != null) && file.exists()) {
                    File[] the_Files;
                    the_Files = file.listFiles (new MyFilter (extensions));
                    if (the_Files == null) {
                        PlayList.getinstance().rootPath = root_path;
                    }
                    BrowserFile (PlayList.getinstance().rootPath);
                }
                else {
                    PlayList.getinstance().rootPath = root_path;
                    BrowserFile (PlayList.getinstance().rootPath);
                }
                getListView().setSelectionFromTop (item_position_selected, fromtop_piexl);
                StorageManager m_storagemgr = (StorageManager) getSystemService (Context.STORAGE_SERVICE);
                m_storagemgr.registerListener (mListener);
            }
            else {
                IntentFilter f = new IntentFilter();
                f.addAction (Intent.ACTION_MEDIA_EJECT);
                f.addAction (Intent.ACTION_MEDIA_MOUNTED);
                f.addAction (Intent.ACTION_MEDIA_UNMOUNTED);
                f.addAction (Intent.ACTION_MEDIA_SCANNER_STARTED);
                f.addAction (Intent.ACTION_MEDIA_SCANNER_FINISHED);
                f.addDataScheme ("file");
                registerReceiver (mScanListener, f);
            }
        }

        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onPause() {
            super.onPause();
            if (!listAllFiles) {
                StorageManager m_storagemgr = (StorageManager) getSystemService (Context.STORAGE_SERVICE);
                m_storagemgr.unregisterListener (mListener);
            }
            else {
                isScanning = false;
                isQuerying = false;
                scanCnt = 0;
                timer.cancel();
                timerScan.cancel();
                unregisterReceiver (mScanListener);
            }
        }

        @Override
        protected void onCreate (Bundle icicle) {
            super.onCreate (icicle);
            extensions = getResources().getString (R.string.support_video_extensions);
            requestWindowFeature (Window.FEATURE_NO_TITLE);
            setContentView (R.layout.file_list);
            PlayList.setContext (this);
            listAllFiles = SystemProperties.getBoolean ("vplayer.listall.enable", false);
            currentlist = new ArrayList<String>();
            if (!listAllFiles) {
                try {
                    Bundle bundle = new Bundle();
                    bundle = this.getIntent().getExtras();
                    if (bundle != null) {
                        item_position_selected = bundle.getInt ("item_position_selected");
                        item_position_first = bundle.getInt ("item_position_first");
                        fromtop_piexl = bundle.getInt ("fromtop_piexl");
                        fileDirectory_position_selected = bundle.getIntegerArrayList ("fileDirectory_position_selected");
                        fileDirectory_position_piexl = bundle.getIntegerArrayList ("fileDirectory_position_piexl");
                        pathLevel = fileDirectory_position_selected.size();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                if (PlayList.getinstance().rootPath == null) {
                    PlayList.getinstance().rootPath = root_path;
                }
                BrowserFile (PlayList.getinstance().rootPath);
            }
            Button home = (Button) findViewById (R.id.Button_home);
            home.setOnClickListener (new View.OnClickListener() {
                public void onClick (View v) {
                    if (listAllFiles) {
                        if (!isScanning) {
                            reScanVideoFiles();
                        }
                    }
                    else {
                        FileList.this.finish();
                        PlayList.getinstance().rootPath = null;
                    }
                }
            });
            Button exit = (Button) findViewById (R.id.Button_exit);
            exit.setOnClickListener (new View.OnClickListener() {
                public void onClick (View v) {
                    if (listAllFiles) {
                        FileList.this.finish();
                        return;
                    }
                    if (paths == null) {
                        FileList.this.finish();
                        PlayList.getinstance().rootPath = null;
                    }
                    else {
                        if (paths.isEmpty()) {
                            FileList.this.finish();
                            PlayList.getinstance().rootPath = null;
                        }
                        file = new File (paths.get (0).toString());
                        if (file.getParent().compareTo (iso_mount_dir) == 0 && ISOpath != null) {
                            file = new File (ISOpath + "/VIRTUAL_CDROM");
                            Log.i (TAG, "[exit button]file:" + file);
                            ISOpath = null;
                        }
                        currenturl = file.getParentFile().getParent();
                        if ( (file.getParent().compareToIgnoreCase (root_path) != 0) && (pathLevel > 0)) {
                            String path = file.getParent();
                            String parent_path = file.getParentFile().getParent();
                            if ( (path.equals (NAND_PATH) || path.equals (SD_PATH) || parent_path.equals (USB_PATH)) && (pathLevel > 0)) {
                                pathLevel = 0;
                                BrowserFile (ROOT_PATH);
                            }
                            else {
                                BrowserFile (currenturl);
                                pathLevel--;
                                getListView().setSelectionFromTop (fileDirectory_position_selected.get (pathLevel), fileDirectory_position_piexl.get (pathLevel));
                                fileDirectory_position_selected.remove (pathLevel);
                                fileDirectory_position_piexl.remove (pathLevel);
                            }
                        }
                        else {
                            FileList.this.finish();
                            PlayList.getinstance().rootPath = null;
                        }
                    }
                }
            });
            nofileText = (TextView) findViewById (R.id.TextView_nofile);
            searchText = (TextView) findViewById (R.id.TextView_searching);
            sp = (ProgressBar) findViewById (R.id.spinner);
            if (listAllFiles) {
                prepareFileForList();
            }
        }

        private void showSpinner() {
            if (listAllFiles) {
                if ( (isScanning) || (isQuerying)) {
                    sp.setVisibility (View.VISIBLE);
                    searchText.setVisibility (View.VISIBLE);
                    nofileText.setVisibility (View.INVISIBLE);
                }
                else {
                    sp.setVisibility (View.INVISIBLE);
                    searchText.setVisibility (View.INVISIBLE);
                    int total = paths.size();
                    if (total == 0) {
                        nofileText.setVisibility (View.VISIBLE);
                    }
                    else if (total > 0) {
                        nofileText.setVisibility (View.INVISIBLE);
                    }
                }
            }
            else {
                sp.setVisibility (View.GONE);
                nofileText.setVisibility (View.GONE);
                searchText.setVisibility (View.GONE);
            }
        }


        private void prepareFileForList() {
            if (listAllFiles) {
                //Intent intent = getIntent();
                //uri = intent.getData();
                String[] mCursorCols = new String[] {
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DURATION,
                    //              MediaStore.Video.Media.BOOKMARK,
                    //              MediaStore.Video.Media.PLAY_TIMES
                };
                String patht = null;
                String namet = null;
                paths = new ArrayList<String>();
                items = new ArrayList<String>();
                paths.clear();
                items.clear();
                setListAdapter (null);
                isQuerying = true;
                showSpinner();
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = getContentResolver().query (uri, mCursorCols, null, null, null);
                cursor.moveToFirst();
                int colidx = cursor.getColumnIndexOrThrow (MediaStore.Video.Media.DATA);
                for (int i = 0; i < cursor.getCount(); i++) {
                    patht = cursor.getString (colidx);
                    //Log.e("wxl", "cursor["+colidx+"]:"+patht);
                    int index = patht.lastIndexOf ("/");
                    if (index >= 0) {
                        namet = patht.substring (index);
                    }
                    items.add (namet);
                    paths.add (patht);
                    cursor.moveToNext();
                }
                tileText = (TextView) findViewById (R.id.TextView_path);
                tileText.setText (R.string.all_file);
                if (paths.size() > 0) {
                    setListAdapter (new MyAdapter (this, items, paths));
                }
                isQuerying = false;
                showSpinner();
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private void BrowserFile (String filePath) {
            int i = 0;
            int dev_usb_count = 0;
            int dev_cd_count = 0;
            file = new File (filePath);
            listFiles = new ArrayList<File>();
            items = new ArrayList<String>();
            paths = new ArrayList<String>();
            String[] files = file.list();
            if (files != null) {
                for (i = 0; i < files.length; i++) {
                    if (files[i].equals ("VIRTUAL_CDROM")) {
                        execCmd ("vdc loop unmount");
                        break;
                    }
                }
            }
            listFiles.clear();
            searchFile (file);
            if (listFiles.isEmpty()) {
                Toast.makeText (FileList.this, R.string.str_no_file, Toast.LENGTH_SHORT).show();
                //paths =currentlist;
                paths.clear();
                paths.addAll (currentlist);
                return;
            }
            Log.d (TAG, "BrowserFile():" + filePath);
            PlayList.getinstance().rootPath = filePath;
            //Log.d(TAG, "BrowserFile() listFiles.size():"+listFiles.size());
            File [] fs = new File[listFiles.size()];
            for (i = 0; i < listFiles.size(); i++) {
                fs[i] = listFiles.get (i);
                //Log.d(TAG, "BrowserFile() fs[i]:"+fs[i]);
            }
            if (!filePath.equals (ROOT_PATH)) {
                //System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
                try {
                    Arrays.sort (fs, new MyComparator (MyComparator.NAME_ASCEND));
                }
                catch (IllegalArgumentException ex) {
                }
            }
            for (i = 0; i < fs.length; i++) {
                File tempF = fs[i];
                String tmppath = tempF.getName();
                //Log.d(TAG, "BrowserFile() tmppath:"+tmppath);
                //change device name;
                if (filePath.equals (ROOT_PATH)) {
                    String tpath = tempF.getAbsolutePath();
                    if (tpath.equals (NAND_PATH)) {
                        tmppath = "sdcard";
                    }
                    else if (tpath.equals (SD_PATH)) {
                        tmppath = "external_sdcard";
                    }
                    else if ( ( (!tpath.equals (SD_PATH)) && tpath.startsWith (USB_PATH + "/sd")) || tpath.startsWith (ROOT_PATH + "/udisk")) {
                        dev_usb_count++;
                        char data = (char) ('A' + dev_usb_count - 1);
                        tmppath =  getText (R.string.usb_device_str) + "(" + data + ":)" ;
                        //tmppath = "usb"+" "+tpath.substring(5);//5 is the len of "/mnt/"
                    }
                    else if ( (!tpath.equals (SD_PATH)) && tpath.startsWith (USB_PATH + "/sr")) {
                        dev_cd_count++;
                        char data = (char) ('A' + dev_cd_count - 1);
                        tmppath =  getText (R.string.cdrom_device_str) + "(" + data + ":)" ;
                    }
                    //delete used folder
                    if ( (!tpath.equals ("/mnt/asec")) && (!tpath.equals ("/mnt/secure")) &&
                            (!tpath.equals ("/mnt/obb")) && (!tpath.equals ("/mnt/usbdrive"))
                            && (!tpath.equals ("/mnt/shell"))) {
                        String path = changeDevName (tmppath);
                        //Log.d(TAG, "BrowserFile() items.add path:"+path);
                        items.add (path);
                        paths.add (tempF.getPath());
                    }
                }
                else {
                    //Log.d(TAG, "BrowserFile() items.add tmppath:"+tmppath);
                    items.add (tmppath);
                    paths.add (tempF.getPath());
                }
            }
            tileText = (TextView) findViewById (R.id.TextView_path);
            tileText.setText (catShowFilePath (filePath));
            setListAdapter (new MyAdapter (this, items, paths));
        }

        private String changeDevName (String tmppath) {
            String path = "";
            String internal = getString (R.string.memory_device_str);
            String sdcard = getString (R.string.sdcard_device_str);
            String usb = getString (R.string.usb_device_str);
            String cdrom = getString (R.string.cdrom_device_str);
            String sdcardExt = getString (R.string.ext_sdcard_device_str);
            //Log.i("wxl","[changeDevName]tmppath:"+tmppath);
            if (tmppath.equals ("flash")) {
                path = internal;
            }
            else if (tmppath.equals ("sdcard")) {
                path = sdcard;
            }
            else if (tmppath.equals ("usb")) {
                path = usb;
            }
            else if (tmppath.equals ("cd-rom")) {
                path = cdrom;
            }
            else if (tmppath.equals ("external_sdcard")) {
                path = sdcardExt;
            }
            else {
                path = tmppath;
            }
            //Log.i("wxl","[changeDevName]path:"+path);
            return path;
        }

        private String catShowFilePath (String path) {
            String text = null;
            if (path.startsWith ("/mnt/flash")) {
                text = path.replaceFirst ("/mnt/flash", "/mnt/nand");
            }
            else if (path.startsWith ("/mnt/sda")) {
                text = path.replaceFirst ("/mnt/sda", "/mnt/usb sda");
            }
            else if (path.startsWith ("/mnt/sdb")) {
                text = path.replaceFirst ("/mnt/sdb", "/mnt/usb sdb");
            }
            //else if(path.startsWith("/mnt/sdcard"))
            //text=path.replaceFirst("/mnt/sdcard","sdcard");
            return text;
        }

        public void searchFile (File file) {
            File filetmp;
            File[] the_Files;
            the_Files = file.listFiles (new MyFilter (extensions));
            if (the_Files == null) {
                Toast.makeText (FileList.this, R.string.str_no_file, Toast.LENGTH_SHORT).show();
                return;
            }
            String curPath = file.getPath();
            if (curPath.equals (root_path)) {
                File dir = new File (NAND_PATH);
                if (dir.exists() && dir.isDirectory()) {
                    filetmp = new File (NAND_PATH);
                    if (filetmp.listFiles() != null && filetmp.listFiles().length > 0) {
                        listFiles.add (dir);
                    }
                }
                dir = new File (SD_PATH);
                if (dir.exists() && dir.isDirectory()) {
                    filetmp = new File (SD_PATH);
                    //Log.i(TAG,"[searchFile]filetmp.listFiles():"+filetmp.listFiles());
                    //Log.i(TAG,"[searchFile]================");
                    if (filetmp.listFiles() != null && filetmp.listFiles().length > 0) {
                        listFiles.add (dir);
                    }
                }
                dir = new File (USB_PATH);
                if (dir.exists() && dir.isDirectory()) {
                    if (dir.listFiles() != null) {
                    for (File pfile : dir.listFiles()) {
                            if (pfile.isDirectory()) {
                                String path = pfile.getAbsolutePath();
                                if ( (path.startsWith (USB_PATH + "/sd") || path.startsWith (USB_PATH + "/sr")) && !path.equals (SD_PATH)) {
                                    filetmp = new File (path);
                                    if (filetmp.listFiles() != null && filetmp.listFiles().length > 0) {
                                        listFiles.add (pfile);
                                    }
                                }
                            }
                        }
                    }
                }
                dir = new File (ROOT_PATH);
                if (dir.exists() && dir.isDirectory()) {
                    if (dir.listFiles() != null) {
                    for (File qfile : dir.listFiles()) {
                            if (qfile.isDirectory()) {
                                String path = qfile.getAbsolutePath();
                                if (path.startsWith (ROOT_PATH + "/udisk")) {
                                    filetmp = new File (path);
                                    if (filetmp.listFiles() != null && filetmp.listFiles().length > 0) {
                                        listFiles.add (qfile);
                                    }
                                }
                            }
                        }
                    }
                }
                return;
            }
            for (int i = 0; i < the_Files.length; i++) {
                File tempF = the_Files[i];
                if (tempF.isDirectory()) {
                    if (!tempF.isHidden()) {
                        listFiles.add (tempF);
                    }
                    //shield some path
                    if ( (tempF.toString()).equals (SHEILD_EXT_STOR)) {
                        listFiles.remove (tempF);
                        continue;
                    }
                }
                else {
                    try {
                        listFiles.add (tempF);
                    }
                    catch (Exception e) {
                        return;
                    }
                }
            }
        }

        public boolean isISOFile (File file) {
            String fname = file.getName();
            String sname = ".iso";
            if (fname == "") {
                Log.e (TAG, "NULL file");
                return false;
            }
            if (fname.toLowerCase().endsWith (sname)) {
                return true;
            }
            return false;
        }

        public void execCmd (String cmd) {
            int ch;
            Process p = null;
            Log.d (TAG, "exec command: " + cmd);
            try {
                p = Runtime.getRuntime().exec (cmd);
                InputStream in = p.getInputStream();
                InputStream err = p.getErrorStream();
                StringBuffer sb = new StringBuffer (512);
                while ( (ch = in.read()) != -1) {
                    sb.append ( (char) ch);
                }
                if (sb.toString() != "") {
                    Log.d (TAG, "exec out:" + sb.toString());
                }
                while ( (ch = err.read()) != -1) {
                    sb.append ( (char) ch);
                }
                if (sb.toString() != "") {
                    Log.d (TAG, "exec error:" + sb.toString());
                }
            }
            catch (IOException e) {
                Log.d (TAG, "IOException: " + e.toString());
            }
        }

        @Override
        protected void onListItemClick (ListView l, View v, int position, long id) {
            File file = new File (paths.get (position));
            currentlist.clear();
            currentlist.addAll (paths);
            //currentlist =paths;
            if (fileDirectory_position_selected == null) {
                fileDirectory_position_selected = new ArrayList<Integer>();
            }
            if (fileDirectory_position_piexl == null) {
                fileDirectory_position_piexl = new ArrayList<Integer>();
            }
            if (file.isDirectory()) {
                item_position_selected = getListView().getSelectedItemPosition();
                item_position_first = getListView().getFirstVisiblePosition();
                View cv = getListView().getChildAt (item_position_selected - item_position_first);
                if (cv != null) {
                    fromtop_piexl = cv.getTop();
                }
                BrowserFile (paths.get (position));
                if (!listFiles.isEmpty()) {
                    fileDirectory_position_selected.add (item_position_selected);
                    fileDirectory_position_piexl.add (fromtop_piexl);
                    pathLevel++;
                }
            }
            else if (isISOFile (file)) {
                execCmd ("vdc loop unmount");
                ISOpath = file.getPath();
                String cm = "vdc loop mount " + "\"" + ISOpath + "\"";
                Log.d (TAG, "ISO path:" + ISOpath);
                execCmd (cm);
                BrowserFile (iso_mount_dir);
                fileDirectory_position_selected.add (item_position_selected);
                fileDirectory_position_piexl.add (fromtop_piexl);
                pathLevel++;
            }
            else {
                if (!listAllFiles) {
                    int pos = filterDir (file);
                    if (pos < 0) {
                        return;
                    }
                    PlayList.getinstance().rootPath = file.getParent();
                    PlayList.getinstance().setlist (paths, pos);
                    item_position_selected = getListView().getSelectedItemPosition();
                    item_position_first = getListView().getFirstVisiblePosition();
                    if (!listAllFiles) {
                        View cv = getListView().getChildAt (item_position_selected - item_position_first);
                        if (cv != null) {
                            fromtop_piexl = cv.getTop();
                        }
                    }
                }
                else {
                    PlayList.getinstance().setlist (paths, position);
                }
                showvideobar();
            }
        }
        public boolean onKeyDown (int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (listAllFiles) {
                    FileList.this.finish();
                    return true;
                }
                if (paths == null) {
                    FileList.this.finish();
                    PlayList.getinstance().rootPath = null;
                }
                else {
                    if (paths.isEmpty()) {
                        FileList.this.finish();
                        PlayList.getinstance().rootPath = null;
                    }
                    file = new File (paths.get (0).toString());
                    if (file.getParent().compareTo (iso_mount_dir) == 0 && ISOpath != null) {
                        file = new File (ISOpath + "/VIRTUAL_CDROM");
                        Log.i (TAG, "[onKeyDown]file:" + file);
                        ISOpath = null;
                    }
                    currenturl = file.getParentFile().getParent();
                    if ( (file.getParent().compareToIgnoreCase (root_path) != 0) && (pathLevel > 0)) {
                        String path = file.getParent();
                        String parent_path = file.getParentFile().getParent();
                        if ( (path.equals (NAND_PATH) || path.equals (SD_PATH) || parent_path.equals (USB_PATH)) && (pathLevel > 0)) {
                            pathLevel = 0;
                            BrowserFile (ROOT_PATH);
                        }
                        else {
                            BrowserFile (currenturl);
                            pathLevel--;
                            getListView().setSelectionFromTop (fileDirectory_position_selected.get (pathLevel), fileDirectory_position_piexl.get (pathLevel));
                            fileDirectory_position_selected.remove (pathLevel);
                            fileDirectory_position_piexl.remove (pathLevel);
                        }
                    }
                    else {
                        FileList.this.finish();
                        PlayList.getinstance().rootPath = null;
                    }
                }
                return true;
            }
            return super.onKeyDown (keyCode, event);
        }
        private void showvideobar() {
            //* new an Intent object and ponit a class to start
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            if (!listAllFiles) {
                bundle.putInt ("item_position_selected", item_position_selected);
                bundle.putInt ("item_position_first", item_position_first);
                bundle.putInt ("fromtop_piexl", fromtop_piexl);
                bundle.putIntegerArrayList ("fileDirectory_position_selected", fileDirectory_position_selected);
                bundle.putIntegerArrayList ("fileDirectory_position_piexl", fileDirectory_position_piexl);
            }
            bundle.putBoolean ("backToOtherAPK", false);
            intent.setClass (FileList.this, VideoPlayer.class);
            intent.putExtras (bundle);
            ///wxl delete
            /*SettingsVP.setSystemWrite(sw);
            if (SettingsVP.chkEnableOSD2XScale() == true)
                this.setVisible(false);*/
            startActivity (intent);
            FileList.this.finish();
        }

        public int filterDir (File file) {
            int pos = -1;
            File[] the_Files;
            File parent = new File (file.getParent());
            the_Files = parent.listFiles (new MyFilter (extensions));
            if (the_Files == null) {
                return pos;
            }
            pos = 0;
            listVideos = new ArrayList<File>();
            for (int i = 0; i < the_Files.length; i++) {
                File tempF = the_Files[i];
                if (tempF.isFile()) {
                    listVideos.add (tempF);
                }
            }
            paths = new ArrayList<String>();
            File [] fs = new File[listVideos.size()];
            for (int i = 0; i < listVideos.size(); i++) {
                fs[i] = listVideos.get (i);
            }
            try {
                Arrays.sort (fs, new MyComparator (MyComparator.NAME_ASCEND));
            }
            catch (IllegalArgumentException ex) {
            }
            for (int i = 0; i < fs.length; i++) {
                File tempF = fs[i];
                if (tempF.getPath().equals (file.getPath())) {
                    pos = i;
                }
                paths.add (tempF.getPath());
            }
            return pos;
        }

        //option menu
        private final int MENU_ABOUT = 0;
        public boolean onCreateOptionsMenu (Menu menu) {
            menu.add (0, MENU_ABOUT, 0, R.string.str_about);
            return true;
        }

        public boolean onOptionsItemSelected (MenuItem item) {
            switch (item.getItemId()) {
                case MENU_ABOUT:
                    try {
                        Toast.makeText (FileList.this, " VideoPlayer \n Version: " +
                                        FileList.this.getPackageManager().getPackageInfo ("com.droidlogic.videoplayer", 0).versionName,
                                        Toast.LENGTH_SHORT)
                        .show();
                    }
                    catch (NameNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return true;
            }
            return false;
        }

        public void reScanVideoFiles() {
            Intent intent = new Intent (Intent.ACTION_MEDIA_MOUNTED, Uri.parse ("file://" + ROOT_PATH));
            this.sendBroadcast (intent);
        }

        public void stopMediaPlayer() { //stop the backgroun music player
            Intent intent = new Intent();
            intent.setAction ("com.android.music.musicservicecommand.pause");
            intent.putExtra ("command", "stop");
            this.sendBroadcast (intent);
        }
}
