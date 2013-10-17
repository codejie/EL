package jie.android.el.fragment;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
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
import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.CommonConsts.AudioNavigateData;
import jie.android.el.CommonConsts.BroadcastAction;
import jie.android.el.CommonConsts.FragmentArgument;
import jie.android.el.CommonConsts.PlayState;
import jie.android.el.CommonConsts;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.CommonConsts.UpdateAudioType;
import jie.android.el.CommonConsts.UpdateUIType;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import jie.android.el.service.OnPlayAudioListener;
import jie.android.el.service.ServiceAccess;
import jie.android.el.utils.ScoreHelper;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.Utils;
import jie.android.el.utils.WordLoader;
import jie.android.el.view.ELPopupMenu;
import jie.android.el.view.ELPopupWindow;
import jie.android.el.view.LACWebViewClient;
import jie.android.el.view.OnPopupWindowDefaultListener;
import jie.android.el.view.OnUrlLoadingListener;

public class ShowFragment extends BaseFragment implements OnClickListener, OnSeekBarChangeListener {

	private static final String Tag = ShowFragment.class.getSimpleName();

	// private class OnPlayListener extends OnPlayAudioListener.Stub {
	//
	// private boolean attached = false;
	//
	// public boolean isAttached() {
	// return attached;
	// }
	//
	// public void setAttached(boolean attached) {
	// this.attached = attached;
	// }
	//
	// // @Override
	// // public void onPrepared(int duration) throws RemoteException {
	// // //handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONPREPARED,
	// duration, -1));
	// // }
	//
	// @Override
	// public void onPlaying(int msec) throws RemoteException {
	// // handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONPLAYING, msec,
	// -1));
	// }
	//
	// @Override
	// public void onCompleted() throws RemoteException {
	// // handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONCOMPLETED));
	// }
	//
	// @Override
	// public void onError(int what, int extra) throws RemoteException {
	// // handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONERROR, what,
	// extra));
	// }
	//
	// @Override
	// public void onSeekTo(int msec) throws RemoteException {
	// // handler.sendMessage(Message.obtain(handler, MSG_PLAY_ONSEEKTO, msec,
	// -1));
	// }
	//
	// // @Override
	// // public void onAudioChanged(int index) throws RemoteException {
	// // Bundle args = new Bundle();
	// // args.putInt(FragmentArgument.ACTION,
	// FragmentArgument.Action.SERVICE_NOTIFICATION.getId());
	// // args.putInt(FragmentArgument.INDEX, index);
	// // args.putInt(FragmentArgument.STATE, PlayState.PLAYING.getId());
	// // args.putInt(FragmentArgument.DURATION, 0);
	// // args.putInt(FragmentArgument.POSITION, 0);
	// //
	// // handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));
	// // }
	//
	// // @Override
	// // public void onIsPlaying(int index, int state, int duration, int msec)
	// throws RemoteException {
	// // Bundle args = new Bundle();
	// // args.putInt(FragmentArgument.ACTION,
	// FragmentArgument.Action.SERVICE_NOTIFICATION.getId());
	// // args.putInt(FragmentArgument.INDEX, index);
	// // args.putInt(FragmentArgument.STATE, state);
	// // args.putInt(FragmentArgument.DURATION, duration);
	// // args.putInt(FragmentArgument.POSITION, msec);
	// //
	// // handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));
	// // }
	//
	// // @Override
	// // public void onStateChanged(int state) throws RemoteException {
	// // handler.sendMessage(Message.obtain(handler, MSG_PLAY_STATE_CHANGED,
	// state, -1));
	// //
	// // }
	// }

	// private final class UpdateHandler extends Handler {
	//
	// public UpdateHandler(Looper looper) {
	// super(looper);
	// }
	//
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case MSG_INDEX:
	// onIndex((Bundle) msg.obj);
	// break;
	// // case MSG_PLAY_ONPREPARED:
	// // onPlayPrepared(msg.arg1);
	// // break;
	// // case MSG_PLAY_ONPLAYING:
	// // onPlayPlaying(msg.arg1);
	// // break;
	// case MSG_PLAY_ONCOMPLETED:
	// onPlayCompleted();
	// break;
	// // case MSG_PLAY_STATE_CHANGED:
	// // onPlayStateChanged(msg.arg1);
	// // break;
	// case MSG_PLAY_ONERROR:
	// onPlayError(msg.arg1, msg.arg2);
	// break;
	// case MSG_PLAY_ONSEEKTO:
	// onPlaySeekTo(msg.arg1);
	// break;
	// case MSG_HIDE_TITLE:
	// onHideTitle();
	// break;
	// // case MSG_AUDIO_PLAYING:
	// // onAudioPlaying();
	// // break;
	// default:
	// ;
	// }
	// }
	//
	// }

	private WordLoader.OnPostExecuteCallback wordLoaderCallback = new WordLoader.OnPostExecuteCallback() {

		@Override
		public void OnPostExecute(String word, String result) {
			showPopWindow(word, result);
		}
	};

	private static final int MSG_INDEX = 1;
	// private static final int MSG_AUDIO_PLAYING = 2;
	// private static final int MSG_PLAY_ONPREPARED = 3;
	private static final int MSG_PLAY_ONPLAYING = 4;
	private static final int MSG_PLAY_ONCOMPLETED = 5;
	private static final int MSG_PLAY_ONERROR = 6;
	private static final int MSG_PLAY_ONSEEKTO = 7;
	private static final int MSG_HIDE_TITLE = 8;
	// private static final int MSG_PLAY_STATE_CHANGED = 9;

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
	// private int audioSlowDialog = -1;
	// private int audioExplanation = -1;
	// private int audioFastDialog = -1;

	// private OnPlayListener onPlayListener = new OnPlayListener();
	// private HandlerThread handlerThread;
	// private UpdateHandler handler;
	//
	// private Handler handler = new Handler() {
	//
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case MSG_INDEX:
	// onIndex((Bundle) msg.obj);
	// break;
	// // case MSG_PLAY_ONPREPARED:
	// // onPlayPrepared(msg.arg1);
	// // break;
	// // case MSG_PLAY_ONPLAYING:
	// // onPlayPlaying(msg.arg1);
	// // break;
	// case MSG_PLAY_ONCOMPLETED:
	// onPlayCompleted();
	// break;
	// // case MSG_PLAY_STATE_CHANGED:
	// // onPlayStateChanged(msg.arg1);
	// // break;
	// case MSG_PLAY_ONERROR:
	// onPlayError(msg.arg1, msg.arg2);
	// break;
	// case MSG_PLAY_ONSEEKTO:
	// onPlaySeekTo(msg.arg1);
	// break;
	// case MSG_HIDE_TITLE:
	// onHideTitle();
	// break;
	// // case MSG_AUDIO_PLAYING:
	// // onAudioPlaying();
	// // break;
	// default:
	// ;
	// }
	// }
	// };

	private boolean isPlayingSeek = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setLayoutRes(R.layout.fragment_show1);

		// handlerThread = new HandlerThread("el.show.handler");
		// handlerThread.run();
		// handler = new UpdateHandler(handlerThread.getLooper());
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

		Intent intent = new Intent(AudioAction.ACTION_AUDIO_QUERY);
		sendBroadcast(intent);
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
		// if (args != null) {
		// int action = args.getInt(FragmentArgument.ACTION, -1);
		// if (action == FragmentArgument.Action.PLAY.getId()) {
		// handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));
		// }
		// // else if (action ==
		// FragmentArgument.Action.SERVICE_NOTIFICATION.getId()) {
		// // handler.sendMessage(Message.obtain(handler, MSG_AUDIO_PLAYING,
		// args));
		// // }
		// }
	}

	private void initAnimation() {
		animShow = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_show);
		animHide = AnimationUtils.loadAnimation(getELActivity(), R.anim.popup_hide);
	}

	private void showPopWindow(boolean show) {
		popWindow.show(show);
	}

	// protected void onIndex(Bundle obj) {
	//
	// int action = obj.getInt(FragmentArgument.ACTION, -1);
	// int state = obj.getInt(FragmentArgument.STATE, -1);
	//
	// int index = obj.getInt(FragmentArgument.INDEX);
	//
	// loadAudioData(index);
	//
	// // if (action == FragmentArgument.Action.PLAY.getId()) { // onclick
	//
	// playPlay.setEnabled(false);
	// playPlay.setSelected(false);
	// playBar.setEnabled(false);
	//
	// // setAudioPlayListener(true);
	// // setAudio(audioIndex);
	// // } else { // notification - onPlaying and onAudioChange
	//
	// // playPlay.setEnabled(true);
	// // playPlay.setSelected(state == PlayState.PLAYING.getId());
	// //
	// // onPlayPrepared(obj.getInt(FragmentArgument.DURATION, 0));
	// // onPlayPlaying(obj.getInt(FragmentArgument.POSITION, 0));
	// // }
	// }

	private void onIndex(int index) {
		if (index == -1) {
			return;
		}

		loadAudioData(index);

		playPlay.setEnabled(false);
		playPlay.setSelected(false);
		playBar.setEnabled(false);

		// setAudioPlayListener(true);
	}

	private boolean loadAudioData(int index) {

		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL, index);
		Cursor cursor = getELActivity().getContentResolver().query(uri, new String[] { "title", "script", "slowdialog", "explanations", "fastdialog" }, null,
				null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {

					String title = cursor.getString(0);
					String script = cursor.getString(1);

					audioIndex = index;

					// audioSlowDialog = cursor.getInt(2);
					// audioExplanation = cursor.getInt(3);
					// audioFastDialog = cursor.getInt(4);

					loadData(audioIndex, title, script);

					return true;
				}
			} finally {
				cursor.close();
			}
		}
		return false;
	}

	//
	// private void setAudioPlayListener(boolean attach) {
	//
	// if (attach == onPlayListener.isAttached()) {
	// return;
	// }
	//
	// try {
	// ServiceAccess service = getELActivity().getServiceAccess();
	// if (service != null) {
	// service.setAudioListener(attach ? onPlayListener : null);
	// onPlayListener.setAttached(attach);
	// }
	// } catch (RemoteException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	private void loadData(int index, String title, String data) {

		String html = assembleHtmlScript(data);
		webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);

		// playPlay.setEnabled(true);
		// playPlay.setSelected(false);
		//
		// // if (audioSlowDialog == -1 && audioExplanation == -1 &&
		// audioFastDialog == -1) {
		// if (audioNavigate == AudioNavigateData.DISABLE) {
		// playNavigate.setEnabled(false);
		// } else {
		// playNavigate.setEnabled(true);
		// }
		//
		textView.setVisibility(View.VISIBLE);
		textView.setText(String.format("%d. %s", index, title));

//		if (getELActivity().getSharedPreferences().getBoolean(Setting.CONTENT_HIDE_TITLE, false)) {
//			// Message msg = Message.obtain(handler, MSG_HIDE_TITLE);
//			// handler.sendMessageDelayed(msg, 1500);
//		}
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

	// private void setAudio(int index) {
	// try {
	// getELActivity().getServiceAccess().setAudio(index);
	// } catch (RemoteException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	private void playAudio() {

		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_PLAY));
		//
		// try {
		// getELActivity().getServiceAccess().playAudio();
		// playPlay.setSelected(true);
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private void pauseAudio() {

		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_PLAY));

		// try {
		// getELActivity().getServiceAccess().pauseAudio();
		// playPlay.setSelected(false);
		//
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private void stopAudio() {
		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_STOP));
		// try {
		// getELActivity().getServiceAccess().stopAudio();
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private void seekAudio(int type) {

		// Intent intent = new Intent(AudioAction.ACTION_AUDIO_SEEK);
		// intent.putExtra(AudioAction.DATA_POSITION, position * 1000);
		Intent intent = new Intent(AudioAction.ACTION_AUDIO_NAVIGATE);
		intent.putExtra(AudioAction.DATA_NAVIGATE, type);

		sendBroadcast(intent);

		// try {
		// getELActivity().getServiceAccess().seekAudio(position * 1000);
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	// private PlayState getPlayState() {
	// try {
	// return
	// PlayState.getState(getELActivity().getServiceAccess().getPlayState());
	// } catch (RemoteException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	// }

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

		// Intent intent = new Intent(AudioAction.ACTION_UPDATE_UI);
		// intent.putExtra(AudioAction.DATA_STATE,
		// UIState.AUDIO_WINDOW_CLOSE.getId());
		// sendBroadcast(intent);

		// setAudioPlayListener(false);
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

	// private void togglePlay() {
	//
	// if (playPlay.isSelected()) {
	// pauseAudio();
	// } else {
	// if (getPlayState() != PlayState.NONE) {
	// playAudio();
	// } else {
	// Bundle args = new Bundle();
	// args.putInt(FragmentArgument.ACTION,
	// FragmentArgument.Action.PLAY.getId());
	// args.putInt(FragmentArgument.INDEX, audioIndex);
	// handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));
	// }
	// }
	// }

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
		Toast.makeText(getELActivity(), String.format("ERROR:%d Extra:%d", what, extra), Toast.LENGTH_SHORT).show();
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

		// if (audioSlowDialog == -1 && audioExplanation == -1 &&
		// audioFastDialog == -1) {
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

	//
	// protected void onPlayStateChanged(int state) {
	// if (state == PlayState.PREPARED.getId()) {
	// playPlay.setEnabled(true);
	// // playNavigate.setEnabled(true);
	// } else if (state == PlayState.PLAYING.getId()) {
	// playPlay.setSelected(true);
	// // playNavigate.setEnabled(true);
	// } else if (state == PlayState.PAUSED.getId()) {
	// playPlay.setSelected(false);
	// // playNavigate.setEnabled(false);
	// } else if (state == PlayState.NONE.getId()) {
	// playPlay.setSelected(false);
	// // playNavigate.setEnabled(false);
	// }
	// }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			seekAudio(progress);
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

		// boolean random =
		// getELActivity().getSharedPreferences().getBoolean(CommonConsts.Setting.PLAY_RANDOM_ORDER,
		// false);
		//
		// Cursor cursor = Utils.getNextAudio(getELActivity(), audioIndex, new
		// String[] { "idx" }, random, true);
		// if (cursor != null) {
		// try {
		// if (cursor.moveToFirst()) {
		// Bundle args = new Bundle();
		// args.putInt(FragmentArgument.ACTION,
		// FragmentArgument.Action.PLAY.getId());
		// args.putInt(FragmentArgument.INDEX, cursor.getInt(0));
		//
		// handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));
		// }
		// } finally {
		// cursor.close();
		// }
		// }
	}

	private void getPrevAudio() {
		sendBroadcast(new Intent(AudioAction.ACTION_AUDIO_PREV));

		// boolean random =
		// getELActivity().getSharedPreferences().getBoolean(CommonConsts.Setting.PLAY_RANDOM_ORDER,
		// false);
		//
		// Cursor cursor = Utils.getNextAudio(getELActivity(), audioIndex, new
		// String[] { "idx" }, random, false);
		// if (cursor != null) {
		// try {
		// if (cursor.moveToFirst()) {
		// Bundle args = new Bundle();
		// args.putInt(FragmentArgument.ACTION,
		// FragmentArgument.Action.PLAY.getId());
		// args.putInt(FragmentArgument.INDEX, cursor.getInt(0));
		//
		// handler.sendMessage(Message.obtain(handler, MSG_INDEX, args));
		// }
		// } finally {
		// cursor.close();
		// }
		// }
	}

	@Override
	public void onPause() {
		super.onPause();

		Intent intent = new Intent(AudioAction.ACTION_UPDATE_UI);
		intent.putExtra(AudioAction.DATA_TYPE, UpdateUIType.AUDIO_WINDOW_CLOSE.getId());
		sendBroadcast(intent);

		// setAudioPlayListener(false);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Intent intent = new Intent(AudioAction.ACTION_AUDIO_QUERY);
		// sendBroadcast(intent);

		Intent intent = new Intent(AudioAction.ACTION_UPDATE_UI);
		intent.putExtra(AudioAction.DATA_TYPE, UpdateUIType.AUDIO_WINDOW_SHOW.getId());
		sendBroadcast(intent);

		if (playShuffle != null) {
			playShuffle.setSelected(getELActivity().getSharedPreferences().getBoolean(Setting.PLAY_RANDOM_ORDER, false));
		}

		// setAudioPlayListener(true);
	}

	protected void onHideTitle() {
		textView.setVisibility(View.GONE);
	}

	// protected void onAudioPlaying() {
	// setAudioPlayListener(true);
	// }

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
				} else if (type == UpdateAudioType.AUDIO_CHANGED.getId()) {
					onAudioChanged(intent);
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

		// if (state == PlayState.PLAYING.getId()) {
		// onPlayPlaying(intent.getIntExtra(AudioAction.DATA_POSITION, 0));
		// if (!playPlay.isSelected()) {
		// playPlay.setSelected(true);
		// }
		// } else
		if (state == PlayState.PREPARED.getId()) {
			// int index = intent.getIntExtra(AudioAction.DATA_ID, -1);
			// if (index != audioIndex) {
			// onIndex(index);
			// }
			// onPlayPrepared(intent.getIntExtra(AudioAction.DATA_DURATION, 0),
			// intent.getIntExtra(AudioAction.DATA_POSITION, 0));
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

	private void onAudioChanged(Intent intent) {
		int index = intent.getIntExtra(AudioAction.DATA_ID, -1);
		if (index != audioIndex) {
			onIndex(index);
		} else {
			//
		}
		audioNavigate = intent.getIntExtra(AudioAction.DATA_NAVIGATE, AudioNavigateData.DISABLE);
		onPlayPrepared(intent.getIntExtra(AudioAction.DATA_DURATION, 0), intent.getIntExtra(AudioAction.DATA_POSITION, 0));
		onStateChanged(intent);
	}

}
