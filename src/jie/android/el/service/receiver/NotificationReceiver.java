package jie.android.el.service.receiver;

import jie.android.el.CommonConsts.BroadcastAction;
import jie.android.el.CommonConsts.NotificationAction;
import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.service.NotificationSetter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

	private static final String Tag = NotificationReceiver.class.getSimpleName();
	
	private static NotificationSetter notificationSetter;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (action.equals(BroadcastAction.ACTION_SERVICE_INIT)) {
			initNotificationSetter(context);
			Log.d(Tag, "init service action.");
		} else if (action.equals(BroadcastAction.ACTION_SERVICE_END)) {
			releaseNotificationSetter();
			Log.d(Tag, "release service action.");
		} else if (action.equals(NotificationAction.ACTION_SHOW)) {
			final int type = intent.getExtras().getInt(NotificationAction.DATA_TYPE);
			final String title = intent.getExtras().getString(NotificationAction.DATA_TITLE);
			final String text = intent.getExtras().getString(NotificationAction.DATA_TEXT);
			final boolean play = intent.getExtras().getBoolean(NotificationAction.DATA_STATE, false);
			if (title != null && text != null) {
				onShowNotification(type, play, title, text);
			}
		} else if (action.equals(NotificationAction.ACTION_REMOVE)) {
			onRemoveNotification(intent.getExtras().getInt(NotificationAction.DATA_TYPE), intent.getExtras().getInt(NotificationAction.DATA_ID));
		} else {
			Log.w(Tag, "unknown intent action - " + action);
		}
	}

	private void initNotificationSetter(Context context) {
		notificationSetter = new NotificationSetter(context);
		if (notificationSetter == null) {
			Log.e(Tag, "init notification setter failed.");
		} else {
			Log.d(Tag, "setter is not null" + this.hashCode() + " - " + this.toString());
		}
	}

	private void releaseNotificationSetter() {
		if (notificationSetter != null) {
			notificationSetter.release();
			notificationSetter = null;
		}
	}

	private int onShowNotification(int level, boolean play, String title, String text) {
//		Log.d(Tag, "code = "  + this.hashCode() + " - " + this.toString());
		if (notificationSetter != null) {
			NotificationType type = NotificationType.getType(level);
			if (type != null) {
				notificationSetter.show(type, play, title, text);
			}
		}
		return 0;
	}
	
	private void onRemoveNotification(int level, int id) {
//		Log.d(Tag, "code = "  + this.hashCode() + " - " + this.toString());
		if (notificationSetter != null) {
			NotificationType type = NotificationType.getType(level);
			if (type != null) {
				notificationSetter.remove(type, id);
			}
		}
	}	
}
