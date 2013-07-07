package jie.android.el;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class BaseFragment extends SherlockFragment {

	private int resLayout = -1;
	private int resMenu = -1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (resLayout != -1) {
			return inflater.inflate(resLayout, container, false);
		} else {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
	}

	public int getResLayout() {
		return resLayout;
	}

	public void setResLayout(int resLayout) {
		this.resLayout = resLayout;
	}

	public int getResMenu() {
		return resMenu;
	}

	public void setResMenu(int resMenu) {
		this.resMenu = resMenu;
	}
	
}
