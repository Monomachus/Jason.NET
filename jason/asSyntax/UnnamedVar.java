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
    // static private Logger logger =
    // Logger.getLogger(UnnamedVar.class.getName());
    private static int varCont = 1;

    public UnnamedVar() {
        super();
        setFunctor("_" + (varCont++));
    }

    private UnnamedVar(String name) {
        super();
        setFunctor(name);
    }

    public Object clone() {
        UnnamedVar v = new UnnamedVar(getFunctor());
        if (hasValue()) {
            v.setValue((Term) getValue().clone());
        }
        return v;
    }

    /**
     * overridden VarTerm setValue, this method does nothing, so the Var never
     * has value
     */
    // @Override public boolean setValue(Term vl) {
    // return true;
    // }
    
    @Override
    public boolean isUnnamedVar() {
        return !hasValue();
    }

    // public String toString() {
    // return "_";
    // }
}
