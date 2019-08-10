package org.jbpt.petri.unfolding;

import java.io.File;

import org.jbpt.petri.Flow;
import org.jbpt.petri.Marking;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;

import com.iise.bpplus.FileNameSelector;

public class QCUnfoldingTest {

	public static void main(String[] args) {
//		SoundUnfolding unfolding = new SoundUnfolding();
//		unfolding.
		
		PNMLSerializer pnmlSerializer = new PNMLSerializer();
		File folder = new File("Models/efficacy");
        File[] arModels = folder.listFiles(new FileNameSelector("pnml"));

        long nTotalCostBPP = 0;
        long nTotalCostBP = 0;
        int nTotalTrans = 0;
        int nMaxTrans = 0;
        int nMinTrans = Integer.MAX_VALUE;
        int nTotalPlace = 0;
        int nMaxPlace = 0;
        int nMinPlace = Integer.MAX_VALUE;
        int nTotalArc = 0;
        int nMaxArc = 0;
        int nMinArc = Integer.MAX_VALUE;
        
        for(int i=0; i<arModels.length; i++)
        {
            String fpModelP = arModels[i].getName();
            String filepathP = arModels[i].getAbsolutePath();
            NetSystem netP = pnmlSerializer.parse(filepathP);
            
            CompletePrefixUnfolding completePrefixUnfolding = 
            		new CompletePrefixUnfolding( netP );
            IOccurrenceNet ocn = completePrefixUnfolding.getOccurrenceNet();
            System.out.println( ocn.toString() + "\n\n" );
           
        }
	}
}
