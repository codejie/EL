package jie.android.el.fragment;

import java.util.HashMap;

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
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import jie.android.el.ELActivity;
import jie.android.el.R;
import jie.android.el.database.DBAccess;

public class ListFragment extends BaseFragment {
	
	class Adapter extends CursorAdapter {

		//private HashMap<Integer, Integer> idxMap = new HashMap<Integer, Integer>();
		
		private LayoutInflater inflater = null;
		public Adapter(Context context, Cursor c) {
			super(context, c, true);
			
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			TextView tv = (TextView) view.findViewById(R.id.textView1);
			tv.setText(cursor.getString(1));
			view.setId(cursor.getInt(0));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return inflater.inflate(R.layout.layout_record, parent, false); 
		}
	}
	
	private DBAccess dbAccess = null;
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
		
		dbAccess = getELActivity().getDBAccess();

		adapter = new Adapter(getActivity(), dbAccess.queryESL(new String[] { "idx as _id", "title" }, null, null));
		
		listView = (ListView) view.findViewById(R.id.listView1);
		listView.setAdapter(adapter);

	}	
	
}
