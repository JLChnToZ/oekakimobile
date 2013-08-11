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

import java.io.*;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.*;
import android.widget.Toast;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.*;
import com.actionbarsherlock.view.MenuItem.*;

import com.chibipaint.*;
import com.chibipaint.CPController.*;
import com.chibipaint.engine.*;
import com.chibipaint.util.*;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends SherlockActivity implements
		OnMenuItemClickListener, ICPToolListener, ICPModeListener,
		ICPColorListener, ICPEventListener, ICPViewListener,
		SlidingMenu.OnCloseListener, SlidingMenu.OnOpenListener, OnTouchListener {

	public static final int MSG_UPDATECOLOR = 2;

	final String extFilePath = Environment.getExternalStorageDirectory()
			.getPath();
	String fileName;

	PaintCanvas PC;
	public SlidingMenu drawer;

	brushSettingsDialog BSD;

	int penChecked;
	SubMenu pensMenu;
	ArrayList<MenuItem> customPenMenuItems;

	int modeChecked;
	MenuItem[] modeMenuItems;

	MenuItem colorMainMenuItem, colorMenuItem, brushSettingsMenuItem,
			textureSettingsMenuItem;
	LayerDrawerHandler drawerHandler1;
	ColorTextureDrawerHandler drawerHandler2;
	ActionMode drawerActionMode;

	CPBrushInfo Cinfo;
	CPController controller;
	CPArtwork artwork;
	ArrayList<CPBrushInfo> customPens;

	BitmapDrawable colorIcon;
	Bitmap colorIconBase;
	Canvas ColorIconCanvas;
	int colorPicked;

	savedSettings settings;

	boolean doubleBackToExitPressedOnce;

	public MainActivity() {
		super();
		customPenMenuItems = new ArrayList<MenuItem>();
		customPens = new ArrayList<CPBrushInfo>();
	}

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setHomeButtonEnabled(true);

		settings = new savedSettings(this);

		PC = (PaintCanvas) findViewById(R.id.cbpaintcanvas);
		PC.setOnTouchListener(this);
		controller = PC.controller;
		Intent currentIntent = getIntent();
		if (currentIntent.hasExtra("file"))
			fileName = currentIntent.getStringExtra("file");
		else if (currentIntent.getData() != null
				&& !currentIntent.hasExtra("notfirstrun"))
			fileName = currentIntent.getData().getEncodedPath();
		if (fileName != null)
			try {
				File file = new File(fileName);
				if (file.exists()) {
					FileInputStream FIS = new FileInputStream(file);
					setArtwork(CPChibiFile.read(MainActivity.this, FIS));
					FIS.close();
				}
			} catch (Exception e) {
				Toast.makeText(MainActivity.this, getString(R.string.fileioerror),
						Toast.LENGTH_LONG).show();
			}
		else {
			int width = 800, height = 600;
			if (currentIntent.hasExtra("width"))
				width = currentIntent.getIntExtra("width", 800);
			if (currentIntent.hasExtra("height"))
				height = currentIntent.getIntExtra("height", 600);
			setArtwork(new CPArtwork(this, width, height));
			fileName = "";
		}

		modeMenuItems = new MenuItem[controller.M_MAX];

		BSD = new brushSettingsDialog(this, controller);

		drawer = new SlidingMenu(this);
		drawer.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		drawer.setShadowWidthRes(R.dimen.shadow_width);
		drawer.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		drawer.setFadeDegree(0.35f);
		drawer.setBehindWidthRes(R.dimen.drawer_size);

		drawer.setShadowDrawable(R.drawable.shadow);
		drawer.setMenu(R.layout.drawer_content);

		drawer.setSecondaryShadowDrawable(R.drawable.shadow_r);
		drawer.setSecondaryMenu(R.layout.drawer_content2);

		drawer.setOnOpenListener(this);
		drawer.setOnCloseListener(this);

		drawer.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		drawerHandler1 = new LayerDrawerHandler(this, controller, drawer.getMenu());
		drawerHandler2 = new ColorTextureDrawerHandler(this, controller,
				drawer.getSecondaryMenu());

		controller.setCurColor(new CPColor(getResources().getColor(
				R.color.DefaultColor)));

		controller.addToolListener(this);
		controller.addColorListener(this);
		controller.addCPEventListener(this);
		controller.addModeListener(this);
		controller.addViewListener(this);

		settings.getCustomPens(customPens);
	}

	@SuppressLint("HandlerLeak")
	private final Handler _h = new Handler() {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case MSG_UPDATECOLOR:
				colorIconBase = drawerHandler2.getBitmap();
				colorIcon = new BitmapDrawable(getResources(), colorIconBase);
				colorMainMenuItem.setIcon(colorIcon);
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getSupportMenuInflater().inflate(R.menu.main, menu);

		pensMenu = menu.addSubMenu(getString(R.string.tools));

		refreshPensMenu();

		pensMenu
				.getItem()
				.setIcon(R.drawable.ic_menu_tools)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		SubMenu colorMenu = menu.addSubMenu(getString(R.string.colortexture))
				.setIcon(colorIcon);

		colorMainMenuItem = colorMenu.getItem();
		colorMainMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		colorMainMenuItem.setOnMenuItemClickListener(this);
		newColor(controller.getCurColor());

		return true;
	}

	private void refreshPensMenu() {
		runOnUiThread(r_RefreshPensMenu);
	}

	private Runnable r_RefreshPensMenu = new Runnable() {
		@Override
		public void run() {
			pensMenu.clear();
			customPenMenuItems.clear();

			modeMenuItems[CPController.M_MOVE_TOOL] = pensMenu.add(2, Menu.NONE,
					Menu.NONE, getString(R.string.move)).setOnMenuItemClickListener(
					MainActivity.this);
			modeMenuItems[CPController.M_MOVE_CANVAS] = pensMenu.add(2, Menu.NONE,
					Menu.NONE, getString(R.string.movecanvas))
					.setOnMenuItemClickListener(MainActivity.this);
			modeMenuItems[CPController.M_RECT_SELECTION] = pensMenu.add(2, Menu.NONE,
					Menu.NONE, getString(R.string.rectselection))
					.setOnMenuItemClickListener(MainActivity.this);
			modeMenuItems[CPController.M_COLOR_PICKER] = pensMenu.add(2, Menu.NONE,
					Menu.NONE, getString(R.string.colorpicker))
					.setOnMenuItemClickListener(MainActivity.this);
			modeMenuItems[CPController.M_FLOODFILL] = pensMenu.add(2, Menu.NONE,
					Menu.NONE, getString(R.string.floodfill)).setOnMenuItemClickListener(
					MainActivity.this);

			for (int i = 0; i < modeMenuItems.length; i++)
				if (modeMenuItems[i] != null)
					modeMenuItems[i].setChecked(i == modeChecked);

			for (int i = 0; i < customPens.size(); i++) {
				while (customPenMenuItems.size() <= i)
					customPenMenuItems.add(null);
				if (customPens.get(i) != null)
					customPenMenuItems.set(
							i,
							pensMenu
									.add(2, Menu.NONE, Menu.NONE, customPens.get(i).getName())
									.setChecked(
											penChecked == i && modeChecked == CPController.M_DRAW)
									.setOnMenuItemClickListener(MainActivity.this));
			}

			pensMenu.setGroupCheckable(1, true, false);
			pensMenu.setGroupCheckable(2, true, false);

			brushSettingsMenuItem = pensMenu.add(3, Menu.NONE, Menu.NONE,
					getString(R.string.brushsettings)).setOnMenuItemClickListener(
					MainActivity.this);
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return onMenuItemClick(item) ? true : super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (colorMainMenuItem == item) {
			drawer.setMode(SlidingMenu.RIGHT);
			drawerHandler1.drawerView.setVisibility(View.INVISIBLE);
			drawerHandler2.drawerView.setVisibility(View.VISIBLE);
			if (drawer.isMenuShowing())
				drawer.showContent();
			if (drawer.isSecondaryMenuShowing())
				drawer.showContent();
			else
				drawer.showSecondaryMenu();
		} else if (brushSettingsMenuItem == item)
			BSD.showDialog();
		else
			switch (item.getItemId()) {
			case android.R.id.home:
				drawer.setMode(SlidingMenu.LEFT);
				drawerHandler1.drawerView.setVisibility(View.VISIBLE);
				drawerHandler2.drawerView.setVisibility(View.INVISIBLE);
				if (drawer.isSecondaryMenuShowing())
					drawer.showContent();
				if (drawer.isMenuShowing())
					drawer.showContent();
				else
					drawer.showMenu();
				break;
			case R.id.menu_edit:
				drawerActionMode = startActionMode(new EditActionMode(this, controller));
				break;
			case R.id.menu_new:
				SizeDialog nfdlg = new SizeDialog(this, getString(R.string.newfile),
						artwork.width, artwork.height, new SizeDialog.SizeDialogCallBack() {
							@Override
							public void onCallBack(SizeDialog which, int width, int height) {
								restartAndOpenWith(width, height);
							}
						});
				nfdlg.showDialog();
				break;
			case R.id.menu_open:
				FileDialog openDlg = new FileDialog(this, getString(R.string.open),
						extFilePath + File.separator + "mypaint", ".chi",
						new String[] { "chi" }, true, new FileDialog.FileDialogCallBack() {
							@Override
							public void onCallBack(FileDialog which, File file) {
								restartAndOpenWith(file);
							}
						});
				openDlg.showDialog();
				break;
			case R.id.menu_save:
				if (fileName != null && fileName != "") {
					saveFile(new File(fileName));
					break;
				}
			case R.id.menu_saveas:
				FileDialog saveDlg = new FileDialog(this, getString(R.string.save),
						extFilePath + File.separator + "mypaint",
						new File(fileName).getName(), new String[] { "chi" }, false,
						new FileDialog.FileDialogCallBack() {
							@Override
							public void onCallBack(FileDialog which, File file) {
								saveFile(file);
							}
						});
				saveDlg.showDialog();
				break;
			case R.id.menu_share:
				String _fileName = outputImage(fileName);
				if (_fileName == "")
					break;
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("image/png");
				share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + _fileName));
				startActivity(Intent.createChooser(share, getString(R.string.share)));
				break;
			case R.id.menu_hfilp:
				artwork.hFlip();
				break;
			case R.id.menu_vflip:
				artwork.vFlip();
				break;
			case R.id.menu_invert:
				artwork.invert();
				break;
			case R.id.menu_cnoise:
				artwork.colorNoise();
				break;
			case R.id.menu_noise:
				artwork.monochromaticNoise();
				break;
			case R.id.menu_boxblur:
				BlurDialog bbdlg = new BlurDialog(this, getString(R.string.boxblur), 3,
						3, 3, new BlurDialog.BlurDialogCallBack() {
							@Override
							public void onCallBack(BlurDialog which, int width, int height,
									int iterations) {
								artwork.boxBlur(width, height, iterations);
							}
						});
				bbdlg.showDialog();
				break;
			case R.id.menu_fullscreen:
				toggleStatusBar(getWindow(), false);
				getSupportActionBar().hide();
				break;
			case R.id.menu_about:
				AboutBox _ab = new AboutBox(this);
				_ab.showDialog();
				break;
			default:
				for (int i = 0; i < customPenMenuItems.size(); i++)
					if (item == customPenMenuItems.get(i)) {
						controller.setTool(customPens.get(i));
						return true;
					}
				for (int i = 0; i < modeMenuItems.length; i++)
					if (modeMenuItems[i] == item) {
						controller.setMode(i);
						return true;
					}
				return false;
			}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		exitFullScreen();
		return true;
	}

	private void exitFullScreen() {
		if (!getSupportActionBar().isShowing()) {
			getSupportActionBar().show();
			toggleStatusBar(getWindow(), true);
		}
	}

	private void toggleStatusBar(android.view.Window w, boolean show) {
		if (show) {
			WindowManager.LayoutParams attrs = w.getAttributes();
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
			w.setAttributes(attrs);
		} else {
			WindowManager.LayoutParams attrs = w.getAttributes();
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			w.setAttributes(attrs);
		}
	}

	@Override
	public void modeChange(int mode) {
		modeChecked = mode;
		refreshPensMenu();
	}

	@Override
	public void onOpen() {
		if (drawer.getMode() == SlidingMenu.LEFT) {
			onClose();
			drawerActionMode = startActionMode(new LayersActionMode(this, controller));
		}
	}

	@Override
	public void onClose() {
		if (drawerActionMode != null)
			drawerActionMode.finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if (v == PC)
			onClose();
		return false;
	}

	@Override
	public void newTool(int tool, CPBrushInfo toolInfo) {
		if (tool < 0)
			toolInfo.toolNb = tool = customPens.size();
		penChecked = tool;
		Cinfo = toolInfo;
		if (toolInfo.getName() != "") {
			while (tool >= customPens.size())
				customPens.add(null);
			customPens.set(tool, toolInfo);
		}
		refreshPensMenu();
	}

	@Override
	public void viewChange(CPViewInfo viewInfo) {
	}

	@Override
	public void cpEvent() {
		android.util.Log.d("CPEVENT", "CPEVENT CALLED");
	}

	@Override
	public void newColor(CPColor color) {
		colorPicked = color.rgb | 0xFF << 24;
		Message m = new Message();
		m.what = MSG_UPDATECOLOR;
		_h.sendMessage(m);
	}

	private void setArtwork(CPArtwork newArtWork) {
		PC.setArtWork(newArtWork);
		artwork = controller.artwork;
		PC.invalidate();
		controller.setTool(CPController.T_PEN);
		artwork.callListenersLayerChange();
	}

	private void saveFile(File file) {
		try {
			if (!file.getName().endsWith(".chi"))
				file = new File(file.getPath() + ".chi");
			file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
			FileOutputStream FOS = new FileOutputStream(file);
			CPChibiFile.write(FOS, artwork);
			FOS.close();
			fileName = file.getPath();
			outputImage(fileName);
		} catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(MainActivity.this, getString(R.string.fileioerror),
					Toast.LENGTH_LONG).show();
		}
	}

	private String outputImage(String fileName) {
		Bitmap bm = Bitmap.createBitmap(artwork.width, artwork.height,
				Bitmap.Config.ARGB_8888);
		bm.setPixels(artwork.getDisplayBM().data, 0, artwork.width, 0, 0,
				artwork.width, artwork.height);
		ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, BAOS);
		if (fileName == null || fileName == "")
			fileName = extFilePath + "/mypaint/share.png";
		else if (!fileName.endsWith(".png"))
			fileName += ".png";
		try {
			File file = new File(fileName);
			file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
			FileOutputStream FOS = new FileOutputStream(file);
			FOS.write(BAOS.toByteArray());
			FOS.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(MainActivity.this, getString(R.string.fileioerror),
					Toast.LENGTH_LONG).show();
			return "";
		}
		return fileName;
	}

	private void restartAndOpenWith(int width, int height) {
		Intent i = getIntent();
		if (i.hasExtra("file"))
			i.removeExtra("file");
		i.putExtra("width", width);
		i.putExtra("height", height);
		i.putExtra("notfirstrun", true);
		finish();
		startActivity(i);
	}

	private void restartAndOpenWith(File file) {
		Intent i = getIntent();
		if (i.hasExtra("width"))
			i.removeExtra("width");
		if (i.hasExtra("height"))
			i.removeExtra("height");
		i.putExtra("file", file.getPath());
		i.putExtra("notfirstrun", true);
		finish();
		startActivity(i);
	}

	@Override
	public void onBackPressed() {
		exitFullScreen();
		if (drawer.isSecondaryMenuShowing() || drawer.isMenuShowing()) {
			drawer.showContent();
			return;
		}
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}
		doubleBackToExitPressedOnce = true;
		Toast.makeText(this, R.string.backagaintoexit, Toast.LENGTH_SHORT).show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				doubleBackToExitPressedOnce = false;
			}
		}, 2000);
	}

	public savedSettings getSettings() {
		return settings;
	}
}
