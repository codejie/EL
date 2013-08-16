package jie.android.el.fragment;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import jie.android.el.CommonConsts.FragmentArgument;
import jie.android.el.CommonConsts;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import jie.android.el.database.Word;
import jie.android.el.service.Dictionary;
import jie.android.el.service.OnPlayAudioListener;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.Utils;
import jie.android.el.utils.XmlTranslator;
import jie.android.el.view.LACWebViewClient;
import jie.android.el.view.OnUrlLoadingListener;
import jie.android.el.view.PopupLayout;

public class ShowFragment extends BaseFragment implements OnClickListener, OnSeekBarChangeListener{

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
			if (result.getXmlData().size() > 0) {
				return XmlTranslator.trans(assembleXmlResult(word, result));
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			showPopWindow(word, result);				
		}
	};
	
	private class OnPlayListener extends OnPlayAudioListener.Stub {
		
		@Override
		public void onPrepared(int duration) throws RemoteException {
			handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONPREPARED, duration, -1));
		}

		@Override
		public void onPlaying(int msec) throws RemoteException {
			handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONPLAYING, msec, -1));
		}

		@Override
		public void onCompleted() throws RemoteException {
			handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONCOMPLETED));
		}

		@Override
		public void onError(int what, int extra) throws RemoteException {
			handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONERROR, what, extra));		
		}
		

		@Override
		public void onSeekTo(int msec) throws RemoteException {
			handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONSEEKTO, msec, -1));			
		}

		@Override
		public void onAudioChanged(int index) throws RemoteException {
			Bundle args = new Bundle();
			args.putInt(FragmentArgument.INDEX, index);
			args.putInt(FragmentArgument.ACTION, FragmentArgument.Action.SERVICE_NOTIFICATION.getId());
			
			handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));			
		}
	}
		
	private static final int MSG_INDEX				=	1;
	private static final int MSG_AUDIO				=	2;
	private static final int MSG_PLAY_ONPREPARED	=	3;
	private static final int MSG_PLAY_ONPLAYING		=	4;
	private static final int MSG_PLAY_ONCOMPLETED	=	5;
	private static final int MSG_PLAY_ONERROR		=	6;
	private static final int MSG_PLAY_ONSEEKTO		=	7;
	
	private Animation animShow = null;
	private Animation animHide = null;
	
	private TextView textView = null;
	private WebView webView = null;
	
	private PopupLayout popupLayout = null;
	private TextView popTextView = null;
	private WebView popWebView = null;	
	private ImageView popCloseButton = null;
	
	private LinearLayout seekLayout = null;
	private LinearLayout controlLayout = null;
	
	private TextView playTime = null;
	private SeekBar playBar = null;
	private ImageView playRepeat = null;
	private ImageView playShuffle = null;
	private ImageView playPrev = null;
	private ImageView playPlay = null;
	private ImageView playNext = null;
		
//	private String audio = null;
	private String audioDuration = null;
	
	private int audioIndex = -1;
		
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_INDEX:
				onIndex((Bundle)msg.obj);
				break;
			case MSG_PLAY_ONPREPARED:
				OnPlayPrepared(msg.arg1);
				break;
			case MSG_PLAY_ONPLAYING:
				OnPlayPlaying(msg.arg1);
				break;
			case MSG_PLAY_ONCOMPLETED:
				OnPlayCompleted();
				break;
			case MSG_PLAY_ONERROR:
				OnPlayError(msg.arg1, msg.arg2);
				break;
			case MSG_PLAY_ONSEEKTO:
				OnPlaySeekTo(msg.arg1);
				break;
			default:;
			}
		}		
	};
	
	private boolean isPlayingSeek = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_show);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		initAnimation();
		
		textView = (TextView) view.findViewById(R.id.textIndex);
		webView = (WebView) view.findViewById(R.id.webView1);
		LACWebViewClient client = new LACWebViewClient();
		client.setOnUrlLoadingListener(new OnUrlLoadingListener() {

			@Override
			public boolean onLoading(String url) {
				return onUrlLoading(url);
			}
			
		});
		webView.setWebViewClient(client);
//		webView.setOnClickListener(this);
		webView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				return true;
			}
		});

		popupLayout = (PopupLayout)view.findViewById(R.id.popup_window);		
		popTextView = (TextView) popupLayout.findViewById(R.id.textView1);
		popTextView.setOnClickListener(this);
		popWebView = (WebView) popupLayout.findViewById(R.id.webView2);
		popCloseButton = (ImageView) popupLayout.findViewById(R.id.imageView1);// .imageButton1);
		popCloseButton.setOnClickListener(this);

		seekLayout = (LinearLayout) view.findViewById(R.id.seekLayout);
		seekLayout.setOnClickListener(this);
		controlLayout = (LinearLayout) view.findViewById(R.id.controlLayout);
		
		playTime = (TextView) view.findViewById(R.id.playTextTime);
		playTime.setOnClickListener(this);
		playBar = (SeekBar) view.findViewById(R.id.playSeekBar);
		playBar.setOnSeekBarChangeListener(this);
		playBar.setEnabled(false);
		
		playRepeat = (ImageView) view.findViewById(R.id.playImageView1);
		playRepeat.setOnClickListener(this);
//		playRepeat.setEnabled(false);
		
		playShuffle = (ImageView) view.findViewById(R.id.playImageView2);
		playShuffle.setOnClickListener(this);		
		playPrev = (ImageView) view.findViewById(R.id.playImageView3);
		playPrev.setOnClickListener(this);
		playPlay = (ImageView) view.findViewById(R.id.playImageView4);
		playPlay.setOnClickListener(this);
		playNext = (ImageView) view.findViewById(R.id.playImageView5);
		playNext.setOnClickListener(this);
		
//		playRepeat.setSelected(true);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (playShuffle != null) {
			playShuffle.setSelected(getELActivity().getSharedPreferences().getBoolean(Setting.PLAY_RANDOM_ORDER, false));
		}
	}
	
	@Override
	public void onArguments(Bundle args) {
		if (args != null) {
			//isServiceNotification = args.getBoolean("servicenotification", false);
			//handler.sendMessage(Message.obtain(handler, MSG_INDEX, args.getInt("index"), -1));
			handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));
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
	
	protected void onIndex(Bundle obj) {
		
		audioIndex = obj.getInt(FragmentArgument.INDEX);
		
		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL, audioIndex);		
		Cursor cursor = getELActivity().getContentResolver().query(uri, new String[] { "title", "script"}, null, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					String title = cursor.getString(0);
					String script = cursor.getString(1);

					setAudioPlayListener(true);					
					loadData(audioIndex, title, script);
					if (obj.getInt(FragmentArgument.ACTION, FragmentArgument.Action.NONE.getId()) != FragmentArgument.Action.SERVICE_NOTIFICATION.getId()) {
						setAudio(audioIndex);
						playAudio();
					} else {
						handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONPREPARED, obj.getInt("duration"), -1));						
					}
				}				
			} finally {
				cursor.close();
			}
		}		
	}

	private void setAudioPlayListener(boolean attach) {
		try {
			if (attach) {
				getELActivity().getServiceAccess().setAudioListener(new OnPlayListener());
			} else {
				getELActivity().getServiceAccess().setAudioListener(null);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadData(int index, String title, String data) {
		textView.setText(String.format("%d . %s", index, title));
		
//		getELActivity().setTitle(title);
		
		String html = assembleHtmlScript(data);
		
		
		webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
	}

	private String assembleHtmlScript(String data) {
		
//		<HTML>
//		<HEAD>
//		<STYLE>
//		.header { font-size:150% }
//		.body { font-size:100% }
//		.lac { color:#65881a; text-decoration:none; }
//		.src { color:#65881a }
//		</STYLE>
//		</HEAD>
//		<BODY>
//		<DIV>
//		<P class="header">ESL Podcast 31</P>
//		<P class="header">Reading the Newspaper</P>
//		<P class="body">I <A href="lac://fell" class="lac">fell</A> <A href="lac://into" class="lac">into</A> the <A href="lac://habit" class="lac">habit</A> of reading the newspaper every morning when I was a kid. <A href="lac://Back" class="lac">Back</A> then, there used to be two daily <NOBR>newspapers��a</NOBR> morning <A href="lac://edition" class="lac">edition</A> and an afternoon edition. Nowadays, of course, many cities in the U.S. have just a morning paper. <A href="lac://I��m" class="lac">I��m</A> not too <A href="lac://picky" class="lac">picky</A> about which newspaper I read, although <A href="lac://when it comes to" class="src">when it comes to</A> national newspapers, I <A href="lac://" class="lac">prefer</A> reading the New York Times or the Wall Street lournal over USA Today. <A href="lac://" class="src">Don��t get me wrong: </A>I��m not a news <A href="lac://junkie" class="lac">junkie</A>. I just like <A href="lac://browsing" class="lac">browsing</A> the different sections, reading the headlines, and checking out the <A href="lac://classifieds" class="lac">classifieds</A>. I usually <A href="lac://skip" class="lac">skip</A> the sports section and the <A href="lac://funnies" class="lac">funnies</A>, and only <A href="lac://flip through" class="lac">flip through</A> the food and health sections, but I always read the front page and the <A href="lac://editorial" class="lac">editorial</A> page. On the weekends, I��ll <A href="lac://skim" class="lac">skim</A> the entertainment section for the movie listings and reviews.</P>
//		<P class="body">I��m sort of <NOBR>old-fashioned</NOBR> in that I still like reading a real, paper newspaper. Sure, I also read some of my news online, but nothing <A href="lac://beats" class="lac">beats</A> <A href="lac://lounging" class="lac">lounging <A href="lac://around" class="lac">around</A> on Sunday morning reading the big, thick paper. Don��t worry, though: I always <A href="lac://recycle" class="lac">recycle</A> my <A href="lac://stack" class="lac">stack</A> of newspapers.</P>
//		</DIV>
//		</BODY>
//		</HTML>		
		
		SharedPreferences prefs = getELActivity().getSharedPreferences();
		
		String html = "<HTML><HEAD><STYLE>\n.header { font-size:150% }\n.body { font-size:";
		if (prefs.getBoolean(CommonConsts.Setting.CONTENTY_MEDIUM_FONT_SIZE, false)) {
			html += "120%";
		} else if (prefs.getBoolean(CommonConsts.Setting.CONTENTY_LARGE_FONT_SIZE, false)) {
			html += "150%";			
		} else {
			html += "100%";
		}
		html += "}\n.lac { color:#65aa1a; text-decoration:none; }\n.src { color:#65881a }\n.jie { color:#65aa88; text-decoration:none; }\n</STYLE></HEAD><BODY>";
		html += data;
		html += "</BODY></HTML>";
		
		return html;
	}

	private void setAudio(int index) {
//		this.audio = Environment.getExternalStorageDirectory() + "/jie/el/" + audio;
		
		try {
			getELActivity().getServiceAccess().setAudio(index);
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
//			setAudioPlayListener(false);
			getELActivity().getServiceAccess().stopAudio();
			playPlay.setSelected(false);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void seekAudio(int position) {
		try {
			getELActivity().getServiceAccess().seekAudio(position * 1000);
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
				setAudioPlayListener(false);
				stopAudio();
				return false;
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
			ret += "<LAC-R><LAC-D>" + "Vicon English-Chinese Dictionary" + "</LAC-D>";
			int i = 0;			
			
			for(final Dictionary.XmlResult r : data.getResult()) {
				//TODO : cannot get the correct word, so do not display it now.
				//ret += "<w>" + r.word + "</w>";				
				ret += r.result;
				if ((++i) < data.getResult().size())
					ret += "<s/>";
			}
			ret += "</LAC-R>";
		}
		ret += "</LAC>";
		
		return ret;
	}	

	private void showPopWindow(final String word, final String html) {
		popTextView.setText(word);
		if (html != null) {
			popWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
		} else {
			popWebView.loadDataWithBaseURL(null, "<html><body>404, Not Found.<p>please tell this to me (codejie@gmail.com).</body></html>", "text/html", "utf-8", null);
		}
		
		togglePopupWindow();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.webView1:
//			if (isPopupWindowOpen()) {
//				togglePopupWindow();
//			}
//			break;
		case R.id.textView1:
			speak(popTextView.getText().toString());
			break;
		case R.id.imageView1:
			togglePopupWindow();
			break;			
		case R.id.playImageView1:
			showPopupMenu(v);
			break;
		case R.id.playImageView2:
			toggleRandom();
			break;
		case R.id.playImageView3:
			getPrevAudio();
			break;
		case R.id.playImageView4:
			togglePlay();
			break;
		case R.id.playImageView5:
			getNextAudio();
			break;
		case R.id.playTextTime:
		case R.id.seekLayout:
			controlLayout.setVisibility(controlLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
			break;
		default:;
		}
	}

	private void showPopupMenu(View v) {
		PopupMenu pm = new PopupMenu(getELActivity(), v);
		pm.getMenuInflater().inflate(R.menu.fragment_show_pop, pm.getMenu());
		pm.show();
	}

	private void toggleRandom() {
		boolean selected = !playShuffle.isSelected();		
		getELActivity().getSharedPreferences().edit().putBoolean(Setting.PLAY_RANDOM_ORDER, selected).commit();

		playShuffle.setSelected(selected);
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

	protected void OnPlaySeekTo(int arg1) {
		if (isPlayingSeek) {
			playAudio();
			
			isPlayingSeek = false;
		}
	}

	protected void OnPlayError(int what, int extra) {
		stopAudio();
		Toast.makeText(getELActivity(), String.format("ERROR:%d Extra:%d", what, extra), Toast.LENGTH_SHORT).show();
	}

	protected void OnPlayCompleted() {
		stopAudio();
		playBar.setEnabled(false);
		playTime.setText(audioDuration);
	}

	protected void OnPlayPlaying(int msec) {
		
		playBar.setProgress(msec / 1000);
		
		playTime.setText(Utils.formatMSec(msec) + "/" + audioDuration);
	}

	protected void OnPlayPrepared(int duration) {
		audioDuration = Utils.formatMSec(duration);
		
		playBar.setMax(duration / 1000 - 1);
		playBar.setProgress(0);
		
		playBar.setEnabled(true);
		
		playTime.setText(audioDuration);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			seekAudio(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		isPlayingSeek = playPlay.isSelected();
		
		if (isPlayingSeek) {
			pauseAudio();
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
//		playAudio();
	}
	
	private void getNextAudio() {	
		boolean random = getELActivity().getSharedPreferences().getBoolean(CommonConsts.Setting.PLAY_RANDOM_ORDER, false);

		Cursor cursor = Utils.getNextAudio(getELActivity(), audioIndex, new String[] { "idx" }, random, true);		
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					Bundle args = new Bundle();
					args.putInt(FragmentArgument.INDEX, cursor.getInt(0));
					
					handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));					
				}
			} finally {
				cursor.close();
			}
		}
	}
	
	private void getPrevAudio() {
		boolean random = getELActivity().getSharedPreferences().getBoolean(CommonConsts.Setting.PLAY_RANDOM_ORDER, false);

		Cursor cursor = Utils.getNextAudio(getELActivity(), audioIndex, new String[] { "idx" }, random, false);		
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					Bundle args = new Bundle();
					args.putInt(FragmentArgument.INDEX, cursor.getInt(0));
					
					handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));					
				}
			} finally {
				cursor.close();
			}
		}		
	}

}
