package jie.android.el.service;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.AsyncTask;
import android.os.DeadObjectException;
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
	
	public void setData(int index, final String title, final String file){

		if (player.isPlaying()) {
			player.stop();
		}
		
		try {
			player.reset();
			
			player.setDataSource(file);
			player.prepare();
			
			audioIndex = index;
			audioTitle = title;
			
			if (listener != null) {
				try {
					listener.onPrepared(player.getDuration());
				} catch (DeadObjectException e) {
					listener = null;				
				} catch (RemoteException e) {
					e.printStackTrace();
				}					
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
