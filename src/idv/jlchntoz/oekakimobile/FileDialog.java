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

import java.io.*;
import java.util.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class FileDialog implements OnItemClickListener {
	public interface FileDialogCallBack {
		public void onCallBack(FileDialog which, File file);
	}
	
	private final Context context;
	private final FileDialogCallBack callBack;
	private final View DialogView;
	private final EditText tvfiles;
	private final ListView lvwfiles;
	private final boolean isOpenDialog;
	private final String[] extensionFilter;
	private ArrayAdapter<String> aafileList;
	private ArrayList<String> fileNameList;
	private ArrayList<File> fileList;
	private AlertDialog dialog;
	private String prompt, path, name;
	private File path_file;
	private int selitem;
	
	public FileDialog(Context context, String prompt, String basepath, String filename, String[] extensionFilter, boolean isOpenDialog, FileDialogCallBack Callback) {
		this.callBack = Callback;
		this.context = context;
		this.prompt = prompt;
		this.path = basepath;
		this.name = filename;
		this.isOpenDialog = isOpenDialog;
		this.extensionFilter = extensionFilter;
		
		this.DialogView = LayoutInflater.from(context).inflate(R.layout.filedlg, null);
		this.tvfiles = (EditText)this.DialogView.findViewById(R.id.etfilename);
		this.lvwfiles = (ListView)this.DialogView.findViewById(R.id.lvwfiles);
		
		if(this.isOpenDialog)
			this.tvfiles.setVisibility(View.GONE);
		else
			this.tvfiles.setText(filename);
		
		buildAdapter();
		buildDialog();
	}
	
	private void buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(prompt);
		builder.setView(DialogView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	doCallback();
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
	
	private void buildAdapter() {
		path_file = new File(path);
		path_file.mkdirs();
		File[] files = path_file.listFiles();
		fileNameList = new ArrayList<String>();
		fileList = new ArrayList<File>();
		if(files != null)
			for(File file : files) {
				if(file.isFile()) {
					if(extensionFilter != null && extensionFilter.length > 0) {
						for(String ext : extensionFilter)
							if(file.getName().endsWith("." + ext)) {
								fileList.add(file);
								fileNameList.add(file.getName());
							}
					} else {
						fileList.add(file);
						fileNameList.add(file.getName());
					}
				}
			}
		aafileList = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, fileNameList);
		lvwfiles.setAdapter(aafileList);
		lvwfiles.setOnItemClickListener(this);
		selitem = 0;
	}
	
	public void showDialog() {
		dialog.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long i) {
		selitem = pos;
		tvfiles.setText(fileList.get(selitem).getName());
		doCallback();
	}
	
	private void doCallback() {
    	name = tvfiles.getText().toString();
    	File ret = new File(path, name);
    	if(isOpenDialog && !ret.exists())
    		Toast.makeText(context, String.format(context.getString(R.string.filenotexists), ret.getName()), Toast.LENGTH_LONG).show();
    	callBack.onCallBack(FileDialog.this, ret);
	}

}
