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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final int CHEATS_ALL = 0,
							CHEATS_WIN = 1,
							CHEATS_TRASH = 2,
							CHEATS_COMPUTER_HAND = 3;
	
	private SharedPreferences mPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		addPreferencesFromResource(R.xml.preferences);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		// Set up Report a Bug
		PreferenceScreen bugReport = (PreferenceScreen) findPreference(getResources().getString(R.string.pref_key_email));
		bugReport.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String model = Build.MANUFACTURER + " " + Build.PRODUCT + " (" + Build.MODEL + ")";
				String version = Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT +")";
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO, Uri.parse(getResources().getString(R.string.link_email)));
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.subject_email));
				String subject = String.format(getResources().getString(R.string.body_email), getResources().getString(R.string.app_version), model, version);
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, subject);
	            startActivity(Intent.createChooser(emailIntent, "Send email using:"));
				return true;
			}	
		});
		// Set up the check for updates button
		PreferenceScreen update = (PreferenceScreen)findPreference(getResources().getString(R.string.pref_key_update));
		update.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent updateIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.link_market)));
				updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(updateIntent);
				return false;
			}
		});
		// Set up the remove cheats button
		PreferenceScreen removeCheats = (PreferenceScreen)findPreference(getResources().getString(R.string.pref_key_disableCheats));
		removeCheats.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				disableCheats();
				return false;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		update();
	}
	
	private void update() {
		// Enable/disable check for updates
		PreferenceScreen updateCheck = (PreferenceScreen) findPreference(getResources().getString(R.string.pref_key_update));
		boolean inGame = mPrefs.getBoolean(getResources().getString(R.string.pref_key_inGame), false);
		updateCheck.setEnabled(!inGame);
		
		// Enable/disable user settings
		PreferenceScreen userSettings = (PreferenceScreen) findPreference(getResources().getString(R.string.pref_key_userSettings));
		userSettings.setEnabled(!inGame);
		
		// Hide locked cheats
		PreferenceCategory cheatCategory = (PreferenceCategory) findPreference(getResources().getString(R.string.pref_key_cheatsCategory));
		boolean cheatsWin = mPrefs.getBoolean(getResources().getString(R.string.pref_key_cheatsWinUnlocked), false);
		boolean cheatsTrash = mPrefs.getBoolean(getResources().getString(R.string.pref_key_cheatsTrashUnlocked), false);
		boolean cheatsComputerHand = mPrefs.getBoolean(getResources().getString(R.string.pref_key_cheatsComputerHandUnlocked), false);
		if (!cheatsWin) {
			CheckBoxPreference win = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_key_cheatsWin));
			if (win != null) cheatCategory.removePreference(win);
		}
		if (!cheatsTrash) {
			CheckBoxPreference trash = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_key_cheatsTrash));
			if (trash != null) cheatCategory.removePreference(trash);
		}
		if (!cheatsComputerHand) {
			CheckBoxPreference computerHand = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_key_cheatsComputerHand));
			if (computerHand != null) cheatCategory.removePreference(computerHand);
		}
		
		boolean cheatsUnlocked = (cheatsWin || cheatsTrash || cheatsComputerHand);
		if (!cheatsUnlocked) {
			PreferenceScreen disableCheats = (PreferenceScreen) findPreference(getResources().getString(R.string.pref_key_disableCheats));
			if (disableCheats != null) cheatCategory.removePreference(disableCheats);
		}
		
		//Set up clear saves
		PreferenceScreen clearSaves = (PreferenceScreen) findPreference(getResources().getString(R.string.pref_key_clearSaves));
		clearSaves.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showDialog(Main.CLEAR_SAVES_DIAG);
				return true;
			}	
		});	
		clearSaves.setEnabled(checkForSaves());
		
		// Set up the Username preference
		Preference pref = findPreference(getResources().getString(R.string.pref_key_username));
		ListPreference userPref = (ListPreference) pref;
		String[] files = fileList();
		int usersCount = 0;
		for (int i = 0; i < files.length; i++) {
			if (!files[i].contains(".")){
				usersCount++;
			}
		}
		String[] users = new String[usersCount];
		String[] userList = new String[users.length+1];
		userList[0] = getResources().getString(R.string.username_none);
		int curCount = 1;
		for (int i = 0; i < files.length; i ++) {
			if (!files[i].contains(".")) {
				userList[curCount] = files[i];
				curCount++;
			}
		}
		if (users.length <= 0) {
			userPref.setEnabled(false);
		} else {
			userPref.setEnabled(true);
		}
		userPref.setEntries(userList);
		userPref.setEntryValues(userList);

		String name = mPrefs.getString(getResources().getString(R.string.pref_key_username), getResources().getString(R.string.username_none));
		if (name != null && !name.equals("") && !name.equals(getResources().getString(R.string.username_none).toString())) {
			userPref.setValue(name);
			userPref.setSummary(name);
		} else {
			userPref.setValue(getResources().getString(R.string.username_none));
			userPref.setSummary(getResources().getString(R.string.pref_summary_username));
		}
		clearTextPrefs();
	}
	
	private void clearTextPrefs() {
		// Clear the New User preference
		Preference pref = findPreference(getResources().getString(R.string.pref_key_newUser));
		EditTextPreference newUserPref = (EditTextPreference) pref;
		newUserPref.setText("");
		newUserPref.getEditText().setSingleLine();
		
		// Clear the Enter Code preference
		pref = findPreference(getResources().getString(R.string.pref_key_enterCode));
		EditTextPreference enterCodePref = (EditTextPreference) pref;
		enterCodePref.setText("");
		enterCodePref.getEditText().setSingleLine();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	    Preference pref = findPreference(key);
	    
	    if (pref instanceof EditTextPreference) {
	    	EditTextPreference editText = (EditTextPreference) pref;
	    	String text = editText.getText().toString();
	    	if (key.equals(getResources().getString(R.string.pref_key_newUser))) {
	    		checkName(text);
	    	} else if (key.equals(getResources().getString(R.string.pref_key_enterCode))) {
	    		enterCode(text);
	    	}        
	    }
	    update();
	}
	
	private void checkName(String name) {
		if (name != null) {
	        if (name.length() > 25) {
				Toast.makeText(this, R.string.toast_usernameLong, Toast.LENGTH_SHORT).show();
	        } else if (name.length() > 0 && name.length() < 3) {
				Toast.makeText(this, R.string.toast_usernameShort, Toast.LENGTH_SHORT).show();
	        } else if (name.length() > 0 && !Character.isLetter(name.charAt(0)) ) {
				Toast.makeText(this, R.string.toast_usernameStart, Toast.LENGTH_SHORT).show();
	        } else if (name.toString().toLowerCase().equals("none")) {
	        	Toast.makeText(this, R.string.toast_usernameInvalid, Toast.LENGTH_SHORT).show();
	        } else if (name.toString().contains(".")) {
	        	Toast.makeText(this, R.string.toast_usernamePeriod, Toast.LENGTH_SHORT).show();
	        } else if (!name.equals("") && !name.equals(getResources().getString(R.string.username_none))) {
	        	addUser(name);
	        }
        }
	}
	
	private void addUser(String name) {	
		Boolean dupe = false;
		for (String file : fileList()) {
			if (file.toLowerCase().equals(name.toLowerCase())) {
				dupe = true;
			}
		}

		if (!dupe) {
			try {
			Player p = new Player(name);
			FileOutputStream fos = openFileOutput(name, Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(p);
			out.close();
			fos.close();
			} catch (FileNotFoundException e) {
				Toast.makeText(this, getResources().getString(R.string.toast_fileNotFoundException), Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Toast.makeText(this, getResources().getString(R.string.toast_IOException), Toast.LENGTH_SHORT).show();
			}
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			prefs.edit().putString(getResources().getString(R.string.pref_key_username), name).commit();
			update();
			String format = getResources().getString(R.string.toast_usernameCreated);
			String string = String.format(format, name);
			Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.toast_usernameExists, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Main.CLEAR_SAVES_DIAG:
			AlertDialog.Builder diagBuilder = new AlertDialog.Builder(this)
			.setMessage(R.string.diag_clearSaves)
			.setPositiveButton(R.string.button_clearSaves, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					for (String file : fileList()) {
						if (file.contains("_save.dat")) {
							deleteFile(file);
						}
					}
					Toast.makeText(getApplicationContext(), R.string.toast_savesCleared, Toast.LENGTH_SHORT).show();
					update();
					dismissDialog(Main.CLEAR_SAVES_DIAG);
				}
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog(Main.CLEAR_SAVES_DIAG);
				}
			});
			diagBuilder.setCancelable(true);
			AlertDialog dialog = diagBuilder.create();
			return dialog;
		}
		return null;
	}
	
	private boolean checkForSaves() {
		for (String file : fileList()) {
			if (file.contains("_save.dat")) {
				return true;
			}
		}
		return false;
	}
	
	private void enterCode(String code) {
		boolean cheatsWin = mPrefs.getBoolean(getResources().getString(R.string.pref_key_cheatsWinUnlocked), false);
		boolean cheatsTrash = mPrefs.getBoolean(getResources().getString(R.string.pref_key_cheatsTrashUnlocked), false);
		boolean cheatsComputerHand = mPrefs.getBoolean(getResources().getString(R.string.pref_key_cheatsComputerHandUnlocked), false);
		
		boolean cheatsAll = (cheatsWin && cheatsTrash && cheatsComputerHand);
		
		if (!cheatsAll && code.equals(getResources().getString(R.string.pref_key_cheatsAllString))) {
			Toast.makeText(this, R.string.toast_cheatsAllEnabled, Toast.LENGTH_SHORT).show();
			enableCheats(CHEATS_ALL);
		} else if (!cheatsWin && code.equals(getResources().getString(R.string.pref_key_cheatsWinString))) {
			Toast.makeText(this, R.string.toast_cheatsWinEnabled, Toast.LENGTH_SHORT).show();
			enableCheats(CHEATS_WIN);
		} else if (!cheatsTrash && code.equals(getResources().getString(R.string.pref_key_cheatsTrashString))) {
			Toast.makeText(this, R.string.toast_cheatsTrashEnabled, Toast.LENGTH_SHORT).show();
			enableCheats(CHEATS_TRASH);
		} else if (!cheatsComputerHand && code.equals(getResources().getString(R.string.pref_key_cheatsComputerHandString))) {
			Toast.makeText(this, R.string.toast_cheatsComputerHandEnabled, Toast.LENGTH_SHORT).show();
			enableCheats(CHEATS_COMPUTER_HAND);
		}
	}

	private void enableCheats(int cheatValue) {
		switch(cheatValue) {
		case CHEATS_ALL:
			mPrefs.edit()
			.putBoolean(getResources().getString(R.string.pref_key_cheatsWinUnlocked), true)
			.putBoolean(getResources().getString(R.string.pref_key_cheatsTrashUnlocked), true)
			.putBoolean(getResources().getString(R.string.pref_key_cheatsComputerHandUnlocked), true)
			.commit();
			break;
		case CHEATS_WIN:
			mPrefs.edit().putBoolean(getResources().getString(R.string.pref_key_cheatsWinUnlocked), true).commit();
			break;
		case CHEATS_TRASH:
			mPrefs.edit().putBoolean(getResources().getString(R.string.pref_key_cheatsTrashUnlocked), true).commit();
			break;
		case CHEATS_COMPUTER_HAND:
			mPrefs.edit().putBoolean(getResources().getString(R.string.pref_key_cheatsComputerHandUnlocked), true).commit();
			break;
		}
		Intent prefsIntent = this.getIntent();
		startActivity(prefsIntent);
		finish();
	}
	
	private void disableCheats() {
		mPrefs.edit()
		.putBoolean(getResources().getString(R.string.pref_key_cheatsWinUnlocked), false)
		.putBoolean(getResources().getString(R.string.pref_key_cheatsTrashUnlocked), false)
		.putBoolean(getResources().getString(R.string.pref_key_cheatsComputerHandUnlocked), false)
		.commit();
	}
}