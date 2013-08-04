package idv.jlchntoz.oekakimobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.widget.Toast;

public class savedSettings {

	private static String colorDBName = "colorpalettes";
	private final Context context;
	DBHelper colorMap;
	
	public savedSettings(Context c) {
		this.context = c;
		colorMap = new DBHelper(c, "oekakimobile_settings", colorDBName, "id INTEGER PRIMARY KEY, color INTEGER");
	}
	
	public Integer[] getColors() {
		try {
			Integer[] ret = null;
			Cursor c;
			int max = -1;
			SQLiteDatabase db = colorMap.getReadableDatabase();
			c = db.rawQuery("SELECT MAX(id) FROM "+colorDBName, null);
			c.moveToNext();
			if(c.getCount() > 0)
				max =  c.getInt(c.getColumnIndex("MAX(id)"));
			c.close();
			if(max >= 0) {
				c = db.rawQuery("SELECT * FROM "+colorDBName, null);
				ret = new Integer[max+1];
				while(c.moveToNext())
					ret[c.getInt(0)] = c.getInt(1);
				c.close();
			}
			db.close();
			return ret;
		} catch(Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG).show();
			return null;
		}
	}
	
	public void saveColors(Integer[] colors) {
		try {
			SQLiteDatabase db = colorMap.getWritableDatabase();
			for(int i = 0; i < colors.length; i++)
				db.execSQL("INSERT OR REPLACE INTO "+colorDBName+" VALUES ("+i+", "+colors[i]+")");
			db.close();
		} catch(Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	public void saveColor(int index, int color) {
		try {
			SQLiteDatabase db = colorMap.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO "+colorDBName+" VALUES ("+index+", "+color+")");
			db.close();
		} catch(Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private class DBHelper extends SQLiteOpenHelper {
		public final String tableName;
		private final String cStatement;
		
		public DBHelper(Context c, String dbName, String tableName, String createStatement) {
			super(c, dbName, null, 33);
			this.tableName = tableName;
			this.cStatement = createStatement;
		}

		@Override
		public void onCreate(SQLiteDatabase d) {
			d.execSQL("CREATE TABLE IF NOT EXISTS "+this.tableName+" ("+this.cStatement+")");
		}

		@Override
		public void onUpgrade(SQLiteDatabase d, int v1, int v2) {
			d.execSQL("DROP TABLE IF EXISTS "+this.tableName);
			onCreate(d);
		}
	}
}
