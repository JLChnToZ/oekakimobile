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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ColorPickerDialog {
	public interface ColorPickerCallback {
		public void onCallback(ColorPickerDialog which, int color);
	}

	private final Context context;
	private final ColorPickerCallback callback;
	private ColorPickerView picker;
	private AlertDialog dialog;
	private int color;

	public ColorPickerDialog(Context context, int color,
			ColorPickerCallback callback) {
		this.context = context;
		this.color = color;
		this.callback = callback;

		buildDialog();
	}

	public void buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		picker = new ColorPickerView(context);
		picker.setColor(color);
		builder.setView(picker);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						callback.onCallback(ColorPickerDialog.this,
								picker.getColor());
					}
				});

		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		dialog = builder.create();
	}

	public AlertDialog getDialog() {
		return dialog;
	}

}
