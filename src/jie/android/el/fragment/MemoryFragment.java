package jie.android.el.fragment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.FutureTask;

import jie.android.el.CommonConsts.Setting;
import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import jie.android.el.utils.Speaker;
import jie.android.el.utils.WordLoader;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MemoryFragment extends BaseFragment implements OnCheckedChangeListener, OnClickListener {

	private static String Tag = MemoryFragment.class.getSimpleName();
	
	private class ScoreData {
		public String word;
		public long checkin_date;
		public long checkin_count;
		public int score;
		public int level;
		
		public ScoreData(String word, long checkin_date, long checkin_count, int score, int level) {
			this.word = word;
			this.checkin_date = checkin_date;
			this.checkin_count = checkin_count;
			this.score = score;
			this.level = level;
		}
	}	

	private static final int MSG_LOADNEXT	=	1;
	private static final int MAX_LOAD_ONCE	=	60;
	
    private static final float rateTable[][] = {
        { 1.75f, 0.80f, 0.45f, 0.17f },
        { 1.50f, 1.25f, 0.55f, 0.20f },
        { 1.00f, 0.80f, 0.45f, 0.20f },
        { 0.80f, 0.50f, 0.30f, 0.17f }
    };

	private static final int judgeTable[][] = {
		{ 0, 1, 1, 2 },
		{ 2, 2, 3, 3 } 
	};

	private static final String[] projection = new String[] { "word", "checkin_date", "checkin_count", "score", "level" };	
	
	private TextView textTip;
	private TextView textWord;
	private RadioGroup radioGroup;
	
	private RelativeLayout layoutResult;
	private WebView webView;
	private ImageButton btnYes;
	private ImageButton btnNo;	
	
	private int tipTotal;
	private int tipCount = 0;
	
	private int judge = -1;
	private int level = -1;
	
	private int loadCount = 0;
	private Queue<ScoreData> scoreData = new LinkedList<ScoreData>();
	private ScoreData curData;

	private boolean isRandom = false;
	private boolean needCheck = true;
	private boolean needShowResult = true;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOADNEXT:
				showWord();
				break;
			default:
				break;
			}
		}		
	};
	
	private WordLoader.OnPostExecuteCallback wordLoaderCallback = new WordLoader.OnPostExecuteCallback() {
		
		@Override
		public void OnPostExecute(String word, String result) {
			showResult(word, result);
		}
	};	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_memory);
		this.setMenuRes(R.menu.fragment_memory);
		
		loadPreferences();
	}

	private void loadPreferences() {
		SharedPreferences prefs = getELActivity().getSharedPreferences();
		isRandom = prefs.getBoolean(Setting.MEMORY_MODE_RANDOM, false);
		needCheck = prefs.getBoolean(Setting.MEMORY_NEED_CHECK, true);
		needShowResult = prefs.getBoolean(Setting.MEMORY_SHOW_RESULT, true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		textTip = (TextView) view.findViewById(R.id.textView3);
		textWord = (TextView) view.findViewById(R.id.textView2);
		textWord.setOnClickListener(this);
		radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup1);
		radioGroup.clearCheck();
		radioGroup.setOnCheckedChangeListener(this);
		
		layoutResult = (RelativeLayout) view.findViewById(R.id.layoutResult);
		webView = (WebView) view.findViewById(R.id.webView1);
		btnNo = (ImageButton) view.findViewById(R.id.imageButton1);
		btnNo.setOnClickListener(this);
		btnYes = (ImageButton) view.findViewById(R.id.imageButton2);
		btnYes.setOnClickListener(this);		
	
		tipTotal = getScoreTotal();
		
		handler.sendEmptyMessage(MSG_LOADNEXT);
	}
	
	private int getScoreTotal() {
		Cursor cursor = getELActivity().getContentResolver().query(ELContentProvider.URI_EL_SCORE, new String[] { "count(*)" }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
		} finally {
			cursor.close();
		}
		return 0;
	}
	
	private ScoreData loadNext() {
		if (!isRandom) {
			if (scoreData.isEmpty()) {
				String sort = "score limit " + MAX_LOAD_ONCE + " offset " + (loadCount ++) * MAX_LOAD_ONCE;
				Cursor cursor = getELActivity().getContentResolver().query(ELContentProvider.URI_EL_SCORE_NEXT, projection, null, null, sort);
				try {
					if (cursor.moveToFirst()) {
						do {
							scoreData.add(new ScoreData(cursor.getString(0), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3), cursor.getInt(4)));
						} while (cursor.moveToNext());
					}
				} finally {
					cursor.close();
				}
			}
			
			return scoreData.poll();
			
		} else {
			Cursor cursor = getELActivity().getContentResolver().query(ELContentProvider.URI_EL_SCORE_RANDOM, projection, null, null, null);
			try {
				if (cursor.moveToFirst()) {
					return new ScoreData(cursor.getString(0), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3), cursor.getInt(4));
				}
			} finally {
				cursor.close();
			}
			
			return null;			
		}
	}
	
	private void showWord() {

		showResultLayout(false);
		
		curData = loadNext();
		
		if (curData != null) {
			
			textTip.setText(String.format("%d/%d", ++ tipCount, tipTotal));
			textWord.setText(curData.word);
			
			radioGroup.clearCheck();
			
			loadWordResult(curData.word);
			
		} else { 
			Toast.makeText(getELActivity(), "No more words in vocab", Toast.LENGTH_SHORT).show();
			textTip.setText(String.format("%d/%d", tipCount, tipTotal));
			textWord.setText("No more words");
			enableLevelRadios(false);
//			radioGroup.setClickable(false);
		}
		
		radioGroup.clearCheck();		
	}
	
	private void loadWordResult(String word) {
		WordLoader loader = new WordLoader(getELActivity().getServiceAccess(), wordLoaderCallback);
		loader.execute(word);		
	}
	
	private void showResultLayout(boolean show) {
		enableLevelRadios(show ? false : true);
		//radioGroup.setClickable(show ? false : true);
		layoutResult.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void enableLevelRadios(boolean enabled) {
		for (int i = 0; i < radioGroup.getChildCount(); ++ i) {
			radioGroup.getChildAt(i).setEnabled(enabled);
		}
	}
	
	private void calcScoreData(ScoreData data, int level, int judge) {
		int check = judgeTable[judge][level];
		data.score = (int) ((data.score + 1) * rateTable[data.level][check]);
		data.level = level;
		Log.d(Tag, "new score is " + data.score);
	}
	
	private void updateScoreData(ScoreData data, int level, int judge) {
		calcScoreData(data, level, judge);
		
		ContentValues values = new ContentValues();
		values.put("score", data.score);
		values.put("level", data.level);
		getELActivity().getContentResolver().update(ELContentProvider.URI_EL_SCORE_UPDATE_SCORE, values, "word=?", new String[] { data.word });	
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == textWord.getId()) {
			Speaker.speak(textWord.getText().toString());
		} else {
			judge = (v.getId() == btnYes.getId() ? 0 : 1);
			
			updateScoreData(curData, level, judge);
			
			handler.sendEmptyMessage(MSG_LOADNEXT);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId) {
		case R.id.radio0:
			level = 0;
			break;
		case R.id.radio1:
			level = 1;
			break;
		case R.id.radio2:
			level = 2;
			break;
		case R.id.radio3:
			level = 3;
			break;
		default:
			return;
		}
		
		showResultLayout(true);
	}

	private void showResult(String word, String result) {
		webView.loadDataWithBaseURL(null, result, "text/html", "utf-8", null);
	}	
}
