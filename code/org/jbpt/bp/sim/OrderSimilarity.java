package org.jbpt.bp.sim;

import org.jbpt.alignment.Alignment;
import org.jbpt.bp.RelSet;
import org.jbpt.bp.RelSetType;
import org.jbpt.hypergraph.abs.IEntity;
import org.jbpt.hypergraph.abs.IEntityModel;

/**
 * Scores two models by only assessing the overlap of their
 * order relation.
 * 
 * @author matthias.weidlich
 *
 */
public class OrderSimilarity<R extends RelSet<M, N>, M extends IEntityModel<N>, N extends IEntity> extends AbstractRelSetSimilarity<R,M,N> {

	public double score(Alignment<R,N> alignment) {
		double in1 = super.getSizeOfRelation(alignment.getFirstModel(), RelSetType.Order);
		double in2 = super.getSizeOfRelation(alignment.getSecondModel(), RelSetType.Order);
		
		double intersection = super.getSizeOfIntersectionOfRelation(alignment, RelSetType.Order);
		
		return (intersection > 0) ? (intersection / (in1 + in2 - intersection)) : 0;
	}	

	public double scoreDice(Alignment<R,N> alignment) {
		double in1 = super.getSizeOfRelation(alignment.getFirstModel(), RelSetType.Order);
		double in2 = super.getSizeOfRelation(alignment.getSecondModel(), RelSetType.Order);
		
		double intersection = super.getSizeOfIntersectionOfRelation(alignment, RelSetType.Order);
		
		return (in1 + in2 > 0) ? (2*intersection / (in1 + in2)) : 0;
	}

	public int[] getSizeOfIntersectionAndUnion(Alignment<R, N> alignment) {
		int in1 = super.getSizeOfRelation(alignment.getFirstModel(), RelSetType.Order);
		int in2 = super.getSizeOfRelation(alignment.getSecondModel(), RelSetType.Order);
		int intersection = super.getSizeOfIntersectionOfRelation(alignment, RelSetType.Order);
		return new int[]{intersection, in1 + in2 - intersection};
	}
}
