package jie.android.el.fragment;

import jie.android.el.CommonConsts;
import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.CommonConsts.AudioNavigateData;
import jie.android.el.CommonConsts.BroadcastAction;
import jie.android.el.CommonConsts.PlayState;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.CommonConsts.UpdateAudioType;
import jie.android.el.CommonConsts.UpdateUIType;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.ScoreHelper;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.Utils;
import jie.android.el.utils.WordLoader;
import jie.android.el.view.ELPopupMenu;
import jie.android.el.view.ELPopupWindow;
import jie.android.el.view.LACWebViewClient;
import jie.android.el.view.OnPopupWindowDefaultListener;
import jie.android.el.view.OnUrlLoadingListener;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ShowFragment extends BaseFragment implements OnClickListener, OnSeekBarChangeListener {

	private static final String Tag = ShowFragment.class.getSimpleName();

	private WordLoader.OnPostExecuteCallback wordLoaderCallback = new WordLoader.OnPostExecuteCallback() {

		@Override
		public void OnPostExecute(String word, String result) {
			showPopWindow(word, result);
		}
	};

	private Animation animShow = null;
	private Animation animHide = null;

	private TextView textView = null;
	private WebView webView = null;

	private ELPopupWindow popWindow = null;

	private LinearLayout seekLayout = null;
	private LinearLayout controlLayout = null;

	private TextView playTime = null;
	private SeekBar playBar = null;
	private ImageView playNavigate = null;
	private ImageView playShuffle = null;
	private ImageView playPrev = null;
	private ImageView playPlay = null;
	private ImageView playNext = null;

	private String audioDuration = null;

	private int audioIndex = -1;
	private int audioNavigate = AudioNavigateData.DISABLE;

	private boolean isPlayingSeek = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setLayoutRes(R.layout.fragment_show1);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initAnimation();

		textView = (TextView) view.findViewById(R.id.textIndex);
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				textView.setVisibility(View.GONE);
			}
		});

		webView = (WebView) view.findViewById(R.id.webView1);
		LACWebViewClient client = new LACWebViewClient();
		client.setOnUrlLoadingListener(new OnUrlLoadingListener() {

			@Override
			public boolean onLoading(String url) {
				return onUrlLoading(url);
			}

		});
		webView.setWebViewClient(client);
		webView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (popWindow.isShowing()) {
						showPopWindow(false);
					}
				}
				return false;
			}

		});

		popWindow = (ELPopupWindow) view.findViewById(R.id.eLPopupWindow1);
		popWindow.setOnPopupWindowListener(new OnPopupWindowDefaultListener(popWindow) {
			@Override
			public boolean onTextLongClick(String text) {
				ScoreHelper.insertWord(getELActivity(), text, audioIndex);
				Utils.showToast(getELActivity(), String.format(getELActivity().getText(R.string.el_toast_add_word_to_score).toString(), text));
				return true;
			}
		});
		popWindow.setAnimation(animShow, animHide);

		seekLayout = (LinearLayout) view.findViewById(R.id.seekLayout);
		seekLayout.setOnClickListener(this);
		controlLayout = (LinearLayout) view.findViewById(R.id.controlLayout);

		playTime = (TextView) view.findViewById(R.id.playTextTime);
		playTime.setOnClickListener(this);
		playBar = (SeekBar) view.findViewById(R.id.playSeekBar);
		playBar.setOnSeekBarChangeListener(this);
		playBar.setEnabled(false);

		playNavigate = (ImageView) view.findViewById(R.id.playImageView1);
		playNavigate.setOnClickListener(this);

		playShuffle = (ImageView) view.findViewById(R.id.playImageView2);
		playShuffle.setOnClickListener(this);
		playPrev = (ImageView) view.findViewById(R.id.playImageView3);
		playPrev.setOnClickListener(this);
		playPlay = (ImageView) view.findViewById(R.id.playImageView4);
		playPlay.setOnClickListener(this);
		playNext = (ImageView) view.findViewById(R.id.playImageView5);
		playNext.setOnClickListener(this);
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
	}

	private void initAnimation() {
		animShow = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_show);
		animHide = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_hide);
	}

	private void showPopWindow(boolean show) {
		popWindow.show(show);
	}

	private void onIndex(int index) {
		if (index == -1) {
			return;
		}

		loadAudioData(index);

		playPlay.setEnabled(false);
		playPlay.setSelected(false);
		playBar.setEnabled(false);
	}

	private boolean loadAudioData(int index) {

		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL, index);
		Cursor cursor = getELActivity().getContentResolver().query(uri, new String[] { "title", "script" }, null, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {

					String title = cursor.getString(0);
					String script = cursor.getString(1);

					audioIndex = index;

					loadData(audioIndex, title, script);

					return true;
				}
			} finally {
				cursor.close();
			}
		}
		return false;
	}

	private void loadData(int index, String title, String data) {

		String html = assembleHtmlScript(data);
		webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);

		textView.setVisibility(View.VISIBLE);
		textView.setText(String.format("%d. %s", index, title));
	}

	private String assembleHtmlScript(String data) {

		// <HTML>
		// <HEAD>
		// <STYLE>
		// .header { font-size:150% }
		// .body { font-size:100% }
		// .lac { color:#65881a; text-decoration:none; }
		// .src { color:#65881a }
		// </STYLE>
		// </HEAD>
		// <BODY>
		// <DIV>
		// <P class="header">ESL Podcast 31</P>
		// <P class="header">Reading the Newspaper</P>
		// <P class="body">I <A href="lac://fell" class="lac">fell</A> <A
		// href="lac://into" class="lac">into</A> the <A href="lac://habit"
		// class="lac">habit</A> of reading the newspaper every morning when I
		// was a kid. <A href="lac://Back" class="lac">Back</A> then, there used
		// to be two daily <NOBR>newspapers��a</NOBR> morning <A
		// href="lac://edition" class="lac">edition</A> and an afternoon
		// edition. Nowadays, of course, many cities in the U.S. have just a
		// morning paper. <A href="lac://I��m" class="lac">I��m</A> not too <A
		// href="lac://picky" class="lac">picky</A> about which newspaper I
		// read, although <A href="lac://when it comes to" class="src">when it
		// comes to</A> national newspapers, I <A href="lac://"
		// class="lac">prefer</A> reading the New York Times or the Wall Street
		// lournal over USA Today. <A href="lac://" class="src">Don��t get me
		// wrong: </A>I��m not a news <A href="lac://junkie"
		// class="lac">junkie</A>. I just like <A href="lac://browsing"
		// class="lac">browsing</A> the different sections, reading the
		// headlines, and checking out the <A href="lac://classifieds"
		// class="lac">classifieds</A>. I usually <A href="lac://skip"
		// class="lac">skip</A> the sports section and the <A
		// href="lac://funnies" class="lac">funnies</A>, and only <A
		// href="lac://flip through" class="lac">flip through</A> the food and
		// health sections, but I always read the front page and the <A
		// href="lac://editorial" class="lac">editorial</A> page. On the
		// weekends, I��ll <A href="lac://skim" class="lac">skim</A> the
		// entertainment section for the movie listings and reviews.</P>
		// <P class="body">I��m sort of <NOBR>old-fashioned</NOBR> in that I
		// still like reading a real, paper newspaper. Sure, I also read some of
		// my news online, but nothing <A href="lac://beats"
		// class="lac">beats</A> <A href="lac://lounging" class="lac">lounging
		// <A href="lac://around" class="lac">around</A> on Sunday morning
		// reading the big, thick paper. Don��t worry, though: I always <A
		// href="lac://recycle" class="lac">recycle</A> my <A href="lac://stack"
		// class="lac">stack</A> of newspapers.</P>
		// </DIV>
		// </BODY>
		// </HTML>

		SharedPreferences prefs = getELActivity().getSharedPreferences();

		String html = "<HTML><HEAD><STYLE>\n.header { font-size:150% }\n.body { font-size:";
		if (prefs.getBoolean(CommonConsts.Setting.CONTENT_MEDIUM_FONT_SIZE, false)) {
			html += "120%";
		} else if (prefs.getBoolean(CommonConsts.Setting.CONTENT_LARGE_FONT_SIZE, false)) {
			html += "150%";
		} else {
			html += "100%";
		}
		html += "}\n.lac { color:#65aa1a; text-decoration:none; }\n.src { color:#65881a }\n.jie { color:#65aa88; text-decoration:none; }\n</STYLE></HEAD><BODY>";
		html += data;
		html += "<BR/><BR/></BODY></HTML>";

		return html;
	}

	private void playAudio() {
		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_PLAY));
	}

	// private void pauseAudio() {
	// sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_PLAY));
	// }

	private void stopAudio() {
		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_STOP));
	}

	private void seekAudio(int type) {
		Intent intent = new Intent(AudioAction.ACTION_AUDIO_NAVIGATE);
		intent.putExtra(AudioAction.DATA_NAVIGATE, type);

		sendBroadcast(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (popWindow.isShowing()) {
				showPopWindow(false);
			} else {
				onClose();
				return false;
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void onClose() {
		stopAudio();
	}

	protected boolean onUrlLoading(String url) {
		// Toast.makeText(getELActivity(), "url = " + url,
		// Toast.LENGTH_SHORT).show();

		int pos = url.indexOf("lac://", 0);
		if (pos != -1) {
			String word = url.substring(6);
			WordLoader loader = new WordLoader(getELActivity().getServiceAccess(), wordLoaderCallback);
			loader.execute(word);
			return true;
		}

		return false;
	}

	private void showPopWindow(final String word, final String html) {
		popWindow.setText(word);
		if (html != null) {
			popWindow.loadWebContent(html);
		} else {
			popWindow.loadWebContent("<html><body>404, Not Found.<p>please tell this to me (codejie@gmail.com).</body></html>");
		}

		showPopWindow(true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
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
			playAudio();
			// togglePlay();
			break;
		case R.id.playImageView5:
			getNextAudio();
			break;
		case R.id.playTextTime:
		case R.id.seekLayout:
			controlLayout.setVisibility(controlLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
			break;
		default:
			;
		}
	}

	private void showPopupMenu(View v) {
		ELPopupMenu pm = new ELPopupMenu(getELActivity(), R.menu.popmenu_menu, v, new ELPopupMenu.OnItemClickListener() {

			@Override
			public void OnClick(int id) {
				onNavigate(id);
			}
		});

		pm.setItemEnabled(R.id.el_menu_show_slowdialog, ((audioNavigate & AudioNavigateData.SLOWDIALOG) == AudioNavigateData.SLOWDIALOG));
		pm.setItemEnabled(R.id.el_menu_show_explanation, ((audioNavigate & AudioNavigateData.EXPLANATION) == AudioNavigateData.EXPLANATION));
		pm.setItemEnabled(R.id.el_menu_show_fastdialog, ((audioNavigate & AudioNavigateData.FASTDIALOG) == AudioNavigateData.FASTDIALOG));

		pm.show();
	}

	protected void onNavigate(int id) {
		if (id == R.id.el_menu_show_slowdialog) {
			seekAudio(AudioNavigateData.SLOWDIALOG);
		} else if (id == R.id.el_menu_show_explanation) {
			seekAudio(AudioNavigateData.EXPLANATION);
		} else if (id == R.id.el_menu_show_fastdialog) {
			seekAudio(AudioNavigateData.FASTDIALOG);
		}
	}

	private void toggleRandom() {
		boolean selected = !playShuffle.isSelected();
		getELActivity().getSharedPreferences().edit().putBoolean(Setting.PLAY_RANDOM_ORDER, selected).commit();

		playShuffle.setSelected(selected);
	}

	private void speak(final String text) {
		Speaker.speak(text);
	}

	protected void onPlaySeekTo(int arg1) {
		if (isPlayingSeek) {
			playAudio();

			isPlayingSeek = false;
		}
	}

	protected void onPlayError(int what, int extra) {
		playNavigate.setEnabled(false);
		playPlay.setEnabled(false);
		Toast.makeText(getELActivity(), String.format("Play failed - ERROR:%d Extra:%d", what, extra), Toast.LENGTH_SHORT).show();
	}

	protected void onPlayCompleted() {
		playPlay.setSelected(false);
		playBar.setEnabled(false);
		playNavigate.setEnabled(false);
		playTime.setText(audioDuration);
	}

	protected void onPlayPlaying(int msec) {
		playBar.setProgress(msec / 1000);
		playTime.setText(Utils.formatMSec(msec) + "/" + audioDuration);
	}

	protected void onPlayPrepared(int duration, int position) {

		playPlay.setEnabled(true);
		playPlay.setSelected(false);

		if (audioNavigate == AudioNavigateData.DISABLE) {
			playNavigate.setEnabled(false);
		} else {
			playNavigate.setEnabled(true);
		}

		audioDuration = Utils.formatMSec(duration);

		playBar.setMax(duration / 1000 - 1);
		playBar.setProgress(position / 1000);

		playBar.setEnabled(true);

		playTime.setText(audioDuration);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			Intent intent = new Intent(AudioAction.ACTION_AUDIO_SEEK);
			intent.putExtra(AudioAction.DATA_POSITION, progress * 1000);
			sendBroadcast(intent);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// isPlayingSeek = playPlay.isSelected();
		//
		// if (isPlayingSeek) {
		// pauseAudio();
		// }
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// playAudio();
	}

	private void getNextAudio() {
		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_NEXT));
	}

	private void getPrevAudio() {
		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_PREV));
	}

	@Override
	public void onPause() {
		super.onPause();

		Intent intent = new Intent(AudioAction.ACTION_UPDATE_UI);
		intent.putExtra(AudioAction.DATA_TYPE, UpdateUIType.AUDIO_WINDOW_CLOSE.getId());
		sendBroadcast(intent);
	}

	@Override
	public void onResume() {
		super.onResume();

		Intent intent = new Intent(AudioAction.ACTION_UPDATE_UI);
		intent.putExtra(AudioAction.DATA_TYPE, UpdateUIType.AUDIO_WINDOW_SHOW.getId());
		sendBroadcast(intent);

		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_QUERY));

		if (playShuffle != null) {
			playShuffle.setSelected(getELActivity().getSharedPreferences().getBoolean(Setting.PLAY_RANDOM_ORDER, false));
		}
	}

	protected void onHideTitle() {
		textView.setVisibility(View.GONE);
	}

	@Override
	public void onIntent(Intent intent) {
		final String action = intent.getAction();
		if (action.equals(AudioAction.ACTION_UPDATE_AUDIO_PLAYING)) {
			onPlayPlaying(intent.getIntExtra(AudioAction.DATA_POSITION, 0));
			if (!playPlay.isSelected()) {
				playPlay.setSelected(true);
			}
		} else if (action.equals(AudioAction.ACTION_UPDATE_AUDIO)) {
			int type = intent.getIntExtra(AudioAction.DATA_TYPE, -1);
			if (type != -1) {
				if (type == UpdateAudioType.STATE_CHANGED.getId()) {
					onStateChanged(intent);
				} else if (type == UpdateAudioType.AUDIO_CHANGED_OPEN.getId()) {
					onAudioChanged(true, intent);
				} else if (type == UpdateAudioType.AUDIO_CHANGED_CLOSE.getId()) {
					onAudioChanged(false, intent);
				}
			}
		} else if (action.equals(AudioAction.ACTION_AUDIO_SET)) {
			//
		} else if (action.equals(BroadcastAction.ACTION_UPDATE_UI)) {
			if (intent.getIntExtra(BroadcastAction.DATA_TYPE, -1) == UpdateUIType.HIDE_TITLE.getId()) {
				onHideTitle();
			}
		}
	}

	private void onStateChanged(Intent intent) {
		int state = intent.getIntExtra(AudioAction.DATA_STATE, -1);

		if (state == PlayState.PREPARED.getId()) {
			playPlay.setEnabled(true);
			if (playPlay.isSelected()) {
				playPlay.setSelected(false);
			}

		} else if (state == PlayState.PLAY.getId()) {
			if (!playPlay.isSelected()) {
				playPlay.setSelected(true);
			}
		} else if (state == PlayState.PAUSED.getId()) {
			if (playPlay.isSelected()) {
				playPlay.setSelected(false);
			}

		} else if (state == PlayState.NONE.getId()) {
			if (playPlay.isSelected()) {
				playPlay.setSelected(false);
			}
		}
	}

	private void onAudioChanged(boolean open, Intent intent) {
		if (open) {
			int index = intent.getIntExtra(AudioAction.DATA_ID, -1);
			if (index != audioIndex) {
				onIndex(index);
			}
			audioNavigate = intent.getIntExtra(AudioAction.DATA_NAVIGATE, AudioNavigateData.DISABLE);
			onPlayPrepared(intent.getIntExtra(AudioAction.DATA_DURATION, 0), intent.getIntExtra(AudioAction.DATA_POSITION, 0));
			onStateChanged(intent);
		} else {
			//
		}
	}

}
