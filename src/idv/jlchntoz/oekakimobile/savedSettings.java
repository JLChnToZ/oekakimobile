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

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.*;
import android.widget.Toast;

import com.chibipaint.CPController;
import com.chibipaint.engine.*;

public class savedSettings {

	private static String DBFileName = "oekakimobile_settings";
	private static String colorDBName = "colorpalettes";
	private static String pensDBName = "custompens";
	private final Context context;
	DBHelper helper;

	public savedSettings(Context c) {
		this.context = c;
		helper = new DBHelper(c, DBFileName);

		SQLiteDatabase db = helper.getWritableDatabase();

		db.execSQL("CREATE TABLE IF NOT EXISTS " + colorDBName
				+ " (id INTEGER PRIMARY KEY, color INTEGER)");
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ pensDBName
				+ " (id INTEGER PRIMARY KEY, name TEXT NOT NULL, "
				+ "size INTEGER NOT NULL, alpha INTEGER NOT NULL, flag INTEGER NOT NULL, minspecing REAL NOT NULL, "
				+ "specing REAL NOT NULL, btype INTEGER NOT NULL, pmode INTEGER NOT NULL, resat REAL NOT NULL, bleed REAL NOT NULL)");

		if (DatabaseUtils.queryNumEntries(db, pensDBName) <= 0) {
			saveCustomPen2(
					db,
					new CPBrushInfo(CPController.T_PENCIL, 16, 255, true,
							false, .5f, .05f, false, true,
							CPBrushInfo.B_ROUND_AA, CPBrushInfo.M_PAINT, 1f, 0f)
							.setName(c.getResources()
									.getString(R.string.pencil)));
			saveCustomPen2(
					db,
					new CPBrushInfo(CPController.T_ERASER, 16, 255, true,
							false, .5f, .05f, false, false,
							CPBrushInfo.B_ROUND_AA, CPBrushInfo.M_ERASE, 1f, 0f)
							.setName(c.getResources()
									.getString(R.string.eraser)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_PEN, 2, 128, true, false,
							.5f, .05f, true, false, CPBrushInfo.B_ROUND_AA,
							CPBrushInfo.M_PAINT, 1f, 0f).setName(c
							.getResources().getString(R.string.pen)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_SOFTERASER, 16, 64, false,
							true, .5f, .05f, false, true,
							CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_ERASE,
							1f, 0f).setName(c.getResources().getString(
							R.string.softearser)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_AIRBRUSH, 50, 32, false,
							true, .5f, .05f, false, true,
							CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_PAINT,
							1f, 0f).setName(c.getResources().getString(
							R.string.airbrush)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_DODGE, 30, 32, false, true,
							.5f, .05f, false, true,
							CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_DODGE,
							1f, 0f).setName(c.getResources().getString(
							R.string.dodge)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_BURN, 30, 32, false, true,
							.5f, .05f, false, true,
							CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_BURN,
							1f, 0f).setName(c.getResources().getString(
							R.string.burn)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_WATER, 30, 70, false, true,
							.5f, .02f, false, true, CPBrushInfo.B_ROUND_AA,
							CPBrushInfo.M_WATER, .3f, .6f).setName(c
							.getResources().getString(R.string.water)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_BLUR, 20, 255, false, true,
							.5f, .05f, false, true, CPBrushInfo.B_ROUND_PIXEL,
							CPBrushInfo.M_BLUR, 1f, 0f).setName(c
							.getResources().getString(R.string.blur)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_SMUDGE, 20, 128, false,
							true, .5f, .01f, false, true,
							CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_SMUDGE,
							0f, 1f).setName(c.getResources().getString(
							R.string.smudge)));
			saveCustomPen2(db,
					new CPBrushInfo(CPController.T_SMUDGE, 20, 60, false, true,
							.5f, .1f, false, true,
							CPBrushInfo.B_ROUND_AIRBRUSH, CPBrushInfo.M_OIL,
							0f, .07f).setName(c.getResources().getString(
							R.string.blender)));
		}
		db.close();
	}

	public Integer[] getColors() {
		try {
			Integer[] ret = null;
			Cursor c;
			int max = -1;
			SQLiteDatabase db = helper.getReadableDatabase();
			c = db.rawQuery("SELECT MAX(id) FROM " + colorDBName, null);
			c.moveToNext();
			if (c.getCount() > 0)
				max = c.getInt(c.getColumnIndex("MAX(id)"));
			c.close();
			if (max >= 0) {
				c = db.rawQuery("SELECT * FROM " + colorDBName, null);
				ret = new Integer[max + 1];
				while (c.moveToNext())
					ret[c.getInt(0)] = c.getInt(1);
				c.close();
			}
			db.close();
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG)
					.show();
			return null;
		}
	}

	public void saveColors(Integer[] colors) {
		try {
			SQLiteDatabase db = helper.getWritableDatabase();
			for (int i = 0; i < colors.length; i++)
				db.execSQL("INSERT OR REPLACE INTO " + colorDBName
						+ " VALUES (" + i + ", " + colors[i] + ")");
			db.close();
		} catch (Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	public void saveColor(int index, int color) {
		try {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL("INSERT OR REPLACE INTO " + colorDBName + " VALUES ("
					+ index + ", " + color + ")");
			db.close();
		} catch (Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	public ArrayList<CPBrushInfo> getCustomPens() {
		ArrayList<CPBrushInfo> p = new ArrayList<CPBrushInfo>();
		getCustomPens(p);
		return p;
	}

	public void getCustomPens(ArrayList<CPBrushInfo> pensList) {
		try {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor c = db.rawQuery("SELECT * FROM " + pensDBName,
					new String[] {});
			while (c.moveToNext()) {
				int flags = c.getInt(c.getColumnIndex("flag"));
				pensList.add(new CPBrushInfo(c.getInt(c.getColumnIndex("id")),
						c.getInt(c.getColumnIndex("size")), c.getInt(c
								.getColumnIndex("alpha")), c.getFloat(c
								.getColumnIndex("minspecing")), c.getFloat(c
								.getColumnIndex("specing")),
						(flags & (1 << 0)) != 0, (flags & (1 << 1)) != 0, c
								.getInt(c.getColumnIndex("btype")), c.getInt(c
								.getColumnIndex("pmode")), c.getFloat(c
								.getColumnIndex("resat")), c.getFloat(c
								.getColumnIndex("bleed"))).setName(c
						.getString(c.getColumnIndex("name"))));
			}
			db.close();
		} catch (Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	public void saveCustomPen(CPBrushInfo pen) {
		try {
			SQLiteDatabase db = helper.getWritableDatabase();
			int flags = (pen.pressureSize ? 1 << 0 : 0)
					| (pen.pressureAlpha ? 1 << 1 : 0);
			db.execSQL("INSERT OR REPLACE INTO "
					+ pensDBName
					+ "(id, name, size, alpha, flag, minspecing, specing, btype, "
					+ "pmode, resat, bleed) VALUES (" + pen.toolNb + ", \""
					+ pen.getName() + "\", " + pen.size + ", " + pen.alpha
					+ "," + flags + ", " + pen.minSpacing + ", " + pen.spacing
					+ ", " + pen.type + ", " + pen.paintMode + ", " + pen.resat
					+ ", " + pen.bleed + ")");
			db.close();
		} catch (Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private void saveCustomPen2(SQLiteDatabase db, CPBrushInfo pen) {
		try {
			int flags = (pen.isAA ? 1 << 0 : 0) | (pen.isAirbrush ? 1 << 1 : 0)
					| (pen.pressureSize ? 1 << 2 : 0)
					| (pen.pressureAlpha ? 1 << 3 : 0);
			db.execSQL("INSERT OR REPLACE INTO "
					+ pensDBName
					+ "(id, name, size, alpha, flag, minspecing, specing, btype, "
					+ "pmode, resat, bleed) VALUES (" + pen.toolNb + ", \""
					+ pen.getName() + "\", " + pen.size + ", " + pen.alpha
					+ "," + flags + ", " + pen.minSpacing + ", " + pen.spacing
					+ ", " + pen.type + ", " + pen.paintMode + ", " + pen.resat
					+ ", " + pen.bleed + ")");
		} catch (Exception ex) {
			ex.printStackTrace(); // For debugging
			Toast.makeText(this.context, ex.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context c, String dbName) {
			super(c, dbName, null, 33);
		}

		@Override
		public void onCreate(SQLiteDatabase a) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase a, int b, int c) {

		}
	}
}
