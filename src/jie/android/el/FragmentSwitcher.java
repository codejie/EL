package jie.android.el;

import jie.android.el.fragment.ListFragment;
import android.support.v4.app.FragmentManager;

public class FragmentSwitcher {
	
	public enum Type {
		
		LIST("list", false), SHOW("show", false), ABOUT("about", true), SETTING("setting", true);
		
		private final String title;
		private final boolean removed;
		
		private Type(final String title, boolean removed) {
			this.title = title;
			this.removed = removed;
		}
		
		public String getTitle() {
			return title;
		}

		public boolean hasRemoved() {
			return removed;
		}		
	}
	
	private final ELActivity activity;
	private FragmentManager fragmentManager = null;
	private Type curType = null;
	
	public FragmentSwitcher(final ELActivity activity) {
		this.activity = activity;
	}
	
	public boolean show(Type type) {
		if (curType != null && curType == type) {
			return true;
		} else if (curType != type) {
			hide(curType);
		}
		
		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
		if (fragment == null) {
			fragment = create(type);
			if (fragment == null) {
				return false;
			}				
		}
		
		fragmentManager.beginTransaction().show(fragment).commit();
		curType = type;
		
		return true;
	}

	private BaseFragment create(Type type) {
		switch(type) {
		case LIST:
			return new ListFragment();
		default:
			return null;
		}
	}

	private void hide(Type type) {
		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
		if (fragment != null) {
			if (type.hasRemoved()) {
				fragmentManager.beginTransaction().remove(fragment).commit();
			} else {
				fragmentManager.beginTransaction().hide(fragment).commit();
			}
			curType = null;			
		}
	}
}
