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

import org.xmlpull.v1.XmlPullParserException;

import jie.android.el.CommonConsts.AppArgument;
import jie.android.el.database.ELContentProvider;
import jie.android.el.database.Word;
import jie.android.el.service.Dictionary;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Xml;
import android.widget.Toast;

public class Utils {
	
	public static String NS_ANDROID = "http://schemas.android.com/apk/res/android";
	
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
	
	public static Cursor getNextAudio(Context context, int index, final String[] projection, boolean random, boolean next) {
		Cursor cursor = null;
		if (random) {
			cursor = context.getContentResolver().query(ELContentProvider.URI_EL_ESL_RANDOM, projection, null, null, null);
		} else if (next) {
			Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL_NEXT, index);
			cursor = context.getContentResolver().query(uri, projection, null, null, null);
			if (cursor != null && cursor.getCount() == 0) {
				cursor.close();
				cursor = context.getContentResolver().query(ELContentProvider.URI_EL_ESL_FIRST, projection, null, null, null);
			}
		} else {
			Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL_PREV, index);
			cursor = context.getContentResolver().query(uri, projection, null, null, null);
			if (cursor != null && cursor.getCount() == 0) {
				cursor.close();
				cursor = context.getContentResolver().query(ELContentProvider.URI_EL_ESL_LAST, projection, null, null, null);
			}			
		}
		
		return cursor;
	}

	public static final String assembleXmlResult(final String word, final Word.XmlResult result) {
		
		String ret = "<LAC><LAC-W>" + word + "</LAC-W>";
		for (final Word.XmlResult.XmlData data : result.getXmlData()) {
			ret += "<LAC-R><LAC-D>" + "Vicon English-Chinese Dictionary" + "</LAC-D>";
			int i = 0;			
			
			for(final Dictionary.XmlResult r : data.getResult()) {
				//TODO : cannot get the correct word, so do not display it now.
				//ret += "<w>" + r.word + "</w>";				
				ret += r.result;
				if ((++i) < data.getResult().size())
					ret += "<s/>";
			}
			ret += "</LAC-R>";
		}
		ret += "</LAC>";
		
		return ret;
	}
	
	public static final SharedPreferences getSharedPreferences(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			return context.getSharedPreferences(AppArgument.NAME, Context.MODE_MULTI_PROCESS);
		} else {
			return context.getSharedPreferences(AppArgument.NAME, 0);
		}
	}

	public static AttributeSet getAttributeSet(Context context, int resId, final String className) {

		XmlResourceParser p = context.getResources().getXml(resId);
		
		try {
			int event = p.next();
			while (event != XmlResourceParser.END_DOCUMENT) {
				if (event == XmlResourceParser.START_TAG) {
					if (p.getName().equals(className)) {
						return Xml.asAttributeSet(p);
					}
				}
				event = p.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static AttributeSet getAttributeSet(Context context, int resId, String className, int id) {
		XmlResourceParser xrp = context.getResources().getXml(resId);
		try {
			int event = xrp.next();
			
			while (event != XmlResourceParser.END_DOCUMENT) {
				if (event == XmlResourceParser.START_TAG) {
					if (xrp.getName().equals(className)) {
						String vid = xrp.getAttributeValue(NS_ANDROID, "id");
						if (vid != null) {
							if (Integer.valueOf(vid.substring(1)) == id) {
								return Xml.asAttributeSet(xrp);
							}
						}
					}
				}
				event = xrp.next();
			}			
			
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static void showToast(Context context, int resId) {
		showToast(context, context.getText(resId).toString());
	}
	public static void showToast(Context context, final String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
}
