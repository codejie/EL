package jie.android.el.service;

interface PlayAudioListener {
	void onStart(in int token);
	void onStop(in int token);
}