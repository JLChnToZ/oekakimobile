package idv.jlchntoz.oekakimobile;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CheckBoxedArrayAdapter extends ArrayAdapter<CheckBoxedArrayAdapter.ContentHolder> {
	private list entries;
	private int resource, checkBoxResourceId, textViewResourceId;
	private Context context;
	OnCheckChangedListener CheckListener;
	OnItemClickListener ClickListener;
	Resources res;
	
	public interface OnCheckChangedListener {
		public void OnCheckedChange(CompoundButton buttonView, int position, boolean isChecked);
	}
	
	public interface OnItemClickListener {
		public void OnItemClick(View view, int position);
	}
	
	@SuppressWarnings("serial")
	public final static class list extends ArrayList<ContentHolder> {
		public list() { super(); }
	}
	
	public CheckBoxedArrayAdapter(Context context, int resource, int checkBoxResourceId,
			int textViewResourceId, list entries) {
		super(context, resource, textViewResourceId, entries);
		this.context = context;
		this.resource = resource;
		this.checkBoxResourceId = checkBoxResourceId;
		this.textViewResourceId = textViewResourceId;
		this.entries = entries;
		res = context.getResources();
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder VH;
		if(convertView == null) {
			LayoutInflater VI = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = VI.inflate(resource, null);
			VH = new ViewHolder();
			VH.TVName = (TextView)convertView.findViewById(textViewResourceId);
			VH.Checker = (CheckBox)convertView.findViewById(checkBoxResourceId);
			convertView.setTag(VH);
		} else
			VH = (ViewHolder)convertView.getTag();
		final ContentHolder CH = entries.get(position);
		if(CH != null) {
			VH.TVName.setText(CH.name);
			VH.Checker.setChecked(CH.Checked);
			if(CheckListener != null)
				VH.Checker.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						CheckListener.OnCheckedChange(buttonView, position, isChecked);
					}
				});
			if(CH.Selected)
				convertView.setBackgroundColor(res.getColor(android.R.color.darker_gray));
			else
				convertView.setBackgroundColor(res.getColor(android.R.color.transparent));
		}
		OnTouchListener OTL = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				switch(e.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if(ClickListener != null)
							ClickListener.OnItemClick(v, position);
						break;
				}
				return false;
			}
		};
		convertView.setOnTouchListener(OTL);
		VH.TVName.setOnTouchListener(OTL);
		return convertView;
	}

	public void setOnCheckedChangeListener(OnCheckChangedListener listener) {
		CheckListener = listener;
	}
	
	public void setOnClickListener(OnItemClickListener listener) {
		ClickListener = listener;
	}
	
	public void add(String name, Boolean Checked, Boolean Selected) {
		super.add(new ContentHolder(name, Checked, Selected));
	}
	
	public void insert(String name, Boolean Checked, Boolean Selected, int index) {
		super.insert(new ContentHolder(name, Checked, Selected), index);
	}

	private class ViewHolder {
		public TextView TVName;
		public CheckBox Checker;
	}
	
	public class ContentHolder {
		public String name;
		public Boolean Checked;
		public Boolean Selected;
		
		public ContentHolder() {
			this("");
		}
		
		public ContentHolder(String name) {
			this(name, false);
		}
		
		public ContentHolder(String name, Boolean Checked) {
			this(name, Checked, false);
		}

		public ContentHolder(String name, Boolean Checked, Boolean Selected) {
			this.name = name;
			this.Checked = Checked;
			this.Selected = Selected;
		}
	}
}
