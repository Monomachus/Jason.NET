//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jIDE;

import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.TokenMgrError;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;

import javax.swing.text.Document;

/** a parser thread to compile AS sources while being edited */
public class ASParserThread extends Thread {
    
    as2j parser;
    
    JasonID jasonID;
    
    boolean stop = false;
    boolean ok = false;
    boolean foregroundCompilation = false;
    boolean forDone = true;
    boolean compilationDone = false;
    
    ASParserThread(JasonID jasonID) {
    	super("ASParserThread");
        this.jasonID = jasonID;
        createParser();
    }
    
    private void createParser() {
        try {
        	parser = new as2j(new StringReader(""));
        } catch (Exception ex) {
        	System.err.println("error creating parser!"+ex);
        	ex.printStackTrace();
        	parser = null;
        }    	
    }
    
    boolean parseTab(int tabIndex) {
    	if (parser == null) {
    		createParser();
    		if (parser == null) {
    			return false;
    		}
    	}
        EditorPane editorPanel = null;
        // compile
        try {
            editorPanel = (EditorPane)jasonID.tab.getComponentAt(tabIndex);
            if (RunCentralisedMAS.logPropFile.startsWith(editorPanel.getFileName())) {
            	return true;
            }
            if (!editorPanel.needsParsing && !foregroundCompilation) {
                return true;
            }
            if (foregroundCompilation) {
                System.out.print("Parsing "+editorPanel.getFileName()+"...");
            }
            Document doc  = editorPanel.editor.getDocument();
            String text = doc.getText(0, doc.getLength());
            parser.ReInit(new StringReader(text));
            parser.ag(null);
            
            if (foregroundCompilation) {
                System.out.println(" parsed successfully!");
            }
            return true;
        } catch (ParseException ex) {
            if (foregroundCompilation) {
                System.out.println("\nas2j: parsing errors found... \n" + ex);
            } else {
                jasonID.updateTabTitle(tabIndex, editorPanel, "!line "+ex.currentToken.beginLine);
            }
        } catch (TokenMgrError ex) {
            if (foregroundCompilation) {
                System.out.println("\nas2j: error parsing tokens ... \n" + ex);
            } else {
                int p = ex.toString().indexOf("line");
                int v = ex.toString().indexOf(", ", p);
                if (p > 0 && v > p) {
                    jasonID.updateTabTitle(tabIndex, editorPanel, "!line "+ex.toString().substring(p+4,v).trim());
                }
            }
        } catch (Exception ex) {
        	System.err.println("Unexpected error "+ex);
            ex.printStackTrace();
        } finally {
            editorPanel.needsParsing = false;
        }
        return false;
    }
    
    private void parseAllTabs() {
        ok = true;
        try {
            for (int tabIndex=1; tabIndex < jasonID.tab.getComponentCount(); tabIndex++) {
                ok = parseTab(tabIndex);
                if (!ok) {
                    break; 
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            compilationDone = true;
            stopWaiting();
        }
    }
    
    synchronized void waitSomeTime() {
        try {
            wait(2000); // waits 2 seconds
        } catch (Exception e) { }
    }
    
    synchronized void waitCompilation() {
    	int trycount = 0;
        while (!compilationDone && trycount < 3) {
            try {
                wait(2000); // waits 2 seconds
                trycount++;
            } catch (Exception e) { }
        }
        if (trycount >= 3) {
        	System.out.println("stop waiting compilation, some unexpected error ocorred");
        	ok = false;
        }
    }
    
    synchronized void stopWaiting() {
        notifyAll();
    }
    
    public boolean foregroundCompile() {
        foregroundCompilation = true;
        compilationDone = false;
        // TODO: the following two lines (and the run method) was commented
        // since the AS online parsing does not work properly
        //stopWaiting(); // wakeup this thread
        //waitCompilation(); // waits the end of compilation
        parseAllTabs();
        foregroundCompilation = false;
        return ok;
    }
    
    public void stopParser() {
        stop = true;
    }

    /*
    public void run() {
        while (!stop) {
            waitSomeTime();
            if (!stop) {
                parseAllTabs();
            }
        }
    }
    */
}
