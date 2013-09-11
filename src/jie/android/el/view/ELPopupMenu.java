package jie.android.el.view;

import jie.android.el.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
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
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			popup = makePopupMenu(context, parent);
		} else {
			popup = makePopupWinodow(context, parent);
		}
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

	private Object makePopupWinodow(Context context, View parent) {
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
	
	public void show() {
		if (popup != null) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				((PopupMenu)popup).show();
			} else {
				((PopupWindow)popup).showAtLocation(parent, Gravity.LEFT | Gravity.BOTTOM, 8, 64);
			}
		}
	}
	
	
	public void setItemEnabled(ItemId item, boolean enabled) {
		if (popup != null) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				if (item != null) {
					Menu menu = ((PopupMenu)popup).getMenu();
					menu.getItem(item.getId()).setEnabled(enabled);
				}
			} else {
				int id = getId(item);
				if (id != -1) {
					View v = ((PopupWindow)popup).getContentView().findViewById(id);
					v.setEnabled(enabled);
				}
			}
		}
	}
	
}
