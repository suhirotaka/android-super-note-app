package jp.gr.java_conf.suhirotaka.android_noteapp_proto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;

public class MyUtil {
	
	public static final String LOG_TAG_EXCEPTION = "my_exception";

	public static String getSqlDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

	public static boolean copyFile(File fromFile, File toFile) {
  	    InputStream fromStream = null;
   	    OutputStream toStream = null;
		try {
  	        fromStream = new FileInputStream(fromFile);
   	        toStream = new FileOutputStream(toFile);
    
    	    // Transfer bytes
    	    byte[] buf = new byte[1024];
    	    int len;
    	    while ((len = fromStream.read(buf)) > 0) {
    	        toStream.write(buf, 0, len);
    	    }
        }catch(IOException e) {
        	return false;
        }finally {
            try {
                if (fromStream != null) {
                    fromStream.close();
                }
            }catch (IOException e) {
            	Log.i(LOG_TAG_EXCEPTION, "Failed to close file stream.");
            } finally {
            	try {
                    if (toStream != null) {
                        toStream.close();
                    }
                }catch (IOException e) {
                	Log.i(LOG_TAG_EXCEPTION, "Failed to close file stream.");
                }
            }
        }
	    return true;
	}
	/*
	public static boolean copyFile(FileInputStream fromFile, FileOutputStream toFile) {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        }catch(IOException e) {
        	return false;
        }finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            }catch (IOException e) {
            	Log.i(LOG_TAG_EXCEPTION, "Failed to close file channel.");
            } finally {
            	try {
                    if (toChannel != null) {
                        toChannel.close();
                    }
                }catch (IOException e) {
            	    Log.i(LOG_TAG_EXCEPTION, "Failed to close file channel.");
                }
            }
        }
        return true;
    }
    */

}