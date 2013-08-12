package jie.android.el.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class LACDBAccess extends DBAccess {

	public static final String DBFILE	=	"lac2.db";
	
	public LACDBAccess(Context context, String name) {
		super(context, name);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
