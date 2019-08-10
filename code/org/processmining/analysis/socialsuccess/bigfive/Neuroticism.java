/**
 * 
 */
package org.processmining.analysis.socialsuccess.bigfive;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.neuroticism.*;

/**
 * @author MvanWingerden
 * 
 */
public class Neuroticism extends Trait {

	/**
	 * @param inp
	 */
	public Neuroticism(PersonalityData inp) {
		super(inp);
		// TODO Auto-generated constructor stub
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.socialsuccess.bigfive.Trait#loadBehaviour()
	 */
	@Override
	protected void loadBehaviour() {
		b[0] = new IritatedEasily(this);
		b[1] = new OverwhelmedByEmotions(this);
	}

}
