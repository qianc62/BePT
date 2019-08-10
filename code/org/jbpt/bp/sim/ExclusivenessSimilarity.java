package org.jbpt.bp.sim;

import org.jbpt.alignment.Alignment;
import org.jbpt.bp.RelSet;
import org.jbpt.bp.RelSetType;
import org.jbpt.hypergraph.abs.IEntity;
import org.jbpt.hypergraph.abs.IEntityModel;

/**
 * Scores two models by only assessing the overlap of their
 * exclusiveness relation.
 *
 * @author matthias.weidlich
 */
public class ExclusivenessSimilarity<R extends RelSet<M, N>, M extends IEntityModel<N>, N extends IEntity> extends AbstractRelSetSimilarity<R, M, N> {

    public double score(Alignment<R, N> alignment) {
        double in1 = super.getSizeOfRelation(alignment.getFirstModel(), RelSetType.Exclusive);
        double in2 = super.getSizeOfRelation(alignment.getSecondModel(), RelSetType.Exclusive);

        double intersection = super.getSizeOfIntersectionOfRelation(alignment, RelSetType.Exclusive);

        return (intersection > 0) ? (intersection / (in1 + in2 - intersection)) : 0;
    }

    public double scoreDice(Alignment<R, N> alignment) {
        double in1 = super.getSizeOfRelation(alignment.getFirstModel(), RelSetType.Exclusive);
        double in2 = super.getSizeOfRelation(alignment.getSecondModel(), RelSetType.Exclusive);

        double intersection = super.getSizeOfIntersectionOfRelation(alignment, RelSetType.Exclusive);

        return (in1 + in2 > 0) ? (2 * intersection / (in1 + in2)) : 0;
    }

    public int[] getSizeOfIntersectionAndUnion(Alignment<R, N> alignment) {
        int in1 = super.getSizeOfRelation(alignment.getFirstModel(), RelSetType.Exclusive);
        int in2 = super.getSizeOfRelation(alignment.getSecondModel(), RelSetType.Exclusive);
        int intersection = super.getSizeOfIntersectionOfRelation(alignment, RelSetType.Exclusive);
        return new int[]{intersection, in1 + in2 - intersection};
    }
}
