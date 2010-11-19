package amlogic.videoplayer;

import java.io.File;

public class FileType {
	private String type;
	
	public FileType() { 
		type = ""; 
		}
	public String getFileType(File f) { 
		String fName=f.getName();
		String end=fName.substring(fName.lastIndexOf(".")+1,fName.length()).toLowerCase(); 

		if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||end.equals("xmf")
				||end.equals("ogg")||end.equals("wav")) {
			type = "audio"; 
		}
		else if(end.equals("3gp")||end.equals("mp4")||end.equals("avi")||
				end.equals("mov")||end.equals("mpeg")||end.equals("mpg")||
				end.equals("rm")||end.equals("rmvb")||end.equals("vob")
				||end.equals("mkv")||end.equals("ts")) {
			type = "video";
		}
		else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||end.equals("jpeg")
				||end.equals("bmp")) {
			type = "image";
		}
		else {
			type="*";
		}
		type += "/*"; 
		return type; 
	}
}

