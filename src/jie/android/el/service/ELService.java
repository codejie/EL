package jie.android.el.service;

import jie.android.el.database.Dictionary;
import jie.android.el.database.LACDBAccess;
import jie.android.el.database.Word;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ELService extends Service {
	
	private static final String Tag = ELService.class.getSimpleName();

	private class AccessStub extends ServiceAccess.Stub {

		@Override
		public Word.XmlResult queryWordResult(String word) throws RemoteException {
			return dictionary.getWordXmlResult(word);
		}

		@Override
		public void setAudio(String audio, OnPlayAudioListener listener) throws RemoteException {
			player.setOnPlayAudioListener(listener);
			player.setData(audio);
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
	}
	
	private LACDBAccess dbAccess = null;
	private Dictionary dictionary = null;
	private AudioPlayer player = null;
	
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
	
}
