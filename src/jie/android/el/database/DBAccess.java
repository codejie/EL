package jie.android.el.database;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class DBAccess {

	private static int VERSIION		=	1;
	
	private Context context = null;
	
	private SQLiteDatabase db = null;
	
	public DBAccess(Context context) {
		this.context = context;
	}
	
	public void close() {
		if (db != null) {
			db.close();
		}
	}
	
	public boolean open() {
		
		String path = Environment.getExternalStorageDirectory() + "/jie/el";
		File f = new File(path);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				return false;
			}
		}
		
		path += "/el.db";
		
		db = SQLiteDatabase.openOrCreateDatabase(path, null);
		if (db == null) {
			return false;
		}
				
		return createTables();
	}
	
	private boolean createTables() {
		String sql = "CREATE TABLE IF NOT EXISTS [esl] ("
					+ "[idx] INTEGER PRIMARY KEY,"
					+ "[title] TEXT,"
					+ "[doc] TEXT,"
					+ "[playfile] TEXT)";
		db.execSQL(sql);
		
		return true;
	}
	
}
