// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.11  2005/12/08 20:05:01  jomifred
//   changes for JasonIDE plugin
//
//----------------------------------------------------------------------------

package jIDE;

import jason.mas2j.parser.SimpleCharStream;
import jason.mas2j.parser.Token;
import jason.mas2j.parser.TokenMgrError;
import jason.mas2j.parser.mas2jConstants;
import jason.mas2j.parser.mas2jTokenManager;

import java.awt.Color;
import java.io.StringReader;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class MAS2JSyntaxHighLight extends ASSyntaxHighLight {
	mas2jTokenManager tm = new mas2jTokenManager(new SimpleCharStream(new StringReader("")));

	public MAS2JSyntaxHighLight(JTextPane p, JasonID jID) {
		super(p, jID);
		context = new MAS2JStyles();
	}

	ASParserThread getParserThread() {
		if (jasonID != null && jasonID.fMAS2jThread != null) {
			return jasonID.fMAS2jThread;
		} else {
			return null;
		}
	}

	
	/*
	int paintLine(int offset) {
		try {
			StyledDocument sd = (StyledDocument) editor.getDocument();
			Element ePar = sd.getParagraphElement(offset);
			int eIni = ePar.getStartOffset();
			int eEnd = ePar.getEndOffset();
			String sPar = sd.getText(eIni, eEnd- eIni);
			sd.setParagraphAttributes(eIni, eEnd-eIni+1, context.getStyle(StyleContext.DEFAULT_STYLE), false);
			//System.out.println("$"+sPar+"$");
			
			if (sPar.trim().startsWith("//")) {
				sd.setCharacterAttributes(eIni, eEnd-eIni-1, commentStyle, true);					
			} else {

				// cursor line (check for error in this line)
				if (jasonID != null && jasonID.fMAS2jThread != null) {
					// identify the current line of ePar
					int curLine = getParLineNumber(ePar);
					if (jasonID.fMAS2jThread.getErrorLine() == curLine) { // has an error?
						sd.setCharacterAttributes(eIni, eEnd-eIni, errorStyle, true);
						// paint previous line with no error
						Element p = getPreviousLine(ePar);
						sd.setCharacterAttributes(p.getStartOffset(), p.getEndOffset()-p.getStartOffset(), noErrorStyle, true);								
					} else {
						sd.setCharacterAttributes(eIni, eEnd-eIni, noErrorStyle, true);								
					}
				}
				
				tm.ReInit(new SimpleCharStream(new StringReader(sPar)));
				try {
					Token lastToken = null;
					Token t = tm.getNextToken();
					while (t.kind != mas2jConstants.EOF) {
						sd.setCharacterAttributes(eIni+t.beginColumn-1, t.endColumn-t.beginColumn+1,	 context.tokenStyles[t.kind], false);
						lastToken = t;
						t = tm.getNextToken();
					}
					// verify the end of line comments
					if (lastToken != null && lastToken.endColumn+eIni+1 < eEnd) {
						sPar = sd.getText(lastToken.endColumn+eIni, eEnd-(lastToken.endColumn+eIni));
						if (sPar.trim().startsWith("//")) {
							sd.setCharacterAttributes(lastToken.endColumn+eIni, eEnd-(lastToken.endColumn+eIni), commentStyle, true);					
						}
					}
				} catch (TokenMgrError e) {
			}
			}
			offset = eEnd;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return offset;
	}
	*/
	
	void changeAttributesBasedOnTokens(StyledDocument sd, int eIni, int eEnd, String sPar) {
		try {
			tm.ReInit(new SimpleCharStream(new StringReader(sPar)));
			Token lastToken = null;
			Token t = tm.getNextToken();
			while (t.kind != mas2jConstants.EOF) {
				sd.setCharacterAttributes(eIni+t.beginColumn-1, t.endColumn-t.beginColumn+1,	 context.tokenStyles[t.kind], false);
				lastToken = t;
				t = tm.getNextToken();
			}
			// verify the end of line comments
			if (lastToken != null && lastToken.endColumn+eIni+1 < eEnd) {
				sPar = sd.getText(lastToken.endColumn+eIni, eEnd-(lastToken.endColumn+eIni));
				if (sPar.trim().startsWith("//")) {
					sd.setCharacterAttributes(lastToken.endColumn+eIni, eEnd-(lastToken.endColumn+eIni), commentStyle, true);					
				}
			}
		} catch (TokenMgrError e) {
		} catch (javax.swing.text.BadLocationException ex) {
		}
	}

	public class MAS2JStyles extends ASStyles { 
		public MAS2JStyles() {
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
			tokenStyles[mas2jConstants.INFRA].addAttributes(style);

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
			tokenStyles[mas2jConstants.INFRAV].addAttributes(style);

			// ag options
			style = new SimpleAttributeSet();
			StyleConstants.setForeground(style, Color.blue);
			StyleConstants.setBold(style, true);
			tokenStyles[mas2jConstants.ASOEE].addAttributes(style);
			tokenStyles[mas2jConstants.ASOIB].addAttributes(style);
			tokenStyles[mas2jConstants.ASOV].addAttributes(style);
			tokenStyles[mas2jConstants.ASOSYNC].addAttributes(style);
			tokenStyles[mas2jConstants.ASONRC].addAttributes(style);

		}
	}
}	

