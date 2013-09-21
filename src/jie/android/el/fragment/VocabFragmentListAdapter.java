package jie.android.el.fragment;

import java.util.ArrayList;
import java.util.List;

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

public final class VocabFragmentListAdapter {
	
	public interface OnRefreshListener {
		public void onLoadEnd(int count, int total, int maxPerPage);
	}
	
	protected interface VocabListAdapter {
		public void load(String filter, String order);
		public void refresh();
	}
	
	public static class FlatListAdapter extends BaseAdapter implements VocabListAdapter {

		private final String[] projection = new String[] { "word", "lesson_index" };
		
		public final class Data {
			public String word;
			public int lesson;
			
			public Data(String word, int lesson) {
				this.word = word;
				this.lesson = lesson;
			}
		}
		
		private final class LoadTask extends AsyncTask<String, Void, List<Data>> {

			@Override
			protected List<Data> doInBackground(String... params) {
				
				ArrayList<Data> ret = new ArrayList<Data>();
				Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_EL_SCORE, projection, params[0], null, params[1]);
				try {
					if (cursor.moveToFirst()) {
						do {
							ret.add(new Data(cursor.getString(0), cursor.getInt(1)));
						} while (cursor.moveToNext());
					}
				} finally {
					cursor.close();
				}
				
				return ret;
			}

			@Override
			protected void onPostExecute(List<Data> result) {
				dataArray.addAll(result);
				FlatListAdapter.this.notifyDataSetChanged();
				if (listener != null) {
					listener.onLoadEnd(result.size(), dataArray.size(), maxPerPage);
				}
			}
		}		
		
		private Context context;
		private ArrayList<Data> dataArray = new ArrayList<Data>();
		private int loadCount;
		private int maxPerPage;
		
		private LoadTask loadTask;
		private OnRefreshListener listener;

		public FlatListAdapter(Context context) {
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
			return position;
		}
		
		public final String getItemText(int position) {
			return dataArray.get(position).word;
		}		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.layout_vocab_item, parent, false);
			}
			
			Data data = dataArray.get(position);
			
			TextView tv = (TextView) convertView.findViewById(R.id.textView1);
			tv.setText(data.word);
			
			tv = (TextView) convertView.findViewById(R.id.textView2);
			tv.setText(String.valueOf(data.lesson));
			
			return convertView;
		}
		
		public void setOnRefreshListener(OnRefreshListener l) {
			listener = l;
		}
		
		public void setMaxPerPage(int max) {
			maxPerPage = max;
		}
		
		@Override
		public void refresh() {
			if (loadTask != null && loadTask.getStatus() != AsyncTask.Status.FINISHED) {
				loadTask.cancel(true);
			}
			
			loadTask = new LoadTask();
			loadTask.execute(null, "word limit " + maxPerPage * ( ++ loadCount) + " offset " + dataArray.size());			
		}

		@Override
		public void load(String filter, String order) {
			loadCount = 0;
			dataArray.clear();
			
			refresh();
		}
	}
}
