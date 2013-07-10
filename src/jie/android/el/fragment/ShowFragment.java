package jie.android.el.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import jie.android.el.R;
import jie.android.el.database.DBAccess;
import jie.android.el.view.PopupLayout;

public class ShowFragment extends BaseFragment {

	private static int MSG_INDEX	=	1;
	private static int MSG_AUDIO	=	2;
	
	private int dataIndex = -1;
	
	private Animation animShow = null;
	private Animation animHide = null;
	
	private PopupLayout popupLayout = null;

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
		try {
			
			String title = cursor.getString(0);
			String data = cursor.getString(1);
			String audio = cursor.getString(2);
			
			loadData(index, title, data);
			playAudio(audio);
			
		} finally {
			cursor.close();
		}
		
	}

	private void loadData(int index, String title, String data) {
		// TODO Auto-generated method stub
		
	}

	private void playAudio(String audio) {
		// TODO Auto-generated method stub
		
	}	
}
