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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RemoveUserDialog extends Activity {
	private String[] mUsers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remove_user);
		LinearLayout layout = (LinearLayout)this.findViewById(R.id.LinearLayoutRemoveUser);
		mUsers = fileList();
		int count = 0;
		for (String name : mUsers) {
			if (!name.contains(".")) {
				TextView t = new TextView(this);
				t.setText(name);
				t.setTextSize(24);
				t.setId(count);
				t.setClickable(true);
				t.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String name = mUsers[v.getId()];
						deleteFile(mUsers[v.getId()]);
						deleteFile(mUsers[v.getId()] + "_save.dat");
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						if (prefs.getString(getResources().getString(R.string.pref_key_username), getResources().getString(R.string.username_none)).equals(mUsers[v.getId()])) {
							prefs.edit().putString(getResources().getString(R.string.pref_key_username), getResources().getString(R.string.username_none)).commit();
						}
						String format = getResources().getString(R.string.toast_usernameDeleted);
						String string = String.format(format, name);
						Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
						finish();
					}
				});

				TextView blank = new TextView(this);
				blank.setHeight(24);

				layout.addView(t);
				layout.addView(blank);
				count++;
			}
		}
	}
}
