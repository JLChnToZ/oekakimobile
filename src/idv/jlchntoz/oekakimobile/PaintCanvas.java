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

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.os.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.chibipaint.CPController;
import com.chibipaint.engine.CPArtwork;
import com.chibipaint.engine.CPBrushInfo;
import com.chibipaint.util.CPBezier;
import com.chibipaint.util.CPRect;

public class PaintCanvas extends View implements CPController.ICPToolListener,
CPController.ICPModeListener, CPArtwork.ICPArtworkListener  {
	private static final int MSG_REDRAW = 1;
	
	private int width, height;
	private CPArtwork artwork;
	private Paint paintShader, paintHint;
	private Matrix transform, transform_invert;
	private CPMode ActiveMode, curDrawMode;
	private Bitmap BM, CheckerBoard;
	private BitmapShader CheckerBoardShader;
	private PointF offset;
	private int backgroundColor, checkerBoardColor1, checkerBoardColor2, selectionColor;
	private float rotation, zoom;
	private int screenWidth, screenHeight;
	public CPController controller;
	private ArrayList<MotionEvent> ME;
	private Thread TE;
	private Rect totalUpdateRegion;
	private boolean updateBM;
	
	//
	// Modes system: modes control the way the GUI is reacting to the user input
	// All the tools are implemented through modes
	//
	private CPMode colorPickerMode = new CPColorPickerMode();
	private CPMode moveCanvasMode = new CPMoveCanvasMode();
	private CPMode rotateCanvasMode = new CPRotateCanvasMode();
	private CPMode floodFillMode = new CPFloodFillMode();
	private CPMode rectSelectionMode = new CPRectSelectionMode();
	private CPMode moveToolMode = new CPMoveToolMode();

	// this must correspond to the stroke modes defined in CPToolInfo
	private CPMode drawingModes[] = { new CPFreehandMode(), new CPLineMode(), new CPBezierMode(), };
	
	public PaintCanvas(Context c) {
		super(c);
		width = 800;
		height = 600;
		initialize();
	}

	public PaintCanvas(Context c, int width, int height) {
		super(c);
		this.width = width;
		this.height = height;
		initialize();
	}
	
	public PaintCanvas(Context c, AttributeSet a) {
		super(c, a);
		width = 800;
		height = 600;
		initialize();
	}
	
	private void initialize() {
		curDrawMode = drawingModes[0];
		ActiveMode = curDrawMode;
		offset = new PointF(0, 0);
		zoom = 1;
		rotation = 0;
		screenWidth = 0;
		screenHeight = 0;
		paintShader = new Paint();
		paintHint = new Paint();
		controller = new CPControllerDroid();
		controller.addToolListener(this);
		controller.addModeListener(this);
		
		setArtWork(new CPArtwork(getContext(), width, height));
		
		Resources res = getResources();
		
		backgroundColor = res.getColor(R.color.BackgroundColor);
		checkerBoardColor1 = res.getColor(R.color.CheckerBoardColor1);
		checkerBoardColor2 = res.getColor(R.color.CheckerBoardColor2);
		selectionColor = res.getColor(R.color.SelectionColor);
		
		paintHint.setStyle(Paint.Style.STROKE);
		paintHint.setColor(selectionColor);
		paintHint.setStrokeWidth(1);
		
		doTransform();
		
		//Generate CheckerBoard Background
		if(CheckerBoard == null) {
			CheckerBoard = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
			for(int i = 0; i < 20; i++)
				for(int j = 0; j < 20; j++)
					if(Math.floor(i / 10) == 0 ^ Math.floor(j / 10) == 1)
						CheckerBoard.setPixel(i, j, checkerBoardColor1);
					else
						CheckerBoard.setPixel(i, j, checkerBoardColor2);
		}
		if(CheckerBoardShader == null)
			CheckerBoardShader = new BitmapShader(CheckerBoard, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
		paintShader.setShader(CheckerBoardShader);
	}
	
	@SuppressLint("HandlerLeak")
	private final Handler _h = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch(m.what) {
				case MSG_REDRAW:
					invalidate();
					break;
			}
		}
	};
	
	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		screenWidth = w;
		screenHeight = h;
		if(artwork != null)
			setOffset((screenWidth - artwork.width) / 2F, (screenHeight - artwork.height) / 2F);
	}
	
	public void setArtWork(CPArtwork artwork) {
		controller.setArtwork(artwork);
		this.artwork = artwork;
		this.BM = Bitmap.createBitmap(artwork.width, artwork.height, Bitmap.Config.ARGB_8888);
		this.width = artwork.width;
		this.height = artwork.height;
		BM.setPixels(artwork.getDisplayBM().data, 0, this.width, 0, 0, this.width, this.height);
		if(screenWidth > 0 && screenHeight > 0)
			setOffset((screenWidth - artwork.width) / 2F, (screenHeight - artwork.height) / 2F);
		artwork.addListener(this);
	}
	
	@Override
	public void onDraw(Canvas c) {
		c.drawColor(backgroundColor);
		c.drawRect(coordToDisplay(artwork.getSize()), paintShader);
		c.drawBitmap(BM, transform, null);
		ActiveMode.paint(c);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(ME == null)
			ME = new ArrayList<MotionEvent>();
		ME.add(MotionEvent.obtain(ev)); // Push current state to array pending for process
		if(TE == null || !TE.isAlive()) { // If the thread is died or not yet created, make the new one.
			TE = new Thread() {
				@Override
				public void run() {
					updateBM = false;
					for(int i = 0; i < ME.size(); i++) {
						MotionEvent e = ME.get(i);
						switch(e.getAction() & MotionEvent.ACTION_MASK) {
							case MotionEvent.ACTION_DOWN:
								ActiveMode.mousePressed(e);
								break;
							case MotionEvent.ACTION_MOVE:
									ActiveMode.mouseDragged(e);
								break;
							case MotionEvent.ACTION_UP:
								ActiveMode.mouseDragged(e);
								ActiveMode.mouseReleased(e);
								break;
							case MotionEvent.ACTION_POINTER_DOWN:
								ActiveMode.secondMousePressed(e);
								break;
							case MotionEvent.ACTION_POINTER_UP:
								ActiveMode.secondMouseReleased(e);
								break;
						}
						e.recycle();
					}
					ME.clear();
					updateBM = true;
					repaint();
				}
			};
			TE.start();
		}
		return true;
	}
	
	@Override
	public void updateRegion(CPArtwork artwork, CPRect region) {
		// Only updating the region changed, or the app will be super-laggy.
		// Prevent out of bounds, before passing parameters to update,
		// it should be checked to make sure the values are inside the bitmap.
		int _left = Math.max(0, region.left), _top = Math.max(0, region.top);
		int _width = region.getWidth(), _height = region.getHeight();
		if(_width - _left > width) _width = width + _left;
		if(_height - _top > height) _height = height + _top;
		if(totalUpdateRegion == null)
			totalUpdateRegion = new Rect(_left, _top, _left+_width, _top+_height);
		else
			totalUpdateRegion.union(_left, _top, _left+_width, _top+_height);
		repaint();
	}

	@Override
	public void layerChange(CPArtwork artwork) { }

	@Override
	public void modeChange(int mode) {
		switch (mode) {
		case CPController.M_DRAW:
			ActiveMode = curDrawMode;
			break;
		case CPController.M_FLOODFILL:
			ActiveMode = floodFillMode;
			break;

		case CPController.M_RECT_SELECTION:
			ActiveMode = rectSelectionMode;
			break;

		case CPController.M_MOVE_TOOL:
			ActiveMode = moveToolMode;
			break;

		case CPController.M_ROTATE_CANVAS:
			ActiveMode = rotateCanvasMode;
			break;
			
		case CPController.M_COLOR_PICKER:
			ActiveMode = colorPickerMode;
			break;
			
		case CPController.M_MOVE_CANVAS:
			ActiveMode = moveCanvasMode;
			break;
		}
	}
	
	private void doTransform() {
		if(transform == null)
			transform = new Matrix();
		if(transform_invert == null)
			transform_invert = new Matrix();
		transform.reset();
		transform.postTranslate(offset.x, offset.y);
		transform.postScale(zoom, zoom);
		transform.postRotate(rotation);
		transform.invert(transform_invert);
	}
	
	private PointF transformBy(Matrix m, PointF p) {
		float[] _p = new float[2];
		_p[0] = p.x;
		_p[1] = p.y;
		m.mapPoints(_p);
		return new PointF(_p[0], _p[1]);
	}
	
	private RectF transformBy(Matrix m, RectF r) {
		float[] _p = new float[4];
		_p[0] = r.left;
		_p[1] = r.top;
		_p[2] = r.right;
		_p[3] = r.bottom;
		m.mapPoints(_p);
		return new RectF(_p[0], _p[1], _p[2], _p[3]);
	}

	@Override
	public void newTool(int tool, CPBrushInfo toolInfo) {
		curDrawMode = drawingModes[toolInfo.strokeMode];
	}
	
	public PointF coordToDocument(PointF p) {
		return transformBy(transform_invert, p);
	}

	public PointF coordToDisplay(PointF p) {
		return transformBy(transform, p);
	}
	
	public RectF coordToDisplay(CPRect r) {
		return transformBy(transform, new RectF(r.left, r.top, r.right, r.bottom));
	}

	public PointF getOffset() {
		return offset;
	}
	
	public void setOffset(PointF p) {
		offset = p;
		doTransform();
	}
	
	public void setOffset(float x, float y) {
		setOffset(new PointF(x, y));
	}
	
	public void repaint() {
		if(updateBM && totalUpdateRegion != null) {
			BM.setPixels(artwork.getDisplayBM().data,
					totalUpdateRegion.left + totalUpdateRegion.top * width, width,
					totalUpdateRegion.left, totalUpdateRegion.top, totalUpdateRegion.width(), totalUpdateRegion.height());
			totalUpdateRegion = null;
			updateBM = false;
		}
		repaint(0, 0, width, height);
	}
	
	public void repaint(float x, float y, float w, float h) {
		Message m = new Message();
		m.what = MSG_REDRAW;
		_h.sendMessage(m);
	}
	
	public float getRotation2() {
		return rotation;
	}
	
	public void setRotation2(float r) {
		rotation = r;
		doTransform();
	}

	public void resetRotation() {
		setRotation2(0);
	}
	
	public float getZoom() {
		return zoom;
	}
	
	public void zoomIn() {
		zoom *= 2;
		doTransform();
	}
	
	public void zoomOut() {
		zoom /= 2;
		doTransform();
	}
	
	public void resetZoom() {
		setZoom(1);
	}
	
	public void setZoom(float value) {
		zoom = value;
		doTransform();
	}

	public PointF getSize() {
		return new PointF(width, height);
	}

	//
	// base class for the different modes
	//

	abstract class CPMode {

		// Mouse (Finger) Input
		public void mousePressed(MotionEvent e) { }

		public void mouseDragged(MotionEvent e) { }

		public void mouseReleased(MotionEvent e) { }

		public void mouseMoved(MotionEvent e) { }

		public void mouseClicked(MotionEvent e) { }

		public void mouseEntered(MotionEvent e) { }

		public void mouseExited(MotionEvent e) { }
		
		// Multi-touch here
		public void secondMousePressed(MotionEvent e) { }
		
		public void secondMouseReleased(MotionEvent e) { }

		// GUI drawing
		public void paint(Canvas g2d) { }

	}


	//
	// Freehand mode
	//

	// FIXME: dragLeft no longer necessary, should not specify the drag button

	class CPFreehandMode extends CPMode {

		boolean dragLeft = false;
		PointF smoothMouse = new PointF(0, 0);

		public void mousePressed(MotionEvent e) {
				PointF p = new PointF(e.getX(), e.getY());
				PointF pf = coordToDocument(p);

				dragLeft = true;
				artwork.beginStroke(pf.x, pf.y, e.getPressure());

				smoothMouse = pf;
		}

		public void mouseDragged(MotionEvent e) {
			PointF p = new PointF(e.getX(), e.getY());
			PointF pf = coordToDocument(p);

			float smoothing = Math.min(.999f, (float) Math.pow(controller.getBrushInfo().smoothing, .3));

			smoothMouse.x = (1f - smoothing) * pf.x + smoothing * smoothMouse.x;
			smoothMouse.y = (1f - smoothing) * pf.y + smoothing * smoothMouse.y;

			if (dragLeft) {
				artwork.continueStroke(smoothMouse.x, smoothMouse.y, e.getPressure());
			}
		}

		public void mouseReleased(MotionEvent e) {
				dragLeft = false;
				artwork.endStroke();

		}
	}

	//
	// Line drawing mode
	//

	class CPLineMode extends CPMode {

		boolean dragLine = false;
		PointF dragLineFrom, dragLineTo;

		public void mousePressed(MotionEvent e) {
			if (!dragLine) {
				PointF p = new PointF(e.getX(), e.getY());

				dragLine = true;
				dragLineFrom = dragLineTo = p;
			}
		}

		public void mouseDragged(MotionEvent e) {
			PointF p = new PointF(e.getX(), e.getY());

			RectF r = new RectF(Math.min(dragLineFrom.x, dragLineTo.x), Math.min(dragLineFrom.y, dragLineTo.y),
					Math.abs(dragLineFrom.x - dragLineTo.x) + 1, Math.abs(dragLineFrom.y - dragLineTo.y) + 1);
			r.union(new RectF(Math.min(dragLineFrom.x, p.x), Math.min(dragLineFrom.y, p.y), Math
					.abs(dragLineFrom.x - p.x) + 1, Math.abs(dragLineFrom.y - p.y) + 1));
			dragLineTo = p;
			repaint(r.left, r.top, r.width(), r.height());
		}

		public void mouseReleased(MotionEvent e) {
				PointF p = new PointF(e.getX(), e.getY());
				PointF pf = coordToDocument(p);

				dragLine = false;

				PointF from = coordToDocument(dragLineFrom);
				artwork.beginStroke(from.x, from.y, 1);
				artwork.continueStroke(pf.x, pf.y, 1);
				artwork.endStroke();

				RectF r = new RectF(Math.min(dragLineFrom.x, dragLineTo.x), Math.min(dragLineFrom.y,
						dragLineTo.y), Math.abs(dragLineFrom.x - dragLineTo.x) + 1, Math.abs(dragLineFrom.y
						- dragLineTo.y) + 1);
				repaint(r.left, r.top, r.width(), r.height());
		}

		public void paint(Canvas g2d) {
			if (dragLine) {
				g2d.drawLine(dragLineFrom.x, dragLineFrom.y, dragLineTo.x, dragLineTo.y, paintHint);
			}
		}
	}

	//
	// Bezier drawing mode
	//

	class CPBezierMode extends CPMode {

		// bezier drawing
		static final int BEZIER_POINTS = 500;
		static final int BEZIER_POINTS_PREVIEW = 100;

		boolean dragBezier = false;
		int dragBezierMode; // 0 Initial drag, 1 first control point, 2 second point
		PointF dragBezierP0, dragBezierP1, dragBezierP2, dragBezierP3;

		public void mousePressed(MotionEvent e) {
			PointF p = coordToDocument(new PointF(e.getX(), e.getY()));
			if (!dragBezier) {
				dragBezier = true;
				dragBezierMode = 0;
				dragBezierP0 = dragBezierP1 = dragBezierP2 = dragBezierP3 = p;
			} else {
				if (dragBezierMode == 1) {
					dragBezierP1 = p;
					repaint(); // FIXME: repaint only the bezier region
				}

				if (dragBezierMode == 2) {
					dragBezierP2 = p;
					repaint(); // FIXME: repaint only the bezier region
				}
			}
		}

		public void mouseDragged(MotionEvent e) {
			PointF p = coordToDocument(new PointF(e.getX(), e.getY()));

			if (dragBezier && dragBezierMode == 0) {
				dragBezierP2 = dragBezierP3 = p;
				repaint();
			}
		}

		public void mouseReleased(MotionEvent e) {
			if (dragBezier) {
				if (dragBezierMode == 0) {
					dragBezierMode = 1;
				} else if (dragBezierMode == 1) {
					dragBezierMode = 2;
				} else if (dragBezierMode == 2) {
					dragBezier = false;

					PointF p0 = dragBezierP0;
					PointF p1 = dragBezierP1;
					PointF p2 = dragBezierP2;
					PointF p3 = dragBezierP3;

					CPBezier bezier = new CPBezier();
					bezier.x0 = p0.x;
					bezier.y0 = p0.y;
					bezier.x1 = p1.x;
					bezier.y1 = p1.y;
					bezier.x2 = p2.x;
					bezier.y2 = p2.y;
					bezier.x3 = p3.x;
					bezier.y3 = p3.y;

					float x[] = new float[BEZIER_POINTS];
					float y[] = new float[BEZIER_POINTS];

					bezier.compute(x, y, BEZIER_POINTS);

					artwork.beginStroke(x[0], y[0], 1);
					for (int i = 1; i < BEZIER_POINTS; i++) {
						artwork.continueStroke(x[i], y[i], 1);
					}
					artwork.endStroke();
					repaint();
				}
			}
		}

		public void paint(Canvas g2d) {
			if (dragBezier) {
				CPBezier bezier = new CPBezier();
				coordToDisplay(dragBezierP0);
				PointF p0 = coordToDisplay(dragBezierP0);
				PointF p1 = coordToDisplay(dragBezierP1);
				PointF p2 = coordToDisplay(dragBezierP2);
				PointF p3 = coordToDisplay(dragBezierP3);

				bezier.x0 = p0.x;
				bezier.y0 = p0.y;
				bezier.x1 = p1.x;
				bezier.y1 = p1.y;
				bezier.x2 = p2.x;
				bezier.y2 = p2.y;
				bezier.x3 = p3.x;
				bezier.y3 = p3.y;

				int x[] = new int[BEZIER_POINTS_PREVIEW];
				int y[] = new int[BEZIER_POINTS_PREVIEW];
				bezier.compute(x, y, BEZIER_POINTS_PREVIEW);
				Path pt = new Path();
				pt.moveTo(x[0], y[0]);
				for(int i = 0; i < BEZIER_POINTS_PREVIEW; i++) {
					pt.lineTo(x[i], y[i]);
				}
				
				g2d.drawPath(pt, paintHint);
				g2d.drawLine(p0.x, p0.y, p1.x, p1.y, paintHint);
				g2d.drawLine(p2.x, p2.y, p3.x, p3.y, paintHint);
			}
		}
	}

	//
	// Color picker mode
	//

	class CPColorPickerMode extends CPMode {

		public void mousePressed(MotionEvent e) {
			PointF p = new PointF(e.getX(), e.getY());
			PointF pf = coordToDocument(p);


			if (artwork.isPointWithin(pf.x, pf.y)) {
				controller.setCurColorRgb(artwork.colorPicker(pf.x, pf.y));
			}
		}

		public void mouseDragged(MotionEvent e) {
			PointF p = new PointF(e.getX(), e.getY());
			PointF pf = coordToDocument(p);

			if (artwork.isPointWithin(pf.x, pf.y)) {
				controller.setCurColorRgb(artwork.colorPicker(pf.x, pf.y));
			}
		}

		public void mouseReleased(MotionEvent e) {
		}
	}

	//
	// Canvas move mode
	//

	class CPMoveCanvasMode extends CPMode {

		boolean dragMiddle = false, dragZoom = false;
		float dragMoveX, dragMoveY, oldDistance, scale, prescale;
		Matrix postTransform;
		PointF dragMoveOffset, midpoint;

		public void mousePressed(MotionEvent e) {
			PointF p = new PointF(e.getX(), e.getY());
			midpoint = new PointF(p.x, p.y);
			dragMiddle = true;
			dragMoveX = p.x;
			dragMoveY = p.y;
			dragMoveOffset = getOffset();
			doTransform();
			postTransform = new Matrix(transform);
			prescale = getZoom();
		}

		public void mouseDragged(MotionEvent e) {
			PointF p = new PointF(e.getX(), e.getY());
			if(dragZoom) {
				scale = spacing(e) / oldDistance;
				midPoint(midpoint, e);
				postTransform.reset();
				postTransform.postTranslate((p.x - dragMoveX) / prescale / scale, (p.y - dragMoveY) / prescale / scale);
				postTransform.postScale(scale, scale, midpoint.x / prescale, midpoint.y / prescale);
				PointF pd = transformBy(postTransform, dragMoveOffset);
				setOffset(pd.x, pd.y);
				setZoom(prescale * scale);
				repaint();
			} else if(dragMiddle) {
				postTransform.reset();
				postTransform.postTranslate((p.x - dragMoveX) / prescale, (p.y - dragMoveY) / prescale);
				PointF pd = transformBy(postTransform, dragMoveOffset);
				setOffset(pd.x, pd.y);
				repaint();
			}
		}

		public void mouseReleased(MotionEvent e) {
			if(dragMiddle) {
				dragMiddle = false;
			}
		}
		
		public void secondMousePressed(MotionEvent e) {
			dragZoom = true;
			oldDistance = spacing(e);
		}
		
		public void secondMouseReleased(MotionEvent e) {
			dragZoom = false;
			dragMiddle = false;
		}
		

		private float spacing(MotionEvent event) {
		   float x = event.getX(0) - event.getX(1);
		   float y = event.getY(0) - event.getY(1);
		   return (float)Math.sqrt(x * x + y * y);
		}
		
		private void midPoint(PointF point, MotionEvent event) {
		   float x = event.getX(0) + event.getX(1);
		   float y = event.getY(0) + event.getY(1);
		   point.set(x / 2, y / 2);
		}

	}

	//
	// Flood fill mode
	//

	class CPFloodFillMode extends CPMode {

		public void mousePressed(MotionEvent e) {
			PointF p = new PointF(e.getX(), e.getY());
			PointF pf = coordToDocument(p);

			if (artwork.isPointWithin(pf.x, pf.y)) {
				artwork.floodFill(pf.x, pf.y);
				repaint();
			}
		}

		public void mouseDragged(MotionEvent e) {
		}

		public void mouseReleased(MotionEvent e) {
		}
	}

	//
	// CPRectSelection mode
	//

	class CPRectSelectionMode extends CPMode {

		PointF firstClick;
		CPRect curRect = new CPRect();

		public void mousePressed(MotionEvent e) {
			PointF p = coordToDocument(new PointF(e.getX(), e.getY()));

			curRect.makeEmpty();
			firstClick = p;

			repaint();
		}

		public void mouseDragged(MotionEvent e) {
			PointF p = coordToDocument(new PointF(e.getX(), e.getY()));
			boolean square = false;
			float squareDist = Math.max(Math.abs(p.x - firstClick.x), Math.abs(p.y - firstClick.y));

			if (p.x >= firstClick.x) {
				curRect.left = (int) firstClick.x;
				curRect.right = (int) (square ? firstClick.x + squareDist : p.x);
			} else {
				curRect.left = (int) (square ? firstClick.x - squareDist : p.x);
				curRect.right = (int) firstClick.x;
			}

			if (p.y >= firstClick.y) {
				curRect.top = (int) firstClick.y;
				curRect.bottom = (int) (square ? firstClick.y + squareDist : p.y);
			} else {
				curRect.top = (int) (square ? firstClick.y - squareDist : p.y);
				curRect.bottom = (int) firstClick.y;
			}

			repaint();
		}

		public void mouseReleased(MotionEvent e) {
			artwork.rectangleSelection(curRect);
			repaint();
		}

		public void paint(Canvas g2d) {
			if (!curRect.isEmpty()) {
				g2d.drawRect(coordToDisplay(curRect), paintHint);
			}
		}
	}

	//
	// CPMoveTool mode
	//

	class CPMoveToolMode extends CPMode {

		PointF firstClick;

		public void mousePressed(MotionEvent e) {
			PointF p = coordToDocument(new PointF(e.getX(), e.getY()));
			firstClick = p;

			artwork.beginPreviewMode(false);

			// FIXME: The following hack avoids a slight display glitch
			// if the whole move tool mess is fixed it probably won't be necessary anymore
			artwork.move(0, 0);
		}

		public void mouseDragged(MotionEvent e) {
			PointF p = coordToDocument(new PointF(e.getX(), e.getY()));
			artwork.move((int)(p.x - firstClick.x), (int) (p.y - firstClick.y));
			repaint();
		}

		public void mouseReleased(MotionEvent e) {
			artwork.endPreviewMode();
			repaint();
		}
	}

	//
	// Canvas rotate mode
	//

	class CPRotateCanvasMode extends CPMode {

		PointF firstClick;
		float initAngle;
		Matrix initTransform;
		boolean dragged;

		public void mousePressed(MotionEvent e) {
			firstClick = new PointF(e.getX(), e.getY());

			initAngle = getRotation2();
			initTransform = new Matrix(transform);

			dragged = false;
		}

		public void mouseDragged(MotionEvent e) {
			dragged = true;

			PointF p = new PointF(e.getX(), e.getY());
			PointF d = getSize();
			PointF center = new PointF(d.x / 2.f, d.y / 2.f);

			float deltaAngle = (float) Math.atan2(p.y - center.y, p.x - center.x)
					- (float) Math.atan2(firstClick.y - center.y, firstClick.x - center.x);

			Matrix rotTrans = new Matrix();
			rotTrans.setRotate(deltaAngle, center.x, center.y);

			rotTrans.postConcat(initTransform);

			setRotation2(initAngle + deltaAngle);
			float[] pts = new float[2];
			pts[0] = firstClick.x;
			pts[1] = firstClick.y;
			rotTrans.mapPoints(pts);
			
			setOffset(pts[0], pts[1]);
			repaint();
		}

		public void mouseReleased(MotionEvent e) {
			if (!dragged) {
				resetRotation();
			}
		}
	}
}

