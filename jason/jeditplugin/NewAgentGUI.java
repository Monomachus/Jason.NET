package jason.jeditplugin;

import jason.infra.centralised.StartNewAgentGUI;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;

import java.io.File;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;

public class NewAgentGUI extends StartNewAgentGUI {

	Buffer buffer = null;
	View view;
	
	public NewAgentGUI(String title, Buffer b, View view, String openDir) {
		super(view, title, openDir);
		buffer = b;
		this.view = view;
	}
	

	protected boolean ok() {
		AgentParameters agDecl = getAgDecl();
		if  (agDecl == null) {
			JOptionPane.showMessageDialog(this, "An agent name must be informed.");
			return false;
		}
		if (agDecl.asSource == null) {
			agDecl.asSource = new File(buffer.getDirectory() + agDecl.name + "." + MAS2JProject.AS_EXT);
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
		
		boolean newFile = !agDecl.asSource.exists();
		Buffer nb = org.gjt.sp.jedit.jEdit.openFile(view, agDecl.asSource.getAbsolutePath());
		if (newFile) {
			try {
				nb.writeLock();
				String agcode = "// "+agName.getText().trim() + " in project "+buffer.getName()+
								"\ndemo.\n+demo : true <- .print(\"hello world.\").";
				nb.insert(0, agcode);
				nb.save(view, agDecl.asSource.getAbsolutePath());
			} finally {
				nb.writeUnlock();
			}
		}
		return true;
	}
	
}
