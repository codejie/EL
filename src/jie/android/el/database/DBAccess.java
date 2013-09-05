package jie.android.el.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DBAccess extends SQLiteOpenHelper {

	private static final int VERSION	=	3;
	
	public DBAccess(Context context, String name) {
		super(context, name, null, VERSION);
	}

}
