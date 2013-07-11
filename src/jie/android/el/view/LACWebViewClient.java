package jie.android.el.view;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class LACWebViewClient extends WebViewClient {

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
//		return super.shouldOverrideUrlLoading(view, url);
		Toast.makeText(view.getContext(), "url = " + url, Toast.LENGTH_SHORT).show();
		if (url.startsWith("lac://")) {
			
			return true;
		} else {
			return false;
		}
	}

}
