package jie.android.el.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import jie.android.el.FragmentSwitcher;
import jie.android.el.R;
import jie.android.el.database.ELDBAccess;
import jie.android.el.database.Word;
import jie.android.el.database.XmlTranslator;
import jie.android.el.view.LACWebViewClient;
import jie.android.el.view.OnUrlLoadingListener;
import jie.android.el.view.PopupLayout;

public class ShowFragment extends BaseFragment {

	private static final String Tag = ShowFragment.class.getSimpleName();
	
	private static int MSG_INDEX	=	1;
	private static int MSG_AUDIO	=	2;
	
	private int dataIndex = -1;
	
	private Animation animShow = null;
	private Animation animHide = null;
	
	private PopupLayout popupLayout = null;
	private TextView textView = null;
	private WebView webView = null;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_INDEX) {
				onIndex(msg.arg1);
			}
		}
		
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_show);
		
//		Bundle b = this.getArguments();
//		if (b != null) {
//			handler.sendMessage(Message.obtain(handler, MSG_INDEX, b.getInt("index"), -1));
//		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		initAnimation();
		
		popupLayout = (PopupLayout)view.findViewById(R.id.popup_window);
		textView = (TextView) view.findViewById(R.id.textView2);
		webView = (WebView) view.findViewById(R.id.webView1);
		LACWebViewClient client = new LACWebViewClient();
		client.setOnUrlLoadingListener(new OnUrlLoadingListener() {

			@Override
			public boolean onLoading(String url) {
				return onUrlLoading(url);
			}
			
		});
		webView.setWebViewClient(client);
	}

	@Override
	public void onArguments(Bundle args) {
		if (args != null) {
			handler.sendMessage(Message.obtain(handler, MSG_INDEX, args.getInt("index"), -1));
		}
	}	
	
	private void initAnimation() {
    	animShow = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_show);
    	animHide = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_hide);
	}

	private void togglePopupWindow() {
		if (popupLayout.getVisibility() == View.GONE) {
			popupLayout.setVisibility(View.VISIBLE);
			popupLayout.startAnimation(animShow);
		} else {
			popupLayout.startAnimation(animHide);
			popupLayout.setVisibility(View.GONE);
		}
	}
	
	protected void onIndex(int index) {
		ELDBAccess db = getELActivity().getDBAccess();
		Cursor cursor = db.queryESLIssue(index);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					String title = cursor.getString(0);
					String data = cursor.getString(1);
					String audio = cursor.getString(2);
					
					loadData(index, title, data);
					playAudio(audio);
				}				
			} finally {
				cursor.close();
			}
		}		
	}

	private void loadData(int index, String title, String data) {
		textView.setText(String.format("%d : %s", index, title));
		
		webView.loadData(data, "text/html", "utf-8");
	}

	private void playAudio(String audio) {
		// post message to service
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			getELActivity().showFragment(FragmentSwitcher.Type.LIST, null);
			return true;			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected boolean onUrlLoading(String url) {
		Toast.makeText(getELActivity(), "url = " + url, Toast.LENGTH_SHORT).show();
		
		int pos = url.indexOf("lac://", 0);
		if (pos != -1) {
			try {
				String word = url.substring(6);
				Word.XmlResult result = getELActivity().getServiceAccess().queryWordResult(word);
				String xml = assembleXmlResult(word, result);
				String html = XmlTranslator.trans(xml);
				
				Log.d(Tag, "result = " + html);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
	}
	
	private final String assembleXmlResult(final String word, final Word.XmlResult result) {
		
		String ret = "<LAC><LAC-W>" + word + "</LAC-W>";
		for (final Word.XmlResult.XmlData data : result.getXmlData()) {
			ret += "<LAC-R><LAC-D>" + "Vicon E-C" + "</LAC-D>";
			for(final String xml : data.getXml()) {
				ret += xml;
			}
			ret += "</LAC-R>";
		}
		ret += "</LAC>";
		
		return ret;
	}	
	
}
