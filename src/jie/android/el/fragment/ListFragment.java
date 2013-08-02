package jie.android.el.fragment;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import jie.android.el.ELActivity;
import jie.android.el.FragmentSwitcher;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import jie.android.el.database.ELDBAccess;
import jie.android.el.utils.Utils;

public class ListFragment extends BaseFragment implements OnItemClickListener {

	class Adapter extends CursorAdapter {

		//private HashMap<Integer, Integer> idxMap = new HashMap<Integer, Integer>();
		
		private LayoutInflater inflater = null;
		public Adapter(Context context, Cursor c) {
			super(context, c, true);
			
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			TextView idx = (TextView) view.findViewById(R.id.textView2);
			idx.setText(cursor.getString(0));
			
			TextView title = (TextView) view.findViewById(R.id.textView3);
			title.setText(cursor.getString(1));
			
			TextView duration = (TextView) view.findViewById(R.id.textView4);
			duration.setText(Utils.formatMSec(cursor.getInt(2) * 1000));
			
			view.setId(cursor.getInt(0));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return inflater.inflate(R.layout.layout_record, parent, false); 
		}
	}
	
	private ListView listView = null;
	private Adapter adapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_list);
	}	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		Cursor cursor = getELActivity().getContentResolver().query(ELContentProvider.URI_EL_ESL, new String[] {"idx as _id", "title", "duration"}, null,  null, null);
		
		adapter = new Adapter(getActivity(), cursor);
		
		listView = (ListView) view.findViewById(R.id.listView1);
		listView.setOnItemClickListener(this);
		
		listView.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Bundle args = new Bundle();
		args.putInt("index", (int) id);
		args.putInt("position", position);
		args.putInt("total", adapter.getCount());
		getELActivity().showFragment(FragmentSwitcher.Type.SHOW, args);
	}	
	
}
