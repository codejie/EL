package jie.android.el.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import jie.android.el.R;
import jie.android.el.utils.Utils;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
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

	public enum ItemId {
		ITEM_SLOWDIALOG, ITEM_EXPLANATIONS, ITEM_FASTDIALOG;
		
		public int getId() {
			return this.ordinal();
		}
	}
	
	private class MenuData {
		public int id;
		public String title;
		
		public MenuData(int id, String title) {
			this.id = id;
			this.title = title;
		}
	}
	
	public interface OnItemClickListener {
		public void OnClick(ItemId item);
	}
	
	private Context context;
	private View parent;
	private Object popup;
	private OnItemClickListener listener;
	
	public ELPopupMenu(Context context, View parent, OnItemClickListener listener) {
		this.context = context;
		this.parent = parent;
		this.listener = listener;
		
		make();
	}
	
	private void make() {
//		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//			popup = makePopupMenu(context, parent);
//		} else {
			popup = makePopupWindow(context, parent, R.menu.popmenu_menu);
//		}
	}
	
	private ItemId getItemId(int id) {
		switch(id) {
		case R.id.el_menu_show_slowdialog:
			return ItemId.ITEM_SLOWDIALOG;
		case R.id.el_menu_show_explanation:
			return ItemId.ITEM_EXPLANATIONS;
		case R.id.el_menu_show_fastdialog:
			return ItemId.ITEM_FASTDIALOG;
		default:
			return null;
		}		
	}
	
	private int getId(ItemId itemid) {
		if (itemid == ItemId.ITEM_SLOWDIALOG) {
			return R.id.el_menu_show_slowdialog;
		} else if (itemid == ItemId.ITEM_EXPLANATIONS) {
			return R.id.el_menu_show_explanation;
		} else if (itemid == ItemId.ITEM_FASTDIALOG) {
			return R.id.el_menu_show_fastdialog;
		} else {
			return -1;
		}
	}

	private Object makePopupMenu(Context context, View parent) {
		PopupMenu pm = new PopupMenu(context, parent);
		pm.getMenuInflater().inflate(R.menu.popmenu_menu, pm.getMenu());
		pm.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem menu) {
				if (listener != null) {
					ItemId itemid = getItemId(menu.getItemId());
					if (itemid != null) {
						listener.OnClick(itemid);
						return true;
					}
				} 
				return false;
			}			
		});
		
		return pm;
	}

	private Object makePopupWindow(Context context, View parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.layout_popmenu_window, null);
		v.setFocusable(true);
		v.setFocusableInTouchMode(true);
		
		final PopupWindow pw = new PopupWindow(v);
		
		OnClickListener l = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					ItemId itemid = getItemId(v.getId());
					if (itemid != null) {
						listener.OnClick(itemid);
					}
				}
				pw.dismiss();
			}			
		};
		
		Button btn = (Button)v.findViewById(R.id.el_menu_show_slowdialog);
		btn.setOnClickListener(l);
		btn = (Button)v.findViewById(R.id.el_menu_show_explanation);
		btn.setOnClickListener(l);
		btn = (Button)v.findViewById(R.id.el_menu_show_fastdialog);
		btn.setOnClickListener(l);
	
		pw.setWidth(250);
		pw.setHeight(LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);
		pw.setOutsideTouchable(true);
		pw.setBackgroundDrawable(new BitmapDrawable());
		
		return pw;
	}
	
	private String getAttributeStringValue(Context context, String resId) {
		int id = Integer.valueOf(resId.substring(1));
		return context.getString(id);
	}
	
	
	private ArrayList<MenuData> analyseMenuResource(Context context, int resMenu) {
		
		ArrayList<MenuData> menu = new ArrayList<MenuData>();
		
		XmlResourceParser parser = context.getResources().getXml(resMenu);
		
		try {
			int event = parser.next();
			while (event != XmlResourceParser.END_DOCUMENT) {
				if (event == XmlResourceParser.START_TAG) {
					if (parser.getName().equals("item")) {
						menu.add(new MenuData(parser.getAttributeIntValue(Utils.NS_ANDROID, "id", -1),
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
	
	private Object makePopupWindow(Context context, View parent, int resMenu) {
		
		ArrayList<MenuData> menu = analyseMenuResource(context, resMenu);
		if (menu.size() == 0) {
			return null;
		}
		
		AttributeSet attrs = Utils.getAttributeSet(context, R.layout.layout_popmenu_window_template, "LinearLayout", R.id.linearLayout1);
		LinearLayout ll = new LinearLayout(context, attrs);
		
		int pos = 0;
		for (MenuData data : menu) {
			if (pos == 0) {
				attrs = Utils.getAttributeSet(context, R.layout.layout_popmenu_window_template, "Button", R.id.button1);
			} else if (pos == menu.size() -1) {
				attrs = Utils.getAttributeSet(context, R.layout.layout_popmenu_window_template, "Button", R.id.button2);
			} else {
				attrs = Utils.getAttributeSet(context, R.layout.layout_popmenu_window_template, "Button", R.id.button3);
			}
			
			Button btn = new Button(context, attrs);
			btn.setId(data.id);
			btn.setText(data.title);
			
			ll.addView(btn, ll.generateLayoutParams(attrs));
			
			++ pos;
		}
		
		final PopupWindow pw = new PopupWindow(ll);
		pw.setWidth(250);
		pw.setHeight(LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);
		pw.setOutsideTouchable(true);
		pw.setBackgroundDrawable(new BitmapDrawable());
		
		return pw;		
	}
	
	public void show() {
		if (popup != null) {
//			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//				((PopupMenu)popup).show();
//			} else {
				((PopupWindow)popup).showAtLocation(parent, Gravity.LEFT | Gravity.BOTTOM, 8, 64);
//			}
		}
	}
	
	
	public void setItemEnabled(ItemId item, boolean enabled) {
		if (popup != null) {
//			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//				if (item != null) {
//					Menu menu = ((PopupMenu)popup).getMenu();
//					menu.getItem(item.getId()).setEnabled(enabled);
//				}
//			} else {
				int id = getId(item);
				if (id != -1) {
					View v = ((PopupWindow)popup).getContentView().findViewById(id);
//					v.setEnabled(enabled);
				}
//			}
		}
	}
	
}
