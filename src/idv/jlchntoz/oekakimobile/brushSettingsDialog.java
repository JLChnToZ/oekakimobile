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

import com.chibipaint.CPController;
import com.chibipaint.engine.CPBrushInfo;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.view.*;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class brushSettingsDialog implements OnSeekBarChangeListener,
		CPController.ICPToolListener {

	CPBrushInfo cinfo;
	final CPController ctrl;
	final AlertDialog dlg;
	final View AlertDialogView;
	final Spinner sptoolinfo, sptooltype;
	final SeekBar sbstrokeSize, sbalpha, sbcolor, sbmix, sbspecing, sbscattering,
			sbsmoothing;
	final TextView lbstrokeSize, lbalpha, lbcolor, lbmix, lbspecing,
			lbscattering, lbsmoothing;
	final EditText TBName;
	final CheckBox cbstrokeSize, cbalpha, cbscattering;
	final Context context;
	boolean updateLock;

	public brushSettingsDialog(Context context, CPController ctrl) {
		this.context = context;
		this.ctrl = ctrl;
		cinfo = ctrl.getBrushInfo();
		updateLock = false;

		AlertDialogView = LayoutInflater.from(context).inflate(
				R.layout.brushsettings, null);

		TBName = (EditText) AlertDialogView.findViewById(R.id.TBToolName);

		sptoolinfo = (Spinner) AlertDialogView.findViewById(R.id.SPToolInfo);
		sptooltype = (Spinner) AlertDialogView.findViewById(R.id.SPToolType);

		sbstrokeSize = (SeekBar) AlertDialogView.findViewById(R.id.SBStrokeWidth);
		sbalpha = (SeekBar) AlertDialogView.findViewById(R.id.SBAlpha);
		sbcolor = (SeekBar) AlertDialogView.findViewById(R.id.SBColor);
		sbmix = (SeekBar) AlertDialogView.findViewById(R.id.SBMix);
		sbspecing = (SeekBar) AlertDialogView.findViewById(R.id.SBSpecing);
		sbscattering = (SeekBar) AlertDialogView.findViewById(R.id.SBScattering);
		sbsmoothing = (SeekBar) AlertDialogView.findViewById(R.id.SBSmoothing);

		lbstrokeSize = (TextView) AlertDialogView.findViewById(R.id.LblStrokeWidth);
		lbalpha = (TextView) AlertDialogView.findViewById(R.id.LblAlpha);
		lbcolor = (TextView) AlertDialogView.findViewById(R.id.LblColor);
		lbmix = (TextView) AlertDialogView.findViewById(R.id.LblMix);
		lbspecing = (TextView) AlertDialogView.findViewById(R.id.LblSpecing);
		lbscattering = (TextView) AlertDialogView.findViewById(R.id.LblScattering);
		lbsmoothing = (TextView) AlertDialogView.findViewById(R.id.LblSmoothing);

		cbstrokeSize = (CheckBox) AlertDialogView
				.findViewById(R.id.CBPressureStroke);
		cbalpha = (CheckBox) AlertDialogView.findViewById(R.id.CBPressureAlpha);
		cbscattering = (CheckBox) AlertDialogView
				.findViewById(R.id.CBPressureScattering);

		ArrayAdapter<CharSequence> aatoolinfo = ArrayAdapter.createFromResource(
				context, R.array.tipNames, android.R.layout.simple_spinner_item);
		aatoolinfo
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sptoolinfo.setAdapter(aatoolinfo);

		ArrayAdapter<CharSequence> aatooltype = ArrayAdapter.createFromResource(
				context, R.array.toolTypes, android.R.layout.simple_spinner_item);
		aatooltype
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sptooltype.setAdapter(aatooltype);

		sbstrokeSize.setOnSeekBarChangeListener(this);
		sbstrokeSize.setMax(200);

		sbalpha.setOnSeekBarChangeListener(this);
		sbalpha.setMax(255);

		sbcolor.setOnSeekBarChangeListener(this);
		sbcolor.setMax(100);

		sbmix.setOnSeekBarChangeListener(this);
		sbmix.setMax(100);

		sbspecing.setOnSeekBarChangeListener(this);
		sbspecing.setMax(100);

		sbscattering.setOnSeekBarChangeListener(this);
		sbscattering.setMax(1000);

		sbsmoothing.setOnSeekBarChangeListener(this);
		sbsmoothing.setMax(100);

		dlg = new AlertDialog.Builder(context).setTitle(R.string.brushsettings)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						updateValue(false);
					}
				}).setNeutralButton(R.string.saveas, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						updateValue(true);
					}
				}).setNegativeButton(android.R.string.cancel, null).create();

		dlg.setView(AlertDialogView);

		this.ctrl.addToolListener(this);
		ctrl.callToolListeners();
	}

	public void showDialog() {
		ctrl.callToolListeners();
		dlg.show();
	}

	@Override
	public void onProgressChanged(SeekBar sb, int p, boolean u) {
		if (sb == sbstrokeSize) {
			lbstrokeSize.setText(R.string.strokesize);
			lbstrokeSize.append(": " + p);
		} else if (sb == sbalpha) {
			lbalpha.setText(R.string.alpha);
			lbalpha.append(": " + p);
		} else if (sb == sbcolor) {
			lbcolor.setText(R.string.color);
			lbcolor.append(": " + p + "%");
		} else if (sb == sbmix) {
			lbmix.setText(R.string.mix);
			lbmix.append(": " + p + "%");
		} else if (sb == sbspecing) {
			lbspecing.setText(R.string.specing);
			lbspecing.append(": " + p + "%");
		} else if (sb == sbscattering) {
			lbscattering.setText(R.string.scattering);
			lbscattering.append(": " + p + "%");
		} else if (sb == sbsmoothing) {
			lbsmoothing.setText(R.string.smoothing);
			lbsmoothing.append(": " + p + "%");
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Nothing to do here.
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Nothing to do here.
	}

	@Override
	public void newTool(int tool, CPBrushInfo toolInfo) {
		if (updateLock)
			return;
		if (cinfo != toolInfo)
			cinfo = toolInfo;
		TBName.setText(cinfo.getName());
		sptoolinfo.setSelection(toolInfo.type);
		sptooltype.setSelection(toolInfo.paintMode);
		sbalpha.setProgress(toolInfo.alpha);
		sbstrokeSize.setProgress(toolInfo.size);
		sbcolor.setProgress(Math.round(toolInfo.resat * 100));
		sbmix.setProgress(Math.round(toolInfo.bleed * 100));
		sbspecing.setProgress(Math.round(toolInfo.spacing * 100));
		sbscattering.setProgress(Math.round(toolInfo.scattering * 100));
		sbsmoothing.setProgress(Math.round(toolInfo.smoothing * 100));
		cbalpha.setChecked(toolInfo.pressureAlpha);
		cbstrokeSize.setChecked(toolInfo.pressureSize);
		cbscattering.setChecked(toolInfo.pressureScattering);
	}

	private void updateValue(boolean clone) {
		updateLock = true;
		if (clone) {
			cinfo = ctrl.getBrushInfo().clone(-1);
			ctrl.setTool(cinfo);
		} else
			cinfo = ctrl.getBrushInfo();
		cinfo.setName(TBName.getText().toString());
		ctrl.setBrushSize(sbstrokeSize.getProgress());
		ctrl.setAlpha(sbalpha.getProgress());
		cinfo.type = sptoolinfo.getSelectedItemPosition();
		cinfo.paintMode = sptooltype.getSelectedItemPosition();
		cinfo.resat = sbcolor.getProgress() / 100F;
		cinfo.spacing = sbspecing.getProgress() / 100F;
		cinfo.scattering = sbscattering.getProgress() / 100F;
		cinfo.smoothing = sbsmoothing.getProgress() / 100F;
		cinfo.pressureSize = cbstrokeSize.isChecked();
		cinfo.pressureAlpha = cbalpha.isChecked();
		cinfo.pressureScattering = cbscattering.isChecked();
		updateLock = false;
		((MainActivity) context).getSettings().saveCustomPen(cinfo);
		ctrl.callToolListeners();
	}
}
