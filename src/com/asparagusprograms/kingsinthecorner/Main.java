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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class Main extends Activity implements OnClickListener {
	//Debugging variable
	public static final boolean DEBUG = false;
	
	// Static values used by classes and activities
	public static final int MENU_ABOUT = 1,
					 		MENU_HELP = 2,
					 		MENU_PREFS = 3,
					 		MENU_UNDO = 4,
					 		MENU_SORTHAND = 5,
					 		MENU_PLAYAGAIN = 6,
					 		MENU_CHEATS = 7,
					 		MENU_WIN = 8,
					 		MENU_TRASH = 9;
	
	public static final int NO_USERS_DIAG = 0,
					 		SELECT_USER_DIAG = 1,
					 		NO_INTERNET_DIAG = 2,
					 		TURN_DIAG = 3,
					 		QUIT_DIAG = 4,
					 		NOCARDS_DIAG = 5,
					 		WIN_DIAG = 6,
					 		FIRST_DIAG = 7,
					 		CLEAR_SAVES_DIAG = 8,
					 		FIRST_RUN_DIAG = 9,
					 		OLD_SAVES_DIAG = 10;
					 		
	//Global variables
	private SharedPreferences prefs;
	
    /** Called when the activity is first created */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
        setContentView(R.layout.main);
        
        versionCheck();
        
        Button singlePlayerGame = (Button)this.findViewById(R.id.SinglePlayerButton);
        singlePlayerGame.setOnClickListener(this);
        Button multiplayer = (Button)this.findViewById(R.id.MultiplayerButton);
        multiplayer.setOnClickListener(this);
        Button stats = (Button)this.findViewById(R.id.StatsButton);
        stats.setOnClickListener(this);
        Button settings = (Button)this.findViewById(R.id.SettingsButton);
        settings.setOnClickListener(this);
    }
    
    /** Creates the menu items */
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	menu.add(0, MENU_HELP, 0, R.string.menu_help).setIcon(R.drawable.ic_menu_help);
    	menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(R.drawable.ic_menu_about);
        return true;
    }
    
    /** Handles item selections */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ABOUT:
        	Intent aboutIntent = new Intent(this, About.class);
        	startActivity(aboutIntent);
            return true;
        case MENU_HELP:
        	Intent helpIntent = new Intent(this, Help.class);
        	startActivity(helpIntent);
        	return true;
		case MENU_PREFS:
			Intent prefsIntent = new Intent(this, Preferences.class);
			startActivity(prefsIntent);
			return true;
		}
        return false;
    }

    /** Handles button clicks */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.SinglePlayerButton:
			String noUser = getResources().getString(R.string.username_none);
			String username = prefs.getString(getResources().getString(R.string.pref_key_username), noUser);
			if (username == null || username.equals("") || username.equals(noUser)) {
				if (prefs.getBoolean(getResources().getString(R.string.pref_key_noUserWarning), true)) {
					showDialog(SELECT_USER_DIAG);
				} else {
					Intent newGameIntent = new Intent(this, Game.class);
					newGameIntent.putExtra(getResources().getString(R.string.extras_players), 1);
					startActivity(newGameIntent);
				}
			} else {
				Intent newGameIntent = new Intent(this, Game.class);
				newGameIntent.putExtra(getResources().getString(R.string.extras_players), 1);
				startActivity(newGameIntent);
			}
			break;
		case R.id.MultiplayerButton:			
			Intent multiplayerIntent = new Intent(this, SelectPlayers.class);
			startActivity(multiplayerIntent);
			break;
		case R.id.StatsButton:
			boolean noUsers = true;
			for (String file : fileList()) {
				if (!file.contains(".")) {
					Intent statsIntent = new Intent(this, Stats.class);
					startActivity(statsIntent);
					noUsers = false;
					break;
				}
			}
	        if (noUsers) {
	        	showDialog(NO_USERS_DIAG);
	        }
	        break;
		case R.id.SettingsButton:
			Intent prefsIntent = new Intent(this, Preferences.class);
			startActivity(prefsIntent);
			break;
		case R.id.FirstRunDoneButton:
			dismissDialog(FIRST_RUN_DIAG);
			break;
		default:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NO_USERS_DIAG:				
			AlertDialog.Builder diagBuilder = new AlertDialog.Builder(this)
			.setMessage(R.string.diag_noUserStats)
			.setPositiveButton(R.string.button_createUser, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dismissDialog(NO_USERS_DIAG);
					Intent prefsIntent = new Intent(getBaseContext(), Preferences.class);
					startActivity(prefsIntent);
				}
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog(NO_USERS_DIAG);
				}
			});
			diagBuilder.setCancelable(true);
			AlertDialog dialog = diagBuilder.create();
			return dialog;
		case SELECT_USER_DIAG:				
			AlertDialog.Builder diagBuilder2 = new AlertDialog.Builder(this)
			.setMessage(R.string.diag_noUserGame)
			.setPositiveButton(R.string.button_playGame, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dismissDialog(SELECT_USER_DIAG);
					Intent newGameIntent = new Intent(getBaseContext(), Game.class);
					newGameIntent.putExtra(getResources().getString(R.string.extras_players), 1);
					startActivity(newGameIntent);
				}
			})
			.setNegativeButton(R.string.button_selectUser, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog(SELECT_USER_DIAG);
					Intent prefsIntent = new Intent(getBaseContext(), Preferences.class);
					startActivity(prefsIntent);
				}
			});
			diagBuilder2.setCancelable(true);
			AlertDialog dialog2 = diagBuilder2.create();
			return dialog2;
		case FIRST_RUN_DIAG:
			AlertDialog.Builder builder;
			AlertDialog alertDialog;
			Context context = this;
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.firstrun, (ViewGroup)findViewById(R.id.FirstRunRoot));
			builder = new AlertDialog.Builder(context);
			builder.setView(layout);
			builder.setCancelable(true);
			builder.setTitle(getResources().getString(R.string.app_name) + " v" + getResources().getString(R.string.app_version));
			builder.setIcon(R.drawable.icon);
			Button frButton = (Button)layout.findViewById(R.id.FirstRunDoneButton);
			frButton.setOnClickListener(this);
			alertDialog = builder.create();
			return alertDialog;
		case OLD_SAVES_DIAG:				
			AlertDialog.Builder diagBuilder3 = new AlertDialog.Builder(this)
			.setMessage(R.string.diag_oldSaves)
			.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					for (String file : fileList()) {
						if (file.contains("_save.dat")) {
							deleteFile(file);
						}
					}
					dismissDialog(OLD_SAVES_DIAG);
				}
			});
			diagBuilder3.setCancelable(true);
			AlertDialog dialog3 = diagBuilder3.create();
			return dialog3;
		}
		return null;
	}
	
	private void versionCheck() {
		// Get the version for this run and the version for the last run
		String lastRunVersion = prefs.getString(getResources().getString(R.string.pref_key_lastRunVersion), "0");
        String thisVersion = getResources().getString(R.string.app_version);
        
        // See if this version is older than the previous version
        if (lastRunVersion.compareTo(thisVersion) < 0 || DEBUG) {
        	// If so, show the dialog
        	showDialog(FIRST_RUN_DIAG);
        	
        	// Do checks for special versions
        	if (lastRunVersion.compareTo("1.4.2") < 0) {
        		// Delete saves made prior to version 1.4.2
        		boolean showDialog = false;
        		for (String file : fileList()) {
					if (file.contains("_save.dat")) {
						deleteFile(file);
						showDialog = true;
					}
				}
        		if (showDialog) showDialog(OLD_SAVES_DIAG);
        	}
        }
        // Update the last run version to the current version
        prefs.edit().putString(getResources().getString(R.string.pref_key_lastRunVersion), thisVersion).commit();
	}
}