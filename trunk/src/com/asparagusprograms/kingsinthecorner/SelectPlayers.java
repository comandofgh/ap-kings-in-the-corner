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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelectPlayers extends Activity implements OnClickListener {

	/** Called when the activity is first created */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.players);
		Button twoPlayers = (Button)this.findViewById(R.id.TwoPlayers);
		twoPlayers.setOnClickListener(this);
		Button threePlayers = (Button)this.findViewById(R.id.ThreePlayers);
		threePlayers.setOnClickListener(this);
		Button fourPlayers = (Button)this.findViewById(R.id.FourPlayers);
		fourPlayers.setOnClickListener(this);
	}

	/** Handles button clicks */
	@Override
	public void onClick(View v) {
		Intent newGameIntent = new Intent(this, Game.class);
		switch(v.getId()) {
			case R.id.TwoPlayers:
				newGameIntent.putExtra(getResources().getString(R.string.extras_players), 2);
				break;
			case R.id.ThreePlayers:
				newGameIntent.putExtra(getResources().getString(R.string.extras_players), 3);
				break;
			case R.id.FourPlayers:
				newGameIntent.putExtra(getResources().getString(R.string.extras_players), 4);
				break;
			default:
				break;
		}
		startActivity(newGameIntent);
		finish();
	}
	
	/** Creates the menu items */
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	menu.add(0, Main.MENU_PREFS, 0, getResources().getString(R.string.menu_prefs)).setIcon(R.drawable.ic_menu_preferences);
    	menu.add(0, Main.MENU_HELP, 0, getResources().getString(R.string.menu_help)).setIcon(R.drawable.ic_menu_help);
    	menu.add(0, Main.MENU_ABOUT, 0, getResources().getString(R.string.menu_about)).setIcon(R.drawable.ic_menu_about);
        return true;
    }
    
    /** Handles item selections */
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
		case Main.MENU_PREFS:
			Intent prefsIntent = new Intent(this, Preferences.class);
			startActivity(prefsIntent);
			return true;
        }
        return false;
    }
}
