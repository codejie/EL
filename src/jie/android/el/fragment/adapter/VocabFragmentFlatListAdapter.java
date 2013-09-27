package jie.android.el.fragment.adapter;

import java.util.ArrayList;
import java.util.List;

import jie.android.el.R;
import jie.android.el.database.ELContentProvider;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class VocabFragmentFlatListAdapter extends VocabFragmentListAdapter {

	private final String[] projection = new String[] { "word"/* , "lesson_index" */};

	public final class Data {
		public String word;

		public Data(String word) {
			this.word = word;
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
						ret.add(new Data(cursor.getString(0)));
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
			notifyDataSetChanged();

			if (listener != null) {
				listener.onLoadEnd(result.size(), dataArray.size(), maxPerPage);
			}
		}
	}

	private ArrayList<Data> dataArray = new ArrayList<Data>();
	private int loadCount;
	private int maxPerPage;

	private LoadTask loadTask;

	public VocabFragmentFlatListAdapter(Context context) {
		super(context);
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.layout_vocab_item, parent, false);
		}

		final Data data = dataArray.get(position);

		TextView tv = (TextView) convertView.findViewById(R.id.textView1);
		tv.setText(data.word);

		ImageButton btn = (ImageButton) convertView.findViewById(R.id.imageButton1);
		if (isEditable) {
			btn.setVisibility(View.VISIBLE);
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					RemoveWord(data.word, position);
					if (listener != null) {
						listener.onItemRemoved(ItemType.WORD, data.word);
					}
				}
			});
		} else {
			btn.setVisibility(View.GONE);
			btn.setOnClickListener(null);
		}

		return convertView;
	}

	protected void RemoveWord(String word, int position) {
		context.getContentResolver().delete(ELContentProvider.URI_EL_SCORE, "word=?", new String[] { word });
		dataArray.remove(position);
		notifyDataSetChanged();
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
		if (sortMode == SORT_BY_ALPHA) {
			loadTask.execute(null, "word limit " + maxPerPage * (++loadCount) + " offset " + dataArray.size());
		} else {
			loadTask.execute(null, "score limit " + maxPerPage * (++loadCount) + " offset " + dataArray.size());
		}
	}

	@Override
	public void load(String filter) {
		loadCount = 0;
		dataArray.clear();

		refresh();
	}
}
