package jie.android.el;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class PackageImporter {
	
	public static final String Tag = PackageImporter.class.getSimpleName();

	private class ImportTask extends AsyncTask<String, Void, Boolean> {

		private String zipfile = null;
		private String local = null;
		private String dbfile = null;
		private String output = null;
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			local = arg0[0];
			zipfile = Environment.getExternalStorageDirectory() + "/jie/cache" + File.separator + arg0[0];
			output = Environment.getExternalStorageDirectory() + "/jie/el";
			
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
			if (result == Boolean.FALSE) {
				Log.e(Tag, "import failed - zipfile : " + zipfile);
			}

			Utils.removeFile(dbfile);
			Utils.removeFile(zipfile);
			
			packageList.remove(local);			
			
			taskRunning = false;
			
			startImport();
		}
	}
	
	
	private ELActivity activity = null;
	private ArrayList<String> packageList = new ArrayList<String>();
	
	private boolean taskRunning = false;

	public PackageImporter(ELActivity activity, String[] packageList) {
		this.activity = activity;
		if (packageList != null) {
			for (final String str : packageList) {
				this.packageList.add(str);
			}
		}
	}

	public void release() {
		
	}
	
	public static String[] check() {
		String path = Environment.getExternalStorageDirectory() + "/jie/cache";
		File p = new File(path);
		if (!p.exists()) {
			return null;
		}
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
				
				new ImportTask().execute(packageList.get(0));
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
						values.put("idx", cursor.getInt(0));
						values.put("title", cursor.getString(1));
						values.put("script", cursor.getString(2));
						values.put("audio", cursor.getString(3));
						values.put("duration", cursor.getInt(4));
						values.put("slowdialog", cursor.getInt(5));
						values.put("explanations", cursor.getInt(6));
						values.put("fastdialog", cursor.getInt(7));
						values.put("flag", cursor.getInt(8));

						if (!isExist(cursor.getInt(0))) {
							activity.getContentResolver().insert(ELContentProvider.URI_EL_ESL, values);
						} else {
							activity.getContentResolver().update(ELContentProvider.URI_EL_ESL, values, "idx=" + cursor.getInt(0), null);
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
		Cursor cursor = activity.getContentResolver().query(ELContentProvider.URI_EL_ESL, new String[] { "count(*)" }, "idx=" + idx, null, null);
		try {
			if (cursor.moveToFirst()) {
				return (cursor.getInt(0) > 0);
			}
		} finally {
			cursor.close();
		}
		
		return false;
	}
	
}
