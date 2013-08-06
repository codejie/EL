package jie.android.el.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class PopupLayout extends LinearLayout {

	private Paint innerPaint = null;
	private Paint borderPaint = null;
	
	public PopupLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}

	private void init() {
		innerPaint = new Paint();
		innerPaint.setARGB(238, 102, 102, 102); 
		innerPaint.setAntiAlias(true);

		borderPaint = new Paint();
		borderPaint.setARGB(255, 255, 255, 255);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(4);		
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
    	RectF drawRect = new RectF();
    	drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
    	
    	canvas.drawRoundRect(drawRect, 5, 5, innerPaint);
		canvas.drawRoundRect(drawRect, 5, 5, borderPaint);
		
		super.dispatchDraw(canvas);
	}

}
