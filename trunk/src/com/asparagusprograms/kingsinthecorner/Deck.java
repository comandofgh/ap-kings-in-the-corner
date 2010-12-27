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

/* 
    An object of type Deck represents an ordinary deck of 52 playing cards.
    The deck can be shuffled, and cards can be dealt from the deck.
 */

public class Deck {

	private Card[] deck;   // An array of 52 Cards, representing the deck.
	private int cardsUsed; // How many cards have been dealt from the deck.
	private Context context;
	private String cardStyle;

	public Deck(Context c, String style) {
		// Create an unshuffled deck of cards.
		context = c;
		if (style == null || style == "") cardStyle = "classic";
		else cardStyle = style;
		
		deck = new Card[52];
		int cardCt = 0; // How many cards have been created so far.
		for ( int suit = 0; suit <= 3; suit++ ) {
			for ( int value = 1; value <= 13; value++ ) {
				deck[cardCt] = new Card(value,suit);
				cardCt++;
			} 
		}
		cardsUsed = 0;
	}

	public void shuffle() {
		// Put all the used cards back into the deck, and shuffle it into
		// a random order.
		Card temp;
		for ( int i = 51; i > 0; i-- ) {
			int rand = (int)(Math.random()*(i+1));
			temp = deck[i];
			deck[i] = deck[rand];
			deck[rand] = temp;
		}
		cardsUsed = 0;
	}

	public int cardsLeft() {
		// As cards are dealt from the deck, the number of cards left
		// decreases.  This function returns the number of cards that
		// are still left in the deck.
		return 52 - cardsUsed;
	}

	public Card dealCard() {
		// Deals one card from the deck and returns it.
		if (cardsUsed == 52)
			shuffle();
		cardsUsed++;
		Card c = deck[cardsUsed - 1];
		c.setImage(context, cardStyle);
		deck[cardsUsed - 1] = null;
		return c;
	}

	public Card cardAt(int x) {
		return deck[x];
	}

	public void setCardAt(int x, Card c) {
		deck[x] = c;
	}

	public void setCardsUsed(int x) {
		cardsUsed = x;
	}
}
