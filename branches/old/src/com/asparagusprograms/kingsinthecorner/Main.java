/**
 * Copyright 2010,2011 Trevor Boyce
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * This is the main menu activity that is launched at application startup.
 */
public class Main extends Activity implements OnClickListener {
	/** Object for intrinsic lock. */
	static final Object[] sDataLock = new Object[0];

	// Static values used by classes and activities
	/** Item ID for creating menus. */
	public static final int MENU_ABOUT = 1,
					 		MENU_HELP = 2,
					 		MENU_PREFS = 3,
					 		MENU_UNDO = 4,
					 		MENU_SORTHAND = 5,
					 		MENU_PLAYAGAIN = 6,
					 		MENU_CHEATS = 7,
					 		MENU_WIN = 8,
					 		MENU_TRASH = 9,
					 		MENU_SHOW_COMPUTER = 10;
	
	/** Item ID for displaying dialogs. */
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
					 		OLD_SAVES_DIAG = 10,
					 		OLD_STATS_ERROR_DIAG = 11;
					 		
	//Global variables
	/** The shared preferences associated with this application's context. */
	private SharedPreferences mPrefs;
	
	/** The width of the button layouts. Used to animate them the correct distance. */
	private int mWidth = 0;
	
	/** A linear layout containing a set of buttons. */
	private LinearLayout mFirstButtons, mSecondButtons;
	
	/** A translate animation used to animate button layouts on and off screen. */
	private TranslateAnimation mOutLeftAnimation, mOutRightAnimation, mInLeftAnimation, mInRightAnimation;
	
	/** True if the button layouts are currently performing an animation, false otherwise. */
	private boolean mIsAnimating = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
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
        Button help = (Button)this.findViewById(R.id.HelpButton);
        help.setOnClickListener(this);
        Button about = (Button)this.findViewById(R.id.AboutButton);
        about.setOnClickListener(this);
        Button more = (Button)this.findViewById(R.id.MoreButton);
        more.setOnClickListener(this);
        Button back = (Button)this.findViewById(R.id.BackButton);
        back.setOnClickListener(this);
        mFirstButtons = (LinearLayout)findViewById(R.id.MainLeftButtons);
        mSecondButtons = (LinearLayout)findViewById(R.id.MainRightButtons);
        mFirstButtons.setVisibility(View.VISIBLE);
        mSecondButtons.setVisibility(View.GONE);
    }
    
    @Override
	protected void onResume() {
    	mPrefs.edit().putBoolean(getResources().getString(R.string.pref_key_inGame), false).commit();    	
    	super.onResume();
	}
    
    @Override
    protected void onPause() {               
        mFirstButtons.setVisibility(View.VISIBLE);
        mSecondButtons.setVisibility(View.GONE);
        super.onPause();
    }

	@Override
	public void onClick(View v) {
		if (mIsAnimating) return;
		switch (v.getId()) {
		case R.id.SinglePlayerButton:
			boolean showUserWarning = mPrefs.getBoolean(getResources().getString(R.string.pref_key_noUserWarning), true);
			
			String noUser = getResources().getString(R.string.username_none);
			String username = mPrefs.getString(getResources().getString(R.string.pref_key_username), noUser);
			
			boolean nullOrNoUser = (username == null || username.equals("") || username.equals(noUser));		
			
			if (showUserWarning && nullOrNoUser) {
				showDialog(SELECT_USER_DIAG);
			} else {
				Intent newGameIntent = new Intent(this, GameActivity.class);
				newGameIntent.putExtra(getResources().getString(R.string.extras_players), 1);
				startActivity(newGameIntent);
			}
			break;
		case R.id.MultiplayerButton:			
			Intent multiplayerIntent = new Intent(this, SelectPlayersActivity.class);
			startActivity(multiplayerIntent);
			break;
		case R.id.StatsButton:
			StatsManager sm = new StatsManager(this);
			if (sm.getPlayerCount() <= 0) {
				showDialog(NO_USERS_DIAG);
			} else {
				Intent statsIntent = new Intent(this, StatisticsActivity.class);
				startActivity(statsIntent);
			}
	        break;
		case R.id.SettingsButton:
			Intent prefsIntent = new Intent(this, Preferences.class);
			startActivity(prefsIntent);
			break;
		case R.id.FirstRunDoneButton:
			dismissDialog(FIRST_RUN_DIAG);
			specialVersionCheck();
			break;
		case R.id.HelpButton:
			Intent helpIntent = new Intent(this, HelpActivity.class);
        	startActivity(helpIntent);
			break;
		case R.id.AboutButton:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
        	startActivity(aboutIntent);
			break;
		case R.id.MoreButton:
			// Initialize the animations if they are not already
			if (mWidth <= 0) {
				mWidth = mFirstButtons.getWidth();
				int duration = 300;
		        
		        mOutLeftAnimation = new TranslateAnimation(0, -mWidth, 0, 0);
		        mOutLeftAnimation.setDuration(duration);
		        
		        mOutRightAnimation = new TranslateAnimation(0, mWidth, 0, 0); 
		        mOutRightAnimation.setDuration(duration);
		        
		        mInLeftAnimation = new TranslateAnimation(mWidth, 0, 0, 0); 
		        mInLeftAnimation.setDuration(duration);
		        
		        mInRightAnimation = new TranslateAnimation(-mWidth, 0, 0, 0); 
		        mInRightAnimation.setDuration(duration);
			}
			
			// Perform the animation to switch to second buttons
			mFirstButtons.startAnimation(mOutLeftAnimation);
			mFirstButtons.setVisibility(View.GONE);
			
            mSecondButtons.startAnimation(mInLeftAnimation);			
			mSecondButtons.setVisibility(View.VISIBLE);
			break;
		case R.id.BackButton:
			// Perform the animation to switch to first buttons
			mSecondButtons.startAnimation(mOutRightAnimation);
			mSecondButtons.setVisibility(View.GONE);
			
			mFirstButtons.startAnimation(mInRightAnimation);			
			mFirstButtons.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NO_USERS_DIAG:				
			AlertDialog.Builder noUsersBuilder = new AlertDialog.Builder(this)
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
			noUsersBuilder.setCancelable(true);
			AlertDialog noUsersDialog = noUsersBuilder.create();
			return noUsersDialog;
		case SELECT_USER_DIAG:				
			AlertDialog.Builder selectUserBuilder = new AlertDialog.Builder(this)
			.setMessage(R.string.diag_noUserGame)
			.setPositiveButton(R.string.button_playGame, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dismissDialog(SELECT_USER_DIAG);
					Intent newGameIntent = new Intent(getBaseContext(), GameActivity.class);
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
			selectUserBuilder.setCancelable(true);
			AlertDialog selectUserDialog = selectUserBuilder.create();
			return selectUserDialog;
		case FIRST_RUN_DIAG:		
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.firstrun, (ViewGroup)findViewById(R.id.FirstRunRoot));
			AlertDialog.Builder firstRunBuilder = new AlertDialog.Builder(this)
			.setView(layout)
			.setCancelable(true)
			.setTitle(getResources().getString(R.string.app_name) + " v" + getResources().getString(R.string.app_version))
			.setIcon(R.drawable.icon);
			Button frButton = (Button)layout.findViewById(R.id.FirstRunDoneButton);
			frButton.setOnClickListener(this);
			AlertDialog firstRunDialog = firstRunBuilder.create();
			return firstRunDialog;
		case OLD_SAVES_DIAG:				
			AlertDialog.Builder oldSavesBuilder = new AlertDialog.Builder(this)
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
			oldSavesBuilder.setCancelable(true);
			AlertDialog oldSavesDialog = oldSavesBuilder.create();
			return oldSavesDialog;
		case OLD_STATS_ERROR_DIAG:
			AlertDialog.Builder oldStatsBuilder = new AlertDialog.Builder(this)
			.setMessage(R.string.diag_oldStats)
			.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog(OLD_STATS_ERROR_DIAG);
				}
			});
			oldStatsBuilder.setCancelable(true);
			AlertDialog oldStatsDialog = oldStatsBuilder.create();
			return oldStatsDialog;
		}
		return null;
	}
	
	/**
	 * Checks to see if the current version is newer than the last run version.
	 * If it is, displays the first run dialog box then calls {@link #specialVersionCheck()}
	 * when it is dismissed. If it is not newer, just call {@link #specialVersionCheck()} 
	 * anyway to be safe. This may not be needed and could possibly be replaced with simply
	 * updating the last run version to the current version here instead.
	 */
	private void versionCheck() {
		// Get the version for this run and the version for the last run
		String lastRunVersion = mPrefs.getString(getResources().getString(R.string.pref_key_lastRunVersion), "0");
        String thisVersion = getResources().getString(R.string.app_version);
        
        // See if this version is newer than the previous version
        if (lastRunVersion.compareTo(thisVersion) < 0) {
        	// If so, show the dialog. Check for special version after dismissing
        	showDialog(FIRST_RUN_DIAG);
        } else {
        	// Otherwise, check for special versions now
        	specialVersionCheck();
        }
	}
	
	/**
	 * Checks the last run version against special versions to handle any
	 * special cases where some action needs to be taken. Updates last run
	 * version to this version after all checks are complete.
	 */
	private void specialVersionCheck() {
		String lastRunVersion = mPrefs.getString(getResources().getString(R.string.pref_key_lastRunVersion), "0");
		String thisVersion = getResources().getString(R.string.app_version);
		
		// Delete saves made prior to version 1.4.2
		if (lastRunVersion.compareTo("1.4.2") < 0) {
    		boolean showDialog = false;
    		for (String file : fileList()) {
				if (file.contains("_save.dat")) {
					deleteFile(file);
					showDialog = true;
				}
			}
    		if (showDialog) showDialog(OLD_SAVES_DIAG);
    	}
		
		// Try to move stats prior to version 1.5.0 into the new format
		if (lastRunVersion.compareTo("1.5.0") < 0) {
			for (String file : fileList()) {
				if (file.contains("_save.dat")) {
					deleteFile(file);
				}
			}
			StatsManager sm = new StatsManager(this);
			if (!sm.combineSplitToSingle()) {
				mPrefs.edit().putString(getResources().getString(R.string.pref_key_username), getResources().getString(R.string.username_none)).commit();
				showDialog(OLD_STATS_ERROR_DIAG);
			}
		}
		
		// Update the last run version to the current version
        mPrefs.edit().putString(getResources().getString(R.string.pref_key_lastRunVersion), thisVersion).commit();
	}
}