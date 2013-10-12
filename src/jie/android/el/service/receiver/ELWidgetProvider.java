package jie.android.el.service.receiver;

import jie.android.el.R;
import jie.android.el.CommonConsts.AudioAction;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class ELWidgetProvider extends AppWidgetProvider {

	private static final String Tag = ELWidgetProvider.class.getSimpleName();
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(Tag, "onUpate()");
		
		startService(context);
		
		bindRemoteView(context, appWidgetManager, appWidgetIds);
	}

	private void startService(Context context) {
		Intent intent = new Intent("jie.android.el.elservice");
		if (this.peekService(context, intent) == null) {
			context.startService(intent);
		}
	}

	private void bindRemoteView(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

		setPendingIntent(context, view, R.id.playImageView5, AudioAction.ACTION_AUDIO_NEXT);		
		
		appWidgetManager.updateAppWidget(appWidgetIds[0], view);
	}

	private void setPendingIntent(Context context, RemoteViews view, int id, String action) {
		Intent intent = new Intent(action);
		PendingIntent castIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		view.setOnClickPendingIntent(id, castIntent);
	}	
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
	}
	
}
