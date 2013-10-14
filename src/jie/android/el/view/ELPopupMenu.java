package jie.android.el.view;

import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParserException;

import jie.android.el.R;
import jie.android.el.utils.Utils;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.PopupWindow;

public class ELPopupMenu {

	private class MenuData {
		public int id;
		public String title;
		
		public MenuData(int id, String title) {
			this.id = id;
			this.title = title;
		}
	}
	
	public interface OnItemClickListener {
		public void OnClick(int item);
	}
	
	private Context context;
	private int resMenuId;
	private View parent;
	private Object popup;
	private OnItemClickListener listener;
	
	public ELPopupMenu(Context context, int resMenuId, View parent, OnItemClickListener listener) {
		this.context = context;
		this.resMenuId = resMenuId;
		this.parent = parent;
		this.listener = listener;
		
		make();
	}
	
	private void make() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			popup = makePopupMenu(context, resMenuId, parent);
		} else {
			popup = makePopupWindow(context, resMenuId, parent);
		}
	}

	private Object makePopupMenu(Context context, int resId, View parent) {
		PopupMenu pm = new PopupMenu(context, parent);
		pm.getMenuInflater().inflate(resId, pm.getMenu());
		pm.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem menu) {
				if (listener != null) {
					listener.OnClick(menu.getItemId());
					return true;
				} 
				return false;
			}			
		});
		
		return pm;
	}

	private int getAttributeIntValue(Context context, String resId) {
		return Integer.valueOf(resId.substring(1));
	}
	private String getAttributeStringValue(Context context, String resId) {
		int id = Integer.valueOf(resId.substring(1));
		return context.getString(id);
	}
	
	
	private ArrayList<MenuData> analyseMenuResource(Context context, int resId) {
		
		ArrayList<MenuData> menu = new ArrayList<MenuData>();
		
		XmlResourceParser parser = context.getResources().getXml(resId);
		
		try {
			int event = parser.next();
			while (event != XmlResourceParser.END_DOCUMENT) {
				if (event == XmlResourceParser.START_TAG) {
					if (parser.getName().equals("item")) {
						menu.add(new MenuData(getAttributeIntValue(context, parser.getAttributeValue(Utils.NS_ANDROID, "id")),
								getAttributeStringValue(context, parser.getAttributeValue(Utils.NS_ANDROID, "title"))));
					}
				}
				event = parser.next();
			}
			
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return menu;
	}
	
	private Object makePopupWindow(Context context, int resId, View parent) {
		
		ArrayList<MenuData> menu = analyseMenuResource(context, resId);
		if (menu.size() == 0) {
			return null;
		}
			
		OnClickListener l = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.OnClick(v.getId());
				}
				if (popup != null) {
					((PopupWindow)popup).dismiss();
				}
			}			
		};		
		
		AttributeSet attrs = Utils.getAttributeSet(context, R.layout.layout_popmenu_window_template, "LinearLayout", R.id.linearLayout2);
		LinearLayout ll = new LinearLayout(context, attrs);
		
		int pos = 0;
		for (MenuData data : menu) {
			if (pos == 0) {
				attrs = Utils.getAttributeSet(context, R.layout.layout_popmenu_window_template, "Button", R.id.button1);
			} else if (pos == menu.size() -1) {
				attrs = Utils.getAttributeSet(context, R.layout.layout_popmenu_window_template, "Button", R.id.button3);
			} else {
				attrs = Utils.getAttributeSet(context, R.layout.layout_popmenu_window_template, "Button", R.id.button2);
			}
			
			Button btn = new Button(context, attrs);
			btn.setId(data.id);
			btn.setText(data.title);
			btn.setOnClickListener(l);
			
			ll.addView(btn, ll.generateLayoutParams(attrs));
			
			++ pos;
		}
		
		PopupWindow pw = new PopupWindow(ll);
		//pw.setWidth(250);
		pw.setWidth(LayoutParams.WRAP_CONTENT);
		pw.setHeight(LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);
		pw.setOutsideTouchable(true);
		pw.setBackgroundDrawable(new BitmapDrawable());
		
		return pw;		
	}
	
	public void show() {
		show(Gravity.LEFT | Gravity.BOTTOM, 16, 64);
	}	

	public void show(int gravity, int x, int y) {
		if (popup != null) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				((PopupMenu)popup).show();
			} else {
				((PopupWindow)popup).showAtLocation(parent, gravity, x, y);
			}
		}
	}	
	
	public void setItemEnabled(int item, boolean enabled) {
		if (popup != null) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				Menu menu = ((PopupMenu)popup).getMenu();
				MenuItem sub = menu.findItem(item);
				if (sub != null) {
					sub.setEnabled(enabled);
				}
			} else {
				View p = ((PopupWindow)popup).getContentView();
				View v = p.findViewById(item);
				v.setEnabled(enabled);
			}
		}
	}
	
}
