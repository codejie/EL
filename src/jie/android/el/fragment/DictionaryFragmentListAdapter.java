package jie.android.el.fragment;

import java.util.ArrayList;

import jie.android.el.ELActivity;
import jie.android.el.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DictionaryFragmentListAdapter extends BaseAdapter {

	private final class Data {
		public int index = -1;
		public String text = null;
	}
	
	private Context context = null;
	private ArrayList<Data> dataArray = new ArrayList<Data>();
	
	
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

}
