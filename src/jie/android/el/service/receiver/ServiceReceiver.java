package jie.android.el.service.receiver;

import jie.android.el.ELActivity;
import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.CommonConsts.WidgetAction;
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
	
	public interface OnServiceIntentListener {
		void onAudioAction(Intent intent);
		void onAudioUpdate(Intent intent);
		void onUIUpdate(Intent intent);
	}
	
	private OnServiceIntentListener listener;
	
//	private static ServiceAccess serviceAccess;
	
	public ServiceReceiver(OnServiceIntentListener l) {
		listener = l;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
//		if (action.equals(AudioAction.ACTION_SERVICE_INIT) || action.equals(AudioAction.ACTION_SERVICE_BINDED)) {
//			initService(context);
//		} 
		if (action.equals(AudioAction.ACTION_UPDATE_AUDIO_PLAYING) || action.equals(AudioAction.ACTION_UPDATE_AUDIO)) {
			onUpdateAudio(intent);
		} else if (action.startsWith(AudioAction.ACTION_AUDIO)) {
			onAudioAction(intent);
		} else if (action.equals(AudioAction.ACTION_UPDATE_UI)) {
			onUIUpdate(intent);
		} else if (action.equals(WidgetAction.ACTION_STARTACTIVITY)) {
 			Intent sa = new Intent(context, ELActivity.class);
 			context.startActivity(sa);
		} else {
			
		}
	}

	private void initService(Context context) {
//		Intent si = new Intent("jie.android.el.elservice");
//		IBinder binder = this.peekService(context, si);
//		if (binder != null) {
//			serviceAccess = ServiceAccess.Stub.asInterface(binder);
//			Log.d(Tag, "server init succ.");
//		} else {
//			Log.e(Tag, "cannot get service interface.");
//		}
	}
	
	private void onAudioAction(Intent intent) {
		if (listener != null) {
			listener.onAudioAction(intent);
		}
//		if (serviceAccess != null) {
//			try {
//				serviceAccess.setAudioAction(intent);
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}		
	}

	private void onUpdateAudio(Intent intent) {
		if (listener != null) {
			listener.onAudioUpdate(intent);
		}
//		if (serviceAccess != null) {
//			try {
//				serviceAccess.setAudioAction(intent);
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}		
	}

	private void onUIUpdate(Intent intent) {
		if (listener != null) {
			listener.onUIUpdate(intent);
		}
	}
	
}
