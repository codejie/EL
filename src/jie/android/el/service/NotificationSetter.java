package jie.android.el.service;

import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.ELActivity;
import jie.android.el.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

public class NotificationSetter {
	
	private Context context = null;
	private NotificationManager manager = null;
	
	private final int playId = 0;
	private final int importId = 1;
	private int otherId = 10;
	
	public NotificationSetter(Context context) {
		this.context = context;
		
		manager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void release() {		
		manager.cancelAll();
	}
	
	public int show(NotificationType type, final String title, final String text) {
		
		Intent result = new Intent(context, ELActivity.class);
		PendingIntent intent = PendingIntent.getActivity(context, 0, result, 0);
		
		Notification nt = null;
		int id = -1;
		
		if (type == NotificationType.PLAY) {
			
			manager.cancel(playId);
			
			nt = buildPlayNotification(intent, title, text);
			id = playId;
		} else if (type == NotificationType.IMPORT) {
			nt = buildImportNotification(intent, title, text);
			id = importId;
		} else if (type == NotificationType.WARNING) {
			nt = buildWarningNotification(intent, title, text);
			id = ++ otherId;
		} else {
			return -1;
		}
		
		manager.notify(id, nt);
		
		return id;
	}
	
	private Notification buildPlayNotification(PendingIntent intent, String title, String text) {
		
		RemoteViews rv = new RemoteViews(context.getPackageName(),R.layout.layout_notification_play);
		
		Notification nt = new Notification();
		nt.icon = R.drawable.el_nt_play;
		nt.tickerText = title;
		//nt.tickerView = rv;
		//nt.bigContentView = rv;
		nt.contentView = rv;
		nt.contentIntent = intent;
//		
//		builder.setSmallIcon(R.drawable.el_nt_play);
//		builder.setContentText(text);
//		builder.setContentTitle(title);
//		builder.setTicker(title);
//		
//		Notification nt = builder.build();
//		
//		RemoteViews rv = new RemoteViews(context.getPackageName(),R.layout.layout_notification_play);

		
		
		nt.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT; 
		
		return nt;
	}

	private Notification buildImportNotification(PendingIntent intent, String title,	String text) {
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		
		builder.setSmallIcon(R.drawable.el_nt_import);
//		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.el_nt_import));
		builder.setContentText(text);
		builder.setContentTitle(title);
		builder.setTicker(title);
		builder.setContentIntent(intent);

		Notification nt = builder.build();
		nt.flags = Notification.FLAG_AUTO_CANCEL;//FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT; 

		return nt;
	}

	private Notification buildWarningNotification(PendingIntent intent, String title, String text) {
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		
		builder.setSmallIcon(R.drawable.el_nt_warning);
//		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.el_nt_warning));
		builder.setContentText(text);
		builder.setContentTitle(title);
		builder.setTicker(title);	
		builder.setContentIntent(intent);
		
		Notification nt = builder.build();
		nt.flags = Notification.FLAG_AUTO_CANCEL; 

		return nt;
	}
	
	public void remove(NotificationType type, int id) {
		if (type == NotificationType.PLAY) {
			manager.cancel(playId);
		} else if (type == NotificationType.IMPORT) {
			manager.cancel(importId);
		} else {
			manager.cancel(id);
		}
	}
	
}
