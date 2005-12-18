package jason.jeditplugin;

import jIDE.ASEditorPane;
import jason.mas2j.MAS2JProject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;

public class NewAgentGUI extends JDialog {

	private JTextField agName;
	private JTextField archClass;
	private JTextField agClass;
	private JTextField nbAgs;
	private JTextField agHost;
	private JComboBox  verbose;
	
	JButton ok;
	JButton cancel;
	
	Buffer buffer = null;
	
	View view;
	
	public NewAgentGUI(String title, Buffer b, View view) {
		super(view);
		buffer = b;
		this.view = view;
		
		setTitle(title);
		initComponents();
		pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int)( (screenSize.width - getWidth()) / 2),(int) ((screenSize.height - getHeight())/2));
        setVisible(true);
	}
	
	void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		
		// Fields
		JPanel fields = new JPanel(new GridLayout(0,1));
		
		agName = new JTextField(10);
		createField(fields, "Agent name", agName, "The agent name. The file will be this name + .asl.");
		
		agClass = new JTextField(10);
		createField(fields, "Agent class", agClass, "The customisation class for the agent (<package.classname>). If not filled, the default agent class will be used.");
		
		archClass = new JTextField(10);
		createField(fields, "Architecture class", archClass, "The customisation class for the agent architecture (<package.classname>). If not filled, the default architecture will be used.");

		nbAgs = new JTextField(5);
		nbAgs.setText("1");
		createField(fields, "Number of agents", nbAgs, "The number of agents that will be instantiated from this declaration.");

		verbose = new JComboBox(new String[] { "no output", "normal", "debug" });
		verbose.setSelectedIndex(1);
		createField(fields, "Verbose", verbose, "Set the verbose level");

		agHost = new JTextField(10);
		agHost.setText("localhost");
		createField(fields, "Host to run", agHost, "The host where this agent will run. The infrastructure must support distributed launching.");
		
		fields.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "New agent parameters", TitledBorder.LEFT, TitledBorder.TOP));

		getContentPane().add(fields, BorderLayout.CENTER);
		getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	JPanel createButtonsPanel() {
		// Buttons
		JPanel bts = new JPanel(new FlowLayout(FlowLayout.CENTER));
		ok = new JButton("Ok");
		ok.setDefaultCapable(true);
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (ok()) {
					setVisible(false);
				}
			}
		});
		bts.add(ok);
		cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		bts.add(cancel);
		getRootPane().setDefaultButton(ok);
		return bts;
	}

	void createField(JPanel fields, String label, JComponent tf, String tooltip) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel jl = new JLabel(label+": ");
		jl.setToolTipText(tooltip);
		p.add(jl);
		p.add(tf);
		tf.setToolTipText(tooltip);
		fields.add(p);
	}
	
	boolean ok() {
		String agDecl = getAgDecl();
		if  (agDecl == null) {
			JOptionPane.showMessageDialog(this, "An agent name must be informed.");
			return false;
		}
		try {
			buffer.writeLock();
			String proj = buffer.getText(0, buffer.getLength());
			int pos = proj.lastIndexOf("}");
			if (pos > 0) {
				pos--;
			} else {
				pos = buffer.getLength();
			}
			buffer.insert(pos, "\n\t\t"+agDecl);
		} finally {
			buffer.writeUnlock();
		}
		
		// create new agent buffer
		String agFile = buffer.getDirectory() + File.separator + agName.getText().trim() + "." + MAS2JProject.AS_EXT;
		
		Buffer nb = org.gjt.sp.jedit.jEdit.openFile(view, agFile);
		try {
			nb.writeLock();
			nb.insert(0, ASEditorPane.getDefaultText(agName.getText().trim() + " in project "+buffer.getName()));
			nb.save(view, agFile);
		} finally {
			nb.writeUnlock();
		}
		
		return true;
	}
	
	private String getAgDecl() {
		if  (agName.getText().trim().length() == 0) {
			return null;
		}
		
		String agDecl = agName.getText().trim();
		if (verbose.getSelectedIndex() != 1) {
			agDecl += " [verbose="+verbose.getSelectedIndex()+"]";
		}
		
		if (archClass.getText().trim().length() > 0) {
			agDecl += " agentArchClass "+archClass.getText().trim();
		}

		if (agClass.getText().trim().length() > 0) {
			agDecl += " agentClass "+agClass.getText().trim();
		}
		if (!nbAgs.getText().trim().equals("1")) {
			agDecl += " #"+nbAgs.getText().trim();			
		}
		if (!agHost.getText().trim().equals("localhost")) {
			agDecl += " at "+agHost.getText().trim();			
		}
		agDecl += ";";
		return agDecl;
	}
}
