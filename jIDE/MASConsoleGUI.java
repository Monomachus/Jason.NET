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
// http://www.csc.liv.ac.uk/~bordini
// http://www.inf.furb.br/~jomi
//----------------------------------------------------------------------------

package jIDE;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** runs a MAS */
public class MASConsoleGUI extends JFrame  {
    
    private static MASConsoleGUI masConsole = null;
    
    /** for sigleton pattern */
    public static MASConsoleGUI get(String title, RunMAS runMAS) {
        if (masConsole == null) {
            masConsole = new MASConsoleGUI(title, runMAS);
        }
        return masConsole;
    }

    /** for sigleton pattern */
    public static MASConsoleGUI get() {
        if (masConsole == null) {
            masConsole = new MASConsoleGUI("MAS Console", null);
        }
        return masConsole;
    }
    
    public static boolean hasConsole() {
        return masConsole != null;
    }
    
    JTextArea output;
    PrintStream originalOut = null;
    PrintStream originalErr = null;
    
    private MASConsoleGUI(String title, final RunMAS runMAS) {
    	super(title);
    	addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (runMAS != null) {
                    runMAS.stopMAS();
                }
                close();
            }
        });
        
        output = new JTextArea();
        output.setEditable(false);
        JButton btClean = new JButton("Clean");
        btClean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                output.setText("");
            }
        });
        
        JPanel pBt = new JPanel();
        pBt.setLayout(new FlowLayout(FlowLayout.CENTER));
        pBt.add(btClean);
        
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(output));
        getContentPane().add(BorderLayout.SOUTH, pBt);
        pack();
        setBounds(250, 10, 700, 500);
        setVisible(true);
    }
    
  
	public void append(String s) {
		int l = output.getDocument().getLength();
		if (l > 10000) {
			output.setText("");
			l = output.getDocument().getLength();
		}
		output.append(s);
		output.setCaretPosition(l);
    }
    
    public void close() {
    	if (masConsole == null) return;
    	
        masConsole.setVisible(false);
        if (masConsole.originalOut != null) {
            System.setOut(masConsole.originalOut);
        }
        if (masConsole.originalErr != null) {
            System.setOut(masConsole.originalErr);
        }
        masConsole = null;
    }
    
    public void setAsDefaultOut() {
        MyOutputStream out = new MyOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(out);
        System.setErr(out);
    }
    
    class MyOutputStream extends PrintStream {
        MyOutputStream() {
            super(System.out);
        }
        public void print(String s) {
			append(s);
        }
        public void println(String s) {
            append(s+"\n");
        }
        public void println() {
            append("\n");
        }
    }
    
}
