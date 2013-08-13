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

public class NotificationSetter {
	
	private Context context = null;
	private NotificationManager manager = null;
	
	private final int playId = 0;
	private int otherId = 1;
	
	public NotificationSetter(Context context) {
		this.context = context;
		
		manager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void release() {		
		manager.cancelAll();
	}
	
	public int show(NotificationType type, final String title, final String text) {
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		
		Intent result = new Intent(context, ELActivity.class);
		PendingIntent intent = PendingIntent.getActivity(context, 0, result, 0);

		builder.setContentIntent(intent);
		
		Notification nt = null;
		int id = -1;
		
		if (type == NotificationType.PLAY) {
			
			manager.cancel(playId);
			
			nt = buildPlayNotification(builder, title, text);
			id = playId;
		} else if (type == NotificationType.IMPORT) {
			nt = buildImportNotification(builder, title, text);
			id = ++ otherId;
		} else if (type == NotificationType.WARNING) {
			nt = buildWarningNotification(builder, title, text);
			id = ++ otherId;
		} else {
			return -1;
		}
		
		manager.notify(id, nt);
		
		return id;
	}
	
	private Notification buildPlayNotification(Builder builder, String title, String text) {
		builder.setSmallIcon(R.drawable.el_nt_play);
//		BitmapFactory.Options opts = new BitmapFactory.Options();
////		opts.inScaled = true;
//		opts.outHeight = 64;
//		opts.outWidth = 64;
//		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.el_nt_play, opts));
		builder.setContentText(text);
		builder.setContentTitle(title);
		builder.setTicker(title);
		
		Notification nt = builder.build();
		nt.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT; 
		
		return nt;
	}

	private Notification buildImportNotification(Builder builder, String title,	String text) {
		builder.setSmallIcon(R.drawable.el_nt_import);
//		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.el_nt_import));
		builder.setContentText(text);
		builder.setContentTitle(title);
		builder.setTicker(title);

		Notification nt = builder.build();
		nt.flags = Notification.FLAG_AUTO_CANCEL;//FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT; 

		return nt;
	}

	private Notification buildWarningNotification(Builder builder, String title, String text) {
		builder.setSmallIcon(R.drawable.el_nt_warning);
//		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.el_nt_warning));
		builder.setContentText(text);
		builder.setContentTitle(title);
		builder.setTicker(title);	
		Notification nt = builder.build();
		nt.flags = Notification.FLAG_AUTO_CANCEL; 

		return nt;
	}
	
	public void remove(NotificationType type, int id) {
		if (type == NotificationType.PLAY) {
			manager.cancel(playId);
		} else {
			manager.cancel(id);
		}
	}
	
}
