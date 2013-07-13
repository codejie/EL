package jie.android.el.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import jie.android.el.R;
import jie.android.el.database.DBAccess;
import jie.android.el.view.LACWebViewClient;
import jie.android.el.view.PopupLayout;

public class ShowFragment extends BaseFragment {

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
		
		Bundle b = this.getArguments();
		if (b != null) {
			handler.sendMessage(Message.obtain(handler, MSG_INDEX, b.getInt("index"), -1));
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		initAnimation();
		
		popupLayout = (PopupLayout)view.findViewById(R.id.popup_window);
		textView = (TextView) view.findViewById(R.id.textView2);
		webView = (WebView) view.findViewById(R.id.webView1);
		webView.setWebViewClient(new LACWebViewClient());
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
		DBAccess db = getELActivity().getDBAccess();
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
}
