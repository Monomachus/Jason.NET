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


package jason.asSemantics;


public class Message {
    
    private String ilForce  = null;
    private String sender   = null;
    private String receiver = null;
    private Object propCont = null;
    private String msgId    = null;
    private String inReplyTo = null;
    
    private static int idCount = 1;
    
    public final static String[] knownPerformatives = {"tell","untell","achieve","unachieve","askOne","askAll","tellHow", "untellHow","askHow"};
    
    public Message() {
    }

    public Message(String ilf, String s, String r, Object c) {
    	this(ilf, s, r, c, "mid"+(idCount++));
    }
    
    public Message(String ilf, String s, String r, Object c, String id) {
        ilForce  = ilf;
        sender   = s;
        receiver = r;
        propCont = c;
        msgId    = id;
    }

    
    public Message(Message m) {
        ilForce  = m.ilForce;
        sender   = m.sender;
        receiver = m.receiver;
        propCont = m.propCont;
        msgId    = m.msgId;
        inReplyTo= m.inReplyTo;
    }
    
	public String getIlForce() {
		return ilForce;
	}
	
	public boolean isAsk() {
		return ilForce.startsWith("ask");
	}
	public boolean isTell() {
		return ilForce.equals("tell");
	}
	public boolean isUnTell() {
		return ilForce.equals("untell");
	}

	public boolean isKnownPerformative() {
	    for (String s: knownPerformatives) {
	        if (ilForce.equals(s)) 
	            return true;
	    }
	    return false;
	}
	
	public void setPropCont(Object o) {
		propCont = o;
	}
	public Object getPropCont() {
		return propCont;
	}
	
	public String getReceiver() {
		return receiver;
	}
	public void setSender(String agName) {
		sender = agName;
	}
	public String getSender() {
		return sender;
	}
	public void setReceiver(String agName) {
		receiver = agName;
	}
	
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String id) {
		msgId = id;
	}
	
	public String getInReplyTo() {
		return inReplyTo;
	}
	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}
	
	public Message copy() {
		return new Message(this);
	}
    public Object clone() {
        return new Message(this);
    }
	
	public String toString() {
		String irt = (inReplyTo == null ? "" : "->"+inReplyTo);
        return "<"+msgId+irt+","+sender+","+ilForce+","+receiver+","+propCont+">";
    }
}
