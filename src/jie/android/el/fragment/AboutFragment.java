package jie.android.el.fragment;

import jie.android.el.CommonConsts.DownloadRequest;
import jie.android.el.CommonConsts.Setting;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import jie.android.el.database.ELDBAccess;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutFragment extends BaseFragment {

	private class DownloadAlertDialog extends DialogFragment {

		private Context context;

		public DownloadAlertDialog(Context context) {
			super();
			this.context = context;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final AlertDialog dlg = new AlertDialog.Builder(context).create();
			dlg.setIcon(R.drawable.ic_launcher);
			dlg.setTitle(R.string.el_about_download_dialog_title);
			dlg.setButton(DialogInterface.BUTTON_POSITIVE, getText(android.R.string.yes), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					downloadLatestVersion();
				}
			});

			dlg.setButton(DialogInterface.BUTTON_NEGATIVE, getText(android.R.string.no), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dlg.dismiss();
				}
			});

			return dlg;
		}
	}

	private static final String PACKAGE_URL = "http://item.taobao.com/item.htm?id=19680021933";
	private static final String EL_DOWNLOAD_URL = "http://www.cppblog.com/codejie/archive/2010/07/23/108996.html";
	private static final String EL_HOMESITE_URL = "http://www.cppblog.com/codejie";

	private class PackageAdapter extends SimpleCursorAdapter {

		public PackageAdapter(Context context, Cursor c) {
			super(context, R.layout.layout_newpackage_item, c, new String[] { "_id", "title", "updated", "desc" }, new int[] { R.id.textView1, R.id.textView2,
					R.id.textView3, R.id.textView4 }, 0);
		}
	}

	private ListView list;
	// private TextView latest;
	private Cursor cursor;
	private ContentObserver observer;

//	private boolean loadedCheck = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setLayoutRes(R.layout.fragment_about);
	}

	@Override
	public void onDetach() {
		if (observer != null) {
			getELActivity().getContentResolver().unregisterContentObserver(observer);
		}

		if (cursor != null) {
			cursor.close();
		}
		super.onDetach();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadPackageList(view);

		Button check = (Button) view.findViewById(R.id.button1);
		check.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onCheckNewPackages();
				// showDialog();
			}

		});

		TextView site = (TextView) view.findViewById(R.id.textView6);
		site.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(EL_HOMESITE_URL));
				startActivity(intent);
			}

		});

		TextView ver = (TextView) view.findViewById(R.id.textView2);
		ver.setText("version " + getELActivity().getResources().getText(R.string.app_version).toString());
	}

	private void loadPackageList(final View v) {

		list = (ListView) v.findViewById(R.id.listView1);
		observer = new ContentObserver(new Handler()) {

			@Override
			public void onChange(boolean selfChange) {
				loadLatest(v);
				loadList();
			}
		};

		getELActivity().getContentResolver().registerContentObserver(ELContentProvider.URI_EL_NEW_PACKAGES, true, observer);

		loadLatest(v);
		loadList();
	}

	protected void loadLatest(View v) {

		TextView tv = (TextView) v.findViewById(R.id.textView7);
		tv.setVisibility(View.GONE);

		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_SYS_INFO, ELDBAccess.SYSINFO_LATESTVERSION);
		Cursor cursor = getELActivity().getContentResolver().query(uri, new String[] { "value" }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				String ver = cursor.getString(0);
				if (!ver.equals(getELActivity().getResources().getText(R.string.app_version).toString())) {
					tv.setText("(latest version is " + ver + ")");
					tv.setVisibility(View.VISIBLE);
					tv.setClickable(true);
					tv.setFocusable(true);
					tv.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(EL_DOWNLOAD_URL));
							startActivity(intent);
						}

					});
					showDialog();
				}
			}
		} finally {
			cursor.close();
		}
	}

	private void showDialog() {
		if (hasShowDialog()) {
			return;
		}
		hasShowDialog(true);
		
		DownloadAlertDialog dlg = new DownloadAlertDialog(getELActivity());
		dlg.show(getFragmentManager(), "download");
	}

	protected void loadList() {
		if (cursor != null) {
			cursor.close();
		}

		cursor = getELActivity().getContentResolver().query(ELContentProvider.URI_EL_NEW_PACKAGES, new String[] { "idx as _id", "title", "updated", "desc" },
				null, null, "idx desc");
		list.setAdapter(new PackageAdapter(getELActivity(), cursor));

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onPackageItemClick(position, id);
			}
		});
	}

	protected void onPackageItemClick(int position, long id) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PACKAGE_URL));
		startActivity(intent);
	}

	protected void onCheckNewPackages() {
		try {
			if (getELActivity().getServiceAccess().addDownloadRequest(DownloadRequest.CHECK_NEW_PACKAGES, null)) {
				Toast.makeText(getELActivity(), "retrieving the info of new packages..", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getELActivity(), "check for new packages failed.", Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		hasShowDialog(false);
	}

	protected void downloadLatestVersion() {
		Uri uri = ContentUris.withAppendedId(ELContentProvider.URI_EL_SYS_INFO, ELDBAccess.SYSINFO_LATESTPACKAGE);
		Cursor cursor = getELActivity().getContentResolver().query(uri, new String[] { "value" }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				String link = cursor.getString(0);
				if (getELActivity().getServiceAccess().addDownloadRequest(DownloadRequest.DOWNLOAD_LATEST_VERSION, link)) {
					Toast.makeText(getELActivity(), "downloading latest version of EL..", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getELActivity(), "try to download latest version failed.", Toast.LENGTH_SHORT).show();
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			cursor.close();
		}
	}
	
	private boolean hasShowDialog() {
		return getELActivity().getSharedPreferences().getBoolean(Setting.ABOUT_DIALOG_SHOWN, false);
	}
	
	private void hasShowDialog(boolean shown) {
		getELActivity().getSharedPreferences().edit().putBoolean(Setting.ABOUT_DIALOG_SHOWN, shown).apply();
	}
	
}
