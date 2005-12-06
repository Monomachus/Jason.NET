package jason.jeditplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.PropertiesChanged;

public class JasonID extends JPanel implements EBComponent {
	private View view;
	private boolean floating;

	private JTextArea textArea;
    //private JasonIDToolPanel toolPanel;
    
	public JasonID(View view, String position) {
		super(new BorderLayout());

		this.view = view;
		this.floating = position.equals(DockableWindowManager.FLOATING);

		// TODO: add tool bar (see console example)
		// increase text area (see console example)
		
		/*
		 this.filename = jEdit.getProperty(JasonIDPluginQuickNotepadPlugin.OPTION_PREFIX	            + "filepath");
		 if(this.filename == null || this.filename.length() == 0)
		 {
		 this.filename = new String(jEdit.getSettingsDirectory()
		 + File.separator + "qn.txt");
		 jEdit.setProperty(QuickNotepadPlugin.OPTION_PREFIX
		 + "filepath",this.filename);
		 }
		 this.defaultFilename = new String(this.filename);
		 */

		//this.toolPanel = new JasonIdToolPanel(this);
		
		//add(BorderLayout.NORTH, this.toolPanel);

		if (floating)
			this.setPreferredSize(new Dimension(500, 250));

		textArea = new JTextArea(40,20);
		textArea.setText("Jjjjj");
		//textArea.setFont(QuickNotepadOptionPane.makeFont());
		//textArea.addKeyListener(new KeyHandler());
		//textArea.addAncestorListener(new AncestorHandler());
		JScrollPane pane = new JScrollPane(textArea);
		add(BorderLayout.CENTER, pane);

		//readFile();
	}

    public void handleMessage(EBMessage message) {
        if (message instanceof PropertiesChanged) {
            propertiesChanged();
        }
    }


    private void propertiesChanged() {
    }

	public void addNotify() {
		super.addNotify();
		EditBus.addToBus(this);
	} 
	
	public void removeNotify() {
        // saveFile();
        super.removeNotify();
        EditBus.removeFromBus(this);
    }
	
	public void runMAS() {
		textArea.setText("running");
	}
	public void debugMAS() {
		textArea.setText("debugging");
	}
	public void stopMAS() {
		textArea.setText("Stop!");
	}
}