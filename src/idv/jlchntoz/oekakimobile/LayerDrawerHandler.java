package idv.jlchntoz.oekakimobile;

import idv.jlchntoz.oekakimobile.CheckBoxedArrayAdapter.OnCheckChangedListener;

import com.chibipaint.*;
import com.chibipaint.engine.*;
import com.chibipaint.util.CPRect;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class LayerDrawerHandler implements CPArtwork.ICPArtworkListener, OnClickListener,
DragSortListView.DropListener, DragSortListView.RemoveListener {
	
	public final CPController controller;
	public final Context context;
	public final View drawerView;
	
	DragSortController lstLayerCtrl;
	CheckBoxedArrayAdapter lstLayersAdapter;
	DragSortListView lstLayers;
	ImageButton btnAdd, btnRemove, btnRename, btnDuplicate, btnMergeDown;
	CheckBox CBLockAlpha, CBSampleAll;
	SeekBar SBAlpha;
	Spinner SPMixType;
	
	public LayerDrawerHandler(Context context, CPController controller, View drawerView) {
		this.context = context;
		this.drawerView = drawerView;
		this.controller = controller;
		
        lstLayers = (DragSortListView)drawerView.findViewById(R.id.lstlayers);
        CBLockAlpha = (CheckBox)drawerView.findViewById(R.id.cblockalpha);
        CBSampleAll = (CheckBox)drawerView.findViewById(R.id.cbsampleall);
        SBAlpha = (SeekBar)drawerView.findViewById(R.id.sblayeralpha);
        SPMixType = (Spinner)drawerView.findViewById(R.id.spmixtype);
        btnAdd = (ImageButton)drawerView.findViewById(R.id.btnadd);
        btnRemove = (ImageButton)drawerView.findViewById(R.id.btnremove);
        btnRename = (ImageButton)drawerView.findViewById(R.id.btneditname);
        btnDuplicate = (ImageButton)drawerView.findViewById(R.id.btnduplicate);
        btnMergeDown = (ImageButton)drawerView.findViewById(R.id.btnmergedown);
        
        btnAdd.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        btnRename.setOnClickListener(this);
        btnDuplicate.setOnClickListener(this);
        btnMergeDown.setOnClickListener(this);

		ArrayAdapter<CharSequence> aaMixType = ArrayAdapter.createFromResource(this.context,
				R.array.mixModeNames, android.R.layout.simple_spinner_item);
		aaMixType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SPMixType.setAdapter(aaMixType);
        SPMixType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				LayerDrawerHandler.this.controller.artwork.setBlendMode(
						LayerDrawerHandler.this.controller.artwork.getActiveLayerNb(),
						position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) { }
        });
		
        SBAlpha.setMax(100);
        SBAlpha.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar sb, int p, boolean u) {
				LayerDrawerHandler.this.controller.artwork.setLayerAlpha(
						LayerDrawerHandler.this.controller.artwork.getActiveLayerNb(), p);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        
        CBLockAlpha.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				LayerDrawerHandler.this.controller.artwork.setLockAlpha(isChecked);
			}
        });
        
        CBSampleAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				LayerDrawerHandler.this.controller.artwork.setSampleAllLayers(isChecked);
				
			}
        });

		loadLayerSettings();

        lstLayerCtrl = new DragSortController(lstLayers);
        lstLayers.setFloatViewManager(lstLayerCtrl);
        lstLayers.setOnTouchListener(lstLayerCtrl);
        lstLayers.setChoiceMode(DragSortListView.CHOICE_MODE_MULTIPLE);
        lstLayers.setDropListener(this);
        lstLayers.setRemoveListener(this);
        
        lstLayerCtrl.setDragHandleId(R.id.drag_handle);
        lstLayerCtrl.setRemoveMode(DragSortController.FLING_REMOVE);
        lstLayerCtrl.setDragInitMode(DragSortController.ON_DRAG);
        
        controller.artwork.addListener(this);
        layerChange(controller.artwork);
	}

	@Override
	public void updateRegion(CPArtwork artwork, CPRect region) { }

	@Override
	public void layerChange(CPArtwork artwork) {
		Boolean lstNull = false;
		Object layers[] = artwork.getLayers();
		int index = 0, count = layers.length;
		if(lstLayersAdapter == null) {
	        lstLayersAdapter = new CheckBoxedArrayAdapter(this.context,
	        		R.layout.checkablelayout, R.id.cbchecked, R.id.tvlsicontent,
	        		new CheckBoxedArrayAdapter.list());
	        lstLayersAdapter.setOnCheckedChangeListener(new OnCheckChangedListener() {
				@Override
				public void OnCheckedChange(CompoundButton buttonView,
						int position, boolean isChecked) {
					LayerDrawerHandler.this.controller.artwork.setLayerVisibility(
							lstLayersAdapter.getCount() - 1 - position, isChecked);
				}
	        });
	        lstLayersAdapter.setOnClickListener(new CheckBoxedArrayAdapter.OnItemClickListener() {
				@Override
				public void OnItemClick(View view, int pos) {
					int p = lstLayersAdapter.getCount() - 1 - pos;
					LayerDrawerHandler.this.controller.artwork.setActiveLayer(p);
					loadLayerSettings();
					LayerDrawerHandler.this.controller.artwork.callListenersLayerChange();
				}
	        });
	        lstNull = true;
	    }
		for(Object layer : layers) {
			CPLayer _l = (CPLayer)layer;
			int _index = lstLayersAdapter.getCount() - 1 - index;
			if(index < lstLayersAdapter.getCount()) {
				lstLayersAdapter.remove(lstLayersAdapter.getItem(_index));
				lstLayersAdapter.insert(_l.name, _l.visible, this.controller.artwork.getActiveLayerNb() == index , _index);
			} else
				lstLayersAdapter.add(_l.name, _l.visible, this.controller.artwork.getActiveLayerNb() == index);
			index++;
		}
		while(index < lstLayersAdapter.getCount())
			lstLayersAdapter.remove(lstLayersAdapter.getItem(0));
		if(lstNull)
			lstLayers.setAdapter(lstLayersAdapter);
		else
			lstLayersAdapter.notifyDataSetChanged();
		for(int i = 0; i < count; i++)
			lstLayers.setItemChecked(lstLayersAdapter.getCount() - 1 - i, ((CPLayer)layers[i]).visible);
		lstLayerCtrl.setRemoveEnabled(count > 1);
		this.controller.artwork.callListenersUpdateRegion(this.controller.artwork.getSize());
	}

	@Override
	public void onClick(View view) {
		if(view == btnAdd)
			this.controller.artwork.addLayer();
		else if(view == btnRemove)
			this.controller.artwork.removeLayer();
		else if(view == btnMergeDown)
			this.controller.artwork.mergeDown(true);
		else if(view == btnDuplicate)
			this.controller.artwork.duplicateLayer();
		else if(view == btnRename) {
			PromptDialog dialog = new PromptDialog(this.context,
					this.context.getString(R.string.layername),
					this.controller.artwork.getActiveLayer().name,
					new PromptDialog.PromptDialogCallBack() {
						@Override
						public void onCallBack(PromptDialog which, String result) {
					        LayerDrawerHandler.this.controller.artwork.setLayerName(
					        		LayerDrawerHandler.this.controller.artwork.getActiveLayerNb(),
					        		result);
						}
					});
			dialog.showDialog();
		}
		
	}
	
	private void loadLayerSettings() {
		SBAlpha.setProgress(this.controller.artwork.getActiveLayer().alpha);
		SPMixType.setSelection(this.controller.artwork.getActiveLayer().blendMode);
	}

	@Override
	public void remove(int which) {
		this.controller.artwork.setActiveLayer(lstLayersAdapter.getCount() - 1 - which);
		this.controller.artwork.removeLayer();
	}

	@Override
	public void drop(int from, int to) {
		from = lstLayersAdapter.getCount() - 1 - from;
		to = lstLayersAdapter.getCount() - 1 - to;
		this.controller.artwork.setActiveLayer(to);
		loadLayerSettings();
		if(to > from) {
			int t = to;
			to = from;
			from = t;
		}
		if(to != from)
			this.controller.artwork.moveLayer(from, to);
		loadLayerSettings();
	}

}
