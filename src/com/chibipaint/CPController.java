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
			for (Object l : colorListeners)
				((ICPColorListener) l).newColor(color);
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
		for (ICPToolListener l : toolListeners)
			l.newTool(curBrush.toolNb, curBrush);
	}

	public void addModeListener(ICPModeListener listener) {
		modeListeners.addLast(listener);
	}

	public void callModeListeners() {
		for (ICPModeListener l : modeListeners)
			l.modeChange(curMode);
	}

	public void addViewListener(ICPViewListener listener) {
		viewListeners.addLast(listener);
	}

	public void callViewListeners(CPViewInfo info) {
		for (ICPViewListener l : viewListeners)
			l.viewChange(info);
	}

	public void addCPEventListener(ICPEventListener listener) {
		cpEventListeners.addLast(listener);
	}

	public void callCPEventListeners() {
		for (ICPEventListener l : cpEventListeners)
			l.cpEvent();
	}

	byte[] getPngData(Bitmap img) {
		Bitmap.Config imageType = Bitmap.Config.ARGB_8888;

		Bitmap bi = Bitmap.createBitmap(img.getWidth(), img.getHeight(), imageType);

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
