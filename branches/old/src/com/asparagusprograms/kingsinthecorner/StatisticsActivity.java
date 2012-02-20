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

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatisticsActivity extends Activity {	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats);		
		initialize();
	}

	/** Gets the stored player stats from the {@link StatsManager}
	 * and calls <code>addStats(PlayerStats playerStats)</code>
	 * for each set of stats.
	 */
	public void initialize() {
		StatsManager sm = new StatsManager(this);
		ArrayList<PlayerStats> players = sm.getPlayerList();
		for (PlayerStats playerStats : players) {
			addStats(playerStats);
		}
	}

	
	
	/**
	 * Adds the views to dispay a player's stats.
	 * @param playerStats the player's stats to add.
	 */
	private void addStats(PlayerStats playerStats) {
		if (playerStats == null) return;
		TextView name = new TextView(this);
		name.setText(playerStats.getName());

		TextView total = new TextView(this);
		String format = (getResources().getString(R.string.stats_games));
		int games = playerStats.getTotal();
		total.setText(String.format(format, games));

		TextView win = new TextView(this);
		format = (getResources().getString(R.string.stats_won));
		DecimalFormat df = new DecimalFormat("0.#");
		int wins = playerStats.getWins();
		double percentWins = 
			(wins > 0 && games > 0) ? (double)wins/(double)games*100 : 0;
		win.setText(String.format(format, wins, df.format(percentWins)));

		TextView lose = new TextView(this);
		int losses = playerStats.getLosses();
		double percentLosses = 
			(losses > 0 && games > 0) ? (double)losses/(double)games*100 : 0;
			format = (getResources().getString(R.string.stats_lost));
		lose.setText(String.format(format, losses, df.format(percentLosses)));

		TextView curStreak = new TextView(this);
		format = (getResources().getString(R.string.stats_cstreak));
		curStreak.setText(String.format(format, playerStats.getWinStreakCur()));

		TextView longStreak = new TextView(this);
		format = (getResources().getString(R.string.stats_lstreak));
		longStreak.setText(String.format(format, playerStats.getWinStreakLong()));

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

		LinearLayout layout = (LinearLayout)findViewById(R.id.LinearLayoutStats);
		layout.addView(name);
		layout.addView(total);
		layout.addView(win);
		layout.addView(lose);
		layout.addView(curStreak);
		layout.addView(longStreak);

		TextView blank = new TextView(this);
		blank.setLayoutParams(lp);
		blank.setHeight(15);
		layout.addView(blank);
	}
}
