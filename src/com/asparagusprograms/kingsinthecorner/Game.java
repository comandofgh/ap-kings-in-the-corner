/**
 * Copyright 2010 Trevor Boyce
 * 
 * This file is part of Kings in the Corner.
 *
 *    Kings in the Corner is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Kings in the Corner is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Kings in the Corner.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.asparagusprograms.kingsinthecorner;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

public class Game extends Activity implements Observer {	
	// Global variables
	private GameEngine mGameEngine;
	private boolean mFirstYourTurn;
	private SharedPreferences mPrefs;
	
	/** Called when the activity is first created **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initialize variables
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		mFirstYourTurn = true;
		// Initialize the CardTableView
		setContentView(R.layout.table);
		CardTableView table = (CardTableView)this.findViewById(R.id.Table);
		table.setFocusableInTouchMode(true);
		// Creates the game engine
		mGameEngine = new GameEngine(this, getIntent().getExtras().getInt(getResources().getString(R.string.extras_players)), table);
		mGameEngine.addObserver(this);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mGameEngine.screenHeight(metrics.heightPixels);	
		table.setGameEngine(mGameEngine);
		//Start a new game or restore a saved one
		if (mGameEngine.restore()) {
			showToast(getResources().getString(R.string.toast_gameRestored));
			if (mGameEngine.turn() == 0) {showToast(getResources().getString(R.string.toast_yourTurn)); mFirstYourTurn = false;}
		} else {
			if (mPrefs.getBoolean(getResources().getString(R.string.pref_key_firstGame), true)) {
				showDialog(Main.FIRST_DIAG);
				mGameEngine.firstGame();
			}
			mGameEngine.newGame();
		}
		mGameEngine.inGame(true);
	}
	
	/** Called when the activity is being resumed or after onCreate **/
	@Override
	protected void onResume() {
		super.onResume();
		// Update user preferences
		mGameEngine.updatePrefs();
		
		// Run last to ensure everything is set up properly first
		mGameEngine.resume();
	}
	
	/** Called when the activity is being sent to the background **/
	@Override
	protected void onPause() {
		mGameEngine.pause();
		super.onPause();
	}
	
	/** Called to finish the application **/
	@Override
	public void finish() {
		mGameEngine.stop();
		mGameEngine.freeUp();
		super.finish();
	}
	
	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	// Methods for handling key presses	
	/** Back button with support for Android 1.6) **/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if (mGameEngine == null || !mGameEngine.drawInitialized()) return true; // Do not exit unless the game is completely initialized
			mGameEngine.pause();
			if (mGameEngine.winner() == -1) {
				if (mGameEngine.isSinglePlayer() && mPrefs.getBoolean(getResources().getString(R.string.pref_key_autosave), false)) {
					finish();
				} else {				
					showDialog(Main.QUIT_DIAG);
				}
			} else {
				finish();
			}
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	/** Handles the menu key press **/
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (mGameEngine.winner() != -1) {
			menu.add(0, Main.MENU_PLAYAGAIN, 0, R.string.menu_playAgain).setIcon(R.drawable.ic_menu_newgame);
		} else if ( (mGameEngine.isSinglePlayer() && mGameEngine.turn() == 0) || !mGameEngine.isSinglePlayer()){
			if (mGameEngine.isSinglePlayer() && mGameEngine.canUndo()) {
				menu.add(0, Main.MENU_UNDO, 0, R.string.menu_undo).setIcon(R.drawable.ic_menu_undo);
			} else if (mGameEngine.isSinglePlayer()){
				menu.add(0, Main.MENU_UNDO, 0, R.string.menu_undoDisabled).setIcon(R.drawable.ic_menu_undodisabled);
			}
			if (mGameEngine.handSorted()) {
				menu.add(0, Main.MENU_SORTHAND, 0, R.string.menu_sortHandStop).setIcon(R.drawable.ic_menu_cards);
			} else {
				menu.add(0, Main.MENU_SORTHAND, 0, R.string.menu_sortHand).setIcon(R.drawable.ic_menu_cardsdisabled);
			}
		}
		menu.add(0, Main.MENU_PREFS, 0, R.string.menu_prefs).setIcon(R.drawable.ic_menu_preferences);
		menu.add(0, Main.MENU_HELP, 0, R.string.menu_help).setIcon(R.drawable.ic_menu_help);
		menu.add(0, Main.MENU_ABOUT, 0, R.string.menu_about).setIcon(R.drawable.ic_menu_about);
		if (mGameEngine.isSinglePlayer() && mGameEngine.winner() == -1 && mGameEngine.turn() == 0 && mPrefs.getBoolean(getResources().getString(R.string.pref_key_cheatsEnabled), false)) {
			SubMenu cheats = menu.addSubMenu(R.string.menu_cheats);
			cheats.setIcon(R.drawable.ic_menu_more);
			cheats.add(0, Main.MENU_WIN, 0, R.string.menu_win);
			cheats.add(0, Main.MENU_TRASH, 0, R.string.menu_trash);
		}
		return true;
	}
	
	/** Handles menu item selections **/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Main.MENU_ABOUT:
			Intent aboutIntent = new Intent(this, About.class);
			startActivity(aboutIntent);
			return true;
		case Main.MENU_HELP:
			Intent helpIntent = new Intent(this, Help.class);
			startActivity(helpIntent);
			return true;
		case Main.MENU_UNDO:
			if (mGameEngine.canUndo()) mGameEngine.undo();
			return true;
		case Main.MENU_SORTHAND:
			mGameEngine.sortHand();
			return true;
		case Main.MENU_PLAYAGAIN:
			Intent playAgainIntent = this.getIntent();
			startActivity(playAgainIntent);
			finish();
			return true;
		case Main.MENU_PREFS:
			Intent prefsIntent = new Intent(this, Preferences.class);
			startActivity(prefsIntent);
			return true;
		case Main.MENU_WIN:
			mGameEngine.autoWin();
			return true;
		case Main.MENU_TRASH:
			mGameEngine.trash();
			return true;
		}
		return false;
	}
	
	/** Called when a Dialog is created **/
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Main.TURN_DIAG:				
			AlertDialog.Builder turnBuilder = new AlertDialog.Builder(this);
			String turnFormat = getResources().getString(R.string.diag_playerTurn);
			String turnString = String.format(turnFormat, mGameEngine.turn()+1);
			turnBuilder.setMessage(turnString);
			turnBuilder.setPositiveButton(R.string.button_continue, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					removeDialog(Main.TURN_DIAG);
					mGameEngine.hideHand(false);
				}
			});
			turnBuilder.setCancelable(false);
			AlertDialog turnDialog = turnBuilder.create();
			return turnDialog;
		case Main.NOCARDS_DIAG:				
			AlertDialog.Builder noCardsBuilder = new AlertDialog.Builder(this);
			noCardsBuilder.setMessage(R.string.diag_deckEmpty)
			.setCancelable(true)
			.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dismissDialog(Main.NOCARDS_DIAG);
				}
			});
			AlertDialog noCardsDialog = noCardsBuilder.create();
			return noCardsDialog;
		case Main.FIRST_DIAG:				
			AlertDialog.Builder firstBuilder = new AlertDialog.Builder(this);
			firstBuilder.setMessage(R.string.diag_firstTime)
			.setCancelable(true)
			.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dismissDialog(Main.FIRST_DIAG);
				}
			});
			AlertDialog firstDialog = firstBuilder.create();
			return firstDialog;
		case Main.QUIT_DIAG:				
			AlertDialog.Builder quitBuilder = new AlertDialog.Builder(this);
			if (mGameEngine.isSinglePlayer()) {
				quitBuilder.setMessage(R.string.diag_quitSingle)
				.setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						mGameEngine.resume();
					}
				})
				.setPositiveButton(R.string.button_saveYes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						mGameEngine.save();
						finish();
					}
				})
				.setNegativeButton(R.string.button_saveNo, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mGameEngine.isSinglePlayer()) {
							mGameEngine.deleteSave();
							finish();
						} else {
							mGameEngine.resume();
							dismissDialog(Main.QUIT_DIAG);
						}
					}
				});
			}
			else {
				quitBuilder.setMessage(R.string.diag_quitMulti)
				.setPositiveButton(R.string.button_quitYes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				})
				.setNegativeButton(R.string.button_quitNo, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(Main.QUIT_DIAG);
					}
				});
			}
			AlertDialog quitDialog = quitBuilder.create();
			return quitDialog;
		case Main.WIN_DIAG:
			AlertDialog.Builder winBuilder = new AlertDialog.Builder(this);
			if (mGameEngine.isSinglePlayer() && mGameEngine.winner() == 1) {
				winBuilder.setMessage(R.string.diag_youLose);
			} else {
				if (mGameEngine.isSinglePlayer()) {
					winBuilder.setMessage(R.string.diag_youWin);
				} else {
					String winFormat = getResources().getString(R.string.diag_playerWin);
					String winString = String.format(winFormat, mGameEngine.turn()+1);
					winBuilder.setMessage(winString);
				}
			}
			winBuilder.setCancelable(true)
			.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					removeDialog(Main.WIN_DIAG);
				}
			});
			AlertDialog winDialog = winBuilder.create();
			mGameEngine.deleteSave();
			return winDialog;
		}
		return null;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable == mGameEngine) {
			if (mGameEngine.winner() == -1) {
				if (mGameEngine.turn() >= 0) {
					if ((mGameEngine.isSinglePlayer() && mGameEngine.turn() == 0 && ((mPrefs.getInt(getResources().getString(R.string.pref_key_computerDelay), 1000) > 100) || mFirstYourTurn))) {
						mFirstYourTurn = false;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showToast(getResources().getString(R.string.toast_yourTurn));
							}
						});
					} else if (!mGameEngine.isSinglePlayer()) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showDialog(Main.TURN_DIAG);
							}
						});
					}
				}
				if (mGameEngine.warnEmpty()) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showDialog(Main.NOCARDS_DIAG);
						}
					});
				}
			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showDialog(Main.WIN_DIAG);
					}
				});
			}
		}			
	}
}
