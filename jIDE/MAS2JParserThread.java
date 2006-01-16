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
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.15  2006/01/16 16:47:35  jomifred
//   added a new kind of console with one tab for agent
//
//   Revision 1.14  2006/01/14 18:24:20  jomifred
//   centralised infra does not use xml script file anymore
//
//   Revision 1.13  2005/12/08 20:05:01  jomifred
//   changes for JasonIDE plugin
//
//   Revision 1.11  2005/10/29 21:46:22  jomifred
//   add a new class (MAS2JProject) to store information parsed by the mas2j parser. This new class also create the project scripts
//
//   Revision 1.10  2005/08/12 21:08:23  jomifred
//   add cvs keywords
//
//----------------------------------------------------------------------------

package jIDE;

import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.TokenMgrError;
import jason.mas2j.parser.mas2j;

import java.io.File;
import java.io.StringReader;

import javax.swing.text.Document;

/** a parser thread to compile MAS2j source while being edited */
public class MAS2JParserThread extends ASParserThread { //Thread {
    
    mas2j           fParserMAS2J;
    MAS2JProject    fCurrentProject;
    boolean         fDebug;
    
    MAS2JParserThread(MAS2JEditorPane editor, JasonID jasonID) {
    	super("MAS2JParser", jasonID);
    }

    public void debugOn() {
    	fDebug = true;
    }
    public void debugOff() {
    	fDebug = false;
    }

    void createParser() {
        try {
            fParserMAS2J = new mas2j(new StringReader(""));
        } catch (Exception ex) {
        	System.err.println("Error creating mas2j parser!"+ex);
        	ex.printStackTrace();
        }    	
    }
    
    
    void parse() {
        // compile
        try {
        	if (!fJasonID.mas2jPane.needsParsing && !fForegroundCompilation) {
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
            fCurrentProject = fParserMAS2J.mas();
            fCurrentProject.setDirectory( fJasonID.projectDirectory );
            fCurrentProject.setProjectFile( new File(fJasonID.mas2jPane.getFileName()) ); // TODO: not work. Fix it 
            
            fOk = true;
            
            if (fForegroundCompilation) {
            	fCurrentProject.writeScripts(fDebug);
                System.out.println(" parsed successfully!");
                //System.out.println("scripts was written on "+editorPanel.mainGUI.projectDirectory+".");
            }
            
            fJasonID.openAllASFiles(fCurrentProject.getAllASFiles());

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
			System.err.println("Error: "+ex);
            ex.printStackTrace();
        } finally {
            fCompilationDone = true;
            fJasonID.mas2jPane.needsParsing = false;
            stopWaiting();
        }
    }
    
    synchronized public boolean foregroundCompile() {
    	waitCompilation(); // waits the eventual running parsing
        fForegroundCompilation = true;
        fJasonID.mas2jPane.needsParsing = true;
        fCompilationDone = false; // it is necessary here!
        stopWaiting();
        waitCompilation(); // waits the end of compilation
        fForegroundCompilation = false;
        return fOk;
    }
}
