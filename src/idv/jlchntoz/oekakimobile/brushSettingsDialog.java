package idv.jlchntoz.oekakimobile;

import com.chibipaint.CPController;
import com.chibipaint.engine.CPBrushInfo;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.view.*;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class brushSettingsDialog implements OnSeekBarChangeListener, CPController.ICPToolListener {
	
	CPBrushInfo cinfo;
	final CPController ctrl;
	final AlertDialog dlg;
	final View AlertDialogView;
	final Spinner sptoolinfo;
	final SeekBar sbstrokeSize, sbalpha, sbcolor, sbmix, sbspecing, sbscattering, sbsmoothing;
	final TextView lbstrokeSize, lbalpha, lbcolor, lbmix, lbspecing, lbscattering, lbsmoothing;
	final CheckBox cbstrokeSize, cbalpha, cbscattering;
	boolean updateLock;
	
	
	public brushSettingsDialog(Context context, CPController ctrl) {
		this.ctrl = ctrl;
		this.cinfo = ctrl.getBrushInfo();
		this.updateLock = false;
		
		AlertDialogView = LayoutInflater.from(context).inflate(R.layout.brushsettings, null);
		
		sptoolinfo = (Spinner)AlertDialogView.findViewById(R.id.SPToolInfo);
		
		sbstrokeSize = (SeekBar)AlertDialogView.findViewById(R.id.SBStrokeWidth);
		sbalpha = (SeekBar)AlertDialogView.findViewById(R.id.SBAlpha);
		sbcolor = (SeekBar)AlertDialogView.findViewById(R.id.SBColor);
		sbmix = (SeekBar)AlertDialogView.findViewById(R.id.SBMix);
		sbspecing = (SeekBar)AlertDialogView.findViewById(R.id.SBSpecing);
		sbscattering = (SeekBar)AlertDialogView.findViewById(R.id.SBScattering);
		sbsmoothing = (SeekBar)AlertDialogView.findViewById(R.id.SBSmoothing);
		
		lbstrokeSize = (TextView)AlertDialogView.findViewById(R.id.LblStrokeWidth);
		lbalpha = (TextView)AlertDialogView.findViewById(R.id.LblAlpha);
		lbcolor = (TextView)AlertDialogView.findViewById(R.id.LblColor);
		lbmix = (TextView)AlertDialogView.findViewById(R.id.LblMix);
		lbspecing = (TextView)AlertDialogView.findViewById(R.id.LblSpecing);
		lbscattering = (TextView)AlertDialogView.findViewById(R.id.LblScattering);
		lbsmoothing = (TextView)AlertDialogView.findViewById(R.id.LblSmoothing);

		cbstrokeSize = (CheckBox)AlertDialogView.findViewById(R.id.CBPressureStroke);
		cbalpha = (CheckBox)AlertDialogView.findViewById(R.id.CBPressureAlpha);
		cbscattering = (CheckBox)AlertDialogView.findViewById(R.id.CBPressureScattering);
		
		ArrayAdapter<CharSequence> aatoolinfo = ArrayAdapter.createFromResource(context, R.array.tipNames, android.R.layout.simple_spinner_item);
		aatoolinfo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sptoolinfo.setAdapter(aatoolinfo);
		sptoolinfo.setSelection(cinfo.type);

		sbstrokeSize.setOnSeekBarChangeListener(this);
		sbstrokeSize.setMax(200);
		sbstrokeSize.setProgress(ctrl.getBrushSize());

		sbalpha.setOnSeekBarChangeListener(this);
		sbalpha.setMax(255);
		sbalpha.setProgress(ctrl.getAlpha());

		sbcolor.setOnSeekBarChangeListener(this);
		sbcolor.setMax(100);
		sbstrokeSize.setProgress(Math.round(cinfo.resat));

		sbmix.setOnSeekBarChangeListener(this);
		sbmix.setMax(100);
		sbmix.setProgress(Math.round(cinfo.bleed));

		sbspecing.setOnSeekBarChangeListener(this);
		sbspecing.setMax(100);
		sbspecing.setProgress(Math.round(cinfo.spacing * 100));

		sbscattering.setOnSeekBarChangeListener(this);
		sbscattering.setMax(1000);
		sbscattering.setProgress(Math.round(cinfo.scattering * 100));

		sbsmoothing.setOnSeekBarChangeListener(this);
		sbsmoothing.setMax(100);
		sbsmoothing.setProgress(Math.round(cinfo.smoothing * 100));
		
		cbstrokeSize.setChecked(cinfo.pressureSize);
		cbalpha.setChecked(cinfo.pressureAlpha);
		cbscattering.setChecked(cinfo.pressureScattering);
		
		dlg = new AlertDialog.Builder(context)
		.setTitle(R.string.brushsettings)
		.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				updateLock = true;
				brushSettingsDialog.this.cinfo = brushSettingsDialog.this.ctrl.getBrushInfo();
				brushSettingsDialog.this.ctrl.setBrushSize(sbstrokeSize.getProgress());
				brushSettingsDialog.this.ctrl.setAlpha(sbalpha.getProgress());
				brushSettingsDialog.this.cinfo.type = sptoolinfo.getSelectedItemPosition();
				brushSettingsDialog.this.cinfo.resat = sbcolor.getProgress() / 100F;
				brushSettingsDialog.this.cinfo.spacing = sbspecing.getProgress() / 100F;
				brushSettingsDialog.this.cinfo.scattering = sbscattering.getProgress() / 100F;
				brushSettingsDialog.this.cinfo.smoothing = sbsmoothing.getProgress() / 100F;
				brushSettingsDialog.this.cinfo.pressureSize = cbstrokeSize.isChecked();
				brushSettingsDialog.this.cinfo.pressureAlpha = cbalpha.isChecked();
				brushSettingsDialog.this.cinfo.pressureScattering = cbscattering.isChecked();
				updateLock = false;
				brushSettingsDialog.this.ctrl.callToolListeners();
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.create();
		
		dlg.setView(AlertDialogView);
		
		this.ctrl.addToolListener(this);
	}
	
	public void showDialog() {
		dlg.show();
	}

	@Override
	public void onProgressChanged(SeekBar sb, int p, boolean u) {
		if(sb == sbstrokeSize) {
			lbstrokeSize.setText(R.string.strokesize);
			lbstrokeSize.append(": " + p);
		} else if(sb == sbalpha) {
			lbalpha.setText(R.string.alpha);
			lbalpha.append(": " + p);
		} else if(sb == sbcolor) {
			lbcolor.setText(R.string.color);
			lbcolor.append(": " + p + "%");
		} else if(sb == sbmix) {
			lbmix.setText(R.string.mix);
			lbmix.append(": " + p + "%");
		} else if(sb == sbspecing) {
			lbspecing.setText(R.string.specing);
			lbspecing.append(": " + p + "%");
		} else if(sb == sbscattering) {
			lbscattering.setText(R.string.scattering);
			lbscattering.append(": " + p + "%");
		} else if(sb == sbsmoothing) {
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
		if(updateLock) return;
		sptoolinfo.setSelection(toolInfo.type);
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

}
