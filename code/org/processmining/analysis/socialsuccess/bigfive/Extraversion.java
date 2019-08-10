package org.processmining.analysis.socialsuccess.bigfive;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.extraversion.*;

public class Extraversion extends Trait {

	public Extraversion(PersonalityData inp) {
		super(inp);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void loadBehaviour() {
		b[0] = new ModeratingGroups(this);
		b[1] = new PrivatePerson(this);
		b[2] = new StartConversations(this);
	}

}
