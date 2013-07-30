package jie.android.el.service;

import java.util.ArrayList;

import jie.android.el.database.LACDBAccess;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Downloader {
	
	private static final String Tag = Downloader.class.getSimpleName();

	private static final int TYPE_UPDATE	=	0;
	private static final int TYPE_PACKAGE	=	1;

	private static final int STATUS_INIT		=	-1;
	private static final int STATUS_DONE		=	0;
	private static final int STATUS_START		=	1;
	private static final int STATUS_DOWNLOADED	=	2;
	
	private class DownloadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			
			if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				onDownloadCompleted(intent);
			}
		}
		
	}
	
	private ELService service = null;	
	private LACDBAccess dbAccess = null;
	private DownloadManager downloadManager = null;
	private DownloadReceiver receiver = null;
	
	public Downloader(ELService service) {
		this.service = service;
		dbAccess = this.service.getDBAccess();
	}

	public static boolean check(LACDBAccess dbAccess) {
		return dbAccess.checkHasUpdate();
	}
	
	public boolean init() {
		downloadManager = (DownloadManager) service.getSystemService(Context.DOWNLOAD_SERVICE);

		regiestReceiver();
		
		return (downloadManager != null);
	}
	
	public void release() {
		unregistReceiver();
	}	
	
	private boolean loadRequestData() {
		return false;
	}
	
	public boolean addDownloadRequest(final String request) {
		final String file = "update.zip";
		final String url = request2Url(request, file);
		
		if (url == null) {
			return false;
		}
		
		if (!dbAccess.insertUpdateData(request, url, TYPE_UPDATE, -1, STATUS_INIT, 0)) {
			return false;
		}
		
		return checkUpdateData();
	}
	
	private String request2Url(String request, String file) {
		//XXXX-X-XX-XX
		int s = 0;
		int e = request.indexOf("-", s);
		if (e == -1) {
			return null;
		}
		String uc = request.substring(s, e);
		s = e + 1;
		e = request.indexOf("-", s);
		if (e == -1) {
			return null;
		}
		
		String p = request.substring(s, e);
		//
		
		if (p.equals("3")) {
			return "http://www.cppblog.com/Files/codejie/" + uc + "_" + file;
		}
		
		return null;
	}
	
	private boolean checkUpdateData() {
//		public int status = -1; //-1: initial; 0: done; 1: start; 2: update 
//		public int type = -1; //0: update.xml; 1: package.zip
		
		Cursor cursor = dbAccess.getNextUpdateData();
		try {
			if (cursor.moveToFirst()) {
				int status = cursor.getInt(5);
				if (status == STATUS_INIT || status == STATUS_START) {
					return startDownload(cursor.getInt(0), cursor.getString(3));
				} else if (status == STATUS_DOWNLOADED) {
					return processDownload(cursor.getInt(0), cursor.getInt(2), cursor.getLong(4), cursor.getString(3));
				} else {
					
				}
				
			}
		} finally {
			cursor.close();
		}
		
		return false;
	}	

	private boolean startDownload(int idx, final String url) {
		DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
		long syncid = downloadManager.enqueue(req);
		return dbAccess.updateUpdateData(idx, syncid, STATUS_START);
	}

	private boolean processDownload(int idx, int type, long syncid, String file) {
		if (type == TYPE_UPDATE) {
			onUpdateDownloaded(syncid, file);
		} else if (type == TYPE_PACKAGE) {
			onPackageDownloaded(syncid, file);
		}
		
		return false;
	}
	
	private void regiestReceiver() {
		IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		
		receiver = new DownloadReceiver();
		
		service.registerReceiver(receiver , filter);
	}
	
	private void unregistReceiver() {
		if (receiver != null) {
			service.unregisterReceiver(receiver);
		}		
	}

	public void onDownloadCompleted(Intent intent) {
		long syncid = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(syncid);
		Cursor cursor = downloadManager.query(query);
		
		try {
			if (cursor.moveToFirst()) {
				int status =cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)); 
				if (status == DownloadManager.STATUS_SUCCESSFUL) {
					final String file = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));					
					int type = dbAccess.updateUpdateData(syncid, file, STATUS_DOWNLOADED);
					if (type == 0) {
						onUpdateDownloaded(syncid, file);
					} else {
						onPackageDownloaded(syncid, file);
					}
				}
			}
			
		} finally {
			cursor.close();
		}
	}

	private void onUpdateDownloaded(long syncid, String file) {
		Log.d(Tag, "onUpdateDownloaded() syncid:" + syncid + " url:" + file);
		Uri uri = Uri.parse(file);
		Cursor cursor = service.getContentResolver().query(uri, null, null, null, null);
		Log.d(Tag, "count =" + cursor.getCount());
		for (final String str : cursor.getColumnNames()) {
			Log.d(Tag, "col:" + str);
		}
		cursor.moveToFirst();
		Log.d(Tag, "uri = " + cursor.getString(cursor.getColumnIndex("uri")));
		Log.d(Tag, "title = " + cursor.getString(cursor.getColumnIndex("title")));
		Log.d(Tag, "mdeiaprovider_uri = " + cursor.getString(cursor.getColumnIndex("mediaprovider_uri")));
		Log.d(Tag, "entity = " + cursor.getString(cursor.getColumnIndex("entity")));
		Log.d(Tag, "_display_name = " + cursor.getString(cursor.getColumnIndex("_display_name")));
		Log.d(Tag, "destination = " + cursor.getString(cursor.getColumnIndex("destination")));
		Log.d(Tag, "_data = " + cursor.getString(cursor.getColumnIndex("_data")));
		
		
		cursor.close();
	}

	private void onPackageDownloaded(long syncid, String file) {
		service.onPackageReady(syncid, file);		
	}
	
}
