 package jason.jeditplugin;

import jason.asSemantics.Agent;
import jason.asSyntax.Plan;

import java.io.StringReader;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;

import org.gjt.sp.jedit.Buffer;

import sidekick.Asset;
import sidekick.SideKickParsedData;
import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;

public class AgentSpeakSideKickParser extends sidekick.SideKickParser {
	public static final String ID = "as_parser";
	
	public AgentSpeakSideKickParser() {
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
            	jason.asSyntax.parser.as2j parser = new jason.asSyntax.parser.as2j(new StringReader(text));
            	
            	Agent ag = new Agent();
            	parser.agent(ag);
            	
            	// create nodes 
            	SideKickParsedData pd = new SideKickParsedData(buf.getName());
            	Iterator i = ag.getPL().getPlans().iterator();
            	while (i.hasNext()) {
            		Plan p = (Plan)i.next();
            		DefaultMutableTreeNode node = new PlanAsset(p, buf).createTreeNode();
            	    pd.root.add(node);
            	}          
        		return pd;
        		
        } catch (jason.asSyntax.parser.ParseException ex) {
        	addError(ex, errorSource, buf.getPath());
        } catch (Exception e) {
        	e.printStackTrace();
        }
    	return null;		
	}

	public static void addError(jason.asSyntax.parser.ParseException ex, DefaultErrorSource errorSource, String path) {
    	if (ex.currentToken != null && ex.currentToken.next != null && errorSource != null) {
    		int line = ex.currentToken.next.beginLine-1;
    		if (line < 0) line = 0;
    		errorSource.addError(new DefaultErrorSource.DefaultError(
    				errorSource, 
    				ErrorSource.ERROR, 
    				path,
    				line, 0, 0,
    		    	ex.toString()));
    	}		
	}
	
	public String toString() {
		return ID;
	}

	private static Icon PLAN_ICON;

	class PlanAsset extends Asset {

		private Plan plan;

		public PlanAsset(Plan p, Buffer buf) {
			//super(((p.getLabel() == null) ? "" : "@" + p.getLabel() + " ") 	+ p.getTriggerEvent());
			super(p.getTriggerEvent().toString());
			this.plan = p;
			this.start = toPos(buf, p.getStartSourceLine());
			this.end = toPos(buf, p.getEndSourceLine());
		}

		public Icon getIcon() {
			if (PLAN_ICON == null) {
				PLAN_ICON = new ImageIcon(AgentSpeakSideKickParser.class
						.getResource("/images/plan.png"));
			}
			return PLAN_ICON;
		}

		public String getShortString() {
			return name;
		}

		public String getLongString() {
			return plan.toASString();
		}

		private Position toPos(Buffer buffer, int line) {
			if ((line - 1) > buffer.getLineCount()) {
				return buffer.createPosition(buffer.getLength() - 1);
			}
			int offset = buffer.getLineStartOffset(line - 1);
			if (offset >= buffer.getLength()) {
				return buffer.createPosition(buffer.getLength() - 1);
			}
			return buffer.createPosition(offset);
		}

		public DefaultMutableTreeNode createTreeNode() {
			return new DefaultMutableTreeNode(this, true);
		}
	}
}
