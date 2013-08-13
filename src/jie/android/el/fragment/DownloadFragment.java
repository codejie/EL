package jie.android.el.fragment;

import jie.android.el.R;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadFragment extends BaseFragment implements OnClickListener {

	private TextView textRequestCode = null;
	private TextView textCheckCode = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_download);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		textRequestCode = (TextView) view.findViewById(R.id.editText1);
		//textRequestCode.setText("0000-3-00-00");
		textCheckCode = (TextView) view.findViewById(R.id.editText2);
		//textCheckCode.setText("0-00-00");		
		
		view.findViewById(R.id.button1).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			onButtonClick();
			break;
		}
	}

	private void onButtonClick() {
		final String request = textRequestCode.getText().toString();
		
		try {
			if (!getELActivity().getServiceAccess().addDownloadRequest(request)) {
				showNotification("wrong code, please try again.");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showNotification(String text) {
		Toast.makeText(getELActivity(), text, Toast.LENGTH_SHORT).show();
	}
	
}
