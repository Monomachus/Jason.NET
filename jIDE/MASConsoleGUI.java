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
//   Revision 1.11  2005/11/16 18:35:25  jomifred
//   fixed the print(int) on console bug
//
//   Revision 1.10  2005/09/20 16:59:14  jomifred
//   do not use MASConsole when the logger in Console (and so, do not need an X11)
//
//   Revision 1.9  2005/09/04 17:03:23  jomifred
//   using dispose instead of setVisible(false)
//
//   Revision 1.8  2005/08/12 21:08:23  jomifred
//   add cvs keywords
//
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
public class MASConsoleGUI  {
    
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
    
    JFrame frame = null;
    JTextArea output;
    JPanel    pBt = null;
    PrintStream originalOut = null;
    PrintStream originalErr = null;

    private boolean inPause = false; 
    
    private MASConsoleGUI(String title, final RunMAS runMAS) {
    	frame = new JFrame(title);
    	frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (runMAS != null) {
                    runMAS.stopMAS();
                }
                close();
            }
        });
        
        output = new JTextArea();
        output.setEditable(false);

		pBt = new JPanel();
		pBt.setLayout(new FlowLayout(FlowLayout.CENTER));

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(BorderLayout.CENTER, new JScrollPane(output));
        frame.getContentPane().add(BorderLayout.SOUTH, pBt);

        JButton btClean = new JButton("Clean");
        btClean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                output.setText("");
            }
        });
        
        addButton(btClean);
        
        frame.setBounds(250, 10, 700, 500);
    }
    
    public void setTitle(String s) {
    	frame.setTitle(s);
    }
    
    public void addButton(JButton jb) {
    	pBt.add(jb);
    	pBt.revalidate();
    	//pack();
    }
    
    synchronized public void setPause(boolean b) {
    	inPause = b;
    	notifyAll();
    }
    
    synchronized void waitNotPause() {
    	try {
    		while (inPause) {
    			wait();
    		}
    	} catch (Exception e) {}
    }
    
    public boolean isPause() {
    	return inPause;
    }
  
	public void append(String s) {
		if (!frame.isVisible()) {
			frame.setVisible(true);
		}
		if (inPause) {
			waitNotPause();
		}
		int l = output.getDocument().getLength();
		if (l > 30000) {
			output.setText("");
			//l = output.getDocument().getLength();
		}
		output.append(s);
		//output.setCaretPosition(l);
    }
    
    public void close() {
    	if (masConsole == null) return;
    	
        masConsole.frame.dispose();
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
        public void print(Object s) {
			append(s.toString());
        }
        public void println(Object s) {
            append(s+"\n");
        }
        public void print(boolean arg) {
        	append(arg+"");
		}
		public void print(char arg0) {
			append(arg0+"");
		}
		public void print(double arg0) {
			append(arg0+"");
		}
		public void print(float arg0) {
			append(arg0+"");
		}
		public void print(int arg0) {
			append(arg0+"");
		}
		public void print(long arg0) {
			append(arg0+"");
		}
		public void println(boolean arg0) {
			append(arg0+"\n");
		}
		public void println(char arg0) {
			append(arg0+"\n");
		}
		public void println(double arg0) {
			append(arg0+"\n");
		}
		public void println(float arg0) {
			append(arg0+"\n");
		}
		public void println(int arg0) {
			append(arg0+"\n");
		}
		public void println(long arg0) {
			append(arg0+"\n");
		}
		public void println() {
            append("\n");
        }
    }
    
}
