package jIDE;

import jIDE.parser.mas2jConstants;

import java.awt.Color;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

public class MAS2JContext extends ASContext { //implements ViewFactory {

	/**
	 * Constructs a set of styles to represent java lexical tokens. By default
	 * there are no colors or fonts specified.
	 */
	public MAS2JContext() {
		super();
		Style root = getStyle(DEFAULT_STYLE);

		tokenStyles = new Style[mas2jConstants.tokenImage.length];
		for (int i = 0; i < tokenStyles.length; i++) {
			Style s = addStyle(null,root); // all styles are children of root (!)
			tokenStyles[i] = s; // store the style ref
		}
		
		SimpleAttributeSet style;
		
		// palavras reservadas
		style = new SimpleAttributeSet();
		//StyleConstants.setFontFamily(style, "Monospaced");
		//StyleConstants.setFontSize(style, 12);
		//StyleConstants.setBackground(style, Color.white);
		StyleConstants.setForeground(style, Color.darkGray);
		StyleConstants.setBold(style, true);
		//StyleConstants.setItalic(style, false);
		tokenStyles[mas2jConstants.MAS].addAttributes(style);
		tokenStyles[mas2jConstants.ENV].addAttributes(style);
		tokenStyles[mas2jConstants.AGS].addAttributes(style);
		tokenStyles[mas2jConstants.CONTROL].addAttributes(style);
		tokenStyles[mas2jConstants.ARCH].addAttributes(style);

		// ag pars
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.black);
		StyleConstants.setItalic(style, true);
		StyleConstants.setBold(style, true);
		tokenStyles[mas2jConstants.AT].addAttributes(style);
		tokenStyles[mas2jConstants.ASAGCLASS].addAttributes(style);
		tokenStyles[mas2jConstants.ASAGARCHCLASS].addAttributes(style);
		
		// Values
		style = new SimpleAttributeSet();
		float chsb[] = Color.RGBtoHSB(120,20,120,null);
		StyleConstants.setForeground(style, Color.getHSBColor(chsb[0],chsb[1],chsb[2]));
		StyleConstants.setBold(style, true);
		tokenStyles[mas2jConstants.ARCHV].addAttributes(style);

		// ! ?
		style = new SimpleAttributeSet();
		StyleConstants.setForeground(style, Color.blue);
		StyleConstants.setBold(style, true);
		tokenStyles[mas2jConstants.ASOEE].addAttributes(style);
		tokenStyles[mas2jConstants.ASOIB].addAttributes(style);
		tokenStyles[mas2jConstants.ASOV].addAttributes(style);
		tokenStyles[mas2jConstants.ASOSYNC].addAttributes(style);

	}

	// --- ViewFactory methods -------------------------------------
	/*
	public View create(Element elem) {
		return new MAS2JView(elem);
	}

	class MAS2JView extends ASContext.ASView {
		MAS2JView(Element elem) {
			super(elem);
		}
		protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
			Document doc = getDocument();
			String text = doc.getText(p0, p1-p0);
			//System.out.println(p0 +"-"+p1+"="+text);
			mas2jTokenManager tm = new mas2jTokenManager(new SimpleCharStream(new StringReader(text)));
			int lastTokenEnd = 0;
			try {
				Token t = tm.getNextToken();
				while (t.kind != mas2jConstants.EOF) {
					if (lastTokenEnd+1 != t.beginColumn) {
						// draw inter-token chars
						g.setColor(getForeground(mas2jConstants.DEFAULT));
						g.setFont(getFont(mas2jConstants.DEFAULT));
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
				g.setColor(getForeground(mas2jConstants.DEFAULT));
				g.setFont(getFont(mas2jConstants.DEFAULT));
				Segment stext = getLineBuffer();
				doc.getText(p0+lastTokenEnd, p1-p0-lastTokenEnd-1 , stext);
				//System.out.println("%!!"+"%"+doc.getText(p0+lastTokenEnd, p1-p0-lastTokenEnd-1));
				x = Utilities.drawTabbedText(stext, x, y, g, this, 0);
			}
			return x;
		}
	}
*/
}
