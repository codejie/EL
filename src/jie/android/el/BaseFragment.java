package jie.android.el;

import android.os.Bundle;
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
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
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
	
}
