package jie.android.el.view;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LACWebViewClient extends WebViewClient {

	private OnUrlLoadingListener listener = null; 
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {

		if (listener != null) {
			return listener.onLoading(url);
		}
		
		return super.shouldOverrideUrlLoading(view, url);
	}

	public void setOnUrlLoadingListener(OnUrlLoadingListener listener) {
		this.listener = listener;
	}
}
