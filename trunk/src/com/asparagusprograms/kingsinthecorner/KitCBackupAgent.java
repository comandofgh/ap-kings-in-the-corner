package com.asparagusprograms.kingsinthecorner;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

public class KitCBackupAgent extends BackupAgentHelper {
	public static final String SHARED_PREFS_KEY = "shared_prefs";
	public static final String STATS_KEY = "stats";
	
	public void onCreate() {
		SharedPreferencesBackupHelper prefsHelper = new SharedPreferencesBackupHelper(this, Preferences.SHARED_PREFS_NAME);
		addHelper(SHARED_PREFS_KEY, prefsHelper);
		
		FileBackupHelper fileHelper = new FileBackupHelper(this, StatsManager.STATS_FILE_NAME);
		addHelper(STATS_KEY, fileHelper);
	}
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		// Hold the lock while the FileBackupHelper performs backup
		synchronized (Main.sDataLock) {
			super.onBackup(oldState, data, newState);
		}
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		// Hold the lock while the FileBackupHelper restores the file
		synchronized (Main.sDataLock) {			
			super.onRestore(data, appVersionCode, newState);
		}
	}
}
