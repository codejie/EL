package jie.android.el.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class ELService extends Service {

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
	
	@Override
	public IBinder onBind(Intent arg0) {
		return new AccessStub();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		initDatabase();		
	}

	private void initDatabase() {

	}

}
