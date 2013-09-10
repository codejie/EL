package jie.android.el.view;

import jie.android.el.R;
import jie.android.el.utils.Utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ELPopupWindow extends LinearLayout {

	private Paint innerPaint = null;
	private Paint borderPaint = null;
	
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
		super.dispatchDraw(canvas);
		
    	RectF drawRect = new RectF();
    	drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
    	
    	canvas.drawRect(drawRect, innerPaint);		
	}

	private void initViews(Context context) {
		
		AttributeSet attrs = Utils.getAttributeSet(context, "android.widget.Linearlayout", R.layout.layout_popwin_linearlayout);		
		LinearLayout ll = new LinearLayout(context, attrs);
	}

}
