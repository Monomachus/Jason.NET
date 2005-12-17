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
//   Revision 1.8  2005/12/17 19:28:46  jomifred
//   no message
//
//   Revision 1.7  2005/12/16 22:41:16  jomifred
//   no message
//
//   Revision 1.6  2005/12/16 22:09:20  jomifred
//   no message
//
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
import jason.runtime.RunCentralisedMAS;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.AnimatedIcon;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.jedit.msg.BufferUpdate;

import projectviewer.ProjectManager;
import projectviewer.ProjectViewer;
import projectviewer.vpt.VPTProject;
import projectviewer.vpt.VPTRoot;
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
		//handleMessage(null); // to try to add parser in sidekick
	}
	
	public synchronized void stop() {
        stopMAS();
        /*
		EditPlugin pv = org.gjt.sp.jedit.jEdit.getPlugin(SideKickPlugin.class.getName(),false);
		if (pv != null) {
			SideKickPlugin.unregisterParser(msp);
		}
		*/
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

		toolBar.addSeparator();
		toolBar.add(createToolBarButton("New MAS", GUIUtilities.loadIcon("New.png"), new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				newMAS();
			}
		}));
		
		toolBar.addSeparator();
		toolBar.add(createToolBarButton("Clear panel", GUIUtilities.loadIcon("Clear.png"), new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		}));
		add(Box.createGlue());
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(toolBar, BorderLayout.EAST);
		
		JLabel jasonLabel = new JLabel("Jason IDE", new ImageIcon(JasonID.class.getResource("/images/Jason-GMoreau-Small-Icon.jpg")), JLabel.CENTER);
		jasonLabel.setFont(new Font("Times", Font.BOLD | Font.ITALIC, 16));
		p.add(jasonLabel, BorderLayout.WEST);
				
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
    	if (msp == null) {
    		EditPlugin pv = org.gjt.sp.jedit.jEdit.getPlugin(SideKickPlugin.class.getName(),false);
    		if (pv != null) {
    			msp = new JasonSideKickParser();
    			SideKickPlugin.registerParser(msp);
    			Log.log(Log.DEBUG,this,"Registered "+msp);
    		}
    	}
    	*/
    	
    	if (message == null) return;
    	
    	/*
        if (message instanceof PropertiesChanged) {
            propertiesChanged();
        } else */

    	if (message instanceof BufferUpdate) {
        	BufferUpdate bu = (BufferUpdate)message;
        	
        	if ((bu.getWhat() == BufferUpdate.LOADED || bu.getWhat() == BufferUpdate.CREATED) &&
        		bu.getBuffer().getPath().endsWith(MAS2JProject.EXT)) {
        		
        		String projName = bu.getBuffer().getName().substring(0, bu.getBuffer().getName().length()-(MAS2JProject.EXT.length()+1));
        		checkProjectView(projName, new File(bu.getBuffer().getDirectory()));
        		
        		//bu.getBuffer().setProperty("sidekick.parser",JasonSideKickParser.ID);

        	}
        	/*
          	if (bu.getWhat() == BufferUpdate.LOADED &&
            	bu.getBuffer().getPath().endsWith(MAS2JProject.AS_EXT)) {
          		bu.getBuffer().setProperty("sidekick.parser", AgentSpeakSideKickParser.ID);
            } 
            */       	
        }
    }


	public void addNotify() {
		super.addNotify();
		EditBus.addToBus(this);
	} 
	
	public void removeNotify() {
        super.removeNotify();
        EditBus.removeFromBus(this);
    }

	/** returns the current MAS2J project */
	private Buffer getProjectBuffer() {

		if (view.getBuffer().getPath().endsWith(MAS2JProject.EXT)) {
			return view.getBuffer();
		}
		Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
        for (int i = 0; i < bufs.length; i++) {
            if (bufs[i].getPath().endsWith(MAS2JProject.EXT)) {
            	return bufs[i];
            }
        }
        return null;
	}
	
	private Buffer getProjectBuffer(String name) {
		Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
        for (int i = 0; i < bufs.length; i++) {
            if (bufs[i].getPath().endsWith(name)) {
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
        	
        	String text;
    		try	{
    			projectBufffer.readLock();
    			text = projectBufffer.getText(0,projectBufffer.getLength());
    		} finally {
    			projectBufffer.readUnlock();
    		}

        	jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(new StringReader(text));
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
			
			
			Buffer[] bufs = org.gjt.sp.jedit.jEdit.getBuffers();
			for (int i = 0; i < bufs.length; i++) {
	        	System.out.println(bufs[i].getStringProperty("sidekick.parser"));
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
        String projName = JOptionPane.showInputDialog("What is the new project name?");

        if (projName == null) {
        	return;
        }
        if (Character.isUpperCase(projName.charAt(0))) {
        	projName = Character.toLowerCase(projName.charAt(0)) + projName.substring(1);
        }
        
        if (getProjectBuffer(projName + "." + MAS2JProject.EXT) != null) {
        	textArea.setText("There already is a project called "+projName);
        	return;
        }
        
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setDialogTitle("Select the project folder");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File projDirectory = chooser.getSelectedFile();
			if (projDirectory.isDirectory()) {
				String pFile = projDirectory.getAbsolutePath() + File.separator + projName + "." + MAS2JProject.EXT;
				Buffer b = org.gjt.sp.jedit.jEdit.openFile(view, pFile);
				try {
					b.writeLock();
					b.insert(0, MAS2JEditorPane.getDefaultText(projName, "ag1;"));
					b.save(view,pFile);
				} finally {
					b.writeUnlock();
				}
				checkProjectView(projName, projDirectory);

		    	DockableWindowManager d = view.getDockableWindowManager();
		    	if (d.getDockableWindow("projectviewer") != null) {
		    		if (!d.isDockableWindowVisible("projectviewer")) {
		    			d.addDockableWindow("projectviewer");
		    		}
		    	}
				
				textArea.setText("Project created!");
			}
		}
	}

	
	private void checkProjectView(String projName, File projDirectory) {
		// add in project viewer
		EditPlugin pv = org.gjt.sp.jedit.jEdit.getPlugin(projectviewer.ProjectPlugin.class.getName(),false);
		if (pv == null) {
			return;
		}

		// we can use the projectviewer plugin
		ProjectManager pm = ProjectManager.getInstance();
		ProjectViewer projView = ProjectViewer.getViewer(view);
		if (! pm.hasProject(projName)) {
			VPTProject proj = new VPTProject(projName);
			proj.setRootPath(projDirectory.getAbsolutePath());

			pm.addProject(proj, VPTRoot.getInstance());
			projView.setActiveNode(view, proj);
			
			JasonProjectImporter fi = new JasonProjectImporter(proj, projView);
			fi.doImport();
							
			// add special actions (new agent, run, ....)
		} else {
			// show it
			//VPTProject proj = pm.getProject(projName);
			//projView.setActiveNode(view, proj);
		}
	}
	
	public void editLog() {
		Buffer curBuf = getProjectBuffer();
        try {
        	InputStream in = JasonID.class.getResource("/"+RunCentralisedMAS.logPropFile).openStream();
        	File f = new File(curBuf.getDirectory() + File.separator + RunCentralisedMAS.logPropFile);
        	if (f.exists()) {
        		in = new FileInputStream(f);
        	}
    		StringBuffer content = new StringBuffer();
    		try {
    			int c = in.read();
    			while (c != -1) {
    				content.append((char)c);
    				c = in.read();
    			}
    		} catch (EOFException e) {
    		} catch (IOException e) {
    			System.err.println("Error reading text!");
    			e.printStackTrace();
    		}
    		Buffer b = org.gjt.sp.jedit.jEdit.openFile(view, f.getAbsolutePath());
			try {
				b.writeLock();
				b.insert(0,content.toString());
				b.save(view,f.getAbsolutePath());
			} finally {
				b.writeUnlock();
			}
				
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
}