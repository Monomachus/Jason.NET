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

import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.TermImpl;
import jason.asSyntax.Trigger;

import java.io.Serializable;
import java.util.ListIterator;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Intention implements Serializable {

    public static final Intention EmptyInt = null;

    private static int idCount = 1;

    private int id;

    private boolean isAtomic = false;

    Stack<IntendedMeans> fIntendedMeans = new Stack<IntendedMeans>();

    // static private Logger logger = Logger.getLogger(intend.class.getName());

    public Intention() {
        id = idCount++;
    }

    public int getId() {
        return id;
    }

    public void push(IntendedMeans im) {
        fIntendedMeans.push(im);
        if (im.isAtomic()) {
            isAtomic = true;
        }
    }

    public IntendedMeans peek() {
        return fIntendedMeans.peek();
    }

    public IntendedMeans get(int index) {
        return fIntendedMeans.get(index);
    }

    public IntendedMeans pop() {
        IntendedMeans top = fIntendedMeans.pop();

        isAtomic = false;
        for (IntendedMeans im : fIntendedMeans) {
            if (im.isAtomic()) {
                isAtomic = true;
                break;
            }
        }
        return top;
    }

    public ListIterator<IntendedMeans> iterator() {
        return fIntendedMeans.listIterator(fIntendedMeans.size());
    }

    public boolean isFinished() {
        return fIntendedMeans.size() == 0;
    }

    public int size() {
        return fIntendedMeans.size();
    }

    boolean isAtomic() {
        return isAtomic;
    }
    

    public boolean hasTrigger(Trigger g, Unifier u) {
        for (IntendedMeans im : fIntendedMeans) {
            Trigger it = (Trigger) im.getPlan().getTriggerEvent().clone();
            im.unif.apply(it.getLiteral());
            if (u.unifies(g, it)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        ListIterator<IntendedMeans> i = fIntendedMeans.listIterator(fIntendedMeans.size());
        while (i.hasPrevious()) {
            s.append("    " + i.previous() + "\n");
        }
        return s.toString();
    }

    public Term getAsTerm() {
        Term intention = new TermImpl("intention");
        ListTerm lt = new ListTermImpl();
        ListIterator<IntendedMeans> i = fIntendedMeans.listIterator(fIntendedMeans.size());
        while (i.hasPrevious()) {
            lt.add(i.previous().getAsTerm());            
        }
        intention.addTerm(lt);
        return intention;        
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element eint = (Element) document.createElement("intention");
        eint.setAttribute("id", id + "");
        for (int i = fIntendedMeans.size() - 1; i >= 0; i--) {
            IntendedMeans im = (IntendedMeans) fIntendedMeans.get(i);
            eint.appendChild(im.getAsDOM(document));
        }
        return eint;
    }
}
