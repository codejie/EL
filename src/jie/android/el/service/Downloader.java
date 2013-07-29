package jie.android.el.service;

import java.util.ArrayList;

import jie.android.el.database.LACDBAccess;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Downloader {

	private class DownloadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
		}
		
	}
	
	private class UpdateData {
		
		public int syncid = -1;
		public int status = -1; //-1: initial; 0: done; 1: start; 2: update 
		public int type = -1; //0: update.xml; 1: package.zip
		public String url = null;
		
		public UpdateData(int type, final String url) {
			this.type = type;
			this.url = url;
		}
	}
	
	private ELService service = null;	
	private LACDBAccess dbAccess = null;
	private DownloadManager downloadManager = null;
	
	private ArrayList<UpdateData> updateData = null;	
	
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
	
	private boolean loadRequestData() {
		return false;
	}
	
	public boolean addDownloadRequest(final String request) {
		final String file = "update.xml";
		final String url = request2Url(request, file);
		
		if (url == null) {
			return false;
		}
		
		if (!dbAccess.insertUpdateData(request, url, 0, -1, -1, 0)) {
			return false;
		}
		
		if (updateData == null) {
			updateData = new ArrayList<UpdateData>();			
		}
		
		UpdateData data = new UpdateData(0, url);
		updateData.add(data);
		
		return true;
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
		
		if (p == "3") {
			return "http://www.cppblog.com/Files/codejie/" + uc + "_" + file;
		}
		
		return null;
	}

	private int download(final String url) {
		//downloadManager.d
	}
	
	
	public void release() {
		unregistReceiver();
	}
	
	private void regiestReceiver() {
		// TODO Auto-generated method stub
		
	}
	
	private void unregistReceiver() {
		// TODO Auto-generated method stub
		
	}

	private boolean download(final Uri uri) {
		return false;
	}
	
}
