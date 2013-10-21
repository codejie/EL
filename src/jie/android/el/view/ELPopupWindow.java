package jie.android.el.view;

import jie.android.el.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ELPopupWindow extends LinearLayout {

	public interface OnPopupWindowListener {
		void onCloseClick();
		void onTextClick(String text);
		boolean onTextLongClick(String text);
	}
	
	private Paint innerPaint;
	private Paint borderPaint;
	
	private OnPopupWindowListener listener;
	private Animation animShow = null;
	private Animation animHide = null;
	
	private TextView textView;
	private WebView webView;
	
	public ELPopupWindow(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
		
		initViews(context);
	}

	private void init() {
		innerPaint = new Paint();
		innerPaint.setARGB(238, 102, 102, 102); 
		innerPaint.setAntiAlias(true);

//		borderPaint = new Paint();
//		borderPaint.setARGB(255, 255, 255, 255);
//		borderPaint.setAntiAlias(true);
//		borderPaint.setStyle(Style.STROKE);
//		borderPaint.setStrokeWidth(4);	
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
    	RectF drawRect = new RectF();
    	drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
    	
    	canvas.drawRect(drawRect, innerPaint);
    	
		super.dispatchDraw(canvas);
	}

	private void initViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.layout_popwindow, this);
		
		final ImageView btnClose = (ImageView) v.findViewById(R.id.imageView1);
		btnClose.setClickable(true);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (listener != null) {
					listener.onCloseClick();
				}
			}			
		});
		
		textView = (TextView) v.findViewById(R.id.textView1);
		textView.setClickable(true);
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onTextClick(textView.getText().toString());
				}
			}
		});
		
		textView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				if (listener != null) {
					return listener.onTextLongClick(textView.getText().toString());
				}
				return false;
			}			
		});
		
		webView = (WebView) v.findViewById(R.id.webView2);
	}
	
	public void setText(String text) {
		if (textView != null) {
			textView.setText(text);
		}
	}
	
	public void loadWebContent(String html) {
		if (webView != null) {
			webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
		}
	}
	
	public void show(boolean show, Animation animation) {
		if (show) {
			setVisibility(View.VISIBLE);
			requestFocus();
			startAnimation(animation);			
		} else {
			startAnimation(animation);
			setVisibility(View.GONE);			
		}		
	}
	
	public void show(boolean show) {
		if (show) {
			if (getVisibility() != View.VISIBLE) {
				setVisibility(View.VISIBLE);
				requestFocus();
				startAnimation(animShow);
			}
		} else {
			if (getVisibility() != View.GONE) {
				startAnimation(animHide);
				setVisibility(View.GONE);
			}
		}
	}

	public boolean isShowing() {
		return getVisibility() != View.GONE;
	}
	
	public void setOnPopupWindowListener(OnPopupWindowListener l) {
		listener = l;
	}
	
	public void setAnimation(Animation show, Animation hide) {
		animShow = show;
		animHide = hide;
	}
}
