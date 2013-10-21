package jie.android.el.service;

import jie.android.el.service.ServiceNotification;
//import jie.android.el.service.OnPlayAudioListener;
import jie.android.el.database.Word;

interface ServiceAccess {
	void regServiceNotification(in int token, in ServiceNotification notification);
	void unregServiceNotification(in int token);
	
	boolean canExit(); 
	
//	void setAudio(in int index);
//	void setAudioListener(in OnPlayAudioListener listener);
//	void playAudio();
//	void stopAudio();
//	void pauseAudio();
//	void seekAudio(in int position);
//	int getPlayState();
	
	Word.XmlResult queryWordResult(in String word);
	
	boolean addDownloadRequest(in String request, in String check);
	
//	void setUIState(in int state);
	
	void setAudioAction(in Intent intent);
}