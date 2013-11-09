package jie.android.el.service.receiver;

import jie.android.el.CommonConsts.AudioNavigateData;
import jie.android.el.CommonConsts.PlayState;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.CommonConsts.UpdateAudioType;
import jie.android.el.CommonConsts.WidgetAction;
import jie.android.el.ELActivity;
import jie.android.el.R;
import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.utils.Utils;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
	private static String audioTitle = "<No Audio>";
	private static int audioNavigate = AudioNavigateData.DISABLE;
	
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
		
		setPendingIntent(context, remoteViews, R.id.imageView1, WidgetAction.ACTION_STARTACTIVITY);
		
		remoteViews.setImageViewResource(R.id.playImageView4, isPlaying ? R.drawable.el_audio_pause : R.drawable.el_audio_play);
		remoteViews.setTextViewText(R.id.textView1, audioTitle);	

		setPendingIntent(context, remoteViews, R.id.playImageView1, WidgetAction.ACTION_NAVIGATE);		
		setPendingIntent(context, remoteViews, R.id.playImageView2, WidgetAction.ACTION_RANDOMMODE);		
		setPendingIntent(context, remoteViews, R.id.playImageView3, AudioAction.ACTION_AUDIO_PREV);
		setPendingIntent(context, remoteViews, R.id.playImageView4, AudioAction.ACTION_AUDIO_PLAY);
		setPendingIntent(context, remoteViews, R.id.playImageView5, AudioAction.ACTION_AUDIO_NEXT);
		
		if (audioNavigate != AudioNavigateData.DISABLE) {
			if ((audioNavigate & AudioNavigateData.SLOWDIALOG) == AudioNavigateData.SLOWDIALOG) {
				remoteViews.setViewVisibility(R.id.textView2, View.VISIBLE);
				setNavigatePendingIntent(context, remoteViews, R.id.textView2, AudioNavigateData.SLOWDIALOG);
			} else {
				remoteViews.setViewVisibility(R.id.textView2, View.GONE);
			}
			
			if ((audioNavigate & AudioNavigateData.EXPLANATION) == AudioNavigateData.EXPLANATION) {
				remoteViews.setViewVisibility(R.id.textView3, View.VISIBLE);
				setNavigatePendingIntent(context, remoteViews, R.id.textView3, AudioNavigateData.EXPLANATION);
			} else {
				remoteViews.setViewVisibility(R.id.textView3, View.GONE);
			}
			
			if ((audioNavigate & AudioNavigateData.FASTDIALOG) == AudioNavigateData.FASTDIALOG) {
				remoteViews.setViewVisibility(R.id.textView4, View.VISIBLE);
				setNavigatePendingIntent(context, remoteViews, R.id.textView4, AudioNavigateData.FASTDIALOG);
			} else {
				remoteViews.setViewVisibility(R.id.textView4, View.GONE);
			}			
		} else {
			showNavigator = false;
		}
		
		SharedPreferences prefs = Utils.getSharedPreferences(context);
		if (prefs.getBoolean(Setting.PLAY_RANDOM_ORDER, false)) {
			remoteViews.setImageViewResource(R.id.playImageView2, R.drawable.el_audio_shuffle_sel);
		} else {
			remoteViews.setImageViewResource(R.id.playImageView2, R.drawable.el_audio_shuffle);
		}

		remoteViews.setViewVisibility(R.id.linearLayout2, showNavigator ? View.VISIBLE : View.GONE);

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
		String action = null;
		if (value == AudioNavigateData.SLOWDIALOG) {
			action = AudioAction.ACTION_AUDIO_NAVIGATE_SLOWDIALOG;
		} else if (value == AudioNavigateData.EXPLANATION) {
			action = AudioAction.ACTION_AUDIO_NAVIGATE_EXPLANATION;
		} else if (value == AudioNavigateData.FASTDIALOG) {
			action = AudioAction.ACTION_AUDIO_NAVIGATE_FASTDIALOG;
		} else {
			return;
		}

		Intent intent = new Intent(action);
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
//		Log.d(Tag, "onReceive - " + intent.getAction());
		super.onReceive(context, intent);
		
		final String action = intent.getAction();
		if (action.equals(WidgetAction.ACTION_SERVICE_INIT)) {
			queryPlayState(context);
		} else if (action.equals(WidgetAction.ACTION_NAVIGATE)) {
			showNavigator = !showNavigator;
			updateRemoteView(context);
 		} else if (action.startsWith(AudioAction.ACTION_AUDIO_NAVIGATE)) {
 			showNavigator = false;
 			updateRemoteView(context);
 		} else if (action.equals(AudioAction.ACTION_UPDATE_AUDIO)) {
			showNavigator = false;

 			int type = intent.getIntExtra(AudioAction.DATA_TYPE, -1);
 			if (type == UpdateAudioType.STATE_CHANGED.getId()) {
 				onStateChange(context, intent);
 			} else if (type == UpdateAudioType.AUDIO_CHANGED_OPEN.getId()) {
 				onAudioChanged(context, true, intent);
 			} else if (type == UpdateAudioType.AUDIO_CHANGED_CLOSE.getId()) {
 				onAudioChanged(context, false, intent);
 			}
 		} else if (action.equals(WidgetAction.ACTION_RANDOMMODE)) {
			showNavigator = false;
 			
 			onRandomModeChanged(context, intent);
 		} else if (action.equals(WidgetAction.ACTION_STARTACTIVITY)) {
			showNavigator = false;

 			Intent sa = new Intent(context, ELActivity.class);
 			sa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(sa);
 		}
	}

	private void onAudioChanged(Context context, boolean open, Intent intent) {
		if (open) {
			audioTitle = intent.getStringExtra(AudioAction.DATA_TITLE);
			audioNavigate = intent.getIntExtra(AudioAction.DATA_NAVIGATE, AudioNavigateData.DISABLE);
			onStateChange(context, intent);
		} else {
			audioTitle = context.getString(R.string.el_show_noaudio);
			showNavigator = false; 
			isPlaying = false;
			audioNavigate = AudioNavigateData.DISABLE; 
		}
		updateRemoteView(context);
	}

	private void onStateChange(Context context, Intent intent) {
		int state = intent.getIntExtra(AudioAction.DATA_STATE, -1);
		if (state == PlayState.PLAY.getId()) {
			if (!isPlaying) {
				isPlaying = true;
				updateRemoteView(context);
			}
		} else {
			if (isPlaying) {
				isPlaying = false;
				updateRemoteView(context);
			}			
		}
	}

	private void queryPlayState(Context context) {
		Intent intent = new Intent(AudioAction.ACTION_AUDIO_QUERY);
		context.sendBroadcast(intent);
	}

	private void onRandomModeChanged(Context context, Intent intent) {
		SharedPreferences prefs = Utils.getSharedPreferences(context);
		boolean random = prefs.getBoolean(Setting.PLAY_RANDOM_ORDER, false);
		prefs.edit().putBoolean(Setting.PLAY_RANDOM_ORDER, !random).commit();
		updateRemoteView(context);
	}
	
}
