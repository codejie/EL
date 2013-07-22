package jie.android.el.service;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.AsyncTask;

public class AudioPlayer implements OnCompletionListener, OnSeekCompleteListener, OnErrorListener {
		
	private MediaPlayer player = null;
	private Context context = null;
	
	private AsyncTask<Integer, Integer, Boolean> PlayTask = new AsyncTask<Integer, Integer, Boolean>() {

//		private Thread counter = new Thread() {
//			
//		};
		
		@Override
		protected void onPreExecute() {
//			player.reset();
		}

		@Override
		protected void onCancelled(Boolean result) {
			int pos = player.getCurrentPosition();
			player.stop();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				
			} else {
				
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected Boolean doInBackground(Integer... arg0) {
			int pos = arg0[0].intValue();
			if (pos > 0) {
				player.seekTo(pos);
			}
			player.start();

			return Boolean.TRUE;
		}
		
	};	
	
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void setOnPlayAudioListener(PlayAudioListener listener) {
		
	}
	
	public void setData(final String file) {
		if (PlayTask.getStatus() == AsyncTask.Status.RUNNING) {
			PlayTask.cancel(true);
		}
		
		try {
			player.setDataSource(file);
			player.prepare();
			
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
		PlayTask.execute(0);
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
