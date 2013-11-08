package jie.android.el.fragment;

import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
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
		
		view.findViewById(R.id.button2).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			onSubmitButtonClick();
			break;
		case R.id.button2:
			onRemoveAllButtonClick();
			break;
		default:;
		}
	}

	private void onRemoveAllButtonClick() {
		getELActivity().getContentResolver().delete(ELContentProvider.URI_LAC_SYS_UPDATE, null, null);
	}

	private void onSubmitButtonClick() {
		final String request = textRequestCode.getText().toString();
		final String check = textCheckCode.getText().toString();
		
		if (request.isEmpty()) {
			return;
		}
		
		try {
			if (getELActivity().getServiceAccess().addDownloadRequest(request, check)) {
				showNotification(getString(R.string.el_download_request_added));// "Request has been added the download queue.");
			} else {
				showNotification(getString(R.string.el_download_request_wrong));//"wrong code, please try again.");
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
