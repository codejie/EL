package jie.android.el.view;

import jie.android.el.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

public class ShowPopWindow implements OnClickListener {

	private Context context;
	private View anchor;
	private OnClickListener listener;
	private PopupWindow win;
	
	private boolean[] itemEnabled = new boolean[] { true, true, true };
	
	public ShowPopWindow(Context context, View anchor) {
		this.context = context;
		this.anchor = anchor;
	}
	
	private View makeView() {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.window_show_pop, null);
		v.setFocusable(true);
		v.setFocusableInTouchMode(true);
		
		Button btn = (Button)v.findViewById(R.id.el_menu_show_slowdialog);
		btn.setOnClickListener(this);
		btn.setEnabled(itemEnabled[0]);
		btn = (Button)v.findViewById(R.id.el_menu_show_explanation);
		btn.setOnClickListener(this);
		btn.setEnabled(itemEnabled[1]);
		btn = (Button)v.findViewById(R.id.el_menu_show_fastdialog);
		btn.setOnClickListener(this);
		btn.setEnabled(itemEnabled[2]);
		
		return v;
	}

	@Override
	public void onClick(View v) {
		if (listener != null) {
			listener.onClick(v);
		}
	}
	
	public void setOnClickListener(OnClickListener listener) {
		this.listener = listener;
	}
	
	public void show() {
		View v = makeView();
		win = new PopupWindow(v);
		win.setWidth(250);
		win.setHeight(LayoutParams.WRAP_CONTENT);
		win.setFocusable(true);
		win.setOutsideTouchable(true);
		win.setBackgroundDrawable(new BitmapDrawable());
		win.showAtLocation(anchor, Gravity.LEFT | Gravity.BOTTOM, 8, 64);
	}
	
	public void dismiss() {
		if (win != null) {
			win.dismiss();
			win = null;
		}
	}
	
	public boolean isShowing() {
		return win != null;
	}

	public void setItemEnable(int item, boolean enable) {
		if (item > 0 && item < itemEnabled.length) {
			itemEnabled[item] = enable;
		}
	}
	
}
