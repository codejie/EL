package jie.android.el.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jie.android.el.utils.AssetsHelper;

import android.content.ContentValues;
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
		public static final String[] UpdateData = new String[] { "idx", "request", "type", "url", "syncid", "status", "updatetime" };
	}
	
	private Context context = null;
	
	public LACDBAccess(Context context) {
		super(VERSION);
		
		this.context = context;
	}

	@Override
	public boolean open() {
//		if (!checkDataFile()) {
//			return false;
//		}
		
		File f = context.getDatabasePath(FILE);
		return openDb(f, true);
	}
	
//	private boolean checkDataFile() {
//		if (!context.getDatabasePath(FILE).exists()) {
//			return unzipDataFile();
//		}
//		return true;
//	}
//
//	private boolean unzipDataFile() {
//		
//		File target = context.getDatabasePath(FILE).getParentFile();		
//
//		if (!target.exists()) {
//			target.mkdirs();
//		}
//		
//		InputStream input;
//		try {
//			input = context.getAssets().open("lac2.zip");
//			AssetsHelper.UnzipTo(input, target.getAbsolutePath(), null);
//		} catch (IOException e) {
//			e.printStackTrace();			
//			return false;
//		}
//		
//		return true;
//	}	
	
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

	public boolean checkHasUpdate() {
		Cursor cursor = db.query("sys_update", new String[] { "count(*)" }, "status != 0", null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			return (cursor.getInt(0) > 0);
		}
		return false;
	}

	public boolean insertUpdateData(String request, String url, int type, int syncid, int status, int update) {
		
		ContentValues values = new ContentValues();
		values.put("request", request);
		values.put("type", type);
		values.put("url", url);
		values.put("syncid", syncid);
		values.put("status", status);
		values.put("updatetime", update);
		
		return (db.insert("sys_update", null, values) != -1);
	}

	public int updateUpdateData(long syncid, String url, int status) {
		Cursor cursor = db.query("sys_update", new String[] { "type" }, "syncid=" + syncid, null, null, null, null);
		int type = -1;
		try {
			if (cursor.moveToFirst()) {
				type = cursor.getInt(0);
				
				ContentValues values = new ContentValues();
				values.put("url", url);
				values.put("status", status);
				db.update("sys_update", values, "syncid=" + syncid, null);
			}
		} finally {
			cursor.close();
		}
		
		return type;
	}

	public Cursor getNextUpdateData() {
		return  db.query("sys_update", Projection.UpdateData, "status != 0", null, null, null, "idx");
	}

	public boolean updateUpdateData(int idx, long syncid, int status) {
		ContentValues values = new ContentValues();
		values.put("syncid", syncid);
		values.put("status", status);
		return (db.update("sys_update", values, "idx=" + idx, null) > 0);
	}
	
}
