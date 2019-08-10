/**
 * 
 */
package org.processmining.analysis.socialsuccess.bigfive;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.agreeableness.*;

/**
 * @author MvanWingerden
 * 
 */
public class Agreeableness extends Trait {

	public Agreeableness(PersonalityData inp) {
		super(inp);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void loadBehaviour() {
		b[0] = new InterestedInOthers(this);
		b[1] = new OnGoodTerms(this);
		b[2] = new ShowGratitude(this);
	}

}
