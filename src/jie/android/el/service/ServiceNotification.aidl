package jie.android.el.service;

interface ServiceNotification {
	void onUnzip();
	void onReady();
	
	void onAudioPlaying(in int index, in int duration, in int position);
}