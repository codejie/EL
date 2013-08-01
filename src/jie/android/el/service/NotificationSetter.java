package jie.android.el.service;

import jie.android.el.ELActivity;
import jie.android.el.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class NotificationSetter {
	
	private final int NID	=	1;
	
	private ELService service = null;
	private NotificationManager manager = null;
	
	public NotificationSetter(ELService service) {
		this.service = service;
		
		manager = (NotificationManager) this.service.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void post(int index, final String title) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(service);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentText("EL is playing..");
		builder.setContentTitle(String.format("%d - %s", index, title));

		Intent result = new Intent(service, ELActivity.class);
		PendingIntent intent = PendingIntent.getActivity(service, 0, result, 0);

		builder.setContentIntent(intent);
		
		manager.notify(NID, builder.build());
	}
	
	public void remove() {
		manager.cancel(NID);
	}
	
}
