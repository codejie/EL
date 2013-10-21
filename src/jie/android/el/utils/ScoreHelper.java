package jie.android.el.utils;

import jie.android.el.database.ELContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public final class ScoreHelper {

	private static final int DEFAULT_SCORE 	= 	14;
	private static final int DEFAULT_LEVEL	=	3;
	
	private static final int CONSTANT_MSEC_PER_DAY	= (1000 * 60 * 60 * 24);	
	
	public static boolean insertWord(Context context, String word, int lesson) {
		
		if (!isWordExist(context, word)) {			
			ContentValues values = new ContentValues();
			
			values.put("word", word);
			values.put("lesson_index", lesson);
			values.put("checkin_date", (int)(System.currentTimeMillis() / CONSTANT_MSEC_PER_DAY));
			values.put("checkin_count", 1);
			values.put("score", DEFAULT_SCORE);
			values.put("level", DEFAULT_LEVEL);
			
			context.getContentResolver().insert(ELContentProvider.URI_EL_SCORE, values);			
		} else {
			context.getContentResolver().update(ELContentProvider.URI_EL_SCORE_UPDATE_CHECKIN, null, "word='" + word + "'", null);
		}
		
		return true;
	}
	
	public static boolean isWordExist(Context context, String word) {
		Cursor  cursor = context.getContentResolver().query(ELContentProvider.URI_EL_SCORE, new String[] { "count(*)" }, "word=?", new String[] { word }, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0) > 0;
			}
		} finally {
			cursor.close();
		}
		return false;
	}
}
