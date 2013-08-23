package jie.android.el.fragment;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import jie.android.el.CommonConsts.FragmentArgument;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.database.Word;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.Utils;
import jie.android.el.utils.XmlTranslator;
import jie.android.el.view.PopupLayout;
import jie.android.el.R;

public class DictionaryFragment extends BaseFragment implements OnRefreshListener<ListView>, OnItemClickListener, DictionaryFragmentListAdapter.OnRefreshListener, OnClickListener {

	private class WordLoader extends AsyncTask<String, Void, String> {

		private String word = null;
		@Override
		protected String doInBackground(String... arg0) {
			word = arg0[0];
			Word.XmlResult result;
			try {
				result = getELActivity().getServiceAccess().queryWordResult(word);
			} catch (RemoteException e) {
				e.printStackTrace();
				return null;
			}
			if (result.getXmlData().size() > 0) {
				return XmlTranslator.trans(Utils.assembleXmlResult(word, result));
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			showPopWindowText(word, result);				
		}
	};	
	
	private static final int MSG_SEARCHVIEW	=	1;
	
	private PullToRefreshListView pullList = null;
	private LinearLayout footLayout = null;
	private TextView footText = null;
	
	private DictionaryFragmentListAdapter adapter = null;
	
	private Animation animShow = null;
	private Animation animHide = null;
	
	private PopupLayout popupLayout = null;
	private TextView popTextView = null;
	private WebView popWebView = null;	
	private ImageView popCloseButton = null;
	
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
		popupLayout = (PopupLayout)view.findViewById(R.id.popup_window);		
		popTextView = (TextView) popupLayout.findViewById(R.id.textView1);
		popTextView.setOnClickListener(this);
		popWebView = (WebView) popupLayout.findViewById(R.id.webView2);
		popCloseButton = (ImageView) popupLayout.findViewById(R.id.imageView1);// .imageButton1);
		popCloseButton.setOnClickListener(this);		
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.textView1:
			speak(popTextView.getText().toString());
			break;
		case R.id.imageView1:
			showPopWindow(false);
			break;
		default:;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		new WordLoader().execute(adapter.getItemText(position));
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
		
		if (getELActivity().getSharedPreferences().getBoolean(Setting.DICTIONARY_LIST_NOT_EXTENSION, false)) {
			filter += " and (flag!=2)";
		}
		
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
		} else if (count < maxPerPage) { // || total < maxPerPage) {
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
	
	private void showPopWindow(boolean show) {
		if (show) {
			popupLayout.setVisibility(View.VISIBLE);
			popupLayout.requestFocus();
			popupLayout.startAnimation(animShow);
		} else {
			popupLayout.startAnimation(animHide);
			popupLayout.setVisibility(View.GONE);			
		}
	}
	
	private void speak(final String text) {
		Speaker.speak(text);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (popupLayout.getVisibility() != View.GONE) {
				showPopWindow(false);
				return true;
			}		
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void showPopWindowText(String word, String html) {
		popTextView.setText(word);
		if (html != null) {
			popWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
		} else {
			popWebView.loadDataWithBaseURL(null, "<html><body>404, Not Found.<p>please tell this to me (codejie@gmail.com).</body></html>", "text/html", "utf-8", null);
		}
		
		showPopWindow(true);
	}	
}
