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
import android.text.Html;
import android.view.*;
import android.widget.TextView;

public class AboutBox {
	private final Context context;
	private final View AboutView;
	private AlertDialog dialog;

	public AboutBox(Context context) {
		this.context = context;
		AboutView = LayoutInflater.from(context).inflate(R.layout.aboutbox, null);
		buildDialog();
	}

	private void buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setPositiveButton(android.R.string.ok, null);
		builder.setView(AboutView);
		TextView lblNotice = (TextView) AboutView.findViewById(R.id.lblAboutNotice);
		lblNotice.setText(Html.fromHtml(lblNotice.getText().toString()));
		lblNotice.setSelected(true);

		dialog = builder.create();
	}

	public void showDialog() {
		dialog.show();
	}

}
