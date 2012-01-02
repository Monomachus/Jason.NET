using System;
using System.Runtime.Serialization;
using Jason.Utils;

namespace Jason.asSyntax
{
	public interface Term : ICloneable, IComparable<Term>, ISerializable, ToDOM {

		 bool isVar();
		 bool isUnnamedVar();
	 	 bool isLiteral();
		 bool isRule();
		 bool isList();
		 bool isString();
		 bool isInternalAction();
		 bool isArithExpr();
		 bool isNumeric();
		 bool isPred();
		 bool isGround();
		 bool isStructure();
		 bool isAtom();
		 bool isPlanBody();

		 /// TODO : add VarTerm 
//		 bool hasVar(VarTerm t);
//		 void countVars(Map<VarTerm, Integer> c);

		 Term clone();

		 bool equals(Object o);
		
		/** replaces variables by their values in the unifier, returns true if some variable was applied */
		/// TODO : add Unifier 
//		 bool apply(Unifier u);

		 void setSrcInfo(SourceInfo s);
		 SourceInfo getSrcInfo();
	}

}
