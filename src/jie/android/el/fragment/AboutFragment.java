package jie.android.el.fragment;

import jie.android.el.R;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AboutFragment extends BaseFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_about);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		Button check = (Button) view.findViewById(R.id.button1);
		check.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onCheckNewPackages();
			}
			
		});
	}

	protected void onCheckNewPackages() {
		try {
			if (!getELActivity().getServiceAccess().checkNewPackages()) {
				Toast.makeText(getELActivity(), "check new packages failed.", Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
