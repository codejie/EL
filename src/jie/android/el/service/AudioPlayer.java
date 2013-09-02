package jie.android.el.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import jie.android.el.CommonConsts;
import jie.android.el.R;
import jie.android.el.CommonConsts.AppArgument;
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
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.DeadObjectException;
import android.os.RemoteException;

public class AudioPlayer implements OnCompletionListener, OnSeekCompleteListener, OnErrorListener {

	private class TickCounterRunnable implements Runnable {

		@Override
		public void run() {
			 while (isPlaying() && listener != null) {
				try {
					listener.onPlaying(player.getCurrentPosition());
					
					Thread.sleep(777);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (DeadObjectException e) {
					listener = null;
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}			
		}		
	}
	
//	private class TickCounterTask extends AsyncTask<Void, Void, Void> {
//
//		@Override
//		protected Void doInBackground(Void... arg0) {
//			 while (isPlaying() && listener != null) {
//				try {
//					listener.onPlaying(player.getCurrentPosition());
//					
//					Thread.sleep(777);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				} catch (DeadObjectException e) {
//					listener = null;
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			return null;
//		}
//	}
	
	private Context context = null;
	
	private MediaPlayer player = null;
	private OnPlayAudioListener listener = null;
	
	private int audioIndex = -1;
	private String audioTitle = null;

//	private TickCounterTask tickTask = null;
	
	private PlayState playState = PlayState.NONE; 
	
	public AudioPlayer(Context context) {
		this.context = context;
		
		init();
	}

	public void release() {
		player.release();
	}

	private void init() {
		player = new MediaPlayer();
		
		player.setOnCompletionListener(this);
		player.setOnSeekCompleteListener(this);
		player.setOnErrorListener(this);
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		if (listener != null) {
			try {
				listener.onError(arg1, arg2);
			} catch (DeadObjectException e) {
				listener = null;				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		if (listener != null) {
			try {
				listener.onSeekTo(player.getCurrentPosition());
			} catch (DeadObjectException e) {
				listener = null;				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		if (listener != null) {
			try {
				listener.onCompleted();
				
				showNotification(false, null);

			} catch (DeadObjectException e) {
				listener = null;								
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		boolean next = context.getSharedPreferences(AppArgument.NAME, 0).getBoolean(CommonConsts.Setting.PLAY_STOP_AFTER_CURRENT, false);
		if (!next) {
			getNextAudio();
		}
		
	}
	
	public void setOnPlayAudioListener(OnPlayAudioListener listener) {
		this.listener = listener;
		
		if (isPlaying() && this.listener != null) {
			new Thread(new TickCounterRunnable()).start();
			
//			tickTask = new TickCounterTask();
//			tickTask.execute();
		}
		
	}
	
	public void setData(int index) {

		stop();

		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL, index);			
		Cursor cursor = context.getContentResolver().query(uri, new String[] { "title", "audio" }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				if(!prepareData(index, cursor.getString(0), cursor.getString(1))) {
					showWarningNotification("Can't play audio file - " + cursor.getShort(2));
				}
			}
		} finally {
			cursor.close();
		}	
	}
	
	private boolean prepareData(int index, String title, String audio) {

		audioIndex = index;
		audioTitle = title;
		//check audio
		audio = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_EL + audio;
		File f = new File(audio);
		if (!f.exists()) {
			return false;
		}
		
		try {
			player.reset();
		
			player.setDataSource(audio);
			player.prepare();
			if (listener != null) {
				listener.onPrepared(player.getDuration());
			}
			
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
		} catch (DeadObjectException e) {
			listener = null;	
			e.printStackTrace();
			return false;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public void play() {
		if (isPlaying())
			return;
					
		player.start();
		
		setAudioPlayFlag(audioIndex, true);
		
		playState = PlayState.PLAYING;

		new Thread(new TickCounterRunnable()).start();
//		tickTask = new TickCounterTask();
//		tickTask.execute();
		
		showNotification(true, context.getResources().getString(R.string.el_play_el_is_playing));		
	}
	
	public void pause() {
		if (isPlaying()) {
			player.pause();
			showNotification(true, context.getResources().getString(R.string.el_play_el_pause_playback));
			playState = PlayState.PAUSE;
		}
	}
	
	public void stop() {
		if (isPlaying() || isPause()) {			
			player.stop();
			playState = PlayState.NONE;			
			showNotification(false, null);			
		}
	}
	
	public void seekTo(int msec) {
		player.seekTo(msec);
	}
	
	public void reset() {
		player.release();
	}

	public boolean isPlaying() {
		return playState == PlayState.PLAYING; 
	}
	
	public boolean isPause() {
		return playState == PlayState.PAUSE;
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

	private void getNextAudio() {	
		boolean random = context.getSharedPreferences(AppArgument.NAME, 0).getBoolean(CommonConsts.Setting.PLAY_RANDOM_ORDER, false);

		Cursor cursor = Utils.getNextAudio(context, audioIndex, new String[] { "idx", "title", "audio" }, random, true);
		
		try {
			if (cursor.moveToFirst()) {
				if (prepareData(cursor.getInt(0), cursor.getString(1), cursor.getString(2))) {
					
					if (listener != null) {
						try {
							listener.onAudioChanged(audioIndex);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					} else {
						play();
					}
				} else {
					showWarningNotification("Can't play audio file - " + cursor.getShort(2));
				}
			}
		} finally {
			cursor.close();
		}		
	}
	
	private void showNotification(boolean show, final String title) {
		
		Intent intent = null;
		
		if (show) {
			intent = new Intent(NotificationAction.ACTION_SHOW);
			intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.PLAY.getId());
			intent.putExtra(NotificationAction.DATA_TITLE, String.format("%s.%s", audioIndex, audioTitle));
			intent.putExtra(NotificationAction.DATA_TEXT, title);
		} else {
			intent = new Intent(NotificationAction.ACTION_REMOVE);
			intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.PLAY.getId());
			intent.putExtra(NotificationAction.DATA_ID, 0);
		}
		
		context.sendBroadcast(intent);
	}
	
	private void showWarningNotification(final String text) {
		Intent intent = new Intent(NotificationAction.ACTION_SHOW);
		intent.putExtra(NotificationAction.DATA_TYPE, NotificationType.WARNING.getId());
		intent.putExtra(NotificationAction.DATA_TITLE, text);
		intent.putExtra(NotificationAction.DATA_TEXT, "EL Warning");
		
		context.sendBroadcast(intent);
	}

	private void setAudioPlayFlag(int index, boolean play) {
		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL_PLAYFLAG, index);
		context.getContentResolver().update(uri, null, null, null);
	}

	public PlayState getPlayState() {
		return playState;
	}
	
}
