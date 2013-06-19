package idv.jlchntoz.oekakimobile;

import java.util.List;
import java.util.Map;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;

public class HashMapTextIconAdapter extends SimpleAdapter {

    private LayoutInflater mInflater;
    private String imageNameName, imageIconName;
    private int resource, imageNameSpinner, imageIconSpinner;
    private List<? extends Map<String, ?>> dataRecieved;

	public HashMapTextIconAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
        this.dataRecieved = data;
        this.mInflater = LayoutInflater.from(context);
        this.resource = resource;
        this.imageNameName = from[0];
        this.imageIconName = from[1];
        this.imageNameSpinner = to[0];
        this.imageIconSpinner = to[1];
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(resource, null);
        }
        ((TextView)convertView.findViewById(imageNameSpinner))
                .setText((String)dataRecieved.get(position).get(imageNameName));
        ((ImageView)convertView.findViewById(imageIconSpinner))
                .setImageBitmap((Bitmap)dataRecieved.get(position).get(imageIconName));
        return convertView;
    }

	@Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
