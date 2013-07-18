package jie.android.el.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;

public class LACDBAccess extends DBAccess {

	private static int VERSION			=	1;
	public static final String FILE	=	"lac2.db";
	
	private static final class Projection {
		public static final String[] WordIndex = new String[] { "idx" };
		public static final String[] DictionaryInfo = new String[] {"idx", "title", "file", "offset", "d_decoder", "x_decoder"};
		public static final String[] DictionaryBlock = new String[] {"offset", "length", "start", "end"};
		public static final String[] WordXmlIndex = new String[] { "offset", "length", "block1" };
	}
	
	private Context context = null;
	
	public LACDBAccess(Context context) {
		super(VERSION);
		
		this.context = context;
	}

	@Override
	public boolean open() {
		if (!checkDataFile()) {
			return false;
		}
		
		File f = context.getDatabasePath(FILE);
		return openDb(f, true);
	}
	
	private boolean checkDataFile() {
		if (!context.getDatabasePath(FILE).exists()) {
			return unzipDataFile();
		}
		return true;
	}

	private boolean unzipDataFile() {
		
		File target = context.getDatabasePath(FILE).getParentFile();		

		if (!target.exists()) {
			target.mkdirs();
		}
		
		InputStream input;
		try {
			input = context.getAssets().open("lac2.zip");
			AssetsHelper.UnzipTo(input, target.getAbsolutePath(), null);
		} catch (IOException e) {
			e.printStackTrace();			
			return false;
		}
		
		return true;
	}	
	
	public Cursor getWord(String condition, int offset, int limit) {
		condition = "word LIKE '" + condition + "%'";
		String sql = "SELECT idx, word, flag FROM word_info";
		if(condition != null) {
			sql += " WHERE " + condition;
		}
		sql += " LIMIT " + limit + " OFFSET " + offset ;
		
		return db.rawQuery(sql, null);
	}

	public int getWordIndex(String word) {
		Cursor cursor = db.query("word_info", Projection.WordIndex, "word=?", new String[] { word }, null, null, null);
		if (cursor == null) {
			return -1;
		}
		
		try {
			if (cursor.getCount() == 1 && cursor.moveToFirst()) {
				return cursor.getInt(0);
			} else {
				return -1;
			}
		} finally {
			cursor.close();
		}
	}
	
	public Cursor queryDictionaryInfo() {
		return db.query("dict_info", Projection.DictionaryInfo, null, null, null, null, null);
	}	
	
	public Cursor queryBlockData(int dictIndex) {
		return db.query("block_info_" + dictIndex, Projection.DictionaryBlock, null, null, null, null, null);
	}

	public Cursor queryWordXmlIndex(int dictIndex, int wordIndex) {
		return db.query("word_index_" + dictIndex, Projection.WordXmlIndex, "word_idx=" + wordIndex, null, null,null, null);
	}
	
}
