package jason.jeditplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CheckVersion extends Thread {

    public static final String JasonSite = "http://jason.sf.net";

    private String download = null;
    
    String getLatestVersion() {
        // read version from Jason site
        try {
            Properties p = new Properties();
            p.load(new URL(JasonSite+"/latest.properties").openStream());
            download = p.getProperty("download");
            return p.getProperty("version") + "." + p.getProperty("release");
        } catch (Exception ex) {
            return null;
        }
    }

    boolean isLatestVersion() {
        // get user version
        String cVersion = Config.get().getJasonRunningVersion();
        return cVersion.equals(getLatestVersion());
    }

    String whatsNew() {
        try {
            StringBuffer out = new StringBuffer();
            InputStream in = new URL(JasonSite+"/whatsnew.txt").openStream();
            int c = in.read();
            while (c != -1) {
                out.append((char) c);
                c = in.read();
            }
            return out.toString();
        } catch (Exception e) {
            System.err.println("Error reading what's new!" + e);
            return null;
        }
    }

    void show() {
        final JFrame f = new JFrame("Jason update");
        f.setLayout(new BorderLayout());

        JTextArea ta = new JTextArea(20, 50);
        f.add(BorderLayout.CENTER,new JScrollPane(ta));
        
        JPanel sp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        //JButton notShow = new JButton("Do not show this message again.");
        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                f.setVisible(false);            }
        });
        JButton notShow = new JButton("Do not show this message again");
        notShow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                f.setVisible(false);
                Config.get().setProperty(Config.CHECK_VERSION,"false");
                Config.get().store();
            }
        });
        sp.add(ok);
        sp.add(notShow);
        f.add(BorderLayout.SOUTH, sp);
        f.pack();
        
        ta.append("A new version of Jason (" + getLatestVersion() + ") is available at\n\n");
        ta.append("         "+JasonSite+"\n\n");
        String wn = whatsNew();
        if (wn != null) {
            ta.append("Release notes:\n");
            ta.append(wn);
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation((int)( (screenSize.width - f.getWidth()) / 2),(int) ((screenSize.height - f.getHeight())/2));
        f.setVisible(true);
    }

    public void run() {
        if (Config.get().getProperty(Config.CHECK_VERSION,"true").equals("true")) {
            if (!isLatestVersion()) {
                show();
            }
        }
    }

    public static void main(String[] a) {
        CheckVersion cv = new CheckVersion();
        cv.start();
    }
}