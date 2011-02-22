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

import java.io.Serializable;

public class Player implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7932293913196756290L;
	private String mUsername;
	private int mTotalGames,
				mWins,
				mLosses,
				mWinStreakCur,
				mWinStreakLong;

	// Creates a new player with a given username
	public Player (String name) {
		mUsername = name;
		mTotalGames = 0;
		mWins = 0;
		mLosses = 0;
		mWinStreakCur = 0;
		mWinStreakLong = 0;
	}

	public String getName() {
		return mUsername;
	}


	public int getTotal() {
		return mTotalGames;
	}

	public int getWins() {
		return mWins;
	}

	public int getLosses() {
		return mLosses;
	}

	public int getWinStreakCur() {
		return mWinStreakCur;
	}

	public int getWinStreakLong() {
		return mWinStreakLong;
	}

	public void Win() {
		mTotalGames++;
		mWins++;
		mWinStreakCur++;
		if (mWinStreakCur > mWinStreakLong) {
			mWinStreakLong++;
		}
	}

	public void Lose() {
		mTotalGames++;
		mLosses++;
		mWinStreakCur = 0;
	}
}
