package jie.android.el.fragment;

import jie.android.el.ELActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class BaseFragment extends SherlockFragment {

	private int layoutRes = -1;
	private int menuRes = -1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (layoutRes != -1) {
			return inflater.inflate(layoutRes, container, false);
		} else {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (menuRes != -1) {
			getSherlockActivity().invalidateOptionsMenu();
		}
	}

	@Override
	public void onDetach() {
		
		if (menuRes != -1) {
			getSherlockActivity().invalidateOptionsMenu();
		}		
		
		super.onDetach();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (menuRes != -1) {
			menu.clear();
			inflater.inflate(menuRes, menu);
		}		
	}

//	@Override
//	public void onPrepareOptionsMenu(Menu menu) {
//		if (menuRes != -1) {
//			menu.clear();
//			getELActivity().getSupportMenuInflater().inflate(menuRes, menu);
//		}
//	}

	public int getLayoutRes() {
		return layoutRes;
	}

	public void setLayoutRes(int resLayout) {
		this.layoutRes = resLayout;
	}

	public int getMenuRes() {
		return menuRes;
	}

	public void setMenuRes(int resMenu) {
		this.menuRes = resMenu;
		if (this.menuRes != -1) {
			this.getSherlockActivity().invalidateOptionsMenu();
		}
	}
	
	public ELActivity getELActivity() {
		return (ELActivity)this.getSherlockActivity();
	}

	public void onArguments(Bundle args) {		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}
}
