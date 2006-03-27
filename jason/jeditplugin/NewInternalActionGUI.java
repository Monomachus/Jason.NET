package jason.jeditplugin;

import jason.asSemantics.InternalAction;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;

public class NewInternalActionGUI extends NewAgentGUI {

	private JTextField iaClass;
	private JTextField iaPkg;
	
	public NewInternalActionGUI(String title, Buffer b, View view) {
		super(title, b, view);
	}
	
	protected void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		
		// Fields
		iaPkg = new JTextField(20);
		createField("Java package", iaPkg, "The name of the java package of the new internal action.");
		
		iaClass = new JTextField(20);
		createField("Internal action name", iaClass, "The name of the new internal action class.");
		
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "New internal action parameters", TitledBorder.LEFT, TitledBorder.TOP));
		p.add(pLabels, BorderLayout.CENTER);
		p.add(pFields, BorderLayout.EAST);
		
		getContentPane().add(p, BorderLayout.CENTER);
		getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	protected boolean ok() {
		if  (iaPkg.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "An package name must be informed.");
			return false;
		}
		if  (iaClass.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "A name for the action must be informed.");
			return false;
		}

		String pck = iaPkg.getText().trim();
		String pckDir = pck.replace('.', '/');
		try {
			// to create directory
			new File(buffer.getDirectory() + pckDir).mkdirs();
		} catch (Exception e) { }
		
		String ia = iaClass.getText().trim();
		if (Character.isUpperCase(ia.charAt(0))) {
			ia = Character.toLowerCase(ia.charAt(0)) + ia.substring(1);
		}
		
		// create new agent buffer
		String iaFile = buffer.getDirectory() + pckDir + File.separator + ia + ".java";
		
		Buffer nb = org.gjt.sp.jedit.jEdit.openFile(view, iaFile);
		try {
			nb.writeLock();
			nb.insert(0, getIAText(pck, ia));
			nb.save(view, iaFile);
		} finally {
			nb.writeUnlock();
		}
		return true;
	}
	
	String getIAText(String pck, String className) {
		StringBuffer s = new StringBuffer("// Internal action code for project "+buffer.getName()+"\n\n");
		s.append("package "+pck+";\n\n");
		s.append("import jason.asSemantics.*;\n");
		s.append("import jason.asSyntax.*;\n");
		s.append("import java.util.logging.*;\n\n");
		
		s.append("public class "+className+" implements "+InternalAction.class.getName()+" {\n\n");

		s.append("\tprivate Logger logger = Logger.getLogger(\""+buffer.getName()+".\"+"+className+".class.getName());\n\n");
		
		s.append("\t public boolean execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {\n");
		s.append("\t\tlogger.info(\"not implemented!\");\n");
		s.append("\t\treturn true;\n");
		s.append("\t}\n");
		s.append("}\n");
		return s.toString();
	}
}
