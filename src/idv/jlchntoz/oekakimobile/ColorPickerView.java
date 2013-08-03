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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;

public class ColorPickerView extends View {
	Bitmap _hsvColorWheel;
	Paint _p, _ps;
	int width, height, downMode, selectMode, selColor;
	float radius, innerRadius, centerX, centerY, SVSize;
	int SVX1, SVY1, SVX2, SVY2, paddingSize;
	int[] _px;
	float[] selHSV, selHSV1;
	LinearGradient sh1, sh2;
	ComposeShader sh3;
	colorslider c_r, c_g, c_b;
	colorslider[] sliders;

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
		downMode = 0;
		selectMode = 0;
		_ps = new Paint();
		_p = new Paint();
		_ps.setStyle(Style.FILL);
		_p.setStyle(Style.STROKE);
		_p.setStrokeWidth(3);
		_ps.setTypeface(Typeface.SANS_SERIF);
		_ps.setTextAlign(Align.CENTER);
		_ps.setTextSize(20);
		selColor = Color.TRANSPARENT;
		sliders = new colorslider[3];
		sliders[0] = c_r = new colorslider();
		sliders[1] = c_g = new colorslider();
		sliders[2] = c_b = new colorslider();
		paddingSize = (int)TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				this.getContext().getResources().getDimension(R.dimen.dialpgpadding),
				this.getContext().getResources().getDisplayMetrics());
	}
	
	public int getColor() {
		if(selectMode == 0)
			selColor = Color.HSVToColor(selHSV);
		return selColor;
	}
	
	public void setColor(int color) {
		selColor = color;
		Color.colorToHSV(color, selHSV);
		selHSV1[0] = selHSV[0];
		c_r.setValue(Color.red(color)/255F);
		c_g.setValue(Color.green(color)/255F);
		c_b.setValue(Color.blue(color)/255F);
		updateShaders();
	}

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
    	width = w;
    	height = h;
    	measuseValues(w, h);
		invalidate();
    }
    
    @SuppressLint("WrongCall")
	@Override
    protected void onDraw(Canvas c) {
    	c.drawColor(Color.TRANSPARENT);
		_ps.setShader(null);
		_ps.setColor(selColor);
		c.drawRect(width-paddingSize*3, paddingSize, width-paddingSize, paddingSize*3, _ps);
    	if(selectMode == 1) {
        	_ps.setColor(Color.WHITE);
        	c.drawText("HSV", paddingSize*2, paddingSize*2, _ps);
        	
			for(colorslider s : sliders)
				s.onDraw(c);
    	} else {
        	_ps.setColor(Color.WHITE);
        	c.drawText("RGB", paddingSize*2, paddingSize*2, _ps);
        	
	    	if(_hsvColorWheel != null)
	    		c.drawBitmap(_hsvColorWheel, 0, 0, null);
	
	    	_ps.setShader(sh3);
	    	c.drawRect(SVX1, SVY1, SVX2, SVY2, _ps);
	
	    	_p.setColor(fastCompleColor(Color.HSVToColor(selHSV1)));
	    	float r = getRadian(selHSV[0]), cr = (float)Math.cos(r), sr = (float)Math.sin(r);
	    	c.drawLine(cr*radius+centerX, sr*radius+centerY, cr*innerRadius+centerX, sr*innerRadius+centerY, _p);
	
	    	_p.setColor(fastCompleColor(getColor()));
	    	c.drawCircle(SVX1+selHSV[1]*SVSize, SVY1+selHSV[2]*SVSize, 5, _p);
    	}
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
    	float ex = e.getX(), ey = e.getY(), d = (float)getDistance(ex, ey, centerX, centerY);
    	if(e.getAction() == MotionEvent.ACTION_DOWN && ex < paddingSize*3 && ey < paddingSize*3) {
    		selectMode = (selectMode+1)%2;
    		invalidate();
    		return true;
    	} else if(selectMode == 1) {
			boolean ret = false;
			for(colorslider s : sliders)
				if(s.onTouchEvent(e)) {
					ret = true;
					break;
				}
			updateShaders();
			Color.colorToHSV(selColor, selHSV);
			selHSV1[0] = selHSV[0]; // Sync the color to HSV mode.
			return ret;
		} else {
	    	switch(e.getAction()) {
		    	case MotionEvent.ACTION_DOWN:
		    		if(d < radius && d > innerRadius)
		    			downMode = 1;
		    		else if(isIn(SVX1, SVY1, SVX2, SVY2, ex, ey))
		    			downMode = 2;
		    		break;
		    	case MotionEvent.ACTION_UP:
		    		downMode = 0;
		    		break;
	    	}
			switch(downMode) {
				case 1:
					selHSV1[0] = selHSV[0] = getAngle(ex-centerX, ey-centerY);
					updateShaders();
					break;
				case 2:
					selHSV[1] = getPercentage(SVX1, SVX2, ex);
					selHSV[2] =  getPercentage(SVY1, SVY2, ey);
					invalidate();
					break;
			}
			selColor = Color.HSVToColor(selHSV); // Sync the color to RGB mode.
			c_r.setValue(Color.red(selColor)/255F);
			c_g.setValue(Color.green(selColor)/255F);
			c_b.setValue(Color.blue(selColor)/255F);
			return downMode != 0;
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
    	for(int i = 0; i < sliders.length; i++) {
    		sliders[i].setPosition(paddingSize, paddingSize*4*(i+1), w-paddingSize*2, paddingSize*4);
    	}
    	updateShaders();
    	updateBitmap();
    }
    
    private void updateShaders() {
		int _r = Math.round(c_r.getValue()*255);
		int _g = Math.round(c_g.getValue()*255);
		int _b = Math.round(c_b.getValue()*255);
		c_r.setColor(Color.argb(255, 0, _g, _b), Color.argb(255, 255, _g, _b));
		c_g.setColor(Color.argb(255, _r, 0, _b), Color.argb(255, _r, 255, _b));
		c_b.setColor(Color.argb(255, _r, _g, 0), Color.argb(255, _r, _g, 255));
    	if(selectMode == 1)
			selColor = Color.argb(255, _r, _g, _b);
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
	
	private int mixValue(int v1, int v2, float p) {
		return v1+Math.round((v2-v1)*p);
	}
	
	private float mixValue(float v1, float v2, float p) {
		return v1+(v2-v1)*p;
	}
	
	private float getPercentage(float start, float end, float mid) {
		return Math.max(0, Math.min(1, (mid-start)/(end-start)));
	}
	
	private int fastCompleColor(int color) {
		return 0xFF<<24|~color;
	}
    
    private class colorslider {
    	float x1, y1, x2, y2, value;
    	boolean isDown;
    	int color1, color2;
    	Paint _p, _p2;
    	LinearGradient _lg;
    	
    	public colorslider() {
    		this.value = 0;
    		this.isDown = false;
    		this.color1 = Color.TRANSPARENT;
    		this.color2 = Color.TRANSPARENT;
    		this._p = new Paint();
    		this._p2 = new Paint();
    		this._p.setStyle(Style.STROKE);
    		this._p.setStrokeWidth(3);
    		this._p2.setStyle(Style.FILL);
    		setPosition(0, 0, 0, 0);
    	}
    	
    	public void setPosition(float x, float y, float w, float h) {
    		this.x1 = x;
    		this.x2 = x+w;
    		this.y1 = y;
    		this.y2 = y+h;
    		if(this._lg != null)
    			setColor(this.color1, this.color2);
    	}
    	
    	public void setColor(int start, int end) {
    		this.color1 = start;
    		this.color2 = end;
    		this._lg = new LinearGradient(this.x1, this.y1, this.x2, this.y1, start, end, TileMode.CLAMP);
    		this._p2.setShader(_lg);
    	}
    	
    	public int getSelectedColor() {
    		int _a = mixValue(Color.alpha(this.color1), Color.alpha(this.color2), this.value);
    		int _r = mixValue(Color.red(this.color1), Color.red(this.color2), this.value);
    		int _g = mixValue(Color.green(this.color1), Color.green(this.color2), this.value);
    		int _b = mixValue(Color.blue(this.color1), Color.blue(this.color2), this.value);
    		return Color.argb(_a, _r, _g, _b);
    	}
    	
    	public void setValue(float value) {
    		this.value = value;
    	}
    	
    	public float getValue() {
    		return value;
    	}
    	
    	public void onDraw(Canvas c) {
    		c.drawRect(this.x1, this.y1, this.x2, this.y2, this._p2);
    		float _x = mixValue(this.x1, this.x2, this.value);
    		_p.setColor(fastCompleColor(getSelectedColor()));
    		c.drawLine(_x, this.y1, _x, this.y2, this._p);
    	}
    	
    	public boolean onTouchEvent(MotionEvent e) {
    		float _x = e.getX(), _y =e.getY();
    		switch(e.getAction()) {
    		case MotionEvent.ACTION_DOWN:
    			if(isIn(this.x1, this.y1, this.x2, this.y2, _x, _y))
    				isDown = true;
    			break;
    		case MotionEvent.ACTION_UP:
    			isDown = false;
    			break;
    		}
    		if(isDown)
    			this.value = getPercentage(this.x1, this.x2, _x);
    		return isDown;
    	}
    }
}
