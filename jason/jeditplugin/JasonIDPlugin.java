package jason.jeditplugin;


import org.gjt.sp.jedit.EditPlugin;

public class JasonIDPlugin extends EditPlugin {
	public static final String NAME = "jason";
    public static final String MENU = "jason.menu";
    public static final String PROPERTY_PREFIX = "plugin.jason.";
    public static final String OPTION_PREFIX   = "options.jason.";

    /*
    public void createMenuItems(Vector menuItems) {
        menuItems.addElement(GUIUtilities.loadMenu(MENU));
    }
    
    public void createOptionPanes(OptionsDialog od) {
		od.addOptionPane(new JasonOptionPanel());
    }
    */

}
