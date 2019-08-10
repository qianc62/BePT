package qc;

import java.util.ArrayList;

import dataModel.dsynt.DSynTSentence;
import sentenceRealization.SurfaceRealizer;
import textPlanning.TextPlanner;

public class QcPrintDsynT {

	public static int num = 0;
	public static SurfaceRealizer surfaceRealizer = null;
	
	public static String print( DSynTSentence dsynt ) {
		ArrayList<DSynTSentence> sentencePlan = new ArrayList<DSynTSentence>( 1 ); 
		sentencePlan.add( dsynt );
		
		if( surfaceRealizer == null ){
			surfaceRealizer = new SurfaceRealizer();
		}
		
		String surfaceText = surfaceRealizer.realizePlan( sentencePlan );
		surfaceText = surfaceText.replace( '\n' , '\0' );
		System.out.println( "【" + surfaceText + "】" );
		
		return surfaceText;
	}
}
