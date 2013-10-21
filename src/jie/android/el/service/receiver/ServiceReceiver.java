package jie.android.el.service.receiver;

import jie.android.el.ELActivity;
import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.CommonConsts.BroadcastAction;
import jie.android.el.CommonConsts.WidgetAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceReceiver extends BroadcastReceiver {

	private static final String Tag = ServiceReceiver.class.getSimpleName();
	
	public interface OnServiceIntentListener {
		void onAudioAction(Intent intent);
		void onAudioUpdate(Intent intent);
		void onUIUpdate(Intent intent);
		void onDownloadCompleted(Intent intent);
	}
	
	private OnServiceIntentListener listener;
	
	public ServiceReceiver(OnServiceIntentListener l) {
		listener = l;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(AudioAction.ACTION_UPDATE_AUDIO_PLAYING) || action.equals(AudioAction.ACTION_UPDATE_AUDIO)) {
			onUpdateAudio(intent);
		} else if (action.startsWith(AudioAction.ACTION_AUDIO)) {
			onAudioAction(intent);
		} else if (action.equals(AudioAction.ACTION_UPDATE_UI)) {
			onUIUpdate(intent);
		} else if (action.equals(WidgetAction.ACTION_STARTACTIVITY)) {
 			Intent sa = new Intent(context, ELActivity.class);
 			context.startActivity(sa);
		} else if (action.equals(BroadcastAction.ACTION_DOWNLOAD_COMPLETED)) {
			onDownloadCompleted(intent);
		} else {
			Log.w(Tag, "Unknown Action - " + action);
		}
	}

	private void onAudioAction(Intent intent) {
		if (listener != null) {
			listener.onAudioAction(intent);
		}
	}

	private void onUpdateAudio(Intent intent) {
		if (listener != null) {
			listener.onAudioUpdate(intent);
		}
	}

	private void onUIUpdate(Intent intent) {
		if (listener != null) {
			listener.onUIUpdate(intent);
		}
	}
	
	private void onDownloadCompleted(Intent intent) {
		if (listener != null) {
			listener.onDownloadCompleted(intent);
		}		
	}
	
}
