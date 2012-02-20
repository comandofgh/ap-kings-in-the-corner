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

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CardTableView extends View {
	// Global variables
	
	/** The game engine used to actually draw the game. */
	private GameEngine mGameEngine;
	
	// Constructors
	/**
	 * Constructs a new {@link CardTableView}.
	 * @param context The context associated with this view.
	 */
	public CardTableView(Context context) {
		super (context);
	}
	
	/**
	 * Constructs a new {@link CardTableView}.
	 * @param context The context associated with this view.
	 * @param attrs The attribute set given to this view.
	 */
	public CardTableView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Sets the game engine used to draw the game.
	 * @param ge The game engine to use to draw the game.
	 */
	public void setGameEngine(GameEngine ge) {
		mGameEngine = ge;
	}

	/** 
	 * If a game engine is set, this will call the
	 * {@link GameEngine#onDraw(Canvas)} method. 
	 * Otherwise, this method does nothing.
	 */
	@Override
	protected void onDraw(Canvas c) {
		if (mGameEngine != null) {
			mGameEngine.onDraw(c);
		}
	}
	
	/**
	 * If a game engine is set, this will call the
	 * {@link GameEngine#onTouchEvent(MotionEvent)} method.
	 * @param event The motion event.
	 * @return The value returned from {@link GameEngine#onTouchEvent(MotionEvent)}.
	 * 			If no game engine is set, returns false.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if (mGameEngine == null) {
			return false;
		}
		return mGameEngine.onTouchEvent(event);
	}
	
	/**
	 * Gets the current width of this view.
	 * @return The current width of this view.
	 */
	public int getViewWidth() {
		return this.getWidth();
	}
	
	/**
	 * Gets the current height of this view.
	 * @return The current height of this view.
	 */
	public int getViewHeight() {
		return this.getHeight();
	}
}
