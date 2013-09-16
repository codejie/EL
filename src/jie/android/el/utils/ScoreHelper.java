package jie.android.el.utils;

import jie.android.el.database.ELContentProvider;
import android.content.ContentValues;
import android.content.Context;

public final class ScoreHelper {

	private static final int DEFAULT_SCORE 	= 	7;
	private static final int DEFAULT_LEVEL	=	3;

	public static boolean insertWord(Context context, String word, int lesson) {
		
		if (isWordExist(word)) {
			return false;
		}
		
		ContentValues values = new ContentValues();
		
		values.put("word", word);
		values.put("lesson_index", lesson);
		values.put("checkin_date", System.currentTimeMillis());
		values.put("checkin_count", 0);
		values.put("score", DEFAULT_SCORE);
		values.put("level", DEFAULT_LEVEL);
		
		context.getContentResolver().insert(ELContentProvider.URI_EL_SCORE, values);
		
		return true;
	}
	
}
