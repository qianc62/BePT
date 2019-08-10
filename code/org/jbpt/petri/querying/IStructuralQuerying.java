package org.jbpt.petri.querying;

import org.jbpt.petri.*;

import java.util.Set;

public interface IStructuralQuerying<F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition, M extends IMarking<F, N, P, T>> {

    public boolean areModeledAll(Set<String> setOfLabels);

    public boolean areModeledAllExpanded(Set<Set<String>> setsOfLabels);

    public boolean isModeledOne(Set<String> setOfLabels);
}
