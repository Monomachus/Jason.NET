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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jIDE;



import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

class ASEditorPane extends JPanel {

	JTextPane editor;
	UndoManager undo;
	JScrollPane editorScroller;
	boolean modified = false;
	boolean needsParsing = false;

	private String fileName = "";
	String extension = "asl";
	JasonID mainID = null;
	int tabIndex;

	//EditorKit editorKit = new EditorKit(new ASContext(), "text/asl");
	//ASContext context = new ASContext();
	ASSyntaxHighLight syntaxThread;
	
	
	ASEditorPane() {
		this(null, 0);
	}

	ASEditorPane(JasonID mainID, int tabIndex) {
		super(true);
		this.mainID = mainID;
		this.tabIndex = tabIndex;
		createEditor();
		createUndoManager(editor);
		editorScroller = new JScrollPane();
		editorScroller.getViewport().add(editor);
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, editorScroller);
		createSyntaxHighlightThread();
		updateFont();
	}
	
	void createSyntaxHighlightThread() {
		syntaxThread = new ASSyntaxHighLight(editor, mainID);
		syntaxThread.start();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String file) {
		this.fileName = removeExtension(file);
	}
	
	public void setFileName(File f) {
		try {
			setFileName(f.getCanonicalPath());
		} catch (Exception e) {
			setFileName(f.toString());
		}
	}
	
    protected String removeExtension(String s) {
       	if (mainID != null) {
        	if (s.startsWith(mainID.projectDirectory)) {
        		s = s.substring(mainID.projectDirectory.length()+1);
        	}
        	if (s.startsWith("./")) {
        		s = s.substring(2);
        	}
        }
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            return s.substring(0, i);
        } else {
            return s;
        }
    }
	
	void createNewPlainText(String text) {
		//editor.setDocument(new DefaultStyledDocument());
		try {
			editor.getDocument().remove(0, editor.getDocument().getLength());
			editor.getDocument().insertString(0, text, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		createUndoManager(editor);
		syntaxThread.addDocListener();
		syntaxThread.repainAll();
	}

	void createNewPlainText(InputStream in) {
		StringBuffer s = new StringBuffer();
		try {
			int c = in.read();
			while (c != -1) {
				s.append((char)c);
				c = in.read();
			}
		} catch (EOFException e) {
		} catch (IOException e) {
			System.err.println("Error reading text!");
			e.printStackTrace();
		}
		createNewPlainText(s.toString());
	}

	/**
	 * Create an editor to represent the given document.
	 */
	protected void createEditor() {
		editor = new JTextPane();//JEditorPane(); //new JTextArea();
		
		editor.addKeyListener(new KeyListener() {
			boolean lastKeyWasSave = false;

			public void keyTyped(KeyEvent evt) {
				if (!lastKeyWasSave) {
					modified = true;
					needsParsing = true;
				}
				if (mainID != null) {
					mainID.updateTabTitle(tabIndex, ASEditorPane.this, null);
				}
				if (!mainID.runMASButton.isEnabled()) {
					mainID.runMASButton.setEnabled(true);
				}
			}

			public void keyPressed(KeyEvent evt) {
				lastKeyWasSave = false;
				if ((evt.getKeyCode() == KeyEvent.VK_Z) && (evt.isControlDown())) {
					try {
						undo.undo();
					} catch (CannotUndoException cue) {
						Toolkit.getDefaultToolkit().beep();
					}
				} else if ((evt.getKeyCode() == KeyEvent.VK_Y) && (evt.isControlDown())) {
					try {
						//Redo changes
						undo.redo();
					} catch (CannotRedoException cue) {
						Toolkit.getDefaultToolkit().beep();
					}
				} else if ((evt.getKeyCode() == KeyEvent.VK_S) && (evt.isControlDown())) {
					try {
						mainID.saveAct.actionPerformed(null);
					} catch (CannotRedoException cue) {
						Toolkit.getDefaultToolkit().beep();
					}
					lastKeyWasSave = true;
				}
			}

			public void keyReleased(KeyEvent evt) {
			}
		});
		editor.setDragEnabled(true);
		/*
		editor.setEditorKit(new StyledEditorKit() {
			public ViewFactory getViewFactory() {
				return new NumberedViewFactory();
			}
		});
		*/
		
		//editor.setEditorKitForContentType(editorKit.getContentType(), editorKit);
		//editor.setContentType("text/asl");
	}

	public void updateFont() {
		if (mainID != null) {
			String font = JasonID.userProperties.getProperty("font");
			if (font == null) {
				return;
			}
			int size = 12;
			try {
				size = Integer.parseInt(JasonID.userProperties.getProperty("fontSize"));
			} catch (Exception e) {}
			syntaxThread.setFont(font, size);
		}
	}

	
/*	
	class EditorKit extends DefaultEditorKit {
		String mime;
		EditorKit(ASContext style, String mime) {
			context = style;
			this.mime = mime;
		}
		public ASContext getStylePreferences() {
			return context;
		}
		public String getContentType() {
			return mime;
		}
		public Document createDefaultDocument() {
			return new PlainDocument();
		}
		public final ViewFactory getViewFactory() {
			return getStylePreferences();
		}
		//public ViewFactory getViewFactory() {
		//	return new NumberedViewFactory();
		//}
	}
	*/

	private void createUndoManager(JTextComponent ed) {
		// undo support
		undo = new UndoManager();
		undo.setLimit(100);
		ed.getDocument().addUndoableEditListener(new UndoableEditListener() {
			/**
			 * Messaged when the Document has created an edit, the edit is added
			 * to <code>undo</code>, an instance of UndoManager.
			 */
			public void undoableEditHappened(UndoableEditEvent e) {
				undo.addEdit(e.getEdit());
			}
		});
	}

	String getDefaultText(String s) {
		return "// "+s+"\ndemo.\n+demo : true <- .print(\"hello world.\").";
	}

	class NumberedViewFactory implements ViewFactory {
		public View create(Element elem) {
			String kind = elem.getName();
			if (kind != null)
				if (kind.equals(AbstractDocument.ContentElementName)) {
					return new LabelView(elem);
				} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
					//	              return new ParagraphView(elem);
					return new NumberedParagraphView(elem);
				} else if (kind.equals(AbstractDocument.SectionElementName)) {
					return new BoxView(elem, View.Y_AXIS);
				} else if (kind.equals(StyleConstants.ComponentElementName)) {
					return new ComponentView(elem);
				} else if (kind.equals(StyleConstants.IconElementName)) {
					return new IconView(elem);
				}
			// default to text display
			return new LabelView(elem);
		}
	}

	class NumberedParagraphView extends ParagraphView {
		public short NUMBERS_WIDTH = 25;

		public NumberedParagraphView(Element e) {
			super(e);
			short top = 0;
			short left = 0;
			short bottom = 0;
			short right = 0;
			this.setInsets(top, left, bottom, right);
		}

		protected void setInsets(short top, short left, short bottom, short right) {
			super.setInsets(top, (short) (left + NUMBERS_WIDTH), bottom, right);
		}

		public void paintChild(Graphics g, Rectangle r, int n) {
			super.paintChild(g, r, n);
			int previousLineCount = getPreviousLineCount();
			int numberX = r.x - getLeftInset();
			int numberY = r.y + r.height - 5;
			g.drawString(Integer.toString(previousLineCount + n + 1), numberX, numberY);
		}

		public int getPreviousLineCount() {
			int lineCount = 0;
			View parent = this.getParent();
			int count = parent.getViewCount();
			for (int i = 0; i < count; i++) {
				if (parent.getView(i) == this) {
					break;
				} else {
					lineCount += parent.getView(i).getViewCount();
				}
			}
			return lineCount;
		}
	}
}