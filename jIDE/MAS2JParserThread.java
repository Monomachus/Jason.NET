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
public class MAS2JParserThread extends ASParserThread { //Thread {
    
    mas2j           fParserMAS2J;

    MAS2JParserThread(MAS2JEditorPane editor, JasonID jasonID) {
    	super("MAS2JParser", jasonID);
    }

    void createParser() {
        try {
            fParserMAS2J = new mas2j(new StringReader(""));
            fParserMAS2J.setNoOut(true);
        } catch (Exception ex) {
        	System.err.println("error creating mas2j parser!"+ex);
        	ex.printStackTrace();
        }    	
    }
    
    
    void parse() {
        // compile
        try {
        	if (! fJasonID.mas2jPane.needsParsing) {
        		return;
        	}
            fOk = false;
            fCompilationDone = false;
            if (fForegroundCompilation) {
                System.out.print("Parsing project file... ");
            }
            Document doc  = fJasonID.mas2jPane.editor.getDocument();
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
            
            fJasonID.openAllASFiles(fParserMAS2J.getAgASFiles().values());

            fErrorLine = -1; // set no error!
            fJasonID.mas2jPane.syntaxHL.paintLine(); //errorOffSet); // 1 char was inserted
            
        } catch (ParseException ex) {
            if (fForegroundCompilation) {
                System.out.println("\nmas2j: parsing errors found... \n" + ex);
            } else {
            	if (fJasonID.tab.getSelectedIndex() == 0 && ex.currentToken != null) {
            		//fJasonID.updateTabTitle(0, fEditorPanel, "!line "+ex.currentToken.beginLine);
            		fErrorLine = ex.currentToken.beginLine;
            		fJasonID.mas2jPane.syntaxHL.paintLine();
            	}
            }
        } catch (TokenMgrError ex) {
            if (fForegroundCompilation) {
                System.out.println("\nmas2j: error parsing tokens ... \n" + ex);
            } else {
                int p = ex.toString().indexOf("line");
                int v = ex.toString().indexOf(", ", p);
                if (p > 0 && v > p) {
            		fErrorLine = Integer.parseInt(ex.toString().substring(p+4,v).trim());
            		//System.out.println("error line is "+ex.currentToken.beginLine+" buf bline="+inStream.getBeginLine()+" buf eline="+inStream.getEndLine()+" offset="+errorOffSet);
            		fJasonID.mas2jPane.syntaxHL.paintLine();
            		//fJasonID.updateTabTitle(0, fEditorPanel, "!line "+ex.toString().substring(p+4,v).trim());
                }
            }
        } catch (Exception ex) {
			System.err.println("Error:"+ex);
            ex.printStackTrace();
        } finally {
            fCompilationDone = true;
            fJasonID.mas2jPane.needsParsing = false;
            stopWaiting();
        }
    }
    
    synchronized public boolean foregroundCompile() {
        fParserMAS2J.setNoOut(false);
        fForegroundCompilation = true;
        fJasonID.mas2jPane.needsParsing = true;
        fCompilationDone = false; // it is necessary here!
        stopWaiting();
        waitCompilation(); // waits the end of compilation
        fParserMAS2J.setNoOut(true);
        fForegroundCompilation = false;
        return fOk;
    }
}
