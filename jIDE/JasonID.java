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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class JasonID extends EditorPane {
    
    JFrame frame = null;
	JMenuBar  menuBar;
    JTextArea output;
    JTabbedPane tab;
    JToolBar  toolBar;
    StatusBar status;
    JButton runMASButton;
    JButton debugMASButton;
    JButton stopMASButton;
    
    MAS2JParserThread fMAS2jThread;
    ASParserThread    fASParser;
    
    String projectDirectory = "";

    static Properties userProperties = new Properties();
    
    PrintStream originalOut;
    PrintStream originalErr;
    
    //HashMap editorFiles = new HashMap();
    
    AbstractAction newAct;
    OpenProject    openAct;
    Save           saveAct;
    AbstractAction saveAsAct;
	SaveAll        saveAllAct;
    RunMAS         runMASAct;
    DebugMAS       debugMASAct;
    AbstractAction stopMASAct;
    AbstractAction exitAppAct;
    
    public static void main(String[] args) {
        String currJasonVersion;
        String initProject = null;

        try {
            Properties p = new Properties();
            p.load(JasonID.class.getResource("/dist.properties").openStream());
            currJasonVersion = p.getProperty("version") + "." + p.getProperty("release");
        } catch (Exception ex) { 
        	currJasonVersion = "?";
        }
        
        try {
    		// try to load properties from a user preferences file
        	File jasonConfFile = getUserConfFile();
        	if (jasonConfFile.exists()) {
        		userProperties.load(new FileInputStream(jasonConfFile));
        		if (!userProperties.getProperty("version").equals(currJasonVersion)) { 
        			// new version, set all values to default
        			userProperties.remove("javaHome");
        			userProperties.remove("saciJar");
        			userProperties.remove("jasonJar");
        			userProperties.remove("log4jJar");
        		}
        	} 

            // try some default values for properties
            if (userProperties.get("jasonJar") == null || !checkJar(userProperties.getProperty("jasonJar"))) {
            	String jasonJar = getPathFromClassPath("jason.jar");//new File("..").getAbsolutePath();
            	if (checkJar(jasonJar)) {
            		userProperties.put("jasonJar", jasonJar);
            	} else {
            		userProperties.put("jasonJar", File.separator);            		
            	}
            }
            if (userProperties.get("saciJar") == null  || !checkJar(userProperties.getProperty("saciJar"))) {
            	String saciJar = getPathFromClassPath("saci.jar");//new File("../lib/saci").getAbsolutePath();
            	if (checkJar(saciJar)) {
            		userProperties.put("saciJar", saciJar);
            	} else {
            		userProperties.put("saciJar", File.separator);            		
            	}
            }
            if (userProperties.get("log4jJar") == null  || !checkJar(userProperties.getProperty("log4jJar"))) {
            	String log4jJar = getPathFromClassPath("log4j.jar");
            	if (checkJar(log4jJar)) {
            		userProperties.put("log4jJar", log4jJar);
            	} else {
            		userProperties.put("log4jJar", File.separator);            		
            	}
            }
			
            if (userProperties.get("javaHome") == null) {
            	String javaHome = System.getProperty("java.home");
            	if (checkJavaPath(javaHome)) {
            		userProperties.put("javaHome", javaHome);
            	} else {
            		userProperties.put("javaHome", File.separator);            		
            	}
            }
            
            userProperties.put("version", currJasonVersion);
            jasonConfFile.getParentFile().mkdirs();
            storePrefs();

            JasonID jasonID = new JasonID();

            if (args.length > 0) {
                initProject = args[0];
            }
            
            jasonID.createMainFrame(initProject);
        } catch (Throwable t) {
            System.out.println("uncaught exception: " + t);
            t.printStackTrace();
        }
    }

    public static File getUserConfFile() {
    	return new File(System.getProperties().get("user.home") + File.separator + ".jason/user.properties");
    }
    
    public static void storePrefs() {
    	try {
    		userProperties.store(new FileOutputStream(getUserConfFile()), "Jason user configuration");
    	} catch (Exception e) {
    		System.err.println("Error writting preferences");
    		e.printStackTrace();
    	}
    }
    
    public Properties getConf() {
    	return userProperties;
    }
    
	static boolean checkJar(String jar) {
        try {
        	if (jar != null && new File(jar).exists()) {
        		return true;
            }
        } catch (Exception e) {}
        return false;
    }
	/*
	static boolean checkJasonJar(String jasonJar) {
        try {
        	//if (!jasonHome.endsWith(File.separator)) {
        	//	jasonHome += File.separator;
        	//}
        	//File f = jasonHome + "bin" + File.separatorChar + "jason.jar");
        	if (new File(jasonJar).exists()) {
        		return true;
            }
        } catch (Exception e) {}
        return false;
    }
    static boolean checkSaciPath(String saciHome) {
        try {
			if (!saciHome.endsWith(File.separator)) {
        		saciHome += File.separator;
        	}
        	File f = new File(saciHome + "bin" + File.separatorChar + "applications.dtd");
        	if (f.exists()) {
        		return true;
            }
        } catch (Exception e) {}
        return false;
    }
    */
    
    static boolean checkJavaPath(String javaHome) {
        try {
        	if (!javaHome.endsWith(File.separator)) {
        		javaHome += File.separator;
        	}
            File javac1 = new File(javaHome + "bin" + File.separatorChar + "javac");
            File javac2 = new File(javaHome + "bin" + File.separatorChar + "javac.exe");
            if (javac1.exists() || javac2.exists()) {
        		return true;
            }
        } catch (Exception e) {}
        return false;
    }
	static String getPathFromClassPath(String file) {
		StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.endsWith(file)) {
				return new File(token).getAbsolutePath();
			}
		}
		return null;
 	}

    /*
    static String checkSaci(String saciHome) {
        boolean saciHomeOk = false;
        while (!saciHomeOk) {
            try {
                if (checkSaciPath(saciHome)) {
                    saciHomeOk = true;
                    break;
                } else {
                    Object[] options = {"Ok, let me select saci installation directory", "Cancel, try to continue without saci"};
                    int n = JOptionPane.showOptionDialog(null,
                    "Jason needs Saci for some features!\n"+
                    "(see jason manual for more information)\n\n"+
                    "The current saci directory (\""+saciHome+"\") is not a saci directory.",
                    "Saci home directory misconfigured",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
                    if (n == 1) {
                        break;
                    }
                }
                JFileChooser chooser = new JFileChooser(".");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showDialog(null, "Select the SACI/bin directory") == JFileChooser.APPROVE_OPTION) {
                    saciHome = (new File(chooser.getSelectedFile().getPath())).getParentFile().getCanonicalPath();
                }
            } catch (Exception e) {}
        }
        return saciHome;
    }
    */

    /*
    static String checkJavaHome(String javaHome) {
        boolean javaHomeOk = false;
        while (!javaHomeOk) {
            try {
                File javac1 = new File(javaHome + File.separatorChar + "bin" + File.separatorChar + "javac");
                File javac2 = new File(javaHome + File.separatorChar + "bin" + File.separatorChar + "javac.exe");
                if (javac1.exists() || javac2.exists()) {
                    javaHomeOk = true;
                    break;
                } else {
                    Object[] options = {"Ok, let me select java home directory", "Cancel, try to continue without javac"};
                    int n = JOptionPane.showOptionDialog(null,
                    "Jason needs javac for some features!\n\n"+
                    "The current java directory (\""+javaHome+"\") is not a java directory.",
                    "Java home directory misconfigured",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
                    if (n == 1) {
                        break;
                    }
                }
                JFileChooser chooser = new JFileChooser(".");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showDialog(null, "Select the Java Home/bin directory") == JFileChooser.APPROVE_OPTION) {
                    javaHome = (new File(chooser.getSelectedFile().getPath())).getParentFile().getCanonicalPath();
                }
            } catch (Exception e) {}
        }
        return javaHome;
    }
    */
    /*
    static void storeEnv(String saciHome, String javaHome) {
        try {
            if (System.getProperty("os.name").indexOf("indows") > 0) {
                BufferedWriter out = new BufferedWriter(new FileWriter("jason.bat"));
                out.write("@echo off\n");
                //out.write("rem generated by jason, do not edit\n");
                out.write("SET SACI_HOME="+saciHome+"\n");
                out.write("SET JAVA_HOME="+javaHome+"\n");
                out.write("SET PATH=\"%JAVA_HOME%\\bin\";%PATH%\n");
                out.write("java -classpath classes;\"%SACI_HOME%\\bin\\saci.jar\" jIDE.JasonID \"%SACI_HOME%\" \"%JAVA_HOME%\"\n");
                out.close();
            } else {
                BufferedWriter out = new BufferedWriter(new FileWriter("setenv.sh"));
                out.write("#!/bin/sh\n");
                out.write("export SACI_HOME="+saciHome+"\n");
                out.write("export JAVA_HOME="+javaHome+"\n");
                out.write("java -classpath classes:\"$SACI_HOME/bin/saci.jar\" jIDE.JasonID \"$SACI_HOME\" \"$JAVA_HOME\" $1\n");
                out.close();
            }
        } catch (Exception e) {
            System.err.println("Error writing setenv file."+e);
        }
    }
    */
    
    
    public JasonID() {
        super();
        mainID = this;
        extension   = "mas2j";

		newAct      = new NewProject();
	    openAct     = new OpenProject();
	    saveAct     = new Save();
	    saveAsAct   = new SaveAs();
		saveAllAct  = new SaveAll();
		runMASAct   = new RunMAS(this);
        debugMASAct = new DebugMAS(this);
	    stopMASAct  = new StopMAS();
	    exitAppAct  = new ExitApp();
    }
    
    
    JFrame createMainFrame(String initProject) {
        frame = new JFrame();
        frame.setTitle("Jason");
        //frame.setBackground(Color.lightGray);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitAppAct.actionPerformed(null);
            }
        });
        
        //setBorder(BorderFactory.createEtchedBorder());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(BorderLayout.NORTH, createMenuBar());
        
        tab = new JTabbedPane();
        tab.add("project", this);
        
        int height = 440;
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setOneTouchExpandable(true);
        split.setTopComponent(tab);
        split.setBottomComponent(createOutput());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(BorderLayout.NORTH, createToolBar());
        panel.add(BorderLayout.CENTER, split);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.getContentPane().add(BorderLayout.SOUTH, createStatusBar());
        
        frame.pack();
        frame.setSize((int)(height * 1.618), height);
        split.setDividerLocation(height-200);
        frame.setVisible(true);
        
        
        if (initProject != null) {
            openAct.loadProject(new File(initProject));
        }
        fMAS2jThread = new MAS2JParserThread( this, this);
        fMAS2jThread.start();
        fASParser = new ASParserThread( this );
        fASParser.start();
        return frame;
    }
    
    
    protected boolean checkNeedsSave() {
        for (int i = 0; i<tab.getComponentCount(); i++) {
            if (! checkNeedsSave(i)) {
                return false;
            }
        }
        return true;
    }
    
    protected boolean checkNeedsSave(int indexTab) {
        EditorPane pane = (EditorPane)tab.getComponentAt(indexTab);
        if (pane.modified) {
            tab.setSelectedIndex(indexTab);
            int op = JOptionPane.showConfirmDialog(mainID.getFrame(), "Do you want to save "+pane.getFileName()+"?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                saveAct.actionPerformed(null);
            } else if (op == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }
    
    void openAllASFiles(Collection files) {
        // remove files not used anymore
        for (int i = 1; i<tab.getComponentCount(); i++) {
            boolean alreadyHas = false;
            Iterator iFiles = files.iterator();
            while (iFiles.hasNext()) {
                String sFile = removeExtension(iFiles.next().toString());
                if (tab.getTitleAt(i).startsWith(sFile)) {
                    alreadyHas = true;
                    break;
                }
            }
            if (!alreadyHas) {
                if (checkNeedsSave(i)) {
                    tab.remove(i);
                    i--;
                }
            }
        }
        Iterator iFiles = files.iterator();
        while (iFiles.hasNext()) {
            File file = new File(iFiles.next().toString());
            String sFile = removeExtension(file.toString());
            boolean alreadyHas = false;
            for (int i = 1; i<tab.getComponentCount(); i++) {
                if (tab.getTitleAt(i).startsWith(sFile)) {
                    alreadyHas = true;
                    break;
                }
            }
            if (!alreadyHas) {
                int tabIndex = tab.getComponentCount();
                EditorPane newPane = new EditorPane(this, tabIndex);
                newPane.setFileName(file);
                tab.add("new", newPane);
                openAct.load(tabIndex, file, newPane);
            }
        }
    }
    
    /**
     * Create a status bar
     */
    protected Component createStatusBar() {
        // need to do something reasonable here
        status = new StatusBar();
        return status;
    }
    
    protected Component createOutput() {
        output = new JTextArea();
        output.setEditable(false);
        //output.setEnabled(false);
        JScrollPane scroller = new JScrollPane(output);
        MyOutputStream out = new MyOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(out);
        System.setErr(out);
        return scroller;
    }
    
    
    class MyOutputStream extends java.io.PrintStream {
        MyOutputStream() {
            super(System.out);
        }
        public void print(String s) {
            output.append(s);
        }
        public void println(String s) {
            output.append(s+"\n");
        }
    }
    
    
    protected JToolBar createToolBar() {
        toolBar = new JToolBar();
        createToolBarButton(newAct, "Start new project");
        createToolBarButton(openAct, "Open project");
        createToolBarButton(saveAct, "Save project");
        toolBar.addSeparator();
        runMASButton = createToolBarButton(runMASAct, "Run MAS");
        debugMASButton = createToolBarButton(debugMASAct, "Debug MAS");
        stopMASButton = createToolBarButton(stopMASAct, "Stop MAS");
        stopMASButton.setEnabled(false);
        return toolBar;
    }
    
    protected JMenuBar createMenuBar() {
        
        JMenu jMenuProject = new JMenu("Project");
        jMenuProject.add(newAct);
        jMenuProject.add(openAct);
        jMenuProject.add(saveAct);
        jMenuProject.add(saveAsAct);
        jMenuProject.add(saveAllAct);
        
        jMenuProject.addSeparator();
        
        jMenuProject.add(runMASAct);
        jMenuProject.add(debugMASAct);
        jMenuProject.add(stopMASAct);
        
        jMenuProject.addSeparator();
        
        jMenuProject.add(exitAppAct);

        JMenu jMenuEdit = new JMenu("Edit");
        jMenuEdit.add(new EditPreferences());
        
        
        JMenu jMenuHelp = new JMenu("Help");
        jMenuHelp.add(new HelpAbout());
        
        menuBar = new JMenuBar();
        menuBar.add(jMenuProject);
        menuBar.add(jMenuEdit);
        menuBar.add(jMenuHelp);
        return menuBar;
    }
    
    protected JButton createToolBarButton(Action act, String toolTip) {
        JButton button;
        button = toolBar.add(act);
        button.setRequestFocusEnabled(false);
        button.setMargin(new Insets(1,1,1,1));
        button.setToolTipText(toolTip);
        return button;
    }
    
    
    /**
     * Find the hosting frame, for the file-chooser dialog.
     */
    Frame getFrame() {
        for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof Frame) {
                return (Frame) p;
            }
        }
        return null;
    }
    
    protected void updateTabTitle(int index, EditorPane pane, String error) {
        String title = "";
        if (pane.getFileName().length() > 0) {
            title = pane.getFileName() + "." + pane.extension;
        }
        if (pane.modified) {
            title += " [*]";
        }
        if (error != null) {
            title += " "+error;
        }
        if (index < tab.getTabCount()) {
            tab.setTitleAt(index, title);
        }
    }
    
    
    class StatusBar extends JPanel {
        //JLabel text = new JLabel();
        JProgressBar progress = new JProgressBar();
        
        public StatusBar() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            //add(text);
            //add(Box.createHorizontalStrut(5));
            add(progress);
        }
        
        /*
        public void updateText() {
            String status = "";
            if (projectName.length() > 0) {
                status = projectDirectory+File.separatorChar+projectName;
            }
            if (modified) {
                status += " [*]";
            }
            text.setText(status);
        }
         */
    }
    
    //
    //
    //     Project menu (file part)
    //     -----------------------------
    //
    
    class NewProject extends AbstractAction {
        JFileChooser chooser;

    	NewProject() {
            super("New project", new ImageIcon( JasonID.class.getResource("/images/new.gif")));
            chooser = new JFileChooser(System.getProperty("user.dir"));
            //chooser.setFileFilter(new DirectoryFileFilter());
            chooser.setDialogTitle("Select the project directory");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (checkNeedsSave()) {
                String tmpFileName = JOptionPane.showInputDialog("What is the new project name?");

                if (tmpFileName == null) {
                	return;
                }

                if (chooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (f.isDirectory()) {
						projectDirectory = f.getAbsolutePath();
					}
				} else {
					return;
				}
                
	            tab.removeAll();
                tab.add("Project",  JasonID.this);
                setFileName(tmpFileName);
                createNewPlainText(getDefaultText(tmpFileName));
                revalidate();
                modified = true;
                needsParsing = true;
                updateTabTitle(0, (EditorPane)tab.getComponentAt(0), null);
                    
                EditorPane newPane = new EditorPane(JasonID.this, 1);
                newPane.setFileName("ag1.asl");
                newPane.modified = true;
                newPane.needsParsing = true;
                tab.add("new", newPane);
                newPane.createNewPlainText("demo.\n+demo : true <- .print(\"hello world.\").");
                updateTabTitle(1, newPane, null);
                
            }
        }
    }
    
    
    class OpenProject extends AbstractAction {
        JFileChooser chooser;
        OpenProject() {
            super("Open project", new ImageIcon(JasonID.class.getResource("/images/openProject.gif")));
            chooser = new JFileChooser(System.getProperty("user.dir"));
            chooser.setFileFilter(new JasonFileFilter());
        }
        
        public void actionPerformed(ActionEvent e) {
            if (checkNeedsSave()) {
                if (chooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    if (f.isFile()) {
                    	runMASButton.setEnabled(false);
                    	debugMASButton.setEnabled(false);
                        tab.removeAll();
                        tab.add(f.getName(),  JasonID.this);
                        loadProject(f);
                    }
                }
            }
        }
        
        
        void loadProject(File f) {
            try {
                projectDirectory = f.getParentFile().getCanonicalPath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setFileName(f);
            load(0, f, JasonID.this);
        }
        
        void load(int tabIndex, File f, EditorPane pane) {
            pane.createNewPlainText("");
            pane.modified = false;
            Thread loader = new FileLoader(f, pane);
            loader.start();
            updateTabTitle(tabIndex, pane, null);
        }
        
        class FileLoader extends Thread {
            EditorPane pane;
            File f;
            FileLoader(File f, EditorPane pane) {
                this.f = f;
                this.pane = pane;
            }
            
            public void run() {
                try {
                    pane.needsParsing = false;
                    Document doc = pane.editor.getDocument();
                    status.progress.setMinimum(0);
                    status.progress.setMaximum((int) f.length());
                    // try to start reading
                    java.io.Reader in = new java.io.FileReader(f);
                    char[] buff = new char[4096];
                    int nch;
                    while ((nch = in.read(buff, 0, buff.length)) != -1) {
                        doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
                        status.progress.setValue(status.progress.getValue() + nch);
                    }
                    //status.progress.setValue(0);
                    pane.needsParsing = true;
                    pane.undo.discardAllEdits();
                    if (fMAS2jThread != null) {
                    	fMAS2jThread.stopWaiting();
                    }
                } catch (java.io.IOException e) {
                	System.err.println("I/O error for "+f+" -- "+e.getMessage());
                    e.printStackTrace();
                } catch (BadLocationException e) {
                	System.err.println("BadLocationException error for "+f+" -- "+e.getMessage());
                    e.printStackTrace();
                } finally {
                	runMASButton.setEnabled(true);
                	debugMASButton.setEnabled(true);
                }
            }
        }
    }
    
    
    class Save extends AbstractAction {
        Save() {
            super("Save", new ImageIcon(JasonID.class.getResource("/images/save.gif")));
        }
        
        public void actionPerformed(ActionEvent e) {
            savePane(tab.getSelectedIndex(), (EditorPane)tab.getSelectedComponent());
        }
		
		public void savePane(int index, EditorPane pane) {
            if (pane.getFileName().length() == 0) {
                saveAsAct.actionPerformed(null);
            } else {
                File f = new File(projectDirectory+File.separatorChar+pane.getFileName()+"."+pane.extension);
                output.append("Saving to "+f.getPath()+"\n");
                try {
                    Document doc  = pane.editor.getDocument();
                    String text = doc.getText(0, doc.getLength());
                    status.progress.setMinimum(0);
                    status.progress.setMaximum(doc.getLength());
                    java.io.Writer out = new java.io.FileWriter(f);
                    for (int i = 0; i < doc.getLength(); i++) {
                        out.write(text.charAt(i));
                        status.progress.setValue(i);
                    }
                    out.close();
                    //status.progress.setValue(0);
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                pane.modified = false;
                updateTabTitle(index, pane, null);
            }
        }
    }

    class SaveAll extends AbstractAction {
        SaveAll() {
            super("Save all", new ImageIcon(JasonID.class.getResource("/images/save.gif")));
        }
        
        public void actionPerformed(ActionEvent e) {
	        for (int i = 0; i<tab.getComponentCount(); i++) {
				EditorPane pane = (EditorPane)tab.getComponentAt(i);
		        if (pane.modified) {
					saveAct.savePane(i,pane);
		        }
			}
        }
    }
	
	
    class SaveAs extends AbstractAction {
        JFileChooser chooser;
        SaveAs() {
            super("Save as ...", new ImageIcon(JasonID.class.getResource("/images/save.gif")));
            chooser = new JFileChooser(System.getProperty("user.dir"));
            chooser.setFileFilter(new JasonFileFilter());
        }
        
        public void actionPerformed(ActionEvent e) {
            if (chooser.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (! f.getName().toLowerCase().endsWith(extension)) {
                    f = new File(f.getPath()+"."+extension);
                }
                if (f.getName().toLowerCase().endsWith(extension)) {
                    projectDirectory = f.getParentFile().getPath();
                }
                EditorPane pane = (EditorPane)tab.getSelectedComponent();
                
                pane.setFileName(f);
                saveAct.actionPerformed(e);
            }
        }
    }
    
    //
    //
    //     Project menu (execuion part)
    //     -----------------------------
    //
    
    class StopMAS extends AbstractAction {
        StopMAS() {
            super("Stop MAS", new ImageIcon(JasonID.class.getResource("/images/suspend.gif")));
        }
        
        public void actionPerformed(ActionEvent e) {
            runMASAct.stopMAS();
        }
    }
    
    class ExitApp extends AbstractAction {
        ExitApp() {
            super("Exit");
        }
        
        public void actionPerformed(ActionEvent e) {
            if (checkNeedsSave()) {
                runMASAct.exitJason();
                System.exit(0);
            }
        }
    }

    class EditPreferences extends AbstractAction {
    	JDialog d = null;
    	JTextField saciTF;
    	JTextField jasonTF;
    	JTextField javaTF;
    	
        EditPreferences() {
            super("Preferences...");
        	d = new JDialog(frame, "Jason Preferences");
        	d.getContentPane().setLayout(new GridLayout(0, 1));

        	// jason home
        	JPanel jasonHomePanel = new JPanel();
        	jasonHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        	jasonHomePanel.add(new JLabel("Jason jar"));
        	jasonTF = new JTextField(30);
        	jasonHomePanel.add(jasonTF);
        	JButton setJason = new JButton("Set");
        	setJason.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
		            try {
						JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
						chooser.setDialogTitle("Select the jason.jar file");
						chooser.setFileFilter(new JarFileFilter("jason.jar", "The Jason.jar file"));
		                //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		                if (chooser.showOpenDialog(JasonID.this) == JFileChooser.APPROVE_OPTION) {
		                	String jasonJar = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
		                	if (checkJar(jasonJar)) {
								jasonTF.setText(jasonJar);
		                	}
		                }
		            } catch (Exception e) {}
				}
        	});
        	jasonHomePanel.add(setJason);
        	d.getContentPane().add(jasonHomePanel);
        	
        	
        	// saci home
        	JPanel saciHomePanel = new JPanel();
        	saciHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        	saciHomePanel.add(new JLabel("Saci jar"));
        	saciTF = new JTextField(30);
        	saciHomePanel.add(saciTF);
        	JButton setSaci = new JButton("Set");
        	setSaci.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
		            try {
		                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
						chooser.setDialogTitle("Select the Saci.jar file");
						chooser.setFileFilter(new JarFileFilter("saci.jar", "The Saci.jar file"));
						//chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		                if (chooser.showOpenDialog(JasonID.this) == JFileChooser.APPROVE_OPTION) {
		                	String saciJar = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
		                	if (checkJar(saciJar)) {
		                		saciTF.setText(saciJar);
		                	}
		                }
		            } catch (Exception e) {}
				}
        	});
        	saciHomePanel.add(setSaci);
        	d.getContentPane().add(saciHomePanel);

        	// java home
        	JPanel javaHomePanel = new JPanel();
        	javaHomePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        	javaHomePanel.add(new JLabel("Java home"));
        	javaTF = new JTextField(30);
        	javaHomePanel.add(javaTF);
        	JButton setJava = new JButton("Set");
        	setJava.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
		            try {
		                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
						 chooser.setDialogTitle("Select the Java Home directory");
		                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		                if (chooser.showOpenDialog(JasonID.this) == JFileChooser.APPROVE_OPTION) {
		                	String javaHome = (new File(chooser.getSelectedFile().getPath())).getCanonicalPath();
		                	if (checkJavaPath(javaHome)) {
		                		javaTF.setText(javaHome);
		                	}
		                }
		            } catch (Exception e) {}
				}
        	});
        	javaHomePanel.add(setJava);
        	d.getContentPane().add(javaHomePanel);

        	
        	JPanel btPanel = new JPanel();
        	btPanel.setLayout(new FlowLayout());
        	JButton okBt = new JButton("Ok");
        	btPanel.add(okBt);
        	okBt.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (checkJar(saciTF.getText())) {
						userProperties.put("saciJar", saciTF.getText());
					}
					if (checkJar(jasonTF.getText())) {
						userProperties.put("jasonJar", jasonTF.getText());
					}
					if (checkJavaPath(javaTF.getText())) {
						userProperties.put("javaHome", javaTF.getText());
					}
					storePrefs();
					d.setVisible(false);
				}
        	});
        	JButton canelBt = new JButton("Cancel");
        	canelBt.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					d.setVisible(false);
				}
        	});
        	btPanel.add(canelBt);
        	d.getContentPane().add(btPanel);
        	d.pack();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            d.setLocation((int)( (screenSize.width - d.getWidth()) / 2),(int) ((screenSize.height -d.getHeight())/2));
        }
        
        public void actionPerformed(ActionEvent e) {
        	saciTF.setText(userProperties.getProperty("saciJar"));
        	jasonTF.setText(userProperties.getProperty("jasonJar"));
        	javaTF.setText(userProperties.getProperty("javaHome"));
        	d.setVisible(true);
        }
    }

    
    class HelpAbout extends AbstractAction {
        HelpAbout() {
            super("About...");
        }
        
        // TODO: put the copyright of the image bellow it.
        
        public void actionPerformed(ActionEvent e) {
            String version = "";
            String build = "";

            try {
                Properties p = new Properties();
                p.load(JasonID.class.getResource("/dist.properties").openStream());
                version = "Jason " + p.get("version") + "." + p.get("release");
                build = " build " + p.get("build") + " on " + p.get("build.date") + "\n\n";
            } catch (Exception ex) { }

            JOptionPane.showMessageDialog( getFrame(),
            version +  build+
            "Copyright (C) 2003-2005  Rafael H. Bordini, Jomi F. Hubner, et al.\n\n"+
            "This library is free software; you can redistribute it and/or\n"+
            "modify it under the terms of the GNU Lesser General Public\n"+
            "License as published by the Free Software Foundation; either\n"+
            "version 2.1 of the License, or (at your option) any later version.\n\n"+
            "This library is distributed in the hope that it will be useful,\n"+
            "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
            "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
            "GNU Lesser General Public License for more details.\n\n"+
            "You should have received a copy of the GNU Lesser General Public\n"+
            "License along with this library; if not, write to the Free Software\n"+
            "Foundation, Inc., 59 Temple Place, Suite 330,\nBoston, MA  02111-1307  USA\n\n"+
			"About the image: Jason by Gustave Moreau (1865).\n"+
			"Copyright Photo RMN (Agence Photographique de la Réunion des\n"+
			"Musées Nationaux, France). Photograph by Hervé Lewandowski.\n\n"+
            "To contact the authors:\n"+
            "http://www.csc.liv.ac.uk/~bordini\n"+
            "http://www.inf.furb.br/~jomi",
            "JasonID - About",
            JOptionPane.INFORMATION_MESSAGE,
            new ImageIcon(JasonID.class.getResource("/images/Jason-GMoreau-Small.jpg")));
        }
    }
    
    class JasonFileFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            
            String s = f.getName();
            String ext = null;
            int i = s.lastIndexOf('.');
            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            if (ext != null) {
                if (ext.equals(extension)) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
        
        public String getDescription() {
            return "Jason project files";
        }
    }

    class JarFileFilter extends FileFilter {
		String jar,ds;
		public JarFileFilter(String jar, String ds) {
			this.jar = jar;
			this.ds  = ds;
		}
        public boolean accept(File f) {
            if (f.getName().endsWith(jar)) {
				return true;
            } else {
				return false;
            }
        }
        
        public String getDescription() {
            return ds;
        }
    }
}
