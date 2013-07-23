package jie.android.el.service;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.AsyncTask;
import android.os.RemoteException;

public class AudioPlayer implements OnCompletionListener, OnSeekCompleteListener, OnErrorListener {
		
	private Context context = null;
	
	private MediaPlayer player = null;
	private PlayAudioListener listener = null;
		
	
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
			String what = String.format("code:%d extra:%d",  arg1, arg2);
			try {
				listener.onError(what);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		if (listener != null) {
			try {
				listener.onCompleted();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setOnPlayAudioListener(PlayAudioListener listener) {
		this.listener = listener;
	}
	
	public void setData(final String file){

		if (player.isPlaying()) {
			player.stop();
		}
		
		try {
			player.setDataSource(file);
			player.prepare();
			
			if (listener != null) {
				listener.onPrepared(player.getDuration());
			}
		} catch (RemoteException e) {
			
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
		player.start();
	}
	
	public void pause() {
		player.pause();
	}
	
	public void stop() {
		player.stop();
	}
	
	public void seekTo(int msec) {
		player.seekTo(msec);
	}
	
	public void reset() {
		player.release();
	}
}
