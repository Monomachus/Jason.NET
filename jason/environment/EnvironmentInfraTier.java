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
// CVS information:
//   $Date$
//   $Revision$
//   $Log$
//   Revision 1.2  2006/02/27 18:46:26  jomifred
//   creation of the RuntimeServices interface
//
//   Revision 1.1  2006/02/17 13:16:16  jomifred
//   change a lot of method/classes names and improve some comments
//
//   Revision 1.5  2005/10/30 16:07:33  jomifred
//   add comments
//
//   Revision 1.4  2005/08/12 22:26:08  jomifred
//   add cvs keywords
//
//
//----------------------------------------------------------------------------


package jason.environment;

import jason.runtime.RuntimeServicesInfraTier;

import java.util.Collection;

/** 
 * The infrastructure tier interface for Environment.
 * 
 *  <p>It is implemented by jason to ecapsulate the communication side 
 *  of the distributed/centralised environment, so the user environment can call 
 *  "informAgsEnvironmentChanged" either in centralised or distributed
 *  executions.  
 * 
 * <p>An example of interaction:
 * <img src="../../../uml/environmentInteraction.gif" />
 *
 * <p>The related classes:
 * <img src="../../../uml/jason.environment.gif" />
 */
public interface EnvironmentInfraTier {

    /** 
     * sends a message to all agents notifying them that the environment has changed 
     * (called by the user environment). 
     */
    public void informAgsEnvironmentChanged();

    /**
     * Sends a message to a set of agents notifying them that the environment has changed. 
     * The collection has the agents' names. 
     * (called by the user environment). 
     */
    public void informAgsEnvironmentChanged(Collection agents);

    /** gets an object with infrastructure runtime services */
    public RuntimeServicesInfraTier getRuntimeServices();
}
