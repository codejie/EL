package jie.android.el.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

public class Utils {
	public static final String formatMSec(int msec) {
		return String.format("%02d:%02d", 
			    TimeUnit.MILLISECONDS.toMinutes(msec),
			    TimeUnit.MILLISECONDS.toSeconds(msec) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(msec)));
	}
	
	public static final boolean removeFile(final String file) {
		if (file == null) {
			return true;
		}
		File f = new File(file);
		if (f == null) {
			return false;
		}
		return f.delete();
	}
	
	public static final String[] unzipFile(final String input, final String outputPath) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		File in = new File(input);
		
		ZipInputStream zipStream = null;

		try {
			FileInputStream is = new FileInputStream(in);
			zipStream = new ZipInputStream(is);
			
			byte[] buf = new byte[64 * 1024];
			ZipEntry entry = null;
			while ((entry = zipStream.getNextEntry()) != null) {
				File f = new File(outputPath + File.separator + entry.getName());
				if (!entry.isDirectory()) {
					f.deleteOnExit();					
//					if (!f.createNewFile()) {
//						return null;
//					}
					FileOutputStream out = new FileOutputStream(f);
					int size = -1;
					while((size = zipStream.read(buf)) > 0) {
						out.write(buf, 0, size);
					}
					out.close();
					
					ret.add(f.getName());
					
				} else {
					f.mkdirs();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (zipStream != null) {
					zipStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret.toArray(new String[] {});
	}
	
	public static final String checkExternalStorageDirectory(final String path) {
		Pattern pattern = Pattern.compile("/storage/emulated/\\d{1,2}");
		Matcher matcher = pattern.matcher(path);
		if (!matcher.find()) {
			return path;
		} else {
			return path.replace("/storage/emulated/", "/storage/sdcard");
		}
	}
	
	public static final String getExtenalSDCardDirectory() {
		//return checkExternalStorageDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	public static Cursor getNextAudio(Context context, final String[] projects, boolean random, boolean next) {
		
	//	if ()
		
		
		return null;
	}
	
}
