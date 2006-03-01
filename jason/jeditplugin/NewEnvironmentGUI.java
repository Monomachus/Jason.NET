package jason.jeditplugin;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;

import jason.environment.Environment;

public class NewEnvironmentGUI extends NewAgentGUI {

	private JTextField envClass;
	
	public NewEnvironmentGUI(String title, Buffer b, View view) {
		super(title, b, view);
	}
	
	void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		
		// Fields
		
		envClass = new JTextField(20);
		createField("Environment class name", envClass, "The name of the environment java class.");
		
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "New environment parameters", TitledBorder.LEFT, TitledBorder.TOP));
		p.add(pLabels, BorderLayout.CENTER);
		p.add(pFields, BorderLayout.EAST);
		
		getContentPane().add(p, BorderLayout.CENTER);
		getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	boolean ok() {
		String env = getEnvName();
		if  (env == null) {
			JOptionPane.showMessageDialog(this, "An environment name must be informed.");
			return false;
		}
		try {
			buffer.writeLock();
			String proj = buffer.getText(0, buffer.getLength());
			int pos = proj.lastIndexOf("agents");
			if (pos > 0) {
				pos--;
			} else {
				pos = buffer.getLength();
			}
			buffer.insert(pos, "\tenvironment: "+env+"\n");
		} finally {
			buffer.writeUnlock();
		}
		
		// create new agent buffer
		String envFile = buffer.getDirectory() + env + ".java";
		
		Buffer nb = org.gjt.sp.jedit.jEdit.openFile(view, envFile);
		try {
			nb.writeLock();
			nb.insert(0, getEnvText(env));
			nb.save(view, envFile);
		} finally {
			nb.writeUnlock();
		}
		return true;
	}
	
	private String getEnvName() {
		String env = envClass.getText().trim();
		if (env.length() > 0) {
			// change first letter
			if (Character.isUpperCase(env.charAt(0))) {
				env = Character.toLowerCase(env.charAt(0)) + env.substring(1);
			}
			return env;
		} else {
			return null;
		}
	}
	
	String getEnvText(String className) {
		StringBuffer s = new StringBuffer("// Environment code for project "+buffer.getName()+"\n\n");
		s.append("import jason.asSyntax.*;\n");
		s.append("import jason.environment.*;\n");
		s.append("import java.util.logging.*;\n\n");
		s.append("public class "+className+" extends "+Environment.class.getName()+" {\n\n");
		s.append("\tprivate Logger logger = Logger.getLogger(\""+buffer.getName()+".\"+"+className+".class.getName());\n\n");
		s.append("\tpublic "+className+"() {\n");
		s.append("\t\taddPercept(Literal.parseLiteral(\"percept(demo)\"));\n");
		s.append("\t}\n\n");
		s.append("\tpublic boolean executeAction(String ag, Term action) {\n");
		s.append("\t\tlogger.info(\"executing: \"+action+\", but not implemented!\");\n");
		s.append("\t\treturn true;\n");
		s.append("\t}\n");
		s.append("}\n");
		return s.toString();
	}
}
