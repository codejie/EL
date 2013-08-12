package jie.android.el.service;

import java.util.HashSet;
import java.util.Set;

import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.ELActivity;
import jie.android.el.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;

public class NotificationSetter {
	
//	private final int NID	=	1;
	
	private Context context = null;
	private NotificationManager manager = null;
	
	private int nid = 0;
	
	private HashSet<Integer> idSet = new HashSet<Integer>();
	
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
		
		if (type == NotificationType.PLAY) {
			nt = buildPlayNotification(builder, title, text);
		} else if (type == NotificationType.IMPORT) {
			nt = buildImportNotification(builder, title, text);
		} else if (type == NotificationType.WARNING) {
			nt = buildWarningNotification(builder, title, text);
		} else {
			return -1;
		}
		
		manager.notify(++ nid, nt);
		
		return nid;
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
	
	public void remove(int id) {
		manager.cancel(id);
	}
	
}
