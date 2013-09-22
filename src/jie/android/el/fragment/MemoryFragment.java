package jie.android.el.fragment;

import jie.android.el.R;
import jie.android.el.database.ELContentProvider;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setLayoutRes(R.layout.fragment_memory);
		this.setMenuRes(R.menu.fragment_memory);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		textTip = (TextView) view.findViewById(R.id.textView3);
		textWord = (TextView) view.findViewById(R.id.textView2);
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
		//TODO: prefers
		Cursor cursor = getELActivity().getContentResolver().query(ELContentProvider.URI_EL_SCORE_NEXT, projection, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				return new ScoreData(cursor.getString(0), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3), cursor.getInt(4));
			}
		} finally {
			cursor.close();
		}
		
		return null;
	}
	
	private void showWord() {

		showResultLayout(false);
		
		if (tipCount < tipCount) {
			ScoreData data = loadNext();
			
			if (data != null) {
				textTip.setText(String.format("%d/%d", tipCount ++, tipTotal));
				textWord.setText(data.word);
				
				radioGroup.clearCheck();
			}
		} else { 
			Toast.makeText(getELActivity(), "No more words in vocab", Toast.LENGTH_SHORT).show();
			textWord.setText("No more Words");
			radioGroup.setEnabled(false);
		}
		
		textTip.setText(String.format("%d/%d", tipCount ++, tipTotal));
		
		radioGroup.clearCheck();		
	}
	
	private void showResultLayout(boolean show) {
		layoutResult.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void calcScoreData(ScoreData data) {
		
	}

	@Override
	public void onClick(View v) {
		handler.sendEmptyMessage(MSG_LOADNEXT);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId) {
		case R.id.radio0:
			break;
		case R.id.radio1:
			break;
		case R.id.radio2:
			break;
		case R.id.radio3:
			break;
		default:
			return;
		}
		
		showResultLayout(true);
	}	
	
}
