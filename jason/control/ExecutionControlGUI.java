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
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.5  2005/08/12 20:45:36  jomifred
//   change informAgs method name
//

//----------------------------------------------------------------------------

package jason.control;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class ExecutionControlGUI extends ExecutionControl {

	String currentAg = "";
	boolean inRunMode = false;
	
	// xml components
	Transformer agTransformer = null;

	public ExecutionControlGUI() {
		initComponents();
	}

	// Inteface components
	JButton jBtStep = null;
	JButton jBtRun = null;

	JTextPane jTA = null;

	JList jList = null;

	MyListModel listModel;

	void initComponents() {
		JFrame frame = new JFrame("MAS Execution Control");

		jBtStep = new JButton("Step");
		jBtStep.setToolTipText("ask all agents to perform one reasoning cycle");
		jBtStep.setEnabled(false);
		jBtStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBtStep.setEnabled(false);
				fJasonControl.informAllAgsToPerformCycle();
			}
		});

		jBtRun = new JButton("Run");
		jBtRun.setToolTipText("Run the MAS until some agent achieve a breakpoint. Breakpoints are annotations in plans' label");
		jBtRun.setEnabled(false);
		jBtRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBtRun.setEnabled(false);
				inRunMode = true;
				fJasonControl.informAllAgsToPerformCycle();
			}
		});

		
		jTA = new JTextPane();
		jTA.setEditable(false);
		jTA.setContentType("text/html");
		jTA.setAutoscrolls(false);
		
		JScrollPane spTA = new JScrollPane(jTA);
		spTA.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Agent Inspection", TitledBorder.LEFT,
				TitledBorder.TOP));

		/*
		jConsole = new JTextArea(10, 40);
		jConsole.setEditable(false);
		JScrollPane spConsole = new JScrollPane(jConsole);
		spConsole.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Console", TitledBorder.LEFT,
				TitledBorder.TOP));
        */
		
		listModel = new MyListModel();
		jList = new JList(listModel);
		JScrollPane spList = new JScrollPane(jList);
		spList.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Agents", TitledBorder.LEFT,
				TitledBorder.TOP));
		jList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String ag = jList.getSelectedValue().toString();
				if (!ag.equals(currentAg)) {
					inspectAgent(ag);
					currentAg = ag;
				}
			}

		});

		JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pButtons.add(jBtStep);
		pButtons.add(jBtRun);

		JSplitPane splitPaneHor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneHor.setTopComponent(spList);
		splitPaneHor.setDividerLocation(100);
		splitPaneHor.setBottomComponent(spTA);
		splitPaneHor.setOneTouchExpandable(true);
		//splitPane.setPreferredSize(new Dimension(600, 300));

		//JSplitPane splitPaneVer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		//splitPaneVer.setTopComponent(splitPaneHor);
		//splitPaneVer.setBottomComponent(spConsole);
		//splitPaneVer.setOneTouchExpandable(true);

		frame.getContentPane().add(BorderLayout.SOUTH, pButtons);
		frame.getContentPane().add(BorderLayout.CENTER, splitPaneHor);//splitPaneVer);
		frame.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = (int)(screenSize.height * 0.618);
		frame.setBounds(80, 30, (int)(height*1.2), height);
		//splitPaneVer.setDividerLocation((int)(splitPaneVer.getHeight()*0.618));
		//splitPaneVer.setDividerLocation(height - 200);
		
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//close();
			}
		});
	}
	

	private void inspectAgent(String agName) {
		if (agName == null) {
			return;
		}
		if (agName.length() == 0) {
			return;
		}
		if (agTransformer == null) {
			try {
				agTransformer = TransformerFactory.newInstance().newTransformer(
						new StreamSource( ExecutionControlGUI.class.getResource("/xml/agInspection.xsl").openStream()));
			} catch (Exception e) {
				jTA.setText("Error initializing XML transformer");
				e.printStackTrace();
				return;
			}
		}
		
		Document agState = fJasonControl.getAgState(agName);

		StringWriter so = new StringWriter();
		try {
			agTransformer.transform(new DOMSource(agState),
					                new StreamResult(so));
			jTA.setText(so.toString());
		} catch (Exception e) {
			jTA.setText("Error in XML transformation!" + e + "\n");
			e.printStackTrace();
		}
		
	}

	/** 
	 * Called when the agent <i>agName</i> has finished its reasoning cycle.
	 * <i>breakpoint</i> is true in case the agent selected one plan with "breakpoint" 
	 * annotation. 
	  */
	public void receiveFinishedCycle(String agName, boolean breakpoint) {
		if (breakpoint) {
			inRunMode = false;
		}
		listModel.addAgent(agName);
		super.receiveFinishedCycle(agName, breakpoint);
	}


	/** called when all agents have finished the current cycle */
	protected void allAgsFinished() {
		if (inRunMode) {
			fJasonControl.informAllAgsToPerformCycle();
		} else {
			inspectAgent(currentAg);
			jBtStep.setEnabled(true);
			jBtRun.setEnabled(true);
		}
	}

	class MyListModel extends AbstractListModel {
		List agents = new ArrayList();

		public void addAgent(String agName) {
			if (!agents.contains(agName)) {
				agents.add(agName);
				Collections.sort(agents);
				fireContentsChanged(this, 0, agents.size()-1);
				//fireIntervalAdded(this, agents.size() - 1, agents.size());
			}
		}

		public Object getElementAt(int index) {
			return agents.get(index);
		}

		public int getSize() {
			return agents.size();
		}
	}
}