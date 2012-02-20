package com.asparagusprograms.kingsinthecorner;

import java.io.Serializable;

/**
 * Old player class. Used to read in old stats to
 * convert them to the method of saving stats.
 */
public class Player implements Serializable {
	private static final long serialVersionUID = -7932293913196756290L;
	public String mUsername;
	public int 	mTotalGames,
				mWins,
				mLosses,
				mWinStreakCur,
				mWinStreakLong;
}
