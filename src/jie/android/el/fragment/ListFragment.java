package jie.android.el.fragment;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import jie.android.el.CommonConsts.FragmentArgument;
import jie.android.el.CommonConsts.ListItemFlag;
import jie.android.el.FragmentSwitcher;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.Utils;

public class ListFragment extends BaseFragment implements OnItemClickListener {
	
	private static String[] ItemProjects = new String[] { "idx as _id", "title", "duration", "flag" }; 

	class Adapter extends CursorAdapter {

		//private HashMap<Integer, Integer> idxMap = new HashMap<Integer, Integer>();
		
		private LayoutInflater inflater = null;
		public Adapter(Context context, Cursor c) {
			super(context, c, true);
			
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			TextView idx = (TextView) view.findViewById(R.id.textIndex);
			idx.setText("ESL Podcast " + cursor.getString(0));
			
			TextView title = (TextView) view.findViewById(R.id.textTitle);
			title.setText(cursor.getString(1));
			
			TextView duration = (TextView) view.findViewById(R.id.textDuration);
			duration.setText(Utils.formatMSec(cursor.getInt(2) * 1000));
			
			View icon = view.findViewById(R.id.imageView1);
			if ((cursor.getInt(3) & ListItemFlag.LAST_PLAY) == ListItemFlag.LAST_PLAY) {
				icon.setVisibility(View.VISIBLE);
			} else {
				icon.setVisibility(View.INVISIBLE);
			}
			
			view.setId(cursor.getInt(0));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return inflater.inflate(R.layout.layout_list_item, parent, false); 
		}
	}

	private class ContextChangedObserver extends ContentObserver {

		public ContextChangedObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			if (!selfChange) {
				adapter.changeCursor(getELActivity().getContentResolver().query(ELContentProvider.URI_EL_ESL, ItemProjects, null,  null, "idx"));
			}
		}		
	}
	
	private ListView listView = null;
	private Adapter adapter = null;
	private ContextChangedObserver changedObserver = new ContextChangedObserver();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_list);
	}	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		adapter = new Adapter(getActivity(), getELActivity().getContentResolver().query(ELContentProvider.URI_EL_ESL, ItemProjects, null,  null, "idx"));
		
		listView = (ListView) view.findViewById(R.id.listView1);
		listView.setOnItemClickListener(this);
		
		listView.setAdapter(adapter);
		
//		if (adapter.getCount() == 0) {
//			loadBundleData();
//		}
	}

//	private void loadBundleData() {
//		getELActivity().getHandler().sendEmptyMessage(CommonConsts.UIMsg.UI_LOAD_BUNDLEDPACKAGE);
//	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Bundle args = new Bundle();
		args.putInt(FragmentArgument.ACTION, FragmentArgument.Action.PLAY.getId());
		args.putInt(FragmentArgument.INDEX, (int) id);
		getELActivity().showFragment(FragmentSwitcher.Type.SHOW, args);
	}

	@Override
	public void onArguments(Bundle args) {
		if (args == null)
			return;
		
		if (args.getInt(FragmentArgument.ACTION, FragmentArgument.Action.NONE.getId()) == FragmentArgument.Action.PACKAGE_CHANGED.getId()) {
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onPause() {
		getELActivity().getContentResolver().unregisterContentObserver(changedObserver);
		super.onPause();		
	}

	@Override
	public void onResume() {
		super.onResume();		
		getELActivity().getContentResolver().registerContentObserver(ELContentProvider.URI_EL_ESL, true, changedObserver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			getELActivity().finish();
			return true;
		}
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}	
	
}
