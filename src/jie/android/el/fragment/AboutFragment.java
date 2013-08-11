package jie.android.el.fragment;

import jie.android.el.R;
import android.os.Bundle;

public class AboutFragment extends BaseFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_about);
	}	
}
