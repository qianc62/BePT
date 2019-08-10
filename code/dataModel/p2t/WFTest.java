package dataModel.p2t;

import java.io.IOException;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.unfolding.OccurrenceNet;
import org.junit.Test;

public class WFTest {

	@Test
	public void test() throws IOException {
		NetSystem sys = new NetSystem();
		
		/*Place p1 = new Place("p1");
		Place p2 = new Place("p2");
		Place p3 = new Place("p3");
		Place p4 = new Place("p4");
		Transition t1 = new Transition("t1");
		Transition t2 = new Transition("t2");
		Transition t3 = new Transition("t3");
		Transition t4 = new Transition("t4");
		
		sys.addFlow(p1, t1);
		sys.addFlow(t1, p2);
		sys.addFlow(p2, t2);
		sys.addFlow(p2, t3);
		sys.addFlow(t2, p3);
		sys.addFlow(t3, p3);
		sys.addFlow(p3, t4);
		sys.addFlow(t4, p4);*/
		
		Place p1 = new Place("p1");
		Place p2 = new Place("p2");
		Place p3 = new Place("p3");
		Place p4 = new Place("p4");
		Place p5 = new Place("p5");
		Place p6 = new Place("p6");
		Place p7 = new Place("p7");
		Place p8 = new Place("p8");
		Place p9 = new Place("p9");
		
		Transition t1 = new Transition("t1");
		Transition t2 = new Transition("t2");
		Transition t3 = new Transition("t3");
		Transition t4 = new Transition("t4");
		Transition t5 = new Transition("t5");
		Transition t6 = new Transition("t6");
		Transition t7 = new Transition("t7");
		Transition t8 = new Transition("t8");
		Transition t9 = new Transition("t9");
		
		sys.addFlow(p1,t1);
		sys.addFlow(t1,p2);
		sys.addFlow(t1,p6);
		sys.addFlow(p2,t2);
		sys.addFlow(t2,p3);
		sys.addFlow(p3,t3);
		sys.addFlow(t3,p4);
		sys.addFlow(p4,t9);
		sys.addFlow(t9,p9);
		sys.addFlow(p1,t4);
		sys.addFlow(t4,p5);
		sys.addFlow(t4,p2);
		sys.addFlow(p5,t5);
		sys.addFlow(t5,p6);
		sys.addFlow(p6,t6);
		sys.addFlow(t6,p7);
		sys.addFlow(p7,t7);
		sys.addFlow(t7,p8);
		sys.addFlow(p8,t9);
		sys.addFlow(p8,t8);
		sys.addFlow(t8,p5);
		
		sys.loadNaturalMarking();
		
//		IOUtils.invokeDOT(".", "sys.png", "/usr/local/bin/dot");
		
		WFnet2Processes wf2pi = new WFnet2Processes(sys);
		
		System.out.println(wf2pi.getPetriNetPaths());
		
		int c=1;
		for (OccurrenceNet net : wf2pi.getNets()) {
//			IOUtils.invokeDOT(".", "pi"+(c++)+".png", net.toDOT());
			System.out.println(net);
		}
	}

}
