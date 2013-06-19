package idv.jlchntoz.oekakimobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class BlurDialog {
	public interface BlurDialogCallBack {
		public void onCallBack(BlurDialog which, int width, int height, int iterations);
	}
	
	private final Context context;
	private final BlurDialogCallBack callBack;
	private final View DialogView;
	private final EditText tvwidth, tvheight, tviterations;
	private AlertDialog dialog;
	private String prompt;
	private int w, h, i;
	
	public BlurDialog(Context context, String prompt, int width, int height, int iterations, BlurDialogCallBack Callback) {
		this.callBack = Callback;
		this.context = context;
		this.prompt = prompt;
		this.w = width;
		this.h = height;
		this.i = iterations;
		
		this.DialogView = LayoutInflater.from(context).inflate(R.layout.blurdialog, null);
		this.tvwidth = (EditText)this.DialogView.findViewById(R.id.blurdlgwidth);
		this.tvheight = (EditText)this.DialogView.findViewById(R.id.blurdlgheight);
		this.tviterations = (EditText)this.DialogView.findViewById(R.id.blurdlgitr);
		
		buildDialog();
	}
	
	private void buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setTitle(prompt);
		builder.setView(DialogView);
		
		tvwidth.setText("" + w);
		tvheight.setText("" + h);
		
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	w = Integer.parseInt(tvwidth.getText().toString());
		    	h = Integer.parseInt(tvheight.getText().toString());
		    	i = Integer.parseInt(tviterations.getText().toString());
		    	callBack.onCallBack(BlurDialog.this, w, h, i);
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
