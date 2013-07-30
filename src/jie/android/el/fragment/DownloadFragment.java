package jie.android.el.fragment;

import jie.android.el.R;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DownloadFragment extends BaseFragment implements OnClickListener {

	private TextView textView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_download);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		textView = (TextView) view.findViewById(R.id.editText1);
		textView.setText("0000-3-00-00");
		
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
		final String request = textView.getText().toString();
		
		try {
			getELActivity().getServiceAccess().addDownloadRequest(request);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
