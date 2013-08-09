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

public class LayersActionMode implements Callback {

	public final CPController controller;
	public final Context context;
	public final CPArtwork artwork;

	public LayersActionMode(Context context, CPController controller) {
		this.context = context;
		this.controller = controller;
		this.artwork = controller.getArtwork();
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		((SherlockActivity) context).getSupportMenuInflater().inflate(
				R.menu.editlayers, menu);
		mode.setTitle(R.string.layers);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_layer_add:
			this.artwork.addLayer();
			break;
		case R.id.menu_layer_remove:
			this.controller.artwork.removeLayer();
			break;
		case R.id.menu_layer_mergedown:
			this.controller.artwork.mergeDown(true);
			break;
		case R.id.menu_layer_duplicate:
			this.controller.artwork.duplicateLayer();
			break;
		case R.id.menu_layer_edit:
			PromptDialog dialog = new PromptDialog(this.context,
					this.context.getString(R.string.layername),
					this.controller.artwork.getActiveLayer().name,
					new PromptDialog.PromptDialogCallBack() {
						@Override
						public void onCallBack(PromptDialog which, String result) {
							artwork.setLayerName(artwork.getActiveLayerNb(),
									result);
						}
					});
			dialog.showDialog();
			break;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		((MainActivity) context).drawer.showContent();
	}

}
