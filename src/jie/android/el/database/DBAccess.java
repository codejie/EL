package jie.android.el.database;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
					+ "[data] TEXT,"
					+ "[audio] TEXT)";
		db.execSQL(sql);
		
		return true;
	}
	
	public boolean insertESL(final ContentValues values) {
		return (db.insert("esl", null, values) != -1);
	}
	
	public Cursor queryESL(final String[] columns, final String selection, final String[] selectionArgs) {
		return db.query("esl", columns, selection, selectionArgs, null, null, null);
	}

	public Cursor queryESLIssue(int index) {
		return db.query("els", new String[] { "title", "data", "audio" }, "idx=" + index, null, null, null, null);
	}
	
}
