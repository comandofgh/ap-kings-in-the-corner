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

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CardTableView extends View {
	// Global variables
	private GameEngine mGameEngine;
	
	// Constructors
	public CardTableView(Context context) {
		super (context);
	}
	
	public CardTableView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/** Set the game engine to draw to **/
	public void setGameEngine(GameEngine ge) {
		mGameEngine = ge;
	}

	@Override
	protected void onDraw(Canvas c) {
		if (mGameEngine != null) {
			mGameEngine.onDraw(c);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e){
		if (mGameEngine == null) {
			return true;
		}
		return mGameEngine.onTouchEvent(e);
	}
	
	public int getViewWidth() {
		return this.getWidth();
	}
	
	public int getViewHeight() {
		return this.getHeight();
	}
}
