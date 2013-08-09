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

import java.util.*;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;

public class ColorPaletteSelector extends View {
	OnSelectedListener Listener;
	ArrayList<Integer> Colors;
	final EventHandler _evh;
	int width, height, targetH;
	float posX, posY;
	final int blockSize;
	int _a, _b;
	Paint p;

	public interface OnSelectedListener {
		public void OnSelected(int index, int color);

		public int OnReplace(int index);
	}

	public ColorPaletteSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		_evh = new EventHandler();
		blockSize = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources()
						.getDisplayMetrics());
		Colors = new ArrayList<Integer>();
		p = new Paint();
		this.setClickable(true);
		this.setLongClickable(true);
		this.setOnClickListener(_evh);
		this.setOnLongClickListener(_evh);
		this.setOnTouchListener(_evh);
		targetH = blockSize * 2 + 1;
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		width = w;
		height = h;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas c) {
		c.drawColor(Color.TRANSPARENT);
		_a = _b = 0;
		boolean extraHeight = false;
		for (int i = 0; i < Colors.size(); i++) {
			if (_a + blockSize > width) {
				_a = 0;
				_b += blockSize;
			}
			p.setStyle(Style.FILL);
			p.setColor(getColor(i));
			c.drawRect(_a, _b, _a + blockSize, _b + blockSize, p);
			p.setStyle(Style.STROKE);
			p.setColor(Color.GRAY);
			c.drawRect(_a, _b, _a + blockSize, _b + blockSize, p);
			_a += blockSize;
		}
		while (_a + blockSize < width || !extraHeight) {
			p.setStyle(Style.STROKE);
			p.setColor(Color.GRAY);
			c.drawRect(_a, _b, _a + blockSize, _b + blockSize, p);
			_a += blockSize;
			if (!extraHeight && _a + blockSize > width) {
				extraHeight = true;
				_a = 0;
				_b += blockSize;
			}
		}
		if (_b + blockSize + 1 > height) {
			targetH = _b + blockSize + 1;
			requestLayout();
		}
	}

	@Override
	protected void onMeasure(int w, int h) {
		int widthSize = MeasureSpec.getSize(w);
		int heightSize = MeasureSpec.getSize(h);
		int mh;
		switch (MeasureSpec.getMode(h)) {
		case MeasureSpec.EXACTLY:
			mh = heightSize;
		case MeasureSpec.AT_MOST:
			mh = Math.min(targetH, heightSize);
			break;
		default:
			mh = targetH;
			break;
		}
		setMeasuredDimension(widthSize, mh);
	}

	public void setListener(OnSelectedListener Listener) {
		this.Listener = Listener;
	}

	public void setColor(int index, int color) {
		while (index >= Colors.size())
			Colors.add(Color.TRANSPARENT);
		Colors.set(index, color);
		invalidate();
	}

	public int getColor(int index) {
		return Colors.get(index);
	}

	private int getIndexByPosition(float x, float y) {
		int widthFixed = (int) Math.floor(width / blockSize);
		x = (float) Math.floor(x / blockSize);
		y = (float) Math.floor(y / blockSize);
		int index = (int) (y * widthFixed + x);
		if (x > widthFixed)
			return -1;
		return index;
	}

	private class EventHandler implements OnLongClickListener, OnTouchListener,
			OnClickListener {
		@Override
		public void onClick(View v) {
			if (v != ColorPaletteSelector.this)
				return;
			int index = getIndexByPosition(posX, posY);
			if (index >= 0 && index < Colors.size() && Listener != null)
				Listener.OnSelected(index, Colors.get(index));
		}

		@Override
		public boolean onLongClick(View v) {
			if (v != ColorPaletteSelector.this)
				return false;
			int index = getIndexByPosition(posX, posY);
			if (index >= 0 && Listener != null) {
				setColor(index, Listener.OnReplace(index));
				ColorPaletteSelector.this.invalidate();
				return true;
			}
			return false;
		}

		@Override
		public boolean onTouch(View v, MotionEvent e) {
			if (v != ColorPaletteSelector.this)
				return false;
			posX = e.getX();
			posY = e.getY();
			return false;
		}
	}
}
