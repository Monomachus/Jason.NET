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


package jason.environment;

import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An abstract adapter class for Environment.
 * 
 * It is overriden by the user application to define the environment "behavior".
 * 
 * <p>An example of interaction:
 * <img src="../../../uml/environmentInteraction.gif" />
 *
 * <p>The related classes:
 * <img src="../../../uml/jason.environment.gif" />
 *  
 */
public abstract class Environment { //implements EnvironmentInterface {

	// TODO: why to list? Could it be just one list of literals (not preds anymore)?
    private List percepts = Collections.synchronizedList(new ArrayList());
    private List negPercepts = Collections.synchronizedList(new ArrayList());

	/**
	 * 
	 * @uml.property name="jasonEnvironment"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private EnvironmentInterface jasonEnvironment = null;

	/**
	 * sets the jason part of the environment (saci or centralised)
	 * 
	 * @uml.property name="jasonEnvironment"
	 */
	protected void setJasonEnvironment(EnvironmentInterface je) {
		jasonEnvironment = je;
	}
	protected EnvironmentInterface getJasonEnvironment() {
		return jasonEnvironment;
	}

    
	/**
	 * @see jason.environment.EnvironmentInterface#informAgsEnvironmentChanged(java.util.Collection)
	 */
    public void informAgsEnvironmentChanged(Collection agents) {
        if (jasonEnvironment != null) {
            jasonEnvironment.informAgsEnvironmentChanged(agents);
        }
    }

	/**
	 * @see jason.environment.EnvironmentInterface#informAgsEnvironmentChanged()
	 */
    public void informAgsEnvironmentChanged() {
        if (jasonEnvironment != null) {
            jasonEnvironment.informAgsEnvironmentChanged();
        }
    }

	/**
	 * Returns percepts list.
	 * 
	 * @uml.property name="percepts"
	 */
	public List getPercepts() {
		return percepts;
	}

	/**
	 * Returns percepts list for an agent.
	 * 
	 * @uml.property name="percepts"
	 */
    public List getPercepts(String agName) {
        return percepts;
    }

    /** Returns negative percepts list.  */
    public List getNegativePercepts() {
        return negPercepts;
    }

	/**
	 * Returns negative percepts list for an agent.
	 * 
	 * @uml.property name="percepts"
	 */
    public List getNegativePercepts(String agName) {
        return negPercepts;
    }

    /**
     * called by the agent architecture to execute an action on the environment.
     */
    public boolean executeAction(String agName, Term act) {
        return true;
    }
}
