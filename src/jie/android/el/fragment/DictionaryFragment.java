package jie.android.el.fragment;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import jie.android.el.CommonConsts.FragmentArgument;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.R;

public class DictionaryFragment extends BaseFragment implements OnRefreshListener<ListView>, OnItemClickListener {

	private PullToRefreshListView pullList = null;
	private LinearLayout footLayout = null;
	private TextView footText = null;
	
	private DictionaryFragmentListAdapter adapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_dictionary);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		initList(view);		
	}

	private void initList(View view) {
		pullList = (PullToRefreshListView) view.findViewById(R.id.pullToRefreshListView1);
		pullList.setMode(Mode.PULL_FROM_END);
		
		pullList.setOnRefreshListener(this);
		pullList.setOnItemClickListener(this);
		
		View v = getELActivity().getLayoutInflater().inflate(R.layout.fragment_dictionary_list_foot, null);
		footLayout = (LinearLayout) v.findViewById(R.id.footLayout);
		footText = (TextView) v.findViewById(R.id.textFoot);
		
		pullList.getRefreshableView().addFooterView(v);
		pullList.getRefreshableView().setFooterDividersEnabled(false);
		
		adapter = new DictionaryFragmentListAdapter(getELActivity());
		pullList.setAdapter(adapter);

		int maxRecord = getELActivity().getSharedPreferences().getInt(Setting.DICTIONARY_LIST_MAXPERPAGE, -1); 
		
		if (maxRecord == -1) {
			pullList.getRefreshableView().setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					getELActivity().getSharedPreferences().edit().putInt(Setting.DICTIONARY_LIST_MAXPERPAGE, visibleItemCount + 1);
					adapter.setMaxPerPage(visibleItemCount + 1);
					pullList.getRefreshableView().setOnScrollListener(null);
				}

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
				}				
			});
			adapter.setMaxPerPage(35);
		} else {
			adapter.setMaxPerPage(maxRecord);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		adapter.refresh();
	}

	@Override
	public void onArguments(Bundle args) {
		if (args != null) {
			String filter = "(word like '" + args.getString(FragmentArgument.TEXT) + "%')";
			adapter.load(filter);
		}
	}

}
