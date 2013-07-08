package jie.android.el.fragment;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import jie.android.el.BaseFragment;
import jie.android.el.R;

public class ListFragment extends BaseFragment {
	
	class Adapter extends CursorAdapter {

		private HashMap<Integer, Integer> idxMap = new HashMap<Integer, Integer>();
		
		private LayoutInflater inflater = null;
		public Adapter(Context context, Cursor c) {
			super(context, c, true);
			
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return inflater.inflate(R.layout.layout_record, parent, false); 
		}
		
	}

	
	
	private static final int LOADER_LIST	=	1;
	
	private ListView listView = null;
	
	private LoaderCallbacks<Cursor> loadCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			return null;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
		}
		
	};
	
	public ListFragment() {
		this.setLayoutRes(R.layout.fragment_list);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView) view.findViewById(R.id.listView1);
		
		this.getLoaderManager().initLoader(LOADER_LIST, savedInstanceState, loadCallbacks);
		
	}	
	
}
