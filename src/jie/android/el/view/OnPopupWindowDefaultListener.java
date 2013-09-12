package jie.android.el.view;

import jie.android.el.utils.Speaker;
import jie.android.el.view.ELPopupWindow;

public class OnPopupWindowDefaultListener implements ELPopupWindow.OnPopupWindowListener {

	private ELPopupWindow window;
	
	public OnPopupWindowDefaultListener(ELPopupWindow window) {
		this.window = window;
	}
	
	@Override
	public void onCloseClick() {
		window.show(false);
	}

	@Override
	public void onTextClick(String text) {
		Speaker.speak(text);
	}

	@Override
	public boolean onTextLongClick(String text) {
		return false;
	}

}
