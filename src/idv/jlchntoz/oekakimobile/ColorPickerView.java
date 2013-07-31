/*
 *  This file is part of Oekaki Mobile.
 *  Copyright (C) 2013 Jeremy Lam
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package idv.jlchntoz.oekakimobile;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;

public class ColorPickerView extends View {
	Bitmap _hsvColorWheel;
	Paint _p, _ps;
	int width, height, currentMode;
	float radius, innerRadius, centerX, centerY, SVSize;
	int SVX1, SVY1, SVX2, SVY2, paddingSize;
	int[] _px;
	float[] selHSV, selHSV1;
	LinearGradient sh1, sh2;
	ComposeShader sh3;

	public ColorPickerView(Context c) {
		super(c);
		initialize();
	}
	
	public ColorPickerView(Context c, AttributeSet a) {
		super(c, a);
		initialize();
	}
	
	private void initialize() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		selHSV = new float[]{0, 0, 1};
		selHSV1 = new float[]{0, 1, 1};
		currentMode = 0;
		_ps = new Paint();
		_p = new Paint();
		_ps.setStyle(Style.FILL);
		_p.setStyle(Style.STROKE);
		_p.setStrokeWidth(3);
		paddingSize = (int)TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				this.getContext().getResources().getDimension(R.dimen.dialpgpadding),
				this.getContext().getResources().getDisplayMetrics());
	}
	
	public int getColor() {
		return Color.HSVToColor(selHSV);
	}
	
	public void setColor(int color) {
		Color.colorToHSV(color, selHSV);
		selHSV1[0] = selHSV[0];
		updateShaders();
	}

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
    	width = w;
    	height = h;
    	measuseValues(w, h);
		invalidate();
    }
    
    @Override
    protected void onDraw(Canvas c) {
    	c.drawColor(Color.TRANSPARENT);
    	
    	if(_hsvColorWheel != null)
    		c.drawBitmap(_hsvColorWheel, 0, 0, null);

    	_ps.setShader(sh3);
    	c.drawRect(SVX1, SVY1, SVX2, SVY2, _ps);

    	_p.setColor(0xFF<<24|~Color.HSVToColor(selHSV1));
    	float r = getRadian(selHSV[0]), cr = (float)Math.cos(r), sr = (float)Math.sin(r);
    	c.drawLine(cr*radius+centerX, sr*radius+centerY, cr*innerRadius+centerX, sr*innerRadius+centerY, _p);

    	_p.setColor(0xFF<<24|~getColor());
    	c.drawCircle(SVX1+selHSV[1]*SVSize, SVY1+selHSV[2]*SVSize, 5, _p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
    	float ex = e.getX(), ey = e.getY(), d = (float)getDistance(ex, ey, centerX, centerY);
    	switch(e.getAction()) {
	    	case MotionEvent.ACTION_DOWN:
	    		if(d < radius && d > innerRadius)
	    			currentMode = 1;
	    		else if(isIn(SVX1, SVY1, SVX2, SVY2, ex, ey))
	    			currentMode = 2;
	    		break;
	    	case MotionEvent.ACTION_UP:
	    		currentMode = 0;
	    		break;
    	}
		switch(currentMode) {
			case 1:
				selHSV1[0] = selHSV[0] = getAngle(ex-centerX, ey-centerY);
				updateShaders();
				return true;
			case 2:
				selHSV[1] = Math.min(1, Math.max(0, (float)((ex-SVX1)/SVSize)));
				selHSV[2] =  Math.min(1, Math.max(0, (float)((ey-SVY1)/SVSize)));
				invalidate();
				return true;
			default:
			    return false;
		}
    }

	@Override
	protected void onMeasure(int w, int h) {
	    int widthSize = MeasureSpec.getSize(w);
	    int heightSize = MeasureSpec.getSize(h);
	    int height = Math.min(widthSize, heightSize);
	    int mh;
	    switch(MeasureSpec.getMode(h)) {
	    case MeasureSpec.EXACTLY:
	        mh = heightSize;
	    case MeasureSpec.AT_MOST:
	    	mh = Math.min(height, heightSize);
	    	break;
	    default:
	    	mh = height;
		   	break;
	    }
	    setMeasuredDimension(widthSize, mh);
	}
    
    private void measuseValues(int w, int h) {
    	width = w;
    	height = h;
    	radius = Math.min(w, h)/2-paddingSize;
    	innerRadius = radius*0.8F;
    	centerX  = w/2;
    	centerY = h/2;
    	float d = innerRadius*(float)Math.sqrt(2)/2;
    	SVX1 = (int)Math.round(centerX-d);
    	SVY1 = (int)Math.round(centerY-d);
    	SVX2 = (int)Math.round(centerX+d);
    	SVY2 = (int)Math.round(centerY+d);
    	SVSize = d*2;
    	sh1 = new LinearGradient(SVX1, SVY1, SVX1, SVY2, Color.BLACK, Color.WHITE, TileMode.CLAMP);
    	updateShaders();
    	updateBitmap();
    }
    
    private void updateShaders() {
    	if(sh1 == null) return;
    	sh2 = new LinearGradient(SVX1, SVY1, SVX2, SVY1, Color.WHITE, Color.HSVToColor(selHSV1), TileMode.CLAMP);
    	sh3 = new ComposeShader(sh1, sh2, PorterDuff.Mode.MULTIPLY);
    	if(_hsvColorWheel == null) updateBitmap();
		invalidate();
    }
    
    private Bitmap updateBitmap() {
    	float[] hsv = new float[]{0, 1, 1};
    	if(width < 1 || height < 1) return null;
    	boolean updateAll = _hsvColorWheel == null || width != _hsvColorWheel.getWidth() || height != _hsvColorWheel.getHeight();
    	if(updateAll) {
    		_hsvColorWheel = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    		_px = new int[width*height];
        	int x1 = (int)(centerX-radius), x2 =  (int)(centerX+radius);
        	int y1 = (int)(centerY-radius), y2 =  (int)(centerY+radius);
        	float d = 0;
        	for(int y = y1; y < y2; y++) {
        		for(int x = x1; x < x2; x++) {
    				d = getDistance(x, y, centerX, centerY);
    				if(d < radius && d > innerRadius) {
    					hsv[0] = getAngle(x-centerX, y-centerY);
    					_px[x+width*y] = Color.HSVToColor(hsv);
    				}
    			}
    		}
    	}
    	_hsvColorWheel.setPixels(_px, 0, width, 0, 0, width, height);
    	return _hsvColorWheel;
    }
    
    private float getDistance(float x1, float y1, float x2, float y2) {
    	return (float)Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
    }
    
    private float getAngle(float x, float y) {
    	return (float)(Math.atan2(y, x)/Math.PI*180)+180;
    }
    
    private float getRadian(float a) {
    	return (float)((a-180)/ 180f*Math.PI);
    }
    
    private boolean isIn(float x1, float y1, float x2, float y2, float targetX, float targetY) {
    	if(x1 > x2) {
    		float t = x2;
    		x2 = x1;
    		x1 = t;
    	}
    	if(y1 > y2) {
    		float t = y2;
    		y2 = y1;
    		y1 = t;
    	}
    	return targetX > x1 && targetX < x2 && targetY > y1 && targetY < y2;
    }
}
