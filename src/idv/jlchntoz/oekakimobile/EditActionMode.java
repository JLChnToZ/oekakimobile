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

import android.content.Context;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.*;
import com.actionbarsherlock.view.ActionMode.Callback;

import com.chibipaint.CPController;
import com.chibipaint.engine.CPArtwork;
import com.chibipaint.util.CPRect;

public class EditActionMode implements Callback {

	public final CPController controller;
	public final Context context;
	public final CPArtwork artwork;

	public EditActionMode(Context context, CPController controller) {
		this.context = context;
		this.controller = controller;
		artwork = controller.getArtwork();
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		((SherlockActivity) context).getSupportMenuInflater().inflate(R.menu.edit,
				menu);
		mode.setTitle(R.string.edit);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_undo:
			artwork.undo();
			((MainActivity)context).rePaint();
			break;
		case R.id.menu_redo:
			artwork.redo();
			((MainActivity)context).rePaint();
			break;
		case R.id.menu_cut:
			artwork.cutSelection(true);
			((MainActivity)context).rePaint();
			break;
		case R.id.menu_copy:
			artwork.copySelection();
			break;
		case R.id.menu_copymerged:
			artwork.copySelectionMerged();
			break;
		case R.id.menu_paste:
			artwork.pasteClipboard(true);
			((MainActivity)context).rePaint();
			break;
		case R.id.menu_selall:
			artwork.rectangleSelection(artwork.getSize());
			break;
		case R.id.menu_deselall:
			artwork.rectangleSelection(new CPRect());
			break;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub

	}

}
