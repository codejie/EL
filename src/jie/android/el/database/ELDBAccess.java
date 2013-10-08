package jie.android.el.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ELDBAccess extends DBAccess {

	public static final String DBFILE = "el.db";

	public static final int SYSINFO_DBVERSION = 1;
	public static final int SYSINFO_LATESTVERSION = 2;
	public static final int SYSINFO_LATESTPACKAGE = 3;

	public ELDBAccess(Context context, String name) {
		super(context, name);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
		case 1:
			db.rawQuery("update esl set flag=0", null);
			break;
		case 2:
			createNewPackagesTable(db);
			break;
		case 3:
			createSysInfoTable(db);
			createScoreTable(db);
			break;
		default:
			;
		}
	}

	private boolean createTables(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS [esl] (" + "[idx] INTEGER PRIMARY KEY," + "[title] TEXT," + "[script] TEXT," + "[audio] TEXT,"
				+ "[duration] INTEGER DEFAULT (0)," + "[slowdialog] INTEGER DEFAULT (-1)," + "[explanations] INTEGER DEFAULT (-1),"
				+ "[fastdialog] INTEGER DEFAULT (-1)," + "[flag] INTEGER DEFAULT (0))";
		db.execSQL(sql);

		createSysInfoTable(db);
		createNewPackagesTable(db);
		createScoreTable(db);

		return true;
	}

	private boolean createNewPackagesTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS [new_packages] (" + "[idx] INTEGER," + "[title] TEXT," + "[desc] TEXT," + "[link] TEXT," + "[size] INTEGER,"
				+ "[updated] TEXT)";

		db.execSQL(sql);

		return true;
	}

	private boolean createSysInfoTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS [sys_info] ([idx] INTEGER,[value] TEXT)";
		db.execSQL(sql);

		ContentValues values = new ContentValues();
		values.put("idx", SYSINFO_DBVERSION);
		values.put("value", DBAccess.VERSION);
		db.insert("sys_info", null, values);

		return true;
	}

	private boolean createScoreTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS [score] (" + "[word] TEXT PRIMARY KEY," + "[lesson_index] INTEGER," + "[checkin_date] INTEGER,"
				+ "[checkin_count] INTEGER," + "[score] INTEGER," + "[level] INTEGER)";
		db.execSQL(sql);

		return true;
	}

}
