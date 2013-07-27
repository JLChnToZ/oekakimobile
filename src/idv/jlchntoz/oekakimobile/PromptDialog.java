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
import android.text.InputType;
import android.widget.EditText;

public class PromptDialog {
	public interface PromptDialogCallBack {
		public void onCallBack(PromptDialog which, String result);
	}
	
	private final Context context;
	private final PromptDialogCallBack callBack;
	private EditText input;
	private AlertDialog dialog;
	private String prompt, value;
	
	public PromptDialog(Context context, String prompt, String defaultValue, PromptDialogCallBack Callback) {
		this.callBack = Callback;
		this.context = context;
		this.prompt = prompt;
		this.value = defaultValue;
		
		buildDialog();
	}
	
	private void buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(prompt);
		
		if(input == null) {
			input = new EditText(context);
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			input.setText(value);
		}
		
		builder.setView(input);
		
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	callBack.onCallBack(PromptDialog.this, input.getText().toString());
		    }
		});
		
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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
