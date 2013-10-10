package jie.android.el.service.receiver;

import jie.android.el.R;
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
		Intent intent = new Intent("elService");		
		context.startService(intent);
	}

	private void bindRemoteView(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
		view.setTextViewText(R.id.textView1, "null");
		
		appWidgetManager.updateAppWidget(appWidgetIds[0], view);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
	}
	
}
