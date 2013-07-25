package jie.android.el;

import jie.android.el.fragment.BaseFragment;
import jie.android.el.fragment.DownloadFragment;
import jie.android.el.fragment.ListFragment;
import jie.android.el.fragment.ShowFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public class FragmentSwitcher {
	
	public enum Type {
		
		LIST("list", false), SHOW("show", true), ABOUT("about", true), SETTING("setting", true),
		DOWNLOAD("download", true);
		
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
		fragmentManager = this.activity.getSupportFragmentManager();
	}
	
	public boolean show(Type type) {
		return show(type, null);
	}
	
	public boolean show(Type type, Bundle args) {
		if (curType != null) {
			if (curType == type) {
				return true;
			} else {
				hide(curType);
			}
		}
		
		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
		if (fragment == null) {
			fragment = create(type);
			if (fragment == null) {
				return false;
			}
		}
		
		fragment.onArguments(args);
		
		fragmentManager.beginTransaction().show(fragment).commit();
		curType = type;
		
		return true;
	}

	private BaseFragment create(Type type) {
		BaseFragment fragment = null;
		switch(type) {
		case LIST:
			fragment = new ListFragment();
			break;
		case SHOW:
			fragment = new ShowFragment();
			break;
		case DOWNLOAD:
			fragment = new DownloadFragment();
			break;
		default:
			return null;
		}
		
		fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commit();
		
		return fragment;
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

	public BaseFragment getFragment() {
		if (curType == null) {
			return null;
		}
		return (BaseFragment) fragmentManager.findFragmentByTag(curType.getTitle());
	}
}
