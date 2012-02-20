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

import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
   An object representing one of the 52 cards in a
   standard deck of playing cards.  Each card has a suit and
   a value.
*/
public class Card implements Serializable {

	private static final long serialVersionUID = 8373398048173195242L;

	/** Code for one of the four suits. */
	public final static int SPADES = 0,
                            HEARTS = 1,
                            DIAMONDS = 2,
                            CLUBS = 3;
                           
	/** 
	 * Code for a non-numeric card. Numeric 
	 * cards have their value for their code.
	 */
    public final static int ACE = 1,          // Codes for the non-numeric cards.
                            JACK = 11,        //   Cards 2 through 10 have their 
                            QUEEN = 12,       //   numerical values for their codes.
                            KING = 13;
              
    /** 
     * The suit of this card. Can be one of {@link #SPADES},
     * {@link #HEARTS}, {@link #DIAMONDS}, or {@link #CLUBS}.
     */
    private final int mSuit;   // The suit of this card, one of the constants
                              //    SPADES, HEARTS, DIAMONDS, CLUBS.
                
    /** The value of this card. An integer from 1 to 13 */
    private final int mValue;
    
    /** The current x-coordinate for this card when drawn to a canvas. */
    private transient int mXPos;
    /** The current y-coordinate for this card when drawn to a canvas. */
    private transient int mYPos;
    
    /** 
     * The rotation of this card from its normal upright position
     * when drawn to a canvas.
     */
    private transient int mCurrentRotation = 0;
    
    /** The image for this card to drawn to a canvas. */
    private transient Bitmap image;
    
    /**
     * Construct a card with the specified value and suit.
     * If the parameters are outside their acceptable ranges,
     * the constructed card object will be invalid.
     * @param value The card's value. Must be between 1 and 13.
     * @param suit The card's suit. Must be between 0 and 3.
     */
    public Card(int value, int suit) {
        mValue = value;
        mSuit = suit;
    }
    
    /**
     * Sets the x- and y-coordinate for drawing this card to a canvas.
     * @param x The x-coordinate for the left side of this card.
     * @param y The y-coordinate for the top of this card.
     */
    public void setPos(int x, int y) {
    	mXPos = x;
    	mYPos = y;
    }
    
    /**
     * Gets the x-coordinate of this card.
     * @return The x-coordinate for the left side of this card.
     */
    public int getX() {
    	return mXPos;
    }
    
    /**
     * Gets the y-coordinate of this card.
     * @return The y-coordinate for the top of this card.
     */
    public int getY() {
    	return mYPos;
    }
        
    /**
     * Gets the suit of this card as an integer.
     * @return The suit of this card represented as an integer.
     */
    public int getSuit() {
        return mSuit;
    }
    
    /**
     * Gets the value of this card as an integer.
     * @return The value of this card represented as an integer.
     */
    public int getValue() {
        return mValue;
    }
    
    /**
     * Gets the suit of this card as a string.
     * @return A string representation for the suit of this card.
     */
    public String getSuitAsString() {
        switch ( mSuit ) {
           case SPADES:   return "Spades";
           case HEARTS:   return "Hearts";
           case DIAMONDS: return "Diamonds";
           case CLUBS:    return "Clubs";
           default:       return "??";
        }
    }
    
    /**
     * Gets the value of this card as a string.
     * @return A string representation for the value of this card.
     */
    public String getValueAsString() {
        switch ( mValue ) {
           case 1:   return "Ace";
           case 2:   return "2";
           case 3:   return "3";
           case 4:   return "4";
           case 5:   return "5";
           case 6:   return "6";
           case 7:   return "7";
           case 8:   return "8";
           case 9:   return "9";
           case 10:  return "10";
           case 11:  return "Jack";
           case 12:  return "Queen";
           case 13:  return "King";
           default:  return "??";
        }
    }
    
    @Override
	public String toString() {
        return getValueAsString() + " of " + getSuitAsString();
    }
    
    /**
     * Determines if this card and another card are the same.
     * @param c The card to compare to this card.
     * @return True if this card and the given card have
     * 			the same suit and value, false otherwise.
     */
    public boolean isSame(Card c) {
    	if (c == null) {
    		return false;
    	} else if (this.mSuit == c.mSuit && this.mValue == c.mValue) {
    		return true;
    	} else {
    		return false;
    	}
    }

    /**
     * Determines if this card covers a given card.
     * @param c The card to check if this card covers.
     * @return True if this card covers the given card, false otherwise.
     */
    public boolean covers(Card c) {
    	if (c == null || this == null || this.mSuit == 4) {
    		return false;
    	} else if (this.mSuit == 0 || this.mSuit == 3) {
    		if (c.mSuit == 1 || c.mSuit == 2) {
    			if (this.mValue+1 == c.mValue) {
    				return true;
    			}
    		}
    	} else {
    		if (c.mSuit == 0 || c.mSuit == 3) {
    			if (this.mValue+1 == c.mValue) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * Gets the image associated with this card.
     * @return The image associated with this card.
     */
    public Bitmap getImage() {
    	return image;
    }
    
    /**
     * Gets the width of this card's image.
     * @return The width of this card's image.
     * 			If no image is set, returns 0.
     */
    public int getWidth() {
    	if (image == null) return 0;
    	
    	return image.getWidth();
    }
    
    /**
     * Gets the height of this card's image.
     * @return The height of this card's image.
     * 			If no image is set, returns 0.
     */
    public int getHeight() {
    	if (image == null) return 0;
    	
    	return image.getHeight();
    }
    
    /**
     * Rotates this card's image.
     * @param degrees The number of degrees to rotate this card's image.
     * @param context The context used to get image resources if needed.
     * @param style The style of card to use if needed.
     */
    public void setRotate(int degrees, Context context, String style) {
    	// Make sure there is an image and it actually needs rotated
    	if (image == null || mCurrentRotation == degrees) return;
    	
    	// Reset the image to 0 rotation
    	if (mCurrentRotation != 0) {
    		setImage(context, style);
    	}
    	
    	// Rotate the image to the given degrees
    	if (degrees != 0) {
    		Matrix m = new Matrix();
        	m.postRotate(degrees);
    		image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);
    	}
    	mCurrentRotation = degrees;
    }
    
    /**
     * Sets the image for this card.
     * @param context The context used to get image resources.
     * @param style The style of card to use.
     */
    public void setImage(Context context, String style) {
    	if (style != null && style.equals("classic")) {
    		setImageClassic(context);
    	} else {
    		setImageSimple(context);
    	}
    	
    	mCurrentRotation = 0;
    }
    
    /**
     * Draws this card a canvas.
     * @param c The canvas to draw this card to.
     * @param paint The paint to use when drawing.
     * @param cardBack A card back image to draw if this card
     * 					is not face up. If this is null, the card
     * 					will be drawn face up.
     */
    public void draw(Canvas c, Paint paint, Bitmap cardBack) {
    	if (cardBack == null)
    		c.drawBitmap(image, mXPos, mYPos, paint);
    	
    	else
    		c.drawBitmap(cardBack, mXPos, mYPos, paint);
    }
    
    /**
     * Sets this card's image to the Simple card style.
     * @param context The context used to get image resources.
     */
    private void setImageSimple(Context context) {
		switch (mSuit) {
		case Card.CLUBS:    
			switch (mValue) {
			case 1:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c1);
				break;
			case 2:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c2);
				break;
			case 3:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c3);
				break;
			case 4:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c4);
				break;
			case 5:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c5);
				break;
			case 6:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c6);
				break;
			case 7:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c7);
				break;
			case 8:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c8);
				break;
			case 9:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c9);
				break;
			case 10:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c10);
				break;
			case 11:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c11);
				break;
			case 12:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c12);
				break;
			case 13:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c13);
				break;
			}
			break;
		case Card.DIAMONDS: 
			switch (mValue) {
			case 1:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d1);
				break;
			case 2:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d2);
				break;
			case 3:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d3);
				break;
			case 4:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d4);
				break;
			case 5:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d5);
				break;
			case 6:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d6);
				break;
			case 7:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d7);
				break;
			case 8:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d8);
				break;
			case 9:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d9);
				break;
			case 10:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d10);
				break;
			case 11:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d11);
				break;
			case 12:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d12);
				break;
			case 13:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d13);
				break;
			}
			break;
		case Card.HEARTS:   
			switch (mValue) {
			case 1:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h1);
				break;
			case 2:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h2);
				break;
			case 3:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h3);
				break;
			case 4:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h4);
				break;
			case 5:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h5);
				break;
			case 6:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h6);
				break;
			case 7:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h7);
				break;
			case 8:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h8);
				break;
			case 9:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h9);
				break;
			case 10:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h10);
				break;
			case 11:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h11);
				break;
			case 12:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h12);
				break;
			case 13:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h13);
				break;
			}
			break;
		case Card.SPADES:
			switch (mValue) {
			case 1:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s1);
				break;
			case 2:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s2);
				break;
			case 3:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s3);
				break;
			case 4:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s4);
				break;
			case 5:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s5);
				break;
			case 6:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s6);
				break;
			case 7:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s7);
				break;
			case 8:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s8);
				break;
			case 9:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s9);
				break;
			case 10:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s10);
				break;
			case 11:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s11);
				break;
			case 12:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s12);
				break;
			case 13:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s13);
				break;
			}
			break;
		case 4: // Joker
			image = BitmapFactory.decodeResource(context.getResources(), R.drawable.joker);
			break;
		}
    }
    
    /**
     * Sets this card's image to the Classic card style.
     * @param context The context used to get image resources.
     */
    private void setImageClassic(Context context) {
		switch (mSuit) {
		case Card.CLUBS:    
			switch (mValue) {
			case 1:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c1c);
				break;
			case 2:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c2c);
				break;
			case 3:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c3c);
				break;
			case 4:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c4c);
				break;
			case 5:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c5c);
				break;
			case 6:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c6c);
				break;
			case 7:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c7c);
				break;
			case 8:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c8c);
				break;
			case 9:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c9c);
				break;
			case 10:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c10c);
				break;
			case 11:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c11c);
				break;
			case 12:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c12c);
				break;
			case 13:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.c13c);
				break;
			}
			break;
		case Card.DIAMONDS: 
			switch (mValue) {
			case 1:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d1c);
				break;
			case 2:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d2c);
				break;
			case 3:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d3c);
				break;
			case 4:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d4c);
				break;
			case 5:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d5c);
				break;
			case 6:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d6c);
				break;
			case 7:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d7c);
				break;
			case 8:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d8c);
				break;
			case 9:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d9c);
				break;
			case 10:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d10c);
				break;
			case 11:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d11c);
				break;
			case 12:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d12c);
				break;
			case 13:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.d13c);
				break;
			}
			break;
		case Card.HEARTS:   
			switch (mValue) {
			case 1:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h1c);
				break;
			case 2:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h2c);
				break;
			case 3:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h3c);
				break;
			case 4:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h4c);
				break;
			case 5:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h5c);
				break;
			case 6:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h6c);
				break;
			case 7:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h7c);
				break;
			case 8:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h8c);
				break;
			case 9:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h9c);
				break;
			case 10:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h10c);
				break;
			case 11:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h11c);
				break;
			case 12:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h12c);
				break;
			case 13:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.h13c);
				break;
			}
			break;
		case Card.SPADES:
			switch (mValue) {
			case 1:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s1c);
				break;
			case 2:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s2c);
				break;
			case 3:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s3c);
				break;
			case 4:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s4c);
				break;
			case 5:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s5c);
				break;
			case 6:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s6c);
				break;
			case 7:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s7c);
				break;
			case 8:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s8c);
				break;
			case 9:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s9c);
				break;
			case 10:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s10c);
				break;
			case 11:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s11c);
				break;
			case 12:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s12c);
				break;
			case 13:
				image = BitmapFactory.decodeResource(context.getResources(), R.drawable.s13c);
				break;
			}
			break;
		case 4: // Joker
			image = BitmapFactory.decodeResource(context.getResources(), R.drawable.joker);
			break;
		}
    }    
}
