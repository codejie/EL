package jie.android.el;

import jie.android.el.database.DBAccess;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ELActivity extends SherlockFragmentActivity {

	private static String Tag = ELActivity.class.getSimpleName();
	
	private DBAccess dbAccess = null;
	
	private FragmentSwitcher fragmentSwitcher = null;
	
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			fragmentSwitcher.show(FragmentSwitcher.Type.LIST);
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fragmentSwitcher = new FragmentSwitcher(this);
		
		this.setContentView(R.layout.activity_el);

		initDatabase();
		
		handler.sendEmptyMessage(0);
		
//		fragmentSwitcher.show(FragmentSwitcher.Type.LIST);
	}

	private void initDatabase() {
		dbAccess = new DBAccess(this);
		if(!dbAccess.open()) {
			Log.e(Tag, "init database failed.");
		}
	}

	public DBAccess getDBAccess() {
		return dbAccess;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

}
