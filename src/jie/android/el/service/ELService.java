package jie.android.el.service;

import jie.android.el.CommonConsts.DownloadRequest;
import jie.android.el.CommonConsts.NotificationAction;
import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.CommonConsts.PlayState;
import jie.android.el.CommonConsts.ServiceState;
import jie.android.el.database.ELContentProvider;
import jie.android.el.database.Word;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ELService extends Service {
	
	private static final String Tag = ELService.class.getSimpleName();

	private class AccessStub extends ServiceAccess.Stub {

		@Override
		public void regServiceNotification(int token, ServiceNotification notification) throws RemoteException {
			serviceNotification = notification;
			
			onUIConnected();
		}

		@Override
		public void unregServiceNotification(int token) throws RemoteException {
			serviceNotification = null;			
		}
		
		@Override
		public Word.XmlResult queryWordResult(String word) throws RemoteException {
			return  dictionary.getWordXmlResult(word);// null;//dictionary.getWordXmlResult(word);
		}

		@Override
		public void setAudio(int index) throws RemoteException {
			player.setData(index);
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
		public int getPlayState() throws RemoteException {
			if (player == null) {
				return PlayState.INVALID.getId();
			}
			return player.getPlayState().getId();
		}		

		@Override
		public boolean canExit() throws RemoteException {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean addDownloadRequest(String request, String check) throws RemoteException {
			return onDownloadRequest(request, check);
		}
				
		@Override
		public void setUIState(int state) throws RemoteException {
			onUIStateChanged(state);
		}

	}
	
	public class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			
			if (action.equals(NotificationAction.ACTION_SHOW)) {
				final int type = intent.getExtras().getInt(NotificationAction.DATA_TYPE);
				final String title = intent.getExtras().getString(NotificationAction.DATA_TITLE);
				final String text = intent.getExtras().getString(NotificationAction.DATA_TEXT);
				final boolean play = intent.getExtras().getBoolean(NotificationAction.DATA_STATE, false);
				if (title != null && text != null) {
					onShowNotification(type, play, title, text);
				}
			} else if (action.equals(NotificationAction.ACTION_REMOVE)) {
				onRemoveNotification(intent.getExtras().getInt(NotificationAction.DATA_TYPE), intent.getExtras().getInt(NotificationAction.DATA_ID));
			} else if (action.startsWith(NotificationAction.ACTION_CLICK)) {
				onNotificationClick(action);
			}
		}
	}

	private NotificationReceiver notificationReceiver = null;
	private Dictionary dictionary = null;
	private AudioPlayer player = null;
	private Downloader downloader = null;
	private PackageImporter packageImporter = null;
	private NotificationSetter notificationSetter = null;
	
	private ServiceNotification serviceNotification = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return new AccessStub();
	}

	public void onUIConnected() {
		postServiceState(ServiceState.READY);
		
		if (player.isPlaying() || player.isPause()) {
			postServiceState(ServiceState.PLAYING);
//			postServiceIsPlaying(player.getPlayState(), player.getAudioIndex(), player.getDuration(), player.getCurrentPosition());
		}
		
		if (Downloader.checkIncomplete(this)) {
			initDownloader();
		}
		
		final String[] res = PackageImporter.check();
		if (res != null && res.length > 0) {
			onPackageReady(res);
		}
		
		loadBundledPackage();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		//android.os.Debug.waitForDebugger();
		
		initNotification();		
		initDictionary();		
		initPlayer();
		
		final String[] res = PackageImporter.check();
		if (res != null && res.length > 0) {
			onPackageReady(res);
//			packageImporter = new PackageImporter(this, res);
//			packageImporter.startImport();
		}
	}

	@Override
	public void onDestroy() {
		
		releaseDownloader();
		releasePlayer();
		releaseDictionary();
		releaseNotification();

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
	
	private void initNotification() {
		notificationSetter = new NotificationSetter(this);
		
		notificationReceiver = new NotificationReceiver();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationAction.ACTION_SHOW);
		filter.addAction(NotificationAction.ACTION_REMOVE);
		filter.addAction(NotificationAction.ACTION_CLICK_PREV);
		filter.addAction(NotificationAction.ACTION_CLICK_NEXT);
		filter.addAction(NotificationAction.ACTION_CLICK_PLAY);
		filter.addAction(NotificationAction.ACTION_CLICK_CLOSE);
		
		registerReceiver(notificationReceiver, filter);
	}
	
	private void releaseNotification() {
		if (notificationReceiver != null) {
			unregisterReceiver(notificationReceiver);
		}
		
		if (notificationSetter != null) {
			notificationSetter.release();
		}
	}

	private void postServiceState(ServiceState state) {
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
	
	public boolean onDownloadRequest(String request, String check) {
		if (downloader == null) {
			initDownloader();
		}
		
		return downloader.addDownloadRequest(request,check);
	}
	
	public boolean onCheckNewPackages() {
		return onDownloadRequest(DownloadRequest.CHECK_NEW_PACKAGES, null);
	}
	
	public void onPackageReady(final String[] res) {
		if (packageImporter == null) {
			packageImporter = new PackageImporter(this, res);
			packageImporter.startImport();			
		} else {
			packageImporter.refresh();
		}		
	}

	public void onUIStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	private void loadBundledPackage() {
		
		Cursor cursor = this.getContentResolver().query(ELContentProvider.URI_EL_ESL, new String[] { "count(*)" }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				if (cursor.getInt(0) == 0) {
					if (packageImporter == null) {
						packageImporter = new PackageImporter(this, null);
					}
					packageImporter.loadBundledPackage();
				}
			}
		} finally {
			cursor.close();
		}
	}

	public int onShowNotification(int level, boolean play, String title, String text) {
		if (notificationSetter != null) {
			NotificationType type = NotificationType.getType(level);
			if (type != null) {
				notificationSetter.show(type, play, title, text);
			}
		}
		return 0;
	}

	public void onRemoveNotification(int level, int id) {
		if (notificationSetter != null) {
			NotificationType type = NotificationType.getType(level);
			if (type != null) {
				notificationSetter.remove(type, id);
			}
		}
	}
	
	public void onNotificationClick(final String action) {
		if (player != null) {
			player.onNotificationClick(action);
		}
	}

	public void onLatestVersionReady(final String file) {
		Log.d(Tag, "latest version file = " + file);
		Toast.makeText(this, "ready to install the latest of EL..", Toast.LENGTH_LONG).show();
		//
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + file), "application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent); 
	}
	
}
