/*
	ChibiPaint
    Copyright (c) 2006-2008 Marc Schefer

    This file is part of ChibiPaint.

    ChibiPaint is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ChibiPaint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ChibiPaint. If not, see <http://www.gnu.org/licenses/>.

 */

package com.chibipaint;

import idv.jlchntoz.oekakimobile.PaintCanvas;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.chibipaint.engine.*;
import com.chibipaint.util.*;

public abstract class CPController {

	final static String VERSION_STRING = "0.7.11.2";

	private CPColor curColor = new CPColor();
	// int curAlpha = 255;
	// int brushSize = 16;

	// some important object references
	public CPArtwork artwork;
	public PaintCanvas canvas;

	CPBrushInfo[] tools;
	CPBrushInfo curBrush;
	int curMode = M_DRAW;

	private LinkedList<ICPColorListener> colorListeners = new LinkedList<ICPColorListener>();
	private LinkedList<ICPToolListener> toolListeners = new LinkedList<ICPToolListener>();
	private LinkedList<ICPModeListener> modeListeners = new LinkedList<ICPModeListener>();
	private LinkedList<ICPViewListener> viewListeners = new LinkedList<ICPViewListener>();
	private LinkedList<ICPEventListener> cpEventListeners = new LinkedList<ICPEventListener>();

	//
	// Definition of all the standard tools available
	//

	public static final int T_PENCIL = 0;
	public static final int T_ERASER = 1;
	public static final int T_PEN = 2;
	public static final int T_SOFTERASER = 3;
	public static final int T_AIRBRUSH = 4;
	public static final int T_DODGE = 5;
	public static final int T_BURN = 6;
	public static final int T_WATER = 7;
	public static final int T_BLUR = 8;
	public static final int T_SMUDGE = 9;
	public static final int T_BLENDER = 10;
	public static final int T_MAX = 11;

	//
	// Definition of all the modes available
	//

	public static final int M_DRAW = 0;
	public static final int M_FLOODFILL = 1;
	public static final int M_RECT_SELECTION = 2;
	public static final int M_MOVE_TOOL = 3;
	public static final int M_ROTATE_CANVAS = 4;
	public static final int M_COLOR_PICKER = 5;
	public static final int M_MOVE_CANVAS = 6;
	public static final int M_MAX = 7;

	// Image loader cache
	private Map<String, Bitmap> imageCache = new HashMap<String, Bitmap>();

	public interface ICPColorListener {

		public void newColor(CPColor color);
	}

	public interface ICPToolListener {

		public void newTool(int tool, CPBrushInfo toolInfo);
	}

	public interface ICPModeListener {

		public void modeChange(int mode);
	}

	public interface ICPViewListener {

		public void viewChange(CPViewInfo viewInfo);
	}

	public interface ICPEventListener {

		public void cpEvent();
	}

	public static class CPViewInfo {

		public float zoom;
		public int offsetX, offsetY;
	}

	public CPController() {
		tools = new CPBrushInfo[T_MAX];
		tools[T_PENCIL] = new CPBrushInfo(T_PENCIL, 16, 255, true, false, .5f,
				.05f, false, true, CPBrushInfo.B_ROUND_AA, CPBrushInfo.M_PAINT,
				1f, 0f);
		tools[T_ERASER] = new CPBrushInfo(T_ERASER, 16, 255, true, false, .5f,
				.05f, false, false, CPBrushInfo.B_ROUND_AA,
				CPBrushInfo.M_ERASE, 1f, 0f);
		tools[T_PEN] = new CPBrushInfo(T_PEN, 2, 128, true, false, .5f, .05f,
				true, false, CPBrushInfo.B_ROUND_AA, CPBrushInfo.M_PAINT, 1f,
				0f);
		tools[T_SOFTERASER] = new CPBrushInfo(T_SOFTERASER, 16, 64, false,
				true, .5f, .05f, false, true, CPBrushInfo.B_ROUND_AIRBRUSH,
				CPBrushInfo.M_ERASE, 1f, 0f);
		tools[T_AIRBRUSH] = new CPBrushInfo(T_AIRBRUSH, 50, 32, false, true,
				.5f, .05f, false, true, CPBrushInfo.B_ROUND_AIRBRUSH,
				CPBrushInfo.M_PAINT, 1f, 0f);
		tools[T_DODGE] = new CPBrushInfo(T_DODGE, 30, 32, false, true, .5f,
				.05f, false, true, CPBrushInfo.B_ROUND_AIRBRUSH,
				CPBrushInfo.M_DODGE, 1f, 0f);
		tools[T_BURN] = new CPBrushInfo(T_BURN, 30, 32, false, true, .5f, .05f,
				false, true, CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_BURN,
				1f, 0f);
		tools[T_WATER] = new CPBrushInfo(T_WATER, 30, 70, false, true, .5f,
				.02f, false, true, CPBrushInfo.B_ROUND_AA, CPBrushInfo.M_WATER,
				.3f, .6f);
		tools[T_BLUR] = new CPBrushInfo(T_BLUR, 20, 255, false, true, .5f,
				.05f, false, true, CPBrushInfo.B_ROUND_PIXEL,
				CPBrushInfo.M_BLUR, 1f, 0f);
		tools[T_SMUDGE] = new CPBrushInfo(T_SMUDGE, 20, 128, false, true, .5f,
				.01f, false, true, CPBrushInfo.B_ROUND_AIRBRUSH,
				CPBrushInfo.M_SMUDGE, 0f, 1f);
		tools[T_BLENDER] = new CPBrushInfo(T_SMUDGE, 20, 60, false, true, .5f,
				.1f, false, true, CPBrushInfo.B_ROUND_AIRBRUSH,
				CPBrushInfo.M_OIL, 0f, .07f);
		curBrush = tools[T_PENCIL];
	}

	public void setArtwork(CPArtwork artwork) {
		this.artwork = artwork;
	}

	public void setCanvas(PaintCanvas canvas) {
		this.canvas = canvas;
	}

	public void setCurColor(CPColor color) {
		if (!curColor.isEqual(color)) {
			artwork.setForegroundColor(color.getRgb());

			curColor.copyFrom(color);
			for (Object l : colorListeners) {
				((ICPColorListener) l).newColor(color);
			}
		}
	}

	public CPColor getCurColor() {
		return (CPColor) curColor.clone();
	}

	public int getCurColorRgb() {
		return curColor.getRgb();
	}

	public void setCurColorRgb(int color) {
		CPColor col = new CPColor(color);
		setCurColor(col);
	}

	public void setBrushSize(int size) {
		curBrush.size = Math.max(1, Math.min(200, size));
		callToolListeners();
	}

	public int getBrushSize() {
		return curBrush.size;
	}

	public void setAlpha(int alpha) {
		curBrush.alpha = alpha;
		callToolListeners();
	}

	public int getAlpha() {
		return curBrush.alpha;
	}

	public void setTool(int tool) {
		setMode(M_DRAW);
		curBrush = tools[tool];
		artwork.setBrush(curBrush);
		callToolListeners();
	}

	public void setTool(CPBrushInfo tool) {
		setMode(M_DRAW);
		curBrush = tool;
		artwork.setBrush(tool);
		callToolListeners();
	}

	public CPBrushInfo getBrushInfo() {
		return curBrush;
	}

	public void setMode(int mode) {
		curMode = mode;
		callModeListeners();
	}

	public void addColorListener(ICPColorListener listener) {
		colorListeners.addLast(listener);
	}

	public void addToolListener(ICPToolListener listener) {
		toolListeners.addLast(listener);
	}

	public void callToolListeners() {
		for (ICPToolListener l : toolListeners) {
			l.newTool(curBrush.toolNb, curBrush);
		}
	}

	public void addModeListener(ICPModeListener listener) {
		modeListeners.addLast(listener);
	}

	public void callModeListeners() {
		for (ICPModeListener l : modeListeners) {
			l.modeChange(curMode);
		}
	}

	public void addViewListener(ICPViewListener listener) {
		viewListeners.addLast(listener);
	}

	public void callViewListeners(CPViewInfo info) {
		for (ICPViewListener l : viewListeners) {
			l.viewChange(info);
		}
	}

	public void addCPEventListener(ICPEventListener listener) {
		cpEventListeners.addLast(listener);
	}

	public void callCPEventListeners() {
		for (ICPEventListener l : cpEventListeners) {
			l.cpEvent();
		}
	}

	byte[] getPngData(Bitmap img) {
		Bitmap.Config imageType = Bitmap.Config.ARGB_8888;

		Bitmap bi = Bitmap.createBitmap(img.getWidth(), img.getHeight(),
				imageType);

		ByteArrayOutputStream pngFileStream = new ByteArrayOutputStream(1024);

		bi.compress(CompressFormat.PNG, 100, pngFileStream);

		byte[] pngData = pngFileStream.toByteArray();

		return pngData;
	}

	public Bitmap loadImage(String imageName) {
		Bitmap img = imageCache.get(imageName);
		if (img == null) {
			try {
				ClassLoader loader = getClass().getClassLoader();
				@SuppressWarnings({ "unused", "rawtypes" })
				Class[] classes = { Bitmap.class };

				URL url = loader.getResource("images/" + imageName);
				img = BitmapFactory.decodeFile(url.toString());
			} catch (Throwable t) {
			}
			imageCache.put(imageName, img);
		}
		return img;
	}

	public CPArtwork getArtwork() {
		return artwork;
	}

	public boolean isRunningAsApplet() {
		return false;
	}
}
