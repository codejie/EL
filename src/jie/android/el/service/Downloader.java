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
		public String uri = null;
		
		public UpdateData(int type, final String uri) {
			this.type = type;
			this.uri = uri;
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
	
	public boolean addDownloadRequest(final String request, final String uricode) {
		final String file = "update.xml";
		if (!dbAccess.insertUpdateData(request, uricode, 0, file, -1, -1, 0)) {
			return false;
		}
		
		String uri = request2Uri(request, uricode, file);		
		
		if (updateData == null) {
			updateData = new ArrayList<UpdateData>();			
		}
		
		UpdateData data = new UpdateData(0, uri);
		updateData.add(data);
	}
	
//	private boolean updateDownloadReqest(int syncid, int status) {
//		
//	}

//	private void clearDownload() {
//		
//	}
	
//	private boolean addDownloadPackage(final String requet, final String uricode, final String file) {
//		
//	}
//	
//	private boolean loadUpdateInfo() {
//		
//	}
	
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
