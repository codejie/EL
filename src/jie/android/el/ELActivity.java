package jie.android.el;

import java.io.IOException;

import jie.android.el.database.ELDBAccess;
import jie.android.el.fragment.BaseFragment;
import jie.android.el.service.ServiceAccess;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.XmlTranslator;
import jie.android.el.R;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ELActivity extends SherlockFragmentActivity {

	private static String Tag = ELActivity.class.getSimpleName();
	
	private ELDBAccess dbAccess = null;
	private ServiceAccess serviceAccess = null;
	
	private FragmentSwitcher fragmentSwitcher = null;
	
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {			
			fragmentSwitcher.show(FragmentSwitcher.Type.LIST);
			//fragmentSwitcher.show(FragmentSwitcher.Type.SHOW);
		}		
	};
	
	ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			serviceAccess = ServiceAccess.Stub.asInterface(binder);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
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
		initDatabase();
		
		handler.sendEmptyMessage(0);
		
//		fragmentSwitcher.show(FragmentSwitcher.Type.LIST);
	}

	@Override
	protected void onDestroy() {
		releaseDatabase();
		releaseSpeasker();
		releaseService();
		
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
		
//		this.startService(intent);
		
		this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private void releaseService() {
		if (serviceAccess != null) {
			//this.unbindService(serviceConnection);
		}
	}
	
	private void initDatabase() {
		dbAccess = new ELDBAccess(this);
		if(!dbAccess.open()) {
			Log.e(Tag, "init database failed.");
		}
	}
	
	private void releaseDatabase() {
		if (dbAccess != null) {
			dbAccess.close();
		}
	}

	public ELDBAccess getDBAccess() {
		return dbAccess;
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}
	 
}
