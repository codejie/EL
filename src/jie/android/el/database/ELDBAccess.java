package jie.android.el.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ELDBAccess extends DBAccess {
	
	public static final String DBFILE	=	"/jie/el/el.db";

	public ELDBAccess(Context context, String name) {
		super(context, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	private boolean createTables(SQLiteDatabase db) {
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
}
