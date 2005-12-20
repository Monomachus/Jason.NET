package jason.jeditplugin;

import jIDE.ASEditorPane;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

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

	JPanel pFields = new JPanel(new GridLayout(0,1));
	JPanel pLabels = new JPanel(new GridLayout(0,1));

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
		
		agName = new JTextField(10);
		createField("Agent name", agName, "The agent name. The file will be this name + .asl.");
		
		agClass = new JTextField(20);
		createField("Agent class", agClass, "The customisation class for the agent (<package.classname>). If not filled, the default agent class will be used.");
		
		archClass = new JTextField(20);
		createField("Architecture class", archClass, "The customisation class for the agent architecture (<package.classname>). If not filled, the default architecture will be used.");

		nbAgs = new JTextField(4);
		nbAgs.setText("1");
		createField("Number of agents", nbAgs, "The number of agents that will be instantiated from this declaration.");

		verbose = new JComboBox(new String[] { "no output", "normal", "debug" });
		verbose.setSelectedIndex(1);
		createField("Verbose", verbose, "Set the verbose level");

		agHost = new JTextField(10);
		agHost.setText("localhost");
		createField( "Host to run", agHost, "The host where this agent will run. The infrastructure must support distributed launching.");
		
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "New agent parameters", TitledBorder.LEFT, TitledBorder.TOP));
		p.add(pLabels, BorderLayout.CENTER);
		p.add(pFields, BorderLayout.EAST);
		
		getContentPane().add(p, BorderLayout.CENTER);
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

	void createField(String label, JComponent tf, String tooltip) {
		JLabel jl = new JLabel(label+": ");
		jl.setToolTipText(tooltip);
		tf.setToolTipText(tooltip);
		pLabels.add(jl);
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(tf);
		pFields.add(p);
	}

	boolean ok() {
		AgentParameters agDecl = getAgDecl();
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
			buffer.insert(pos, "\n\t\t"+agDecl.getAsInMASProject());
		} finally {
			buffer.writeUnlock();
		}
		
		// create new agent buffer
		String agFile = buffer.getDirectory() + agName.getText().trim() + "." + MAS2JProject.AS_EXT;
		
		Buffer nb = org.gjt.sp.jedit.jEdit.openFile(view, agFile);
		try {
			nb.writeLock();
			nb.insert(0, ASEditorPane.getDefaultText(agName.getText().trim() + " in project "+buffer.getName()));
			nb.save(view, agFile);
		} finally {
			nb.writeUnlock();
		}
		
		agDecl.asSource = new File(agFile);
		JasonID.listModel.addElement(agDecl);
		return true;
	}
	
	private AgentParameters getAgDecl() {
		if  (agName.getText().trim().length() == 0) {
			return null;
		}
		AgentParameters ap = new AgentParameters();
		ap.name = agName.getText().trim();
		if (verbose.getSelectedIndex() != 1) {
			ap.options = new HashMap();
			ap.options.put("verbose", verbose.getSelectedIndex()+"");
		}
		
		if (archClass.getText().trim().length() > 0) {
			ap.archClass = archClass.getText().trim();
		}

		if (agClass.getText().trim().length() > 0) {
			ap.agClass = agClass.getText().trim();
		}
		if (!nbAgs.getText().trim().equals("1")) {
			try {
				ap.qty = Integer.parseInt(nbAgs.getText().trim());
			} catch (Exception e) {
				System.err.println("Number of hosts is not a number!");
			}
		}
		if (!agHost.getText().trim().equals("localhost")) {
			ap.host = agHost.getText().trim();			
		}
		return ap;
	}
}
