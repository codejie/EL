package jie.android.el.service;

import java.io.File;
import java.io.IOException;

import jie.android.el.CommonConsts;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.CommonConsts.UpdateAudioType;
import jie.android.el.CommonConsts.UpdateUIType;
import jie.android.el.R;
import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.CommonConsts.AudioNavigateData;
import jie.android.el.CommonConsts.ListItemFlag;
import jie.android.el.CommonConsts.NotificationAction;
import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.CommonConsts.PlayState;
import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class AudioPlayer {

	private static final String Tag = AudioPlayer.class.getSimpleName();

	private class TickCounterRunnable implements Runnable {

		@Override
		public void run() {
			while (isAudioWindowShow && isPlaying()) {
				try {
					onPlayPlaying(player.getCurrentPosition());

					Thread.sleep(777);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Context context = null;

	private MediaPlayer player = null;

	private int audioIndex = -1;
	private String audioTitle;
	private int audioSlowDialog = -1;
	private int audioExplanation = -1;
	private int audioFastDialog = -1;

	private PlayState playState = PlayState.NONE;

	private boolean isAudioWindowShow = false;

	public AudioPlayer(Context context) {
		this.context = context;
	}

	public void release() {
		releasePlayer();
	}

	private void initPlayer() {

		player = new MediaPlayer();

		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				onPlayCompletion();
			}
		});
		player.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer mp) {
				onPlaySeekComplete(player.getCurrentPosition());
			}
		});
		player.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				onPlayError(what, extra);
				return true;
			}
		});

		// player.reset();
	}

	private boolean isPlayerAvailable() {
		if (player != null) {
			synchronized (player) {
				return player != null;
			}
		}
		return false;
	}

	private void releasePlayer() {
		if (player != null) {
			synchronized (player) {
				player.release();
				player = null;
			}
		}
		onAudioChanged();
	}

	public void setData(int index, boolean forcePlay) {

		changePlayState(PlayState.NONE);

		releasePlayer();

		if (index == -1) {
			return;
		}

		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL, index);
		Cursor cursor = context.getContentResolver()
				.query(uri, new String[] { "title", "audio", "slowdialog", "explanations", "fastdialog" }, null, null, null);
		try {
			if (cursor.moveToFirst()) {

				audioIndex = index;
				audioTitle = cursor.getString(0);

				audioSlowDialog = cursor.getInt(2) * 1000;
				audioExplanation = cursor.getInt(3) * 1000;
				audioFastDialog = cursor.getInt(4) * 1000;

				if (prepareData(cursor.getString(1))) {
					SharedPreferences prefs = Utils.getSharedPreferences(this.context);

					if (prefs.getBoolean(Setting.PLAY_AUTO_PLAY, true) || forcePlay) {
						play();
					}
				} else {
					showWarningNotification("Can't play audio file - " + cursor.getString(0));
					onPlayError(-1, -1);
				}
			}
		} finally {
			cursor.close();
		}
	}

	private boolean prepareData(String audio) {
		// check audio
		audio = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_EL + audio;
		File f = new File(audio);
		if (!f.exists()) {
			return false;
		}

		initPlayer();

		try {
			// player.reset();
			player.setDataSource(audio);
			// player.prepareAsync();
			player.prepare();

			changePlayState(PlayState.PREPARED, null);
			onAudioChanged();

			setAudioPlayFlag(audioIndex, true);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void play() {
		if (isPlaying())
			return;

		player.start();

		changePlayState(PlayState.PLAY);

		if (isAudioWindowShow) {
			new Thread(new TickCounterRunnable()).start();
		}

		showNotification(true);
	}

	public void pause() {
		if (isPlaying()) {
			player.pause();

			changePlayState(PlayState.PAUSED);
			showNotification(true);
		}
	}

	protected void togglePlay() {
		if (isPlaying()) {
			player.pause();
			changePlayState(PlayState.PAUSED);
		} else {
			player.start();
			changePlayState(PlayState.PLAY);

			if (isAudioWindowShow) {
				new Thread(new TickCounterRunnable()).start();
			}
		}
		showNotification(true);
	}

	public void stop() {
		if (isPlaying() || isPause()) {
			player.stop();
		}

		changePlayState(PlayState.STOP);

		releasePlayer();
	}

	public void seekTo(int msec) {
		player.seekTo(msec);
		if (!isPlaying()) {
			onPlayPlaying(msec);
		}
	}

	private void navigateTo(int nv) {
		switch (nv) {
		case AudioNavigateData.SLOWDIALOG:// slowdialog
			if (audioSlowDialog != -1) {
				seekTo(audioSlowDialog);
			}
			break;
		case AudioNavigateData.EXPLANATION:// explanation
			if (audioExplanation != -1) {
				seekTo(audioExplanation);
			}
			break;
		case AudioNavigateData.FASTDIALOG:// fastdialog
			if (audioFastDialog != -1) {
				seekTo(audioFastDialog);
			}
			break;
		default:
			break;
		}
	}

	public boolean isPlaying() {
		if (player != null) {
			synchronized (player) {
				return (player != null && playState == PlayState.PLAY);
			}
		}
		return false;
	}

	public boolean isPause() {
		if (player != null) {
			synchronized (player) {
				return (player != null && playState == PlayState.PAUSED);
			}
		}
		return false;
	}

	public int getAudioIndex() {
		return audioIndex;
	}

	public String getAudioTitle() {
		return audioTitle;
	}

	public int getDuration() {
		return player.getDuration();
	}

	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	private void getNextAudio(boolean next) {

		if (audioIndex == -1) {
			return;
		}

		boolean random = Utils.getSharedPreferences(context).getBoolean(CommonConsts.Setting.PLAY_RANDOM_ORDER, false);

		Cursor cursor = Utils.getNextAudio(context, audioIndex, new String[] { "idx" }, random, next);

		try {
			if (cursor.moveToFirst()) {
				setData(cursor.getInt(0), false);
			}
		} finally {
			cursor.close();
		}
	}

	private void showNotification(boolean show) {

		Intent intent = null;

		if (!isAudioWindowShow && show) {
			intent = new Intent(NotificationAction.ACTION_SHOW);
			intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.PLAY.getId());
			intent.putExtra(NotificationAction.DATA_TITLE, String.format("%s.%s", audioIndex, audioTitle));
			if (playState == PlayState.PLAY) {
				intent.putExtra(NotificationAction.DATA_TEXT, context.getResources().getString(R.string.el_play_el_is_playing));
				intent.putExtra(NotificationAction.DATA_STATE, true);
			} else {
				intent.putExtra(NotificationAction.DATA_TEXT, context.getResources().getString(R.string.el_play_el_pause_playback));
				intent.putExtra(NotificationAction.DATA_STATE, false);
			}
		} else {
			intent = new Intent(NotificationAction.ACTION_REMOVE);
			intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.PLAY.getId());
			intent.putExtra(NotificationAction.DATA_ID, 0);
		}

		sendBroadcast(intent);
	}

	private void showWarningNotification(final String text) {
		Intent intent = new Intent(NotificationAction.ACTION_SHOW);
		intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.WARNING.getId());
		intent.putExtra(NotificationAction.DATA_TITLE, text);
		intent.putExtra(NotificationAction.DATA_TEXT, "EL Warning");

		sendBroadcast(intent);
	}

	private void setAudioPlayFlag(int index, boolean play) {
		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL_PLAYFLAG, index);
		context.getContentResolver().update(uri, null, null, null);
	}

	public PlayState getPlayState() {
		return playState;
	}

	protected void onPlayPlaying(int position) {
		Intent intent = new Intent(AudioAction.ACTION_UPDATE_AUDIO_PLAYING);
		intent.putExtra(AudioAction.DATA_POSITION, position);
		sendBroadcast(intent);
	}

	protected void onPlaySeekComplete(int msec) {
	}

	protected void onPlayCompletion() {

		changePlayState(PlayState.COMPLETED);

		releasePlayer();

		if (!Utils.getSharedPreferences(context).getBoolean(CommonConsts.Setting.PLAY_STOP_AFTER_CURRENT, false)) {
			getNextAudio(true);
		} else {
			showNotification(false);
		}
	}

	private void onPlayError(int what, int extra) {

		changePlayState(PlayState.ERROR);

		releasePlayer();

		showNotification(false);
	}

	private void changePlayState(PlayState state) {
		changePlayState(state, null);
	}

	private void changePlayState(PlayState state, Bundle bundle) {
		playState = state;

		Intent intent = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
		intent.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.STATE_CHANGED.getId());
		intent.putExtra(AudioAction.DATA_STATE, state.getId());
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		sendBroadcast(intent);
	}

	private int getLastPlayAudio() {

		Log.d(Tag, "getLastPlayAudio() - playflag");

		final String selection = "flag & " + ListItemFlag.LAST_PLAY + "=" + ListItemFlag.LAST_PLAY;

		Cursor cursor = context.getContentResolver().query(ELContentProvider.URI_EL_ESL, new String[] { "idx" }, selection, null, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
		} finally {
			cursor.close();
		}

		Log.d(Tag, "getLastPlayAudio() - random");

		cursor = context.getContentResolver().query(ELContentProvider.URI_EL_ESL_RANDOM, null, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
		} finally {
			cursor.close();
		}

		return -1;
	}

	private void onAudioChanged() {
		Intent intent = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
		if (playState == PlayState.PREPARED || playState == PlayState.PLAY || playState == PlayState.PAUSED) {
			intent.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.AUDIO_CHANGED_OPEN.getId());
			intent.putExtra(AudioAction.DATA_ID, audioIndex);
			intent.putExtra(AudioAction.DATA_TITLE, audioTitle);
			if (player != null) {
				intent.putExtra(AudioAction.DATA_POSITION, player.getCurrentPosition());
				intent.putExtra(AudioAction.DATA_DURATION, player.getDuration());
			} else {
				intent.putExtra(AudioAction.DATA_POSITION, 0);
				intent.putExtra(AudioAction.DATA_DURATION, 0);
			}
			int n = 0;
			n |= (audioSlowDialog > 0 ? 1 : 0);
			n |= (audioExplanation > 0 ? 2 : 0);
			n |= (audioFastDialog > 0 ? 4 : 0);
			intent.putExtra(AudioAction.DATA_NAVIGATE, n);
			intent.putExtra(AudioAction.DATA_STATE, playState.getId());
		} else {
			intent.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.AUDIO_CHANGED_CLOSE.getId());
		}
		sendBroadcast(intent);
	}

	private void notifyAudioSet() {
		Intent intent = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
		intent.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.AUDIO_IS_SET.getId());
		this.sendBroadcast(intent);
	}

	public void onAction(Intent intent) {
		final String action = intent.getAction();
		if (action.equals(AudioAction.ACTION_AUDIO_SET)) {
			int index = intent.getIntExtra(AudioAction.DATA_ID, -1);
			if (index == -1) {
				index = getLastPlayAudio();
			}
			setData(index, false);

			notifyAudioSet();

		} else if (action.equals(AudioAction.ACTION_AUDIO_PLAY)) {
			if (isPlaying() || isPause()) {
				togglePlay();
			} else {
				if (audioIndex == -1) {
					audioIndex = getLastPlayAudio();
				}
				setData(audioIndex, true);
			}
		} else if (action.equals(AudioAction.ACTION_AUDIO_NEXT)) {
			if (audioIndex == -1) {
				audioIndex = getLastPlayAudio();
			}
			getNextAudio(true);
		} else if (action.equals(AudioAction.ACTION_AUDIO_PREV)) {
			if (audioIndex == -1) {
				audioIndex = getLastPlayAudio();
			}
			getNextAudio(false);
		} else if (action.equals(AudioAction.ACTION_AUDIO_STOP)) {
			stop();
			showNotification(false);
		} else if (action.equals(AudioAction.ACTION_AUDIO_SEEK)) {
			int pos = intent.getIntExtra(AudioAction.DATA_POSITION, -1);
			if (pos != -1) {
				seekTo(pos);
			}
		} else if (action.startsWith(AudioAction.ACTION_AUDIO_NAVIGATE)) {
			int nv = intent.getIntExtra(AudioAction.DATA_NAVIGATE, -1);
			if (nv != -1) {
				navigateTo(nv);
			}
		} else if (action.equals(AudioAction.ACTION_AUDIO_QUERY)) {
			onAudioChanged();
		} else if (action.equals(AudioAction.ACTION_AUDIO_FORCE_PAUSE)) {
			if (isPlaying()) {
				player.pause();
				changePlayState(PlayState.PAUSED);
			}
		}
	}

	public void onUIUpdate(Intent intent) {
		int state = intent.getIntExtra(AudioAction.DATA_TYPE, -1);
		if (state == UpdateUIType.AUDIO_WINDOW_SHOW.getId()) {
			isAudioWindowShow = true;
			if (isPlaying()) {
				new Thread(new TickCounterRunnable()).start();
			}

			showNotification(false);
		} else if (state == UpdateUIType.AUDIO_WINDOW_CLOSE.getId()) {
			isAudioWindowShow = false;

			if (isPlaying() || isPause()) {
				showNotification(true);
			}
		} else if (state == UpdateUIType.LIST_WINDOW_CREATED.getId()) {
			if (isPlaying() || isPause()) {
				notifyAudioSet();
			}
		}
	}

	public void sendBroadcast(Intent intent) {
		if (context != null) {
			context.sendBroadcast(intent);
		}
	}

}
