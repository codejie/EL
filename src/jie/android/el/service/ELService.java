package jie.android.el.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jie.android.el.database.ELContentProvider;
import jie.android.el.database.LACDBAccess;
import jie.android.el.database.Word;
import jie.android.el.utils.AssetsHelper;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ELService extends Service {
	
	private static final String Tag = ELService.class.getSimpleName();

	private class AccessStub extends ServiceAccess.Stub {

		@Override
		public void regServiceNotification(int token, ServiceNotification notification) throws RemoteException {
			serviceNotification = notification;
			
			postServiceState(CommonState.Service.READY);
			
			if (player.isPlaying()) {
				postServiceIsPlaying(player.getAudioIndex(), player.getDuration(), player.getCurrentPosition());
			}
		}

		@Override
		public void unregServiceNotification(int token) throws RemoteException {
			serviceNotification = null;			
		}
		
		@Override
		public Word.XmlResult queryWordResult(String word) throws RemoteException {
			return dictionary.getWordXmlResult(word);// null;//dictionary.getWordXmlResult(word);
		}

		@Override
		public void setAudio(int index, int position) throws RemoteException {
			player.setData(index, position);
		}

		@Override
		public void setAudioListener(OnPlayAudioListener listener) throws RemoteException {
			player.setOnPlayAudioListener(listener);			
		}

		@Override
		public void playAudio() throws RemoteException {
			player.play();			
		}

		@Override
		public void stopAudio() throws RemoteException {
			player.stop();			
		}

		@Override
		public void pauseAudio() throws RemoteException {
			player.pause();			
		}

		@Override
		public void seekAudio(int poistion) throws RemoteException {
			player.seekTo(poistion);
		}

		@Override
		public boolean isAudioPlaying() throws RemoteException {
			return player.isPlaying(); 
		}

		@Override
		public boolean canExit() throws RemoteException {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void addDownloadRequest(String request) throws RemoteException {
			onDownloadRequest(request);
		}

		@Override
		public void setUIState(int state) throws RemoteException {
			onUIStateChanged(state);
		}
	}
	
	private Dictionary dictionary = null;
	private AudioPlayer player = null;
	private Downloader downloader = null;
	
	private boolean isServiceReady = false;
	
	private ServiceNotification serviceNotification = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return new AccessStub();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		//android.os.Debug.waitForDebugger();
		
		initDictionary();
		initPlayer();
		
		if (Downloader.check(this)) {
			initDownloader();
		}
		
		isServiceReady = true;
	}

	@Override
	public void onDestroy() {
		
		releaseDownloader();
		releasePlayer();
		releaseDictionary();

		super.onDestroy();
	}	

	private void initPlayer() {
		player = new AudioPlayer(this);
	}

	private void releasePlayer() {
		if (player != null) {
			player.release();
		}
	}

	private void initDictionary() {
		dictionary = new Dictionary(this);
		if (!dictionary.load()) {
			Log.e(Tag, "load dictionary data failed.");
		}
	}
	
	private void releaseDictionary() {
		if (dictionary != null) {
			dictionary.close();
		}
	}
	
	private void initDownloader() {
		downloader = new Downloader(this);
		if (!downloader.init()) {
			Log.e(Tag, "downloader init failed.");
		}
	}
	
	private void releaseDownloader() {
		if (downloader != null) {
			downloader.release();
		}
	}

	private void postServiceState(CommonState.Service state) {
		if (serviceNotification != null) {
			try {
				serviceNotification.onServiceState(state.getId());
			} catch (DeadObjectException e) {
				serviceNotification = null;
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void postServiceIsPlaying(int index, int duration, int position) {
		if (serviceNotification != null) {
			try {
				serviceNotification.onAudioPlaying(index, duration, position);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public void onDownloadRequest(String request) {
		if (downloader == null) {
			initDownloader();
		}
		
		downloader.addDownloadRequest(request);
	}
	
	public void onPackageReady(long syncid, final String file) {
		try {
			serviceNotification.onPackageReady(syncid, file);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onUIStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}
	
}
