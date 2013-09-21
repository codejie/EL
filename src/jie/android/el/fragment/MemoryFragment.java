package jie.android.el.fragment;

import jie.android.el.R;
import android.os.Bundle;
import android.view.View;

public class MemoryFragment extends BaseFragment {

	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_memory);
		this.setMenuRes(R.menu.fragment_memory);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

}
