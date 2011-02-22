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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Stats extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats);
		initialize();
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.stats);
		initialize();
	}



	public void initialize() {
		String[] users = fileList();
		for (String name : users) {
			if (!name.contains(".")) {
				addStats(name);
			}
		}
	}

	private void addStats(String user) {
		try {
			FileInputStream fis = openFileInput(user);
			ObjectInputStream in = new ObjectInputStream(fis);
			Player p = (Player)in.readObject();
			in.close();
			fis.close();

			TextView name = new TextView(this);
			name.setText(user);
			
			TextView total = new TextView(this);
			String format = (getResources().getString(R.string.stats_games));
			int games = p.getTotal();
			total.setText(String.format(format, games));
			
			TextView win = new TextView(this);
			format = (getResources().getString(R.string.stats_won));
			DecimalFormat df = new DecimalFormat("0.#");
			int wins = p.getWins();
			double percentWins = 
				(wins > 0 && games > 0) ? (double)wins/(double)games*100 : 0;
			win.setText(String.format(format, wins, df.format(percentWins)));
			
			TextView lose = new TextView(this);
			int losses = p.getLosses();
			double percentLosses = 
				(losses > 0 && games > 0) ? (double)losses/(double)games*100 : 0;
			format = (getResources().getString(R.string.stats_lost));
			lose.setText(String.format(format, losses, df.format(percentLosses)));
			
			TextView curStreak = new TextView(this);
			format = (getResources().getString(R.string.stats_cstreak));
			curStreak.setText(String.format(format, p.getWinStreakCur()));
			
			TextView longStreak = new TextView(this);
			format = (getResources().getString(R.string.stats_lstreak));
			longStreak.setText(String.format(format, p.getWinStreakLong()));

			LayoutParams lp = new LayoutParams(
			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			name.setLayoutParams(lp);
			name.setTextColor(Color.WHITE);
			name.setTextSize(24);
			name.setPadding(0, 0, 0, 2);
			name.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);

			total.setLayoutParams(lp);
			total.setTextSize(20);
			total.setBackgroundColor(getResources().getColor(R.color.bg_gray));
			total.setPadding(0, 0, 0, 4);

			win.setLayoutParams(lp);
			win.setTextSize(20);
			win.setBackgroundColor(getResources().getColor(R.color.bg_dkgray));
			win.setPadding(0, 0, 0, 4);

			lose.setLayoutParams(lp);
			lose.setTextSize(20);
			lose.setBackgroundColor(getResources().getColor(R.color.bg_gray));
			lose.setPadding(0, 0, 0, 4);

			curStreak.setLayoutParams(lp);
			curStreak.setTextSize(20);
			curStreak.setBackgroundColor(getResources().getColor(R.color.bg_dkgray));
			curStreak.setPadding(0, 0, 0, 4);

			longStreak.setLayoutParams(lp);
			longStreak.setTextSize(20);
			longStreak.setBackgroundColor(getResources().getColor(R.color.bg_gray));
			longStreak.setPadding(0, 0, 0, 4);

			LinearLayout layout = (LinearLayout)this.findViewById(R.id.LinearLayoutStats);
			layout.addView(name);
			layout.addView(total);
			layout.addView(win);
			layout.addView(lose);
			layout.addView(curStreak);
			layout.addView(longStreak);

			/*
			TextView separator = new TextView(this);
			separator.setLayoutParams(lp);
			separator.setBackgroundColor(Color.WHITE);
			separator.setHeight(1);
			linear.addView(separator);
			*/

			TextView blank = new TextView(this);
			blank.setLayoutParams(lp);
	//		blank.setBackgroundColor(getResources().getColor(R.color.bg_dkgrey));
			blank.setHeight(15);
			layout.addView(blank);
			
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "Error 1", Toast.LENGTH_SHORT).show();
		} catch (StreamCorruptedException e) {
			Toast.makeText(this, "Error 2", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, "Error 3", Toast.LENGTH_SHORT).show();
		} catch (ClassNotFoundException e) {
			Toast.makeText(this, "Error 4", Toast.LENGTH_SHORT).show();
		}
	}
}
