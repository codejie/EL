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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		checkPlayStopAfterCurrent = (CheckBox) view.findViewById(R.id.checkBox1);
		checkPlayRandomOrder = (CheckBox) view.findViewById(R.id.checkBox2);
		
		checkContentFontMedium = (CheckBox) view.findViewById(R.id.checkBox3);
		checkContentFontLarge = (CheckBox) view.findViewById(R.id.checkBox4);

		updateSetting();
		
		checkPlayStopAfterCurrent.setOnCheckedChangeListener(this);		
		checkPlayRandomOrder.setOnCheckedChangeListener(this);
		checkContentFontMedium.setOnCheckedChangeListener(this);		
		checkContentFontLarge.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean checked) {
		
		if (checked) {
			if (view.getId() == R.id.checkBox3) {
				checkContentFontLarge.setChecked(false);			
			} else if (view.getId() == R.id.checkBox4) {
				checkContentFontMedium.setChecked(false);				
			}
		}		
		saveSetting();
	}

	private void updateSetting() {
		SharedPreferences prefs = getELActivity().getSharedPreferences();
		
		checkPlayStopAfterCurrent.setChecked(prefs.getBoolean(Setting.PLAY_STOP_AFTER_CURRENT, false));
		checkPlayRandomOrder.setChecked(prefs.getBoolean(Setting.PLAY_RANDOM_ORDER, false));
		checkContentFontMedium.setChecked(prefs.getBoolean(Setting.CONTENTY_MEDIUM_FONT_SIZE, false));
		checkContentFontLarge.setChecked(prefs.getBoolean(Setting.CONTENTY_LARGE_FONT_SIZE, false));
		
	}
	
	private void saveSetting() {
		
		SharedPreferences.Editor editor = getELActivity().getSharedPreferences().edit();

		editor.putBoolean(Setting.PLAY_STOP_AFTER_CURRENT, checkPlayStopAfterCurrent.isChecked());
		editor.putBoolean(Setting.PLAY_RANDOM_ORDER, checkPlayRandomOrder.isChecked());
		
		editor.putBoolean(Setting.CONTENTY_MEDIUM_FONT_SIZE, checkContentFontMedium.isChecked());
		editor.putBoolean(Setting.CONTENTY_LARGE_FONT_SIZE, checkContentFontLarge.isChecked());
		
		editor.commit();
	}	
}
