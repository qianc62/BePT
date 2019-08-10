package adeorder;

import org.processmining.framework.models.petrinet.PetriNet;
import java.util.Comparator;

public class AdequateOrder_SPF implements Comparator {
    public int compare(Object o1, Object o2) {
        PetriNet p1 = (PetriNet) o1;
        PetriNet p2 = (PetriNet) o2;

        int num1 = p1.getNodes().size();
        int num2 = p2.getNodes().size();

        return num1 - num2;
    }
}
