package jie.android.el.database;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class ELDBAccess_old extends DBAccess {

	private static final int VERSION		=	1;
	private static final String FILE		=	"/jie/el/el.db";
	
	private Context context = null;
	
	public ELDBAccess(Context context) {
		super(VERSION);
		this.context = context;
	}
	
	@Override
	public boolean open() {
		
		String path = Environment.getExternalStorageDirectory() + FILE;
		
		if(openDb(path, false)) {
			return createTables();
		} else {
			return false;
		}
	}
	
	private boolean createTables() {
		String sql = "CREATE TABLE IF NOT EXISTS [esl] ("
					+ "[idx] INTEGER PRIMARY KEY,"
					+ "[title] TEXT,"
					+ "[script] TEXT,"
					+ "[audio] TEXT,"
					+ "[duration] INTEGER DEFAULT (0),"
					+ "[slowdialog] INTEGER DEFAULT (-1)," 
					+ "[explanations] INTEGER DEFAULT (-1)," 
					+ "[fastdialog] INTEGER DEFAULT (-1)," 
					+ "[flag] INTEGER DEFAULT (-1))";
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
		return db.query("esl", new String[] { "title", "script", "audio" }, "idx=" + index, null, null, null, null);
	}
	
}
