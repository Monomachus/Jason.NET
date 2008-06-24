// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

/**
 * Represents an unnamed variable '_'.
 * 
 * @author jomi
 */
public class UnnamedVar extends VarTerm {

	private static final long serialVersionUID = 1L;

	private static int varCont = 1;

    public UnnamedVar() {
        super(createNewName());
    }

    public UnnamedVar(String name) {
        super( name.length() == 1 ? "_" + (varCont++) : name);
    }
    
    public static String createNewName() {
    	return "_" + (varCont++);
    }

    public Object clone() {
        if (hasValue()) {
            return getValue().clone();
        } else {
            UnnamedVar newv = new UnnamedVar(getFunctor());
            if (hasAnnot())
                newv.addAnnots((ListTerm)this.getAnnots().clone());
        	return newv;
        }
    }

    @Override
    public boolean isUnnamedVar() {
        return !hasValue();
    }
}
