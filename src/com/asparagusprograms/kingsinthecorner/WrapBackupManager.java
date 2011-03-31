package com.asparagusprograms.kingsinthecorner;

import android.app.backup.BackupManager;
import android.content.Context;

/**
 * Wrapper class for BackupManager.
 * Used to determine if Backup Manager is available before making
 * calls to it in order to preserve backward compatibility.
 */
public class WrapBackupManager {
	private BackupManager mInstance;
	
	 /* class initialization fails when this throws an exception */
	   static {
	       try {
	           Class.forName("android.app.backup.BackupManager");
	       } catch (Exception ex) {
	           throw new RuntimeException(ex);
	       }
	   }
	   
	   /* calling here forces class initialization */
	   public static void checkAvailable() {}
	   
	   public static void dataChanged(String packageName) {
		   BackupManager.dataChanged(packageName);
	   }

	   public WrapBackupManager(Context context) {
	       mInstance = new BackupManager(context);
	   }

	   public void dataChanged() {
	       mInstance.dataChanged();
	   }
}
