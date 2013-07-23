package jie.android.el.fragment;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import jie.android.el.FragmentSwitcher;
import jie.android.el.R;
import jie.android.el.database.ELDBAccess;
import jie.android.el.database.Word;
import jie.android.el.service.OnPlayAudioListener;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.XmlTranslator;
import jie.android.el.view.LACWebViewClient;
import jie.android.el.view.OnUrlLoadingListener;
import jie.android.el.view.PopupLayout;

public class ShowFragment extends BaseFragment implements OnClickListener{

	private static final String Tag = ShowFragment.class.getSimpleName();
	
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
			return XmlTranslator.trans(assembleXmlResult(word, result));
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				showPopWindow(word, result);				
			}
		}
	};
	
	private class OnPlayListener extends OnPlayAudioListener.Stub {

		@Override
		public void onPrepared(int duration) throws RemoteException {
			playTime.setText(String.valueOf(duration));
		}

		@Override
		public void onPlaying(int msec) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCompleted() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(String what) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSeekTo(int msec) throws RemoteException {
			// TODO Auto-generated method stub
			
		}  
		
	}
	
	private static int MSG_INDEX	=	1;
	private static int MSG_AUDIO	=	2;
	
	private int dataIndex = -1;
	
	private Animation animShow = null;
	private Animation animHide = null;
	
	private TextView textView = null;
	private WebView webView = null;
	
	private PopupLayout popupLayout = null;
	private TextView popTextView = null;
	private WebView popWebView = null;	
	private ImageButton popCloseButton = null;
	
	private TextView playTime = null;
	private SeekBar playBar = null;
	private ImageView playRepeat = null;
	private ImageView playShuffle = null;
	private ImageView playPrev = null;
	private ImageView playPlay = null;
	private ImageView playNext = null;
	
	private String audio = null;
	private int position = -1;
	
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

		popupLayout = (PopupLayout)view.findViewById(R.id.popup_window);		
		popTextView = (TextView) popupLayout.findViewById(R.id.textView1);
		popTextView.setOnClickListener(this);
		popWebView = (WebView) popupLayout.findViewById(R.id.webView2);
		popCloseButton = (ImageButton) popupLayout.findViewById(R.id.imageButton1);
		popCloseButton.setOnClickListener(this);
	
		playTime = (TextView) view.findViewById(R.id.playTextTime);
		playBar = (SeekBar) view.findViewById(R.id.playSeekBar);
		playRepeat = (ImageView) view.findViewById(R.id.playImageView1);
		playRepeat.setOnClickListener(this);
		playShuffle = (ImageView) view.findViewById(R.id.playImageView2);
		playShuffle.setOnClickListener(this);
		playPrev = (ImageView) view.findViewById(R.id.playImageView3);
		playPrev.setOnClickListener(this);
		playPlay = (ImageView) view.findViewById(R.id.playImageView4);
		playPlay.setOnClickListener(this);
		playNext = (ImageView) view.findViewById(R.id.playImageView5);
		playNext.setOnClickListener(this);
		
		playRepeat.setSelected(true);
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
	
	private boolean isPopupWindowOpen() {
		return popupLayout.getVisibility() == View.VISIBLE;
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
					setAudio(audio);
					playAudio();
				}				
			} finally {
				cursor.close();
			}
		}		
	}

	private void loadData(int index, String title, String data) {
		textView.setText(String.format("%d : %s", index, title));
		
		webView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
	}

	private void setAudio(String audio) {
		this.audio = Environment.getExternalStorageDirectory() + "/jie/el/" + audio;
		
		try {
			getELActivity().getServiceAccess().setAudio(this.audio, new OnPlayListener());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void playAudio() {
		
		try {
			getELActivity().getServiceAccess().playAudio();
			playPlay.setSelected(true);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void pauseAudio() {
		try {
			getELActivity().getServiceAccess().pauseAudio();
			playPlay.setSelected(false);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void stopAudio() {
		try {
			getELActivity().getServiceAccess().stopAudio();
			playPlay.setSelected(false);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isPopupWindowOpen()) {
				togglePopupWindow();
			} else {
				stopAudio();
				getELActivity().showFragment(FragmentSwitcher.Type.LIST, null);
			}
			return true;			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected boolean onUrlLoading(String url) {
//		Toast.makeText(getELActivity(), "url = " + url, Toast.LENGTH_SHORT).show();
		
		int pos = url.indexOf("lac://", 0);
		if (pos != -1) {
			String word = url.substring(6);
			new WordLoader().execute(word);
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

	private void showPopWindow(final String word, final String html) {
		popTextView.setText(word);
		popWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
		
		togglePopupWindow();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textView1:
			speak(popTextView.getText().toString());
			break;
		case R.id.imageButton1:
			togglePopupWindow();
			break;
		case R.id.playImageView1:
			break;
		case R.id.playImageView2:
			break;
		case R.id.playImageView3:
			break;
		case R.id.playImageView4:
			togglePlay();
			break;
		case R.id.playImageView5:
			break;
		default:;
		}
	}

	private void togglePlay() {
		if (playPlay.isSelected()) {
			pauseAudio();
		} else {
			playAudio();
		}
	}

	private void speak(final String text) {
		Speaker.speak(text);
	}
	
}
