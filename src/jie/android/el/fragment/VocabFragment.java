package jie.android.el.fragment;

import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

import jie.android.el.FragmentSwitcher;
import jie.android.el.R;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.fragment.adapter.VocabFragmentFlatListAdapter;
import jie.android.el.fragment.adapter.VocabFragmentListAdapter;
import jie.android.el.utils.WordLoader;
import jie.android.el.view.ELPopupWindow;
import jie.android.el.view.OnPopupWindowDefaultListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class VocabFragment extends BaseFragment implements OnItemClickListener, OnRefreshListener<ListView>, VocabFragmentListAdapter.OnAdapterListener {

	private WordLoader.OnPostExecuteCallback wordLoaderCallback = new WordLoader.OnPostExecuteCallback() {

		@Override
		public void OnPostExecute(String word, String result) {
			showPopWindowText(word, result);
		}
	};

	private static final int SORT_BY_ALPHA = 1;
	private static final int SORT_BY_SCORE = 2;

	private static final int GROUP_BY_NONE = 0;
	private static final int GROUP_BY_LESSON = 1;
	private static final int GROUP_BY_SCORE = 2;
	private static final int GROUP_BY_DATE = 3;

	// private LinearLayout footLayout = null;
	// private TextView footText = null;

	private PullToRefreshListView pullList = null;
	private VocabFragmentFlatListAdapter adapter = null;

	private Animation animShow = null;
	private Animation animHide = null;

	private ELPopupWindow popWindow;

	// private int sortMode = SORT_BY_SCORE;
	// private int groupMode = GROUP_BY_NONE;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setLayoutRes(R.layout.fragment_vocab);
		this.setMenuRes(R.menu.fragment_vocab);

		initAnimation();
	}

	private void loadPreferences() {
		SharedPreferences prefs = getELActivity().getSharedPreferences();
		adapter.setSortMode(prefs.getInt(Setting.VOCAB_SORT_MODE, SORT_BY_SCORE));
		adapter.setGroupMode(prefs.getInt(Setting.VOCAB_GROUP_MODE, GROUP_BY_NONE));
	}

	private void savePreferences() {
		SharedPreferences.Editor editor = getELActivity().getSharedPreferences().edit();
		editor.putInt(Setting.VOCAB_SORT_MODE, adapter.getSortMode());
		editor.putInt(Setting.VOCAB_GROUP_MODE, adapter.getGroupMode());
		editor.commit();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initList(view);

		initPopWindow(view);

		loadPreferences();

		adapter.load(null);
	}

	private void initAnimation() {
		animShow = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_show);
		animHide = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_hide);
	}

	private void initList(View view) {
		pullList = (PullToRefreshListView) view.findViewById(R.id.pullToRefreshListView1);
		// pullList.getRefreshableView().setDivider(getResources().getDrawable(R.drawable.el_list_divider));
		pullList.setMode(Mode.PULL_FROM_END);
		pullList.getLoadingLayoutProxy().setPullLabel(getText(R.string.el_fragment_dict_pulltorefresh));
		pullList.getLoadingLayoutProxy().setReleaseLabel(getText(R.string.el_fragment_dict_releasetorefresh));
		pullList.getLoadingLayoutProxy().setRefreshingLabel(getText(R.string.el_fragment_dict_loading));
		// pullList.getRefreshableView().setFooterDividersEnabled(true);

		pullList.setOnRefreshListener(this);
		pullList.setOnItemClickListener(this);

		View v = getELActivity().getLayoutInflater().inflate(R.layout.fragment_dictionary_list_foot, null);
		// footLayout = (LinearLayout) v.findViewById(R.id.footLayout);
		TextView footText = (TextView) v.findViewById(R.id.textFoot);
		footText.setText(getString(R.string.el_vocab_nowords));//"No Words in Vocab");
		pullList.getRefreshableView().setEmptyView(v);

		adapter = new VocabFragmentFlatListAdapter(getELActivity());
		pullList.setAdapter(adapter);

		adapter.setOnAdapterListener(this);

		final int maxRecord = getELActivity().getSharedPreferences().getInt(Setting.DICTIONARY_LIST_MAXPERPAGE, 15);
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
		// loadAdapter("a");
	}

	private void initPopWindow(View view) {

		popWindow = (ELPopupWindow) view.findViewById(R.id.eLPopupWindow1);
		popWindow.setOnPopupWindowListener(new OnPopupWindowDefaultListener(popWindow));
		popWindow.setAnimation(animShow, animHide);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		WordLoader loader = new WordLoader(getELActivity().getServiceAccess(), wordLoaderCallback);
		loader.execute(adapter.getItemText(position - 1));
	}

	@Override
	public void onLoadEnd(int count, int total, int maxPerPage) {
		pullList.onRefreshComplete();
	}

	@Override
	public void onItemRemoved(VocabFragmentListAdapter.ItemType type, String text) {
		//
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		adapter.refresh();
	}

	private void showPopWindow(boolean show) {
		if (show) {
			popWindow.show(show, animShow);
		} else {
			popWindow.show(false, animHide);
		}
	}

	public void showPopWindowText(String word, String html) {
		popWindow.setText(word);
		if (html != null) {
			popWindow.loadWebContent(html);
		} else {
			popWindow.loadWebContent("<html><body>404, Not Found.<p>please tell this to me (codejie@gmail.com).</body></html>");
		}

		showPopWindow(true);
	}

	@Override
	public boolean OnOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.el_menu_memory:
			getELActivity().showFragment(FragmentSwitcher.Type.MEMORY, null);
			break;
		case R.id.el_menu_vocab_edit:
			if (!adapter.isEditable()) {
				adapter.setEditable(true);

				if (popWindow.isShowing()) {
					showPopWindow(false);
				}
				pullList.setOnItemClickListener(null);

				item.setTitle(R.string.el_menu_vocab_edit_1);
			} else {
				adapter.setEditable(false);
				pullList.setOnItemClickListener(this);
				item.setTitle(R.string.el_menu_vocab_edit);
			}
			break;
		case R.id.el_menu_vocab_sortbyalpha:
			adapter.setSortMode(SORT_BY_ALPHA);
			savePreferences();
			reloadList();
			break;
		case R.id.el_menu_vocab_sortbyscore:
			adapter.setSortMode(SORT_BY_SCORE);
			savePreferences();
			reloadList();
			break;
		case R.id.el_menu_vocab_groupbynone:
			adapter.setGroupMode(GROUP_BY_NONE);
			savePreferences();
			reloadList();
			break;
		case R.id.el_menu_vocab_groupbylesson:
			adapter.setGroupMode(GROUP_BY_LESSON);
			savePreferences();
			reloadList();
			break;
		case R.id.el_menu_vocab_groupbyscore:
			adapter.setGroupMode(GROUP_BY_SCORE);
			savePreferences();
			reloadList();
			break;
		case R.id.el_menu_vocab_groupbydate:
			adapter.setGroupMode(GROUP_BY_DATE);
			savePreferences();
			reloadList();
			break;
		default:
			return super.OnOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!adapter.isEmpty()) {
				getELActivity().showFragment(FragmentSwitcher.Type.MEMORY, null);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void reloadList() {
		adapter.load(null);
	}
}
