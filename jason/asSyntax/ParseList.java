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


package jason.asSyntax;

import jason.D;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ParseList {

	protected Term list;

    public ParseList(String sList) {
        as2j parser = new as2j(new StringReader(sList));
        try {
            list = parser.list();
        } catch (Exception e) {
            System.err.println("Error parsing list "+sList);
            e.printStackTrace();
        }
    }
    public ParseList(List l) {
        set(l);
    }
    public ParseList(Term t) {
        set(t);
    }
    
    public void set(Term t) {
        list = t;
    }
    
    public void set(List l) {
        if (l==null) {
            list = null;
        }
        else if (l.isEmpty()) {
            list = new Term(D.EmptyList);
        }
        else {
            Iterator i = l.iterator();
            Term t = new Term(D.ListCons);
            list = t;
            t.terms.add((Term)i.next());
            while ( i.hasNext() ) {
                Term u = new Term(D.ListCons);
                u.terms.add((Term)i.next());
                t.terms.add(u);
                t = u;
            }
            t.terms.add(new Term(D.EmptyList));
        }
    }

	public Term getList() {
		return list;
	}

    
    public List getAsList() {
        if (list==null)
            return null;
        List l = new ArrayList();
        Term t = list;
        while (!t.funcSymb.equals(D.EmptyList)) {
            l.add(t.terms.get(0));
            t = (Term) t.terms.get(1);
        }
        return l;
    }
    
    public String toString() {
        return list.toString();
    }
}
