package jie.android.el.service;

import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.CommonConsts.BroadcastAction;
import jie.android.el.CommonConsts.DownloadCompletedType;
import jie.android.el.CommonConsts.DownloadRequest;
import jie.android.el.CommonConsts.ServiceState;
import jie.android.el.database.ELContentProvider;
import jie.android.el.database.Word;
import jie.android.el.service.receiver.ServiceReceiver;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ELService extends Service implements ServiceReceiver.OnServiceIntentListener {

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
			return dictionary.getWordXmlResult(word);// null;//dictionary.getWordXmlResult(word);
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
		public void setAudioAction(Intent intent) throws RemoteException {
			onAudioAction(intent);
		}
	}

	private Dictionary dictionary = null;
	private AudioPlayer player = null;
	private Downloader downloader = null;
	private PackageImporter packageImporter = null;

	private ServiceNotification serviceNotification = null;

	private ServiceReceiver serviceReceiver = new ServiceReceiver(this);

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(Tag, "onBind:" + intent.getAction());

		sendBroadcast(new Intent(BroadcastAction.ACTION_SERVICE_BINDED));

		return new AccessStub();
	}

	public void onUIConnected() {
		postServiceState(ServiceState.READY);

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

		// android.os.Debug.waitForDebugger();

		initDictionary();
		initPlayer();

		initServiceReceiver();

		sendBroadcast(new Intent(BroadcastAction.ACTION_SERVICE_INIT));
	}

	@Override
	public void onDestroy() {

		Intent intent = new Intent(BroadcastAction.ACTION_SERVICE_END);
		this.sendBroadcast(intent);

		releaseServiceReceiver();

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

	private void initServiceReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioAction.ACTION_AUDIO);
		filter.addAction(AudioAction.ACTION_AUDIO_SET);
		filter.addAction(AudioAction.ACTION_AUDIO_PREV);
		filter.addAction(AudioAction.ACTION_AUDIO_NEXT);
		filter.addAction(AudioAction.ACTION_AUDIO_PLAY);
		filter.addAction(AudioAction.ACTION_AUDIO_STOP);
		filter.addAction(AudioAction.ACTION_AUDIO_NAVIGATE);
		filter.addAction(AudioAction.ACTION_AUDIO_NAVIGATE_SLOWDIALOG);
		filter.addAction(AudioAction.ACTION_AUDIO_NAVIGATE_EXPLANATION);
		filter.addAction(AudioAction.ACTION_AUDIO_NAVIGATE_FASTDIALOG);
		filter.addAction(AudioAction.ACTION_UPDATE_AUDIO);
		filter.addAction(AudioAction.ACTION_UPDATE_UI);
		filter.addAction(AudioAction.ACTION_AUDIO_SEEK);
		filter.addAction(AudioAction.ACTION_AUDIO_QUERY);
		filter.addAction(AudioAction.ACTION_UPDATE_AUDIO_PLAYING);
		filter.addAction(AudioAction.ACTION_AUDIO_FORCE_PAUSE);
		filter.addAction(BroadcastAction.ACTION_DOWNLOAD_COMPLETED);
		// filter.addAction(WidgetAction.ACTION_STARTACTIVITY);

		registerReceiver(serviceReceiver, filter);
	}

	private void releaseServiceReceiver() {
		unregisterReceiver(serviceReceiver);
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

		return downloader.addDownloadRequest(request, check);
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

	private void onLatestVersionReady(final String file) {
		Log.d(Tag, "latest version file = " + file);
		Toast.makeText(this, "ready to install the latest of EL..", Toast.LENGTH_LONG).show();
		//
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + file), "application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public void onAudioAction(Intent intent) {

		if (player != null) {
			player.onAction(intent);
		}
	}

	public void onAudioUpdate(Intent intent) {
		if (serviceNotification != null) {
			try {
				serviceNotification.onUpdateAudio(intent);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onUIUpdate(Intent intent) {
		if (player != null) {
			player.onUIUpdate(intent);
		}
	}

	@Override
	public void onDownloadCompleted(Intent intent) {
		int type = intent.getIntExtra(BroadcastAction.DATA_TYPE, -1);
		if (type == DownloadCompletedType.PACKAGE.getId()) {
			onPackageReady(null);
		} else if (type == DownloadCompletedType.LATEST_VERSION.getId()) {
			final String file = intent.getStringExtra(BroadcastAction.DATA_TITLE);
			if (file != null) {
				onLatestVersionReady(file);
			}
		}
	}
}
