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
//----------------------------------------------------------------------------

package jason.control;

import jason.infra.centralised.RunCentralisedMAS;
import jason.util.asl2html;
import jason.util.asl2tex;
import jason.util.asl2xml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Document;

public class ExecutionControlGUI extends ExecutionControl {

	String currentAg = "";
	boolean inRunMode = false;
	
	// xml components
    asl2xml  agTransformerXML = new asl2xml();
    asl2tex  agTransformerTex = new asl2tex("/xml/ag2tex.xsl");
    asl2html agTransformerHtml = new asl2html("/xml/agInspection.xsl");
    asl2xml  agTransformer = agTransformerHtml;
     

	public ExecutionControlGUI() {
		initComponents();
	}

	// Inteface components
	JFrame  frame;
	JButton jBtStep = null;
	JButton jBtRun = null;
    JComboBox jCbViewAs = null;

	JTextPane jTA = null;

	JList jList = null;
	JPanel spList;
	
	DefaultListModel listModel;
    
    // what to show
    Document agState = null;

    Map<String,Boolean> show = new HashMap<String,Boolean>();

	void initComponents() {
		frame = new JFrame("MAS Execution Control");

		jBtStep = new JButton("Step", new ImageIcon(ExecutionControlGUI.class.getResource("/images/resume_co.gif")));
		jBtStep.setToolTipText("ask all agents to perform one reasoning cycle");
		jBtStep.setEnabled(false);
		jBtStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBtStep.setEnabled(false);
				infraControl.informAllAgsToPerformCycle();
			}
		});

		jBtRun = new JButton("Run", new ImageIcon(ExecutionControlGUI.class.getResource("/images/run.gif")));
		jBtRun.setToolTipText("Run the MAS until some agent achieve a breakpoint. Breakpoints are annotations in plans' label");
		jBtRun.setEnabled(false);
		jBtRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBtRun.setEnabled(false);
				inRunMode = true;
				infraControl.informAllAgsToPerformCycle();
				if (RunCentralisedMAS.getRunner() != null && RunCentralisedMAS.getRunner().btDebug != null) {
					RunCentralisedMAS.getRunner().btDebug.setEnabled(true);
				}
			}
		});

        jCbViewAs = new JComboBox();
        jCbViewAs.addItem("html");
        jCbViewAs.addItem("xml");
        jCbViewAs.addItem("LaTeX");
        jCbViewAs.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ievt) {
                String mime = jCbViewAs.getSelectedItem().toString();
                if (mime.equals("html")) {
                    agTransformer = agTransformerHtml;
                } else if (mime.equals("xml")) {
                    agTransformer = agTransformerXML;
                } else if (mime.equals("LaTeX")) {
                    mime = "xml";
                    agTransformer = agTransformerTex;
                }
                jTA.setContentType("text/"+mime);
                previousMind = "--";
                showAgState();
            }            
        });

		jTA = new JTextPane();
		jTA.setEditable(false);
		jTA.setContentType("text/html");
		jTA.setAutoscrolls(false);
        jTA.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                hyperLink(evt);
            }
        });
        jTA.setText("<html><body>Select the agent to inspect.</body></html>");
		
		JPanel spTA = new JPanel(new BorderLayout());
		spTA.add(BorderLayout.CENTER, new JScrollPane(jTA));
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
		
		listModel = new DefaultListModel();//MyListModel();
		jList = new JList(listModel);
		spList = new JPanel(new BorderLayout());
		spList.add(BorderLayout.CENTER, new JScrollPane(jList));
		spList.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Agents", TitledBorder.LEFT,
				TitledBorder.TOP));
		jList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String ag = jList.getSelectedValue().toString();
				if (!ag.equals(currentAg)) {
                    currentAg = ag;
					inspectAgent(ag);
				}
			}

		});

		JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pButtons.add(jBtStep);
		pButtons.add(jBtRun);
        pButtons.add(new JLabel("           View as:"));
        pButtons.add(jCbViewAs);

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
        
        show.put("bels", true);
        show.put("evt", true);
        show.put("mb", false);
        show.put("int", false);
        show.put("plan", false);
	}
	
	public void stop() {
		super.stop();
		frame.dispose();
		frame = null;
	}

	public void setRunMode(boolean b) {
		inRunMode = b;
	}
	
	private void inspectAgent(String agName) {
		if (agName == null) {
			return;
		}
		if (agName.length() == 0) {
			return;
		}

		try {
			agState = infraControl.getAgState(agName);
            showAgState();
		} catch (Exception e) {
			jTA.setText("can not get the state of agent "+agName);
            logger.log(Level.SEVERE,"Error:",e);
		}
		
	}

    private String previousMind = "--";
    /** show current agent state */
    void showAgState() {
        if (agState != null) {
            try {
                // set parameters
                if (jCbViewAs.getSelectedItem().toString().equals("html")) {
                    // as HTML
                    for (String p: show.keySet()) {
                        agTransformer.getTransformer().setParameter("show-"+p, show.get(p)+"");
                    }
                }
                String sMind = agTransformer.transform(agState);

                if (!sMind.equals(previousMind)) {
                    jTA.setText(sMind);
                }
                previousMind = sMind;
            } catch (Exception e) {
                jTA.setText("Error in XML transformation!" + e + "\n");
                e.printStackTrace();
            }
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
		if (!listModel.contains(agName)) {
			logger.fine("New agent "+agName);
			listModel.addElement(agName);
		}
		super.receiveFinishedCycle(agName, breakpoint);
	}


	/** called when all agents have finished the current cycle */
	protected void allAgsFinished() {
		if (inRunMode) {
			infraControl.informAllAgsToPerformCycle();
		} else {
			inspectAgent(currentAg);
			jBtStep.setEnabled(true);
			jBtRun.setEnabled(true);
		}
	}
    
    private void hyperLink(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            //System.out.println("*evt="+evt.getDescription());
            String uri = "show?";
            int pos = evt.getDescription().indexOf(uri);
            if (pos >= 0) {
                String par = evt.getDescription().substring(pos+uri.length());
                show.put(par,true);
            } else {
                uri = "hide?";
                pos = evt.getDescription().indexOf(uri);
                if (pos >= 0) {
                    String par = evt.getDescription().substring(pos+uri.length());
                    show.put(par,false);
                }
            }
            showAgState();
        }
    }
}