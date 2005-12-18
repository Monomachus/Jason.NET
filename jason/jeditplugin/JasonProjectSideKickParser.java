 package jason.jeditplugin;

import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.mas2j;

import java.io.StringReader;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.GUIUtilities;

import sidekick.Asset;
import sidekick.SideKickParsedData;
import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;

public class JasonProjectSideKickParser extends sidekick.SideKickParser {
	public static final String ID = "jason_parser";
	
	public JasonProjectSideKickParser() {
		super(ID);
	}
	
	public SideKickParsedData parse(Buffer buf, DefaultErrorSource errorSource) {
		String text;
		try	{
			buf.readLock();
			text = buf.getText(0,buf.getLength());
		} finally {
			buf.readUnlock();
		}

        try {
            	mas2j parser = new mas2j(new StringReader(text));
            	MAS2JProject project = parser.mas();
            	
            	// create nodes 
            	SideKickParsedData pd = new SideKickParsedData(buf.getName());
            	
            	pd.root.add(new ProjectAsset("Infrastructure: ",project.getInfrastructure(), buf, INFRA_TYPE).createTreeNode());
            	if (project.getEnvClass() != null) {
                	pd.root.add(new ProjectAsset("Environment: ",project.getEnvClass(), buf, ENV_TYPE).createTreeNode());
            	}
            	Iterator i = project.getAgents().iterator();
            	while (i.hasNext()) {
            		AgentParameters ap = (AgentParameters)i.next();
            	    pd.root.add(new ProjectAsset("", ap.name, buf, AG_TYPE).createTreeNode());
            	}
        		return pd;
        		
        } catch (jason.mas2j.parser.ParseException ex) {
        	if (ex.currentToken != null && ex.currentToken.next != null && errorSource != null) {
        		errorSource.addError(new DefaultErrorSource.DefaultError(
        				errorSource, 
        				ErrorSource.ERROR, 
        				buf.getPath(),
        				ex.currentToken.next.beginLine-1, 0, 0,
        		    	ex.toString()));
        	}	
        } catch (Exception e) {
        	e.printStackTrace();
        }
    	return null;		
	}
	
	public String toString() {
		return ID;
	}

	private static Icon AG_ICON;
	private static Icon INFRA_ICON;
	private static Icon ENV_ICON;

	private static final int AG_TYPE = 1;
	private static final int INFRA_TYPE = 2;
	private static final int ENV_TYPE = 3;
	
	class ProjectAsset extends Asset {

		
		int type = 1;
		
		public ProjectAsset(String pre, String vl, Buffer buf, int type) {
			super(pre+vl);
			this.start = toPos(buf, vl);
			this.end = toPos(buf, vl);
			this.type = type;
		}

		public Icon getIcon() {
			switch (type) {
			case INFRA_TYPE:
				if (INFRA_ICON == null) {
					INFRA_ICON = GUIUtilities.loadIcon("normal.gif");
				}
				return INFRA_ICON;
			case AG_TYPE:
				if (AG_ICON == null) {
					AG_ICON = GUIUtilities.loadIcon("Plus.png");
				}
				return AG_ICON;
			case ENV_TYPE:
				if (ENV_ICON == null) {
					ENV_ICON = GUIUtilities.loadIcon("arrow1.png");
				}
				return ENV_ICON;
			}
			return null;
		}

		public String getShortString() {
			return name;
		}

		public String getLongString() {
			return name;
		}

		private Position toPos(Buffer buffer, String p) {
			String text = buffer.getText(0, buffer.getLength());
			int pos = text.indexOf(p);
			if (pos >= 0) {
				return buffer.createPosition(pos);				
			} else {
				return buffer.createPosition(0);				
			}
		}

		public DefaultMutableTreeNode createTreeNode() {
			return new DefaultMutableTreeNode(this, true);
		}
	}
}
