/* Generated By:JavaCC: Do not edit this line. as2jTokenManager.java */
package jason.asSyntax.parser;
import java.util.*;
import java.io.*;
import java.util.logging.*;
import jason.*;
import jason.asSemantics.*;
import jason.bb.*;
import jason.asSyntax.*;
import jason.asSyntax.directives.*;
import jason.asSyntax.ArithExpr.ArithmeticOp;
import jason.asSyntax.LogExpr.LogicalOp;
import jason.asSyntax.RelExpr.RelationalOp;
import jason.asSyntax.BodyLiteral.BodyType;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
import jason.jeditplugin.*;

public class as2jTokenManager implements as2jConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x7700L) != 0L)
         {
            jjmatchedKind = 20;
            return 46;
         }
         if ((active0 & 0x80000000000000L) != 0L)
            return 17;
         if ((active0 & 0x10000000L) != 0L)
            return 47;
         if ((active0 & 0x8000L) != 0L)
         {
            jjmatchedKind = 20;
            return 1;
         }
         return -1;
      case 1:
         if ((active0 & 0xf700L) != 0L)
         {
            jjmatchedKind = 20;
            jjmatchedPos = 1;
            return 46;
         }
         return -1;
      case 2:
         if ((active0 & 0xb400L) != 0L)
            return 46;
         if ((active0 & 0x4300L) != 0L)
         {
            jjmatchedKind = 20;
            jjmatchedPos = 2;
            return 46;
         }
         return -1;
      case 3:
         if ((active0 & 0x100L) != 0L)
            return 46;
         if ((active0 & 0x4200L) != 0L)
         {
            jjmatchedKind = 20;
            jjmatchedPos = 3;
            return 46;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 29;
         return jjMoveStringLiteralDfa1_0(0x4000000000L);
      case 38:
         return jjStopAtPos(0, 45);
      case 40:
         return jjStopAtPos(0, 39);
      case 41:
         return jjStopAtPos(0, 40);
      case 42:
         jjmatchedKind = 54;
         return jjMoveStringLiteralDfa1_0(0x100000000000000L);
      case 43:
         return jjStopAtPos(0, 34);
      case 44:
         return jjStopAtPos(0, 41);
      case 45:
         return jjStopAtPos(0, 35);
      case 46:
         return jjStartNfaWithStates_0(0, 28, 47);
      case 47:
         return jjStartNfaWithStates_0(0, 55, 17);
      case 58:
         jjmatchedKind = 32;
         return jjMoveStringLiteralDfa1_0(0x8000000L);
      case 59:
         return jjStopAtPos(0, 37);
      case 60:
         jjmatchedKind = 46;
         return jjMoveStringLiteralDfa1_0(0x800200000000L);
      case 61:
         jjmatchedKind = 52;
         return jjMoveStringLiteralDfa1_0(0x24000000000000L);
      case 62:
         jjmatchedKind = 48;
         return jjMoveStringLiteralDfa1_0(0x2000000000000L);
      case 63:
         return jjStopAtPos(0, 36);
      case 64:
         return jjStopAtPos(0, 16);
      case 91:
         return jjStopAtPos(0, 42);
      case 92:
         return jjMoveStringLiteralDfa1_0(0x8000000000000L);
      case 93:
         return jjStopAtPos(0, 44);
      case 98:
         return jjMoveStringLiteralDfa1_0(0x4000L);
      case 100:
         return jjMoveStringLiteralDfa1_0(0x1000L);
      case 101:
         return jjMoveStringLiteralDfa1_0(0x8000L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x200L);
      case 109:
         return jjMoveStringLiteralDfa1_0(0x2000L);
      case 110:
         return jjMoveStringLiteralDfa1_0(0x400L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x100L);
      case 123:
         return jjStopAtPos(0, 30);
      case 124:
         return jjStopAtPos(0, 43);
      case 125:
         return jjStopAtPos(0, 31);
      case 126:
         return jjStopAtPos(0, 11);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 33:
         if ((active0 & 0x4000000000L) != 0L)
            return jjStopAtPos(1, 38);
         break;
      case 42:
         if ((active0 & 0x100000000000000L) != 0L)
            return jjStopAtPos(1, 56);
         break;
      case 45:
         if ((active0 & 0x8000000L) != 0L)
            return jjStopAtPos(1, 27);
         else if ((active0 & 0x200000000L) != 0L)
            return jjStopAtPos(1, 33);
         break;
      case 46:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000000000L);
      case 61:
         if ((active0 & 0x800000000000L) != 0L)
            return jjStopAtPos(1, 47);
         else if ((active0 & 0x2000000000000L) != 0L)
            return jjStopAtPos(1, 49);
         else if ((active0 & 0x4000000000000L) != 0L)
            return jjStopAtPos(1, 50);
         return jjMoveStringLiteralDfa2_0(active0, 0x8000000000000L);
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x200L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000L);
      case 105:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000L);
      case 110:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x2400L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x100L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 46:
         if ((active0 & 0x20000000000000L) != 0L)
            return jjStopAtPos(2, 53);
         break;
      case 61:
         if ((active0 & 0x8000000000000L) != 0L)
            return jjStopAtPos(2, 51);
         break;
      case 100:
         if ((active0 & 0x2000L) != 0L)
            return jjStartNfaWithStates_0(2, 13, 46);
         else if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(2, 15, 46);
         break;
      case 103:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000L);
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x200L);
      case 116:
         if ((active0 & 0x400L) != 0L)
            return jjStartNfaWithStates_0(2, 10, 46);
         break;
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x100L);
      case 118:
         if ((active0 & 0x1000L) != 0L)
            return jjStartNfaWithStates_0(2, 12, 46);
         break;
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x100L) != 0L)
            return jjStartNfaWithStates_0(3, 8, 46);
         break;
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000L);
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x200L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x200L) != 0L)
            return jjStartNfaWithStates_0(4, 9, 46);
         break;
      case 110:
         if ((active0 & 0x4000L) != 0L)
            return jjStartNfaWithStates_0(4, 14, 46);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 46;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 17:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(23, 24);
                  else if (curChar == 47)
                     jjCheckNAddStates(0, 2);
                  break;
               case 47:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 20)
                        kind = 20;
                     jjCheckNAddTwoStates(36, 35);
                  }
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 17)
                        kind = 17;
                     jjCheckNAddTwoStates(31, 32);
                  }
                  break;
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 17)
                        kind = 17;
                     jjCheckNAddStates(3, 7);
                  }
                  else if (curChar == 46)
                     jjCheckNAddTwoStates(31, 35);
                  else if (curChar == 47)
                     jjAddStates(8, 9);
                  else if (curChar == 34)
                     jjCheckNAddStates(10, 12);
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                  }
                  break;
               case 1:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 20)
                        kind = 20;
                     jjCheckNAddTwoStates(36, 35);
                  }
                  else if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(2);
                  else if (curChar == 46)
                     jjCheckNAdd(35);
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 18)
                        kind = 18;
                     jjCheckNAdd(2);
                  }
                  break;
               case 46:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 20)
                        kind = 20;
                     jjCheckNAddTwoStates(36, 35);
                  }
                  else if (curChar == 46)
                     jjCheckNAdd(35);
                  break;
               case 2:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 18)
                     kind = 18;
                  jjCheckNAdd(2);
                  break;
               case 3:
                  if (curChar == 34)
                     jjCheckNAddStates(10, 12);
                  break;
               case 4:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddStates(10, 12);
                  break;
               case 6:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(10, 12);
                  break;
               case 7:
                  if (curChar == 34 && kind > 19)
                     kind = 19;
                  break;
               case 8:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(13, 16);
                  break;
               case 9:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(10, 12);
                  break;
               case 10:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 11;
                  break;
               case 11:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(9);
                  break;
               case 13:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 14:
                  if ((0x3ff000000000000L & l) != 0L && kind > 22)
                     kind = 22;
                  break;
               case 16:
                  if (curChar == 47)
                     jjAddStates(8, 9);
                  break;
               case 18:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 19:
                  if ((0x2400L & l) != 0L && kind > 5)
                     kind = 5;
                  break;
               case 20:
                  if (curChar == 10 && kind > 5)
                     kind = 5;
                  break;
               case 21:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 20;
                  break;
               case 22:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 23:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 24:
                  if (curChar == 42)
                     jjCheckNAddStates(17, 19);
                  break;
               case 25:
                  if ((0xffff7bffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(26, 24);
                  break;
               case 26:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(26, 24);
                  break;
               case 27:
                  if (curChar == 47 && kind > 6)
                     kind = 6;
                  break;
               case 29:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  jjstateSet[jjnewStateCnt++] = 29;
                  break;
               case 30:
                  if (curChar == 46)
                     jjCheckNAddTwoStates(31, 35);
                  break;
               case 31:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 17)
                     kind = 17;
                  jjCheckNAddTwoStates(31, 32);
                  break;
               case 33:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(34);
                  break;
               case 34:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 17)
                     kind = 17;
                  jjCheckNAdd(34);
                  break;
               case 35:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAddTwoStates(36, 35);
                  break;
               case 36:
                  if (curChar == 46)
                     jjCheckNAdd(35);
                  break;
               case 38:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 17)
                     kind = 17;
                  jjCheckNAddStates(3, 7);
                  break;
               case 39:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 17)
                     kind = 17;
                  jjCheckNAdd(39);
                  break;
               case 40:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(40, 41);
                  break;
               case 41:
                  if (curChar == 46)
                     jjCheckNAdd(31);
                  break;
               case 42:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(42, 43);
                  break;
               case 44:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(45);
                  break;
               case 45:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 17)
                     kind = 17;
                  jjCheckNAdd(45);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 47:
               case 35:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAddTwoStates(36, 35);
                  break;
               case 0:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 23)
                        kind = 23;
                  }
                  else if (curChar == 95)
                  {
                     if (kind > 21)
                        kind = 21;
                     jjCheckNAdd(13);
                  }
                  if ((0x7fffffe00000000L & l) != 0L)
                  {
                     if (kind > 20)
                        kind = 20;
                     jjCheckNAddTwoStates(36, 35);
                  }
                  else if ((0x7fffffeL & l) != 0L)
                  {
                     if (kind > 7)
                        kind = 7;
                     jjCheckNAdd(29);
                  }
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(20, 21);
                  break;
               case 1:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAddTwoStates(36, 35);
                  break;
               case 46:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAddTwoStates(36, 35);
                  break;
               case 4:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(10, 12);
                  break;
               case 5:
                  if (curChar == 92)
                     jjAddStates(22, 24);
                  break;
               case 6:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(10, 12);
                  break;
               case 12:
                  if (curChar != 95)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAdd(13);
                  break;
               case 13:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 21)
                     kind = 21;
                  jjCheckNAdd(13);
                  break;
               case 14:
                  if ((0x7fffffe87fffffeL & l) != 0L && kind > 22)
                     kind = 22;
                  break;
               case 15:
                  if ((0x7fffffe07fffffeL & l) != 0L && kind > 23)
                     kind = 23;
                  break;
               case 18:
                  jjAddStates(0, 2);
                  break;
               case 23:
                  jjCheckNAddTwoStates(23, 24);
                  break;
               case 25:
               case 26:
                  jjCheckNAddTwoStates(26, 24);
                  break;
               case 28:
                  if ((0x7fffffeL & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  jjCheckNAdd(29);
                  break;
               case 29:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  jjCheckNAdd(29);
                  break;
               case 32:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(25, 26);
                  break;
               case 37:
                  if ((0x7fffffe00000000L & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAddTwoStates(36, 35);
                  break;
               case 43:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(27, 28);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 4:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(10, 12);
                  break;
               case 18:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(0, 2);
                  break;
               case 23:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 25:
               case 26:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjCheckNAddTwoStates(26, 24);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 46 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   18, 19, 21, 39, 40, 41, 42, 43, 17, 22, 4, 5, 7, 4, 5, 9, 
   7, 24, 25, 27, 1, 2, 6, 8, 10, 33, 34, 44, 45, 
};
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, "\164\162\165\145", 
"\146\141\154\163\145", "\156\157\164", "\176", "\144\151\166", "\155\157\144", 
"\142\145\147\151\156", "\145\156\144", "\100", null, null, null, null, null, null, null, null, null, 
null, "\72\55", "\56", "\41", "\173", "\175", "\72", "\74\55", "\53", "\55", "\77", 
"\73", "\41\41", "\50", "\51", "\54", "\133", "\174", "\135", "\46", "\74", "\74\75", 
"\76", "\76\75", "\75\75", "\134\75\75", "\75", "\75\56\56", "\52", "\57", "\52\52", };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0x1ffffffffffff81L, 
};
static final long[] jjtoSkip = {
   0x7eL, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[46];
private final int[] jjstateSet = new int[92];
protected char curChar;
public as2jTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public as2jTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 46; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
