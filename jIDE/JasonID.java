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
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class JasonID extends EditorPane {
    
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
    static String saciHome;
    static String jasonHome;
    static String javaHome;
    
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
        try {
            javaHome = null;
            saciHome = null;
            jasonHome = new File("..").getCanonicalPath();
            String initProject = null;
            
            if (args.length >= 2) {
                saciHome = args[0];
                javaHome = args[1];
            }
            boolean changed = false;
            String temp = checkSaci(saciHome);
            if (temp != null) {
                if (!temp.equals(saciHome)) {
                    changed = true;
                    saciHome = temp;
                }
            }
            temp = checkJavaHome(javaHome);
            if (temp != null) {
                if (!temp.equals(javaHome)) {
                    changed = true;
                    javaHome = temp;
                }
            }
            if (changed) {
                storeEnv(saciHome, javaHome);
                if (System.getProperty("os.name").indexOf("indows") > 0) {
                		JOptionPane.showMessageDialog(null,"You need to start Jason again since the configuration was changed.");
                }
                System.exit(1);
            }
            
            if (args.length == 3) {
                initProject = args[2];
            }
            
            JasonID jasonID = new JasonID();
            jasonID.createMainFrame(initProject);
        } catch (Throwable t) {
            System.out.println("uncaught exception: " + t);
            t.printStackTrace();
        }
    }
    
    static String checkSaci(String saciHome) {
        boolean saciHomeOk = false;
        // try in properties
        /*
        Properties prop = new Properties();
        try {
            prop.load(new java.io.FileInputStream("jasonID.properties"));
            if (prop.getProperty("saciHome") != null) {
                saciHome = prop.getProperty("saciHome");
            }
        } catch (Exception e) {}
         */
        while (!saciHomeOk) {
            try {
                File saciAppDTD = new File(saciHome + File.separatorChar + "bin" + File.separatorChar + "applications.dtd");
                if (saciAppDTD.exists()) {
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
                    /*
                    prop.put("saciHome", saciHome);
                    java.io.OutputStream propFile = new java.io.FileOutputStream("jasonID.properties");
                    prop.store(propFile, "jasonID properties");
                    propFile.close();
                     */
                }
            } catch (Exception e) {}
        }
        return saciHome;
    }
    
    static String checkJavaHome(String javaHome) {
        boolean javaHomeOk = false;
        /*
        // try in properties
        Properties prop = new Properties();
        try {
            prop.load(new java.io.FileInputStream("jasonID.properties"));
            if (prop.getProperty("javaHome") != null) {
                javaHome = prop.getProperty("javaHome");
            }
        } catch (Exception e) {}
         */
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
                    /*
                    prop.put("javaHome", javaHome);
                    java.io.OutputStream propFile = new java.io.FileOutputStream("jasonID.properties");
                    prop.store(propFile, "jasonID properties");
                    propFile.close();
                     */
                }
            } catch (Exception e) {}
        }
        return javaHome;
    }
    
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
    		/*
    		try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
    		*/
    	
        JFrame frame = new JFrame();
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
        fMAS2jThread = new MAS2JParserThread( this, this, jasonHome, saciHome);
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
        
        JMenu jMenuHelp = new JMenu("Help");
        jMenuHelp.add(new HelpAbout());
        
        menuBar = new JMenuBar();
        menuBar.add(jMenuProject);
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
        NewProject() {
            super("New project", new ImageIcon( JasonID.class.getResource("/images/new.gif")));
        }
        
        public void actionPerformed(ActionEvent e) {
            if (checkNeedsSave()) {
                String tmpFileName = JOptionPane.showInputDialog("What is the new project name?");
                if (tmpFileName != null) {
                    tab.removeAll();
                    tab.add("Project",  JasonID.this);
                    createNewPlainText(getDefaultText());
                    revalidate();
                    modified = false;
                    needsParsing = true;
                    projectDirectory = "";
                    setFileName(tmpFileName);
                    updateTabTitle(0, (EditorPane)tab.getComponentAt(0), null);
                }
            }
        }
    }
    
    
    class OpenProject extends AbstractAction {
        JFileChooser chooser;
        OpenProject() {
            super("Open project", new ImageIcon(JasonID.class.getResource("/images/openProject.gif")));
            chooser = new JFileChooser(".");
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
                    System.err.println(e.toString());
                } catch (BadLocationException e) {
                    System.err.println(e.getMessage());
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
                    System.err.println(ex.toString());
                } catch (BadLocationException ex) {
                    System.err.println(ex.getMessage());
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
            chooser = new JFileChooser(".");
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
                runMASAct.stopMAS();
                runMASAct.exitJason();
                System.exit(0);
            }
        }
    }
    
    class HelpAbout extends AbstractAction {
        HelpAbout() {
            super("About...");
        }
        
        // TODO: put the copyright of the image bellow it.
        
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog( getFrame(),
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
    
}
