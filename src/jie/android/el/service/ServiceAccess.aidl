package jie.android.el.service;

import jie.android.el.service.PlayAudioListener;
import jie.android.el.database.Word;

interface ServiceAccess {
	void setAudio(in String audio, in PlayAudioListener listener);
	void playAudio();
	void stopAudio();
	void pauseAudio();
	void seekAudio(in int poistion);
	
	Word.XmlResult queryWordResult(in String word);
}