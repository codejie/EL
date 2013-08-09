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
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.RemoteException;

public class AudioPlayer implements OnCompletionListener, OnSeekCompleteListener, OnErrorListener {

	private class TickCounter extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			 while (player.isPlaying()) {
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
	private int audioPosition = -1;
	private String audioTitle = null;

	
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
	}
	
	public void setOnPlayAudioListener(OnPlayAudioListener listener) {
		this.listener = listener;
	}
	
	public void setData(int index, int position) {

		if (player.isPlaying()) {
			player.stop();
		}
		
		try {
			player.reset();
		
			Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_ESL, index);			
			Cursor cursor = context.getContentResolver().query(uri, new String[] { "title", "audio" }, null, null, null);
			try {
				if (cursor.moveToFirst()) {
					audioIndex = index;
					audioPosition = position;
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
		if (player.isPlaying())
			return;
						
		player.start();
		new TickCounter().execute();
	}
	
	public void pause() {
		if (player.isPlaying()) {
			player.pause();
		}
	}
	
	public void stop() {
		if (player.isPlaying()) {
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
		return player.isPlaying();
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

}
