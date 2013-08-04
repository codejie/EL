package jie.android.el;

import java.io.IOException;

import jie.android.el.database.ELContentProvider;
import jie.android.el.fragment.BaseFragment;
import jie.android.el.service.ServiceAccess;
import jie.android.el.service.ServiceNotification;
import jie.android.el.service.CommonState;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.XmlTranslator;
import jie.android.el.R;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ELActivity extends SherlockFragmentActivity {

	private static final String Tag = ELActivity.class.getSimpleName();
	
	private static final int MSG_SERVICE_NOTIFICATION	=	1;
	private static final int MSG_SERVICE_AUDIOPLAYING	=	2;
	private static final int MSG_SERVICE_PACKAGE_READY	=	3;
	
	private ServiceAccess serviceAccess = null;
	private PackageImporter packageImporter = null;
	
	private FragmentSwitcher fragmentSwitcher = null;
	
	private ProgressDialog progressDialog = null;	
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SERVICE_NOTIFICATION:
				onServiceNotification(msg.arg1);
				break;
			case MSG_SERVICE_AUDIOPLAYING:
				onServiceAudioPlaying((Bundle)msg.obj);
				break;
			case MSG_SERVICE_PACKAGE_READY:
				OnServicePackageReady();
				break;
			default:;
			}
		}		
	};
	
	private ServiceNotification serviceNotification = new ServiceNotification.Stub() {
		
		@Override
		public void onServiceState(int state) throws RemoteException {
			Message msg = Message.obtain(handler, MSG_SERVICE_NOTIFICATION, state, -1);
			msg.sendToTarget();
		}
		
		@Override
		public void onAudioPlaying(int index, int duration, int position) throws RemoteException {
			Bundle data = new Bundle();
			data.putInt("index", index);
			data.putInt("duration", duration);
			data.putInt("position", position);
			
			Message msg = Message.obtain(handler, MSG_SERVICE_AUDIOPLAYING, data);
			msg.sendToTarget();
		}

		@Override
		public void onPackageReady(long syncid, String file) throws RemoteException {
			Log.d(Tag, "onPackageReady : syncid = " + syncid + " file = " + file);
			Message msg = Message.obtain(handler, MSG_SERVICE_PACKAGE_READY);
			msg.sendToTarget();
		}

	};
	
	ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			serviceAccess = ServiceAccess.Stub.asInterface(binder);
			
			try {
				serviceAccess.regServiceNotification(0, serviceNotification);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			try {
				serviceAccess.unregServiceNotification(0);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serviceAccess = null;
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fragmentSwitcher = new FragmentSwitcher(this);
		
		this.setContentView(R.layout.activity_el);

		initService();
		initSpeaker();
		initTranslator();
		initPackageImporter();
		
	}

	@Override
	protected void onStop() {
		Log.d(Tag, "onStop()");
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(Tag, "onDestroy");
		
		releaseSpeasker();
		releaseService(false);
		
		super.onDestroy();
	}

	private void initSpeaker() {
		Speaker.init(this);
	}
	
	private void releaseSpeasker() {
		Speaker.release();
	}

	private void initTranslator() {
    	try {
			XmlTranslator.init(this.getResources().getAssets().open("ld2.xsl"));
		} catch (IOException e) {
			Log.e(Tag, "init transformer failed.");
		}

	}

	private void initService() {
		Intent intent = new Intent("elService");		
		this.startService(intent);
		
		intent = new Intent("elService");
		this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private void releaseService(boolean stop) {
		if (serviceAccess != null) {
			this.unbindService(serviceConnection);
			if (stop) {
				this.stopService(new Intent("elService"));
			}
			serviceAccess = null;
		}
	}
	
	private void initPackageImporter() {
		final String[] res = PackageImporter.check();
		if (res != null && res.length > 0) {
			packageImporter = new PackageImporter(this, res);
			packageImporter.startImport();
		}
	}	
	
	public ServiceAccess getServiceAccess() {
		return serviceAccess;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_el, menu);
		return super.onPrepareOptionsMenu(menu);
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.el_menu_download:
			showFragment(FragmentSwitcher.Type.DOWNLOAD, null);
			break;
		case R.id.el_menu_setting:
			showFragment(FragmentSwitcher.Type.SETTING, null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showFragment(FragmentSwitcher.Type type, Bundle args) {
		fragmentSwitcher.show(type, args);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		BaseFragment fragment = fragmentSwitcher.getFragment();
		if (fragment != null) {
			if(fragment.onKeyDown(keyCode, event)) {
				return true;
			}
		}
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			releaseService(true);
			finish();
		}
		
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}

	protected void onServiceAudioPlaying(Bundle data) {
		data.putBoolean("serviceNotification", true);
		fragmentSwitcher.show(FragmentSwitcher.Type.SHOW, data);
	}

	protected void onServiceNotification(int state) {
		if (state == CommonState.Service.READY.getId()) {
			showProgressDialog(false, null);
			fragmentSwitcher.show(FragmentSwitcher.Type.LIST);			
		} else if (state == CommonState.Service.UNZIP.getId()) {
			showProgressDialog(true, "Unzip...");
		}
	}

	private void showProgressDialog(boolean show, final String text) {
		if (show) {
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(this);
			}
			
			progressDialog.setMessage(text);
			if (!progressDialog.isShowing()) {
				progressDialog.show();
			}
		} else {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		}
	}

	protected void OnServicePackageReady() {
		if (packageImporter == null) {
			initPackageImporter();			
		} else {
			packageImporter.refresh();
		}
	}	
}
