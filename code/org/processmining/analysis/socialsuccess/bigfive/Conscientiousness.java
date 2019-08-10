package org.processmining.analysis.socialsuccess.bigfive;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.conscientiousness.*;

public class Conscientiousness extends Trait {

	public Conscientiousness(PersonalityData inp) {
		super(inp);
	}

	@Override
	protected void loadBehaviour() {
		b[0] = new OrderAndRegularity(this);
		b[1] = new PayAttention(this);
		b[2] = new TidyUp(this);
	}

}
