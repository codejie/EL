package jie.android.el.service;

import java.io.File;
import java.io.IOException;

import jie.android.el.CommonConsts;
import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.Message;
import android.os.RemoteException;

public class AudioPlayer implements OnCompletionListener, OnSeekCompleteListener, OnErrorListener {

	private class TickCounterTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			 while (isAudioPlaying) {
				if (listener != null) {
					try {
						listener.onPlaying(player.getCurrentPosition());
					} catch (DeadObjectException e) {
						listener = null;
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return null;
		}
	}
	
	private Context context = null;
	
	private MediaPlayer player = null;
	private OnPlayAudioListener listener = null;
	
	private int audioIndex = -1;
	private String audioTitle = null;

	private TickCounterTask tickTask = null;
	private boolean isAudioPlaying = false; 
	
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
			} catch (DeadObjectException e) {
				listener = null;								
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!context.getSharedPreferences("el", 0).getBoolean(CommonConsts.Setting.PLAY_STOP_AFTER_CURRENT, false)) {
			getNextAudio();
		}
		
	}
	
	public void setOnPlayAudioListener(OnPlayAudioListener listener) {
		this.listener = listener;
	}
	
	public void setData(int index) {

		if (isPlaying()) {
			player.stop();
		}
		
		try {
			player.reset();
		
			Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL, index);			
			Cursor cursor = context.getContentResolver().query(uri, new String[] { "title", "audio" }, null, null, null);
			try {
				if (cursor.moveToFirst()) {
					audioIndex = index;
					audioTitle = cursor.getString(0);

					String audio = cursor.getString(1);
					audio = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_EL + audio;
					player.setDataSource(audio);
					player.prepare();
					if (listener != null) {
						listener.onPrepared(player.getDuration());
					}					
				}
			} catch (DeadObjectException e) {
				listener = null;				
			} catch (RemoteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void play() {
		if (isPlaying())
			return;
						
		player.start();
		
		isAudioPlaying = true;
		
		tickTask = new TickCounterTask();
		tickTask.execute();
	}
	
	public void pause() {
		if (isPlaying()) {
			player.pause();
		}
	}
	
	public void stop() {
		if (isPlaying()) {
			isAudioPlaying = false;
//			tickTask.cancel(true);
			player.stop();
		}
	}
	
	public void seekTo(int msec) {
		player.seekTo(msec);
	}
	
	public void reset() {
		player.release();
	}
	
	public boolean isPlaying() {		
		return isAudioPlaying && player.isPlaying();
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
		boolean random = context.getSharedPreferences("el", 0).getBoolean(CommonConsts.Setting.PLAY_RANDOM_ORDER, false);

		Cursor cursor = Utils.getNextAudio(context, audioIndex, new String[] { "idx", "title", "audio" }, random, true);		
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					player.reset();
				
					audioIndex = cursor.getInt(0);
					audioTitle = cursor.getString(1);
					String audio = cursor.getString(2);
					audio = Utils.getExtenalSDCardDirectory() + CommonConsts.AppArgument.PATH_EL + audio;
					player.setDataSource(audio);
					try {
						if (listener != null) {
							listener.onAudioChanged(audioIndex);
						}
						
						player.prepare();

						if (listener != null) {
							listener.onPrepared(player.getDuration());
						}					
					} catch (DeadObjectException e) {
						listener = null;				
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					play();
				}			
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
}
