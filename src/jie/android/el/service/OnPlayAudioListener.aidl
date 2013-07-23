package jie.android.el.service;

interface OnPlayAudioListener {
	void onPrepared(in int duration);
	void onPlaying(in int msec);
	void onCompleted();
	void onError(in String what);
	void onSeekTo(in int msec);
}