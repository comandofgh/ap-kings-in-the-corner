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
import android.graphics.Matrix;

/*
   An object of class card represents one of the 52 cards in a
   standard deck of playing cards.  Each card has a suit and
   a value.
*/
public class Card implements Serializable {

	private static final long serialVersionUID = 8373398048173195242L;

	public final static int SPADES = 0,       // Codes for the 4 suits.
                            HEARTS = 1,
                            DIAMONDS = 2,
                            CLUBS = 3;
                            
    public final static int ACE = 1,          // Codes for the non-numeric cards.
                            JACK = 11,        //   Cards 2 through 10 have their 
                            QUEEN = 12,       //   numerical values for their codes.
                            KING = 13;
    
    public final static int NORMAL = 0,						// Values for setRotate()
    						SIDE_LEFT_RIGHT = 1,
    						CORNER_UP_LEFT_DOWN_RIGHT = 2,
    						CORNER_UP_RIGHT_DOWN_LEFT = 3;
                            
    private final int suit;   // The suit of this card, one of the constants
                              //    SPADES, HEARTS, DIAMONDS, CLUBS.
                              
    private final int value;  // The value of this card, from 1 to 11.
    
    private int xpos, ypos;	  // The x and y coords of the cards current position.
    private int xmax;		  // The maximum x coord visible when the card is in a hand
    						  // unless it is the last card in the hand.
    
    private transient int rotation;
    private transient int corner_rotation; // Same values as above
    
    private transient Bitmap image;			// Normal rotation image
    private transient Bitmap corner;		// Corner images
                             
    public Card(int theValue, int theSuit) {
            // Construct a card with the specified value and suit.
            // Value must be between 1 and 13.  Suit must be between
            // 0 and 3.  If the parameters are outside these ranges,
            // the constructed card object will be invalid.
        value = theValue;
        suit = theSuit;
        rotation = NORMAL;
    }
    
    public void setX(int x) {
    	// Set the current x coord.
    	xpos = x;
    }
    
    public void setY(int y) {
    	// Set the current y coord.
    	ypos = y;
    }
    
    public void setXMax(int xm) {
    	// Set the miximum x coord.
    	xmax = xm;
    }
    
    public int getXMax() {
    	// Gets the maximum x coord.
    	return xmax;
    }
    
    public int getX() {
    	// Return the int giving the current x coord.
    	return xpos;
    }
    
    public int getY() {
    	// Return the int giving the current y coord.
    	return ypos;
    }
        
    public int getSuit() {
            // Return the int that codes for this card's suit.
        return suit;
    }
    
    public int getValue() {
            // Return the int that codes for this card's value.
        return value;
    }
    
    public String getSuitAsString() {
            // Return a String representing the card's suit.
            // (If the card's suit is invalid, "??" is returned.)
        switch ( suit ) {
           case SPADES:   return "Spades";
           case HEARTS:   return "Hearts";
           case DIAMONDS: return "Diamonds";
           case CLUBS:    return "Clubs";
           default:       return "??";
        }
    }
    
    public String getValueAsString() {
            // Return a String representing the card's value.
            // If the card's value is invalid, "??" is returned.
        switch ( value ) {
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
           // Return a String representation of this card, such as
           // "10 of Hearts" or "Queen of Spades".
        return getValueAsString() + " of " + getSuitAsString();
    }
    
    public boolean isSame(Card c) {
    	if (c == null) {
    		return false;
    	} else if (this.suit == c.suit && this.value == c.value) {
    		return true;
    	} else {
    		return false;
    	}
    }

    public boolean covers(Card c) {
    	// Return a boolean indicating whether this card covers the parameter card.
    	if (c == null || this == null || this.suit == 4) {
    		return false;
    	} else if (this.suit == 0 || this.suit == 3) {
    		if (c.suit == 1 || c.suit == 2) {
    			if (this.value+1 == c.value) {
    				return true;
    			}
    		}
    	} else {
    		if (c.suit == 0 || c.suit == 3) {
    			if (this.value+1 == c.value) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    public Bitmap getImage() {
    	if (rotation == CORNER_UP_LEFT_DOWN_RIGHT || rotation == CORNER_UP_RIGHT_DOWN_LEFT) {
    		return corner;
    	}
    	return image;
    }
    
    public void setRotate(int value) {
    	if (rotation == value) return;
    	switch (value) {
    	case NORMAL:
    		if (rotation == SIDE_LEFT_RIGHT) {
    			Matrix rotate90 = new Matrix();
    			rotate90.postRotate(90);
    			image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), rotate90, true);
    		}
    		if (corner != null) {
    		//	corner.recycle();
    		//	corner = null;
    		}
    		rotation = NORMAL;
    		break;
    	case SIDE_LEFT_RIGHT:
    		if (rotation != SIDE_LEFT_RIGHT) {
    			Matrix rotate90 = new Matrix();
    			rotate90.postRotate(90);
    			image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), rotate90, true);
    			rotation = SIDE_LEFT_RIGHT;
    		}
    		break;
    	case CORNER_UP_LEFT_DOWN_RIGHT:
    		if (corner == null) {
    			if (rotation != NORMAL) {
    				setRotate(NORMAL);
    			}
    			Matrix rotateUp45 = new Matrix();
    			rotateUp45.postRotate(315);
    			corner = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), rotateUp45, true);
    		//	image.recycle();
    		} else if (corner_rotation == CORNER_UP_RIGHT_DOWN_LEFT) {
    			Matrix rotate90 = new Matrix();
    			rotate90.postRotate(90);
    			corner = Bitmap.createBitmap(corner, 0, 0, corner.getWidth(), corner.getHeight(), rotate90, true);
    		}
			rotation = CORNER_UP_LEFT_DOWN_RIGHT;
			corner_rotation = CORNER_UP_LEFT_DOWN_RIGHT;
    		break;
    	case CORNER_UP_RIGHT_DOWN_LEFT:
    		if (corner == null) {
    			if (rotation != NORMAL) {
    				setRotate(NORMAL);
    			}
    			Matrix rotateDown45 = new Matrix();
    			rotateDown45.postRotate(45);
    			corner = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), rotateDown45, true);
    		//	image.recycle();
    		} else if (corner_rotation == CORNER_UP_LEFT_DOWN_RIGHT) {
    			Matrix rotate90 = new Matrix();
    			rotate90.postRotate(90);
    			corner = Bitmap.createBitmap(corner, 0, 0, corner.getWidth(), corner.getHeight(), rotate90, true);
    		}
    		rotation = CORNER_UP_RIGHT_DOWN_LEFT;
    		corner_rotation = CORNER_UP_RIGHT_DOWN_LEFT;
    		break;
    	}
    }
    
    public void setImage(Context context, String style) {
    	if (style != null && style.equals("classic")) {
    		setImageClassic(context);
    	} else {
    		setImageNew(context);
    	}
    }
    
    public void setImageNew(Context context) {
		switch (suit) {
		case Card.CLUBS:    
			switch (value) {
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
			switch (value) {
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
			switch (value) {
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
			switch (value) {
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
    
    public void setImageClassic(Context context) {
		switch (suit) {
		case Card.CLUBS:    
			switch (value) {
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
			switch (value) {
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
			switch (value) {
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
			switch (value) {
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
