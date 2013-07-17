package jie.android.el.service;

import jie.android.el.service.PlayAudioListener;
import jie.android.el.database.Word;

interface ServiceAccess {
	void playAudio(in String file, in PlayAudioListener listener);
	void stopAudio(in int token);
	void pauseAudio(in int token);
	
	Word.XmlResult queryWordResult(in String word);
}