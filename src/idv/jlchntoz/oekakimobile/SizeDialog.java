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

public class SizeDialog {
	public interface SizeDialogCallBack {
		public void onCallBack(SizeDialog which, int width, int height);
	}

	private final Context context;
	private final SizeDialogCallBack callBack;
	private final View DialogView;
	private final EditText tvwidth, tvheight;
	private AlertDialog dialog;
	private String prompt;
	private int w, h;

	public SizeDialog(Context context, String prompt, int width, int height,
			SizeDialogCallBack Callback) {
		this.callBack = Callback;
		this.context = context;
		this.prompt = prompt;
		this.w = width;
		this.h = height;

		this.DialogView = LayoutInflater.from(context).inflate(
				R.layout.sizedialog, null);
		this.tvwidth = (EditText) this.DialogView
				.findViewById(R.id.sizedlgwidth);
		this.tvheight = (EditText) this.DialogView
				.findViewById(R.id.sizedlgheight);

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
						callBack.onCallBack(SizeDialog.this, w, h);
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
