package jie.android.el.service.receiver;

import jie.android.el.CommonConsts.WidgetAction;
import jie.android.el.R;
import jie.android.el.CommonConsts.AudioAction;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class ELWidgetProvider extends AppWidgetProvider {

	private static final String Tag = ELWidgetProvider.class.getSimpleName();
	
//	private AppWidgetManager widgetManager = null;
//	private static int widgetId[] = {};
	private static RemoteViews remoteViews = null;
	private static boolean showNavigator = false; 
	private static boolean isPlaying = false;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(Tag, "onUpate()");
		
		int size = appWidgetIds.length;
		for (int i = 0; i < size; ++ i) {
			bindRemoteView(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	private void startService(Context context) {
		Intent intent = new Intent("jie.android.el.elservice");
		if (this.peekService(context, intent) == null) {
			context.startService(intent);
		}
	}

	private void bindRemoteView(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		
//		widgetManager = AppWidgetManager.getInstance(context);
		if (remoteViews == null) {
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
		}
//		widgetId = widgetManager.getAppWidgetIds(new ComponentName(context, ELWidgetProvider.class));

		setPendingIntent(context, remoteViews, R.id.playImageView1, WidgetAction.ACTION_NAVIGATE);		
		setPendingIntent(context, remoteViews, R.id.playImageView2, WidgetAction.ACTION_RANDOMMODE);		
		setPendingIntent(context, remoteViews, R.id.playImageView3, AudioAction.ACTION_AUDIO_PREV);
		setPendingIntent(context, remoteViews, R.id.playImageView4, AudioAction.ACTION_AUDIO_PLAY);
		setPendingIntent(context, remoteViews, R.id.playImageView5, AudioAction.ACTION_AUDIO_NEXT);
		
		remoteViews.setViewVisibility(R.id.linearLayout2, showNavigator ? View.VISIBLE : View.GONE);
		if (showNavigator) {
			setNavigatePendingIntent(context, remoteViews, R.id.textView2, 0);
			setNavigatePendingIntent(context, remoteViews, R.id.textView3, 1);
			setNavigatePendingIntent(context, remoteViews, R.id.textView4, 2);
		}

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	private void updateRemoteView(Context context) {
		
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		int[] widgetId = widgetManager.getAppWidgetIds(new ComponentName(context, ELWidgetProvider.class));
		
		onUpdate(context, widgetManager, widgetId);
	}
	
	private void setPendingIntent(Context context, RemoteViews view, int id, String action) {
		Intent intent = new Intent(action);
		PendingIntent castIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		view.setOnClickPendingIntent(id, castIntent);
	}
	
	private void setNavigatePendingIntent(Context context, RemoteViews view, int id, int value) {
		Intent intent = new Intent(AudioAction.ACTION_AUDIO_NAVIGATE);
		intent.putExtra(AudioAction.DATA_NAVIGATE, value);
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
		Log.d(Tag, "onReceive - " + intent.getAction());
		super.onReceive(context, intent);
		
		final String action = intent.getAction();
		if (action.equals(WidgetAction.ACTION_SERVICE_INIT)) {
			queryPlayState(context);
		} else if (action.equals(WidgetAction.ACTION_NAVIGATE)) {
			showNavigator = !showNavigator;
			updateRemoteView(context);
 		} else if (action.equals(AudioAction.ACTION_AUDIO_NAVIGATE)) {
 			showNavigator = false;
 			updateRemoteView(context);
 		}		
	}

	private void queryPlayState(Context context) {
		Intent intent = new Intent(AudioAction.ACTION_AUDIO_QUERY);
		context.sendBroadcast(intent);
	}

}
