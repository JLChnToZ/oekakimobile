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
	
	public ColorPickerDialog(Context context, int color, ColorPickerCallback callback) {
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
		
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	callback.onCallback(ColorPickerDialog.this, picker.getColor());
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
	
	public ColorPickerView getView() {
		return picker;
	}
	
	public void showDialog() {
		dialog.show();
	}

}
