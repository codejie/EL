package jie.android.el.fragment;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

import jie.android.el.R;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.fragment.VocabFragment.VocabListAdapter.Data;
import jie.android.el.view.ELPopupWindow;
import jie.android.el.view.OnPopupWindowDefaultListener;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class VocabFragment extends BaseFragment implements OnItemClickListener, OnRefreshListener<ListView> {

	public final class VocabListAdapter extends BaseAdapter {

		public final class Data {
			public String word;
			public int lesson;
			
			public Data(String word, int lesson) {
				this.word = word;
				this.lesson = lesson;
			}
		}
		
		private final class LoadTask extends AsyncTask<Integer, Void, List<Data>> {

			@Override
			protected List<Data> doInBackground(Integer... params) {
				
				
				return null;
			}

			@Override
			protected void onPostExecute(List<Data> result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
			}
			
		}
		
		
		private Context context;
		private ArrayList<Data> dataArray = new ArrayList<Data>();

		public VocabListAdapter(Context context) {
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.layout_vocab_item, false);
			}
			
			Data data = dataArray.get(position);
			
			TextView tv = (TextView) convertView.findViewById(R.id.textView1);
			tv.setText(data.word);
			
			tv = (TextView) convertView.findViewById(R.id.textView2);
			tv.setText(String.valueOf(data.lesson));
			
			return null;
		}
		
	}
	
	private PullToRefreshListView pullList = null;
	
	private LinearLayout footLayout = null;
	private TextView footText = null;
	
	private DictionaryFragmentListAdapter adapter = null;
	
	private Animation animShow = null;
	private Animation animHide = null;
	
	private ELPopupWindow popWindow;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_vocab);
		this.setMenuRes(R.menu.fragment_vocab);
		
		initAnimation();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		initList(view);
		
		initPopWindow(view);		
	}

	private void initAnimation() {
    	animShow = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_show);
    	animHide = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_hide);
	}
	
	private void initList(View view) {
		pullList = (PullToRefreshListView) view.findViewById(R.id.pullToRefreshListView1);
		//pullList.getRefreshableView().setDivider(getResources().getDrawable(R.drawable.el_list_divider));
		pullList.setMode(Mode.PULL_FROM_END);
		pullList.getLoadingLayoutProxy().setPullLabel(getText(R.string.el_fragment_dict_pulltorefresh));
		pullList.getLoadingLayoutProxy().setReleaseLabel(getText(R.string.el_fragment_dict_releasetorefresh));
		pullList.getLoadingLayoutProxy().setRefreshingLabel(getText(R.string.el_fragment_dict_loading));
//		pullList.getRefreshableView().setFooterDividersEnabled(true);
		
		pullList.setOnRefreshListener(this);
		pullList.setOnItemClickListener(this);
		
		View v = getELActivity().getLayoutInflater().inflate(R.layout.fragment_dictionary_list_foot, null);
		footLayout = (LinearLayout) v.findViewById(R.id.footLayout);
		footText = (TextView) v.findViewById(R.id.textFoot);
		
		pullList.getRefreshableView().addFooterView(v);
		pullList.getRefreshableView().setFooterDividersEnabled(false);
		
		adapter = new DictionaryFragmentListAdapter(getELActivity());
		pullList.setAdapter(adapter);
		
		adapter.setOnRefrshListener(this);

		final int maxRecord = getELActivity().getSharedPreferences().getInt(Setting.DICTIONARY_LIST_MAXPERPAGE, 2);
		adapter.setMaxPerPage(maxRecord);
		
		pullList.getRefreshableView().setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if ((visibleItemCount + 1) > maxRecord) {
					getELActivity().getSharedPreferences().edit().putInt(Setting.DICTIONARY_LIST_MAXPERPAGE, visibleItemCount + 1).commit();
					adapter.setMaxPerPage(visibleItemCount + 1);	
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}				
		});

		adapter.setMaxPerPage(20);
//		loadAdapter("a");
	}
	
	private void initPopWindow(View view) {
	
		popWindow = (ELPopupWindow) view.findViewById(R.id.eLPopupWindow1);
		popWindow.setOnPopupWindowListener(new OnPopupWindowDefaultListener(popWindow));	
		popWindow.setAnimation(animShow, animHide);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

}
