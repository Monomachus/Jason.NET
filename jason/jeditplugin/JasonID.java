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
//   Revision 1.5  2005/12/13 18:23:39  jomifred
//   no message
//
//   Revision 1.4  2005/12/09 21:34:45  jomifred
//   no message
//
//   Revision 1.3  2005/12/09 14:47:40  jomifred
//   no message
//
//   Revision 1.2  2005/12/08 20:06:59  jomifred
//   changes for JasonIDE plugin
//
//
//----------------------------------------------------------------------------

package jason.jeditplugin;

import jIDE.MAS2JEditorPane;
import jIDE.RunMAS;
import jIDE.RunningMASListener;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.TokenMgrError;
import jason.runtime.OutputStreamAdapter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.AnimatedIcon;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.RolloverButton;

import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;

public class JasonID extends JPanel implements EBComponent, RunningMASListener {

	View view;

	OutputStreamAdapter myOut;
	RunMAS runner = null;
	
	JTextArea textArea;
	AnimatedIcon animation;
	JButton btStop;
	JButton btRun;
	JButton btDebug;
	
	DefaultErrorSource errorSource = null;
	
	public JasonID(View view, String position) {
		super(new BorderLayout());

		this.view = view;

		add(BorderLayout.NORTH, createToolBar());

		boolean floating = position.equals(DockableWindowManager.FLOATING);
		if (floating) this.setPreferredSize(new Dimension(500, 250));

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setText("Use the menu Plugin->Jason->New to create a new project.");
		JScrollPane pane = new JScrollPane(textArea);
		add(BorderLayout.CENTER, pane);

        myOut = new OutputStreamAdapter(null, textArea);
        myOut.setAsDefaultOut();

	}
	
	public synchronized void start() {
	}
	
	public synchronized void stop() {
        stopMAS();
    }
	
	private JPanel createToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new BoxLayout(toolBar,BoxLayout.X_AXIS));
		toolBar.setFloatable(false);
		
		JLabel animationLabel = new JLabel();
		animationLabel.setBorder(new EmptyBorder(2,3,2,3));
		Toolkit toolkit = getToolkit();
		animation = new AnimatedIcon(
			toolkit.getImage(JasonID.class.getResource("/images/Blank.png")),
			new Image[] {
				toolkit.getImage(JasonID.class.getResource("/images/Active1.png")),
				toolkit.getImage(JasonID.class.getResource("/images/Active2.png")),
				toolkit.getImage(JasonID.class.getResource("/images/Active3.png")),
				toolkit.getImage(JasonID.class.getResource("/images/Active4.png"))
			},10,animationLabel
		);
		animationLabel.setIcon(animation);
		toolBar.add(animationLabel);
		
		toolBar.add(createToolBarButton("New MAS", GUIUtilities.loadIcon("New.png"), new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				newMAS();
			}
		}));
		
		btRun = createToolBarButton("Run MAS", GUIUtilities.loadIcon("Run.png"), new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runMAS();
			}
		});
		toolBar.add(btRun);
		
		btDebug = createToolBarButton("Debug MAS", GUIUtilities.loadIcon("RunAgain.png"), new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				debugMAS();
			}
		});
		toolBar.add(btDebug);
		
		btStop = createToolBarButton("Stop MAS", GUIUtilities.loadIcon("Cancel.png"), new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stopMAS();
			}
		});
		btStop.setEnabled(false);
		toolBar.add(btStop);
		
		toolBar.add(createToolBarButton("Clear panel", GUIUtilities.loadIcon("Clear.png"), new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		}));
		add(Box.createGlue());
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.add(toolBar);
		return p;
	}
	
	protected JButton createToolBarButton(String toolTip, Icon icon, ActionListener act) {
		JButton button = new RolloverButton(icon);
		button.setMargin(new Insets(0,0,0,0));
		button.setRequestFocusEnabled(false);
		button.setToolTipText(toolTip);
		button.setRequestFocusEnabled(false);
		button.addActionListener(act);
		return button;
	}
	 	
    public void handleMessage(EBMessage message) {
    	/*
        if (message instanceof PropertiesChanged) {
            propertiesChanged();
        } else if (message instanceof BufferUpdate) {
        	BufferUpdate bu = (BufferUpdate)message;
        	if (bu.getBuffer().getPath().endsWith("mas2j")) {
        		if (bu.getWhat() == BufferUpdate.CLOSED) {
        			//projectBufffer = null;
        		} else {
        			projectBufffer = bu.getBuffer();
        			textArea.append("*"+projectBufffer.getPath());
        		}
        	}
        }
        */
    }


	public void addNotify() {
		super.addNotify();
		EditBus.addToBus(this);
	} 
	
	public void removeNotify() {
        super.removeNotify();
        EditBus.removeFromBus(this);
    }

	private Buffer getProjectBuffer() {
		Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
        for (int i = 0; i < bufs.length; i++) {
            if (bufs[i].getPath().endsWith(".mas2j")) {
            	return bufs[i];
            }
        }
        return null;
	}

	private void saveAll() {
		Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
        for (int i = 0; i < bufs.length; i++) {
        	bufs[i].save(view, null);
        }
	}

    private MAS2JProject parseProject(Buffer projectBufffer, boolean debug) {
        // compile
        try {
        	textArea.append("Parsing project file... ");
        	jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new StringReader(projectBufffer.getText(0, projectBufffer.getLength())));
        	MAS2JProject project = parser.mas();
        	project.setDirectory( projectBufffer.getDirectory());
            if (debug) {
            	project.debugOn();
            } else {
            	project.debugOff();            		
            }
            project.writeXMLScript();
            project.writeScripts();
            textArea.append(" parsed successfully!\n");
            return project;
            
        } catch (ParseException ex) {
        	textArea.append("\nmas2j: parsing errors found... \n" + ex + "\n");
        	if (ex.currentToken != null && ex.currentToken.next != null && errorSource != null) {
        		errorSource.addError(new DefaultErrorSource.DefaultError(
        				errorSource, 
        				ErrorSource.ERROR, 
        				projectBufffer.getPath(),
        				ex.currentToken.next.beginLine-1, 0, 0,
        		    	ex.toString()));
        	}
        } catch (TokenMgrError ex) {
        	textArea.append("\nmas2j: error parsing tokens ... \n" + ex + "\n");
        } catch (Exception ex) {
        	textArea.append("Error: "+ex);
        }
        return null;
    }

    private boolean parseProjectAS(MAS2JProject project) {
        // compile
    	File asFile = null;
        try {
        	Iterator iASfile = project.getAllASFiles().iterator();
        	while (iASfile.hasNext()) {
        		asFile = (File)iASfile.next();
            	textArea.append("Parsing AgentSpeak file '"+asFile.getName()+"'...");
            	jason.asSyntax.parser.as2j parser = new jason.asSyntax.parser.as2j(new FileReader(asFile));
            	parser.ag(null);
                textArea.append(" parsed successfully!\n");
        	}
            return true;
            
        } catch (jason.asSyntax.parser.ParseException ex) {
        	textArea.append("\nas2j: parsing errors found... \n" + ex + "\n");
        	if (ex.currentToken != null && ex.currentToken.next != null && errorSource != null) {
        		errorSource.addError(new DefaultErrorSource.DefaultError(
        				errorSource, 
        				ErrorSource.ERROR, 
        				asFile.getAbsolutePath(),
        				ex.currentToken.next.beginLine-1, 0, 0,
        		    	ex.toString()));
        	}
	    	DockableWindowManager d = view.getDockableWindowManager();
	    	if (!d.isDockableWindowVisible("error-list")) {
	    		d.addDockableWindow("error-list");
	        }
        	
        } catch (TokenMgrError ex) {
        	textArea.append("\nmas2j: error parsing tokens ... \n" + ex + "\n");
        } catch (Exception ex) {
        	textArea.append("Error: "+ex);
        }
        return false;
    }

	
	//
	// RunningMASListener methods
	//
	public void masFinished() {
		animation.stop();
		btStop.setEnabled(false);
		btRun.setEnabled(true);
		btDebug.setEnabled(true);
	}
	
	//
	// Menu actions
	//
	
	public void runMAS(boolean debug) {
		Buffer b = getProjectBuffer();
		if (b == null) {
			textArea.setText("There is no Jason project opened!");
		} else {
			textArea.setText("Running project "+b.getName()+"\n");
			if (errorSource == null) {
				errorSource = new DefaultErrorSource("JasonIDE");
				ErrorSource.registerErrorSource(errorSource);
			}
			errorSource.clear();
			MAS2JProject project = parseProject(b, debug);
			if (project != null) {

				saveAll();
				
				if (parseProjectAS(project)) {
					animation.start();
					btStop.setEnabled(true);
					btRun.setEnabled(false);
					btDebug.setEnabled(false);
					if (runner == null) {
						runner = new RunMAS(this);
					}
					runner.run(project);
				}
			}
		}
	}
	
	public void runMAS() {
		runMAS(false);
	}
	public void debugMAS() {
		runMAS(true);
	}

	public void stopMAS() {
		animation.stop();
		btStop.setEnabled(false);
		btRun.setEnabled(true);
		btDebug.setEnabled(true);
		if (runner != null) {
			runner.stopMAS();
			runner = null;
		}
	}

	public void newMAS() {
        String tmpFileName = JOptionPane.showInputDialog("What is the new project name?");

        if (tmpFileName == null) {
        	return;
        }
        if (Character.isUpperCase(tmpFileName.charAt(0))) {
        	tmpFileName = Character.toLowerCase(tmpFileName.charAt(0)) + tmpFileName.substring(1);
        }
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setDialogTitle("Select the project folder");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (f.isDirectory()) {
				String pFile = f.getAbsolutePath() + File.separator + tmpFileName + ".mas2j";
				Buffer b = org.gjt.sp.jedit.jEdit.openFile(view, pFile);
				b.insert(0,MAS2JEditorPane.getDefaultText(tmpFileName, "ag1;"));
			}
		} else {
			return;
		}
	}

	public void editLog() {
		// TODO: implement
		textArea.setText("EditLog not implemented!");
	}
	
}