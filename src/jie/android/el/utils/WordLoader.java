package jie.android.el.utils;

import jie.android.el.database.Word;
import jie.android.el.service.ServiceAccess;
import android.os.AsyncTask;
import android.os.RemoteException;

public class WordLoader extends AsyncTask<String, Void, String> {

	public interface OnPostExecuteCallback {
		public void OnPostExecute(String word, String result);
	}

	private ServiceAccess service;
	private OnPostExecuteCallback callback;
	private String word = null;
	
	public WordLoader(ServiceAccess service, OnPostExecuteCallback callback) {
		this.service = service;
		this.callback = callback;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		word = arg0[0];
		Word.XmlResult result = null;
		try {
			if (service != null) {
				result = service.queryWordResult(word);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
		if (result.getXmlData().size() > 0) {
			return XmlTranslator.trans(Utils.assembleXmlResult(word, result));
		} else {
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (callback != null) {
			callback.OnPostExecute(word, result);
		}
	}
}
