package jie.android.el.service;

interface ServiceNotification {
	void onServiceState(in int state);
	
	void onAudioPlaying(in int index, in int duration, in int position);
	
	void onPackageReady();
}