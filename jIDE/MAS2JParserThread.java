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

import jIDE.parser.ParseException;
import jIDE.parser.TokenMgrError;
import jIDE.parser.mas2j;

import java.io.StringReader;

import javax.swing.text.Document;

/** a parser thread to compile MAS2j source while being edited */
public class MAS2JParserThread extends Thread {
    
    mas2j fParserMAS2J;
    MAS2JEditorPane fEditorPanel;
    JasonID fJasonID;
    boolean fStop = false;
    boolean fOk = false;
    boolean fForegroundCompilation = false;
    boolean fCompilationDone = true;
    
    MAS2JParserThread(MAS2JEditorPane editor, JasonID jasonID) {
    	super("MAS2JParser");
        fParserMAS2J = new mas2j(new StringReader(""));
        fParserMAS2J.setNoOut(true);
        this.fEditorPanel = editor;
        this.fJasonID = jasonID;
    }
    
    void parse() {
        fOk = false;
        // compile
        try {
            if (fForegroundCompilation) {
                System.out.print("Parsing project file... ");
            }
            Document doc  = fEditorPanel.editor.getDocument();
            String text = doc.getText(0, doc.getLength());
            fParserMAS2J.ReInit(new StringReader(text));
            fParserMAS2J.setDestDir( fJasonID.projectDirectory );
            fParserMAS2J.setJasonJar(fJasonID.getConf().getProperty("jasonJar"));
            fParserMAS2J.setSaciJar(fJasonID.getConf().getProperty("saciJar"));
            fParserMAS2J.setLog4jJar(fJasonID.getConf().getProperty("log4jJar"));
            fParserMAS2J.setJavaHome(fJasonID.getConf().getProperty("javaHome"));
            fParserMAS2J.mas();
            fParserMAS2J.close();
            fOk = true;
            
            if (fForegroundCompilation) {
                fParserMAS2J.writeScripts();
                System.out.println(" parsed successfully!");
                //System.out.println("scripts was written on "+editorPanel.mainGUI.projectDirectory+".");
            }
            if (fOk) {
                fJasonID.openAllASFiles(fParserMAS2J.getAgASFiles().values());
            }
        } catch (ParseException ex) {
            if (fForegroundCompilation) {
                System.out.println("\nmas2j: parsing errors found... \n" + ex);
            } else {
                fJasonID.updateTabTitle(0, fEditorPanel, "!line "+ex.currentToken.beginLine);
            }
        } catch (TokenMgrError ex) {
            if (fForegroundCompilation) {
                System.out.println("\nmas2j: error parsing tokens ... \n" + ex);
            } else {
                int p = ex.toString().indexOf("line");
                int v = ex.toString().indexOf(", ", p);
                if (p > 0 && v > p) {
                    fJasonID.updateTabTitle(0, fEditorPanel, "!line "+ex.toString().substring(p+4,v).trim());
                }
            }
        } catch (Exception ex) {
			System.err.println("Error:"+ex);
            ex.printStackTrace();
        } finally {
            fCompilationDone = true;
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
        while (!fCompilationDone && trycount < 3) {
            try {
                wait(2000); // waits 2 seconds
                trycount++;
            } catch (Exception e) { }
        }
        if (trycount >= 3) {
        	System.out.println("stop waiting compilation, some unexpected error ocorred");
        	fOk = false;
        }
    }
    
    synchronized void stopWaiting() {
        notifyAll();
    }
    
    public boolean foregroundCompile() {
        fParserMAS2J.setNoOut(false);
        fForegroundCompilation = true;
        fEditorPanel.needsParsing = true;
        fCompilationDone = false;
        stopWaiting();
        waitCompilation(); // waits the end of compilation
        fParserMAS2J.setNoOut(true);
        fForegroundCompilation = false;
        return fOk;
    }
    
    public void stopParser() {
        fStop = true;
        stopWaiting();
    }
    
    public void run() {
        while (!fStop) {
            if (!fStop && fEditorPanel.needsParsing) {
                parse();
                fEditorPanel.needsParsing = false;
            }
            waitSomeTime();
        }
    }
}
