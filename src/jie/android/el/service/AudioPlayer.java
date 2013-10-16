package jie.android.el.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import jie.android.el.CommonConsts;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.CommonConsts.UpdateAudioType;
import jie.android.el.CommonConsts.UpdateUIType;
import jie.android.el.R;
import jie.android.el.CommonConsts.AppArgument;
import jie.android.el.CommonConsts.AudioAction;
import jie.android.el.CommonConsts.ListItemFlag;
import jie.android.el.CommonConsts.NotificationAction;
import jie.android.el.CommonConsts.NotificationType;
import jie.android.el.CommonConsts.PlayState;
import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.Utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

public class AudioPlayer {

	private static final String Tag = AudioPlayer.class.getSimpleName();
	
	
	private class TickCounterRunnable implements Runnable {

		@Override
		public void run() {
			//while (listener != null && isPlaying()) {
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
//	private OnPlayAudioListener listener = null;

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

		// showNotification(false);

//		changePlayState(PlayState.NONE);
	}

//	public void setOnPlayAudioListener(OnPlayAudioListener listener) {
//		this.listener = listener;
//
//		if (this.listener != null) {
////			if (isPlaying() || isPause()) {
////				try {
////					this.listener.onIsPlaying(audioIndex, playState.getId(), player.getDuration(), player.getCurrentPosition());
////				} catch (RemoteException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////			}
//
//			if (isPlaying()) {
//				new Thread(new TickCounterRunnable()).start();
//			}
//
//			showNotification(false);
//		} else {
//			if (isPlaying() || isPause()) {
//				showNotification(true);
//			}
//		}
//	}

	public void setData(int index, boolean forcePlay) {

		releasePlayer();
		
		if (index == -1) {
			return;
		}

		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL, index);
		Cursor cursor = context.getContentResolver().query(uri, new String[] { "title", "audio", "slowdialog", "explanations", "fastdialog" }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				
				audioIndex = index;
				audioTitle = cursor.getString(0);
				
				audioSlowDialog = cursor.getInt(2);
				audioExplanation = cursor.getInt(3);
				audioFastDialog = cursor.getInt(4);					
				
				if (prepareData(cursor.getString(1))) {
					
//					audioSlowDialog = cursor.getInt(2);
//					audioExplanation = cursor.getInt(3);
//					audioFastDialog = cursor.getInt(4);					

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

//		audioIndex = index;
//		audioTitle = title;

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

//			if (listener != null) {
//				listener.onPrepared(player.getDuration());
//			}

			onAudioChanged();
//			Intent pp = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
//			pp.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.AUDIO_CHANGED.getId());
//			pp.putExtra(AudioAction.DATA_ID, audioIndex);
//			pp.putExtra(AudioAction.DATA_TITLE, audioTitle);
//			pp.putExtra(AudioAction.DATA_POSITION, 0);
//			pp.putExtra(AudioAction.DATA_DURATION, player.getDuration());
//			pp.putExtra(AudioAction.DATA_STATE, playState.getId());				
//			sendBroadcast(pp);
			
//			bundle.putInt(AudioAction.DATA_ID, audioIndex);
//			bundle.putString(AudioAction.DATA_TITLE, audioTitle);
//			bundle.putInt(AudioAction.DATA_POSITION, 0);
//			bundle.putInt(AudioAction.DATA_DURATION, player.getDuration());
			
			changePlayState(PlayState.PREPARED, null);

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
//		catch (DeadObjectException e) {
//			listener = null;
//			e.printStackTrace();
//			return false;
//		} catch (RemoteException e) {
//			e.printStackTrace();
//			return false;
//		}

		return true;
	}

	public void play() {
		if (isPlaying())
			return;

		player.start();
		
		changePlayState(PlayState.PLAY);

//		if (listener != null) {
		if (isAudioWindowShow) {
			new Thread(new TickCounterRunnable()).start();
		}
		
		showNotification(true);
	}

	public void pause() {
		if (isPlaying()) {
			player.pause();

			changePlayState(PlayState.PAUSED);
			//if (listener == null) {
			showNotification(true);
		}
	}

	protected void togglePlay() {
		if (isPlaying()) {
			player.pause();
			changePlayState(PlayState.PAUSED);
			//if (listener == null) {
		} else {
			player.start();
			changePlayState(PlayState.PLAY);

			//if (listener != null) {
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
		case 0://slowdialog
			if (audioSlowDialog != -1) {
				seekTo(audioSlowDialog);
			}
			break;
		case 1://explanation
			if (audioExplanation != -1) {
				seekTo(audioExplanation);
			}
			break;
		case 2://fastdialog
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
//				if (listener != null) {
//					try {
//						listener.onAudioChanged(cursor.getInt(0));
//					} catch (RemoteException e) {
//						e.printStackTrace();
//					}
//				}
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
		Intent intent = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
		intent.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.STATE_CHANGED.getId());
		intent.putExtra(AudioAction.DATA_STATE, PlayState.PLAYING.getId());
		intent.putExtra(AudioAction.DATA_POSITION, position);
		sendBroadcast(intent);
//		
//		if (listener != null) {
//			try {
//				listener.onPlaying(position);
//			} catch (RemoteException e) {
//				listener = null;
//				e.printStackTrace();
//			}
//		}
	}

	protected void onPlaySeekComplete(int msec) {
//		Intent intent = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
//		intent.putExtra(AudioAction.DATA_STATE, PlayState.PLAYING.getId());
//		intent.putExtra(AudioAction.DATA_POSITION, msec);
		
//		if (listener != null) {
//			try {
//				listener.onSeekTo(msec);
//			} catch (RemoteException e) {
//				listener = null;
//				e.printStackTrace();
//			}
//		}
	}

	protected void onPlayCompletion() {

		releasePlayer();
		
		changePlayState(PlayState.COMPLETED);

//		if (listener != null) {
//			try {
//				listener.onCompleted();
//			} catch (RemoteException e) {
//				listener = null;
//				e.printStackTrace();
//			}
//		}

		if (!Utils.getSharedPreferences(context).getBoolean(CommonConsts.Setting.PLAY_STOP_AFTER_CURRENT, false)) {
			getNextAudio(true);
		} else {			
			showNotification(false);
		}
	}

	private void onPlayError(int what, int extra) {

		releasePlayer();

		changePlayState(PlayState.ERROR);
		
		showNotification(false);

//		if (listener != null) {
//			try {
//				listener.onError(what, extra);
//			} catch (RemoteException e) {
//				listener = null;
//				e.printStackTrace();
//			}
//		}
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
//		
//		
//		if (listener != null) {
//			try {
//				listener.onStateChanged(playState.getId());
//			} catch (DeadObjectException e) {
//				listener = null;
//			} catch (RemoteException e) {
//				listener = null;
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

//	public void onNotificationClick(final String action) {
////		if (action.equals(NotificationAction.ACTION_CLICK_PLAY)) {
////			this.togglePlay();
////		} else if (action.equals(NotificationAction.ACTION_CLICK_NEXT)) {
////			this.getNextAudio(true);
////		} else if (action.equals(NotificationAction.ACTION_CLICK_PREV)) {
////			this.getNextAudio(false);
////		} else if (action.equals(NotificationAction.ACTION_CLICK_CLOSE)) {
////			this.stop();
////			showNotification(false);
////		}
//	}
	
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
		intent.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.AUDIO_CHANGED.getId());
		intent.putExtra(AudioAction.DATA_ID, audioIndex);
		intent.putExtra(AudioAction.DATA_TITLE, audioTitle);
		if (isPlaying() || isPause()) {
			intent.putExtra(AudioAction.DATA_POSITION, player.getCurrentPosition());
			intent.putExtra(AudioAction.DATA_DURATION, player.getDuration());
		} else {
			intent.putExtra(AudioAction.DATA_POSITION, 0);
			intent.putExtra(AudioAction.DATA_DURATION, 0);
		}
		int n = 0;
		n |= (audioSlowDialog != -1 ? 1 : 0);
		n |= (audioExplanation != -1 ? 2 : 0);
		n |= (audioFastDialog != -1 ? 4 : 0);
		intent.putExtra(AudioAction.DATA_NAVIGATE, n);
		intent.putExtra(AudioAction.DATA_STATE, playState.getId());				
		sendBroadcast(intent);		
	}

	public void onAction(Intent intent) {
		final String action = intent.getAction();
		if (action.equals(AudioAction.ACTION_AUDIO_SET)) {
			int index = intent.getIntExtra(AudioAction.DATA_ID, -1);
			if (index == -1) {
				index = getLastPlayAudio();				
			}
			setData(index, false);
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
		} else if (action.equals(AudioAction.ACTION_AUDIO_NAVIGATE)) {
			int nv = intent.getIntExtra(AudioAction.DATA_NAVIGATE, -1);
			if (nv != -1) {
				navigateTo(nv);
			}				
		} else if (action.equals(AudioAction.ACTION_AUDIO_QUERY)) {
			onAudioChanged();
//			Intent pp = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
//			pp.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.AUDIO_CHANGED.getId());
//			pp.putExtra(AudioAction.DATA_ID, audioIndex);
//			pp.putExtra(AudioAction.DATA_TITLE, audioTitle);
//			if (isPlaying() || isPause()) {
//				pp.putExtra(AudioAction.DATA_POSITION, player.getCurrentPosition());
//				pp.putExtra(AudioAction.DATA_DURATION, player.getDuration());
//			} else {
//				pp.putExtra(AudioAction.DATA_POSITION, 0);
//				pp.putExtra(AudioAction.DATA_DURATION, 0);				
//			}
//			pp.putExtra(AudioAction.DATA_STATE, playState.getId());				
//			sendBroadcast(pp);			
		}
	}

	public void onUIUpdate(Intent intent) {
		int state = intent.getIntExtra(AudioAction.DATA_TYPE, -1);
		if (state == UpdateUIType.AUDIO_WINDOW_SHOW.getId()) {
			isAudioWindowShow  = true;

			onAudioChanged();
//			Intent pp = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
//			pp.putExtra(AudioAction.DATA_TYPE, UpdateAudioType.AUDIO_CHANGED.getId());
//			pp.putExtra(AudioAction.DATA_ID, audioIndex);
//			pp.putExtra(AudioAction.DATA_TITLE, audioTitle);
//			if (isPlaying() || isPause()) {
//				pp.putExtra(AudioAction.DATA_POSITION, player.getCurrentPosition());
//				pp.putExtra(AudioAction.DATA_DURATION, player.getDuration());
//			} else {
//				pp.putExtra(AudioAction.DATA_POSITION, 0);
//				pp.putExtra(AudioAction.DATA_DURATION, 0);				
//			}
//			pp.putExtra(AudioAction.DATA_STATE, playState.getId());				
//			sendBroadcast(pp);			
			
			
//			if (isPlaying() || isPause()) {
//				Intent pp = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
//				pp.putExtra(AudioAction.DATA_ID, audioIndex);
//				pp.putExtra(AudioAction.DATA_POSITION, player.getCurrentPosition());
//				pp.putExtra(AudioAction.DATA_DURATION, player.getDuration());
//				pp.putExtra(AudioAction.DATA_STATE, PlayState.PREPARED.getId());				
//				sendBroadcast(pp);
//				
//				Intent st = new Intent(AudioAction.ACTION_UPDATE_AUDIO);
//				st.putExtra(AudioAction.DATA_STATE, playState.getId());
//				sendBroadcast(st);
//			}
			if (isPlaying()) {
				new Thread(new TickCounterRunnable()).start();
			}
			
			showNotification(false);
			
		} else {
			isAudioWindowShow = false;
			
			if (isPlaying() || isPause()) {
				showNotification(true);				
			}
		}
	}
	
	public void sendBroadcast(Intent intent) {
		if (context != null) {
			context.sendBroadcast(intent);
		}
	}

}
