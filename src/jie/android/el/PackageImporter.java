package jie.android.el;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

import jie.android.el.utils.Utils;

import android.os.AsyncTask;
import android.os.Environment;

public class PackageImporter {
	
	private class ImportTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... arg0) {
			String file = Environment.getExternalStorageDirectory() + "/jie/el" + File.separator + arg0[0];
			// TODO Auto-generated method stub
			return arg0[0];
		}

		@Override
		protected void onPostExecute(String result) {
			
			packageList.remove(result);
			
			String file = Environment.getExternalStorageDirectory() + "/jie/el" + File.separator + result;
			
			Utils.removeFile(file);
			
			taskRunning = false;
			
			startImport();
		}
		
	}
	
	
	private ELActivity activity = null;
	private ArrayList<String> packageList = new ArrayList<String>();
	
	private boolean taskRunning = false;

	public PackageImporter(ELActivity activity, String[] packageList) {
		this.activity = activity;
		if (packageList != null) {
			for (final String str : packageList) {
				this.packageList.add(str);
			}
		}
	}

	public static String[] check() {
		String path = Environment.getExternalStorageDirectory() + "/jie/el";
		File p = new File(path);
		return p.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String file) {
				return file.startsWith("package") && file.endsWith(".db");
			}
		});
	}

	public void refresh() {
		final String[] res = PackageImporter.check();
		if (res != null && res.length > 0) {
			packageList.clear();
			for (final String str : packageList) {
				this.packageList.add(str);
			}
		}
		
		startImport();
	}
	
	private void startImport() {
		if (packageList.size() > 0) {
			if (!taskRunning) {
				
				taskRunning = true;
				
				new ImportTask().execute(packageList.get(0));
				
			}
		}
	}
	
}
