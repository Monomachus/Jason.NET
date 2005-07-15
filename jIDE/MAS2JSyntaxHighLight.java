package jIDE;

import jIDE.parser.SimpleCharStream;
import jIDE.parser.Token;
import jIDE.parser.TokenMgrError;
import jIDE.parser.mas2jConstants;
import jIDE.parser.mas2jTokenManager;

import java.awt.Color;
import java.io.StringReader;

import javax.swing.JTextPane;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class MAS2JSyntaxHighLight extends ASSyntaxHighLight {
	mas2jTokenManager tm = new mas2jTokenManager(new SimpleCharStream(new StringReader("")));
	public MAS2JSyntaxHighLight(JTextPane p) {
		super(p, null);
		context = new MAS2JStyles();
	}
	void paintLine() {
		try {
			StyledDocument sd = (StyledDocument) editor.getDocument();
			Element ePar = sd.getParagraphElement(offset);
			int eIni = ePar.getStartOffset();
			int eEnd = ePar.getEndOffset();
			String sPar = sd.getText(eIni, eEnd- eIni);
			//System.out.println("$"+sPar+"$");
			
			if (sPar.trim().startsWith("//")) {
				sd.setCharacterAttributes(eIni, eEnd-eIni-1, commentStyle, true);					
			} else {
				tm.ReInit(new SimpleCharStream(new StringReader(sPar)));
				try {
					Token lastToken = null;
					Token t = tm.getNextToken();
					while (t.kind != mas2jConstants.EOF) {
						sd.setCharacterAttributes(eIni+t.beginColumn-1, t.endColumn-t.beginColumn+1,	 context.tokenStyles[t.kind], true);
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
	}
}	

