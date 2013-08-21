package jie.android.el.fragment;

import java.util.ArrayList;

import jie.android.el.ELActivity;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DictionaryFragmentListAdapter extends BaseAdapter {

	public static interface OnRefreshListener {
		public void onLoadEnd(int count, int total, int maxPerPage);
	}

	private final class LoadTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			dataArray.clear();

			Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_LAC_WORD_INFO, project, params[0], null, params[1]);
			try {
				if (cursor.moveToFirst()) {
					do {
						dataArray.add(new Data(cursor.getInt(0), cursor.getString(1)));
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
			
			return dataArray.size();
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			DictionaryFragmentListAdapter.this.notifyDataSetChanged();
			if (onRefreshListener != null) {
				onRefreshListener.onLoadEnd(result.intValue(), dataArray.size(), maxPerPage);
			}
			
			if (isLoading) {
				isLoading = false;
				refresh();
			}
		}		
	}

	private static final String[] project = new String[] {"idx", "word" };
	
	private final class Data {
		
		public int index = -1;
		public String text = null;		

		public Data(int idx, String txt) {
			index = idx;
			text = txt;
		}
	}
	
	private Context context = null;
	private ArrayList<Data> dataArray = new ArrayList<Data>();

	private OnRefreshListener onRefreshListener = null;
	private int maxPerPage = -1;
	private String filter = null;
	
	private boolean isLoading = false;
	private LoadTask loadTask = null;
	private int loadCount = 0;
	
	public DictionaryFragmentListAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return dataArray.size();
	}

	@Override
	public Object getItem(int position) {
		return dataArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return dataArray.get(position).index;
	}

	@Override
	public View getView(int position, View view, ViewGroup group) {
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.layout_dictionary_item, group, false);
		}
		
		Data data = dataArray.get(position); 
		
		view.setId(data.index);
		
		((TextView)view.findViewById(R.id.textView1)).setText(data.text);
		
		return view;
	}

	public void setOnRefrshListener(OnRefreshListener listener) {
		onRefreshListener = listener;
	}
	
	public void setMaxPerPage(int value) {
		maxPerPage = value;
	}
	
	public void load(final String filter) {
		this.filter = filter;

		refresh();
	}

	public void refresh() {
		if (isLoading) {
			return;
		} else if (loadTask != null && loadTask.getStatus() != AsyncTask.Status.FINISHED) {
			isLoading = true;
			return;
		}
		
		loadTask = new LoadTask();
		loadTask.execute(filter, "idx limit " + maxPerPage * ( ++ loadCount));
	}
	
	
}
