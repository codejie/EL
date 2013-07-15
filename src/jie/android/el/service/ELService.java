package jie.android.el.service;

import jie.android.el.database.Dictionary;
import jie.android.el.database.LACDBAccess;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ELService extends Service {
	
	private static final String Tag = ELService.class.getSimpleName();

	private class AccessStub extends ServiceAccess.Stub {

		@Override
		public void playAudio(String file, PlayAudioListener listener) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stopAudio(int token) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void pauseAudio(int token) throws RemoteException {
			// TODO Auto-generated method stub
			
		}		
	}
	
	private LACDBAccess dbAccess = null;
	private Dictionary dictionary = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return new AccessStub();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		initDatabase();
		initDictionary();
	}
	
	@Override
	public void onDestroy() {
		
		releaseDictionary();
		releaseDatabase();

		super.onDestroy();
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

	}
	
	private void releaseDictionary() {
		
	}
	
}
