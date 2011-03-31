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

/**
 * Holds information about the stats associated
 * with a particular user name.
 */
public class PlayerStats {

	/** The user name associated with this player's stats. */
	private String mUsername;
	
	/** The number of games this player has played. */
	private int mTotalGames;
	
	/** The number of games this player has won. */
	private int	mWins;
	
	/** The number of games this player has lost. */
	private int	mLosses;
	
	/** The current win streak for this player. */
	private int	mWinStreakCur;
	
	/** The longest win streak this player has reached. */
	private int	mWinStreakLong;

	/**
	 * Constructs a new set of player stats with the supplied user name.
	 * @param name The user name to associate with this player's stats.
	 */
	public PlayerStats(String name) {
		mUsername = name;
		mTotalGames = 0;
		mWins = 0;
		mLosses = 0;
		mWinStreakCur = 0;
		mWinStreakLong = 0;
	}
	
	/**
	 * Constructs a new set of player stats with the supplied user name and stats.
	 * @param name The user name to associate with this player's stats.
	 * @param totalGames The total number of games this player has played.
	 * @param wins The number of games this player has won.
	 * @param losses The number of games this player has lost.
	 * @param winStreakCur The current win streak for this player.
	 * @param winStreakLong The longest win streak this player has reached.
	 */
	public PlayerStats(String name, int totalGames, int wins, int losses, int winStreakCur, int winStreakLong) {
		mUsername = name;
		mTotalGames = totalGames;
		mWins = wins;
		mLosses = losses;
		mWinStreakCur = winStreakCur;
		mWinStreakLong = winStreakLong;
	}

	/**
	 * Gets the user name associated with this player's stats.
	 * @return The associated user name as a string.
	 */
	public String getName() {
		return mUsername;
	}

	/**
	 * Gets the total number of games this player has played.
	 * @return The total number of games this player has played as an int.
	 */
	public int getTotal() {
		return mTotalGames;
	}

	/**
	 * Gets the number of games this player has won.
	 * @return The number of games this player has won as an int.
	 */
	public int getWins() {
		return mWins;
	}

	/**
	 * Gets the number of games this player has lost.
	 * @return The number of games this player has lost as an int.
	 */
	public int getLosses() {
		return mLosses;
	}

	/**
	 * Gets the current win streak for this player.
	 * @return The current win streak for this player as an int.
	 */
	public int getWinStreakCur() {
		return mWinStreakCur;
	}

	/**
	 * Gets the longest win streak this player has reached.
	 * @return The longest win streak this player has reached as an int.
	 */
	public int getWinStreakLong() {
		return mWinStreakLong;
	}

	/**
	 * Updates this player's stats to reflect winning one game.
	 * Increases total games and games won by 1 and also updates
	 * the current and longest win streaks as needed.
	 */
	public void Win() {
		mTotalGames++;
		mWins++;
		mWinStreakCur++;
		if (mWinStreakCur > mWinStreakLong) {
			mWinStreakLong++;
		}
	}

	/**
	 * Updates this player's stats to reflect losing one game.
	 * Increases total games and games lost by 1 and resets the
	 * current win streak to 0.
	 */
	public void Lose() {
		mTotalGames++;
		mLosses++;
		mWinStreakCur = 0;
	}
}
