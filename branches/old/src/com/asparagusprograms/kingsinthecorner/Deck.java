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

import android.content.Context;

/** An object representing an ordinary deck of 52 playing cards. */
public class Deck {

	/** An {@code array} containing the 52 cards in the deck. */
	private Card[] deck;
	
	/** The number of cards that have been dealt from the deck. */
	private int mCardsUsed;
	
	/** The {@code context} associated with this deck's resources. */
	private Context mContext;
	
	/** 
	 * The style of cards to use for this deck.
	 * @see {@link #setCardStyle(String)}.
	 */
	private String mCardStyle;

	/**
	 * Constructs a new deck object.
	 * @param context The context associated with this deck's resources.
	 * @param style The style of cards to use for this deck.
	 */
	public Deck(Context context, String style) {
		// Create an unshuffled deck of cards.
		mContext = context;
		if (style == null || style == "") mCardStyle = "classic";
		else mCardStyle = style;
		
		deck = new Card[52];
		int cardCt = 0; // How many cards have been created so far.
		for ( int suit = 0; suit <= 3; suit++ ) {
			for ( int value = 1; value <= 13; value++ ) {
				deck[cardCt] = new Card(value,suit);
				cardCt++;
			} 
		}
		mCardsUsed = 0;
	}

	/**
	 * Put all the cards back into the deck and shuffle them into
	 * a random order.
	 */
	public void shuffle() {
		Card temp;
		for ( int i = 51; i > 0; i-- ) {
			int rand = (int)(Math.random()*(i+1));
			temp = deck[i];
			deck[i] = deck[rand];
			deck[rand] = temp;
		}
		mCardsUsed = 0;
	}

	/**
	 * Getst he number of cards remaining in the deck.
	 * @return The number of cards left in the deck.
	 */
	public int cardsLeft() {
		return 52 - mCardsUsed;
	}

	/**
	 * Deals the top card from the deck.
	 * @return The card dealt from the deck.
	 */
	public Card dealCard() {
		// Deals one card from the deck and returns it.
		if (mCardsUsed == 52)
			shuffle();
		mCardsUsed++;
		Card c = deck[mCardsUsed - 1];
		c.setImage(mContext, mCardStyle);
		deck[mCardsUsed - 1] = null;
		return c;
	}

	/**
	 * Gets the {@link Card} at a given location in the deck.
	 * @param index The index into this deck to get the card.
	 * @return The card at the given index. If the index
	 * 			is not within the bounds of the deck, null
	 * 			is returned.
	 */
	public Card cardAt(int index) {
		if (index < 0 || index > deck.length) return null;
		
		return deck[index];
	}

	/**
	 * Sets the {@link Card} in this deck at a given index.
	 * @param card The card to place in this deck.
	 * @param index The index to place the card at.
	 */
	public void setCardAt(Card card, int index) {
		deck[index] = card;
	}

	/**
	 * Sets the number of cards that have been dealt from this deck.
	 * @param numCardsUsed The number of cards used.
	 */
	public void setCardsUsed(int numCardsUsed) {
		mCardsUsed = numCardsUsed;
	}
	
	/**
	 * Sets the style of card to use for this deck.
	 * @param style The style of cards to use.
	 */
	public void setCardStyle(String style) {
		mCardStyle = style;
	}
}
