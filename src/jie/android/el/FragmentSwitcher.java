package jie.android.el;

import jie.android.el.fragment.AboutFragment;
import jie.android.el.fragment.BaseFragment;
import jie.android.el.fragment.DictionaryFragment;
import jie.android.el.fragment.DownloadFragment;
import jie.android.el.fragment.ListFragment;
import jie.android.el.fragment.MemoryFragment;
import jie.android.el.fragment.SettingFragment;
import jie.android.el.fragment.ShowFragment;
import jie.android.el.fragment.VocabFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentSwitcher {
	
	public interface OnSwitchListener {
		public void onSwitch(Type oldType, Type newType);
	}
	
	public enum Type {
		
		LIST("list", false), SHOW("show", false), ABOUT("about", true), SETTING("setting", true),
		DOWNLOAD("download", true), DICTIONARY("dictionary", true), VOCAB("vocab", true), MEMORY("memory", true);
		
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
		
		public static Type getType(final String title) {
			if (title.equals(LIST.getTitle())) {
				return LIST;
			} else if (title.equals(SHOW.getTitle())) {
				return SHOW;
			} else {
				return null;
			}
		}		
	}
	
	private final ELActivity activity;
	private FragmentManager fragmentManager = null;
	private Type curType = null;
	private OnSwitchListener  onSwitchListener = null;
	
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
				((BaseFragment) fragmentManager.findFragmentByTag(type.getTitle())).onArguments(args);				
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
		
		fragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss();//.commit();
		fragment.onArguments(args);
		
		if (onSwitchListener != null) {
			onSwitchListener.onSwitch(curType, type);
		}
		
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
		case SETTING:
			fragment = new SettingFragment();
			break;
		case ABOUT:
			fragment = new AboutFragment();
			break;
		case DICTIONARY:
			fragment = new DictionaryFragment();
			break;
		case VOCAB:
			fragment = new VocabFragment();
			break;
		case MEMORY:
			fragment = new MemoryFragment();
			break;
		default:
			return null;
		}
		
		fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commitAllowingStateLoss();//commit();
		
		return fragment;
	}

	private void hide(Type type) {
		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
		if (fragment != null) {
			if (type.hasRemoved()) {
				fragmentManager.beginTransaction().remove(fragment).commit();
			} else {
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.addToBackStack(type.getTitle());
				ft.hide(fragment);
				ft.commit();
//				fragmentManager.beginTransaction().hide(fragment).commit();
			}
			
//			curType = null;			
		}
	}

	public BaseFragment getFragment() {
		if (curType == null) {
			return null;
		}
		return (BaseFragment) fragmentManager.findFragmentByTag(curType.getTitle());
	}
	
	public Type getCurrentType() {
		return curType;
	}
	
	public boolean isRemovedType() {
		if (curType == null)
			return false;
		return curType.hasRemoved();
	}
	
	public boolean showPrevFragment() {
		int count = fragmentManager.getBackStackEntryCount();
		if (count > 0) {
			String name = fragmentManager.getBackStackEntryAt(count - 1).getName();
			fragmentManager.popBackStack();// .popBackStackImmediate();
			Type type = Type.getType(name);
			if (type != null) {
				show(type);
				return true;
			}
		}
		
		return false;
	}

	public void saveInstanceState(Bundle outState) {
//		hide(curType);
//		FragmentTransaction ft = fragmentManager.beginTransaction();
//		while (!ft.isEmpty()) {
//			fragmentManager.popBackStackImmediate();
//		}
//		ft.commit();
	}

	public void restoreInstanceState(Bundle savedInstanceState) {
//		show(curType);
	}
	
	public void setOnSwitchListener(OnSwitchListener listener) {
		onSwitchListener = listener;
	}

	public void remvoeAll() {
		while (fragmentManager.getBackStackEntryCount() > 0) {
			fragmentManager.popBackStackImmediate();
		}
	}
}
