package jie.android.el.service.receiver;

import jie.android.el.CommonConsts.BroadcastAction;
import jie.android.el.service.ELService;
import jie.android.el.service.ServiceAccess;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ServiceReceiver extends BroadcastReceiver {

	private static final String Tag = ServiceReceiver.class.getSimpleName();
	
	private static ServiceAccess service;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (action.equals(BroadcastAction.ACTION_SERVICE_INIT)) {
			Intent si = new Intent("elService");
			IBinder binder = this.peekService(context, si);
			if (binder != null) {
				service = ServiceAccess.Stub.asInterface(binder);
				Log.d(Tag, "server init succ.");
			} else {
				Log.e(Tag, "cannot get service interface.");
			}
		} else {
			
		}
	}

}
