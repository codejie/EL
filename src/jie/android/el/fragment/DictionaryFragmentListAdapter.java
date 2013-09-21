package jie.android.el.fragment;

import java.util.ArrayList;
import java.util.List;

import jie.android.el.ELActivity;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DictionaryFragmentListAdapter extends BaseAdapter {

	public static interface OnRefreshListener {
		public void onLoadEnd(int count, int total, int maxPerPage);
	}

	private final class LoadTask extends AsyncTask<String, Void, List<Data>> {

		@Override
		protected List<Data> doInBackground(String... params) {
			
			Log.d("====", "do..." + params[0]);
			
			ArrayList<Data> result = new ArrayList<Data>();
			Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_LAC_WORD_INFO, projection, params[0], null, params[1]);
			try {
				if (cursor.moveToFirst()) {
					do {
						result.add(new Data(cursor.getInt(0), cursor.getString(1)));
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(List<Data> result) {
			dataArray.addAll(result);
			DictionaryFragmentListAdapter.this.notifyDataSetChanged();
			if (onRefreshListener != null) {
				onRefreshListener.onLoadEnd(result.size(), dataArray.size(), maxPerPage);
			}
		}		
	}

	private static final String[] projection = new String[] {"idx", "word" };
	
	private final class Data {
		
		public int index = -1;
		public String text = null;
//		public int flag = 0;

		public Data(int idx, String txt) { //, int flag) {
			index = idx;
			text = txt;
//			this.flag = flag;
		}
		
//		public boolean isNormal() {
//			return flag != 2;
//		}
//		
//		public boolean isReference() {
//			return flag != 1;
//		}
	}
	
	private Context context = null;
	private ArrayList<Data> dataArray = new ArrayList<Data>();

	private OnRefreshListener onRefreshListener = null;
	private int maxPerPage = -1;
	private String filter = null;
	
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
	
	public final String getItemText(int position) {
		return dataArray.get(position).text;
	}

	@Override
	public View getView(int position, View view, ViewGroup group) {
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.layout_dictionary_item, group, false);
		}
		
		Data data = dataArray.get(position); 
		
		view.setId(data.index);
		
		TextView tv = (TextView)view.findViewById(R.id.textView1);
		tv.setText(data.text);
		
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
		loadCount = 0;
		dataArray.clear();
		
		refresh();
	}

	public void refresh() {
		
		if (loadTask != null && loadTask.getStatus() != AsyncTask.Status.FINISHED) {
			loadTask.cancel(true);
		}
		
		loadTask = new LoadTask();
		loadTask.execute(filter, "idx limit " + maxPerPage * ( ++ loadCount) + " offset " + dataArray.size());
	}
	
	
}
