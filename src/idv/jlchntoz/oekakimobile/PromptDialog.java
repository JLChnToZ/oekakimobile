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
