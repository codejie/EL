package jie.android.el.service;

interface ServiceNotification {
	void onServiceState(in int state);
	
	void onAudioAction(in Intent intent);
	void onUpdateAudio(in Intent intent);
}