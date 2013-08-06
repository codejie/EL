package jie.android.el.fragment;

import jie.android.el.FragmentSwitcher;
import jie.android.el.R;
import android.os.Bundle;
import android.view.KeyEvent;

public class SettingFragment extends BaseFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_setting);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			getELActivity().showFragment(FragmentSwitcher.Type.LIST, null);
			return true;
		}
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}	
}
