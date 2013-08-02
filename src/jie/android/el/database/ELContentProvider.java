package jie.android.el.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jie.android.el.utils.AssetsHelper;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class ELContentProvider extends ContentProvider {

	private static final String Tag = ELContentProvider.class.getSimpleName();
	
	private static final String AUTHORITY = "jie.android.el";
	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jie.android.el";
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jie.android.el";
	
	public static final Uri URI_EL_ESL = Uri.parse("content://" + AUTHORITY + "/el/esl");
	public static final Uri URI_LAC_WORD_INFO = Uri.parse("content://" + AUTHORITY + "/lac/word_info");
	
	private static final int MATCH_EL_ESL = 10;
	private static final int MATCH_ITEM_EL_ESL = 11;
	private static final int MATCH_LAC_WORD_INFO = 20;
	private static final int MATCH_ITEM_LAC_WORD_INFO = 21;
	

	private UriMatcher matcher = null;
	private LACDBAccess lacDBAccess = null;//should be a subclass of SQLiteOpenHelper 
	private ELDBAccess elDBAccess = null;
	
	private SQLiteDatabase db = null;
	private boolean isDBReady = false;

	
	@Override
	public boolean onCreate() {
		
		initDatabases();
		initMatcher();
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getType(Uri uri) {
		int res = matcher.match(uri);
		switch (res) {
		case MATCH_EL_ESL:
		case MATCH_LAC_WORD_INFO:
			return CONTENT_TYPE;
		case MATCH_ITEM_EL_ESL:
		case MATCH_ITEM_LAC_WORD_INFO:
			return CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri); 
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(Tag, "ContentProvider insert()");
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int res = matcher.match(uri);
		switch (res) {
		case MATCH_EL_ESL:
		case MATCH_ITEM_EL_ESL:
			db = elDBAccess.getReadableDatabase();
			if (res == MATCH_ITEM_EL_ESL) {
	            selection = "idx=?";  
	            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};  				
			}
			return db.query("esl", projection, selection, selectionArgs, null, null, sortOrder); 
		case MATCH_LAC_WORD_INFO:
		case MATCH_ITEM_LAC_WORD_INFO:
			db = lacDBAccess.getReadableDatabase();
			if (res == MATCH_ITEM_LAC_WORD_INFO) {
	            selection = "idx=?";  
	            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};  				
			}			
			return db.query("word_info", projection, selection, selectionArgs, null, null, sortOrder);
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri); 			
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void initMatcher() {
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		matcher.addURI(AUTHORITY, "el/esl", MATCH_EL_ESL);
		matcher.addURI(AUTHORITY, "el/esl/#", MATCH_ITEM_EL_ESL);
		matcher.addURI(AUTHORITY, "lac/word_info", MATCH_LAC_WORD_INFO);
		matcher.addURI(AUTHORITY, "lac/word_info/#", MATCH_ITEM_LAC_WORD_INFO);
	}
	
	public boolean isDBReady() {
		return isDBReady;
	}
	
	private void initDatabases() {
		
		checkLACDatabase();
		
		String db = Environment.getExternalStorageDirectory() + ELDBAccess.DBFILE;
		elDBAccess = new ELDBAccess(this.getContext(), db);

		db = getContext().getDatabasePath(LACDBAccess.DBFILE).getAbsolutePath();
		lacDBAccess = new LACDBAccess(this.getContext(), db);
		
		isDBReady = true;
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
