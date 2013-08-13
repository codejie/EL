package jie.android.el.service;

import jie.android.el.service.ServiceNotification;
import jie.android.el.service.OnPlayAudioListener;
import jie.android.el.database.Word;

interface ServiceAccess {
	void regServiceNotification(in int token, in ServiceNotification notification);
	void unregServiceNotification(in int token);
	
	boolean canExit(); 
	
	void setAudio(in int index);
	void setAudioListener(in OnPlayAudioListener listener);
	void playAudio();
	void stopAudio();
	void pauseAudio();
	void seekAudio(in int position);
	boolean isAudioPlaying();
	
	Word.XmlResult queryWordResult(in String word);
	
	boolean addDownloadRequest(in String request);
	
	void setUIState(in int state);

	int setNotification(in int type, in String title, in String text);
	void removeNotification(in int type, in int id);  
}