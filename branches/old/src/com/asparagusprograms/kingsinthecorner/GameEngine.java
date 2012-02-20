/**
 * Copyright 2010,2011 Asparagus Programs
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.util.Observable;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.widget.Toast;


/**
 * Handles game functions and drawing and provides access to necessary information.
 */
public class GameEngine extends Observable implements Runnable {
	// Types for computer errors
	/** The computer is moving a pile of cards. Use with {@link #computerError(int)}.*/
	private final static int STYLE_MOVE = 1;
	/** The computer is playing a card from their hand. Use with {@link #computerError(int)}. */
	private final static int STYLE_PLAY = 2;

	// User preferences
	/** Whether or not to display the number of cards remaining in the deck. */
	private boolean mDrawPileCount;
	/** Whether or not to highlight cards and empty slots where another card can be played. */
	private boolean mHighlightCards;
	/** Whether or not to automatically save the game at various points. */
	private boolean mAutosave;
	/** Whether or not to display a warning dialog when the deck becomes empty. */
	private boolean mEmptyDeckWarning;
	/** Whether or not the empty deck warning has been displayed yet. */
	private boolean mWarnedEmpty;
	/** Whether or not to automatically turn on sort hand at the start of the game. */
	private boolean mSortHand;
	/** Whether or not to clear corner piles when they become full. */
	private boolean mClearCorners;
	
	/** 
	 * The current user name associated with a single player game.
	 * This value should only be set during the start of the game so
	 * user names cannot be changed mid-game.
	 */
	private String mUsername;
	/** The style of cards to use. */
	private String mCardStyle;
	/** The image to display for the card backs. */
	private String mCardBackStyle;
	/** The image to use for the table background. */
	private String mTableImage;
	/** The number of milliseconds to delay the computer's playing. */
	private int mComputerDelay;
	
	/**
	 * The current difficulty setting. Higher means easier. This should only be set
	 * at the start of a game so the difficulty cannot be changed mid-game.
	 */
	private int mDifficulty;
	/**
	 * An integer value for the color to use when showing the draw pile count.
	 * @see #mDrawPileCount
	 */
	private int mDrawPileCountColor;
	/** An integer value for the color to use when showing the player scores
	 * for a multi-player game.
	 */
	private int mScoresColor;
	
	// Cheats
	/** Whether or not to show the computer's hand face up. */
	private boolean mShowComputerHand;

	// Global variables
	/** The context associated with the game's resources. */
	private Context mContext;
	/** The default shared preferences. */
	private SharedPreferences mPrefs;
	/** The string to use as a filename when saving the game. */
	private String mSaveString;
	/** The number of human players for the game. */
	private int mPlayerCount;
	/** The value for the current player. */
	private int mTurn;
	
	/**
	 * The value for the winner of the game. Set to -1 when there
	 * is no winner. For a single player game, 0 is the human player
	 * and 1 is the computer player.
	 */
	private int mWinner;
	
	/** Used to pause the computer playing thread. */
	private volatile boolean mPaused;
	/** Used to stop the computer playing thread. */
	private volatile boolean mStop;

	//Undo variables
	/** Whether or not a player's last move can be undone. */
	private boolean mCanUndo;
	/**
	 * True if the move that can be undone is on a side pile.
	 * False if the move that can be undone is on a corner pile.
	 */
	private boolean mUndoIsSide;
	/**
	 * The position of the side or corner pile containing the
	 * card to be returned to the player's hand.
	 */
	private int mUndoPos;
	/** The card to return to the player's hand when a move is undone. */
	private Card mUndoCard;
	/** The card to place back onto the pile when a move is undone. */
	private Card mReplaceWithCard;

	//Hand, deck, etc
	/** An array of hands for the players in the game. */
	private Hand[] mHands;
	/** The deck being used for the game. */
	private Deck mDeck;
	/** The side piles for the game. */
	private Pile[] mSides;
	/** The corner piles for the game. */
	private Pile[] mCorners;
	/** The currently selected card. If no card is selected, this value is null. */
	private Card mSelectedCard;
	/** The currently selected pile. If no pile is selected, this value is -1. */
	private int mSelectedPile;
	/** The pile to highlight. If there is no pile to highlight, this value is -1. */
	private int mHighlightedPile;

	// Drawing variables
	/** True if the game is ready to be drawn to a canvas, false otherwise. */
	private boolean mDrawInitialized;
	/** The paint used when drawing the game to a canvas. */
	private Paint mPaint;
	/** The height of the view the game is to be drawn in. */
	private int mViewHeight;
	/** The width of the view the game is to be drawn in. */
	private int mViewWidth;
	/** The height of a single card when drawn to a canvas. */
	private int mCardHeight;
	/** The width of a single card when drawn to a canvas. */
	private int mCardWidth;
	/** The full height of the screen. */
	private int mScreenHeight;
	/** True if the current hand should be hidden. */
	private boolean mHideHand;
	/** The view used for the card table to draw the game to. */
	private CardTableView mTable;
	/** The target x-coordinate for user touch interaction. */
	private int mTarX;
	/** The target y-coordinate for user touch interaction. */
	private int mTarY;

	// Stored bitmaps
	/**
	 * An array of bitmaps for displayer the current player's turn
	 * in a multi-player game.
	 */
	private Bitmap[] mPlayerBitmaps;
	/** The bitmap to display when showing the back of a card. */
	private Bitmap mCardBack;
	/**
	 * Same as {@link #mCardBack} but rotated 180 to show the computer's hand more
	 * like it is facing the player.
	 */
	private Bitmap mComputerCardBack;
	/** Displayed over a card or pile to make it highlighted. */
	private Bitmap mGlowNormal, mGlowSide, mGlowCorner1, mGlowCorner2;

	//Rects for placing bitmaps
	/** Rectangle used for placing the draw pile on the table. */
	private Rect draw;

	/**
	 * Creates a new game engine to be used throughout this round of games.
	 * @param context The context associated with the game's resources.
	 * @param numPlayers The number of human players in the game.
	 * @param table The view used to draw the game to.
	 */
	public GameEngine(Context context, int numPlayers, CardTableView table) {
		mContext = context;
		mTable = table;
		mPlayerCount = numPlayers;

		Initialize();
	}

	/** Initializes global variables. **/
	public void Initialize() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mUsername = mPrefs.getString(mContext.getResources().getString(R.string.pref_key_username), mContext.getResources().getString(R.string.username_none));
		mCardStyle = mPrefs.getString(mContext.getResources().getString(R.string.pref_key_cardImage), mContext.getResources().getString(R.string.cardImage_default));
		mCardBackStyle = mPrefs.getString(mContext.getResources().getString(R.string.pref_key_cardBack), mContext.getResources().getString(R.string.cardBack_default));
		mDifficulty = Integer.parseInt(mPrefs.getString(mContext.getResources().getString(R.string.pref_key_difficulty), "0"));
		updatePrefs();
		mSaveString = mUsername + "_save.dat";
		mTurn = -1;
		mWinner = -1;

		if (mPlayerCount == 1) {
			mHideHand = false;
		} else {
			mHideHand = true;
		}

		if (mPlayerCount > 1) mPlayerBitmaps = new Bitmap[mPlayerCount];
		switch (mPlayerCount) {
		case 4:
			mPlayerBitmaps[3] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.player4);
		case 3:
			mPlayerBitmaps[2] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.player3);
		case 2:
			mPlayerBitmaps[1] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.player2);
			mPlayerBitmaps[0] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.player1);
			break;
		default:
			break;
		}

		setCardBackImage();

		mGlowNormal = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.glow);
		mGlowSide = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.glow_side);
		mGlowCorner1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.glow_corner1);
		mGlowCorner2 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.glow_corner2);

		mPaint = new Paint();
		mSelectedCard = null;
		mSelectedPile = -1;
		mHighlightedPile = -1;
		mCanUndo = false;

		int count = (mPlayerCount == 1) ? 2 : mPlayerCount;  // Number of hands to deal, if it's a single player game this needs to be 2
		mHands = new Hand[count];

		mWarnedEmpty = false;
		mShowComputerHand = false;
		mDrawInitialized = false;
		mPaused = false;
		mStop = false;
		mDeck = new Deck(mContext, mCardStyle);
		mSides = new Pile[4];
		mCorners = new Pile[4];
		draw = new Rect();
	}

	// Cheats
	/** Automatically win the current game. */
	public void autoWin() {
		mHands[0].clear();
		playerWin();
	}

	/** 
	 * Gives a joker to the computer player.
	 * A joker cannot be played. Anywhere. Ever.
	 */
	public void trash() {
		Card joker = new Card(0, 4);
		joker.setImage(mContext, mCardStyle);
		mHands[1].addCard(joker);
		mTable.postInvalidate();
	}

	/** Toggles {@link #mShowComputerHand} on and off. */
	public void toggleShowComputerHand() {
		mShowComputerHand = !mShowComputerHand;
		mTable.postInvalidate();
	}

	/**
	 * Gets value for the current player's turn.
	 * @return The current turn.
	 */
	public int turn() {
		return mTurn;
	}

	/**
	 * Gets the winner of the game.
	 * @return The winner, if there is one. If there is no winner,
	 * 			returns -1. 
	 */
	public int winner() {
		return mWinner;
	}

	/**
	 * Gets whether or not the empty deck warning dialog needs to be displayed.
	 * @return True if the empty deck warning dialog needs to be displayed, false otherwise.
	 */
	public boolean warnEmpty() {
		if (mDeck.cardsLeft() == 0 && mEmptyDeckWarning && !mWarnedEmpty) {
			mWarnedEmpty = true;
			return true;
		}
		return false;
	}

	/**
	 * Gets whether or not the current game is single player.
	 * @return True if the current game is single player, false otherwise.
	 */
	public boolean isSinglePlayer() {
		return (mPlayerCount == 1);
	}

	/**
	 * Gets whether or not the current player can undo their last move.
	 * @return True if the current player's last move can be undone, false otherwise.
	 */
	public boolean canUndo() {
		if (mUndoCard == null) return false;
		return mCanUndo;
	}

	/**
	 * Sets whether or not to hide the current hand.
	 * @param hideHand Set to true if the current hand should be hidden.
	 */
	public void hideHand(boolean hideHand) {
		mHideHand = hideHand;
		mTable.postInvalidate();
	}

	/** Sets that the next game will no longer be the first game. */
	public void firstGame() {
		mPrefs.edit().putBoolean(mContext.getResources().getString(R.string.pref_key_firstGame), false).commit();
	}

	/**
	 * Sets whether or not a game is currently in progress.
	 * @param inGame Set to true if a game is currently in progress.
	 */
	public void inGame(boolean inGame) {
		mPrefs.edit().putBoolean(mContext.getResources().getString(R.string.pref_key_inGame), inGame).commit();
	}

	//Gameplay methods
	/** Undo the last move. */
	public void undo() {
		if (mUndoIsSide) {
			mSides[mUndoPos].undo(mReplaceWithCard);
		} else {
			mUndoCard.setImage(mContext, mCardStyle);
			mCorners[mUndoPos].undo(mReplaceWithCard);
		}
		mUndoCard.setRotate(0, mContext, mCardStyle);
		mHands[mTurn].addCard(mUndoCard);
		mCanUndo = false;

		mTable.postInvalidate();
	}

	/** Pause the computer playing. */
	public void pause() {
		mPaused = true;
		if (mAutosave) save();
	}

	/** Resume the computer playing. */
	public void resume() {
		mPaused = false;
	}

	/** Start the computer playing. */
	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}

	/** Stop the computer playing. */
	public void stop() {
		mStop = true;
	}

	/**
	 * Gets whether the computer makes a mistake or not based on difficulty and type of play.
	 * @param playType The type of play the computer is making. Can be one of either
	 * 		{@link #STYLE_MOVE} or {@link #STYLE_PLAY}.
	 * @return True if the computer should miss this play or move.
	 */
	private boolean computerError(int playType) {
		if (mDifficulty == 0) return false; // No errors on hard
		int percent = 0;

		switch (playType) {
		case STYLE_MOVE:
			percent = (mDifficulty == 1) ? 20 : 60;
			break;
		case STYLE_PLAY:
			percent = mDifficulty * 10;
			break;
		default:
			return false;
		}

		int chance = (int)(java.lang.Math.random()*100);
		return (chance <= percent);
	}

	/** Thread used for the computer playing so the UI is not locked. */
	@Override
	public void run() {
		try {
			// Dirty way of handling exiting since the thread tries to run after the
			// variables have been freed up, causing a NullPointerException.

			boolean playing = true;
			while (playing) {
				mTable.postInvalidate();
				playing = false;
				// Check if the computer has won or only has a Joker
				if (compWin() || (mHands[1].getCardCount() == 1 && mHands[1].getCard(0).getSuit() == 4) || mStop) return;

				// Wait before playing
				try {
					Thread.sleep(mComputerDelay);
				} catch (InterruptedException e) {}

				// Try to move side piles
				for (int i = 0; i < mSides.length; i++) {
					// Try moving to corners
					for (int j = 0; j < mCorners.length; j++) {
						while(mPaused) { // Wait while the game is paused
							if (mStop) return;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {}
						}	
						if (!computerError(STYLE_MOVE) && mSides[i].moveTo(mCorners[j])) {
							playing = true;
							mTable.postInvalidate();
							// Wait
							try {
								Thread.sleep(mComputerDelay);
							} catch (InterruptedException e) {}
						}
					}
					// Try moving to other sides
					for (int j = 0; j < mSides.length; j++) {
						while(mPaused) { // Wait while the game is paused
							if (mStop) return;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {}
						}
						if (!computerError(STYLE_MOVE) && j != i && mSides[i].moveTo(mSides[j])) {
							playing = true;
							mTable.postInvalidate();
							// Wait
							try {
								Thread.sleep(mComputerDelay);
							} catch (InterruptedException e) {}
						}
					}
				}

				// Try to play cards in hand
				for (int i = 0; i < mHands[1].getCardCount(); i++) {
					// Try to play on corners
					for (int j = 0; j < mCorners.length; j++) {
						while(mPaused) { // Wait while the game is paused
							if (mStop) return;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {}
						}
						if (!computerError(STYLE_PLAY) && mCorners[j].play(mHands[1].getCard(i))) {
							mHands[1].removeCard(i);
							playing = true;
							mTable.postInvalidate();
							// Wait
							try {
								Thread.sleep(mComputerDelay);
							} catch (InterruptedException e) {}
							break;
						}
					}
					// Break if a card was played since the card count and position has changed
					if (playing) break;
					// Try to play on sides
					for (int j = 0; j < mSides.length; j++) {
						while(mPaused) { // Wait while the game is paused
							if (mStop) return;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {}
						}
						if ( (mSides[j].first == null || !computerError(STYLE_PLAY)) && mSides[j].play(mHands[1].getCard(i)) ) {
							mHands[1].removeCard(i);
							playing = true;
							mTable.postInvalidate();
							// Wait
							try {
								Thread.sleep(mComputerDelay);
							} catch (InterruptedException e) {}
							break;
						}
					}
					// Break if a card was played since the card count and position has changed
					if (playing) break;
				}
			}		
			while (mPaused) { // Wait while the game is paused
				if (mStop) return;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
			if (mClearCorners) {
				for (int i = 0; i < 4; i++)
					mCorners[i].clearCorner();
			}
			
			if (!playing) nextTurn();

		} catch (NullPointerException e) {}
	}

	/** 
	 * Handles going to the next player's turn.
	 * Changes {@link #mTurn} to the next player and
	 * deals them a card if the deck is not empty. Starts
	 * the computer playing thread if it is the computer's turn.
	 */
	public void nextTurn() {
		mCanUndo = false;
		mSelectedPile = -1;
		mSelectedCard = null;
		if (mPlayerCount > 1) {
			mHideHand = true;
		} else {
			if (mAutosave) save();
		}

		mTurn++;
		if (mPlayerCount > 1 && mTurn >= mPlayerCount) mTurn = 0;
		else if (mPlayerCount == 1 && mTurn >= 2) mTurn = 0;
		if (mDeck.cardsLeft() > 0) {
			mHands[mTurn].addCard(mDeck.dealCard());
		}

		setChanged();
		notifyObservers();
		mTable.postInvalidate();

		if (mPlayerCount == 1 && mTurn == 1) start();
	}

	/** Sets up a new game. */
	public void newGame() {
		// Set the deck and shuffle it
		mDeck.shuffle();

		// Deal the starting hands
		for (int i = 0; i < mHands.length; i++) {
			mHands[i] = new Hand();
			for (int j = 0; j < 7; j++) {
				mHands[i].addCard(mDeck.dealCard());
			}
		}

		// Deal the side piles
		for (int i = 0; i < 4; i++) {
			mSides[i] = new Pile(i, mDeck.dealCard(), null);
		}

		// Create empty corner piles
		for (int i = 0; i < 4; i++) {
			mCorners[i] = new Pile(i+4, null, null);
		}

		// Set the default Sort Hand option
		if (mPlayerCount == 1 && mSortHand) {
			mHands[0].toggleSortColor();
		}

		nextTurn();
	}

	/** Saves the current game. */
	public void save() {
		if (mWinner == -1 && mPlayerCount == 1 && mDeck != null) {
			try {
				synchronized(Main.sDataLock) {
					FileOutputStream fop = mContext.openFileOutput(mSaveString, Context.MODE_PRIVATE);
					ObjectOutputStream out = new ObjectOutputStream(fop);

					Pile[] sides = mSides;
					Pile[] corners = mCorners;
					out.writeInt(mTurn);
					out.writeInt(mHands[0].getCardCount());
					for (int i = 0; i < mHands[0].getCardCount(); i++) {
						out.writeObject(mHands[0].getCard(i));
					}
					out.writeInt(mHands[1].getCardCount());
					for (int i = 0; i < mHands[1].getCardCount(); i++) {
						out.writeObject(mHands[1].getCard(i));
					}
					out.writeInt(mDeck.cardsLeft());
					for (int i = 0; i < 52; i++) {
						out.writeObject(mDeck.cardAt(i));
					}

					for (int i = 0; i < 4; i++) {
						out.writeObject(sides[i]);
					}
					for (int i = 0; i < 4; i++) {
						out.writeObject(corners[i]);
					}
					out.writeBoolean(mCanUndo);
					out.writeObject(mUndoCard);
					out.writeObject(mReplaceWithCard);
					out.writeInt(mUndoPos);
					out.writeBoolean(mUndoIsSide);

					out.close();
					fop.close();
				}
			} catch (FileNotFoundException e) {
				mTable.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_fileNotFoundException), Toast.LENGTH_SHORT).show();
					}
				});
			} catch (StreamCorruptedException e) {
				mTable.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_streamCorruptedException), Toast.LENGTH_SHORT).show();
					}
				});
			} catch (IOException e) {
				mTable.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_IOException), Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
	}

	/**
	 * Attempts to restore a saved game.
	 * @return True if the restore was successful, false otherwise.
	 */
	public boolean restore() {
		if (mPlayerCount == 1) {
			try {
				mContext.openFileInput(mSaveString);
				if (restoreGame()) {
					if (mTurn == 1) start();
					mTable.postInvalidate();
					//	if (mTurn == 1) start(); // Start the computer playing since it is their turn
					return true; // Return true that a game was restored
				}
			} catch (FileNotFoundException e) {
				// There is no save game file to restore
			}
		}
		return false; // Game was not restored
	}

	/** Deletes the saved game for the current user. */
	public void deleteSave() {
		try {
			mContext.deleteFile(mSaveString);
		} catch (NullPointerException e) {
			if (mTable != null) mTable.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_nullPointerException), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	/** Toggles the "sortedness" of the current hand. */
	public void sortHand() {
		mHands[mTurn].toggleSortColor();
		mTable.postInvalidate();
	}

	/** 
	 * Gets whether the current hand is sorted or not.
	 * @return True if the current hand is sorted by color.
	 */
	public boolean handSorted() {
		if (mHands == null || mHands[mTurn] == null) {
			return false;
		}
		return mHands[mTurn].isSortedColor();
	}

	// Drawing methods
	/**
	 * Gets whether or not the game is ready to be drawn to a canvas.
	 * @return True if the game is ready to be drawn to a canvas, false otherwise.
	 */ 
	public boolean drawInitialized() {
		return mDrawInitialized;
	}

	/** 
	 * Sets the full height of the screen.
	 * @param height The height of the screen.
	 */
	public void screenHeight(int height) {
		mScreenHeight = height;
	}

	/** 
	 * Called whenever the screen is being drawn.
	 * @param canvas The canvas to draw the game to.
	 */
	public void onDraw(Canvas canvas) {
		if (mStop) return;
		if (!mDrawInitialized) {
			// Set scaled dimensions
			mCardWidth = mCardBack.getScaledWidth(canvas);
			mCardHeight = mCardBack.getScaledHeight(canvas);
			mViewWidth = mTable.getViewWidth();
			mViewHeight = mTable.getViewHeight();

			// Set up positions for side piles
			int x = (mViewWidth/2)-(mCardWidth/2)-mCardHeight-10-(mCardHeight/5);
			int y = (mViewHeight/2)-(mCardWidth/2)-(mCardHeight/4);
			mSides[0].pos = new Rect(x, y, x+mCardHeight+(mCardHeight/5), y+mCardWidth);
			x = (mViewWidth/2)-(mCardWidth/2);
			y = (mViewHeight/2)-(mCardHeight/2)-(mCardHeight/4)-mCardHeight-10-(mCardHeight/5);
			mSides[1].pos = new Rect(x, y, x+mCardWidth, y+mCardHeight+(mCardHeight/5));
			x = (mViewWidth/2)+(mCardWidth/2)+10;
			y = (mViewHeight/2)-(mCardWidth/2)-(mCardHeight/4);
			mSides[2].pos = new Rect(x, y, x+mCardHeight+(mCardHeight/5), y+mCardWidth);
			x = (mViewWidth/2)-(mCardWidth/2);
			y = (mViewHeight/2)+(mCardHeight/2)-(mCardHeight/4)+10;
			mSides[3].pos = new Rect(x, y, x+mCardWidth, y+mCardHeight+(mCardHeight/5));

			// Set up positions for corner piles
			x = (mViewWidth/2)-(mCardHeight/2)-mCardHeight-(mCardHeight/8);
			y = (mViewHeight/2)-(mCardHeight/2)-(mCardHeight/4)-mCardHeight-(mCardHeight/8);
			mCorners[0].pos = new Rect(x, y, 0, 0);
			x = (mViewWidth/2)+(mCardWidth/2)+(mCardHeight/6);
			y = (mViewHeight/2)-(mCardHeight/3)-(mCardHeight/2)-mCardHeight-(mCardHeight/8);
			mCorners[1].pos = new Rect(x, y, 0, 0);
			x = (mViewWidth/2)+(mCardWidth/2)+(mCardHeight/6);
			y = (mViewHeight/2)+(mCardHeight/2)-(mCardHeight/4);
			mCorners[2].pos = new Rect(x, y, 0, 0);
			x = (mViewWidth/2)-(mCardHeight/2)-mCardHeight-(mCardHeight/8);
			y = (mViewHeight/2)+(mCardHeight/2)-(mCardHeight/4);
			mCorners[3].pos = new Rect(x, y, 0, 0);

			// Set up rectangle for the draw pile
			x = (mViewWidth/2)-(mCardWidth/2);
			y = (mViewHeight/2)-(mCardHeight/2)-(mCardHeight/4);
			draw.set(x, y, x+mCardWidth, y+mCardHeight);
			mDrawInitialized = true;

			// Set up positions for each hand
			if (mPlayerCount == 1) {
				mHands[0].initializeDraw(mViewHeight-mCardHeight, mViewWidth, mCardWidth);
				mHands[1].initializeDraw(-(mCardHeight/2), mViewWidth, mCardWidth);
			} else {
				for (int i = 0; i < mPlayerCount; i++) {
					mHands[i].initializeDraw(mViewHeight-mCardHeight, mViewWidth, mCardWidth);
				}
			}
		}
		if (mDeck != null) {
			if (mWinner == -1) {	// Nobody has won so draw the game data
				if (mDeck.cardsLeft() != 0) {	// Deck isn't empty so draw the deck
					canvas.drawBitmap(mCardBack, draw.left, draw.top, null);
					if (mDrawPileCount) {	// Print the number of cards in the draw pile
						mPaint.setColor(mDrawPileCountColor);

						mPaint.setTextSize(mCardHeight/2);
						mPaint.setAntiAlias(true);
						DecimalFormat df = new DecimalFormat("00");
						String count = df.format(mDeck.cardsLeft());
						int height = (int) mPaint.descent();
						int width = (int) mPaint.measureText(count);
						canvas.drawText(count, (mViewWidth/2)-(width/2), (mViewHeight/2)-(height), mPaint);
					}
				}
				if (mHands[mTurn] != null && !mHideHand) {
					if (mPlayerCount == 1) mHands[0].draw(canvas, null, null);
					else mHands[mTurn].draw(canvas, null, null);
					
					if (mPlayerCount > 1) {
						canvas.drawBitmap(mPlayerBitmaps[mTurn], 0, 0, null); // Draws the current turn
					}
					// Draw the scores for each player
					if (mPlayerCount > 1) {
						mPaint.setTextSize(mCardHeight/5);
						mPaint.setColor(mScoresColor);
						mPaint.setAntiAlias(true);
						for (int i = 0; i < mPlayerCount; i++) {
							String s = "Player " + (i+1) + ": " + mHands[i].getCardCount() + " ";
							canvas.drawText(s, (int)((mViewWidth)-mPaint.measureText(s)-2), (int)((i+1)*(mPaint.descent()-mPaint.ascent())), mPaint);
						}
					}
					//else drawComputerHand(c);
					else {
						if (mShowComputerHand) mHands[1].draw(canvas, null, null);
						else mHands[1].draw(canvas, null, mComputerCardBack);
					}
				}
				if (mSides != null) {
					// Loop through and draw each side
					for (int i = 0; i < mSides.length; i++) {
						if (i != mSelectedPile && mSides[i] != null) {
							mSides[i].draw(canvas, mCardHeight, mContext, mCardStyle);
						}
					}
				}
				if (mCorners != null) {
					// Loop through and draw each corner
					for (int i = 0; i < mCorners.length; i++) {
						if (mCorners[i] != null) mCorners[i].draw(canvas, mCardHeight, mContext, mCardStyle);
					}
				}
				if (mSides != null && mCorners != null && mHighlightCards) {
					drawHighlighted(canvas);
				}
				// Draw the selected card/pile
				if (mSelectedCard != null) {
					mSelectedCard.setRotate(0, mContext, mCardStyle);
					mSelectedCard.setPos(mTarX-(mCardWidth/2), mTarY-(mCardHeight/4));
					mSelectedCard.draw(canvas, null, null);
				} else if (mSelectedPile >= 0)
					mSides[mSelectedPile].drawSelected(canvas, mCardWidth, mCardHeight, mTarX, mTarY, mContext, mCardStyle);
			} else {
				mPaint.setColor(Color.BLACK);
				mPaint.setTextSize(mCardHeight/4);
				mPaint.setAntiAlias(true);
				String s1 = "Press Menu to play again";
				String s2 = "or Back to exit.";
				canvas.drawText(s1, (mViewWidth/2)-(int)(mPaint.measureText(s1)/2), (mViewHeight/2)-(int)mPaint.descent(), mPaint);
				canvas.drawText(s2, (mViewWidth/2)-(int)(mPaint.measureText(s2)/2), (mViewHeight/2)+(int)mPaint.descent()-(int)mPaint.ascent(), mPaint);
				mTable.postInvalidate();
			}				
		}
	}

	/** Handles user touch events on the given view.
	 * 
	 * @param event The movement event.
	 * @return True if the event was handled, false otherwise.
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if (mWinner != -1) return false;	// Make sure the game isn't over
		if (mPlayerCount == 1 && mTurn != 0) return false;	// Make sure it isn't the computer's turn
		
			int eventaction = event.getAction();
			mTarX=(int)event.getRawX();
			mTarY=(int)event.getRawY()-(mScreenHeight-mViewHeight);
			switch (eventaction) { 
			case (MotionEvent.ACTION_DOWN):
				if (mTarY >= (mViewHeight-mCardHeight)) {
					if (mSelectedCard == null) {
						mSelectedCard = mHands[mTurn].getTargetCard(mTarX);
					}
				} else if (mTarX > draw.left && mTarX < draw.right && mTarY > draw.top && mTarY < draw.bottom) {
					if (mWinner == -1 && (mPlayerCount > 1 || (mPlayerCount == 1 && mTurn == 0))) {
						mSelectedCard = null;
						mSelectedPile = -1;
						nextTurn();
					}
				} else {
					mSelectedCard = null;
					mSelectedPile = -1;
					findTargetPile();
				}
			break;
			case MotionEvent.ACTION_MOVE:
				if (mSelectedCard != null) {
					if (mTarY >= mViewHeight-mCardHeight-mCardHeight/4) {
						mHands[mTurn].hoverCardAt(mTarX);
					}
					else {
						mHands[mTurn].shiftCardsNormal();
						if (mHighlightCards) {
							highlightPile();
						}
					}		
				} else if (mSelectedPile >= 0 && mSides[mSelectedPile] != null) {
					if (mHighlightCards){
						highlightPile();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mTarY >= mViewHeight-mCardHeight-mCardHeight/4 && mSelectedCard != null) {
					mHands[mTurn].addCardAtIndex(mSelectedCard);
				}
				else if (mSelectedCard != null || mSelectedPile >= 0)  {
					if (!findTargetPile() && mSelectedCard != null) {
						mHands[mTurn].addCard(mSelectedCard);
					}
				}
				mSelectedCard = null;
				mSelectedPile = -1;
				mHighlightedPile = -1;
				if (mClearCorners) {
					for (int i = 0; i < 4; i++)
						mCorners[i].clearCorner();
				}
				playerWin();
				break;
			}
			mTable.postInvalidate();
		return true;
	}


	// User preferences methods

	/** Updates all user preferences. */
	public void updatePrefs() {
		// Normal preferences
		mDrawPileCount = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_drawPileCount), false);
		mComputerDelay = mPrefs.getInt(mContext.getResources().getString(R.string.pref_key_computerDelay), 1000);
		mHighlightCards = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_highlightCards), true);
		mAutosave = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_autosave), false);
		mEmptyDeckWarning = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_emptyDeckWarning), true);
		mSortHand = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_sortHand), false);
		mClearCorners = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_clearFullCorners), true);

		// Colors
		mDrawPileCountColor = mPrefs.getInt(mContext.getResources().getString(R.string.pref_key_drawPileCountColor), Color.WHITE);
		mScoresColor = mPrefs.getInt(mContext.getResources().getString(R.string.pref_key_scoreColor), Color.BLACK);

		// Card style
		String oldCardStyle = mCardStyle;
		mCardStyle = mPrefs.getString(mContext.getResources().getString(R.string.pref_key_cardImage), mContext.getResources().getString(R.string.cardImage_default));
		if (!mCardStyle.equals(oldCardStyle)) setCardStyle();

		// Card back images
		String oldStyle = mCardBackStyle;
		mCardBackStyle = mPrefs.getString(mContext.getResources().getString(R.string.pref_key_cardBack), mContext.getResources().getString(R.string.cardBack_default));	
		if (!mCardBackStyle.equals(oldStyle)) setCardBackImage();

		// Background image
		String oldImage = mTableImage;
		mTableImage = mPrefs.getString(mContext.getResources().getString(R.string.pref_key_tableImage), mContext.getResources().getString(R.string.tableImage_default));
		if (!mTableImage.equals(oldImage)) setTableImage();
	}

	/**
	 * Sets all the cards in play to the currently chosen style
	 * as specified by {@link #mCardStyle}.
	 */
	private void setCardStyle() {
		for (int i = 0; i < 4; i++) {
			if (mSides[i].first != null) mSides[i].first.setImage(mContext, mCardStyle);
			if (mSides[i].last != null) mSides[i].last.setImage(mContext, mCardStyle);
			if (mCorners[i].first != null) mCorners[i].first.setImage(mContext, mCardStyle);
			if (mCorners[i].last != null) mCorners[i].last.setImage(mContext, mCardStyle);
		}

		int count = (mPlayerCount == 1) ? 2 : mPlayerCount;
		for (int i = 0; i < count; i++) {
			int numCards = mHands[i].getCardCount();
			for (int j = 0; j < numCards; j++) {
				mHands[i].getCard(j).setImage(mContext, mCardStyle);
			}
		}

		mDeck.setCardStyle(mCardStyle);
	}

	/** Sets the card back images as specified by {@link #mCardBackStyle}. */
	private void setCardBackImage() {		
		if (mCardBackStyle.equals("ap")) {
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_back);
		} else if (mCardBackStyle.equals("cb1_blue")){
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cb1_blue);
		}  else if (mCardBackStyle.equals("cb1_fuscia")){
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cb1_fuscia);
		} else if (mCardBackStyle.equals("cb1_gray")){
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cb1_gray);
		} else if (mCardBackStyle.equals("cb1_green")){
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cb1_green);
		} else if (mCardBackStyle.equals("cb1_orange")){
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cb1_orange);
		} else if (mCardBackStyle.equals("cb1_red")){
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cb1_red);
		} else if (mCardBackStyle.equals("cb1_yellow")){
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cb1_yellow);
		} else {
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_backc);
		}

		// For showing computer's card backs upside down
		Matrix rotate = new Matrix();
		rotate.postRotate(180);
		mComputerCardBack = Bitmap.createBitmap(mCardBack, 0, 0, mCardBack.getWidth(), mCardBack.getHeight(), rotate, true);
	}

	/** Sets the table background image as specified by {@link #mTableImage}. */
	private void setTableImage() {
		int id;
		if (mTableImage.equals("bg_marble")) {
			id = R.drawable.bg_marble;
		} else if (mTableImage.equals("bg_greenfelt")) {
			id = R.drawable.bg_greenfelt;
		} else if (mTableImage.equals("bg_redfelt")) {
			id = R.drawable.bg_redfelt;
		} else if (mTableImage.equals("bg_bluefelt")) {
			id = R.drawable.bg_bluefelt;
		} else {
			id = R.drawable.bg_wood;
		}
		mTable.setBackgroundResource(id);
	}

	// Private methods
	/**
	 * Restore a previously saved game.
	 * @return True if the game was successfully restored, false otherwise.
	 */
	private boolean restoreGame() {
		try {
			synchronized(Main.sDataLock) {
				FileInputStream fip = mContext.openFileInput(mSaveString);
				ObjectInputStream in = new ObjectInputStream(fip);
				mTurn = in.readInt();

				mHands[0] = new Hand();
				int playerCount = in.readInt();
				Card c;
				for (int i = 0; i < playerCount; i++) {
					c = (Card) in.readObject();
					if (c != null) {
						c.setImage(mContext, mCardStyle);
						mHands[0].addCard(c);
					}
					
				}

				int computerCount = in.readInt();
				mHands[1] = new Hand();
				for (int i = 0; i < computerCount; i++) {
					c = (Card) in.readObject();
					if (c != null) {
						c.setImage(mContext, mCardStyle);
						mHands[1].addCard(c);
					}
				}

				int deckLeft = in.readInt();			
				for (int i = 0; i < 52; i++) {
					mDeck.setCardAt((Card)in.readObject(), i);
				}
				mDeck.setCardsUsed(52-deckLeft);

				for (int i = 0; i < 4; i++) {
					mSides[i] = (Pile)in.readObject();
					mSides[i].setImages(mContext, mCardStyle);
					mSides[i].mPileType = i;
				}
				for (int i = 0; i < 4; i++) {
					mCorners[i] = (Pile)in.readObject();
					mCorners[i].setImages(mContext, mCardStyle);
					mCorners[i].mPileType = i+4;
				}

				mCanUndo = in.readBoolean();
				mUndoCard = (Card)in.readObject();
				if (mUndoCard != null) mUndoCard.setImage(mContext, mCardStyle);
				mReplaceWithCard = (Card)in.readObject();
				if (mReplaceWithCard != null) mReplaceWithCard.setImage(mContext, mCardStyle);
				mUndoPos = in.readInt();
				mUndoIsSide = in.readBoolean();

				mContext.deleteFile(mSaveString);
				in.close();
				fip.close();
			}
		} catch (FileNotFoundException e) {
			mTable.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_fileNotFoundException), Toast.LENGTH_SHORT).show();
				}
			});
			return false;
		} catch (StreamCorruptedException e) {
			mTable.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_streamCorruptedException), Toast.LENGTH_SHORT).show();
				}
			});
			return false;
		} catch (IOException e) {
			mTable.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_IOException), Toast.LENGTH_SHORT).show();
				}
			});
			return false;
		} catch (ClassNotFoundException e) {
			mTable.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_classNotFoundException), Toast.LENGTH_SHORT).show();
				}
			});
			return false;
		}
		return true;
	}

	/**
	 * Highlights the pile at the position given by {@link #mHighlightedPile}. 
	 * @param canvas The canvas to draw the highlight to.
	 */
	private void drawHighlighted(Canvas canvas) {
		if (mHighlightedPile < 0) return;	
		// See if it is a corner or side to highlight
		if (mHighlightedPile <= 3) {
			// Highlight side
			if (mSelectedCard != null && !mSides[mHighlightedPile].playable(mSelectedCard)) return;		
			else if (mSelectedPile >= 0 && !mSides[mSelectedPile].playable(mSides[mHighlightedPile])) return;

			int rot = mSides[mHighlightedPile].mPileType;

			if (rot == Pile.UP || rot == Pile.DOWN) mSides[mHighlightedPile].drawHighlighted(canvas, mGlowNormal, mCardHeight);
			else if (rot == Pile.LEFT || rot == Pile.RIGHT) mSides[mHighlightedPile].drawHighlighted(canvas, mGlowSide, mCardHeight);

		} else {
			// Highlight corner
			if (mSelectedCard != null && !mCorners[mHighlightedPile-4].playable(mSelectedCard)) return;		
			else if (mSelectedPile >= 0 && !mSides[mSelectedPile].playable(mCorners[mHighlightedPile-4])) return;

			int rot = mCorners[mHighlightedPile-4].mPileType;

			if (rot == Pile.UP_LEFT || rot == Pile.DOWN_RIGHT) mCorners[mHighlightedPile-4].drawHighlighted(canvas, mGlowCorner1, mCardHeight);
			else if (rot == Pile.UP_RIGHT || rot == Pile.DOWN_LEFT) mCorners[mHighlightedPile-4].drawHighlighted(canvas, mGlowCorner2, mCardHeight);
		}

	}

	/**
	 * Finds the pile at the target x- and y-coordinates
	 * specified in {@link #mTarX} and {@link #mTarY} and
	 * calls the appropriate method.
	 * @return True if the selected card was played and
	 * 			should not be returned to the hand.
	 * @see {@link #playCard(int)}
	 * @see {@link #movePile(int)}
	 */
	private boolean findTargetPile() {
		// Check sides
		for (int i = 0; i < mSides.length; i++) {
			if (mTarX >= mSides[i].pos.left-(mCardWidth/2) && mTarX <= mSides[i].pos.right+(mCardWidth/2) &&
					mTarY >= mSides[i].pos.top-(mCardHeight/2) && mTarY <= mSides[i].pos.bottom+(mCardHeight/4)) {
				if (mSelectedCard != null) {
					return playCard(i);
				} else if (mSelectedPile != -1) {
					movePile(i);
				} else if (mSides[i] != null){
					mSelectedPile = i;
				}
			}
		}

		// Check corners
		if (mTarX >= mSides[0].pos.left && mTarX <= mSides[1].pos.left-5) {
			if (mTarY >= mSides[1].pos.top && mTarY <= mSides[0].pos.top-5) {
				if (mSelectedCard != null || mSelectedPile >= 0) {
					return playCorner(0);
				}
			}
			else if (mTarY >= mSides[0].pos.bottom+5 && mTarY <= mSides[3].pos.bottom) {
				if (mSelectedCard != null || mSelectedPile >= 0) {
					return playCorner(3);
				}
			}
		} else if (mTarX >= mSides[1].pos.right+5 && mTarX <= mSides[2].pos.right) {
			if (mTarY >= mSides[1].pos.top-5 && mTarY <= mSides[2].pos.top) {
				if (mSelectedCard != null || mSelectedPile >= 0) {
					return playCorner(1);
				}
			} else if (mTarY >= mSides[2].pos.bottom+5 && mTarY <= mSides[3].pos.bottom) {
				if (mSelectedCard != null || mSelectedPile >= 0) {
					return playCorner(2);
				}
			}
		}
		return false;
	}

	/**
	 * Moves the selected pile to the pile at the given destination.
	 * @param dest The pile to move the selected pile to.
	 * @see {@link #findTargetPile()}
	 */
	private void movePile(int dest) {
		if (mSides[mSelectedPile].moveTo(mSides[dest])) {
			mCanUndo = false;
		}
	}

	/**
	 * Attempts to play the selected card on the side pile at the given destination.
	 * @param dest The side pile to attempt to play the selected card on.
	 * @return True if the selected card was played on the pile, false otherwise.
	 * @see {@link #findTargetPile()}
	 */
	private boolean playCard(int dest) {
		if (mSelectedCard != null) {
			Card tempUndo = mSelectedCard;
			Card tempReplace = mSides[dest].last;
			if (mSides[dest].play(mSelectedCard)) {
				mCanUndo = true;
				mUndoIsSide = true;
				mUndoPos = dest;
				mUndoCard = tempUndo;
				mReplaceWithCard = tempReplace;
				return true;
			}

			for (int i = 0; i < mSides.length; i++) {
				if (mSides[i].first == null) {
					if (mSides[dest].playUnder(mSelectedCard)) {
						mCanUndo = false;
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Attempts to play the selected card or selected pile on
	 * the corner pile at the given destination.
	 * @param dest The corner pile to play the selected card or pile on.
	 * @return True if the selected card was played on the pile, false otherwise.
	 */
	private boolean playCorner(int dest) {
		if (mSelectedCard != null){
			Card tempUndo = mSelectedCard;
			Card tempReplace = mCorners[dest].last;
			if (mCorners[dest].play(mSelectedCard)) {
				mCanUndo = true;
				mUndoIsSide = false;
				mUndoPos = dest;
				mUndoCard = tempUndo;
				mReplaceWithCard = tempReplace;
				return true;
			}
		} else if (mSelectedPile >= 0) {
			if (mSides[mSelectedPile].moveTo(mCorners[dest])) {
				mCanUndo = false;
			}
		}
		return false;
	}

	/**
	 * Finds the pile to be highlighted, if there is one, and sets
	 * {@link #mHighlightedPile} to the correct value.
	 */
	private void highlightPile() {
		if (mSelectedCard == null && mSelectedPile < 0) return; 	// Make sure there is a card or pile being moved

		// Check sides
		mHighlightedPile = -1;
		for (int i = 0; i < mSides.length; i++) {
			if (mTarX >= mSides[i].pos.left-(mCardWidth/2) && mTarX <= mSides[i].pos.right+(mCardWidth/2) &&
					mTarY >= mSides[i].pos.top-(mCardHeight/2) && mTarY <= mSides[i].pos.bottom+(mCardHeight/4)) {
				mHighlightedPile = i;
			}
		}
		
		// If a side is highlighted, don't bother trying to highlight a corner
		if (mHighlightedPile >= 0) return;
		
		// Check corners
		if (mTarX >= mSides[0].pos.left && mTarX <= mSides[1].pos.left-5) {
			if (mTarY >= mSides[1].pos.top && mTarY <= mSides[0].pos.top-5) {
				mHighlightedPile = 4;
			}
			else if (mTarY >= mSides[0].pos.bottom+5 && mTarY <= mSides[3].pos.bottom) {
				mHighlightedPile = 7;
			}
		} else if (mTarX >= mSides[1].pos.right+5 && mTarX <= mSides[2].pos.right) {
			if (mTarY >= mSides[1].pos.top-5 && mTarY <= mSides[2].pos.top) {
				mHighlightedPile = 5;
			} else if (mTarY >= mSides[2].pos.bottom+5 && mTarY <= mSides[3].pos.bottom) {
				mHighlightedPile = 6;
			}
		}
	}

	/**
	 * Checks if the computer has won. If so, updates stats accordingly.
	 * @return True if the computer has won, false otherwise.
	 */
	private boolean compWin() {
		mTable.postInvalidate();
		if (mPlayerCount == 1 && mHands[1].getCardCount() == 0) {
			if (mUsername != null && !mUsername.equals(mContext.getResources().getString(R.string.username_none)) && !mUsername.equals("")) {
				StatsManager sm = new StatsManager(mContext);
				try {
					sm.playerFinishedGame(mUsername, false);
				} catch (StreamCorruptedException e) {
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
			}
			mCanUndo = false;
			mWinner = 1;
			setChanged();
			notifyObservers();
			mTable.postInvalidate();
			return true;
		}
		return false;
	}

	/**
	 * Checks if the current player has won. If so and the game
	 * is single player, updates stats accordingly.
	 * @return True if the current player has won, false otherwise.
	 */
	private boolean playerWin() {
		if ((mPlayerCount > 1 && mHands[mTurn].getCardCount() == 0) || (mPlayerCount == 1 && mHands[0].getCardCount() == 0)) {
			if (mPlayerCount == 1 && mUsername != null && !mUsername.equals(mContext.getResources().getString(R.string.username_none)) && !mUsername.equals("")) {
				StatsManager sm = new StatsManager(mContext);
				try {
					sm.playerFinishedGame(mUsername, true);
				} catch (StreamCorruptedException e) {
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
			}
			mCanUndo = false;
			mWinner = mTurn;
			setChanged();
			notifyObservers();
			mTable.postInvalidate();
			return true;
		}
		return false;
	}
}
