package jIDE;

import jason.asSyntax.parser.as2j;

import java.awt.Color;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class ASContext extends StyleContext { //implements ViewFactory {

	/**
	 * The styles representing the actual token types.
	 */
	Style[] tokenStyles;

	/**
	 * Cache of foreground colors to represent the various tokens.
	 */
	//transient Color[] tokenColors;

	/**
	 * Cache of fonts to represent the various tokens.
	 */
	//transient Font[] tokenFonts;

	
	/**
	 * Constructs a set of styles to represent java lexical tokens. By default
	 * there are no colors or fonts specified.
	 */
	public ASContext() {
		super();
		Style root = getStyle(DEFAULT_STYLE);

		tokenStyles = new Style[as2j.tokenImage.length];
		for (int i = 0; i < tokenStyles.length; i++) {
			Style s = addStyle(null,root); // all styles are children of root (!)
			tokenStyles[i] = s; // store the style ref
		}
		
		SimpleAttributeSet style;
		
		// atoms
		style = new SimpleAttributeSet();
		//StyleConstants.setFontFamily(style, "Monospaced");
		//StyleConstants.setFontSize(style, 12);
		//StyleConstants.setBackground(style, Color.white);
		StyleConstants.setForeground(style, Color.darkGray);
		//StyleConstants.setBold(style, false);
		//StyleConstants.setItalic(style, false);
		tokenStyles[as2j.ATOM].addAttributes(style);
		
		// strings
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.gray);
		tokenStyles[as2j.STRING].addAttributes(style);

		// vars
		style = new SimpleAttributeSet();
		float chsb[] = Color.RGBtoHSB(120,20,120,null);
		StyleConstants.setForeground(style, Color.getHSBColor(chsb[0],chsb[1],chsb[2]));
		StyleConstants.setBold(style, true);
		tokenStyles[as2j.VAR].addAttributes(style);

		// true false
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.black);
		StyleConstants.setItalic(style, true);
		StyleConstants.setBold(style, true);
		tokenStyles[as2j.TK_TRUE].addAttributes(style);
		// false
		tokenStyles[as2j.TK_FALSE].addAttributes(style);

		// @ TODO: add a token id for it in as2j.jcc
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.orange);
		tokenStyles[22].addAttributes(style);
		
		// . : <- ;
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.black);
		StyleConstants.setBold(style, true);
		tokenStyles[21].addAttributes(style);
		tokenStyles[23].addAttributes(style);
		tokenStyles[24].addAttributes(style);
		tokenStyles[39].addAttributes(style);

		// + -
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.red);
		StyleConstants.setBold(style, true);
		tokenStyles[25].addAttributes(style);
		tokenStyles[26].addAttributes(style);

		// not ~ &
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.green);
		StyleConstants.setBold(style, true);
		tokenStyles[as2j.TK_NOT].addAttributes(style);
		tokenStyles[as2j.TK_NEG].addAttributes(style);
		tokenStyles[29].addAttributes(style);

		// ! ?
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.blue);
		StyleConstants.setBold(style, true);
		tokenStyles[27].addAttributes(style);
		tokenStyles[28].addAttributes(style);

	}

	public void setFont(String font, int size) {
		Style root = getStyle(DEFAULT_STYLE);
		StyleConstants.setFontFamily(root, font);
		StyleConstants.setFontSize(root, size);
		//tokenColors = null;
		//tokenFonts = null;
	}

	/*
	public Color getForeground(int code) {
		if (tokenColors == null) {
			tokenColors = new Color[as2j.tokenImage.length];
		}
		if ((code >= 0) && (code < tokenColors.length)) {
			Color c = tokenColors[code];
			if (c == null) {
				Style s = tokenStyles[code];
				c = StyleConstants.getForeground(s);
			}
			return c;
		}
		return Color.black;
	}

	public Font getFont(int code) {
		if (tokenFonts == null) {
			tokenFonts = new Font[as2j.tokenImage.length];
		}
		if (code < tokenFonts.length) {
			Font f = tokenFonts[code];
			if (f == null) {
				Style s = tokenStyles[code];
				f = getFont(s);
			}
			return f;
		}
		return null;
	}


	// --- ViewFactory methods -------------------------------------

	public View create(Element elem) {
		return new ASView(elem);
	}
	*/

	/**
	 * View that uses the lexical information to determine the style
	 * characteristics of the text that it renders. This simply colorizes the
	 * various tokens and assumes a constant font family and size.
	 */
	/*
	class ASView extends WrappedPlainView {
		ASView(Element elem) {
			super(elem);
		}
*/
		/**
		 * Renders using the given rendering surface and area on that surface.
		 * This is implemented to invalidate the lexical scanner after rendering
		 * so that the next request to drawUnselectedText will set a new range
		 * for the scanner.
		 * 
		 * @param g
		 *            the rendering surface to use
		 * @param a
		 *            the allocated region to render into
		 * @see View#paint
		 */
		/*
		public void paint(Graphics g, Shape a) {
			super.paint(g, a);
		}
		*/

		
		/**
		 * Renders the given range in the model as normal unselected text. This
		 * is implemented to paint colors based upon the token-to-color
		 * translations. To reduce the number of calls to the Graphics object,
		 * text is batched up until a color change is detected or the entire
		 * requested range has been reached.
		 * 
		 * @param g
		 *            the graphics context
		 * @param x
		 *            the starting X coordinate
		 * @param y
		 *            the starting Y coordinate
		 * @param p0
		 *            the beginning position in the model
		 * @param p1
		 *            the ending position in the model
		 * @returns the location of the end of the range
		 * @exception BadLocationException
		 *                if the range is invalid
		 */
	/*	protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
			Document doc = getDocument();
			String text = doc.getText(p0, p1-p0);
			//System.out.println(p0 +"-"+p1+"="+text);
			as2jTokenManager tm = new as2jTokenManager(new SimpleCharStream(new StringReader(text)));
			int lastTokenEnd = 0;
			try {
				Token t = tm.getNextToken();
				while (t.kind != as2j.EOF) {
					if (lastTokenEnd+1 != t.beginColumn) {
						// draw inter-token chars
						g.setColor(getForeground(as2j.DEFAULT));
						g.setFont(getFont(as2j.DEFAULT));
						Segment stext = getLineBuffer();
						doc.getText(p0+lastTokenEnd, t.beginColumn-lastTokenEnd-1 , stext);
						//System.out.println("%"+stext+"%");
						x = Utilities.drawTabbedText(stext, x, y, g, this, 0);
					}
					g.setColor(getForeground(t.kind));
					g.setFont(getFont(t.kind));
					Segment stext = getLineBuffer();
					doc.getText(p0+t.beginColumn-1, t.endColumn - t.beginColumn +1, stext);
					//System.out.println(as2j.tokenImage[t.kind]+"="+t+"/"+stext);
					x = Utilities.drawTabbedText(stext, x, y, g, this, 0);
					
					lastTokenEnd = t.endColumn;
					t = tm.getNextToken();
				}
			} catch (TokenMgrError e) {
				// no problem, just the token was not completly typed
			}
			if (p0+lastTokenEnd+1 < p1) {
				// draw inter-token chars
				//System.out.println(p0 +"-"+p1+"-"+lastTokenEnd);
				g.setColor(getForeground(as2j.DEFAULT));
				g.setFont(getFont(as2j.DEFAULT));
				Segment stext = getLineBuffer();
				doc.getText(p0+lastTokenEnd, p1-p0-lastTokenEnd-1 , stext);
				//System.out.println("%!!"+"%"+doc.getText(p0+lastTokenEnd, p1-p0-lastTokenEnd-1));
				x = Utilities.drawTabbedText(stext, x, y, g, this, 0);
			}
			return x;
		}
	} */
}
