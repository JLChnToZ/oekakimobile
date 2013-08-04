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

import com.chibipaint.CPController;
import com.chibipaint.engine.*;
import com.chibipaint.util.CPColor;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorTextureDrawerHandler implements OnSeekBarChangeListener, OnCheckedChangeListener,
android.view.View.OnClickListener, OnItemSelectedListener, CPController.ICPColorListener, ColorPaletteSelector.OnSelectedListener {

	public final CPController controller;
	public final View drawerView;
	public final Context context;
	private final Spinner sptextures;
	private final ImageView imvpreview;
	private final CheckBox cbinvert, cbmirror;
	private final SeekBar sbbrightness, sbcontrast;
	private final TextView tvbrightness, tvcontrast;
	private final Button btnreset, btncolor;
	private final ColorPaletteSelector CPS;
	private List<HashMap<String, ?>> sptexturContent;
	private HashMapTextIconAdapter HMTIA;
	private ArrayList<CPGreyBmp> textures;
	private CPGreyBmp selectedTexture, processedTexture;
	private Bitmap previewBMP;
	private int targetRGB;
	
	public ColorTextureDrawerHandler(Context context, CPController controller, View drawerView) {
		this.context = context;
		this.controller = controller;
		this.drawerView = drawerView;

		sptextures = (Spinner)this.drawerView.findViewById(R.id.sptextures);
		imvpreview = (ImageView)this.drawerView.findViewById(R.id.imvpreview);
		cbinvert = (CheckBox)this.drawerView.findViewById(R.id.cbinvert);
		cbmirror = (CheckBox)this.drawerView.findViewById(R.id.cbmirror);
		sbbrightness = (SeekBar)this.drawerView.findViewById(R.id.sbbrightness);
		sbcontrast = (SeekBar)this.drawerView.findViewById(R.id.sbcontrast);
		tvbrightness = (TextView)this.drawerView.findViewById(R.id.tvbrightness);
		tvcontrast = (TextView)this.drawerView.findViewById(R.id.tvcontrast);
		btnreset = (Button)this.drawerView.findViewById(R.id.btnreset);
		btncolor = (Button)this.drawerView.findViewById(R.id.btnsetcolor);
		CPS = (ColorPaletteSelector)this.drawerView.findViewById(R.id.CPS);
		
		Integer[] paletteColors = ((MainActivity)context).getSettings().getColors();
		if(paletteColors != null)
			for(int i = 0; i < paletteColors.length; i++)
				if(paletteColors[i] != null && paletteColors[i] != Color.TRANSPARENT)
					CPS.setColor(i, paletteColors[i]);
		
		textures = new ArrayList<CPGreyBmp>();
		createTextures();
		
		sptexturContent = new ArrayList<HashMap<String, ?>>();
		
		for(CPGreyBmp BMP : textures) {
			HashMap<String, Object> itm = new HashMap<String, Object>();
			itm.put("name", "");
			itm.put("icon", TextureFactory.createTextureImage(BMP, 64, 64, Color.WHITE));
			sptexturContent.add(itm);
		}
		HMTIA = new HashMapTextIconAdapter(this.context, sptexturContent,
				R.layout.imagespinneritemlayout,
				new String[] {"name", "icon"},
				new int[] { R.id.spinnertext, R.id.spinnerimage });
		sptextures.setAdapter(HMTIA);
		
		sbbrightness.setMax(200);
		sbcontrast.setMax(200);
		sbbrightness.setProgress(100);
		sbcontrast.setProgress(100);
		
		sbbrightness.setOnSeekBarChangeListener(this);
		sbcontrast.setOnSeekBarChangeListener(this);
		cbinvert.setOnCheckedChangeListener(this);
		cbmirror.setOnCheckedChangeListener(this);
		btnreset.setOnClickListener(this);
		btncolor.setOnClickListener(this);
		sptextures.setOnItemSelectedListener(this);
		CPS.setListener(this);
		
		this.controller.addColorListener(this);
		
		targetRGB = controller.getCurColorRgb();
	}
	
	private void createTextures() {
		CPGreyBmp texture = new CPGreyBmp(1, 1);
		texture.data[0] = (byte)0;
		textures.add(texture);
		textures.add(TextureFactory.makeDotTexture(2));
		textures.add(TextureFactory.makeDotTexture(3));
		textures.add(TextureFactory.makeDotTexture(4));
		textures.add(TextureFactory.makeDotTexture(6));
		textures.add(TextureFactory.makeDotTexture(8));
		textures.add(TextureFactory.makeVertLinesTexture(1, 2));
		textures.add(TextureFactory.makeVertLinesTexture(2, 4));
		textures.add(TextureFactory.makeHorizLinesTexture(1, 2));
		textures.add(TextureFactory.makeHorizLinesTexture(2, 4));
		textures.add(TextureFactory.makeCheckerBoardTexture(1));
		textures.add(TextureFactory.makeCheckerBoardTexture(2));
		textures.add(TextureFactory.makeCheckerBoardTexture(4));
		textures.add(TextureFactory.makeCheckerBoardTexture(8));
		textures.add(TextureFactory.makeCheckerBoardTexture(16));
	}

	public void update() {
		if(sptextures.getSelectedItemPosition() != 0)
			selectedTexture = textures.get(sptextures.getSelectedItemPosition());
		else 
			selectedTexture = null;
		
		if (selectedTexture != null) {
			processedTexture = new CPGreyBmp(selectedTexture);
			
			if (cbmirror.isChecked())
				processedTexture.mirrorHorizontally();

			CPLookUpTable lut = new CPLookUpTable(sbbrightness.getProgress() / 200F - 1F,
					sbcontrast.getProgress() / 200F - 1F);
			
			if (cbinvert.isChecked())
				lut.inverse();
			
			processedTexture.applyLUT(lut);
		} else
			processedTexture = null;

		this.controller.artwork.brushManager.setTexture(processedTexture);
		
		previewBMP = TextureFactory.createTextureImage(processedTexture, previewBMP, 64, 64, targetRGB);
		imvpreview.setImageBitmap(previewBMP);
		
		this.controller.setCurColor(this.controller.getCurColor());
	}
	
	public Bitmap getBitmap() {
		update();
		return previewBMP;
	}
	
	@Override
	public void onClick(View view) {
		if(view == btnreset) {
			cbmirror.setChecked(false);
			cbinvert.setChecked(false);
			sbbrightness.setProgress(100);
			sbcontrast.setProgress(100);
			update();
		} else if(view == btncolor) {
			ColorPickerDialog dlg = new ColorPickerDialog(this.context, targetRGB, new ColorPickerDialog.ColorPickerCallback() {
				@Override
				public void onCallback(ColorPickerDialog which, int color) {
					controller.setCurColorRgb(color);
					update();
				}
			});
			dlg.getDialog().show();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton target, boolean user) {
		update();
	}

	@Override
	public void onProgressChanged(SeekBar target, int amount, boolean user) {
		if(target == sbbrightness) {
			tvbrightness.setText(R.string.brightness);
			tvbrightness.append(": "+(amount-100)+"%");
		} else if(target == sbcontrast) {
			tvcontrast.setText(R.string.contrast);
			tvcontrast.append(": "+(amount-100)+"%");
		}
		update();
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekbar) { }

	@Override
	public void onStopTrackingTouch(SeekBar seekbar) { }

	@Override
	public void onItemSelected(AdapterView<?> a, View v, int i, long d) {
		update();
	}

	@Override
	public void onNothingSelected(AdapterView<?> a) { }

	@Override
	public void newColor(CPColor color) {
		targetRGB = color.rgb;
		int r = Color.red(targetRGB), g = Color.green(targetRGB), b = Color.blue(targetRGB);
		btncolor.setBackgroundColor(0xFF << 24 | targetRGB);
		btncolor.setTextColor((r + g * 2 + b) / 3 > 128 ? Color.BLACK : Color.WHITE);
	}

	@Override
	public void OnSelected(int index, int color) {
		controller.setCurColorRgb(color);
		update();
	}

	@Override
	public int OnReplace(int index) {
		((MainActivity)context).getSettings().saveColor(index, 0xFF << 24 |targetRGB);
		return 0xFF << 24 | targetRGB;
	}


	public final static class TextureFactory {
		public static CPGreyBmp makeDotTexture(int size) {
			CPGreyBmp texture = new CPGreyBmp(size, size);
			for(int i = 1; i < size * size; i++)
				texture.data[i] = (byte) 0xFF;
			return texture;
		}

		public static CPGreyBmp makeCheckerBoardTexture(int size) {
			int textureSize = 2*size;
			CPGreyBmp texture = new CPGreyBmp(textureSize, textureSize);
			for(int i = 0; i < textureSize; i++)
				for(int j = 0; j < textureSize; j++)
					texture.data[i + j * textureSize] = (((i / size) + (j / size)) % 2 == 0) ? (byte) 0 : (byte) 0xFF;
			return texture;
		}

		public static CPGreyBmp makeVertLinesTexture(int lineSize, int size) {
			CPGreyBmp texture = new CPGreyBmp(size, size);
			for(int i = 0; i < size * size; i++)
				if(i % size >= lineSize)
					texture.data[i] = (byte) 0xFF;
			return texture;
		}

		public static CPGreyBmp makeHorizLinesTexture(int lineSize, int size) {
			CPGreyBmp texture = new CPGreyBmp(size, size);
			for(int i = 0; i < size * size; i++)
				if(i / size >= lineSize)
					texture.data[i] = (byte) 0xFF;
			return texture;
		}
		
		public static Bitmap createTextureImage(CPGreyBmp texture, int width, int height, int color) {
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			alterTextureImage(texture, bmp, color);
			return bmp;
		}
		
		public static Bitmap createTextureImage(CPGreyBmp texture, Bitmap bitmap, int width, int height, int color) {
			if(bitmap == null)
				return createTextureImage(texture, width, height, color);
			alterTextureImage(texture, bitmap, color);
			return bitmap;
		}
		
		public static void alterTextureImage(CPGreyBmp texture, Bitmap bitmap, int color) {
			if(bitmap == null) return;
			if(texture == null) {
				texture = new CPGreyBmp(1, 1);
				texture.data[0] = (byte)0x00;
			}
			int red = Color.red(color), green = Color.green(color), blue = Color.blue(color);
			int width = bitmap.getWidth(), height = bitmap.getHeight();
			int[] buffer = new int[width * height];
			for (int i = 0; i < width * height; i++) {
				buffer[i] = ~texture.data[texture.getWidth() * (i / width % texture.getHeight()) + (i % width % texture.getWidth())];
				buffer[i] = (buffer[i] & 0xff) << 24 | red << 16 | green << 8 | blue;
			}
			bitmap.setPixels(buffer, 0, width, 0, 0, width, height);
		}
		
	}
}
