package jie.android.el.service;

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
	
	private ELService service = null;
	
	private DownloadManager downloadManager = null;
	
	public Downloader(ELService service) {
		this.service = service;
	}

	public static boolean check(LACDBAccess dbAccess) {
		// if true, need initial download object
		return false;
	}
	
	public boolean init() {
		downloadManager = (DownloadManager) service.getSystemService(Context.DOWNLOAD_SERVICE);

		regiestReceiver();
		
		return (downloadManager != null);
	}
	
	public void release() {
		//unregistReceiver();
	}
	
	private void regiestReceiver() {
		// TODO Auto-generated method stub
		
	}

	private boolean download(final Uri uri) {
		return false;
	}
	
}
