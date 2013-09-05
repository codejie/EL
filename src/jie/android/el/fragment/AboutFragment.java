package jie.android.el.fragment;

import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class AboutFragment extends BaseFragment {

	private class PackageAdapter extends SimpleCursorAdapter {
		
		public PackageAdapter(Context context, Cursor c) {
			super(context, R.layout.layout_newpackage_item, c, new String[] { "_id", "title", "updated", "desc" }, new int[] { R.id.textView1, R.id.textView2, R.id.textView3, R.id.textView4 }, 0);
		}
	}

	private Cursor cursor;
	private ContentObserver observer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_about);
	}

	@Override
	public void onDetach() {
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
			}
			
		});
	}

	private void loadPackageList(final View v) {

		observer = new ContentObserver(new Handler()) {

			@Override
			public void onChange(boolean selfChange) {
				cursor.requery();
			}			
		};
		
		cursor = getELActivity().getContentResolver().query(ELContentProvider.URI_EL_NEW_PACKAGES, new String[] { "idx as _id", "title", "updated", "desc" }, null, null, "idx");
		cursor.setNotificationUri(getELActivity().getContentResolver(), ELContentProvider.URI_EL_NEW_PACKAGES);
		cursor.registerContentObserver(observer);
		
		ListView list = (ListView) v.findViewById(R.id.listView1);
		list.setAdapter(new PackageAdapter(getELActivity(), cursor));
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onPackageItemClick(position, id);
			}
			
		});

	}

	protected void onPackageItemClick(int position, long id) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://item.taobao.com/item.htm?id=19680021933"));
		startActivity(intent);		
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
