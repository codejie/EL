package jie.android.el.fragment;

import jie.android.el.CommonConsts.Setting;
import jie.android.el.FragmentSwitcher;
import jie.android.el.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingFragment extends BaseFragment implements OnCheckedChangeListener {

	private CheckBox checkPlayStopAfterCurrent = null;
	private CheckBox checkPlayRandomOrder = null;
	private CheckBox checkContentFontMedium = null;
	private CheckBox checkContentFontLarge = null;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_setting);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			getELActivity().showFragment(FragmentSwitcher.Type.LIST, null);
			return true;
		}
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		checkPlayStopAfterCurrent = (CheckBox) view.findViewById(R.id.checkBox1);
		checkPlayStopAfterCurrent.setOnCheckedChangeListener(this);		
		checkPlayRandomOrder = (CheckBox) view.findViewById(R.id.checkBox2);
		checkPlayRandomOrder.setOnCheckedChangeListener(this);
		
		checkContentFontMedium = (CheckBox) view.findViewById(R.id.checkBox3);
		checkContentFontMedium.setOnCheckedChangeListener(this);		
		checkContentFontLarge = (CheckBox) view.findViewById(R.id.checkBox4);
		checkContentFontLarge.setOnCheckedChangeListener(this);

		updateSetting();
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		saveSetting();
	}

	private void updateSetting() {
		SharedPreferences prefs = getELActivity().getSharedPreferences();
		
		checkPlayStopAfterCurrent.setChecked(prefs.getBoolean(Setting.PLAY_STOP_AFTER_CURRENT, false));
		// TODO Auto-generated method stub
		
	}
	
	private void saveSetting() {
		
		SharedPreferences.Editor editor = getELActivity().getSharedPreferences().edit();

		editor.putBoolean(Setting.PLAY_STOP_AFTER_CURRENT, checkPlayStopAfterCurrent.isChecked());
		
		editor.commit();
	}	
}
