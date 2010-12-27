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
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

public class Help extends Activity {
	private ConnectivityManager connMgr;
	private android.net.NetworkInfo networkInfo;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (connectivityCheck()) {
			Intent helpSite = new Intent(Intent.ACTION_VIEW);
			Uri link = Uri.parse("http://code.google.com/p/ap-kings-in-the-corner/wiki/GameplayHelp");
			helpSite.setData(link);
			startActivity(helpSite);
			finish();
		} else {
			showDialog(Main.NO_INTERNET_DIAG);
		}
	}

	private boolean connectivityCheck() {
		connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		networkInfo = connMgr.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Main.NO_INTERNET_DIAG:
			AlertDialog.Builder diagBuilder = new AlertDialog.Builder(this)
			.setMessage(R.string.diag_noInternet)
			.setPositiveButton(R.string.button_showOfflineHelp, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					setContentView(R.layout.help);
					dismissDialog(Main.NO_INTERNET_DIAG);
				}
			})
			.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			diagBuilder.setCancelable(true);
			AlertDialog dialog = diagBuilder.create();
			return dialog;
		}
		return null;
	}
}
