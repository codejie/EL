package jie.android.el;

import java.util.Stack;

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
import android.util.Log;

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
	
	private Stack<Type> stackType = new Stack<Type>();
	
	public FragmentSwitcher(final ELActivity activity) {
		this.activity = activity;
		fragmentManager = this.activity.getSupportFragmentManager();
	}
	
	public boolean show(Type type) {
		return show(type, null);
	}
	
	public boolean show(Type type, Bundle args) {
		if (type == null) {
			return false;
		}
		
		if (curType == type) {
			BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
			if (fragment != null) {
				fragment.onArguments(args);
				return true;
			} else {
				return false;
			}				
		}
		
		boolean found = false;
		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
		if (fragment == null) {
			fragment = create(type);
			if (fragment == null) {
				return false;
			}
			found = false;
		} else {
			found = true;
		}

		FragmentTransaction ft = fragmentManager.beginTransaction();
		if (curType != null) {
			if (found) {
				if (!curType.hasRemoved()) {
					hideFragment(ft, curType);
				} else {
					removeFragment(ft, curType);
				}
				ft.show(fragment);				
			} else {
				if (!curType.hasRemoved()) {
					hideFragment(ft, curType);
					ft.add(R.id.main, fragment, type.getTitle());
				} else {
					ft.replace(R.id.main, fragment, type.getTitle());
				}
			}
			
			
//			if (!curType.hasRemoved()) {
//				hideFragment(ft, curType);
//				if (found) {
//					ft.show(fragment);
//				} else {
//					ft.add(R.id.main, fragment, type.getTitle());
//				}
//			} else {
//				if (found) {
//					ft.show(fragment);
//				} else {
//					ft.replace(R.id.main, fragment, type.getTitle());
//				}
//			}
		} else {
			ft.add(R.id.main, fragment, type.getTitle());
		}
		
		ft.commit();
//		
//		if (curType != null && !curType.hasRemoved())
//		
//		if (found) {
//			if (curType.)
//		} else {
//			
//		}
//		
//		if (curType.hasRemoved()) {
//			replaceFragment(ft, found, type, fragment);
//		} else {
//			addFragment(ft, found, type, fragment);
//		}
//		
//		showFragment(ft, found, curType, type, fragment);			
//		
//		
//		if (curType != null) {
//			if (curType.hasRemoved()) {
//				//fragmentManager.beginTransaction().replace(R.id.main, fragment, type.getTitle()).commit();
//				if (found) {
//					fragmentManager.beginTransaction().show(fragment).commit();
//				} else {
//					fragmentManager.beginTransaction().replace(R.id.main, fragment, type.getTitle()).commit();
//				}
//			} else {
//				hide(curType);			
////				fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commit();
//				if (found) {
//					fragmentManager.beginTransaction().show(fragment).commit();
//				} else {
//					fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commit();
//				}
//			}
//		} else {
//			//fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commit();
//			if (found) {
//				fragmentManager.beginTransaction().show(fragment).commit();
//			} else {
//				fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commit();
//			}			
//		}
//		ft.commit();
		
//		if (!type.hasRemoved()) {
//		fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commit();
//		}
		

//		fragmentManager.beginTransaction().show(fragment).commit();
		
		fragment.onArguments(args);
		
		if (onSwitchListener != null) {
			onSwitchListener.onSwitch(curType, type);
		}
		
		curType = type;
		
		return true;
		
		
//		if (curType != null) {
//			if (curType == type) {
//				((BaseFragment) fragmentManager.findFragmentByTag(type.getTitle())).onArguments(args);				
//				return true;
//			} else {
//				hide(curType);
//			}
//		}
//		
//		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
//		if (fragment == null) {
//			fragment = create(type);
//			if (fragment == null) {
//				return false;
//			}
//		}
//		
//		fragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss();//.commit();
//		fragment.onArguments(args);
//		
//		if (onSwitchListener != null) {
//			onSwitchListener.onSwitch(curType, type);
//		}
//		
//		curType = type;
//		
//		return true;
	}

	private void removeFragment(FragmentTransaction ft, Type type) {
		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
		if (fragment != null) {
			ft.remove(fragment);
		}
	}

	private void hideFragment(FragmentTransaction ft, Type type) {
		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
		if (fragment != null) {
			ft.hide(fragment);
			stackType.push(type);
		}		
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
		
//		if (!type.hasRemoved()) {
//			fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commit();
//		}
//		fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commitAllowingStateLoss();//commit();
		//fragmentManager.beginTransaction().add(R.id.main, fragment, type.getTitle()).commitAllowingStateLoss();//commit();
		
		return fragment;
	}

	private void hide(Type type) {
		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
		if (fragment != null) {
			fragmentManager.beginTransaction().hide(fragment).commit();
//			curType = null;			
		}
	}
	
//	private void hide(Type type) {
//		BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(type.getTitle());
//		if (fragment != null) {
//			if (type.hasRemoved()) {
//				fragmentManager.beginTransaction().remove(fragment).commit();
//			} else {
//				FragmentTransaction ft = fragmentManager.beginTransaction();
//				ft.addToBackStack(type.getTitle());
//				ft.hide(fragment);
//				ft.commit();
////				fragmentManager.beginTransaction().hide(fragment).commit();
//			}
//			
////			curType = null;			
//		}
//	}

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
		if (stackType.size() > 0) {
			Type type = stackType.pop();
//			if (type == Type.LIST) {
//				return false;
//			} else {
				show(type);
				return true;
//			}
		} else {
			return false;
		}
		
//		int count = fragmentManager.getBackStackEntryCount();
//		if (count > 0) {
//			String name = fragmentManager.getBackStackEntryAt(count - 1).getName();
//			fragmentManager.popBackStack();// .popBackStackImmediate();
//			Type type = Type.getType(name);
//			if (type != null) {
//				show(type);
//				return true;
//			}
//		}
//		
//		return false;
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
		curType = null;
		int i = fragmentManager.getBackStackEntryCount();
//		Log.d("====", "fragment count = " + i);
//	
//		fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//		i = fragmentManager.getBackStackEntryCount();
		Log.d("====1", "fragment count = " + i);
		while (fragmentManager.getBackStackEntryCount() > 0) {
			fragmentManager.popBackStackImmediate();
		}
		i = fragmentManager.getBackStackEntryCount();
		Log.d("====2", "fragment count = " + i);
		
	}
}
