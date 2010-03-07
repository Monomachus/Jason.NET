package jason.jeditplugin;

import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class AboutGUI {

    // TODO: put the copyright of the image bellow it.

    public static void show(JFrame parent) {
        String version = "";
        String build = "";
    
        try {
            Properties p = new Properties();
            p.load(JasonID.class.getResource("/dist.properties").openStream());
            version = "Jason " + p.get("version") + "." + p.get("release");
            build = " build on " + p.get("build.date") + "\n\n";
        } catch (Exception ex) { }
    
        JOptionPane.showMessageDialog(parent,
        version +  build+
        "Copyright (C) 2003-2010  Jomi F. Hubner & Rafael H. Bordini.\n\n"+
        "This library is free software; you can redistribute it and/or\n"+
        "modify it under the terms of the GNU Lesser General Public\n"+
        "License as published by the Free Software Foundation; either\n"+
        "version 2.1 of the License, or (at your option) any later version.\n\n"+
        "This library is distributed in the hope that it will be useful,\n"+
        "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
        "GNU Lesser General Public License for more details.\n\n"+
        "You should have received a copy of the GNU Lesser General Public\n"+
        "License along with this library; if not, write to the Free Software\n"+
        "Foundation, Inc., 59 Temple Place, Suite 330,\nBoston, MA  02111-1307  USA\n\n"+
        "About the image: \"Jason\" by Gustave Moreau (1865).\n"+
        "Copyright Photo RMN (Agence Photographique de la Reunion des\n"+
        "Musees Nationaux, France). Photograph by Herve Lewandowski.\n\n"+
        "To contact the authors:\n"+
        "http://www.inf.ufrgs.br/~bordini\n"+
        "http://www.das.ufsc.br/~jomi",
        "JasonID - About",
        JOptionPane.INFORMATION_MESSAGE,
        new ImageIcon(JasonID.class.getResource("/images/Jason-GMoreau-Small.jpg")));
    }
}
