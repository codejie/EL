package jie.android.el.service;

interface OnPlayAudioListener {
//	void onPrepared(in int duration);
	void onIsPlaying(in int index, in int state, in int duration, in int msec);
	void onPlaying(in int msec);
	void onCompleted();
	void onError(in int what, in int extra);
	void onSeekTo(in int msec);
	void onAudioChanged(in int index);
//	void onStateChanged(in int state);
}