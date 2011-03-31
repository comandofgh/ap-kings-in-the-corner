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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This class is used to manage player stats.
 * It handles all reading and writing to the file
 * containing player stats and has various methods to
 * obtain data about the stored stats.
 */
public class StatsManager {
	public static final String STATS_FILE_NAME = "Stats";
	
	// Is BackupManager available and will we be using it?
	private boolean mAllowBackupManager;
	private static boolean mBackupManagerAvailable;
	static {
		try {
			WrapBackupManager.checkAvailable();
			mBackupManagerAvailable = true;
		} catch (Throwable t) {
			mBackupManagerAvailable = false;
		}
	}
	private WrapBackupManager mWrapBackupManager;
	
	private Context mContext;
	private File mDataFile;
	
	/** 
	 * Construct a new StatsManager for the given Context.
	 */
	public StatsManager(Context context) {
		mContext = context;
		mDataFile = new File(mContext.getFilesDir(), STATS_FILE_NAME);
		
		mAllowBackupManager = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(
				mContext.getResources().getString(R.string.pref_key_allowBackupManager), false);
		
		if (mBackupManagerAvailable && mAllowBackupManager) {
			mWrapBackupManager = new WrapBackupManager(mContext);
		}
	}
	
	/**
	 * Constructs an ArrayList from file of all the players with stored stats.
	 * @return an ArrayList containing all the players and their stats.
	 * Returns an empty list if there are no players or an I/O error
	 * occurs while reading from the file.
	 */
	public ArrayList<PlayerStats> getPlayerList() {
		ArrayList<PlayerStats> players = new ArrayList<PlayerStats>();
		int numPlayers = 0;
		try {
			synchronized(Main.sDataLock) {
				RandomAccessFile file = new RandomAccessFile(mDataFile, "r");

				numPlayers = file.readInt();
				for (int i = 0; i < numPlayers; i++) {
					players.add(readPlayer(file));
				}
			}
		} catch (IOException e) { }

		return players;
	}
	
	/**
	 * Gets the number of players with stored stats. Slightly
	 * faster than calling getPlayerList().size() because it only
	 * reads the first int from the file and doesn't need to
	 * construct an ArrayList. If you already have a pointer to
	 * the ArrayList, just call its size() method instead.
	 * @return the number of players with stored stats.
	 * Returns zero if there are no players or an I/O error
	 * occurs while reading from the file.
	 */
	public int getPlayerCount() {
		int numPlayers = 0;
		try {
			synchronized(Main.sDataLock) {
				RandomAccessFile file = new RandomAccessFile(mDataFile, "r");			
				numPlayers = file.readInt();
			}
		} catch (IOException e) { }
		return numPlayers;
	}
	
	/**
	 * Attempts to add a new Player with the given name.
	 * Returns a boolean indicating if the Player was added.
	 * A Player will not be added if a Player already exists with
	 * the same name (capitalization doesn't matter).
	 * @param name the name of the player to add.
	 * @return false if a player with the given name already exists, true otherwise.
	 * @throws IOException if an I/O error occurs while reading from or writing to the file.
	 */
	public boolean addNewPlayer(String name) throws IOException {
		ArrayList<PlayerStats> players = getPlayerList();
		
		// Check if a Player with the same name already exists
		for (PlayerStats p : players) {
			if (p.getName().toLowerCase().equals(name.toLowerCase())) {
				return false;
			}
		}
		
		// If the Player is not a duplicate, add the new Player
		RandomAccessFile file = new RandomAccessFile(mDataFile, "rw");
		file.setLength(0L);

		file.writeInt(players.size()+1);		
		for (PlayerStats p : players) {
			writePlayer(p, file);
		}
		writePlayer(new PlayerStats(name), file);

		if (mWrapBackupManager != null && mAllowBackupManager)
			mWrapBackupManager.dataChanged();

		return true;
	}
	
	/**
	 * Attempts to remove a player from the stored stats.
	 * If a player with the given name is not found, no
	 * Player is actually removed.
	 * @param name the name of the player to remove.
	 * @throws IOException if an I/O error occurs while reading from or writing to the file.
	 */
	public void removePlayer(String name) throws IOException {
		ArrayList<PlayerStats> players = getPlayerList();
		
		RandomAccessFile file = new RandomAccessFile(mDataFile, "rw");
		file.setLength(0L);
		
		/**
		 * Create a new list of players to keep in case a player
		 * with the given name is not found. This way the size
		 * will be correct when writing the number of players
		 * regardless of whether or not a player was actually removed.
		 */
		ArrayList<PlayerStats> keptPlayers = new ArrayList<PlayerStats>();		
		for (PlayerStats p : players) {
			if (!p.getName().equals(name)) {
				keptPlayers.add(p);
			}
		}
		
		file.writeInt(keptPlayers.size());
		for (PlayerStats p : keptPlayers) {
			writePlayer(p, file);
		}
		
		if (mWrapBackupManager != null && mAllowBackupManager)
			mWrapBackupManager.dataChanged();
	}
	
	/**
	 * Update stats for the player with the given name based
	 * on whether they won or lost a game. If a player with the
	 * given name is not found, no stats are changed.
	 * @param name the name of the player to update stats for.
	 * @param playerWon true if the player won the game; false if the player lost the game.
	 * @throws IOException if an I/O error occurs while reading from or writing to the file.
	 */
	public void playerFinishedGame(String name, boolean playerWon) throws IOException {
		ArrayList<PlayerStats> players = getPlayerList();

		synchronized(Main.sDataLock) {				
			RandomAccessFile file = new RandomAccessFile(mDataFile, "rw");
			file.setLength(0L);

			file.writeInt(players.size());
			for (PlayerStats p : players) {
				if (p.getName().equals(name)) {
					if (playerWon) p.Win();
					else p.Lose();
				}
				writePlayer(p, file);
			}
		}

		if (mWrapBackupManager != null && mAllowBackupManager)
			mWrapBackupManager.dataChanged();
	}
	
	/**
	 * Writes a player to a file, starting at the current file pointer.
	 * @param p the player to write to the file.
	 * @param file the file to write to.
	 * @throws IOException if an I/O error occurs while writing to the file.
	 */
	private void writePlayer(PlayerStats p, RandomAccessFile file) throws IOException {
		file.writeUTF(p.getName());
		file.writeInt(p.getTotal());
		file.writeInt(p.getWins());
		file.writeInt(p.getLosses());
		file.writeInt(p.getWinStreakCur());
		file.writeInt(p.getWinStreakLong());
	}
	
	/**
	 * Reads a player from a file, starting at the current file pointer.
	 * @param file the file to read from.
	 * @return the player read from the file.
	 * @throws IOException if an I/O error occurs while reading from the file.
	 */
	private PlayerStats readPlayer(RandomAccessFile file) throws IOException {
		String name = file.readUTF();
		int totalGames = file.readInt();
		int wins = file.readInt();
		int losses = file.readInt();
		int wsc = file.readInt();
		int wsl = file.readInt();
		
		return new PlayerStats(name, totalGames, wins, losses, wsc, wsl);
	}
	
	/**
	 * Combines stats stored in seperate player files into the
	 * new format where all stats are contained in a single file.
	 * @return true if the files were succussfully combined;
	 * false otherwise.
	 */
	public boolean combineSplitToSingle() {		
		String[] files = mContext.fileList();
		ArrayList<PlayerStats> players = new ArrayList<PlayerStats>();
		
		try {		
			// Get all the old player stats and add them to a list of players
			FileInputStream fis;
			ObjectInputStream in;
			for (String file : files) {
				fis = new FileInputStream(mContext.getFilesDir()+"/"+file);
				in = new ObjectInputStream(fis);
				if (!file.contains(".") && !file.equals(STATS_FILE_NAME)) {
					Player p = (Player)in.readObject();
					PlayerStats ps = new PlayerStats(p.mUsername, p.mTotalGames, p.mWins, p.mLosses, p.mWinStreakCur, p.mWinStreakLong);
					players.add(ps);	// Add the player to the list
					mContext.deleteFile(file);				// Delete the old file
				}
			}
			
			// Write the players to the new stats file
			RandomAccessFile file = new RandomAccessFile(mDataFile, "rw");
			file.setLength(0L);
			
			file.writeInt(players.size());
			for (PlayerStats p : players) {
				writePlayer(p, file);
			}		
		
		// Delete the new stats file if an error occurs so there is not partially
		// written data that could mess up later reads or writes
		} catch (IOException e) {
			Log.e("IOEx", e.toString());
			mContext.deleteFile(STATS_FILE_NAME);
			return false;
		} catch (ClassNotFoundException e) {
			Log.e("ClassEx", e.toString());
			mContext.deleteFile(STATS_FILE_NAME);
			return false;
		}
		
		return true;
	}
}
