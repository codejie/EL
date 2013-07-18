package jie.android.el.database;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public abstract class DBAccess {

	protected SQLiteDatabase db = null;
	
	public DBAccess(int version) {
	}
	
	public boolean openDb(final String name, boolean onlyOpen) {
		
		if (name == null) {
			return false;
		}
		
		return openDb(new File(name), onlyOpen);
	}
	
	public boolean openDb(final File file, boolean onlyOpen) {
		if (file == null) {
			return false;
		}
		
		try {
			if (onlyOpen) {
				if (!file.exists()) {
					return false;
				}
				db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
			} else {
				File p = file.getParentFile();
				if (!p.exists()) {
					if (!p.mkdirs()) {
						return false;
					}
				}
				db = SQLiteDatabase.openOrCreateDatabase(file, null);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		
		return (db != null);
		
	}
		
	public void close() {
		if (db != null) {
			db.close();
		}
	}
	
	abstract public boolean open();
	
}
