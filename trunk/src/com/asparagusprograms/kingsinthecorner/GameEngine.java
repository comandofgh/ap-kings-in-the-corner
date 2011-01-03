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
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.widget.Toast;


/** Handles game functions and provides access to necessary information */
public class GameEngine extends Observable implements Runnable {
	// Types for computer errors
	private final static int STYLE_MOVE = 1,
							 STYLE_PLAY = 2;
	
	// User preferences
	private boolean mDrawPileCount, mHighlightCards, mAutosave, mEmptyDeckWarning,
					mWarnedEmpty, mSortHand;
	private String mUsername; // Only set during creation so it is not possible to switch mid-game
	private String mCardStyle;
	private boolean mShowComputerHand; // Cheats
	private int mComputerDelay;
	private int mDifficulty; // Higher is easier

	// Global variables
	private Context mContext;
	private SharedPreferences mPrefs;
	private String mSaveString;
	private int mPlayerCount;
	private int mTurn;		
	private int mWinner;
	private volatile boolean mPaused, mStop;

	//Undo variables
	private boolean mCanUndo;
	private boolean mUndoIsSide;
	private int mUndoPos;
	private Card mUndoCard;
	private Card mReplaceWithCard;

	//Hand, deck, etc
	private Hand[] mHands;
	private Deck mDeck;
	private Pile[] mSides, mCorners;
	private int mSelectedCard;		// -1=none
	private int mSelectedPile;		// -1=none
	private int mHighlightedPile;	// -1=none

	// Drawing variables
	private boolean mDrawInitialized;
	private Paint mPaint;
	private int mViewHeight, mViewWidth, mCardHeight, mCardWidth, mScreenHeight;
	private boolean mHideHand;
	private CardTableView mTable;
	private int tarx, tary;

	// Stored bitmaps
	private Bitmap[] mPlayerBitmaps;
	private Bitmap mCardBack;
	private Bitmap mGlowNormal, mGlowSide, mGlowCorner1, mGlowCorner2;

	//Rects for placing bitmaps
	private Rect draw;

	/** Creates a new GameEngine */
	public GameEngine(Context context, int numPlayers, CardTableView table) {
		mContext = context;
		mTable = table;
		mPlayerCount = numPlayers;
		
		Initialize();
	}
	
	/** Initialize global variables **/
	private void Initialize() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mUsername = mPrefs.getString(mContext.getResources().getString(R.string.pref_key_username), mContext.getResources().getString(R.string.username_none));
		mCardStyle = mPrefs.getString(mContext.getResources().getString(R.string.pref_key_cardImage), mContext.getResources().getString(R.string.cardImage_default));
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
		
		if (mCardStyle != null && mCardStyle.equals("classic")) {
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_backc);
		}
		else {
			mCardBack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_back);
		}
		mGlowNormal = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.glow);
		mGlowSide = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.glow_side);
		mGlowCorner1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.glow_corner1);
		mGlowCorner2 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.glow_corner2);
		
		mPaint = new Paint();
		mSelectedCard = -1;
		mSelectedPile = -1;
		mHighlightedPile = -1;
		mCanUndo = false;
		
		int count = (mPlayerCount == 1) ? 2 : mPlayerCount;  // Number of hands to deal, if it's a single player game this needs to be 2
		mHands = new Hand[count];
		
		mWarnedEmpty = false;
		mDrawInitialized = false;
		mPaused = false;
		mStop = false;
		mDeck = new Deck(mContext, mCardStyle);
		mSides = new Pile[4];
		mCorners = new Pile[4];
		draw = new Rect();
	}

	// Cheats
	/** Auto Win **/
	public void autoWin() {
		mHands[0].clear();
		playerWin();
	}

	/** Trash opponent **/
	public void trash() {
		Card joker = new Card(0, 4);
		joker.setImage(mContext, mCardStyle);
		mHands[1].addCard(joker);
		mTable.postInvalidate();
	}

	/** Returns the current turn */
	public int turn() {return mTurn;}

	/** Returns the winner if there is one else returns -1*/
	public int winner() {return mWinner;}

	/** Returns true if the empty deck warning should be displayed */
	public boolean warnEmpty() {
		if (mDeck.cardsLeft() == 0 && mEmptyDeckWarning && !mWarnedEmpty) {
			mWarnedEmpty = true;
			return true;
		}
		return false;
	}

	/** Returns if the game is a single player game */
	public boolean isSinglePlayer() {return (mPlayerCount == 1);}

	/** Whether or not there is a move that can be undone */
	public boolean canUndo() {
		if (mUndoCard == null) return false;
		return mCanUndo;
		}

	/** Set whether to hide the current hand */
	public void hideHand(boolean bool) {
		mHideHand = bool;
		mTable.postInvalidate();
	}

	/** Set that the game is no longer in firstGame mode */
	public void firstGame() {
		mPrefs.edit().putBoolean(mContext.getResources().getString(R.string.pref_key_firstGame), false).commit();
	}
	
	/** Sets whether or not the user is currently in a game **/
	public void inGame(boolean bool) {
		mPrefs.edit().putBoolean(mContext.getResources().getString(R.string.pref_key_inGame), bool).commit();
	}

	//Gameplay methods
	/** Undo the last move */
	public void undo() {
		if (mUndoIsSide) {
			mSides[mUndoPos].undo(mReplaceWithCard);
		} else {
			mUndoCard.setImage(mContext, mCardStyle);
			mCorners[mUndoPos].undo(mReplaceWithCard);
		}
		mUndoCard.setRotate(Card.NORMAL);
		mHands[mTurn].addCard(mUndoCard);
		mCanUndo = false;
		
		mHands[mTurn].sortByColor();
		
		mTable.postInvalidate();
	}

	/** Pause the computer playing */
	public void pause() {
		mPaused = true;
		if (mAutosave) save();
	}

	/** Resume the computer playing */
	public void resume() {mPaused = false;}
	
	public void start() {
		Thread thread = new Thread(this);
			thread.start();
	}
	
	public void stop() {mStop = true;}
	
	/** Whether the computer makes a mistake or not. Based on difficulty and type of play */
	private boolean computerError(int type) {
		if (mDifficulty == 0) return false; // No errors on hard
		int percent = 0;

		switch (type) {
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

	/** The computer playing */
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
		for (int i = 0; i < 4; i++) mCorners[i].clearCorner();
		if (!playing) nextTurn();

		} catch (NullPointerException e) {}
	}

	/** Handles going to the next player's turn */
	public void nextTurn() {
		mCanUndo = false;
		mSelectedPile = -1;
		mSelectedCard = -1;
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
		mHands[mTurn].sortByColor();
		
		setChanged();
		notifyObservers();
		mTable.postInvalidate();
		
		if (mPlayerCount == 1 && mTurn == 1) start();
	}

	/** Sets up a new game */
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

	/** Saves the current game */
	public void save() {
		if (mWinner == -1 && mPlayerCount == 1 && mDeck != null) {
			try {
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

	/** Attempts to restore a saved game */	
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

	/** Deletes the saved game for the current user */
	public void deleteSave() {
		try {
			mContext.deleteFile(mSaveString);
		} catch (NullPointerException e) {
			mTable.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_nullPointerException), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	/** Toggles the "sortedness" of the current hand */
	public void sortHand() {
		mHands[mTurn].toggleSortColor();
		mHands[mTurn].sortByColor();
		mTable.postInvalidate();
	}

	/** Whether the current hand is sorted or not **/
	public boolean handSorted() {
		if (mHands == null || mHands[mTurn] == null) {
			return false;
		}
		return mHands[mTurn].isSortedColor();
	}

	// Drawing methods
	public boolean drawInitialized() {
		return mDrawInitialized;
	}
	
	/** Set the height of the screen */
	public void screenHeight(int height) {mScreenHeight = height;}

	/** Called whenever the screen is being drawn */
	public void onDraw(Canvas c) {
		if (mStop) return;
		if (!mDrawInitialized) {
			// Set scaled dimensions
			mCardWidth = mCardBack.getScaledWidth(c);
			mCardHeight = mCardBack.getScaledHeight(c);
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
		}
		if (mDeck != null) {
			if (mWinner == -1) {	// Nobody has won so draw the game data
				if (mDeck.cardsLeft() != 0) {	// Deck isn't empty so draw the deck
					c.drawBitmap(mCardBack, draw.left, draw.top, null);
					if (mDrawPileCount) {	// Print the number of cards in the draw pile
						if (mCardStyle.equals("simple")) mPaint.setColor(Color.WHITE);
						else mPaint.setColor(Color.BLACK);

						mPaint.setTextSize(mCardHeight/2);
						mPaint.setAntiAlias(true);
						DecimalFormat df = new DecimalFormat("00");
						String count = df.format(mDeck.cardsLeft());
						int height = (int) mPaint.descent();
						int width = (int) mPaint.measureText(count);
						c.drawText(count, (mViewWidth/2)-(width/2), (mViewHeight/2)-(height), mPaint);
					}
				}
				if (mHands[mTurn] != null && !mHideHand) {
					drawHand(c);
					if (mPlayerCount > 1) {
						c.drawBitmap(mPlayerBitmaps[mTurn], 0, 0, null); // Draws the current turn
					}
					// Draw the scores for each player
					if (mPlayerCount > 1) {
						mPaint.setTextSize(mCardHeight/6);
						mPaint.setColor(Color.BLACK);
						mPaint.setAntiAlias(true);
						for (int i = 0; i < mPlayerCount; i++) {
							String s = "Player " + (i+1) + ": " + mHands[i].getCardCount() + " ";
							c.drawText(s, (int)((mViewWidth)-mPaint.measureText(s)-2), (int)((i+1)*(mPaint.descent()-mPaint.ascent())), mPaint);
						}
					}
					else drawComputerHand(c);
				}
				if (mSides != null) {
					// Loop through and draw each side
					for (int i = 0; i < mSides.length; i++) {
						if (i != mSelectedPile && mSides[i] != null) {
							mSides[i].draw(c, mCardHeight);
						}
					}
				}
				if (mCorners != null) {
					// Loop through and draw each corner
					for (int i = 0; i < mCorners.length; i++) {
						if (mCorners[i] != null) mCorners[i].draw(c, mCardHeight);
					}
				}
				if (mSides != null && mCorners != null && mHighlightCards) {
					drawHighlighted(c);
				}
				// Draw the selected card/pile
				if (mSelectedCard >= 0) {
					Card card = mHands[mTurn].getCard(mSelectedCard);
					card.setRotate(Card.NORMAL);
					c.drawBitmap(card.getImage(), tarx-(mCardWidth/2), tary-(mCardHeight/6), null);
				} else if (mSelectedPile >= 0)
					mSides[mSelectedPile].drawSelected(c, mCardWidth, mCardHeight, tarx, tary);
			} else {
				mPaint.setColor(Color.BLACK);
				mPaint.setTextSize(mCardHeight/4);
				mPaint.setAntiAlias(true);
				String s1 = "Press Menu to play again";
				String s2 = "or Back to exit.";
				c.drawText(s1, (mViewWidth/2)-(int)(mPaint.measureText(s1)/2), (mViewHeight/2)-(int)mPaint.descent(), mPaint);
				c.drawText(s2, (mViewWidth/2)-(int)(mPaint.measureText(s2)/2), (mViewHeight/2)+(int)mPaint.descent()-(int)mPaint.ascent(), mPaint);
				mTable.postInvalidate();
			}				
		}
	}

	/** Handles user touch events on the given View */
	public boolean onTouchEvent(MotionEvent e) {
		if ( (!(mPlayerCount == 1 && mTurn == 1) || mPlayerCount > 1) && mWinner == -1) {	// Player's turn so handle touch events
			int eventaction = e.getAction();
			tarx=(int)e.getRawX();
			tary=(int)e.getRawY()-(mScreenHeight-mViewHeight);
			switch (eventaction) { 
			case (MotionEvent.ACTION_DOWN):
				if (tary >= (mViewHeight-mCardHeight)) {
					if (mSelectedCard == -1) {
						mSelectedCard = findTargetHand();
					}
				} else if (tarx > draw.left && tarx < draw.right && tary > draw.top && tary < draw.bottom) {
					if (mWinner == -1 && (mPlayerCount > 1 || (mPlayerCount == 1 && mTurn == 0))) {
						mSelectedCard = -1;
						mSelectedPile = -1;
						nextTurn();
					}
				} else {
					mSelectedCard = -1;
					mSelectedPile = -1;
					findTargetPile();
				}
			break;
			case MotionEvent.ACTION_MOVE:
				if (mSelectedCard >= 0) {
					if (mHighlightCards) {
						highlightPile();
					}
				} else if (mSelectedPile >= 0 && mSides[mSelectedPile] != null) {
					if (mHighlightCards){
						highlightPile();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					findTargetPile();
				}
				mSelectedCard = -1;
				mSelectedPile = -1;
				mHighlightedPile = -1;
				for (int i = 0; i < 4; i++) mCorners[i].clearCorner();
				playerWin();
				break;
			}
			mTable.postInvalidate();
		}
		return true;
	}

	
	// User preferences methods

	/** Updates all user preferences */
	public void updatePrefs() {
		// Normal preferences
		mDrawPileCount = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_drawPileCount), false);
		mComputerDelay = mPrefs.getInt(mContext.getResources().getString(R.string.pref_key_computerDelay), 1000);
		mHighlightCards = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_highlightCards), true);
		mAutosave = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_autosave), false);
		mEmptyDeckWarning = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_emptyDeckWarning), true);
		mSortHand = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_sortHand), false);
		// Cheats
		mShowComputerHand = mPrefs.getBoolean(mContext.getResources().getString(R.string.pref_key_showComputerHand), false);
	}

	// Private methods
	/** Restore a previously saved game */
	private boolean restoreGame() {
		try {
			FileInputStream fip = mContext.openFileInput(mSaveString);
			ObjectInputStream in = new ObjectInputStream(fip);
			mTurn = in.readInt();
			
			mHands[0] = new Hand();
			int playerCount = in.readInt();
			Card c;
			for (int i = 0; i < playerCount; i++) {
				c = (Card) in.readObject();
				if (c != null) c.setImage(mContext, mCardStyle);
				mHands[0].addCard(c);
			}
			
			int computerCount = in.readInt();
			mHands[1] = new Hand();
			for (int i = 0; i < computerCount; i++) {
				c = (Card) in.readObject();
				if (c != null) c.setImage(mContext, mCardStyle);
				mHands[1].addCard(c);
			}

			int deckLeft = in.readInt();			
			for (int i = 0; i < 52; i++) {
				mDeck.setCardAt(i, (Card)in.readObject());
			}
			mDeck.setCardsUsed(52-deckLeft);
			
			for (int i = 0; i < 4; i++) {
				mSides[i] = (Pile)in.readObject();
				mSides[i].setImages(mContext, mCardStyle);
				mSides[i].rot = i;
			}
			for (int i = 0; i < 4; i++) {
				mCorners[i] = (Pile)in.readObject();
				mCorners[i].setImages(mContext, mCardStyle);
				mCorners[i].rot = i+4;
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

	private void drawHand(Canvas c) {
		int turn = (mPlayerCount == 1 && mTurn == 1) ? 0 : mTurn;
		int x = 0;
		int xmax = 0;
		int y = mViewHeight - mCardHeight;
		int center = mViewWidth/2;
		int count = mHands[turn].getCardCount();
		if (count == 1) {
			if (mHands[turn].getCard(0) != null && mSelectedCard != 0) {
				c.drawBitmap(mHands[turn].getCard(0).getImage(), center-(mCardWidth/2), y, null);
				mHands[turn].getCard(0).setX(center-(mCardWidth/2));
				mHands[turn].getCard(0).setXMax(center-(mCardWidth/2)+mCardWidth);
			}
		} else if (count == 2) {
			if (mHands[turn].getCard(0) != null && mSelectedCard != 0) {
				c.drawBitmap(mHands[turn].getCard(0).getImage(), center-mCardWidth-5, y, null);
				mHands[turn].getCard(0).setX(center-mCardWidth-5);
				mHands[turn].getCard(0).setXMax(center-5);
			}
			if (mHands[turn].getCard(1) != null && mSelectedCard != 1) {
				c.drawBitmap(mHands[turn].getCard(1).getImage(), center+5, y, null);
				mHands[turn].getCard(1).setX(center+5);
				mHands[turn].getCard(1).setXMax(center+5+mCardWidth);
			}
		} else if (count == 3) {
			if (mHands[turn].getCard(0) != null && mSelectedCard != 0) {
				c.drawBitmap(mHands[turn].getCard(0).getImage(), center-(mCardWidth/2)-mCardWidth-10, y, null);
				mHands[turn].getCard(0).setX(center-(mCardWidth/2)-mCardWidth-10);
				mHands[turn].getCard(0).setXMax(center-(mCardWidth/2)-10);
			}
			if (mHands[turn].getCard(1) != null && mSelectedCard != 1) {
				c.drawBitmap(mHands[turn].getCard(1).getImage(), center-(mCardWidth/2), y, null);
				mHands[turn].getCard(1).setX(center-(mCardWidth/2));
				mHands[turn].getCard(1).setXMax(center-(mCardWidth/2)+mCardWidth);
			}
			if (mHands[turn].getCard(2) != null && mSelectedCard != 2) {
				c.drawBitmap(mHands[turn].getCard(2).getImage(), center+(mCardWidth/2)+10, y, null);
				mHands[turn].getCard(2).setX(center+(mCardWidth/2)+10);
				mHands[turn].getCard(2).setXMax(center+(mCardWidth/2)+10+mCardWidth);
			}
		} else if (count == 4) {
			if (mHands[turn].getCard(0) != null && mSelectedCard != 0) {
				c.drawBitmap(mHands[turn].getCard(0).getImage(), center-(mCardWidth*2)-15, y, null);
				mHands[turn].getCard(0).setX(center-(mCardWidth*2)-15);
				mHands[turn].getCard(0).setXMax(center-mCardWidth-15);
			}
			if (mHands[turn].getCard(1) != null && mSelectedCard != 1) {
				c.drawBitmap(mHands[turn].getCard(1).getImage(), center-mCardWidth-5, y, null);
				mHands[turn].getCard(1).setX(center-mCardWidth-5);
				mHands[turn].getCard(1).setXMax(center-5);
			}
			if (mHands[turn].getCard(2) != null && mSelectedCard != 2) {
				c.drawBitmap(mHands[turn].getCard(2).getImage(), center+5, y, null);
				mHands[turn].getCard(2).setX(center+5);
				mHands[turn].getCard(2).setXMax(center+5+mCardWidth);
			}
			if (mHands[turn].getCard(3) != null && mSelectedCard != 3) {
				c.drawBitmap(mHands[turn].getCard(3).getImage(), center+mCardWidth+15, y, null);
				mHands[turn].getCard(3).setX(center+mCardWidth+15);
				mHands[turn].getCard(3).setXMax(center+mCardWidth+15+mCardWidth);
			}
		} else {
			Card card;
			for (int i = 0; i < count; i++) {
				card = mHands[turn].getCard(i);
				x = ( (i * ((mViewWidth-mCardWidth)/(count-1))) );
				if (i < count-1) {
					xmax = ( ((i+1) * ((mViewWidth-mCardWidth)/(count-1))) );
				} else {
					xmax = x+mCardWidth;
				}
				if (card != null && mSelectedCard != i) {
					card.setX(x);
					card.setY(y);
					card.setXMax(xmax);
					c.drawBitmap(card.getImage(), x, y, null);
				}
			}
		}
	}

	private void drawComputerHand(Canvas c) {
		int count = mHands[1].getCardCount();
		int y = -(mCardHeight/2);
		int x = 0;
		int center = mViewWidth/2;
		if (mShowComputerHand) {
			for (int i = 0; i < count; i++) {
				if (count > 1) {
					x = ( (i * ((mViewWidth-mCardWidth)/(count-1))) );
					c.drawBitmap(mHands[1].getCard(i).getImage(), x, y, null);
				} else {
					c.drawBitmap(mHands[1].getCard(0).getImage(), center-(mCardWidth/2), y, null);
				}
			}
		}
		else {
			if (count == 1) {
				c.drawBitmap(mCardBack, center-(mCardWidth/2), y, null);
			} else if (count == 2) {
				c.drawBitmap(mCardBack, center-mCardWidth-5, y, null);
				c.drawBitmap(mCardBack, center+5, y, null);
			} else if (count == 3) {
				c.drawBitmap(mCardBack, center-(mCardWidth/2)-mCardWidth-10, y, null);
				c.drawBitmap(mCardBack, center-(mCardWidth/2), y, null);
				c.drawBitmap(mCardBack, center+(mCardWidth/2)+10, y, null);
			} else if (count == 4) {
				c.drawBitmap(mCardBack, center-(mCardWidth*2)-15, y, null);
				c.drawBitmap(mCardBack, center-mCardWidth-5, y, null);
				c.drawBitmap(mCardBack, center+5, y, null);
				c.drawBitmap(mCardBack, center+mCardWidth+15, y, null);
			} else {
				for (int i = 0; i < count; i++) {
					x = ( (i * ((mViewWidth-mCardWidth)/(count-1))) );
					c.drawBitmap(mCardBack, x, y, null);
				}
			}
		}
	}
	
	private void drawHighlighted(Canvas c) {
		if (mHighlightedPile < 0) return;	
		// See if it is a corner or side to highlight
		if (mHighlightedPile <= 3) {
			// Highlight side
			if (mSelectedCard >= 0 && !mSides[mHighlightedPile].playable(mHands[mTurn].getCard(mSelectedCard))) return;		
			else if (mSelectedPile >= 0 && !mSides[mSelectedPile].playable(mSides[mHighlightedPile])) return;
			
			int rot = mSides[mHighlightedPile].rot;
			
			if (rot == Pile.UP || rot == Pile.DOWN) mSides[mHighlightedPile].drawHighlighted(c, mGlowNormal, mCardHeight);
			else if (rot == Pile.LEFT || rot == Pile.RIGHT) mSides[mHighlightedPile].drawHighlighted(c, mGlowSide, mCardHeight);
			
		} else {
			// Highlight corner
			if (mSelectedCard >= 0 && !mCorners[mHighlightedPile-4].playable(mHands[mTurn].getCard(mSelectedCard))) return;		
			else if (mSelectedPile >= 0 && !mSides[mSelectedPile].playable(mCorners[mHighlightedPile-4])) return;
			
			int rot = mCorners[mHighlightedPile-4].rot;
			
			if (rot == Pile.UP_LEFT || rot == Pile.DOWN_RIGHT) mCorners[mHighlightedPile-4].drawHighlighted(c, mGlowCorner1, mCardHeight);
			else if (rot == Pile.UP_RIGHT || rot == Pile.DOWN_LEFT) mCorners[mHighlightedPile-4].drawHighlighted(c, mGlowCorner2, mCardHeight);
		}
		
	}

	private int findTargetHand() {
		int cards = mHands[mTurn].getCardCount();
		for (int i = 0; i < cards; i++) {
			Card c = mHands[mTurn].getCard(i);
			int cx = c.getX();
			int mx = c.getXMax();
			if ( ((i == cards-1) && tarx >= cx && tarx <= cx + mCardWidth) || (tarx >= cx && tarx <= mx)) { 
				return i;
			}
		}
		return -1;
	}

	private void findTargetPile() {
		// Check sides
		for (int i = 0; i < mSides.length; i++) {
			if (tarx >= mSides[i].pos.left-(mCardWidth/2) && tarx <= mSides[i].pos.right+(mCardWidth/2) &&
					tary >= mSides[i].pos.top-(mCardHeight/2) && tary <= mSides[i].pos.bottom+(mCardHeight/4)) {
				if (mSelectedCard >= 0) {
					playCard(i);
				} else if (mSelectedPile != -1) {
					movePile(i);
				} else if (mSides[i] != null){
					mSelectedPile = i;
				}
			}
		}
		
		// Check corners
		if (tarx >= mSides[0].pos.left && tarx <= mSides[1].pos.left-5) {
			if (tary >= mSides[1].pos.top && tary <= mSides[0].pos.top-5) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					playCorner(0);
				}
			}
			else if (tary >= mSides[0].pos.bottom+5 && tary <= mSides[3].pos.bottom) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					playCorner(3);
				}
			}
		} else if (tarx >= mSides[1].pos.right+5 && tarx <= mSides[2].pos.right) {
			if (tary >= mSides[1].pos.top-5 && tary <= mSides[2].pos.top) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					playCorner(1);
				}
			} else if (tary >= mSides[2].pos.bottom+5 && tary <= mSides[3].pos.bottom) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					playCorner(2);
				}
			}
		}
	}

	private void movePile(int dest) {
		if (mSides[mSelectedPile].moveTo(mSides[dest])) {
			mCanUndo = false;
		}
	}

	private void playCard(int dest) {
		if (mSelectedCard >= 0) {
			mUndoCard = mHands[mTurn].getCard(mSelectedCard);
			mReplaceWithCard = mSides[dest].last;
			if (mSides[dest].play(mHands[mTurn].getCard(mSelectedCard))) {
				mHands[mTurn].removeCard(mSelectedCard);
				mCanUndo = true;
				mUndoIsSide = true;
				mUndoPos = dest;
			}

			for (int i = 0; i < mSides.length; i++) {
				if (mSides[i].first == null) {
					if (mSides[dest].playUnder(mHands[mTurn].getCard(mSelectedCard))) {
						mHands[mTurn].removeCard(mSelectedCard);
						mCanUndo = false;
						break;
					}
				}
			}
		}
	}

	private void playCorner(int dest) {
		if (mSelectedCard >= 0){
			mUndoCard = mHands[mTurn].getCard(mSelectedCard);
			mReplaceWithCard = mCorners[dest].last;
			if (mCorners[dest].play(mHands[mTurn].getCard(mSelectedCard))) {
				mHands[mTurn].removeCard(mSelectedCard);
				mCanUndo = true;
				mUndoIsSide = false;
				mUndoPos = dest;
			}
		} else if (mSelectedPile >= 0) {
			if (mSides[mSelectedPile].moveTo(mCorners[dest])) {
				mCanUndo = false;
			}
		}
	}

	private void highlightPile() {
		// Check sides
		mHighlightedPile = -1;
		for (int i = 0; i < mSides.length; i++) {
			if (tarx >= mSides[i].pos.left-(mCardWidth/2) && tarx <= mSides[i].pos.right+(mCardWidth/2) &&
					tary >= mSides[i].pos.top-(mCardHeight/2) && tary <= mSides[i].pos.bottom+(mCardHeight/4)) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					mHighlightedPile = i;
				}
			}
		}
		if (mHighlightedPile >= 0) return; // If a side is highlighted, don't try to highlight a corner
		// Check corners
		if (tarx >= mSides[0].pos.left && tarx <= mSides[1].pos.left-5) {
			if (tary >= mSides[1].pos.top && tary <= mSides[0].pos.top-5) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					mHighlightedPile = 4;
				}
			}
			else if (tary >= mSides[0].pos.bottom+5 && tary <= mSides[3].pos.bottom) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					mHighlightedPile = 7;
				}
			}
		} else if (tarx >= mSides[1].pos.right+5 && tarx <= mSides[2].pos.right) {
			if (tary >= mSides[1].pos.top-5 && tary <= mSides[2].pos.top) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					mHighlightedPile = 5;
				}
			} else if (tary >= mSides[2].pos.bottom+5 && tary <= mSides[3].pos.bottom) {
				if (mSelectedCard >= 0 || mSelectedPile >= 0) {
					mHighlightedPile = 6;
				}
			}
		}
	}

	/** Check if the computer has won */
	private boolean compWin() {
		mTable.postInvalidate();
		if (mPlayerCount == 1 && mHands[1].getCardCount() == 0) {
			if (mUsername != null && !mUsername.equals(mContext.getResources().getString(R.string.username_none)) && !mUsername.equals("")) {
				try {
					FileInputStream fis = mContext.openFileInput(mUsername);
					ObjectInputStream in = new ObjectInputStream(fis);
					Player p = (Player)in.readObject();
					in.close();
					fis.close();

					p.Lose();

					FileOutputStream fop = mContext.openFileOutput(mUsername, Context.MODE_PRIVATE);
					ObjectOutputStream out = new ObjectOutputStream(fop);
					out.writeObject(p);
					out.close();
					fop.close();
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
				} catch (ClassNotFoundException e) {
					mTable.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_classNotFoundException), Toast.LENGTH_SHORT).show();
						}
					});
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

	/** Check if the current player has won */	
	private boolean playerWin() {
		if ((mPlayerCount > 1 && mHands[mTurn].getCardCount() == 0) || (mPlayerCount == 1 && mHands[0].getCardCount() == 0)) {
			if (mPlayerCount == 1 && mUsername != mContext.getResources().getString(R.string.username_none) && mUsername != null && !mUsername.equals("")) {
				try {
					FileInputStream fis = mContext.openFileInput(mUsername);
					ObjectInputStream in = new ObjectInputStream(fis);
					Player p = (Player)in.readObject();
					in.close();
					fis.close();

					p.Win();

					FileOutputStream fop = mContext.openFileOutput(mUsername, Context.MODE_PRIVATE);
					ObjectOutputStream out = new ObjectOutputStream(fop);
					out.writeObject(p);
					out.close();
					fop.close();
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
				} catch (ClassNotFoundException e) {
					mTable.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_classNotFoundException), Toast.LENGTH_SHORT).show();
						}
					});
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

	/** Null values to ensure they get picked up by Garbage Collector **/
	public void freeUp() {
		mPrefs.edit().putBoolean(mContext.getResources().getString(R.string.pref_key_inGame), false).commit();
		mUndoCard = null;
		mReplaceWithCard = null;
		for (int i = 0; i < mHands.length; i++) mHands[i] = null;	
		for (int i = 0; i < mSides.length; i++) mSides[i] = null;
		for (int i = 0; i < mCorners.length; i++) mCorners[i] = null;
		mDeck = null;
		mSides = null;
		mCorners = null;
		if (mPlayerCount > 1) {
			for (int i = 0; i < mPlayerBitmaps.length; i++) {
				if (mPlayerBitmaps[i] != null) mPlayerBitmaps[i].recycle();
			}
		}
		mGlowNormal.recycle();
		mGlowSide.recycle();
		mGlowCorner1.recycle();
		mGlowCorner2.recycle();
		mCardBack.recycle();
		mContext = null;
		mTable = null;		
		System.gc();
	}
}
