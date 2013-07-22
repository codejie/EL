package jie.android.el.service;

interface PlayAudioListener {
	void onPrepared(in int duration);
	void onStart();
	void onPlaying(in int sec);
	void onCompleted();
	void onPause();
	void onError(in String what);
}