/**
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

import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/** An object representing a hand of cards. */
public class Hand {

	/** The cards in the hand. */
	private Vector<Card> mHand;
	
	/** Whether or not this hand is to be sorted by color */
	private boolean mSortedColor;
	
	/** The maximum width this hand can be when drawn to a canvas. */
	private int mHandWidth;
	
	/** The width of a single card in this hand when drawn to a canvas. */
	private int mCardWidth;
	
	/** The y-coordinate for the top of the hand when drawn to a canvas. */
	private int mTop;
	
	/** Whether or not this hand is being hovered over by a card. */
	private boolean mHovered;
	
	/** Whether or not this hand needs to be shifted back to a normal position. */
	private boolean mNeedsShifted;
	
	/** The last good index obtained when finding the index of a hovered or selected card. */
	private int mLastGoodIndex;

	/**
	 * Constructs an empty hand object.
	 */
	public Hand() {
		mHand = new Vector<Card>();
		mSortedColor = false;
		mHovered = false;
		mNeedsShifted = true;
	}

	/** Removes all cards in this hand. */
	public void clear() {
		mHand.removeAllElements();
	}

	/**
	 * Adds a card to this hand.
	 * @param card The card to add to this hand. If the card
	 * 				is null, no card will be added.
	 */
	public void addCard(Card card) {
		if (card == null) return;
		mHand.addElement(card);
		mNeedsShifted = true;
		sortByColor();
		shiftCardsNormal();
	}
	
	/**
	 * Adds a card to this hand at the last good index obtained
	 * from getIndex(int tarx).
	 * @param card The card to add to this hand. If the card
	 * 				is null, no card will be added.
	 */
	public void addCardAtIndex(Card card) {
		if (card == null) return;
		
		mHand.add(mLastGoodIndex, card);
		mNeedsShifted = true;
		sortByColor();
		shiftCardsNormal();
	}

	/**
	 * Removes a card from this hand.
	 * @param card The card to remove from this hand. If the
	 * 				card is not found, no card is removed.
	 */
	public void removeCard(Card card) {
		if (mHand.removeElement(card)) {
			mNeedsShifted = true;
			shiftCardsNormal();
		}
		
	}

	/**
	 * Removes a card from this hand at a given index.
	 * @param index The index in this hand for the card to
	 * 				remove. If the index is not in the bounds of
	 * 				this hand, no card is removed.
	 */
	public void removeCard(int index) {
		if (index >= 0 && index < mHand.size()) {
			mHand.removeElementAt(index);
			mNeedsShifted = true;
			shiftCardsNormal();
		}
	}

	/**
	 * Gets the number of cards in this hand.
	 * @return The number of cards in this hand.
	 */
	public int getCardCount() {
		return mHand.size();
	}

	/**
	 * Gets a card from this hand at a given index.
	 * @param index The index in this hand for the desired card.
	 * @return The card at the given index. If the index is not in the
	 * 			bounds of this hand, null is returned.
	 */
	public Card getCard(int index) {
		if (index >= 0 && index < mHand.size())
			return mHand.elementAt(index);
		else
			return null;
	}

	/**
	 * Sorts the cards in this hand by suit and cards with
	 * the same suit are sorted by value.
	 */
	public void sortBySuit() {
		Vector<Card> newHand = new Vector<Card>();
		while (mHand.size() > 0) {
			int pos = 0;  // Position of minimal card.
			Card c = mHand.elementAt(0);  // Minimal card.
			for (int i = 1; i < mHand.size(); i++) {
				Card c1 = mHand.elementAt(i);
				if ( c1.getSuit() < c.getSuit() ||
						(c1.getSuit() == c.getSuit() && c1.getValue() < c.getValue()) ) {
					pos = i;
					c = c1;
				}
			}
			mHand.removeElementAt(pos);
			newHand.addElement(c);
		}
		mHand = newHand;
		mNeedsShifted = true;
		shiftCardsNormal();
	}

	/**
	 * Sorts the cards in this hand by value and cards
	 * with the same value are sorted by suit.
	 */
	public void sortByValue() {
		Vector<Card> newHand = new Vector<Card>();
		while (mHand.size() > 0) {
			int pos = 0;  // Position of minimal card.
			Card c = mHand.elementAt(0);  // Minimal card.
			for (int i = 1; i < mHand.size(); i++) {
				Card c1 = mHand.elementAt(i);
				if ( c1.getValue() < c.getValue() ||
						(c1.getValue() == c.getValue() && c1.getSuit() < c.getSuit()) ) {
					pos = i;
					c = c1;
				}
			}
			mHand.removeElementAt(pos);
			newHand.addElement(c);
		}
		mHand = newHand;
		mNeedsShifted = true;
		shiftCardsNormal();
	}

	/** 
	 * Sorts the cards in this hand by color and cards of the
	 * same color are sorted by value. The sorting is only done
	 * if {@link mSortedColor} is set to true.
	 */
	public void sortByColor() {
		if (!mSortedColor) return;
		
		Vector<Card> newHand = new Vector<Card>();
		while (mHand.size() > 0) {
			int pos = 0;  // Position of minimal card.
			Card c = mHand.elementAt(0);  // Minimal card.
			for (int i = 1; i < mHand.size(); i++) {
				Card c1 = mHand.elementAt(i);
				if ( ((c1.getSuit() == 0 || c1.getSuit() == 3) && (c.getSuit() == 1 || c.getSuit() == 2)) ||
						(((c1.getSuit() == 0 || c1.getSuit() == 3) && (c.getSuit() == 0 || c.getSuit() == 3)) &&
								c1.getValue() < c.getValue()) ||
								(((c1.getSuit() == 1 || c1.getSuit() == 2) && (c.getSuit() == 1 || c.getSuit() == 2)) &&
										c1.getValue() < c.getValue()) ) {
					pos = i;
					c = c1;
				}
			}
			mHand.removeElementAt(pos);
			newHand.addElement(c);
		}
		mHand = newHand;
	}

	/**
	 * Toggles the value of {@link mSortedColor} then calls {@link sortByColor()}
	 * and {@link shiftCardsNormal()}.
	 */
	public void toggleSortColor() {
		mSortedColor = !mSortedColor;
		mNeedsShifted = true;
		sortByColor();
		shiftCardsNormal();
	}

	/**
	 * Gets whether or not this hand is sorted by color.
	 * @return True if this hand is sorted by color, false otherwise.
	 */
	public boolean isSortedColor() {
		return mSortedColor;
	}
	
	/**
	 * Shifts cards as needed for hovering a card over
	 * this hand at the given position.
	 * @param tarx The target x-coordinate where another
	 * 				card is being hovered over this hand.
	 */
	public void hoverCardAt(int tarx) {
		int numCards = mHand.size();
		if (numCards <= 0) return;
		
		mHovered = true;
		
		int pos = getIndex(tarx);
		
		if (pos == -1) return;
		
		int visibleWidth = 0;
		
		if (numCards == 1 || (numCards+1)*mCardWidth <= mHandWidth)  visibleWidth = mCardWidth;
		else visibleWidth = (mHandWidth-2*mCardWidth)/(numCards-1);
		
		int cardSpace = (numCards+1) * mCardWidth;
		int indent = 0;
		if (cardSpace <= mHandWidth) {
			indent = (mHandWidth-cardSpace)/2;
		}
		
		// Shift all cards after to the right
		int margin = 0;
		for (int i = 0; i < numCards; i++) {
			if (i < pos) margin = indent + i * visibleWidth;
			else margin = indent + (i*visibleWidth) + mCardWidth;
			mHand.elementAt(i).setPos(margin, mTop);
		}		
	}
	
	/**
	 * Gets the card in this hand at a given x-coordinate.
	 * @param tarx  The target x-coordinate to find the
	 * 				card in this hand at.
	 * @return The card from this hand at the given x-coordinate.
	 * 			If a card is found, that card is also removed
	 * 			from this hand.
	 */
	public Card getTargetCard(int tarx) {		
		int count = mHand.size();
		if (count <= 0) return null;	// Make sure the hand is not empty
		
		int pos = getIndex(tarx);
		
		if (pos == -1 || pos >= count) return null;
		
		Card card = mHand.elementAt(pos);
		mHand.removeElement(card);
		hoverCardAt(tarx);
		return card;
	}

	/**
	 * Gets the index into this hand for a given x-coordinate.
	 * @param tarx The target x-coordinate to get the index from.
	 * @return The index into this hand for the card at the given
	 * 			x-coordinate.
	 */
	private int getIndex(int tarx) {
		int numCards = mHand.size();
		if (numCards <= 0) return -1;	// Make sure the hand is not empty

		int visibleWidth;
		int ans = -1;
		
		// Find the index of the card at the target x-coordinate
		if (mHovered) {
			
			if ((numCards+1)*mCardWidth <= mHandWidth)  visibleWidth = mCardWidth;
			else visibleWidth = (mHandWidth-2*mCardWidth)/(numCards-1);
			
			for (int i = 0; i < numCards; i++) {
				int x = mHand.elementAt(i).getX();
				if (tarx >= x-(visibleWidth/2) && tarx < x-(visibleWidth/2)+visibleWidth) {
					ans = i;
					break;
				}
			}
			
			// Do special checks for last card
			if (visibleWidth == mCardWidth) {		
				int x = mHand.lastElement().getX() + mCardWidth/2;
				if (tarx > x) {
					ans = numCards;
				}
			} else {
				if (tarx > mHandWidth - mCardWidth/2) {
					ans = numCards;
				}
			}

		} else {
			if (numCards*mCardWidth <= mHandWidth) visibleWidth = mCardWidth;
			else visibleWidth = (mHandWidth-mCardWidth)/(numCards-1);

			for (int i = 0; i < numCards; i++) {
				int x = mHand.elementAt(i).getX();
				if (i == numCards-1) visibleWidth = mCardWidth;	// The last card can be seen in full
				if (tarx >= x && tarx < x + visibleWidth) {
					ans = i;
					break;
				}
			}
		}
		if (ans >= 0 && ans <= numCards) mLastGoodIndex = ans;
		return ans;
	}
	
	/**
	 * Initializes values used when drawing this hand to a canvas.
	 * @param top The y-coordinate for the top of this hand.
	 * @param handWidth The maximum width of this hand when drawn to a canvas.
	 * @param cardWidth The width of a single card in this hand when drawn to a canvas.
	 */
	public void initializeDraw(int top, int handWidth, int cardWidth) {
		mTop = top;
		mHandWidth = handWidth;
		mCardWidth = cardWidth;
		mNeedsShifted = true;
		shiftCardsNormal();
	}
	
	/**
	 * Shifts the cards in this hand to a normal position if
	 * {@link mNeedsShifted} is true and {@link mHovered} is false.
	 */
	public void shiftCardsNormal() {
		// Check if the cards actually need to be shifted
		if (!mNeedsShifted && !mHovered) return;
		
		mHovered = false;
		int numCards = mHand.size();
		
		int offset = 0;
		
		// Do count <= 1 to make sure we don't ever divide by zero in the else clause
		if ( numCards * mCardWidth <= mHandWidth)
			offset = mCardWidth;
		
		else offset = (mHandWidth-mCardWidth)/(numCards-1);
		
		int cardSpace = numCards * mCardWidth;
		int indent = 0;
		if (cardSpace <= mHandWidth) {
			indent = (mHandWidth-cardSpace)/2;
		}

		for (int i = 0; i < numCards; i++) {
			int x = indent + i*offset;
			mHand.elementAt(i).setPos(x, mTop);
		}
	}
	
	/**
	 * Draws each card in this hand to a canvas.
	 * @param c The canvas to draw this hand to.
	 * @param paint The paint used to draw this hand.
	 * @param cardBack A card back image to draw if this hand is
	 * 					not face up. If this is null, the hand
	 * 					will be drawn face up.
	 */
	public void draw(Canvas c, Paint paint, Bitmap cardBack) {
		int count = mHand.size();

		for (int i = 0; i < count; i++) {
			mHand.elementAt(i).draw(c, paint, cardBack);
		}
	}
}
