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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Pile implements Serializable {

	// Side pile variables
	public final static int LEFT = 0,
	UP = 1,
	RIGHT = 2,
	DOWN = 3;

	// Corner pile variables
	public final static int UP_LEFT = 4,
	UP_RIGHT = 5,
	DOWN_RIGHT = 6,
	DOWN_LEFT = 7;

	private static final long serialVersionUID = 5498297232455179659L;

	public Card first; // Top card, null if the pile is empty
	public Card last;  // Card covering all other cards, null if the top card is the only card

	public transient Rect pos; // The rectangle specifying where this pile is placed on the table
	public transient int mPileType; // The rotation of the pile

	/** Construct the pile **/
	public Pile(int pileType, Card first, Card last) {
		this.first = first;
		this.last = last;
		mPileType = pileType;
		pos = new Rect();
	}

	/** Attempt to play a card on this pile **/
	public boolean play(Card c) {
		if (c == null) return false; // Null check

		// Play the card on the pile if it's empty
		if (first == null && (!isCorner() || c.getValue() == 13)) {
			// Only play King on empty corner
			first = c;
			return true;
		}
		// Play the card on the last card on the pile
		else if ( (last != null && c.covers(last)) || (last == null && c.covers(first)) ) {
			last = c;
			return true;
		}

		return false;
	}

	public boolean playUnder(Card c) {
		if (first != null && first.covers(c)) {
			if (last == null) last = first;
			first = c;
			return true;
		}
		return false;
	}

	/** Attempt to move this pile onto another pile **/
	public boolean moveTo(Pile p) {
		// If this pile is empty, return false
		if (first == null || p == null) return false;

		// If target pile is empty, check to move King onto a corner
		if (p.first == null) {
			if (p.isCorner() && first.getValue() == 13) {
				p.first = first;
				p.last = last;
				first = null;
				last = null;
				return true;
			}
		}

		// If target pile is not empty, move as normal
		else if (p.last == null && first.covers(p.first)) {
			if (last == null) p.last = first;
			else p.last = last;
			first = null;
			last = null;
			return true;
		}

		else if (first.covers(p.last)) {
			if (last == null) p.last = first;
			else p.last = last;
			first = null;
			last = null;
			return true;
		}

		return false;
	}

	public boolean playable(Card c) {
		if (c == null) return false;

		if (first == null && (!isCorner() || c.getValue() == 13)) {
			return true;
		}

		else if ( (last != null && c.covers(last)) || (last == null && c.covers(first)) ) {
			return true;
		}
		return false;
	}

	public boolean playable(Pile p) {
		if (first == null || p == null) return false;

		if (p.first == null) {
			if (p.isCorner() && first.getValue() == 13) {
				return true;
			}
		}

		else if (p.last == null && first.covers(p.first)) {
			return true;
		}

		else if (first.covers(p.last)) {
			return true;
		}
		return false;
	}

	public void draw(Canvas c, int cardHeight, Context context, String style) {
		if (first == null || c == null) return;

		int rotation = 0;

		if (mPileType == UP || mPileType == DOWN) rotation = 0;
		else if (mPileType == LEFT || mPileType == RIGHT) rotation = 90;
		else if (mPileType == UP_LEFT || mPileType == DOWN_RIGHT) rotation = 135;
		else if (mPileType == UP_RIGHT || mPileType == DOWN_LEFT) rotation = 45;

		if (first != null) first.setRotate(rotation, context, style);
		if (last != null) last.setRotate(rotation, context, style);

		if (!isCorner()) {
			// Draw side
			if (mPileType == LEFT) {
				c.drawBitmap(first.getImage(), pos.left+(cardHeight/4), pos.top, null);
				if (last != null) c.drawBitmap(last.getImage(), pos.left, pos.top, null);
			}

			else if (mPileType == UP) {
				c.drawBitmap(first.getImage(), pos.left, pos.top+(cardHeight/4), null);
				if (last != null) c.drawBitmap(last.getImage(), pos.left, pos.top, null);
			}

			else if (mPileType == RIGHT) {
				c.drawBitmap(first.getImage(), pos.left, pos.top, null);
				if (last != null) c.drawBitmap(last.getImage(), pos.left+(cardHeight/4), pos.top, null);
			}

			else if (mPileType == DOWN) {
				c.drawBitmap(first.getImage(), pos.left, pos.top, null);
				if (last != null) c.drawBitmap(last.getImage(), pos.left, pos.top+(cardHeight/4), null);
			}
		} else {
			// Draw corner
			if (mPileType == UP_LEFT) {
				c.drawBitmap(first.getImage(), pos.left, pos.top, null);
				if (last != null) c.drawBitmap(last.getImage(), pos.left-(cardHeight/6), pos.top-(cardHeight/6), null);
			}

			else if (mPileType == UP_RIGHT) {
				c.drawBitmap(first.getImage(), pos.left, pos.top, null);
				if (last != null) c.drawBitmap(last.getImage(), pos.left+(cardHeight/6), pos.top-(cardHeight/6), null);
			}

			else if (mPileType == DOWN_RIGHT) {
				c.drawBitmap(first.getImage(), pos.left, pos.top, null);
				if (last != null) c.drawBitmap(last.getImage(), pos.left+(cardHeight/6), pos.top+(cardHeight/6), null);
			}

			else if (mPileType == DOWN_LEFT) {
				c.drawBitmap(first.getImage(), pos.left, pos.top, null);
				if (last != null) c.drawBitmap(last.getImage(), pos.left-(cardHeight/6), pos.top+(cardHeight/6), null);
			}
		}
	}

	public void drawSelected(Canvas c, int cardWidth, int cardHeight, int tarx, int tary, Context context, String style) {
		if (first == null) return;

		first.setRotate(0, context, style);
		c.drawBitmap(first.getImage(), tarx-(cardWidth/2), tary-(cardHeight/4), null);

		if (last != null) {
			last.setRotate(0, context, style);
			c.drawBitmap(last.getImage(), tarx-(cardWidth/2), tary-(cardHeight/4)+(cardHeight/4), null);
		}
	}

	public void drawHighlighted(Canvas c, Bitmap h, int cardHeight) {
		if (!isCorner()) {
			// Draw side
			if (mPileType == LEFT) {
				if (last == null) c.drawBitmap(h, pos.left+(cardHeight/4), pos.top, null);
				else c.drawBitmap(h, pos.left, pos.top, null);
			}

			else if (mPileType == UP) {
				if (last == null) c.drawBitmap(h, pos.left, pos.top+(cardHeight/4), null);
				else c.drawBitmap(h, pos.left, pos.top, null);
			}

			else if (mPileType == RIGHT) {
				if (last == null) c.drawBitmap(h, pos.left, pos.top, null);
				else c.drawBitmap(h, pos.left+(cardHeight/4), pos.top, null);
			}

			else if (mPileType == DOWN) {
				if (last == null) c.drawBitmap(h, pos.left, pos.top, null);
				else c.drawBitmap(h, pos.left, pos.top+(cardHeight/4), null);
			}
		} else {
			// Draw corner
			if (mPileType == UP_LEFT) {
				if (last == null) c.drawBitmap(h, pos.left, pos.top, null);
				else c.drawBitmap(h, pos.left-(cardHeight/6), pos.top-(cardHeight/6), null);
			}

			else if (mPileType == UP_RIGHT) {
				if (last == null) c.drawBitmap(h, pos.left, pos.top, null);
				else c.drawBitmap(h, pos.left+(cardHeight/6), pos.top-(cardHeight/6), null);
			}

			else if (mPileType == DOWN_RIGHT) {
				if (last == null) c.drawBitmap(h, pos.left, pos.top, null);
				else c.drawBitmap(h, pos.left+(cardHeight/6), pos.top+(cardHeight/6), null);
			}

			else if (mPileType == DOWN_LEFT) {
				if (last == null) c.drawBitmap(h, pos.left, pos.top, null);
				else c.drawBitmap(h, pos.left-(cardHeight/6), pos.top+(cardHeight/6), null);
			}
		}
	}

	public void undo(Card c) {
		if (last == null) first = c; // c should only ever be null here since there is only 1 card on the pile to undo

		else last = c;
	}

	private boolean isCorner() {return (mPileType > 3);}

	public void clearCorner() {
		if (isCorner() && first != null && last != null && first.getValue() == 13 && last.getValue() == 1) {
			first = null;
			last = null;
		}
	}

	public void setImages(Context c, String style) {
		if (first != null) first.setImage(c, style);
		if (last != null) last.setImage(c, style);
	}
}