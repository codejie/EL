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
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class AboutFragment extends BaseFragment {

	private static final String PACKAGE_URL = "http://item.taobao.com/item.htm?id=19680021933";
	
	private class PackageAdapter extends SimpleCursorAdapter {
		
		public PackageAdapter(Context context, Cursor c) {
			super(context, R.layout.layout_newpackage_item, c, new String[] { "_id", "title", "updated", "desc" }, new int[] { R.id.textView1, R.id.textView2, R.id.textView3, R.id.textView4 }, 0);
		}
	}

	private ListView list;
	private Cursor cursor;
	private ContentObserver observer;
	
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
			}
			
		});
	}

	private void loadPackageList(final View v) {

		list = (ListView) v.findViewById(R.id.listView1);
		
		observer = new ContentObserver(new Handler()) {

			@Override
			public void onChange(boolean selfChange) {
				loadList();
			}			
		};
		
		getELActivity().getContentResolver().registerContentObserver(ELContentProvider.URI_EL_NEW_PACKAGES, true, observer);
		
		loadList();
	}
	
	protected void loadList() {
		if (cursor != null) {
			cursor.close();
		}
		
		cursor = getELActivity().getContentResolver().query(ELContentProvider.URI_EL_NEW_PACKAGES, new String[] { "idx as _id", "title", "updated", "desc" }, null, null, "idx desc");		
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
			if (getELActivity().getServiceAccess().checkNewPackages()) {
				Toast.makeText(getELActivity(), "retrieving the info of new packages..", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getELActivity(), "check for new packages failed.", Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
