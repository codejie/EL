package jie.android.el.fragment;

import jie.android.el.ELActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;

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
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (menuRes != -1) {
			menu.clear();
			getELActivity().getSupportMenuInflater().inflate(menuRes, menu);
		}
	}

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
