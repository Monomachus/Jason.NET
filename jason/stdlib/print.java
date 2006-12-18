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

package jason.stdlib;

import jason.asSemantics.InternalAction;


/**
  <p>Internal action: <b><code>.print</code></b>.
  
  <p>Description: used for printing messages to the console where the
  system is running. It receives any number of parameters, which can
  be not only strings but any AgentSpeak term (including
  variables). Terms are grounded according to the current unifying
  function before being printed out. No new line is printed after the
  parameters.

  <p> The precise format and output device of the message is defined
  by the Java logging configuration as defined in the
  <code>logging.properties</code> file in the project directory.
  
  <p>Parameters:<ul>
  
  <li>+arg[0] ... +arg[n] (any term): the term to be printed.<br/>

  </ul>
  
  <p>Example:<ul> 

  <li> <code>.print(1,X,"bla")</code>: prints the number 1, the value
  of variable X and the string "bla" in the console.</li>

  </ul>

  @see jason.stdlib.println

*/
public class print extends println implements InternalAction {

    @Override
	protected String getNewLine() {
		return "";
	}
}
