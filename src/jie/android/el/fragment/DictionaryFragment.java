package jie.android.el.fragment;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class DictionaryFragment extends BaseFragment implements OnRefreshListener<ListView>, OnItemClickListener, DictionaryFragmentListAdapter.OnRefreshListener {

	private static final int MSG_SEARCHVIEW	=	1;
	
	private PullToRefreshListView pullList = null;
	private LinearLayout footLayout = null;
	private TextView footText = null;
	
	private DictionaryFragmentListAdapter adapter = null;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SEARCHVIEW:
				Bundle data = (Bundle)msg.obj;
				onSearchViewChanged(data.getString(FragmentArgument.TEXT));
				break;
			default:;
			}
		}
		
	};
	
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
			Message msg = Message.obtain(handler, MSG_SEARCHVIEW, args);
			msg.sendToTarget();
		}
	}
	
	private void loadAdapter(final String prefix) {
		String filter = "(word like '" + prefix + "%')";
		adapter.load(filter);		
	}

	protected void onSearchViewChanged(String text) {
		pullList.setMode(Mode.PULL_FROM_END);
		loadAdapter(text);
	}
	
	@Override
	public void onLoadEnd(int count, int total, int maxPerPage) {
		pullList.onRefreshComplete();
		
		if (total == 0) {
			pullList.setMode(Mode.DISABLED);
			showListFootTip(true, R.string.el_fragment_dict_nomatchresult);
		} else if (count == 0 || total < maxPerPage) {
			pullList.setMode(Mode.DISABLED);
			showListFootTip(true, R.string.el_fragment_dict_nomoreresults);
		} else {
			showListFootTip(false, -1);
		}		

	}

	private void showListFootTip(boolean show, int resId) {
		if (show) {
			footLayout.setVisibility(View.VISIBLE);
			if (resId != -1) {
				footText.setText(resId);
			}
		} else {
			footLayout.setVisibility(View.GONE);
		}
	}	
}
