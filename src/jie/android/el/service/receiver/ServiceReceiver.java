package jie.android.el.service.receiver;

import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.service.ELService;
import jie.android.el.service.ServiceAccess;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ServiceReceiver extends BroadcastReceiver {

	private static final String Tag = ServiceReceiver.class.getSimpleName();
	
	private static ServiceAccess serviceAccess;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (action.equals(AudioAction.ACTION_SERVICE_INIT) || action.equals(AudioAction.ACTION_SERVICE_BINDED)) {
			initService(context);
		} else if (action.startsWith(AudioAction.ACTION_AUDIO)) {
			onAudioAction(intent);
		} else {
			
		}
	}

	private void initService(Context context) {
		Intent si = new Intent("jie.android.el.elservice");
		IBinder binder = this.peekService(context, si);
		if (binder != null) {
			serviceAccess = ServiceAccess.Stub.asInterface(binder);
			Log.d(Tag, "server init succ.");
		} else {
			Log.e(Tag, "cannot get service interface.");
		}
	}
	
	private void onAudioAction(Intent intent) {
		if (serviceAccess != null) {
			try {
				serviceAccess.setAudioAction(intent);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}


}
