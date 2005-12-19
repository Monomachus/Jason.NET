package jason.jeditplugin;

import jason.mas2j.MAS2JProject;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DockableWindowManager;

public class NewProjectGUI extends NewAgentGUI {

	JTextField projName;
	JTextField projDir;
	JLabel     projFinalDir;
	JTextField projEnv;
	JComboBox  projInfra;
	
	JasonID jasonID;
	
	public NewProjectGUI(String title, View view, JasonID jasonID) {
		super(title, null, view);
		this.jasonID = jasonID;
	}
	
	void initComponents() {
		getContentPane().setLayout(new BorderLayout());

		
		// Fields
		JPanel fields = new JPanel(new GridLayout(0,1));
		
		projName = new JTextField(10);
		createField(fields, "Project name", projName, "The project name");
		
		projEnv = new JTextField(10);
		createField(fields, "Environment class", projEnv, "The java class that implements the environment (<package.classname>). If not filled, the default class will be used.");
		
		projInfra = new JComboBox(new String[] { "Centralised", "Saci" });
		projInfra.setSelectedIndex(0);
		createField(fields, "Infrastructure", projInfra, "Set the Infrastructure");

    	JPanel jasonHomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	jasonHomePanel.add(new JLabel("Root location:"));
    	projDir = new JTextField(20);
    	projDir.setText(System.getProperty("user.home"));
    	jasonHomePanel.add(projDir);
    	JButton setDir = new JButton("Browse");
    	setDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	            try {
					JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
					chooser.setDialogTitle("Select the project directory");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	        			File projDirectory = chooser.getSelectedFile();
	        			if (projDirectory.isDirectory()) {
	        				projDir.setText(projDirectory.getCanonicalPath());
	        			}
	                }
	            } catch (Exception e) {}
			}
    	});
    	jasonHomePanel.add(setDir);
    	fields.add(jasonHomePanel);
	
    	projFinalDir = new JLabel();
    	createField(fields, "Directory", projFinalDir, "The directory that will be created for the project.");
		// doc listener for Final proj dir
		DocumentListener docLis = new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
		        updateProjDir();
		    }
		    public void removeUpdate(DocumentEvent e) {
		        updateProjDir();
		    }
		    public void changedUpdate(DocumentEvent e) {
		        updateProjDir();
		    }
		};
		projName.getDocument().addDocumentListener(docLis);
		projDir.getDocument().addDocumentListener(docLis);

		
		fields.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "New project parameters", TitledBorder.LEFT, TitledBorder.TOP));

		getContentPane().add(fields, BorderLayout.CENTER);
		getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	void updateProjDir() {
		projFinalDir.setText(projDir.getText() + File.separator + projName.getText());
	}

	
	boolean ok() {
		String projDecl = getProjDecl();
		if  (projDecl == null) {
			return false;
		}
		
		File finalDir = new File(projFinalDir.getText().trim());
		try {
			if (!finalDir.exists()) {
				finalDir.mkdirs();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error creating project directory: "+e);
			return false;
		}
		
		String pFile = finalDir + File.separator + projName.getText() + "." + MAS2JProject.EXT;
		Buffer b = org.gjt.sp.jedit.jEdit.openFile(view, pFile);
		try {
			b.writeLock();
			b.insert(0, projDecl);
			b.save(view,pFile);
		} finally {
			b.writeUnlock();
		}
		jasonID.checkProjectView(projName.getText(), new File(projDir.getText()));

    	DockableWindowManager d = view.getDockableWindowManager();
    	if (d.getDockableWindow("projectviewer") != null) {
    		if (!d.isDockableWindowVisible("projectviewer")) {
    			d.addDockableWindow("projectviewer");
    		}
    	}
		
    	jasonID.textArea.setText("Project created!");
		return true;
	}
	
	private String getProjDecl() {
		if  (projName.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "A project name must be informed.");
			return null;
		}
		String name = projName.getText().trim();
        if (Character.isUpperCase(name.charAt(0))) {
        	name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        if (jasonID.getProjectBuffer(name + "." + MAS2JProject.EXT) != null) {
        	JOptionPane.showMessageDialog(this, "There already is a project called "+name);
        	return null;
        }

		String projDecl = "/* Jason Project */\n\nMAS "+name+ " {\n";
		
		projDecl += "\tinfrastructure: "+projInfra.getSelectedItem()+"\n";
		
		if (projEnv.getText().trim().length() > 0) {
			projDecl += "\tenvironment: "+projEnv.getText().trim()+"\n";
		}

		projDecl += "\tagents:\n";

		projDecl += "}\n";
		return projDecl;
	}
}
