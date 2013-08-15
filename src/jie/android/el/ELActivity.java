package jie.android.el;

import java.io.File;
import java.io.IOException;

import jie.android.el.fragment.BaseFragment;
import jie.android.el.service.ServiceAccess;
import jie.android.el.service.ServiceNotification;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.Utils;
import jie.android.el.utils.XmlTranslator;
import jie.android.el.CommonConsts.AppArgument;
import jie.android.el.CommonConsts.FragmentArgument;
import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.CommonConsts.ServiceState;
import jie.android.el.CommonConsts.UIMsg;
import jie.android.el.R;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

	private ServiceAccess serviceAccess = null;
//	private PackageImporter packageImporter = null;
	
	private FragmentSwitcher fragmentSwitcher = null;
	
	private ProgressDialog progressDialog = null;
	
	private Menu actionMenu = null;
	private SharedPreferences sharedPreferences = null;	
 
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UIMsg.UI_CREATED:
				showProgressDialog(true, "Connecting to service...");
				break;
			case UIMsg.SERVICE_NOTIFICATION:
				onServiceNotification(msg.arg1);
				break;
			case UIMsg.SERVICE_AUDIOPLAYING:
				onServiceAudioPlaying((Bundle)msg.obj);
				break;

			default:;
			}
		}		
	};
	
	private ServiceNotification serviceNotification = new ServiceNotification.Stub() {
		
		@Override
		public void onServiceState(int state) throws RemoteException {
			Message msg = Message.obtain(handler, UIMsg.SERVICE_NOTIFICATION, state, -1);
			msg.sendToTarget();
		}
		
		@Override
		public void onAudioPlaying(int index, int duration, int position) throws RemoteException {
			Bundle data = new Bundle();
			data.putInt(FragmentArgument.INDEX, index);
			data.putInt("duration", duration);
			data.putInt("position", position);
			
			Message msg = Message.obtain(handler, UIMsg.SERVICE_AUDIOPLAYING, data);
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
		
		checkPath();
		
		sharedPreferences = getSharedPreferences(AppArgument.NAME, 0);
		
		fragmentSwitcher = new FragmentSwitcher(this);
		
		this.setContentView(R.layout.activity_el);

		initService();
		initSpeaker();
		initTranslator();
//		initPackageImporter();
	
		handler.sendEmptyMessage(UIMsg.UI_CREATED);
	}
	
	private boolean checkPath() {
		String sdcard = Utils.getExtenalSDCardDirectory();
		File f = new File(sdcard + AppArgument.PATH_ROOT);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				Log.e(Tag, "make directory '"+ f.getAbsolutePath() + "' failed.");
				return false;
			}
		}

		f = new File(sdcard + AppArgument.PATH_EL);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				Log.e(Tag, "make directory '"+ f.getAbsolutePath() + "' failed.");
				return false;
			}
		}
		
		f = new File(sdcard + AppArgument.PATH_CACHE);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				Log.e(Tag, "make directory '"+ f.getAbsolutePath() + "' failed.");
				return false;
			}
		}
		
		return true;
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
	
//	private void initPackageImporter() {
//		final String[] res = PackageImporter.check();
//		if (res != null && res.length > 0) {
//			packageImporter = new PackageImporter(this, res);
//			packageImporter.startImport();
//		}
//	}
		
	public ServiceAccess getServiceAccess() {
		return serviceAccess;
	}

	public Handler getHandler() {
		return handler;
	}
	
	public SharedPreferences getSharedPreferences() {
		return sharedPreferences;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		getSupportMenuInflater().inflate(R.menu.activity_el, menu);
		
		actionMenu  = menu;
		
		return true;
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
		case R.id.el_menu_about:
			showFragment(FragmentSwitcher.Type.ABOUT, null);
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
			if (!fragmentSwitcher.showPrevFragment()) {
//				releaseService(true);
				finish();
				return true;
			}
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			actionMenu.performIdentifierAction(R.id.item1, 0);
			return true;
		}		
		return super.onKeyUp(keyCode, event);
	}	

	protected void onServiceAudioPlaying(Bundle data) {
		data.putInt(FragmentArgument.ACTION, FragmentArgument.Action.SERVICE_NOTIFICATION.getId());
		fragmentSwitcher.show(FragmentSwitcher.Type.SHOW, data);
	}

	protected void onServiceNotification(int state) {
		if (state == ServiceState.READY.getId()) {
			showProgressDialog(false, null);
			fragmentSwitcher.show(FragmentSwitcher.Type.LIST);
		} else {
			
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

	public int showNotification(NotificationType type, final String title, final String text) {
		if (serviceAccess != null) {
			try {
				return serviceAccess.setNotification(type.getId(), title, text);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public void removeNotification(NotificationType type, int id) {
		if (serviceAccess != null) {
			try {
				serviceAccess.removeNotification(type.getId(), id);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
