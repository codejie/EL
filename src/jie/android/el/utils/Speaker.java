package jie.android.el.utils;

import java.util.Locale;

import jie.android.el.R;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public final class Speaker {

	private static TextToSpeech speaker = null;
	private static boolean isReady = false;
	private static Locale locale = Locale.ENGLISH;
	
	public static int init(final Context context) {
		speaker = new TextToSpeech(context, new TextToSpeech.OnInitListener(){

			@Override
			public void onInit(int status) {
				// TODO Auto-generated method stub
				if(status == TextToSpeech.SUCCESS) {
					int result = speaker.setLanguage(locale);
					if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
						Toast.makeText(context, context.getString(R.string.el_speaker_init_missing_data), Toast.LENGTH_LONG).show();
					}
					else {
						isReady = true;
//						_speaker.speak("Ready", TextToSpeech.QUEUE_FLUSH, null);
					}
				}
				else {
					Toast.makeText(context, context.getString(R.string.el_speaker_init_failed) + status, Toast.LENGTH_LONG).show();
				}				
			}			
		});
		return 0;
	}
	
	public static void release() {
		if(speaker != null) {
			speaker.stop();
			speaker.shutdown();
			
			speaker = null;
		}
	}
	
	public static void speak(final String text) {
		if(speaker != null && isReady) {
			speaker.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}
