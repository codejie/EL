package jie.android.el.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jie.android.el.CommonConsts;
import jie.android.el.CommonConsts.ListItemFlag;
import jie.android.el.utils.AssetsHelper;
import jie.android.el.utils.Utils;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

public class ELContentProvider extends ContentProvider {

	private static final String Tag = ELContentProvider.class.getSimpleName();
	
	private static final String AUTHORITY = "jie.android.el";
	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jie.android.el";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jie.android.el";
	
	public static final Uri URI_EL_ESL = Uri.parse("content://" + AUTHORITY + "/el/esl");
	public static final Uri URI_LAC_WORD_INFO = Uri.parse("content://" + AUTHORITY + "/lac/word_info");
	public static final Uri URI_LAC_SYS_UPDATE = Uri.parse("content://" + AUTHORITY + "/lac/sys_update");
	public static final Uri URI_LAC_DICT_INFO = Uri.parse("content://" + AUTHORITY + "/lac/dict_info");
	public static final Uri URI_LAC_WORD_INDEX_JOIN_INFO = Uri.parse("content://" + AUTHORITY + "/lac/word_index_join_info");
	public static final Uri URI_LAC_BLOCK_INFO = Uri.parse("content://" + AUTHORITY + "/lac/block_info");
	public static final Uri URI_EL_ESL_RANDOM = Uri.parse("content://" + AUTHORITY + "/el/esl_random");
	public static final Uri URI_EL_ESL_NEXT = Uri.parse("content://" + AUTHORITY + "/el/esl_next");
	public static final Uri URI_EL_ESL_PREV = Uri.parse("content://" + AUTHORITY + "/el/esl_prev");
	public static final Uri URI_EL_ESL_FIRST = Uri.parse("content://" + AUTHORITY + "/el/esl_first");
	public static final Uri URI_EL_ESL_LAST = Uri.parse("content://" + AUTHORITY + "/el/esl_last");
	public static final Uri URI_EL_ESL_PLAYFLAG = Uri.parse("content://" + AUTHORITY + "/el/esl_playflag");
	public static final Uri URI_EL_NEW_PACKAGES = Uri.parse("content://" + AUTHORITY + "/el/new_packages");
	public static final Uri URI_EL_SCORE = Uri.parse("content://" + AUTHORITY + "/el/score");
	public static final Uri URI_EL_SCORE_RANDOM = Uri.parse("content://" + AUTHORITY + "/el/score_random");
	public static final Uri URI_EL_SCORE_NEXT = Uri.parse("content://" + AUTHORITY + "/el/score_next");
	public static final Uri URI_EL_SYS_INFO = Uri.parse("content://" + AUTHORITY + "/el/sys_info");
	
	
	private static final int MATCH_EL_ESL = 10;
	private static final int MATCH_ITEM_EL_ESL = 11;
	private static final int MATCH_ITEM_EL_ESL_RANDOM = 12;
	private static final int MATCH_ITEM_EL_ESL_NEXT = 13;
	private static final int MATCH_ITEM_EL_ESL_PREV = 14;
	private static final int MATCH_ITEM_EL_ESL_FIRST = 15;
	private static final int MATCH_ITEM_EL_ESL_LAST = 16;
	private static final int MATCH_ITEM_EL_ESL_PLAYFLAG = 17;
	private static final int MATCH_LAC_WORD_INFO = 20;
	private static final int MATCH_ITEM_LAC_WORD_INFO = 21;
	private static final int MATCH_LAC_SYS_UPDATE = 30;
	private static final int MATCH_LAC_DICT_INFO = 40;
	private static final int MATCH_LAC_WORD_INDEX_JOIN_INFO = 50;
	private static final int MATCH_LAC_BLOCK_INFO = 60;
	private static final int MATCH_EL_NEW_PACKAGES = 70;
	private static final int MATCH_EL_SCORE = 80;
	private static final int MATCH_ITEM_EL_SCORE = 81;
	private static final int MATCH_ITEM_EL_SCORE_RANDOM = 82;
	private static final int MATCH_ITEM_EL_SCORE_NEXT = 83;
	private static final int MATCH_EL_SYS_INFO = 90;
	private static final int MATCH_ITEM_EL_SYS_INFO = 91;
	

	private UriMatcher matcher = null;
	private LACDBAccess lacDBAccess = null;//should be a subclass of SQLiteOpenHelper 
	private ELDBAccess elDBAccess = null;
	
	private SQLiteDatabase db = null;
	
	@Override
	public boolean onCreate() {
		
		initDatabases();
		initMatcher();
		
		return true;
	}

	@Override
	public String getType(Uri uri) {
		int res = matcher.match(uri);
		switch (res) {
		case MATCH_EL_ESL:
		case MATCH_LAC_WORD_INFO:
		case MATCH_LAC_SYS_UPDATE:
		case MATCH_LAC_DICT_INFO:
		case MATCH_LAC_WORD_INDEX_JOIN_INFO:
		case MATCH_LAC_BLOCK_INFO:
		case MATCH_ITEM_EL_ESL_RANDOM:
		case MATCH_ITEM_EL_ESL_FIRST:
		case MATCH_ITEM_EL_ESL_LAST:
		case MATCH_EL_NEW_PACKAGES:
		case MATCH_EL_SCORE:
		case MATCH_ITEM_EL_SCORE_RANDOM:
		case MATCH_ITEM_EL_SCORE_NEXT:
		case MATCH_EL_SYS_INFO:			
			return CONTENT_TYPE;
		case MATCH_ITEM_EL_ESL:
		case MATCH_ITEM_LAC_WORD_INFO:
		case MATCH_ITEM_EL_ESL_NEXT:
		case MATCH_ITEM_EL_ESL_PREV:
		case MATCH_ITEM_EL_ESL_PLAYFLAG:
		case MATCH_ITEM_EL_SCORE:
		case MATCH_ITEM_EL_SYS_INFO:
			return CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri); 
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int res = matcher.match(uri);
		
		String table = null;
		
		switch (res) {
		case MATCH_LAC_SYS_UPDATE:
			db = lacDBAccess.getWritableDatabase();
			table = "sys_update";
			break;
		case MATCH_EL_ESL:
			db = elDBAccess.getWritableDatabase();
			table = "esl";
			break;
		case MATCH_EL_NEW_PACKAGES:
			db = elDBAccess.getWritableDatabase();
			table = "new_packages";
			break;
		case MATCH_EL_SCORE:
			db = elDBAccess.getWritableDatabase();
			table = "score";
			break;
		case MATCH_EL_SYS_INFO:
			db = elDBAccess.getWritableDatabase();
			table = "sys_info";			
			break;
		default:
			throw new IllegalArgumentException("insert() Unknown uri: " + uri);
		}
		
		long rowid = db.insert(table, null, values);
		if (rowid != -1) {			
			getContext().getContentResolver().notifyChange(uri, null);			
			return ContentUris.withAppendedId(uri, rowid);
		} else {
			return null;
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int res = matcher.match(uri);
		String table = null;
		switch (res) {
		case MATCH_EL_ESL:
		case MATCH_ITEM_EL_ESL:
			db = elDBAccess.getReadableDatabase();
			table = "esl";
			
			if (res == MATCH_ITEM_EL_ESL) {
	            selection = "idx=?";  
	            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};  				
			}
			break;
		case MATCH_ITEM_EL_ESL_RANDOM:
			db = elDBAccess.getReadableDatabase();
			table = "esl";
			sortOrder = "random() limit 1";
			break;
		case MATCH_ITEM_EL_ESL_NEXT:
		case MATCH_ITEM_EL_ESL_PREV:
		case MATCH_ITEM_EL_ESL_FIRST:
		case MATCH_ITEM_EL_ESL_LAST:			
			if (res == MATCH_ITEM_EL_ESL_NEXT) {
				selection = "idx>" + ContentUris.parseId(uri);
				sortOrder = "idx asc";
			} else if (res == MATCH_ITEM_EL_ESL_PREV) {
				selection = "idx<" + ContentUris.parseId(uri);
				sortOrder = "idx desc";				
			} else if (res == MATCH_ITEM_EL_ESL_FIRST) {
				selection = null;
				sortOrder = "idx asc";
			} else {
				selection = null;
				sortOrder = "idx desc";
			}
			
			db = elDBAccess.getReadableDatabase();
			
			return db.query("esl", projection, selection, null, null, null, sortOrder, "1");			
		case MATCH_LAC_WORD_INFO:
		case MATCH_ITEM_LAC_WORD_INFO:
			db = lacDBAccess.getReadableDatabase();
			table = "word_info";
			
			if (res == MATCH_ITEM_LAC_WORD_INFO) {
	            selection = "idx=?";  
	            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};  				
			}			
			break;
		case MATCH_LAC_SYS_UPDATE:
			db = lacDBAccess.getReadableDatabase();
			table = "sys_update";
			break;
		case MATCH_LAC_DICT_INFO:
			db = lacDBAccess.getReadableDatabase();
			table = "dict_info";
			break;
		case MATCH_LAC_WORD_INDEX_JOIN_INFO:
			db = lacDBAccess.getReadableDatabase();
			table = "word_index_100 inner join word_info on (word_index_100.word_idx=word_info.idx)";
			break;
		case MATCH_LAC_BLOCK_INFO:
			db = lacDBAccess.getReadableDatabase();
			table = "block_info_100";
			break;
		case MATCH_EL_NEW_PACKAGES:
			db = elDBAccess.getReadableDatabase();
			table = "new_packages";
			break;
		case MATCH_ITEM_EL_SYS_INFO:
			db = elDBAccess.getReadableDatabase();
			table = "sys_info";
			selection = "idx=" + ContentUris.parseId(uri);
			break;
		default:			
			throw new IllegalArgumentException("query() Unknown uri: " + uri); 			
		}
		
		return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int res = matcher.match(uri);
		String table = null;
		switch (res) {
		case MATCH_LAC_SYS_UPDATE:
			db = lacDBAccess.getWritableDatabase();
			table = "sys_update";
			break;
		case MATCH_EL_ESL:
			db = elDBAccess.getWritableDatabase();
			table = "esl";
			break;
		case MATCH_ITEM_EL_ESL_PLAYFLAG:
			db = elDBAccess.getWritableDatabase();
			db.execSQL("UPDATE esl SET flag = flag & ~" + ListItemFlag.LAST_PLAY + " where idx!=" + ContentUris.parseId(uri));
			db.execSQL("UPDATE esl SET flag = flag | " + ListItemFlag.LAST_PLAY + " where idx=" + ContentUris.parseId(uri));
			
			getContext().getContentResolver().notifyChange(URI_EL_ESL, null);
			
			return 1;
		default:
			throw new IllegalArgumentException("update() Unknown uri: " + uri);
		}
		int ret = db.update(table, values, selection, selectionArgs);
		if (ret > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return ret;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int res = matcher.match(uri);
		String table = null;
		switch (res) {
		case MATCH_LAC_SYS_UPDATE:
			db = lacDBAccess.getWritableDatabase();
			table = "sys_update";
			break;
		case MATCH_EL_NEW_PACKAGES:
			db = elDBAccess.getWritableDatabase();
			table = "new_packages";
			break;
		case MATCH_ITEM_EL_SYS_INFO:
			db = elDBAccess.getWritableDatabase();
			table = "sys_info";
			selection = "idx=" + ContentUris.parseId(uri);
			selectionArgs = null;
			break;
		default:
			throw new IllegalArgumentException("delete() Unknown uri: " + uri);
		}
		
		int ret = db.delete(table, selection, selectionArgs);
		if (ret > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return ret;
	}
	
	private void initMatcher() {
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		matcher.addURI(AUTHORITY, "el/esl", MATCH_EL_ESL);
		matcher.addURI(AUTHORITY, "el/esl/#", MATCH_ITEM_EL_ESL);
		matcher.addURI(AUTHORITY, "lac/word_info", MATCH_LAC_WORD_INFO);
		matcher.addURI(AUTHORITY, "lac/word_info/#", MATCH_ITEM_LAC_WORD_INFO);
		matcher.addURI(AUTHORITY, "lac/sys_update", MATCH_LAC_SYS_UPDATE);
		matcher.addURI(AUTHORITY, "lac/dict_info", MATCH_LAC_DICT_INFO);
		matcher.addURI(AUTHORITY, "lac/word_index_join_info", MATCH_LAC_WORD_INDEX_JOIN_INFO);
		matcher.addURI(AUTHORITY, "lac/block_info", MATCH_LAC_BLOCK_INFO);
		matcher.addURI(AUTHORITY, "el/esl_random", MATCH_ITEM_EL_ESL_RANDOM);
		matcher.addURI(AUTHORITY, "el/esl_next/#", MATCH_ITEM_EL_ESL_NEXT);
		matcher.addURI(AUTHORITY, "el/esl_prev/#", MATCH_ITEM_EL_ESL_PREV);
		matcher.addURI(AUTHORITY, "el/esl_first", MATCH_ITEM_EL_ESL_FIRST);
		matcher.addURI(AUTHORITY, "el/esl_last", MATCH_ITEM_EL_ESL_LAST);
		matcher.addURI(AUTHORITY, "el/esl_playflag/#", MATCH_ITEM_EL_ESL_PLAYFLAG);
		matcher.addURI(AUTHORITY, "el/new_packages", MATCH_EL_NEW_PACKAGES);
		matcher.addURI(AUTHORITY, "el/score", MATCH_EL_SCORE);
		matcher.addURI(AUTHORITY, "el/score/#", MATCH_ITEM_EL_SCORE);
		matcher.addURI(AUTHORITY, "el/score_random/#", MATCH_ITEM_EL_SCORE_RANDOM);
		matcher.addURI(AUTHORITY, "el/score_next/#", MATCH_ITEM_EL_SCORE_NEXT);
		matcher.addURI(AUTHORITY, "el/sys_info", MATCH_EL_SYS_INFO);
		matcher.addURI(AUTHORITY, "el/sys_info/#", MATCH_ITEM_EL_SYS_INFO);
	}
	
	private void initDatabases() {
		
		checkLACDatabase();
		
		String db = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_EL + ELDBAccess.DBFILE;
		elDBAccess = new ELDBAccess(this.getContext(), db);

		db = getContext().getDatabasePath(LACDBAccess.DBFILE).getAbsolutePath();
		lacDBAccess = new LACDBAccess(this.getContext(), db);
	}

	private void checkLACDatabase() {

		File dbfile = getContext().getDatabasePath(LACDBAccess.DBFILE);
		if (!dbfile.exists()) {
			File parent = dbfile.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			
			InputStream input;
			try {
				input = getContext().getAssets().open("lac2.zip");
				AssetsHelper.UnzipTo(input, parent.getAbsolutePath(), null);
			} catch (IOException e) {
				e.printStackTrace();			
			}			
		}
	}
}
