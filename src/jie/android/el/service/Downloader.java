package jie.android.el.service;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.Utils;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
	private DownloadManager downloadManager = null;
	private DownloadReceiver receiver = null;
	
	private String outputCachePath = null;
	
	public Downloader(ELService service) {
		this.service = service;
	}

	public static boolean check(Context context) {
		Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_LAC_SYS_UPDATE, new String[] { "count(*)" }, "status != 0", null, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0) > 0;
			}
		} finally {
			cursor.close();
		}
		return false;
	}
	
	public boolean init() {
		outputCachePath = Environment.getExternalStorageDirectory() + "/jie/cache";
		File f = new File(outputCachePath);
		if (!f.exists()) {
			f.mkdirs();
		}
		
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
		
		insertUpdateData(request, TYPE_UPDATE, url, file, -1, STATUS_INIT);
		
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
		
		Cursor cursor = service.getContentResolver().query(ELContentProvider.URI_LAC_SYS_UPDATE, new String[] { "idx", "type", "url", "local", "syncid", "status" }, "status != 0", null, "idx");
		try {
			if (cursor.moveToFirst()) {
				int status = cursor.getInt(5);
				if (status == STATUS_INIT || status == STATUS_START) {
					return startDownload(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3));
				} else if (status == STATUS_DOWNLOADED) {
					return processDownload(cursor.getInt(0), cursor.getInt(1), cursor.getLong(4), cursor.getString(2));
				} else {
					//?
				}
				
			}
		} finally {
			cursor.close();
		}
		
		Log.d(Tag, "all update process done.");
		
		return false;
	}	

	private boolean startDownload(int idx, int type, final String url, String local) {
		DownloadManager.Request req = null;
		if (type == TYPE_UPDATE) {
			req = new DownloadManager.Request(Uri.parse(url));
			req.setTitle("EL");
			req.setDescription("downloading update script..");
		} else {
			//packages
			req = new DownloadManager.Request(Uri.parse(url));
			req.setTitle("EL");
			req.setDescription("downloading package..");
			Uri dest = Uri.parse("file://" + outputCachePath + File.separator + local);
			req.setDestinationUri(dest);
			req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		}
		
		long syncid = downloadManager.enqueue(req);
		
		updateStatusByIdx(idx, syncid, STATUS_START);
//		ContentValues values = new ContentValues();
//		values.put("syncid", syncid);
//		values.put("status", STATUS_START);
//		
//		service.getContentResolver().update(ELContentProvider.URI_LAC_SYS_UPDATE, values, "idx=" + idx, null);
		
		return true;
	}

	private boolean processDownload(int idx, int type, long syncid, String url) {
		if (type == TYPE_UPDATE) {
			onUpdateDownloaded(syncid, url);
		} else if (type == TYPE_PACKAGE) {
			onPackageDownloaded(syncid, url);
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
					updateStatusBySyncId(syncid, file, STATUS_DOWNLOADED);
					int type = queryTypeBySyncId(syncid);
					if (type == TYPE_UPDATE) {
						onUpdateDownloaded(syncid, file);
					} else if (type == TYPE_PACKAGE) {
						onPackageDownloaded(syncid, file);
					}
				}
			}
			
		} finally {
			cursor.close();
		}
	}

	private void insertUpdateData(final String request, int type, final String url, final String local, long syncid, int status) {
		ContentValues values = new ContentValues();
		values.put("request", request);
		values.put("type", type);
		values.put("url", url);
		values.put("local", local);
		values.put("syncid", syncid);
		values.put("status", status);
		values.put("updatetime", System.currentTimeMillis());
		
		service.getContentResolver().insert(ELContentProvider.URI_LAC_SYS_UPDATE, values);		
	}
	
	private void updateStatusByIdx(int idx, long syncid, int status) {
	
		ContentValues values = new ContentValues();
		values.put("syncid", syncid);
		values.put("status", status);
		
		service.getContentResolver().update(ELContentProvider.URI_LAC_SYS_UPDATE, values, "idx=" + idx, null);		
	}
	
	private void updateStatusBySyncId(long syncid, final String url, int status) {
		
		ContentValues values = new ContentValues();
		if (url != null) {
			values.put("url", url);
		}
		values.put("status", status);
		values.put("updatetime", System.currentTimeMillis());
		
		service.getContentResolver().update(ELContentProvider.URI_LAC_SYS_UPDATE, values, "syncid=" + syncid, null);
	}
	
	private int queryTypeBySyncId(long syncid) {
		
		Cursor cursor = service.getContentResolver().query(ELContentProvider.URI_LAC_SYS_UPDATE, new String[] { "type" }, "syncid=" + syncid, null, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
		} finally {
			cursor.close();
		}
		return -1;
	}

	private void onUpdateDownloaded(long syncid, String url) {
		Log.d(Tag, "onUpdateDownloaded() syncid:" + syncid + " url:" + url);
		Uri uri = Uri.parse(url);
		Cursor cursor = service.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					final String file = unzipUpdate(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
					if (analyseUpdated(file)) {
						Utils.removeFile(file);
						updateStatusBySyncId(syncid, null, STATUS_DONE);
						
						this.checkUpdateData();
					}
				}
			} finally {
				cursor.close();
			}			
		}		
	}

	private String unzipUpdate(String file) {
		String[] ret = Utils.unzipFile(file, outputCachePath);
		if (ret.length > 0) {
			return outputCachePath + File.separator + ret[0];
		}
				
		return null;
	}

	private boolean analyseUpdated(String file) {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			Document doc = factory.newDocumentBuilder().parse("file://" + file);
			
			NodeList nl = doc.getElementsByTagName("check_code");
			if (nl == null) {
				return false;
			}
			String checkcode = nl.item(0).getChildNodes().item(0).getNodeValue();
			Log.d(Tag, "checkcode = " + checkcode);
			NodeList pk = doc.getElementsByTagName("package");
			if (pk == null) {
				return false;
			}
			for (int i = 0; i < pk.getLength(); ++ i) {
				Element p = (Element) pk.item(i);
				
				NodeList f = p.getElementsByTagName("file");
				Log.d(Tag, "file = " + f.item(0).getFirstChild().getNodeValue());
				NodeList u =  p.getElementsByTagName("url");
				Log.d(Tag, "url = " + u.item(0).getFirstChild().getNodeValue());
				
				insertUpdateData(checkcode, TYPE_PACKAGE, u.item(0).getFirstChild().getNodeValue(), f.item(0).getFirstChild().getNodeValue(), -1, STATUS_INIT);
			}

			return true;
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void onPackageDownloaded(long syncid, String url) {
		
		String file = url.substring("file://".length());
		
		final String dbfile = unzipPackage(file);
		
		// TODO: donot forget open the below line.
		//Utils.removeFile(file); 		
		
		updateStatusBySyncId(syncid, null, STATUS_DONE);
		
		service.onPackageReady(syncid, dbfile);
		
		this.checkUpdateData();
		
		
//		Uri uri = Uri.parse(url);
//		Cursor cursor = service.getContentResolver().query(uri, null, null, null, null);
//		if (cursor != null) {
//			try {
//				if (cursor.moveToFirst()) {
//					final String dbfile = unzipPackage(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
//					service.onPackageReady(syncid, dbfile);
//					
//					this.checkUpdateData();
//				}
//			} finally {
//				cursor.close();
//			}			
//		}	
	}

	private String unzipPackage(String file) {

		String output = Environment.getExternalStorageDirectory() + "/jie/el";
		
		String[] ret = Utils.unzipFile(file, output);
				
		if (ret.length > 0) {
			for (String f : ret) {
				if (f.endsWith(".db")) {
					return (output + File.separator + f);
				}
			}
		}
		
		return null;
	}
//
//	public void onPackageImported(int syncid, String dbfile) {
//		Utils.removeFile(dbfile);		
//		dbAccess.updateUpdateData(syncid, STATUS_DONE);
//		
//		this.checkUpdateData();
//	}
	
}
