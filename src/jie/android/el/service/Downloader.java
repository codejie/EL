package jie.android.el.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jie.android.el.CommonConsts;
import jie.android.el.R;
import jie.android.el.CommonConsts.BroadcastAction;
import jie.android.el.CommonConsts.DownloadCompletedType;
import jie.android.el.CommonConsts.DownloadRequest;
import jie.android.el.CommonConsts.NotificationAction;
import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.database.ELContentProvider;
import jie.android.el.database.ELDBAccess;
import jie.android.el.utils.Utils;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Downloader {

	private static final String Tag = Downloader.class.getSimpleName();

	private static final int TYPE_UPDATE = 0;
	private static final int TYPE_PACKAGE = 1;
	private static final int TYPE_CHECKNEW = 2;
	private static final int TYPE_LATESTVERSION = 3;

	private static final int STATUS_INIT = -1;
	private static final int STATUS_DONE = 0;
	private static final int STATUS_START = 1;
	private static final int STATUS_DOWNLOADED = 2;

	private class DownloadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				onDownloadCompleted(intent);
			}
		}
	}

	private Context context = null;
	private DownloadManager downloadManager = null;
	private DownloadReceiver receiver = null;

	private String outputCachePath = null;

	public Downloader(Context context) {
		this.context = context;
	}

	public static boolean checkDownloaded(Context context) {
		// check tmp files
		String path = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_CACHE;
		File p = new File(path);

		String[] files = p.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String file) {
				return file.startsWith("package") && file.endsWith(".zip.tmp");
			}
		});

		boolean find = false;
		for (final String file : files) {
			p = new File(path + file);
			if (p != null) {
				String local = file.substring(0, file.indexOf(".tmp"));
				Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_LAC_SYS_UPDATE, new String[] { "length" }, "status=0 and local=?",
						new String[] { local }, null);
				if (cursor != null) {
					try {
						if (cursor.moveToFirst()) {
							// if (p.length() == cursor.getLong(0)) {
							File dest = new File(path + local);
							if (p.renameTo(dest)) {
								find = true;
							} else {
								Log.e(Tag, "rename failed - from '" + file + "' to '" + local + "'.");
							}
							// }
						} else {
							Utils.removeFile(path + file);
						}
					} finally {
						cursor.close();
					}
				}
			}
		}
		return find;
	}

	public static boolean checkIncomplete(Context context) {

		checkDownloaded(context);

		// check incomplete download
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
		outputCachePath = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_CACHE;// Environment.getExternalStorageDirectory()
																									// +
																									// "/jie/cache";

		downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

		regiestReceiver();

		if (downloadManager != null) {
			checkUpdateData();
			return true;
		} else {
			return false;
		}
	}

	public void release() {
		unregistReceiver();
	}

	private boolean loadRequestData() {
		return false;
	}

	public boolean addDownloadRequest(final String request, final String check) {

		if (request.indexOf("-") != -1) {
			if (!request2Url_OldMode(request, check)) {
				return false;
			}
		} else if (request.length() == 12) {
			if (!request2Url_NewMode(request, check)) {
				return false;
			}
		} else if (request.indexOf(DownloadRequest.SKYDRIVE_AUTHORIZATION_KEY) != -1) {
			if (!request2Url_SkyDrive(request, check)) {
				return false;
			}
		} else if (request.indexOf(DownloadRequest.CHECK_NEW_PACKAGES) != -1) {
			if (!request2Url_CheckNewPackages(request, check)) {
				return false;
			}
		} else if (request.indexOf(DownloadRequest.DOWNLOAD_LATEST_VERSION) != -1) {
			if (!request2Url_DownloadLatestVersion(request, check)) {
				return false;
			}
		} else {
			return false;
		}

		return checkUpdateData();
	}

	private boolean request2Url_OldMode(String request, String check) {
		// XXXX-X-XX-XX
		int s = 0;
		int e = request.indexOf("-", s);
		if (e == -1) {
			return false;
		}
		String uc = request.substring(s, e);
		s = e + 1;
		e = request.indexOf("-", s);
		if (e == -1) {
			return false;
		}

		int p = Integer.valueOf(request.substring(s, e)).intValue();
		if (p != 3)
			return false;

		final String file = "update.zip";
		final String url = "http://www.cppblog.com/Files/codejie/" + uc + "_" + file;

		insertUpdateData(request, TYPE_UPDATE, url, file, -1, -1, STATUS_INIT);

		return true;
	}

	private boolean request2Url_NewMode(String request, String check) {
		// NNNNNNXMRRCC
		String seq = request.substring(0, 6);
		if (seq == null)
			return false;

		int n6 = Integer.valueOf(request.substring(5, 6)).intValue();

		int x = Integer.valueOf(request.substring(6, 7)).intValue();
		int m = Integer.valueOf(request.substring(7, 8)).intValue();
		int r = Integer.valueOf(request.substring(8, 10)).intValue();
		int c1 = Integer.valueOf(request.substring(10, 11)).intValue();
		int c2 = Integer.valueOf(request.substring(11, 12)).intValue();

		if (((n6 + r) % 7) != c1) // check 1
			return false;

		if (((x + m) % 7) != c2)
			return false;

		if (x != 3) {
			return false; //
		}

		final String file = (m < 5 ? "update.zip" : "script.zip");
		final String url = "http://www.cppblog.com/Files/codejie/" + request + "_" + file;

		insertUpdateData(request, TYPE_UPDATE, url, file, -1, -1, STATUS_INIT);

		return true;
	}

	private boolean request2Url_SkyDrive(String request, String check) {

		// http://skydrive.live.com/download?resid=3BAF6CAC74F7919!120&amp;authkey=!AFB9Zq4LrXKR200

		final String file = "update.zip";
		final String url = "http://skydrive.live.com/download?resid=" + request + "&authkey=" + check;

		insertUpdateData(request, TYPE_UPDATE, url, file, -1, -1, STATUS_INIT);

		return true;
	}

	private boolean request2Url_CheckNewPackages(String request, String check) {
		final String file = "new_packages_update.zip";
		final String url = "http://www.cppblog.com/Files/codejie/" + file;

		insertUpdateData(request, TYPE_CHECKNEW, url, file, -1, -1, STATUS_INIT);
		return true;
	}

	private boolean request2Url_DownloadLatestVersion(String request, String check) {
		final String file = "el.apk";
		final String url = check;		
		insertUpdateData("latest_version", TYPE_LATESTVERSION, url, file, -1, -1, STATUS_INIT);
		
		return true;
	}
	
	private boolean checkUpdateData() {
		// public int status = -1; //-1: initial; 0: done; 1: start; 2: update
		// public int type = -1; //0: update.xml; 1: package.zip

		Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_LAC_SYS_UPDATE,
				new String[] { "idx", "type", "url", "local", "syncid", "status" }, "status != 0", null, "idx");
		try {
			if (cursor.moveToFirst()) {
				do {
					int status = cursor.getInt(5);
					if (status == STATUS_INIT) {
						return startDownload(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3));
					} else if (status == STATUS_START) {
						if (cursor.getLong(4) != -1) {
							if (!isDownloading(cursor.getLong(4))) {
								return startDownload(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3));
							}
						}
					} else if (status == STATUS_DOWNLOADED) {
						return processDownload(cursor.getInt(0), cursor.getInt(1), cursor.getLong(4), cursor.getString(2));
					} else {
						// ?
					}
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}

		Log.d(Tag, "all update process done.");

		return true;
	}

	private boolean isDownloading(long syncid) {
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(syncid);
		Cursor cursor = downloadManager.query(query);

		try {
			if (cursor.moveToFirst()) {
				int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
				if (status == DownloadManager.STATUS_FAILED) {
					downloadManager.remove(syncid);
					return false;
				}
			}

		} finally {
			cursor.close();
		}
		return true;
	}

	private boolean startDownload(int idx, int type, final String url, String local) {
		DownloadManager.Request req = null;
		if (type == TYPE_UPDATE) {
			req = new DownloadManager.Request(Uri.parse(url));
			req.setTitle("EL");
			req.setDescription(context.getString(R.string.el_downloader_dl_update_script));// "downloading update script..");

			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				Uri dest = Uri.parse("file://" + outputCachePath + local + ".tmp");
				req.setDestinationUri(dest);
			}

		} else if (type == TYPE_PACKAGE) {
			// packages
			req = new DownloadManager.Request(Uri.parse(url));
			req.setTitle("EL");
			req.setDescription(context.getString(R.string.el_downloader_dl_package));//"downloading package..");
			Uri dest = Uri.parse("file://" + outputCachePath + local + ".tmp");
			req.setDestinationUri(dest);
			// req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		} else if (type == TYPE_CHECKNEW) {
			req = new DownloadManager.Request(Uri.parse(url));
			req.setTitle("EL");
			req.setDescription(context.getString(R.string.el_downloader_dl_check_packages));//"check for new packages..");

			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				Uri dest = Uri.parse("file://" + outputCachePath + local + ".tmp");
				req.setDestinationUri(dest);
			}
		} else if (type == TYPE_LATESTVERSION) {
			//latest version
			req = new DownloadManager.Request(Uri.parse(url));
			req.setTitle("EL");
			req.setDescription(context.getString(R.string.el_downloader_dl_latest_version));//"downloading latest version..");
			Uri dest = Uri.parse("file://" + outputCachePath + local + ".tmp");
			req.setDestinationUri(dest);			
		} else {
			return false;
		}

		req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		long syncid = downloadManager.enqueue(req);

		updateStatusByIdx(idx, syncid, STATUS_START);

		return true;
	}

	private boolean processDownload(int idx, int type, long syncid, String url) {
		if (type == TYPE_UPDATE) {
			onUpdateDownloaded(syncid, url);
		} else if (type == TYPE_PACKAGE) {
			onPackageDownloaded(syncid, url);
		} else if (type == TYPE_CHECKNEW) {
			onCheckNewDownloaded(syncid, url);
		} else if (type == TYPE_LATESTVERSION) {
			onLatestVersionDownloaded(syncid, url);
		}

		return true;
	}

	private void regiestReceiver() {
		IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

		receiver = new DownloadReceiver();

		context.registerReceiver(receiver, filter);
	}

	private void unregistReceiver() {
		if (receiver != null) {
			context.unregisterReceiver(receiver);
		}
	}

	public void onDownloadCompleted(Intent intent) {
		long syncid = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(syncid);
		Cursor cursor = downloadManager.query(query);

		try {
			if (cursor.moveToFirst()) {
				int type = queryTypeBySyncId(syncid);
				int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
				if (status == DownloadManager.STATUS_SUCCESSFUL) {
					final String file = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
					updateStatusBySyncId(syncid, file, STATUS_DOWNLOADED);
					if (type == TYPE_UPDATE) {
						onUpdateDownloaded(syncid, file);
					} else if (type == TYPE_PACKAGE) {
						onPackageDownloaded(syncid, file);
					} else if (type == TYPE_CHECKNEW) {
						onCheckNewDownloaded(syncid, file);
					} else if (type == TYPE_LATESTVERSION) {
						onLatestVersionDownloaded(syncid, file);
					}
				} else {
					Log.w(Tag, "download failed (" + status + ") -" + syncid);
					removeUpdateDataBySyncId(syncid);

					if (type == TYPE_UPDATE) {
						showNotification(context.getString(R.string.el_downloader_dl_failed), context.getString(R.string.el_downloader_check_request));//"download failed", "EL - please check request code");
					} else if (type == TYPE_PACKAGE) {
						showNotification(context.getString(R.string.el_downloader_dl_failed), context.getString(R.string.el_downloader_input_request));//"download failed", "EL - please input request code again");
					} else if (type == TYPE_CHECKNEW) {
						showNotification(context.getString(R.string.el_downloader_check_failed), context.getString(R.string.el_downloader_try));//"check for new packages failed", "EL - please try again");
					} else if (type == TYPE_LATESTVERSION) {
						showNotification(context.getString(R.string.el_downloader_dl_version_failed), context.getString(R.string.el_downloader_try));//"download latest version of EL failed.", "EL - please try again");
					}

					checkUpdateData();
				}
			}

		} finally {
			cursor.close();
		}
	}

	private void insertUpdateData(final String request, int type, final String url, final String local, int size, long syncid, int status) {

		// check
		Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_LAC_SYS_UPDATE, new String[] { "count(*)" }, "local=? and status!=?",
				new String[] { local, String.valueOf(STATUS_DONE) }, null);
		try {
			if (cursor.moveToFirst()) {
				if (cursor.getInt(0) > 0) {
					Log.w(Tag, local + " has been in download queue.");
					return;
				}
			}
		} finally {
			cursor.close();
		}

		ContentValues values = new ContentValues();
		values.put("request", request);
		values.put("type", type);
		values.put("url", url);
		values.put("local", local);
		values.put("length", size);
		values.put("syncid", syncid);
		values.put("status", status);
		values.put("updatetime", System.currentTimeMillis());

		context.getContentResolver().insert(ELContentProvider.URI_LAC_SYS_UPDATE, values);
	}

	private void removeUpdateDataBySyncId(long syncid) {
		context.getContentResolver().delete(ELContentProvider.URI_LAC_SYS_UPDATE, "syncid=" + syncid, null);
	}

	private void updateStatusByIdx(int idx, long syncid, int status) {

		ContentValues values = new ContentValues();
		values.put("syncid", syncid);
		values.put("status", status);

		context.getContentResolver().update(ELContentProvider.URI_LAC_SYS_UPDATE, values, "idx=" + idx, null);
	}

	private void updateStatusBySyncId(long syncid, final String url, int status) {

		ContentValues values = new ContentValues();
		if (url != null) {
			values.put("url", url);
		}
		values.put("status", status);
		values.put("updatetime", System.currentTimeMillis());

		context.getContentResolver().update(ELContentProvider.URI_LAC_SYS_UPDATE, values, "syncid=" + syncid, null);
	}

	private int queryTypeBySyncId(long syncid) {

		Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_LAC_SYS_UPDATE, new String[] { "type" }, "syncid=" + syncid, null, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
		} finally {
			cursor.close();
		}
		return -1;
	}

	private String queryLocalBySyncId(long syncid) {
		Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_LAC_SYS_UPDATE, new String[] { "local" }, "syncid=" + syncid, null, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getString(0);
			}
		} finally {
			cursor.close();
		}
		return null;
	}

	private void onUpdateDownloaded(long syncid, String url) {
		Log.d(Tag, "onUpdateDownloaded() syncid:" + syncid + " url:" + url);

		String local = getLocalFile(syncid, url);// null;

//		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//
//			Uri uri = Uri.parse(url);
//			Cursor cursor = service.getContentResolver().query(uri, null, null, null, null);
//			if (cursor != null) {
//				try {
//					if (cursor.moveToFirst()) {
//						local = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
//					}
//				} finally {
//					cursor.close();
//				}
//			}
//		} else {
//
//			local = queryLocalBySyncId(syncid);
//			if (local != null) {
//				String file = url.substring("file://".length());
//				local = outputCachePath + local;
//				File f = new File(file);
//				if (f.exists()) {
//					if (!f.renameTo(new File(local))) {
//						Log.e(Tag, "download rename failed - from:" + file + " to:" + local);
//					}
//				}
//			}
//		}

		if (local != null) {
			final String file = unzipUpdate(local);
			if (analyseUpdated(file)) {
				Utils.removeFile(file);
				Utils.removeFile(local);
				updateStatusBySyncId(syncid, null, STATUS_DONE);

				this.checkUpdateData();
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
			for (int i = 0; i < pk.getLength(); ++i) {
				Element p = (Element) pk.item(i);

				NodeList f = p.getElementsByTagName("file");
				Log.d(Tag, "file = " + f.item(0).getFirstChild().getNodeValue());
				NodeList s = p.getElementsByTagName("size");
				Log.d(Tag, "size = " + s.item(0).getFirstChild().getNodeValue());
				NodeList u = p.getElementsByTagName("url");
				Log.d(Tag, "url = " + u.item(0).getFirstChild().getNodeValue());

				if (f == null || s == null || u == null) {
					Log.w(Tag, "update script is incomplete.");
					continue;
				}

				String url = u.item(0).getFirstChild().getNodeValue().replace("%26", "&");

				insertUpdateData(checkcode, TYPE_PACKAGE, url, f.item(0).getFirstChild().getNodeValue(),
						Integer.valueOf(s.item(0).getFirstChild().getNodeValue()), -1, STATUS_INIT);
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

	private String getLocalFile(long syncid, String url) {
		String local = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			Uri uri = Uri.parse(url);
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null) {
				try {
					if (cursor.moveToFirst()) {
						local = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
					}
				} finally {
					cursor.close();
				}
			}
		} else {
			local = queryLocalBySyncId(syncid);

			if (local != null) {
				String file = url.substring("file://".length());
				local = outputCachePath + local;
				File f = new File(file);
				if (f.exists()) {
					if (!f.renameTo(new File(local))) {
						Log.e(Tag, "download rename failed - from:" + file + " to:" + local);
					}
				}
			}
		}		
		return local;
	}	
	
	private void onPackageDownloaded(long syncid, String url) {

		Log.d(Tag, "onPackageDownloaded() syncid:" + syncid + " url:" + url);

		String file = url.substring("file://".length());

		String local = queryLocalBySyncId(syncid);
		if (local != null) {
			local = outputCachePath + local;
			File f = new File(file);
			if (f.exists()) {
				if (!f.renameTo(new File(local))) {
					Log.e(Tag, "download rename failed - from:" + file + " to:" + local);
				}
			}

			updateStatusBySyncId(syncid, null, STATUS_DONE);

			Intent intent = new Intent(BroadcastAction.ACTION_DOWNLOAD_COMPLETED);
			intent.putExtra(BroadcastAction.DATA_TYPE, DownloadCompletedType.PACKAGE.getId());
			context.sendBroadcast(intent);
			
//			context.onPackageReady(null);
		}
		//
		// updateStatusBySyncId(syncid, null, STATUS_DONE);
		//
		// service.onPackageReady();

		this.checkUpdateData();
	}

	private void onCheckNewDownloaded(long syncid, String url) {

		Log.d(Tag, "onCheckNewDownloaded() syncid:" + syncid + " url:" + url);

		String local = getLocalFile(syncid, url);// null;

		if (local != null) {
			final String file = unzipUpdate(local);
			if (analyseCheckNewPackagesUpdated(file)) {
				Utils.removeFile(file);
				Utils.removeFile(local);

				updateStatusBySyncId(syncid, null, STATUS_DONE);

				this.checkUpdateData();
			}
		}
	}

	private void onLatestVersionDownloaded(long syncid, String url) {
		Log.d(Tag, "onLatestVersionDownloaded() syncid:" + syncid + " url:" + url);

		String file = url.substring("file://".length());

		String local = queryLocalBySyncId(syncid);
		if (local != null) {
			local = outputCachePath + local;
			File f = new File(file);
			if (f.exists()) {
				if (!f.renameTo(new File(local))) {
					Log.e(Tag, "download rename failed - from:" + file + " to:" + local);
				}
			}

			updateStatusBySyncId(syncid, null, STATUS_DONE);

			Intent intent = new Intent(BroadcastAction.ACTION_DOWNLOAD_COMPLETED);
			intent.putExtra(BroadcastAction.DATA_TYPE, DownloadCompletedType.LATEST_VERSION.getId());
			intent.putExtra(BroadcastAction.DATA_TITLE, local);
			context.sendBroadcast(intent);
		
//			context.onLatestVersionReady(local);
		}

		this.checkUpdateData();
	}
	
	private boolean analyseCheckNewPackagesUpdated(String file) {

		cleanNewPackageData();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			Document doc = factory.newDocumentBuilder().parse("file://" + file);

			NodeList lv = doc.getElementsByTagName("latest_version");
			
			if (lv != null && lv.getLength() > 0) {
				Element p = (Element) lv.item(0);
				NodeList ver = p.getElementsByTagName("version");
				if (ver != null && ver.getLength() > 0) {
					Log.d(Tag, "latest_version = " + ver.item(0).getFirstChild().getNodeValue());
					ContentValues values = new ContentValues();
					values.put("idx", ELDBAccess.SYSINFO_LATESTVERSION);
					values.put("value", ver.item(0).getFirstChild().getNodeValue());
					insertLatestVersionUpdateData(values);
				} else {
					Log.d(Tag, "latest_version = " + lv.item(0).getFirstChild().getNodeValue());
					ContentValues values = new ContentValues();
					values.put("idx", ELDBAccess.SYSINFO_LATESTVERSION);
					values.put("value", lv.item(0).getFirstChild().getNodeValue());
					insertLatestVersionUpdateData(values);					
				}
				
				NodeList link = p.getElementsByTagName("link");
				if (link != null && ver.getLength() > 0) {
					Log.d(Tag, "latest_link = " + link.item(0).getFirstChild().getNodeValue());
					ContentValues values = new ContentValues();
					values.put("idx", ELDBAccess.SYSINFO_LATESTPACKAGE);
					values.put("value", link.item(0).getFirstChild().getNodeValue());
					insertLatestVersionUpdateData(values);
				}					
//				Log.d(Tag, "latest_version = " + lv.item(0).getFirstChild().getNodeValue());
//				ContentValues values = new ContentValues();
//				values.put("idx", ELDBAccess.SYSINFO_LATESTVERSION);
//				values.put("value", lv.item(0).getFirstChild().getNodeValue());
//				insertLatestVersionUpdateData(values);
			}

			NodeList pk = doc.getElementsByTagName("package");
			if (pk == null) {
				return true;
			}

			for (int i = 0; i < pk.getLength(); ++i) {
				Element p = (Element) pk.item(i);

				NodeList idx = p.getElementsByTagName("index");
				Log.d(Tag, "index = " + idx.item(0).getFirstChild().getNodeValue());
				NodeList title = p.getElementsByTagName("title");
				Log.d(Tag, "title = " + title.item(0).getFirstChild().getNodeValue());
				NodeList desc = p.getElementsByTagName("desc");
				Log.d(Tag, "desc = " + desc.item(0).getFirstChild().getNodeValue());
				NodeList link = p.getElementsByTagName("link");
				Log.d(Tag, "link = " + link.item(0).getFirstChild().getNodeValue());
				NodeList size = p.getElementsByTagName("size");
				Log.d(Tag, "size = " + size.item(0).getFirstChild().getNodeValue());
				NodeList updated = p.getElementsByTagName("updated");
				Log.d(Tag, "updated = " + updated.item(0).getFirstChild().getNodeValue());

				ContentValues values = new ContentValues();
				values.put("idx", Integer.valueOf(idx.item(0).getFirstChild().getNodeValue()));
				values.put("title", title.item(0).getFirstChild().getNodeValue());
				values.put("desc", desc.item(0).getFirstChild().getNodeValue());
				values.put("link", link.item(0).getFirstChild().getNodeValue());
				values.put("size", Integer.valueOf(size.item(0).getFirstChild().getNodeValue()));
				values.put("updated", updated.item(0).getFirstChild().getNodeValue());

				insertCheckNewPackageUpdateData(values);
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

	private void cleanNewPackageData() {
		context.getContentResolver().delete(ELContentProvider.URI_EL_NEW_PACKAGES, null, null);
	}

	private void insertCheckNewPackageUpdateData(ContentValues values) {
		context.getContentResolver().insert(ELContentProvider.URI_EL_NEW_PACKAGES, values);
	}

	private void insertLatestVersionUpdateData(ContentValues values) {
		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_SYS_INFO, values.getAsInteger("idx"));
		context.getContentResolver().delete(uri, null, null);
		context.getContentResolver().insert(ELContentProvider.URI_EL_SYS_INFO, values);
	}

	private void showNotification(String title, String text) {
		Intent intent = new Intent(NotificationAction.ACTION_SHOW);
		intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.WARNING.getId());
		intent.putExtra(NotificationAction.DATA_TITLE, title);
		intent.putExtra(NotificationAction.DATA_TEXT, text);

		context.sendBroadcast(intent);
	}

}
