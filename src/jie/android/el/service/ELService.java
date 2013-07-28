package jie.android.el.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jie.android.el.database.Dictionary;
import jie.android.el.database.LACDBAccess;
import jie.android.el.database.Word;
import jie.android.el.utils.AssetsHelper;
import android.app.Service;
import android.content.Intent;
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
			
			if (player.isPlaying()) {
				postServiceIsPlaying(player.getAudioIndex(), player.getDuration(), player.getCurrentPosition());
			} else if (isServiceReady) {
				postServiceState(ServiceState.READY);
			}
		}

		@Override
		public void unregServiceNotification(int token) throws RemoteException {
			serviceNotification = null;			
		}
		
		@Override
		public Word.XmlResult queryWordResult(String word) throws RemoteException {
			return dictionary.getWordXmlResult(word);
		}

		@Override
		public void setAudio(int index, String audio) throws RemoteException {
			player.setData(index, audio);
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
			return false;
		}
	}
	
	private static final int STATE_READY	=	0;
	private static final int STATE_UNZIP	=	1;
	private static final int STATE_PLAYING	=	2;
	
	private LACDBAccess dbAccess = null;
	private Dictionary dictionary = null;
	private AudioPlayer player = null;
	
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
		
		initDatabase();
		initDictionary();
		initPlayer();
		
		isServiceReady = true;
	}

	@Override
	public void onDestroy() {
		
		releasePlayer();
		releaseDictionary();
		releaseDatabase();

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

	private void initDatabase() {
		if (!checkDataFile()) {
			Log.e(Tag, "check data failed.");
		}
		
		dbAccess = new LACDBAccess(this);
		if (!dbAccess.open()) {
			Log.e(Tag, "init database failed.");
		}
	}
	
	private void releaseDatabase() {
		if (dbAccess != null) {
			dbAccess.close();
		}
	}
	
	private void initDictionary() {
		dictionary = new Dictionary(dbAccess, this.getDatabasePath(LACDBAccess.FILE).getParent());
		if (!dictionary.load()) {
			Log.e(Tag, "load dictionary data failed.");
		}
	}
	
	private void releaseDictionary() {
		if (dictionary != null) {
			dictionary.close();
		}
	}
	
	public LACDBAccess getDBAccess() {
		return dbAccess;
	}
	
	private boolean checkDataFile() {
		if (!getDatabasePath(LACDBAccess.FILE).exists()) {
			postServiceState(ServiceState.UNZIP);
			return unzipDataFile();
		}
		return true;
	}

	private boolean unzipDataFile() {
		
		File target = getDatabasePath(LACDBAccess.FILE).getParentFile();		

		if (!target.exists()) {
			target.mkdirs();
		}
		
		InputStream input;
		try {
			input = getAssets().open("lac2.zip");
			AssetsHelper.UnzipTo(input, target.getAbsolutePath(), null);
		} catch (IOException e) {
			e.printStackTrace();			
			return false;
		}
		
		return true;
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
}
