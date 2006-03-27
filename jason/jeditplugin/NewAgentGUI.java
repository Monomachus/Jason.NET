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
	
	public NewAgentGUI(String title, Buffer b, View view) {
		super(view, title);
		buffer = b;
		this.view = view;
	}
	

	protected boolean ok() {
		AgentParameters agDecl = getAgDecl();
		if  (agDecl == null) {
			JOptionPane.showMessageDialog(this, "An agent name must be informed.");
			return false;
		}
		if (agDecl.asSource != null && !agDecl.asSource.exists()) {
			JOptionPane.showMessageDialog(this, "The source file does not exist");
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
			String agcode = "// "+agName.getText().trim() + " in project "+buffer.getName()+
							"\ndemo.\n+demo : true <- .print(\"hello world.\").";
			nb.insert(0, agcode);
			nb.save(view, agFile);
		} finally {
			nb.writeUnlock();
		}
		
		agDecl.asSource = new File(agFile);
		return true;
	}
	
}
