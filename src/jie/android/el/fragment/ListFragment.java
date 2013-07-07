package jie.android.el.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import jie.android.el.BaseFragment;
import jie.android.el.R;

public class ListFragment extends BaseFragment {
	
	private static final int LOADER_LIST	=	1;
	
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

		this.getLoaderManager().initLoader(LOADER_LIST, savedInstanceState, loadCallbacks);		
	}	
	
}
