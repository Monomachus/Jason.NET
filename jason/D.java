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


package jason;

import jason.asSemantics.Intention;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Term;

public final class D {
    public static final Intention EmptyInt   = null;
    public static final String    SEmptyInt  = "TRUE";

    public static final byte      TEBel      = 0;
    public static final byte      TEAchvG    = 1;
    public static final byte      TETestG    = 2;
    public static final boolean   TEAdd      = false;
    public static final boolean   TEDel      = true;

    public static final boolean   LPos       = true;
    public static final boolean   LNeg       = false;
    public static final boolean   LDefPos    = true;
    public static final boolean   LDefNeg    = false;

    public static final Literal   LTrue      = new Literal(LPos, new Pred("true"));

    public static final byte      HAction    = 0;
    public static final byte      HAchieve   = 1;
    public static final byte      HTest      = 2;
    public static final byte      HAddBel    = 3;
    public static final byte      HDelBel    = 4;

    public static final byte      SStartRC   = 0;
    public static final byte      SProcMsg   = 0;
    public static final byte      SSelEv     = 1;
    public static final byte      SRelPl     = 2;
    public static final byte      SApplPl    = 3;
    public static final byte      SSelAppl   = 4;
    public static final byte      SAddIM     = 5;
    public static final byte      SProcAct   = 6;
    public static final byte      SSelInt    = 7;
    public static final byte      SExecInt   = 8;
    public static final byte      SClrInt    = 9;

    public static final String[]  SRuleNames = { "ProcMsg", "SelEv", "RelPl",
                                                 "ApplPl", "SelAppl", "AddIM",
                                                 "ProcAct", "SelInt", "ExecInt",
                                                 "ClrInt" };

	public static final Term TPercept = Term.parse("source(percept)");
	public static final Term TSelf = Term.parse("source(self)");
	public static final Term TAtomic = Term.parse("atomic");

    public static final byte      ODiscard        = 1;
    public static final byte      ORequeue        = 2;
    public static final byte      ORetrieve       = 3;
    public static final boolean   OSameFocus      = true;
    public static final boolean   ONewFocus       = false;
    public static final int       ODefaultVerbose = 1;
}
