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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class BlurDialog {
	public interface BlurDialogCallBack {
		public void onCallBack(BlurDialog which, int width, int height,
				int iterations);
	}

	private final Context context;
	private final BlurDialogCallBack callBack;
	private final View DialogView;
	private final EditText tvwidth, tvheight, tviterations;
	private AlertDialog dialog;
	private String prompt;
	private int w, h, i;

	public BlurDialog(Context context, String prompt, int width, int height,
			int iterations, BlurDialogCallBack Callback) {
		this.callBack = Callback;
		this.context = context;
		this.prompt = prompt;
		this.w = width;
		this.h = height;
		this.i = iterations;

		this.DialogView = LayoutInflater.from(context).inflate(
				R.layout.blurdialog, null);
		this.tvwidth = (EditText) this.DialogView
				.findViewById(R.id.blurdlgwidth);
		this.tvheight = (EditText) this.DialogView
				.findViewById(R.id.blurdlgheight);
		this.tviterations = (EditText) this.DialogView
				.findViewById(R.id.blurdlgitr);

		buildDialog();
	}

	private void buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(prompt);
		builder.setView(DialogView);

		tvwidth.setText("" + w);
		tvheight.setText("" + h);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						w = Integer.parseInt(tvwidth.getText().toString());
						h = Integer.parseInt(tvheight.getText().toString());
						i = Integer.parseInt(tviterations.getText().toString());
						callBack.onCallBack(BlurDialog.this, w, h, i);
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

	public void showDialog() {
		dialog.show();
	}

}
