package jie.android.el.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jie.android.el.CommonConsts;
import jie.android.el.CommonConsts.NotificationAction;
import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

public class PackageImporter {
	
	public static final String Tag = PackageImporter.class.getSimpleName();

	private class ImportTask extends AsyncTask<String, Void, Boolean> {

		private String zipfile = null;
		private String local = null;
		private String dbfile = null;
		private String output = null;
		
		@Override
		protected Boolean doInBackground(String... args) {
			local = args[0];
			zipfile = args[1] + args[0];
			output = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_EL;
			
			//unzip
			dbfile = unzipPackage(zipfile, output);
			if (dbfile == null) {
				return Boolean.FALSE;
			}
			dbfile = output + File.separator + dbfile;
			
			//import db			
			if (importPackage(dbfile)) {			
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
					
			if (result != Boolean.FALSE) {
				showNotification(true, local, "EL imports package successfully");				
			} else {
				Log.e(Tag, "import failed - zipfile : " + zipfile);				
				showNotification(false, local, "EL imports package failed.");
			}

			Utils.removeFile(dbfile);
			Utils.removeFile(zipfile);
			
			packageList.remove(local);			
			
			taskRunning = false;
			
			startImport();
		}
	}
	
	private Context context = null;
	private ArrayList<String> packageList = new ArrayList<String>();
	
	private boolean taskRunning = false;

	public PackageImporter(Context context, String[] packageList) {
		this.context = context;
		if (packageList != null) {
			for (final String str : packageList) {
				this.packageList.add(str);
			}
		}
	}

	public void release() {
		
	}
	
	public static String[] check() {
		String path = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_CACHE;
		File p = new File(path);
		
		return p.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String file) {
				return file.startsWith("package") && file.endsWith(".zip");
			}
		});
	}

	public void refresh() {
		final String[] res = PackageImporter.check();
		if (res != null && res.length > 0) {
			packageList.clear();
			for (final String str : res) {
				this.packageList.add(str);
			}
		}
		
		startImport();
	}
	
	public void startImport() {
		if (packageList.size() > 0) {
			if (!taskRunning) {
				taskRunning = true;				
				new ImportTask().execute(packageList.get(0), Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_CACHE);
			}
		}
	}
	
	private String unzipPackage(String file, String output) {

//		String output = Environment.getExternalStorageDirectory() + "/jie/el";
		
		String[] ret = Utils.unzipFile(file, output);
				
		if (ret.length > 0) {
			for (String f : ret) {
				if (f.startsWith("package") && f.endsWith(".db")) {
					return (f);
				}
			}
		}
		
		return null;
	}
	
	public boolean importPackage(String dbfile) {
		try {
			SQLiteDatabase src = SQLiteDatabase.openDatabase(dbfile, null, SQLiteDatabase.OPEN_READONLY);
			//check info table (skip)
			//check esl table
			Cursor cursor = src.query("esl", new String[] {"idx", "title", "script", "audio", "duration", "slowdialog", "explanations", "fastdialog", "flag"}, null, null, null, null, null);
			try {
				if (cursor.moveToFirst()) {
					do {
						ContentValues values = new ContentValues();
						values.put("title", cursor.getString(1));
						values.put("script", cursor.getString(2));
						values.put("audio", cursor.getString(3));
						values.put("duration", cursor.getInt(4));
						values.put("slowdialog", cursor.getInt(5));
						values.put("explanations", cursor.getInt(6));
						values.put("fastdialog", cursor.getInt(7));
						values.put("flag", cursor.getInt(8));

						if (!isExist(cursor.getInt(0))) {
							values.put("idx", cursor.getInt(0));
							context.getContentResolver().insert(ELContentProvider.URI_EL_ESL, values);
						} else {
							context.getContentResolver().update(ELContentProvider.URI_EL_ESL, values, "idx=" + cursor.getInt(0), null);
						}
								
					} while (cursor.moveToNext());
				}				
			} finally {
				cursor.close();
			}			
			
		} catch (SQLiteException e) {
			return false;
		}
		
		return true;
	}

	private boolean isExist(int idx) {
		Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_EL_ESL, new String[] { "count(*)" }, "idx=" + idx, null, null);
		try {
			if (cursor.moveToFirst()) {
				return (cursor.getInt(0) > 0);
			}
		} finally {
			cursor.close();
		}
		
		return false;
	}

	public void loadBundledPackage() {

		InputStream is = null;
		try {
			is = context.getAssets().open("package_1.zip");
			File f = new File(Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_CACHE + "package_1.zip");
			FileOutputStream out = new FileOutputStream(f);
			if (out != null) {
				byte[] buf = new byte[64 * 1024];
				try {
					int size = -1;
					while((size = is.read(buf)) > 0) {
						out.write(buf, 0, size);
					}
				} finally {
					out.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		taskRunning = true;				
		new ImportTask().execute("package_1.zip", Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_CACHE);
	}

	private void showNotification(boolean succ, final String title, final String text) {
		
		Intent intent = new Intent(NotificationAction.ACTION_SHOW);
		if (succ) {
			intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.IMPORT.getId());
		} else {
			intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.WARNING.getId());
		}
		intent.putExtra(NotificationAction.DATA_TITLE, title);
		intent.putExtra(NotificationAction.DATA_TEXT, text);
		
		context.sendBroadcast(intent);		
	}
}
